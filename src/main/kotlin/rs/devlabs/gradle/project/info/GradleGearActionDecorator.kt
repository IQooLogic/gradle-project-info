package rs.devlabs.gradle.project.info

import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowEx
import com.intellij.openapi.wm.ex.ToolWindowManagerListener

class GradleGearActionDecorator(private val project: Project) : ToolWindowManagerListener {

    override fun stateChanged(toolWindowManager: ToolWindowManager) {
        val projectView = toolWindowManager.getToolWindow(ToolWindowId.PROJECT_VIEW) as? ToolWindowEx
        projectView?.let { decorateToolWindow(it) }
    }

    private fun decorateToolWindow(toolWindow: ToolWindowEx) {
        // Ensure content manager is available
        if (toolWindow.contentManager?.contentCount == null) return

        // Create our action group
        val actionGroup = DefaultActionGroup().apply {
            // Add our custom action
            add(createToggleShowSimpleProjectInfoSettingsAction())
        }

        // Add our actions to the gear menu
        toolWindow.setAdditionalGearActions(actionGroup)
    }

    private fun createToggleShowSimpleProjectInfoSettingsAction(): ToggleShowGradleProjectInfoSettingsAction {
        return ToggleShowGradleProjectInfoSettingsAction().apply {
            setOnUpdateListener(this@GradleGearActionDecorator::refreshProjectView)
        }
    }

    private fun refreshProjectView() {
        ProjectView.getInstance(project)?.refresh()
    }
}
