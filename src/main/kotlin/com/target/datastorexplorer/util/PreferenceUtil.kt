package com.target.datastorexplorer.util

import androidx.datastore.preferences.PreferencesMapCompat
import com.android.annotations.concurrency.WorkerThread
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeModel

object PreferenceUtil {

	/**
	 * Takes a preference file and returns TreeModel
	 * @param preferenceFile the .preferences_pb file
	 */
	@WorkerThread
	fun preferenceFileToTreeModel(
		preferenceFile: VirtualFile
	): TreeModel {
		val prefMap = PreferencesMapCompat.readFrom(preferenceFile.inputStream).preferencesMap
		val rootNode = DefaultMutableTreeNode("Object {${prefMap.size}}")

		prefMap.forEach { (prefKey, prefVal) ->
			val value = when {
				prefVal.hasBoolean() -> prefVal.boolean
				prefVal.hasFloat() -> prefVal.float
				prefVal.hasInteger() -> prefVal.integer
				prefVal.hasLong() -> prefVal.long
				prefVal.hasString() -> prefVal.string
				prefVal.hasStringSet() -> prefVal.stringSet.stringsList.toString()
				prefVal.hasDouble() -> prefVal.double
				else -> throw IllegalStateException("Pref key not handled")
			}
			rootNode.add(
				DefaultMutableTreeNode("$prefKey: $value")
			)
		}

		return DefaultTreeModel(rootNode)
	}

}
