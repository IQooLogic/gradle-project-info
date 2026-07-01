package rs.devlabs.gradle.project.info

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.SimpleTextAttributes
import git4idea.GitUtil
import git4idea.branch.GitBranchUtil
import git4idea.repo.GitRepository

class GradleSimpleGitBranchDecorator : ProjectViewNodeDecorator {

    private val settings = service<GradleProjectInfoSettings>()

    override fun decorate(node: ProjectViewNode<*>, data: PresentationData) {
        if (!settings.showGitBranch || !settings.enabled) return

        val project = node.project ?: return
        val virtualFile = node.virtualFile ?: return

        if (!virtualFile.isDirectory) return
        if (!isGitRepository(project, virtualFile)) return

        val branch = getBranchName(project, virtualFile)
        if (branch.isNullOrEmpty()) return

        data.addText(
            " $branch", SimpleTextAttributes(
            SimpleTextAttributes.STYLE_BOLD,
            if (settings.useColors) settings.getGitBranchColorRGB() else SimpleTextAttributes.GRAY_ATTRIBUTES.fgColor
        ))
    }

    private fun getBranchName(project: Project, virtualFile: VirtualFile): String? {
        val repository = ApplicationManager.getApplication()
            .runReadAction<GitRepository?> { GitBranchUtil.guessWidgetRepository(project, virtualFile) }
            ?: return null

        return ApplicationManager.getApplication()
            .runReadAction<String?> { repository.currentBranch?.name ?: repository.currentRevision?.take(7) }
    }

    private fun isGitRepository(project: Project, directory: VirtualFile): Boolean {
        return ApplicationManager.getApplication().runReadAction<Boolean> {
            if (directory.findChild(".git")?.isDirectory == true) {
                return@runReadAction true
            }

            val repositories = GitUtil.getRepositories(project)
            repositories.any { repo -> directory.path == repo.root.path }
        }
    }
}
