package tg.masal.telegramgptbot.service;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;


@Service
@PropertySource("application.properties")
public class CharGPTService {
	private final String apiKey;
	private final String apiUrl;
	private final ObjectMapper objectMapper;
	
	public CharGPTService(@Value("${chatgpt.apiKey}") String apiKey, @Value("${chatgpt.apiUrl}") String apiUrl, ObjectMapper objectMapper) {
		this.apiKey = apiKey;
		this.apiUrl = apiUrl;
		this.objectMapper = objectMapper;
	}
	
	public String generateResponse(String prompt) throws IOException {
        String json = objectMapper.writeValueAsString(new ChatGPTRequest(prompt));
        HttpEntity httpEntity = new StringEntity(json);

        HttpPost httpPost = new HttpPost(apiUrl);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
        httpPost.setEntity(httpEntity);

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponse httpResponse = httpClient.execute(httpPost);

        String responseJson = EntityUtils.toString(httpResponse.getEntity());
        JsonNode responseNode = objectMapper.readTree(responseJson);
        System.out.println(responseJson);
        return responseNode.get("choices").get(0).get("text").asText();
    }
	
	private static class ChatGPTRequest {
        private final String prompt;

        public ChatGPTRequest(String prompt) {
            this.prompt = prompt;
        }

        public String getPrompt() {
            return prompt;
        }
    }
}
