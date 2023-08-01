package com.target.datastorexplorer.util

import androidx.datastore.preferences.core.*
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import com.target.datastorexplorer.CoroutineTestRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.InputStream
import java.io.OutputStream
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

@OptIn(ExperimentalCoroutinesApi::class)
class PreferenceUtilTest {

	@get:Rule
	val tempFolder: TemporaryFolder = TemporaryFolder.builder()
		.assureDeletion()
		.build()

	private val prefDataStore = PreferenceDataStoreFactory.create(
		scope = CoroutineScope(Dispatchers.Main + SupervisorJob()),
		produceFile = {
			tempFolder.newFile("test.preferences_pb")
		}
	)

	@get:Rule
	val coroutineTestRule = CoroutineTestRule()

	private fun getVfFile(inputStream: InputStream):VirtualFile{
		return object : VirtualFile() {
			override fun getName(): String { TODO("Not yet implemented") }
			override fun getFileSystem(): VirtualFileSystem { TODO("Not yet implemented") }
			override fun getPath(): String { TODO("Not yet implemented") }
			override fun isWritable(): Boolean { TODO("Not yet implemented") }
			override fun isDirectory(): Boolean { TODO("Not yet implemented") }
			override fun isValid(): Boolean { TODO("Not yet implemented") }
			override fun getParent(): VirtualFile { TODO("Not yet implemented") }
			override fun getChildren(): Array<VirtualFile> { TODO("Not yet implemented") }
			override fun getOutputStream(p0: Any?, p1: Long, p2: Long): OutputStream { TODO("Not yet implemented") }
			override fun contentsToByteArray(): ByteArray { TODO("Not yet implemented") }
			override fun getTimeStamp(): Long { TODO("Not yet implemented") }
			override fun getLength(): Long { TODO("Not yet implemented") }
			override fun refresh(p0: Boolean, p1: Boolean, p2: Runnable?) { TODO("Not yet implemented") }
			override fun getInputStream(): InputStream = inputStream
		}
	}

	@Test
	fun test_prefFileToTreeModel_valid_keys() = runTest(coroutineTestRule.mainTestDispatcher) {
		prefDataStore.edit { settings ->
			settings[booleanPreferencesKey("key1")] = true
			settings[intPreferencesKey("key2")] = 2
			settings[stringPreferencesKey("key3")] = "abcd"
			settings[longPreferencesKey("key4")] = 2L
			settings[doublePreferencesKey("key5")] = 2.0
			settings[floatPreferencesKey("key6")] = 1.2f
			settings[stringSetPreferencesKey("key7")] = hashSetOf("1","2","3")
		}
		val vF = getVfFile(tempFolder.root.listFiles()!![0].inputStream())
		val actualTreeModel = PreferenceUtil.preferenceFileToTreeModel(vF)

		val rootNode = DefaultMutableTreeNode("Object {7}").apply {
			add(DefaultMutableTreeNode("key1: true"))
			add(DefaultMutableTreeNode("key2: 2"))
			add(DefaultMutableTreeNode("key3: abcd"))
			add(DefaultMutableTreeNode("key4: 2"))
			add(DefaultMutableTreeNode("key5: 2.0"))
			add(DefaultMutableTreeNode("key6: 1.2"))
			add(DefaultMutableTreeNode("key7: [1, 2, 3]"))
		}

		assertThat(actualTreeModel).usingRecursiveComparison()
			.isEqualTo(DefaultTreeModel(rootNode))
	}

	@Test
	fun test_prefFileToTreeModel_emptyFile() = runTest(coroutineTestRule.mainTestDispatcher) {
		prefDataStore.edit {
			it.toMutablePreferences()
		}
		val vF = getVfFile(tempFolder.root.listFiles()!![0].inputStream())
		val actualTreeModel = PreferenceUtil.preferenceFileToTreeModel(vF)
		assertThat(actualTreeModel).usingRecursiveComparison()
			.isEqualTo(DefaultTreeModel(
				DefaultMutableTreeNode("Object {0}")
			))
	}
}