package software_market_agent.rag;

import org.junit.jupiter.api.Test;
import software_market_agent.datasource.RagSnapshotReader;
import software_market_agent.embedding.EmbeddingClient;
import software_market_agent.milvus.VectorStoreClient;
import software_market_agent.model.milvus.VectorDocument;
import software_market_agent.model.post.PostSnapshot;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RagIndexServiceTest {

    @Test
    void reindexDeletesOldVectorsBeforeUpsertAndIsIdempotent() {
        RagSnapshotReader reader = mock(RagSnapshotReader.class);
        RagDocumentService documentService = mock(RagDocumentService.class);
        EmbeddingClient embeddingClient = mock(EmbeddingClient.class);
        VectorStoreClient vectorStoreClient = mock(VectorStoreClient.class);

        PostSnapshot snapshot = new PostSnapshot();
        snapshot.setPostId(1);
        VectorDocument doc = new VectorDocument();
        doc.setId("post:1:comment:2:v1");
        doc.setPostId(1);
        doc.setContent("content");

        when(reader.read(1)).thenReturn(snapshot);
        when(documentService.build(snapshot, 1L)).thenReturn(List.of(doc));
        when(embeddingClient.embedBatch(any())).thenReturn(List.of(new float[1024]));

        RagIndexService service = new RagIndexService(reader, documentService, embeddingClient, vectorStoreClient);
        service.reindex(1, 1L);
        service.reindex(1, 1L);

        verify(vectorStoreClient, times(2)).deleteByPostId(1);
        verify(vectorStoreClient, times(2)).upsertBatch(any());
        assertEquals(1024, doc.getEmbedding().length);
    }

    @Test
    void reindexDeletesVectorsWhenPostNoLongerExists() {
        RagSnapshotReader reader = mock(RagSnapshotReader.class);
        RagDocumentService documentService = mock(RagDocumentService.class);
        EmbeddingClient embeddingClient = mock(EmbeddingClient.class);
        VectorStoreClient vectorStoreClient = mock(VectorStoreClient.class);
        when(reader.read(9)).thenReturn(null);

        RagIndexService service = new RagIndexService(reader, documentService, embeddingClient, vectorStoreClient);
        service.reindex(9, 2L);

        verify(vectorStoreClient).deleteByPostId(9);
    }
}
