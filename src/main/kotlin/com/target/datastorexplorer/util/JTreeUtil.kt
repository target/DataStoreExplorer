package com.target.datastorexplorer.util

import com.android.annotations.concurrency.WorkerThread
import kotlinx.serialization.json.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeModel

/**
 * JavaX Swing tree util.
 */
object JTreeUtil {

	@WorkerThread
	fun jsonToTreeModel(
		jsonString: String
	): TreeModel {
		val jsonElement = Json.Default.parseToJsonElement(jsonString)
		val rootNode = DefaultMutableTreeNode("Object ${jsonElement.nodeValue()}")
			.also { jsonElementToTreeNode(rootNode = it, jsonElement) }
		return DefaultTreeModel(rootNode)
	}

	private fun jsonElementToTreeNode(
		rootNode: DefaultMutableTreeNode,
		jsonElement: JsonElement
	) {
		when (jsonElement) {
			is JsonObject -> {
				jsonElement.jsonObject.forEach { key, element ->
					val childNode = DefaultMutableTreeNode("$key: ${element.nodeValue()}")
					rootNode.add(childNode)
					jsonElementToTreeNode(childNode, element)
				}
			}
			is JsonArray -> {
				jsonElement.jsonArray.forEachIndexed { index, element ->
					val childNode = DefaultMutableTreeNode("[$index]: ${element.nodeValue()}")
					rootNode.add(childNode)
					jsonElementToTreeNode(childNode, element)
				}
			}
			else -> {}
		}
	}

	private fun JsonElement.nodeValue(): String {
		return when (this) {
			is JsonPrimitive -> this.content
			is JsonObject -> "{${this.size}}"
			is JsonArray -> "[${this.size}]"
		}
	}
}

