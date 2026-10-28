package software_market_agent.model.internal;

import lombok.Data;

@Data
public class IndexRequest {

    private Integer postId;
    private Long version;
    private boolean delete;
}
