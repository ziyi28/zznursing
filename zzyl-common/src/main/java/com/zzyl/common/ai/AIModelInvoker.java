package com.zzyl.common.ai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ResponseFormatJsonObject;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: ziyi
 * @Date: 2026/5/22 17:13
 * @Version: v1.0.0
 * @Description: TODO
 **/
@Component
@Slf4j
public class AIModelInvoker {
    @Autowired
    private DeepseekAIProperties deepseekAIProperties;
    public String DeepseekInvoker(String prompt){
        OpenAIClient client = OpenAIOkHttpClient.builder()
                .apiKey(deepseekAIProperties.getApiKey()) //将your_APIKey替换为真实值，如何获取API Key请查看https://cloud.baidu.com/doc/WENXINWORKSHOP/s/Um2wxbaps#步骤二-获取api-key
                .baseUrl(deepseekAIProperties.getBaseUrl()) //千帆ModelBuilder平台地址
                .build();

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .addUserMessage(prompt) // 对话messages信息
                .model(deepseekAIProperties.getModel()) // 模型对应的model值，请查看支持的模型列表：https://cloud.baidu.com/doc/WENXINWORKSHOP/s/wm7ltcvgc
                .responseFormat(ChatCompletionCreateParams.ResponseFormat.ofJsonObject(ResponseFormatJsonObject.builder().build()))
                .build();

        ChatCompletion chatCompletion = client.chat().completions().create(params);
        String result = chatCompletion.choices().get(0).message().content().orElseGet(() -> "");
        return result;

    }
}
