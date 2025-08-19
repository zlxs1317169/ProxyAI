package ee.carlrobert.codegpt.design;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Figma资源管理器
 * 用于管理从Figma导入的设计资源，包括图标、颜色、字体等
 */
public class FigmaResourceManager {
    
    private static final String DESIGN_RESOURCES_PATH = "src/main/resources/design";
    private static final String ICONS_PATH = DESIGN_RESOURCES_PATH + "/icons";
    private static final String COLORS_PATH = DESIGN_RESOURCES_PATH + "/colors";
    
    private Map<String, String> colorTokens;
    private Map<String, String> iconPaths;
    
    public FigmaResourceManager() {
        this.colorTokens = new HashMap<>();
        this.iconPaths = new HashMap<>();
        initializeDirectories();
        loadDesignTokens();
    }
    
    /**
     * 初始化设计资源目录
     */
    private void initializeDirectories() {
        try {
            Files.createDirectories(Paths.get(DESIGN_RESOURCES_PATH));
            Files.createDirectories(Paths.get(ICONS_PATH));
            Files.createDirectories(Paths.get(COLORS_PATH));
        } catch (IOException e) {
            System.err.println("Failed to create design directories: " + e.getMessage());
        }
    }
    
    /**
     * 加载设计令牌
     */
    private void loadDesignTokens() {
        // 从Figma导出的设计令牌
        colorTokens.put("primary", "#007bff");
        colorTokens.put("secondary", "#6c757d");
        colorTokens.put("success", "#28a745");
        colorTokens.put("danger", "#dc3545");
        colorTokens.put("warning", "#ffc107");
        colorTokens.put("info", "#17a2b8");
        colorTokens.put("light", "#f8f9fa");
        colorTokens.put("dark", "#343a40");
    }
    
    /**
     * 获取颜色令牌
     */
    public String getColor(String tokenName) {
        return colorTokens.getOrDefault(tokenName, "#000000");
    }
    
    /**
     * 添加图标路径
     */
    public void addIcon(String iconName, String filePath) {
        iconPaths.put(iconName, filePath);
    }
    
    /**
     * 获取图标路径
     */
    public String getIconPath(String iconName) {
        return iconPaths.get(iconName);
    }
    
    /**
     * 导入Figma导出的图标
     */
    public boolean importIcon(String iconName, File iconFile) {
        try {
            Path targetPath = Paths.get(ICONS_PATH, iconName + ".svg");
            Files.copy(iconFile.toPath(), targetPath);
            addIcon(iconName, targetPath.toString());
            return true;
        } catch (IOException e) {
            System.err.println("Failed to import icon: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 生成CSS变量文件
     */
    public void generateCSSVariables() {
        StringBuilder css = new StringBuilder();
        css.append(":root {\n");
        
        // 颜色变量
        for (Map.Entry<String, String> entry : colorTokens.entrySet()) {
            css.append("  --color-").append(entry.getKey()).append(": ").append(entry.getValue()).append(";\n");
        }
        
        css.append("}\n");
        
        try {
            Path cssFile = Paths.get(DESIGN_RESOURCES_PATH, "figma-tokens.css");
            Files.write(cssFile, css.toString().getBytes());
            System.out.println("CSS variables generated at: " + cssFile.toString());
        } catch (IOException e) {
            System.err.println("Failed to generate CSS file: " + e.getMessage());
        }
    }
}