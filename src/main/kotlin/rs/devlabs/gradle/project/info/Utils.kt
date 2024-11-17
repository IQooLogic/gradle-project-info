package rs.devlabs.gradle.project.info

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile

class Utils {
    companion object {
        fun isProjectRoot(project: Project, virtualFile: VirtualFile): Boolean {
            val projectRoot = ProjectRootManager.getInstance(project).contentRoots.firstOrNull()
            return projectRoot == virtualFile
        }
    }
}
