package rs.devlabs.gradle.project.info

import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowEx
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.ui.switcher.QuickActionProvider

class GearActionDecorator(private val project: Project) : ToolWindowManagerListener {

    override fun stateChanged(toolWindowManager: ToolWindowManager) {
        var projectView = toolWindowManager.getToolWindow(ToolWindowId.PROJECT_VIEW)
        if (projectView is ToolWindowEx) {
            var projectViewEx: ToolWindowEx? = projectView as? ToolWindowEx?
            this.decorateToolWindow(projectViewEx)
        }
    }

    private fun decorateToolWindow(toolWindow: ToolWindowEx?) {
        var actions = getProjectView()?.let { getDefaultProjectViewActions(it) } ?: ArrayList()
        if (actions.size > 2) {
            actions = ArrayList(actions.subList(2, actions.size))
        }
        actions.add(createToggleShowSimpleProjectInfoSettingsAction())
        toolWindow?.setAdditionalGearActions(DefaultActionGroup(actions))
    }

    private fun getProjectView(): ProjectView? {
        return ProjectView.getInstance(this.project)?.takeIf { project -> project is QuickActionProvider }
    }

    private fun getDefaultProjectViewActions(projectView: ProjectView?): ArrayList<AnAction> {
        var defaultActions = ArrayList<AnAction>()
        if (projectView is QuickActionProvider) {
            var actionProvider = projectView as? QuickActionProvider
            var providerActions = actionProvider?.getActions(false)
            defaultActions.addAll(providerActions!!)
        }

        return defaultActions
    }

    private fun createToggleShowSimpleProjectInfoSettingsAction(): ToggleShowSimpleProjectInfoSettingsAction {
        var action = ToggleShowSimpleProjectInfoSettingsAction()
        action.setOnUpdateListener(this::refreshProjectView)
        return action
    }

    private fun refreshProjectView() {
        getProjectView()?.refresh()
    }
}
