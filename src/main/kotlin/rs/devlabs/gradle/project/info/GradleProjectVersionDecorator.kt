package rs.devlabs.gradle.project.info

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.SimpleTextAttributes
import org.jetbrains.plugins.gradle.util.GradleConstants
import java.io.File

class GradleProjectVersionDecorator : ProjectViewNodeDecorator {

    private val LOG = Logger.getInstance(GradleProjectVersionDecorator::class.java)
    private val settings = service<GradleProjectInfoSettings>()

    override fun decorate(node: ProjectViewNode<*>, data: PresentationData) {
        if (!settings.showProjectVersion || !settings.enabled) return

        val project = node.project ?: return

        val moduleDir = resolveGradleModuleDir(project, node) ?: return

        // Only decorate directory-level nodes (skip individual files inside the module)
        val vf = node.virtualFile
        if (vf != null && !vf.isDirectory) return

        val version = getGradleProjectVersion(moduleDir) ?: return

        LOG.info("decorating ${node.name} with version $version")

        val versionColor = if (settings.useColors) settings.getProjectVersionColorRGB()
            else SimpleTextAttributes.GRAY_ATTRIBUTES.fgColor
        data.addText(
            " v$version",
            SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, versionColor)
        )
    }

    private fun resolveGradleModuleDir(project: com.intellij.openapi.project.Project, node: ProjectViewNode<*>): VirtualFile? {
        val vf = node.virtualFile

        // Case 1: virtualFile is a directory containing Gradle build files
        if (vf != null && vf.isDirectory && isGradleProject(vf)) {
            return vf
        }

        // Case 2: virtualFile is a file (e.g. build.gradle) — use its parent
        if (vf != null && !vf.isDirectory) {
            val parent = vf.parent
            if (parent != null && parent.isDirectory && isGradleProject(parent)) {
                return parent
            }
        }

        // Case 3: virtualFile is null or not a Gradle dir — fall back to module lookup (Android Studio Android view)
        val module = findModule(project, node, vf)
        if (module != null) {
            val root = moduleGradleRoot(module)
            if (root != null) return root
        }

        return null
    }

    private fun findModule(
        project: com.intellij.openapi.project.Project,
        node: ProjectViewNode<*>,
        virtualFile: VirtualFile?
    ): Module? {
        return try {
            ApplicationManager.getApplication().runReadAction<Module?> {
                val modules = ModuleManager.getInstance(project).modules

                // If we have a virtual file, find the module that owns it
                if (virtualFile != null) {
                    modules.firstOrNull { m ->
                        ModuleRootManager.getInstance(m).contentRoots.any { root ->
                            virtualFile.path.startsWith(root.path + "/") || virtualFile.path == root.path
                        }
                    } ?: return@runReadAction null
                }

                // Otherwise match by node name against module names
                modules.firstOrNull { it.name == node.name }
            }
        } catch (e: Exception) {
            LOG.warn("findModule failed for node=${node.name}", e)
            null
        }
    }

    private fun moduleGradleRoot(module: Module): VirtualFile? {
        return try {
            ApplicationManager.getApplication().runReadAction<VirtualFile?> {
                ModuleRootManager.getInstance(module).contentRoots
                    .firstOrNull { it.isDirectory && isGradleProject(it) }
            }
        } catch (e: Exception) {
            LOG.warn("moduleGradleRoot failed for module=${module.name}", e)
            null
        }
    }

    private fun getGradleProjectVersion(virtualFile: VirtualFile): String? {
        readVersionFromFiles(virtualFile, listOf(
            GradleConstants.KOTLIN_DSL_SCRIPT_NAME,
            GradleConstants.DEFAULT_SCRIPT_NAME
        ))?.let { return it }

        readVersionFromFiles(virtualFile, listOf(
            GradleConstants.KOTLIN_DSL_SETTINGS_FILE_NAME,
            GradleConstants.SETTINGS_FILE_NAME
        ))?.let { return it }

        val rootDir = findRootProjectDir(virtualFile)
        if (rootDir != null && rootDir != virtualFile) {
            readVersionFromFiles(rootDir, listOf(
                GradleConstants.KOTLIN_DSL_SCRIPT_NAME,
                GradleConstants.DEFAULT_SCRIPT_NAME
            ))?.let { return it }
            readVersionFromFiles(rootDir, listOf(
                GradleConstants.KOTLIN_DSL_SETTINGS_FILE_NAME,
                GradleConstants.SETTINGS_FILE_NAME
            ))?.let { return it }
        }

        readVersionFromGradleProperties(virtualFile)?.let { return it }

        if (rootDir != null && rootDir != virtualFile) {
            readVersionFromGradleProperties(rootDir)?.let { return it }
        }

        return null
    }

    private fun readVersionFromFiles(dir: VirtualFile, fileNames: List<String>): String? {
        for (name in fileNames) {
            val f = File(dir.path, name)
            if (!f.exists()) continue
            readVersionFromFile(f)?.let { return it }
        }
        return null
    }

    private fun readVersionFromFile(file: File): String? {
        try {
            val content = file.readText()
            val patterns = listOf(
                """version\s*=\s*['"](.+?)['"]""".toRegex(),
                """version\s*['"](.+?)['"]""".toRegex(),
                """project\.version\s*=\s*['"](.+?)['"]""".toRegex(),
                """rootProject\.version\s*=\s*['"](.+?)['"]""".toRegex()
            )
            for (pattern in patterns) {
                pattern.find(content)?.groupValues?.get(1)?.let { return it }
            }
        } catch (e: Exception) {
            LOG.warn("failed to read ${file.name}", e)
        }
        return null
    }

    private fun readVersionFromGradleProperties(dir: VirtualFile): String? {
        val f = File(dir.path, "gradle.properties")
        if (!f.exists()) return null
        try {
            val content = f.readText()
            val pattern = """^version\s*=\s*(.+)$""".toRegex(RegexOption.MULTILINE)
            return pattern.find(content)?.groupValues?.get(1)?.trim()
        } catch (e: Exception) {
            LOG.warn("failed to read gradle.properties", e)
        }
        return null
    }

    private fun findRootProjectDir(virtualFile: VirtualFile): VirtualFile? {
        var current: VirtualFile? = virtualFile
        while (current != null) {
            if (current.findChild(GradleConstants.KOTLIN_DSL_SETTINGS_FILE_NAME) != null ||
                current.findChild(GradleConstants.SETTINGS_FILE_NAME) != null) {
                return current
            }
            current = current.parent
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
