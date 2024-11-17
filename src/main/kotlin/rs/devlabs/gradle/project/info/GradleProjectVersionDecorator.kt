package rs.devlabs.gradle.project.info

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.SimpleTextAttributes
import org.jetbrains.plugins.gradle.util.GradleConstants

class GradleProjectVersionDecorator  : ProjectViewNodeDecorator {

    private val settings = service<SimpleProjectInfoSettings>()

    override fun decorate(node: ProjectViewNode<*>, presentation: PresentationData) {
        if (!settings.showProjectVersion || !settings.enabled) return
        val project = node.project ?: return
        val virtualFile = node.virtualFile ?: return
        if (!isGradleProject(virtualFile)) return

        if (Utils.isProjectRoot(node.project, virtualFile)) {
            val version = getGradleProjectVersion(project)
            version?.let {
                presentation.clearText()

                // virtualFile.name
                presentation.addText(
                    virtualFile.name, SimpleTextAttributes(
                    SimpleTextAttributes.STYLE_PLAIN,
                    SimpleTextAttributes.REGULAR_ATTRIBUTES.fgColor
                ))
                // project.name
                presentation.addText(
                    " [${project.name}]", SimpleTextAttributes(
                        SimpleTextAttributes.STYLE_BOLD,
                        SimpleTextAttributes.REGULAR_ATTRIBUTES.fgColor
                    ))
                // version
                presentation.addText(" v$it", SimpleTextAttributes(
                    SimpleTextAttributes.STYLE_PLAIN,
                    if (settings.useColors) settings.getProjectVersionColorRGB() else SimpleTextAttributes.GRAY_ATTRIBUTES.fgColor
                ))
            }
        }
    }

    private fun isGradleProject(virtualFile: VirtualFile): Boolean {
        return virtualFile.findChild("build.gradle.kts") != null || virtualFile.findChild("build.gradle") != null
        return virtualFile.findChild(GradleConstants.KOTLIN_DSL_SCRIPT_NAME) != null || virtualFile.findChild(GradleConstants.DEFAULT_SCRIPT_NAME) != null
    }

    private fun getGradleProjectVersion(project: Project): String? {
        val projectDir = project.basePath?.let { java.io.File(it) } ?: return null

        // Try both Kotlin and Groovy build files
        val buildFiles = listOf(
            java.io.File(projectDir, GradleConstants.KOTLIN_DSL_SCRIPT_NAME),
            java.io.File(projectDir, GradleConstants.DEFAULT_SCRIPT_NAME)
        )

        for (buildFile in buildFiles) {
            if (buildFile.exists()) {
                try {
                    val content = buildFile.readText()
                    // Try different version declaration patterns
                    val patterns = listOf(
                        """version\s*=\s*['"](.+?)['"]""".toRegex(),
                        """version\s*['"](.+?)['"]""".toRegex(),
                        """project\.version\s*=\s*['"](.+?)['"]""".toRegex()
                    )

                    for (pattern in patterns) {
                        pattern.find(content)?.groupValues?.get(1)?.let {
                            return it
                        }
                    }
                } catch (_: Exception) {
                    // Log exception if needed
                }
            }
        }
        return null
    }
}
