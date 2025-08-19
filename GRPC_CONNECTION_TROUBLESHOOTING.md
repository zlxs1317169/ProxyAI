# ProxyAI gRPC连接问题排查指南

## 问题描述
当您看到错误信息 `io.grpc.StatusRuntimeException: UNAVAILABLE: unavailable` 时，表示ProxyAI的gRPC服务连接不可用。

## 快速诊断
1. 在IDE中打开 **Tools** 菜单
2. 找到 **ProxyAI 指标调试** 组
3. 点击 **诊断gRPC连接问题**
4. 查看诊断报告

## 常见解决方案

### 1. 检查API密钥配置
- 打开 **File** → **Settings** → **Tools** → **ProxyAI** → **Providers** → **ProxyAI**
- 确保API密钥已正确设置且有效
- 如果没有API密钥，请访问ProxyAI官网获取

### 2. 检查网络连接
- 确保您的网络可以访问 `grpc.tryproxy.io:9090`
- 检查防火墙设置，确保没有阻止gRPC连接
- 如果在企业网络环境，可能需要配置代理

### 3. 检查服务配置
- 打开 **File** → **Settings** → **Tools** → **ProxyAI** → **Models**
- 确保代码补全功能选择了ProxyAI服务
- 确保代码补全功能已启用

### 4. 重启和重试
- 尝试重启IDE
- 在诊断工具中点击"重试连接"
- 检查ProxyAI服务状态

## 降级处理
即使gRPC连接失败，以下功能仍然可用：
- ✅ 指标收集系统继续工作
- ✅ 其他AI服务提供者（OpenAI、Anthropic等）
- ✅ 聊天功能
- ❌ ProxyAI代码补全功能暂时不可用

## 技术细节
- **gRPC服务器**: grpc.tryproxy.io:9090
- **连接协议**: TLS/SSL
- **认证方式**: API密钥 (x-api-key header)
- **超时设置**: 5秒连接超时

## 获取帮助
如果问题持续存在：
1. 运行诊断工具并保存报告
2. 检查IDE日志文件中的详细错误信息
3. 联系ProxyAI技术支持，提供诊断报告

## 开发者信息
- 诊断工具位置: `ee.carlrobert.codegpt.diagnostics.GrpcConnectionDiagnostic`
- gRPC客户端服务: `ee.carlrobert.codegpt.codecompletions.edit.GrpcClientService`
- 连接状态通知: `ee.carlrobert.codegpt.notifications.GrpcConnectionNotifier`