package ee.carlrobert.codegpt.toolwindow

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener
import ee.carlrobert.codegpt.toolwindow.chat.ChatToolWindowPanel
import ee.carlrobert.codegpt.toolwindow.history.ChatHistoryToolWindow
import javax.swing.JComponent

class ProxyAIToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow
    ) {
        var chatToolWindowPanel = ChatToolWindowPanel(project, toolWindow.disposable)
        var chatHistoryToolWindow = ChatHistoryToolWindow(project)

        addContent(toolWindow, chatToolWindowPanel, "Chat")
        addContent(toolWindow, chatHistoryToolWindow.getContent(), "Chat History")

        toolWindow.addContentManagerListener(object : ContentManagerListener {
            override fun selectionChanged(event: ContentManagerEvent) {
                if ("Chat History" == event.content.tabName && event.content.isSelected) {
                    chatHistoryToolWindow.refresh()
                }
            }
        })
    }

    private fun addContent(toolWindow: ToolWindow, panel: JComponent, displayName: String) {
        toolWindow.contentManager.let {
            it.addContent(it.factory.createContent(panel, displayName, false))
        }
    }
}