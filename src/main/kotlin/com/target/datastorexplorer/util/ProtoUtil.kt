package com.target.datastorexplorer.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

/**
 * Utility method for .pb file view.
 */
object ProtoUtil {
	/**
	 * Goes through the current project and gives you all .proto files.
	 */
	fun getAllProtoFiles(
		project: Project
	): List<VirtualFile> {
		return buildList {
			addAll(
				FilenameIndex.getAllFilesByExt(
					project,
					"proto",
					GlobalSearchScope.projectScope(project)
				).filter { !it.isDirectory }
			)
		}
	}
}
