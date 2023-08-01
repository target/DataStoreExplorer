package com.target.datastorexplorer.provider.proto

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.target.datastorexplorer.PROTO_LANGUAGE
import com.target.datastorexplorer.coroutine.DefaultCoroutineDispatchers
import com.target.datastorexplorer.fileType.proto.ProtoFileType
import org.jdom.Element

class ProtoProvider : FileEditorProvider, DumbAware {

	override fun accept(project: Project, file: VirtualFile): Boolean = file.fileType is ProtoFileType

	override fun createEditor(project: Project, file: VirtualFile): FileEditor = ProtoEditor(
		project = project,
		protoBuffFile = file,
		dispatchers = DefaultCoroutineDispatchers()
	)

	override fun readState(
		sourceElement: Element,
		project: Project,
		file: VirtualFile
	): FileEditorState = FileEditorState.INSTANCE

	override fun getEditorTypeId(): String = PROTO_LANGUAGE

	override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR

	override fun disposeEditor(editor: FileEditor) = editor.dispose()

}
