package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * 度量监听器诊断工具
 * 用于检查编辑器事件监听器是否正确安装
 */
public class MetricsListenerDiagnostic {
    
    private static final Logger LOG = Logger.getInstance(MetricsListenerDiagnostic.class);
    
    public static void main(String[] args) {
        System.out.println("=== ProxyAI度量监听器诊断工具 ===");
        
        try {
            // 1. 检查EditorFactory
            System.out.println("1. 检查EditorFactory...");
            EditorFactory editorFactory = EditorFactory.getInstance();
            
            if (editorFactory != null) {
                System.out.println("✓ 成功获取EditorFactory实例");
                
                // 2. 检查当前编辑器
                System.out.println("2. 检查当前编辑器...");
                Editor[] editors = editorFactory.getAllEditors();
                System.out.println("  当前打开的编辑器数量: " + editors.length);
                
                // 3. 检查监听器
                System.out.println("3. 检查监听器...");
                checkEditorFactoryListeners();
                
                // 4. 尝试安装测试监听器
                System.out.println("4. 尝试安装测试监听器...");
                installTestListener(editorFactory);
                
            } else {
                System.out.println("✗ 无法获取EditorFactory实例");
            }
            
        } catch (Exception e) {
            System.err.println("✗ 诊断过程中出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 检查EditorFactory监听器
     */
    private static void checkEditorFactoryListeners() {
        try {
            // 尝试通过反射获取监听器列表
            java.lang.reflect.Field listenersField = EditorFactory.class.getDeclaredField("myEditorFactoryListeners");
            listenersField.setAccessible(true);
            
            Object listenersObj = listenersField.get(EditorFactory.getInstance());
            if (listenersObj instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<EditorFactoryListener> listeners = (java.util.List<EditorFactoryListener>) listenersObj;
                
                System.out.println("  已注册的EditorFactoryListener数量: " + listeners.size());
                
                // 检查是否有MetricsEditorFactoryListener
                boolean hasMetricsListener = false;
                for (EditorFactoryListener listener : listeners) {
                    String className = listener.getClass().getName();
                    System.out.println("  - " + className);
                    
                    if (className.contains("Metrics") || className.contains("metrics")) {
                        hasMetricsListener = true;
                        System.out.println("    ✓ 找到度量相关监听器");
                    }
                }
                
                if (!hasMetricsListener) {
                    System.out.println("  ✗ 未找到度量相关监听器，这可能是数据未被采集的原因");
                }
                
            } else {
                System.out.println("  ✗ 无法获取监听器列表");
            }
            
        } catch (Exception e) {
            System.out.println("  ✗ 检查监听器时出错: " + e.getMessage());
        }
    }
    
    /**
     * 安装测试监听器
     */
    private static void installTestListener(EditorFactory editorFactory) {
        try {
            EditorFactoryListener testListener = new EditorFactoryListener() {
                @Override
                public void editorCreated(EditorFactoryEvent event) {
                    Editor editor = event.getEditor();
                    Document document = editor.getDocument();
                    VirtualFile file = FileDocumentManager.getInstance().getFile(document);
                    
                    System.out.println("  测试监听器: 编辑器创建事件触发");
                    System.out.println("  - 文件: " + (file != null ? file.getName() : "未知"));
                }
                
                @Override
                public void editorReleased(EditorFactoryEvent event) {
                    System.out.println("  测试监听器: 编辑器释放事件触发");
                }
            };
            
            editorFactory.addEditorFactoryListener(testListener, null);
            System.out.println("  ✓ 测试监听器安装成功");
            System.out.println("  现在打开或关闭一个编辑器，应该会看到测试监听器的输出");
            
        } catch (Exception e) {
            System.out.println("  ✗ 安装测试监听器时出错: " + e.getMessage());
        }
    }
}