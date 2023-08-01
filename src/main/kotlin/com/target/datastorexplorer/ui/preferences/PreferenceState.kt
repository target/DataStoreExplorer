package com.target.datastorexplorer.ui.preferences

import com.target.datastorexplorer.ui.JsonTreeLoadingState
import javax.swing.tree.TreeModel

data class PreferenceState(
	val treeModel: TreeModel? = null,
	val loadingState: JsonTreeLoadingState? = null
)