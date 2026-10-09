package software_market.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 与各处 getAvatarUploadDir() 保持一致，避免保存目录和访问目录不是同一个
        String avatarLocation = new File(uploadDir, "avatar")
                .toPath()
                .toAbsolutePath()
                .normalize()
                .toString()
                .replace('\\', '/');

        registry.addResourceHandler("/avatar/**")
                .addResourceLocations("file:" + avatarLocation + "/");
    }
}
