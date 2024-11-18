package rs.devlabs.gradle.project.info

import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.NlsContexts
import com.intellij.ui.ColorPanel
import com.intellij.ui.components.CheckBox
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import java.awt.Color
import javax.swing.JComponent

class GradleProjectInfoSettingsConfigurable : Configurable {
    private var settingsPanel: DialogPanel? = null
    private val settings = service<GradleProjectInfoSettings>()
    // Store initial values as properties
    private var initialState = SettingsState()

    private data class SettingsState(
        var showProjectVersion: Boolean = true,
        var showGitBranch: Boolean = false,
        var useColors: Boolean = true,
        var projectVersionColor: String = "#CC7832",
        var gitBranchColor: String = "#6897BB"
    )

    private val showProjectVersionCheckBox = CheckBox("Show project version")
    private val showGitBranchCheckBox = CheckBox("Show git branch")
    private val showUseColorsCheckBox = CheckBox("Use colors")
    private val projectVersionColorPanel = ColorPanel()
    private val gitBranchColorPanel = ColorPanel()

    override fun getDisplayName(): @NlsContexts.ConfigurableName String? {
        return "Project Info Settings"
    }

    override fun createComponent(): JComponent? {
        // Capture initial state
        saveInitialState()

        // Initialize UI components with current values
        updateUIFromSettings()

        // Initialize color panels with current values
        projectVersionColorPanel.selectedColor = Color.decode(settings.projectVersionColor)
        gitBranchColorPanel.selectedColor = Color.decode(settings.gitBranchColor)

        settingsPanel = panel {
            group("Display Options") {
                row {
                    cell(showProjectVersionCheckBox).bindSelected(
                        { settings.showProjectVersion },
                        {
                            settings.showProjectVersion = it
                        }
                    )
                }
                row {
                    cell(showGitBranchCheckBox).bindSelected(
                        { settings.showGitBranch },
                        {
                            settings.showGitBranch = it
                        }
                    )
                }
                row {
                    cell(showUseColorsCheckBox).bindSelected(
                        { settings.useColors },
                        {
                            settings.useColors = it
                        }
                    )
                }
            }

            group("Color Settings") {
                row("Project Version: ") {
                    cell(projectVersionColorPanel)
                        .component
                }
                row("Git Branch: ") {
                    cell(gitBranchColorPanel)
                        .component
                }
            }

            row {
                button("Reset to Defaults") {
                    settings.resetToDefaults()
                    updateUIFromSettings()
                    settingsPanel?.reset()
                    refreshProjectView()
                }
            }
        }

        return settingsPanel!!
    }

    override fun isModified(): Boolean {
        return settings.showProjectVersion != showProjectVersionCheckBox.isSelected ||
                settings.showGitBranch != showGitBranchCheckBox.isSelected ||
                settings.useColors != showUseColorsCheckBox.isSelected ||
                settings.projectVersionColor != colorToHex(projectVersionColorPanel.selectedColor) ||
                settings.gitBranchColor != colorToHex(gitBranchColorPanel.selectedColor)
    }

    override fun apply() {
        settingsPanel?.apply()

        settings.showProjectVersion = showProjectVersionCheckBox.isSelected
        settings.showGitBranch = showGitBranchCheckBox.isSelected
        settings.useColors = showUseColorsCheckBox.isSelected

        projectVersionColorPanel.selectedColor?.let {
            settings.projectVersionColor = "#%02X%02X%02X".format(it.red, it.green, it.blue)
        }
        gitBranchColorPanel.selectedColor?.let {
            settings.gitBranchColor = "#%02X%02X%02X".format(it.red, it.green, it.blue)
        }

        saveInitialState()
        refreshProjectView()
    }

    override fun reset() {
        // Restore settings to initial state
        settings.showProjectVersion = initialState.showProjectVersion
        settings.showGitBranch = initialState.showGitBranch
        settings.useColors = initialState.useColors
        settings.projectVersionColor = initialState.projectVersionColor
        settings.gitBranchColor = initialState.gitBranchColor

        projectVersionColorPanel.selectedColor = Color.decode(settings.projectVersionColor)
        gitBranchColorPanel.selectedColor = Color.decode(settings.gitBranchColor)

        updateUIFromSettings()
        settingsPanel?.reset()
        refreshProjectView()
    }

    private fun updateUIFromSettings() {
        showProjectVersionCheckBox.isSelected = settings.showProjectVersion
        showGitBranchCheckBox.isSelected = settings.showGitBranch
        showUseColorsCheckBox.isSelected = settings.useColors
        projectVersionColorPanel.selectedColor = Color.decode(settings.projectVersionColor)
        gitBranchColorPanel.selectedColor = Color.decode(settings.gitBranchColor)
    }

    private fun refreshProjectView() {
        ProjectManager.getInstance().openProjects.forEach { project ->
            ProjectView.getInstance(project).refresh()
        }
    }

    private fun saveInitialState() {
        initialState = SettingsState(
            showProjectVersion = settings.showProjectVersion,
            showGitBranch = settings.showGitBranch,
            useColors = settings.useColors,
            projectVersionColor = settings.projectVersionColor,
            gitBranchColor = settings.gitBranchColor
        )
    }

    private fun colorToHex(color: Color?): String {
        return color?.let { "#%02X%02X%02X".format(it.red, it.green, it.blue) } ?: "#000000"
    }
}
