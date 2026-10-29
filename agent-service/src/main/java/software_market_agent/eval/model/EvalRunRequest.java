package software_market_agent.eval.model;

import lombok.Data;

@Data
public class EvalRunRequest {

    /** 支持 classpath: 前缀或本地路径；为空时使用 agent.eval.default-dataset */
    private String datasetPath;
    private EvalConfig config;
}
