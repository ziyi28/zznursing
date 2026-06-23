package com.zzyl.common.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "openai.deepseek")
public class DeepseekAIProperties {
    private String apiKey;
    private String baseUrl;
    private String model;
}