package ee.carlrobert.codegpt.metrics;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import org.jetbrains.annotations.NotNull;

/**
 * 指标系统编辑器工厂监听器
 * 为新创建的编辑器添加指标收集监听器
 */
public class MetricsEditorFactoryListener implements EditorFactoryListener {
    
    private static final Logger LOG = Logger.getInstance(MetricsEditorFactoryListener.class);
    private final CodeCompletionUsageListener documentListener = new CodeCompletionUsageListener();
    
    @Override
    public void editorCreated(@NotNull EditorFactoryEvent event) {
        try {
            Editor editor = event.getEditor();
            
            // 为编辑器的文档添加监听器
            editor.getDocument().addDocumentListener(documentListener);
            
            LOG.debug("为编辑器添加了指标收集监听器");
            
        } catch (Exception e) {
            LOG.warn("为编辑器添加指标监听器时发生错误", e);
        }
    }
    
    @Override
    public void editorReleased(@NotNull EditorFactoryEvent event) {
        try {
            Editor editor = event.getEditor();
            
            // 移除文档监听器
            editor.getDocument().removeDocumentListener(documentListener);
            
            LOG.debug("移除了编辑器的指标收集监听器");
            
        } catch (Exception e) {
            LOG.warn("移除编辑器指标监听器时发生错误", e);
        }
    }
}