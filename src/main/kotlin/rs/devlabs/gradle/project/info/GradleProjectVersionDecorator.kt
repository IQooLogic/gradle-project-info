package rs.devlabs.gradle.project.info

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.openapi.components.service
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.SimpleTextAttributes
import org.jetbrains.plugins.gradle.util.GradleConstants
import java.io.File

class GradleProjectVersionDecorator  : ProjectViewNodeDecorator {

    private val settings = service<GradleProjectInfoSettings>()

    override fun decorate(node: ProjectViewNode<*>, presentation: PresentationData) {
        if (!settings.showProjectVersion || !settings.enabled) return
        val project = node.project ?: return
        val virtualFile = node.virtualFile ?: return
        if (!isGradleProject(virtualFile)) return
        if (!virtualFile.isDirectory) return

        if (node is PsiDirectoryNode) {
            val version = getGradleProjectVersion(virtualFile)
            version?.let {
                presentation.clearText()

                // virtualFile.name
                presentation.addText(
                    virtualFile.name, SimpleTextAttributes(
                    SimpleTextAttributes.STYLE_PLAIN,
                    SimpleTextAttributes.REGULAR_ATTRIBUTES.fgColor
                ))
                // project.name
                if (!virtualFile.parent.isDirectory) {// if not multi-module project
                    presentation.addText(
                        " [${project.name}]", SimpleTextAttributes(
                            SimpleTextAttributes.STYLE_BOLD,
                            SimpleTextAttributes.REGULAR_ATTRIBUTES.fgColor
                        )
                    )
                }
                // version
                presentation.addText(" v$it", SimpleTextAttributes(
                    SimpleTextAttributes.STYLE_PLAIN,
                    if (settings.useColors) settings.getProjectVersionColorRGB() else SimpleTextAttributes.GRAY_ATTRIBUTES.fgColor
                ))
            }
        }
    }

    private fun getGradleProjectVersion(virtualFile: VirtualFile): String? {
        // Try both Kotlin and Groovy build files
        val buildFiles = listOf(
            File(virtualFile.path, GradleConstants.KOTLIN_DSL_SCRIPT_NAME),
            File(virtualFile.path, GradleConstants.DEFAULT_SCRIPT_NAME)
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

    private fun isGradleProject(virtualFile: VirtualFile): Boolean {
        return virtualFile.findChild(GradleConstants.KOTLIN_DSL_SCRIPT_NAME) != null ||
                virtualFile.findChild(GradleConstants.DEFAULT_SCRIPT_NAME) != null ||
                virtualFile.findChild(GradleConstants.KOTLIN_DSL_SETTINGS_FILE_NAME) != null ||
                virtualFile.findChild(GradleConstants.SETTINGS_FILE_NAME) != null
    }
}
