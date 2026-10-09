package software_market.config.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software_market.utils.Result;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class UnauthorizedEntryPoint {
    private final ObjectMapper objectMapper;

    public void handle(HttpServletRequest request, HttpServletResponse response, Exception e) throws IOException {
        log.warn("Unauthorized request: {}, error: {}", request.getRequestURI(), e.getMessage());
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(401, e.getMessage())));
    }
}
