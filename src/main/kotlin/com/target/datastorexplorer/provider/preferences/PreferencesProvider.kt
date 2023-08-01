package com.target.datastorexplorer.provider.preferences

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.target.datastorexplorer.PREFERENCE_LANGUAGE
import com.target.datastorexplorer.coroutine.DefaultCoroutineDispatchers
import com.target.datastorexplorer.fileType.preferences.PreferencesFileType
import org.jdom.Element

class PreferencesProvider : FileEditorProvider, DumbAware {

	override fun accept(project: Project, file: VirtualFile): Boolean = file.fileType is PreferencesFileType

	override fun createEditor(project: Project, file: VirtualFile): FileEditor {
		return PreferencesEditor(
			project = project,
			preferenceFile = file,
			dispatchers = DefaultCoroutineDispatchers()
		)
	}

	override fun readState(
		sourceElement: Element,
		project: Project,
		file: VirtualFile
	): FileEditorState = FileEditorState.INSTANCE

	override fun getEditorTypeId(): String = PREFERENCE_LANGUAGE

	override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR

	override fun disposeEditor(editor: FileEditor) = editor.dispose()

}
