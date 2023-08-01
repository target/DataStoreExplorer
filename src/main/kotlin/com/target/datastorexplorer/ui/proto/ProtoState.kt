package com.target.datastorexplorer.ui.proto

import com.intellij.openapi.vfs.VirtualFile
import com.target.datastorexplorer.ui.JsonTreeLoadingState
import javax.swing.tree.TreeModel

data class ProtoState(
	val protoFile: List<VirtualFile> = listOf(),
	val treeModel: TreeModel? = null,
	val loadingState: JsonTreeLoadingState? = null
)
