package ee.carlrobert.codegpt.diagnostics;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import ee.carlrobert.codegpt.codecompletions.edit.GrpcClientService;
import ee.carlrobert.codegpt.credentials.CredentialsStore;
import ee.carlrobert.codegpt.settings.service.FeatureType;
import ee.carlrobert.codegpt.settings.service.ModelSelectionService;
import ee.carlrobert.codegpt.settings.service.ServiceType;
import ee.carlrobert.codegpt.settings.service.codegpt.CodeGPTServiceSettings;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

/**
 * 诊断gRPC连接问题的工具
 */
public class GrpcConnectionDiagnostic extends AnAction {
    
    private static final Logger LOG = Logger.getInstance(GrpcConnectionDiagnostic.class);
    private static final String GRPC_HOST = "grpc.tryproxy.io";
    private static final int GRPC_PORT = 9090;
    
    public GrpcConnectionDiagnostic() {
        super("诊断gRPC连接问题");
    }
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            StringBuilder report = new StringBuilder();
            report.append("=== gRPC连接诊断报告 ===\n\n");
            
            // 1. 检查服务配置
            checkServiceConfiguration(report);
            
            // 2. 检查API密钥
            checkApiKey(report);
            
            // 3. 检查网络连接
            checkNetworkConnectivity(report);
            
            // 4. 检查gRPC服务状态
            checkGrpcService(project, report);
            
            // 5. 提供修复建议
            provideSuggestions(report);
            
            ApplicationManager.getApplication().invokeLater(() -> {
                Messages.showInfoMessage(project, report.toString(), "gRPC连接诊断");
            });
        });
    }
    
    private void checkServiceConfiguration(StringBuilder report) {
        report.append("1. 服务配置检查:\n");
        
        try {
            ModelSelectionService modelService = ModelSelectionService.getInstance();
            ServiceType serviceType = modelService.getServiceForFeature(FeatureType.CODE_COMPLETION);
            String model = modelService.getModelForFeature(FeatureType.CODE_COMPLETION, null);
            
            report.append("   - 代码补全服务类型: ").append(serviceType).append("\n");
            report.append("   - 代码补全模型: ").append(model != null ? model : "未设置").append("\n");
            
            if (serviceType == ServiceType.PROXYAI) {
                CodeGPTServiceSettings settings = ServiceManager.getService(CodeGPTServiceSettings.class);
                boolean codeCompletionEnabled = settings.getState().getCodeCompletionSettings().getCodeCompletionsEnabled();
                report.append("   - 代码补全功能启用: ").append(codeCompletionEnabled).append("\n");
                
                if (!codeCompletionEnabled) {
                    report.append("   ❌ 代码补全功能未启用\n");
                } else {
                    report.append("   ✅ 服务配置正常\n");
                }
            } else {
                report.append("   ❌ 当前未使用ProxyAI服务\n");
            }
        } catch (Exception ex) {
            report.append("   ❌ 服务配置检查失败: ").append(ex.getMessage()).append("\n");
            LOG.warn("Service configuration check failed", ex);
        }
        
        report.append("\n");
    }
    
    private void checkApiKey(StringBuilder report) {
        report.append("2. API密钥检查:\n");
        
        try {
            String apiKey = CredentialsStore.getCredential(CredentialsStore.CredentialKey.CodeGptApiKey.INSTANCE);
            
            if (apiKey == null || apiKey.trim().isEmpty()) {
                report.append("   ❌ CodeGPT API密钥未设置\n");
                report.append("   请在设置中配置有效的API密钥\n");
            } else {
                // 不显示完整密钥，只显示前几位和长度
                String maskedKey = apiKey.length() > 8 ? 
                    apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4) :
                    "****";
                report.append("   ✅ API密钥已设置: ").append(maskedKey)
                      .append(" (长度: ").append(apiKey.length()).append(")\n");
            }
        } catch (Exception ex) {
            report.append("   ❌ API密钥检查失败: ").append(ex.getMessage()).append("\n");
            LOG.warn("API key check failed", ex);
        }
        
        report.append("\n");
    }
    
    private void checkNetworkConnectivity(StringBuilder report) {
        report.append("3. 网络连接检查:\n");
        
        try {
            report.append("   正在测试连接到 ").append(GRPC_HOST).append(":").append(GRPC_PORT).append("...\n");
            
            CompletableFuture<Boolean> connectTest = CompletableFuture.supplyAsync(() -> {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(GRPC_HOST, GRPC_PORT), 5000);
                    return true;
                } catch (IOException e) {
                    LOG.warn("Network connectivity test failed", e);
                    return false;
                }
            });
            
            Boolean connected = connectTest.get(java.util.concurrent.TimeUnit.SECONDS.toMillis(10), 
                                              java.util.concurrent.TimeUnit.MILLISECONDS);
            
            if (connected) {
                report.append("   ✅ 网络连接正常\n");
            } else {
                report.append("   ❌ 无法连接到gRPC服务器\n");
                report.append("   可能的原因: 网络问题、防火墙阻止、服务器不可用\n");
            }
        } catch (Exception ex) {
            report.append("   ❌ 网络连接测试失败: ").append(ex.getMessage()).append("\n");
            LOG.warn("Network connectivity check failed", ex);
        }
        
        report.append("\n");
    }
    
    private void checkGrpcService(Project project, StringBuilder report) {
        report.append("4. gRPC服务检查:\n");
        
        try {
            GrpcClientService grpcService = project.getService(GrpcClientService.class);
            if (grpcService != null) {
                report.append("   ✅ GrpcClientService实例已创建\n");
                
                // 尝试刷新连接
                report.append("   正在尝试刷新gRPC连接...\n");
                grpcService.refreshConnection();
                report.append("   ✅ 连接刷新完成\n");
            } else {
                report.append("   ❌ 无法获取GrpcClientService实例\n");
            }
        } catch (Exception ex) {
            report.append("   ❌ gRPC服务检查失败: ").append(ex.getMessage()).append("\n");
            LOG.warn("gRPC service check failed", ex);
        }
        
        report.append("\n");
    }
    
    private void provideSuggestions(StringBuilder report) {
        report.append("5. 修复建议:\n");
        report.append("   1. 确保已设置有效的CodeGPT API密钥\n");
        report.append("   2. 检查网络连接和防火墙设置\n");
        report.append("   3. 确认ProxyAI服务已选择并启用代码补全功能\n");
        report.append("   4. 尝试重启IDE以重新初始化连接\n");
        report.append("   5. 如果问题持续，请检查ProxyAI服务状态\n");
        report.append("\n");
        report.append("如果所有检查都通过但仍有问题，可能是服务端临时不可用。\n");
    }
}