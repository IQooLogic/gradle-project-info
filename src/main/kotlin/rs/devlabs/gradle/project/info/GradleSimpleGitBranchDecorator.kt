package rs.devlabs.gradle.project.info

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.SimpleTextAttributes
import git4idea.GitUtil
import git4idea.branch.GitBranchUtil
import git4idea.repo.GitRepository

class GradleSimpleGitBranchDecorator : ProjectViewNodeDecorator {

    private val settings = service<GradleProjectInfoSettings>()

    override fun decorate(
        node: ProjectViewNode<*>?,
        presentation: PresentationData?
    ) {
        if (!settings.showGitBranch || !settings.enabled) return
        if (node == null || presentation == null) return
        if (node.name.isNullOrEmpty() || node.name.isNullOrBlank()) return

        val project = node.project ?: return
        val virtualFile = node.virtualFile ?: return

        if (!virtualFile.isDirectory) return
        if (!isGitRepository(project, virtualFile)) return

        val branch = getBranchName(project, virtualFile)
        if (branch.isNullOrEmpty()) return

        presentation.addText(
            " $branch", SimpleTextAttributes(
            SimpleTextAttributes.STYLE_BOLD,
            if (settings.useColors) settings.getGitBranchColorRGB() else SimpleTextAttributes.GRAY_ATTRIBUTES.fgColor
        ))
    }

    private fun getBranchName(project: Project, virtualFile: VirtualFile): String? {
        // Get repository in background thread
        val repository = ReadAction.compute<GitRepository?, Throwable> {
            GitBranchUtil.guessWidgetRepository(project, virtualFile)
        } ?: return null

        // Get branch info
        return ReadAction.compute<String?, Throwable> {
            repository.currentBranch?.name ?: repository.currentRevision?.take(7)
        }
    }

    /**
     * Checks if the given directory is a Git repository or is under Git version control
     *
     * @param project Current project
     * @param directory Virtual file representing the directory to check
     * @return true if the directory is a Git repository or is under Git version control
     */
    private fun isGitRepository(project: Project, directory: VirtualFile): Boolean {
        return ReadAction.compute<Boolean, Throwable> {
            // Check if .git directory exists in the project root
            if (directory.findChild(".git")?.isDirectory == true) {
                return@compute true
            }

            // If .git doesn't exist directly, check if the directory is under Git control
            val repositories = GitUtil.getRepositories(project)
            repositories.any { repo ->
                directory.path == repo.root.path
            }
        }
    }
}
