package com.target.datastorexplorer.util

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class JTreeUtilTest {

	@Test
	fun testJsonToTreeModel_with_empty_json_string() {
		val jsonString = ""
		assertThatThrownBy {
			JTreeUtil.jsonToTreeModel(jsonString)
		}.hasMessageContaining("Cannot begin reading element, unexpected token")
	}

	@Test
	fun testJsonToTreeModel_with_invalid_json_string(){
		val jsonString = """
			{
				"key1": 20,
				"key2": {
					"1":true								
			}
		""".trimIndent()
		assertThatThrownBy {
			JTreeUtil.jsonToTreeModel(jsonString)
		}.hasMessageContaining("Unexpected JSON token")
	}

	@Test
	fun testJsonToTreeModel_with_valid_json_string() {
		val jsonString = """
			{
			  "key1": 18,
			  "key2": 310,
			  "key3": true,
			  "key4": {
			    "1": true,
			    "2": true,
			    "3": true
			  }
			}
		""".trimIndent()
		val actualTreeModel = JTreeUtil.jsonToTreeModel(jsonString)

		val rootNode = DefaultMutableTreeNode("Object {4}").apply {
			add(DefaultMutableTreeNode("key1: 18"))
			add(DefaultMutableTreeNode("key2: 310"))
			add(DefaultMutableTreeNode("key3: true"))
			add(DefaultMutableTreeNode("key4: {3}").apply {
				add(DefaultMutableTreeNode("1: true"))
				add(DefaultMutableTreeNode("2: true"))
				add(DefaultMutableTreeNode("3: true"))
			})
		}

		assertThat(actualTreeModel).usingRecursiveComparison()
			.isEqualTo(DefaultTreeModel(rootNode))
	}

	@Test
	fun testJsonToTreeModel_with_valid_json_string_with_array() {
		val jsonString = """
			{
			  "key1": 18,
			  "key2": 310,
			  "key3": true,
			  "key4": {
			    "1": true,
			    "2": true,
			    "3": true
			  },
			  "key5": [
			    {
					"done": true										
				},
				{
					"done": false
				}
			  ]
			}
		""".trimIndent()
		val actualTreeModel = JTreeUtil.jsonToTreeModel(jsonString)
		val rootNode = DefaultMutableTreeNode("Object {5}").apply {
			add(DefaultMutableTreeNode("key1: 18"))
			add(DefaultMutableTreeNode("key2: 310"))
			add(DefaultMutableTreeNode("key3: true"))
			add(DefaultMutableTreeNode("key4: {3}").apply {
				add(DefaultMutableTreeNode("1: true"))
				add(DefaultMutableTreeNode("2: true"))
				add(DefaultMutableTreeNode("3: true"))
			})
			add(DefaultMutableTreeNode("key5: [2]").apply {
				add(DefaultMutableTreeNode("[0]: {1}").apply {
					add(DefaultMutableTreeNode("done: true"))
				})
				add(DefaultMutableTreeNode("[1]: {1}").apply {
					add(DefaultMutableTreeNode("done: false"))
				})
			})
		}

		assertThat(actualTreeModel).usingRecursiveComparison()
			.isEqualTo(DefaultTreeModel(rootNode))
	}
}

