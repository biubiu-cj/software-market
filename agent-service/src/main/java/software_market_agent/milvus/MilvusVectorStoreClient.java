package software_market_agent.milvus;

import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.grpc.DataType;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.SearchResults;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.dml.UpsertParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.SearchResultsWrapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software_market_agent.config.AgentProperties;
import software_market_agent.exception.RagUnavailableException;
import software_market_agent.model.milvus.VectorDocument;
import software_market_agent.model.milvus.VectorSearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Milvus 2.4.x 客户端。所有 Milvus 细节收敛在这里，AgentScope 只依赖 VectorStoreClient 接口。
 */
@Slf4j
@Component
public class MilvusVectorStoreClient implements VectorStoreClient {

    private static final int SUCCESS = 0;

    private final AgentProperties properties;

    private final String collection;

    private final int dimension;

    /**
     * Milvus SDK 在构造时就会尝试建立连接；这里延迟创建，
     * 保证 Milvus 未启动时 agent-service 仍可启动并返回降级状态。
     */
    private volatile MilvusServiceClient milvusClient;

    private volatile boolean collectionReady;

    public MilvusVectorStoreClient(AgentProperties properties) {
        this.properties = properties;
        this.collection = properties.getMilvus().getCollection();
        // 以 Embedding 配置为唯一维度来源，避免两处配置不一致
        this.dimension = properties.getEmbedding().getDimension();
    }

    private MilvusServiceClient client() {
        MilvusServiceClient current = milvusClient;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            if (milvusClient == null) {
                try {
                    ConnectParam connectParam = ConnectParam.newBuilder()
                            .withHost(properties.getMilvus().getHost())
                            .withPort(properties.getMilvus().getPort())
                            .withConnectTimeout(properties.getMilvus().getConnectTimeout().toMillis(), TimeUnit.MILLISECONDS)
                            .build();
                    milvusClient = new MilvusServiceClient(connectParam);
                } catch (Exception e) {
                    throw new RagUnavailableException("Milvus 连接失败: " + e.getMessage(), e);
                }
            }
            return milvusClient;
        }
    }

    @PostConstruct
    public void init() {
        if (!properties.getMilvus().isEnsureOnStartup()) {
            return;
        }
        try {
            ensureCollection();
        } catch (Exception e) {
            // Milvus 未启动时不影响 agent-service 启动，首次请求会再次尝试
            log.warn("Milvus 初始化失败，将在首次请求时重试: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void close() {
        MilvusServiceClient current = milvusClient;
        if (current == null) {
            return;
        }
        try {
            current.close(1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void upsertBatch(List<VectorDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }
        ensureCollection();
        int batchSize = Math.max(1, properties.getRag().getUpsertBatchSize());
        for (int start = 0; start < documents.size(); start += batchSize) {
            int end = Math.min(start + batchSize, documents.size());
            upsertOneBatch(documents.subList(start, end));
        }
    }

    @Override
    public void deleteByPostId(Integer postId) {
        if (postId == null) {
            return;
        }
        ensureCollection();
        try {
            DeleteParam param = DeleteParam.newBuilder()
                    .withCollectionName(collection)
                    .withExpr("postId == " + postId)
                    .build();
            R<MutationResult> response = client().delete(param);
            checkStatus(response, "删除帖子向量失败: postId=" + postId);
            log.info("Milvus 删除完成: postId={}", postId);
        } catch (RagUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new RagUnavailableException("Milvus 删除失败: postId=" + postId, e);
        }
    }

    @Override
    public List<VectorSearchResult> search(float[] queryVector,
                                           int topK,
                                           double threshold,
                                           Integer excludePostId) {
        ensureCollection();
        if (queryVector == null || queryVector.length != dimension) {
            throw new RagUnavailableException("查询向量维度不正确");
        }
        int limit = topK > 0 ? topK : properties.getRag().getTopK();
        StringBuilder expr = new StringBuilder("status == \"ACTIVE\"");
        if (excludePostId != null) {
            expr.append(" and postId != ").append(excludePostId);
        }
        try {
            List<Float> vector = toFloatList(queryVector);
            SearchParam param = SearchParam.newBuilder()
                    .withCollectionName(collection)
                    .withMetricType(MetricType.COSINE)
                    .withVectorFieldName("embedding")
                    .withTopK(limit)
                    .withExpr(expr.toString())
                    .withOutFields(List.of(
                            "postId", "commentId", "title", "content", "category", "tag",
                            "sourceType", "createdAt", "status", "docVersion", "chunkIndex"
                    ))
                    .withFloatVectors(List.of(vector))
                    .withParams("{}")
                    .withConsistencyLevel(ConsistencyLevelEnum.BOUNDED)
                    .build();

            R<SearchResults> response = client().search(param);
            checkStatus(response, "Milvus 检索失败");
            if (response.getData() == null || response.getData().getResults() == null) {
                return List.of();
            }
            SearchResultsWrapper wrapper = new SearchResultsWrapper(response.getData().getResults());
            List<SearchResultsWrapper.IDScore> scores = wrapper.getIDScore(0);
            if (scores == null || scores.isEmpty()) {
                return List.of();
            }
            List<?> postIds = wrapper.getFieldData("postId", 0);
            List<?> commentIds = wrapper.getFieldData("commentId", 0);
            List<?> titles = wrapper.getFieldData("title", 0);
            List<?> contents = wrapper.getFieldData("content", 0);
            List<?> categories = wrapper.getFieldData("category", 0);
            List<?> tags = wrapper.getFieldData("tag", 0);
            List<?> sourceTypes = wrapper.getFieldData("sourceType", 0);
            List<?> createdAts = wrapper.getFieldData("createdAt", 0);
            List<?> statuses = wrapper.getFieldData("status", 0);
            List<?> docVersions = wrapper.getFieldData("docVersion", 0);
            List<?> chunkIndexes = wrapper.getFieldData("chunkIndex", 0);

            List<VectorSearchResult> results = new ArrayList<>();
            for (int i = 0; i < scores.size(); i++) {
                double score = scores.get(i).getScore();
                if (score < threshold) {
                    continue;
                }
                VectorSearchResult item = new VectorSearchResult();
                item.setId(scores.get(i).getStrID());
                item.setScore(score);
                item.setPostId(asLong(postIds, i));
                item.setCommentId(asLong(commentIds, i));
                item.setTitle(asString(titles, i));
                item.setContent(asString(contents, i));
                item.setCategory(asString(categories, i));
                item.setTag(asString(tags, i));
                item.setSourceType(asString(sourceTypes, i));
                item.setCreatedAt(asLong(createdAts, i));
                item.setStatus(asString(statuses, i));
                item.setDocVersion(asLong(docVersions, i));
                item.setChunkIndex(asLong(chunkIndexes, i));
                if (excludePostId == null || item.getPostId() != excludePostId) {
                    results.add(item);
                }
            }
            log.info("Milvus 检索完成: topK={}, threshold={}, hits={}, kept={}",
                    limit, threshold, scores.size(), results.size());
            return results;
        } catch (RagUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new RagUnavailableException("Milvus 检索失败", e);
        }
    }

    private void upsertOneBatch(List<VectorDocument> documents) {
        try {
            List<InsertParam.Field> fields = new ArrayList<>();
            fields.add(new InsertParam.Field("id", documents.stream().map(VectorDocument::getId).toList()));
            fields.add(new InsertParam.Field("embedding",
                    documents.stream().map(doc -> toFloatList(doc.getEmbedding())).toList()));
            fields.add(new InsertParam.Field("postId", documents.stream().map(VectorDocument::getPostId).toList()));
            fields.add(new InsertParam.Field("commentId", documents.stream().map(VectorDocument::getCommentId).toList()));
            fields.add(new InsertParam.Field("title", documents.stream().map(VectorDocument::getTitle).toList()));
            fields.add(new InsertParam.Field("content", documents.stream().map(VectorDocument::getContent).toList()));
            fields.add(new InsertParam.Field("category", documents.stream().map(VectorDocument::getCategory).toList()));
            fields.add(new InsertParam.Field("tag", documents.stream().map(VectorDocument::getTag).toList()));
            fields.add(new InsertParam.Field("sourceType", documents.stream().map(VectorDocument::getSourceType).toList()));
            fields.add(new InsertParam.Field("createdAt", documents.stream().map(VectorDocument::getCreatedAt).toList()));
            fields.add(new InsertParam.Field("status", documents.stream().map(VectorDocument::getStatus).toList()));
            fields.add(new InsertParam.Field("docVersion", documents.stream().map(VectorDocument::getDocVersion).toList()));
            fields.add(new InsertParam.Field("chunkIndex", documents.stream().map(VectorDocument::getChunkIndex).toList()));

            UpsertParam param = UpsertParam.newBuilder()
                    .withCollectionName(collection)
                    .withFields(fields)
                    .build();
            R<MutationResult> response = client().upsert(param);
            checkStatus(response, "Milvus Upsert 失败");
            log.info("Milvus Upsert 完成: count={}", documents.size());
        } catch (RagUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new RagUnavailableException("Milvus Upsert 失败", e);
        }
    }

    private synchronized void ensureCollection() {
        if (collectionReady) {
            return;
        }
        try {
            R<Boolean> hasCollection = client().hasCollection(
                    HasCollectionParam.newBuilder().withCollectionName(collection).build()
            );
            checkStatus(hasCollection, "检查 Milvus collection 失败");
            boolean exists = Boolean.TRUE.equals(hasCollection.getData());
            if (!exists) {
                createCollection();
            }
            loadCollection();
            collectionReady = true;
            log.info("Milvus collection 就绪: {}", collection);
        } catch (RagUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new RagUnavailableException("Milvus collection 初始化失败", e);
        }
    }

    private void createCollection() {
        List<FieldType> fields = List.of(
                FieldType.newBuilder().withName("id").withDataType(DataType.VarChar)
                        .withPrimaryKey(true).withAutoID(false).withMaxLength(256).build(),
                FieldType.newBuilder().withName("embedding").withDataType(DataType.FloatVector)
                        .withDimension(dimension).build(),
                FieldType.newBuilder().withName("postId").withDataType(DataType.Int64).build(),
                FieldType.newBuilder().withName("commentId").withDataType(DataType.Int64).build(),
                FieldType.newBuilder().withName("title").withDataType(DataType.VarChar).withMaxLength(512).build(),
                FieldType.newBuilder().withName("content").withDataType(DataType.VarChar).withMaxLength(8192).build(),
                FieldType.newBuilder().withName("category").withDataType(DataType.VarChar).withMaxLength(64).build(),
                FieldType.newBuilder().withName("tag").withDataType(DataType.VarChar).withMaxLength(128).build(),
                FieldType.newBuilder().withName("sourceType").withDataType(DataType.VarChar).withMaxLength(32).build(),
                FieldType.newBuilder().withName("createdAt").withDataType(DataType.Int64).build(),
                FieldType.newBuilder().withName("status").withDataType(DataType.VarChar).withMaxLength(16).build(),
                FieldType.newBuilder().withName("docVersion").withDataType(DataType.Int64).build(),
                FieldType.newBuilder().withName("chunkIndex").withDataType(DataType.Int64).build()
        );
        CreateCollectionParam createParam = CreateCollectionParam.newBuilder()
                .withCollectionName(collection)
                .withDescription("software-market RAG knowledge")
                .withShardsNum(2)
                .withConsistencyLevel(ConsistencyLevelEnum.BOUNDED)
                .withFieldTypes(fields)
                .build();
        R<?> createResponse = client().createCollection(createParam);
        checkStatus(createResponse, "创建 Milvus collection 失败");

        CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                .withCollectionName(collection)
                .withFieldName("embedding")
                .withIndexType(IndexType.AUTOINDEX)
                .withMetricType(MetricType.COSINE)
                .withSyncMode(Boolean.TRUE)
                .build();
        R<?> indexResponse = client().createIndex(indexParam);
        checkStatus(indexResponse, "创建 Milvus 索引失败");
        log.info("Milvus collection 创建完成: {}, dimension={}", collection, dimension);
    }

    private void loadCollection() {
        LoadCollectionParam loadParam = LoadCollectionParam.newBuilder()
                .withCollectionName(collection)
                .withSyncLoad(Boolean.TRUE)
                .build();
        R<?> response = client().loadCollection(loadParam);
        checkStatus(response, "加载 Milvus collection 失败");
    }

    private void checkStatus(R<?> response, String message) {
        if (response == null || response.getStatus() == null || response.getStatus() != SUCCESS) {
            String detail = response == null ? "null response"
                    : response.getMessage() + " (status=" + response.getStatus() + ")";
            throw new RagUnavailableException(message + ": " + detail);
        }
    }

    private List<Float> toFloatList(float[] vector) {
        List<Float> values = new ArrayList<>(vector.length);
        for (float v : vector) {
            values.add(v);
        }
        return values;
    }

    private long asLong(List<?> values, int index) {
        if (values == null || index >= values.size() || values.get(index) == null) {
            return 0L;
        }
        Object value = values.get(index);
        return value instanceof Number number ? number.longValue() : 0L;
    }

    private String asString(List<?> values, int index) {
        if (values == null || index >= values.size() || values.get(index) == null) {
            return "";
        }
        return String.valueOf(values.get(index));
    }
}
