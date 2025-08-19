# 🌐 ProxyAI Web服务器集成指南

## 📋 概述

ProxyAI插件现在集成了Web服务器功能，可以在IntelliJ IDEA启动时自动启动，提供效能度量数据的可视化界面和REST API接口。

## 🚀 自动启动

### 1. 插件启动时自动启动
Web服务器会在以下情况下自动启动：
- IntelliJ IDEA启动时
- 打开项目时
- 插件启用时

### 2. 启动流程
```
插件启动 → MetricsSystemInitializer → WebServerStartupActivity → 启动Web服务器
```

## ⚙️ 配置选项

### 1. 在设置中配置
路径：`File → Settings → Tools → ProxyAI → 提效度量`

- **启用Web服务器**: 控制是否启动Web服务器
- **Web服务器端口**: 设置服务器监听端口（默认：8090）

### 2. 默认设置
- Web服务器默认启用
- 默认端口：8090
- 自动启动：是

## 🌍 访问方式

### 1. 浏览器访问
启动成功后，在浏览器中访问：
- **主页**: `http://localhost:8090/`
- **指标数据**: `http://localhost:8090/api/metrics`
- **数据摘要**: `http://localhost:8090/api/metrics/summary`
- **动作类型**: `http://localhost:8090/api/metrics/actions`
- **模型使用**: `http://localhost:8090/api/metrics/models`

### 2. API接口
```bash
# 获取所有指标数据
GET http://localhost:8090/api/metrics

# 获取数据摘要
GET http://localhost:8090/api/metrics/summary

# 获取动作类型统计
GET http://localhost:8090/api/metrics/actions

# 获取模型使用统计
GET http://localhost:8090/api/metrics/models
```

## 📊 功能特性

### 1. 实时数据展示
- 显示当前收集的效能指标
- 实时更新统计数据
- 可视化图表展示

### 2. 数据导出
- 支持JSON格式导出
- 支持CSV格式导出
- 支持HTML报告导出

### 3. 统计分析
- 代码生成效率统计
- 时间节省统计
- 用户满意度统计
- 模型使用统计

## 🔧 故障排除

### 1. 服务器无法启动
**问题**: Web服务器启动失败
**解决方案**:
- 检查端口是否被占用
- 确认防火墙设置
- 查看IntelliJ IDEA日志

### 2. 无法访问Web界面
**问题**: 浏览器无法访问Web界面
**解决方案**:
- 确认服务器已启动
- 检查端口配置
- 尝试使用localhost而不是127.0.0.1

### 3. 数据不显示
**问题**: Web界面显示无数据
**解决方案**:
- 确认指标收集已启用
- 检查数据库连接
- 查看控制台日志

## 📝 日志信息

启动时会在控制台显示以下信息：
```
🎉 ProxyAI Web服务器启动成功!
🌐 访问地址: http://localhost:8090
📊 指标数据: http://localhost:8090/api/metrics
📈 数据摘要: http://localhost:8090/api/metrics/summary
🔧 动作类型: http://localhost:8090/api/metrics/actions
🤖 模型使用: http://localhost:8090/api/metrics/models
```

## 🔒 安全注意事项

### 1. 访问控制
- 默认只监听本地端口（localhost）
- 不提供身份验证
- 建议在生产环境中添加安全措施

### 2. 数据隐私
- 所有数据存储在本地
- 不向外部发送数据
- 支持数据匿名化

## 🎯 使用场景

### 1. 个人开发
- 监控个人开发效率
- 跟踪代码生成质量
- 分析时间节省情况

### 2. 团队协作
- 团队成员效率对比
- 项目整体进度监控
- 代码质量趋势分析

### 3. 演示展示
- 向客户展示AI辅助效果
- 会议中实时展示数据
- 培训新员工

## 🚀 快速开始

1. **启动IntelliJ IDEA**
2. **打开项目**（Web服务器会自动启动）
3. **查看控制台输出**（确认启动成功）
4. **打开浏览器**访问 `http://localhost:8090`
5. **浏览指标数据**和统计信息

## 📞 技术支持

如果遇到问题，请：
1. 查看IntelliJ IDEA的Event Log
2. 检查控制台输出
3. 确认插件设置正确
4. 重启IDE尝试

---

**注意**: Web服务器功能需要Javalin依赖，确保项目依赖配置正确。
