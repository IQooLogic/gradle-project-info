package rs.devlabs.gradle.project.info

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.components.service
import kotlinx.coroutines.Runnable

class ToggleShowSimpleProjectInfoSettingsAction : ToggleAction("Toggle Gradle Project Info") {
    private val settings = service<SimpleProjectInfoSettings>()
    private var onUpdateListener: Runnable = Runnable {}

    override fun isSelected(p0: AnActionEvent): Boolean {
        return settings.enabled
    }

    override fun setSelected(p0: AnActionEvent, enabled: Boolean) {
        settings.enabled = enabled
        this.onUpdateListener.run()
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    fun setOnUpdateListener(listener: Runnable) {
        onUpdateListener = listener
    }
}
