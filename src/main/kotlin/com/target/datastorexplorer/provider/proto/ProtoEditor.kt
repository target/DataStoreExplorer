package com.target.datastorexplorer.provider.proto

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.FileEditorStateLevel
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.target.datastorexplorer.coroutine.CoroutineDispatchers
import com.target.datastorexplorer.ui.proto.ProtoScreen
import java.beans.PropertyChangeListener
import javax.swing.JComponent

class ProtoEditor(
	project: Project,
	private val protoBuffFile: VirtualFile,
	dispatchers: CoroutineDispatchers
) : UserDataHolderBase(), FileEditor {

	companion object {
		var parentDisposable = Disposer.newCheckedDisposable()
			get() = if (field.isDisposed) Disposer.newCheckedDisposable() else field
	}

	private val panel = ProtoScreen(
		project = project,
		protoBuffFile = protoBuffFile,
		dispatchers = dispatchers
	).protoBuffPanel()

	override fun getComponent(): JComponent = panel

	override fun getPreferredFocusedComponent(): JComponent = panel

	override fun getName(): String = protoBuffFile.name

	override fun setState(state: FileEditorState) {}

	override fun getState(level: FileEditorStateLevel): FileEditorState = FileEditorState.INSTANCE

	override fun isModified(): Boolean = false

	override fun isValid(): Boolean = protoBuffFile.exists()

	override fun addPropertyChangeListener(listener: PropertyChangeListener) {}

	override fun removePropertyChangeListener(listener: PropertyChangeListener) {}

	override fun getCurrentLocation(): FileEditorLocation? = null

	override fun getFile(): VirtualFile = protoBuffFile

	override fun dispose() {
		Disposer.dispose(parentDisposable)
		Disposer.dispose(this)
	}
}