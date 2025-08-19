# CodeBuddy中使用Figma的完整指南

## 概述

本指南介绍如何在CodeBuddy项目中集成和使用Figma设计资源。

## 设置步骤

### 1. 获取Figma访问令牌

1. 登录Figma账户
2. 进入 Settings > Account > Personal access tokens
3. 生成新的访问令牌
4. 保存令牌（仅显示一次）

### 2. 配置项目

```java
// 初始化Figma资源管理器
FigmaResourceManager resourceManager = new FigmaResourceManager();

// 配置API客户端
FigmaApiClient apiClient = new FigmaApiClient("your-figma-token");
```

### 3. 导入设计资源

#### 方法一：手动导入
1. 从Figma导出图标和图片到本地
2. 使用资源管理器导入：

```java
File iconFile = new File("path/to/icon.svg");
resourceManager.importIcon("menu-icon", iconFile);
```

#### 方法二：API自动同步
```java
// 获取Figma文件数据
JsonNode fileData = apiClient.getFile("your-figma-file-key");

// 提取设计令牌
Map<String, Object> tokens = apiClient.extractDesignTokens(fileData);

// 生成CSS变量
resourceManager.generateCSSVariables();
```

## 工作流程

### 1. 设计阶段
- 在Figma中完成UI设计
- 定义设计令牌（颜色、字体、间距等）
- 准备图标和图片资源

### 2. 导出阶段
- 从Figma导出SVG图标
- 导出设计规范文档
- 获取颜色值和尺寸数据

### 3. 集成阶段
- 使用FigmaResourceManager导入资源
- 生成CSS变量文件
- 在代码中引用设计令牌

### 4. 同步阶段
- 定期同步Figma设计变更
- 更新本地设计令牌
- 重新生成样式文件

## 使用示例

### 在Java代码中使用设计令牌

```java
public class UIComponentFactory {
    private FigmaResourceManager resourceManager;
    
    public UIComponentFactory() {
        this.resourceManager = new FigmaResourceManager();
    }
    
    public JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        String primaryColor = resourceManager.getColor("primary");
        button.setBackground(Color.decode(primaryColor));
        return button;
    }
    
    public ImageIcon getIcon(String iconName) {
        String iconPath = resourceManager.getIconPath(iconName);
        return new ImageIcon(iconPath);
    }
}
```

### 在CSS中使用设计令牌

```css
/* 引入生成的Figma令牌 */
@import url('design/figma-tokens.css');

.primary-button {
    background-color: var(--color-primary);
    color: white;
    border: none;
    border-radius: 4px;
    padding: 8px 16px;
}

.secondary-button {
    background-color: var(--color-secondary);
    color: var(--color-dark);
}
```

## 最佳实践

### 1. 命名规范
- 使用语义化的令牌名称（如 `primary`, `secondary`）
- 避免使用具体的颜色名称（如 `blue`, `red`）
- 保持命名的一致性

### 2. 文件组织
```
src/main/resources/design/
├── icons/           # SVG图标文件
├── images/          # 图片资源
├── colors/          # 颜色相关文件
├── figma-tokens.css # 生成的CSS变量
└── design-system.json # 设计系统配置
```

### 3. 版本控制
- 将设计资源纳入版本控制
- 记录设计变更历史
- 建立设计-开发同步流程

### 4. 自动化
- 设置CI/CD流程自动同步Figma资源
- 自动生成设计令牌文件
- 自动检测设计变更

## 故障排除

### 常见问题

1. **API访问失败**
   - 检查访问令牌是否有效
   - 确认文件权限设置
   - 验证网络连接

2. **资源导入失败**
   - 检查文件路径是否正确
   - 确认文件格式支持
   - 验证磁盘空间

3. **样式不生效**
   - 检查CSS变量引用
   - 确认文件路径正确
   - 验证浏览器兼容性

### 调试技巧

```java
// 启用调试模式
System.setProperty("figma.debug", "true");

// 检查资源状态
resourceManager.validateResources();

// 输出设计令牌
System.out.println(resourceManager.getColor("primary"));
```

## 扩展功能

### 1. 自定义导出器
可以扩展FigmaApiClient来支持更多的导出格式和功能。

### 2. 实时同步
实现WebSocket连接来实时同步Figma设计变更。

### 3. 设计审查
集成设计审查工具来确保设计实现的准确性。

## 总结

通过这套Figma集成方案，您可以：
- 统一管理设计资源
- 自动同步设计变更
- 提高设计-开发协作效率
- 确保UI一致性

如需更多帮助，请参考项目文档或联系开发团队。