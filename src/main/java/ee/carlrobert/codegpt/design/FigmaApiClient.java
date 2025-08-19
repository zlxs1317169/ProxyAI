package ee.carlrobert.codegpt.design;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Figma API客户端
 * 用于与Figma API交互，获取设计数据
 */
public class FigmaApiClient {
    
    private static final String FIGMA_API_BASE = "https://api.figma.com/v1";
    private final String accessToken;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public FigmaApiClient(String accessToken) {
        this.accessToken = accessToken;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 获取Figma文件信息
     */
    public JsonNode getFile(String fileKey) throws IOException, InterruptedException {
        String url = FIGMA_API_BASE + "/files/" + fileKey;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-Figma-Token", accessToken)
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readTree(response.body());
        } else {
            throw new RuntimeException("Failed to fetch Figma file: " + response.statusCode());
        }
    }
    
    /**
     * 获取文件中的图片
     */
    public Map<String, String> getImages(String fileKey, String[] nodeIds) throws IOException, InterruptedException {
        StringBuilder url = new StringBuilder(FIGMA_API_BASE + "/images/" + fileKey + "?ids=");
        url.append(String.join(",", nodeIds));
        url.append("&format=svg");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url.toString()))
                .header("X-Figma-Token", accessToken)
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            JsonNode jsonResponse = objectMapper.readTree(response.body());
            JsonNode images = jsonResponse.get("images");
            
            Map<String, String> imageUrls = new HashMap<>();
            images.fields().forEachRemaining(entry -> {
                imageUrls.put(entry.getKey(), entry.getValue().asText());
            });
            
            return imageUrls;
        } else {
            throw new RuntimeException("Failed to fetch images: " + response.statusCode());
        }
    }
    
    /**
     * 提取设计令牌（颜色、字体等）
     */
    public Map<String, Object> extractDesignTokens(JsonNode fileData) {
        Map<String, Object> tokens = new HashMap<>();
        
        // 提取颜色
        Map<String, String> colors = new HashMap<>();
        JsonNode styles = fileData.get("styles");
        if (styles != null) {
            styles.fields().forEachRemaining(entry -> {
                JsonNode style = entry.getValue();
                if ("FILL".equals(style.get("styleType").asText())) {
                    String name = style.get("name").asText();
                    // 这里需要根据实际的Figma API响应结构来提取颜色值
                    colors.put(name, "#000000"); // 占位符
                }
            });
        }
        
        tokens.put("colors", colors);
        return tokens;
    }
}