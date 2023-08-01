package com.target.datastorexplorer.decoder

import com.android.tools.idea.util.toVirtualFile
import com.intellij.testFramework.ApplicationRule
import com.intellij.testFramework.ProjectRule
import com.target.datastorexplorer.CoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ProtoDecoderTest  {

	@get:Rule
	val coroutineTestRule = CoroutineTestRule()

	@get:Rule
	val appRule = ApplicationRule()

	@get:Rule
	val projectRule = ProjectRule()

	@Test
	fun test_decodeProto_valid() = runTest(coroutineTestRule.mainTestDispatcher) {
		val pbFile = File(this::class.java.getResource("/user_pref.pb")!!.file)
			.toVirtualFile()
		val protoFile = File(this::class.java.getResource("/user_preferences.proto")!!.file)
			.toVirtualFile()
		val sut = ProtoDecoder(
			project = projectRule.project,
			protoBuffFile = pbFile!!,
			dispatchers = coroutineTestRule.testCoroutineDispatchers,
		)
		val jsonString = sut.decodeProto(protoFile!!)
		assertThat(
			jsonString
		).isEqualTo(
			"""
				{
				  "deprecatedIntFollowedTopicIds": [1, 5, 1, 5, 1, 5, 1, 5, 1, 5],
				  "topicChangeListVersion": 10,
				  "hasDoneIntToStringIdMigration": true,
				  "deprecatedFollowedTopicIds": ["A", "B", "A", "B", "A", "B", "A", "B", "A", "B"],
				  "deprecatedFollowedAuthorIds": ["R", "S", "R", "S", "R", "S", "R", "S", "R", "S"],
				  "deprecatedBookmarkedNewsResourceIds": ["1", "2", "1", "2", "1", "2", "1", "2", "1", "2"],
				  "themeBrand": "THEME_BRAND_ANDROID",
				  "darkThemeConfig": "DARK_THEME_CONFIG_FOLLOW_SYSTEM",
				  "userPref": {
				    "deprecatedIntFollowedTopicIds": [1, 5],
				    "topicChangeListVersion": 10,
				    "hasDoneIntToStringIdMigration": true,
				    "deprecatedFollowedTopicIds": ["A", "B"],
				    "deprecatedFollowedAuthorIds": ["R", "S"],
				    "deprecatedBookmarkedNewsResourceIds": ["1", "2"],
				    "themeBrand": "THEME_BRAND_ANDROID",
				    "darkThemeConfig": "DARK_THEME_CONFIG_FOLLOW_SYSTEM"
				  }
				}
			""".trimIndent()
		)
	}

	@Test
	fun test_decodeProto_wrong_protoFile() = runTest(coroutineTestRule.mainTestDispatcher) {
		val pbFile = File(this::class.java.getResource("/user_pref.pb")!!.file)
			.toVirtualFile()
		val protoFile = File(this::class.java.getResource("/theme_brand.proto")!!.file)
			.toVirtualFile()
		val sut = ProtoDecoder(
			project = projectRule.project,
			protoBuffFile = pbFile!!,
			dispatchers = coroutineTestRule.testCoroutineDispatchers,
		)
		assertThat(
			sut.decodeProto(protoFile!!)
		).isEmpty()
	}

	@Test
	fun test_decodeProto_wrong_pbFile() = runTest(coroutineTestRule.mainTestDispatcher) {
		val pbFile = File(this::class.java.getResource("/user_pref_invalid.pb")!!.file)
			.toVirtualFile()
		val protoFile = File(this::class.java.getResource("/user_preferences.proto")!!.file)
			.toVirtualFile()
		val sut = ProtoDecoder(
			project = projectRule.project,
			protoBuffFile = pbFile!!,
			dispatchers = coroutineTestRule.testCoroutineDispatchers,
		)
		assertThat(
			sut.decodeProto(protoFile!!)
		).isEmpty()
	}
}
