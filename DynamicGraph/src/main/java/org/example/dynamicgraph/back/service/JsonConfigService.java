package org.example.dynamicgraph.back.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dynamicgraph.back.config.TenantGraphConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JSON配置解析服务
 */
public class JsonConfigService {
    
    private final ObjectMapper objectMapper;
    
    public JsonConfigService() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 从JSON字符串解析配置
     */
    public TenantGraphConfig parseFromJson(String json) throws IOException {
        return objectMapper.readValue(json, TenantGraphConfig.class);
    }
    
    /**
     * 从JSON文件解析配置
     */
    public TenantGraphConfig parseFromJsonFile(String filePath) throws IOException {
        String json = Files.readString(Path.of(filePath));
        return parseFromJson(json);
    }
    
    /**
     * 将配置对象转换为JSON字符串
     */
    public String toJson(TenantGraphConfig config) throws IOException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);
    }
    
    /**
     * 将配置对象保存为JSON文件
     */
    public void saveToJsonFile(TenantGraphConfig config, String filePath) throws IOException {
        String json = toJson(config);
        Files.writeString(Path.of(filePath), json);
    }
}
