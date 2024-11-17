package rs.devlabs.gradle.project.info

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile

class Utils {
    fun isProjectRoot(project: Project?, virtualFile: VirtualFile?): Boolean {// works
        if (project == null || virtualFile == null) return false
        val projectRoot = ProjectRootManager.getInstance(project).contentRoots.firstOrNull()
        return projectRoot == virtualFile
    }

    companion object {
        fun isProjectRoot(project: Project, file: VirtualFile): Boolean {
            return isProjectRoot(project, file)
        }
    }
}
