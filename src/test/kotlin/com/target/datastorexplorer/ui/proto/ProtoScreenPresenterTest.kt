package com.target.datastorexplorer.ui.proto

import com.android.tools.idea.file.explorer.toolwindow.DeviceExplorerController
import com.android.tools.idea.file.explorer.toolwindow.DeviceExplorerModel
import com.android.tools.idea.file.explorer.toolwindow.adbimpl.AdbDeviceFileSystem
import com.android.tools.idea.file.explorer.toolwindow.adbimpl.AdbFileTransfer
import com.android.tools.idea.file.explorer.toolwindow.fs.DeviceFileEntry
import com.android.tools.idea.util.toVirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.target.datastorexplorer.CoroutineTestRule
import com.target.datastorexplorer.sync.AdbSyncProgress
import com.target.datastorexplorer.ui.JsonTreeLoadingState
import com.target.datastorexplorer.util.ProtoUtil
import io.mockk.*
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ProtoScreenPresenterTest : BasePlatformTestCase() {

	@get:Rule
	val coroutineTestRule = CoroutineTestRule()

	@MockK
	lateinit var deviceExplorerController: DeviceExplorerController

	@MockK
	lateinit var deviceExplorerModel: DeviceExplorerModel

	@MockK
	lateinit var adbSyncProgress: AdbSyncProgress

	@MockK
	lateinit var adbDeviceFileSystem: AdbDeviceFileSystem

	@MockK
	lateinit var deviceFileEntry: DeviceFileEntry

	@MockK
	lateinit var adbFileTransfer: AdbFileTransfer

	override fun setUp() {
		super.setUp()
		clearAllMocks()
		MockKAnnotations.init(this)

		mockkObject(DeviceExplorerController)
		val clss: Class<DeviceExplorerController> = deviceExplorerController.javaClass
		val field = clss.getDeclaredField("model")
		field.isAccessible = true
		field.set(deviceExplorerController, deviceExplorerModel)

		adbSyncProgress.apply {
			every { inProgress() } just Runs
			every { completed() } just Runs
			every { error(any()) } just Runs
		}

		every { deviceExplorerController.hasActiveDevice() } returns true
		every { deviceExplorerModel.activeDevice } returns adbDeviceFileSystem
		every { DeviceExplorerController.Companion.getProjectController(project) } returns deviceExplorerController

	}

	override fun tearDown() {
		super.tearDown()
		unmockkAll()
		unmockkObject(ProtoUtil)
	}

	@Test
	fun test_onRefreshClick_when_adbSync_and_decode_proto_succeeds() = runTest(coroutineTestRule.mainTestDispatcher) {
		val protoBuffFile = File(this::class.java.getResource("/user_pref.pb")!!.file).toVirtualFile()!!
		val protoDefinitionFile = File(this::class.java.getResource("/user_preferences.proto")!!.file).toVirtualFile()!!

		mockkObject(ProtoUtil)
		every { ProtoUtil.getAllProtoFiles(project) } returns listOf(protoDefinitionFile)

		coEvery { adbDeviceFileSystem.getEntry(any()) } returns deviceFileEntry
		every { deviceFileEntry.fullPath } returns protoBuffFile.path
		every { deviceFileEntry.size } returns 67
		every { adbDeviceFileSystem.adbFileTransfer } returns adbFileTransfer
		coJustRun {
			adbFileTransfer.downloadFileViaTempLocation(
				remotePath = any(),
				remotePathSize = 67,
				localPath = protoBuffFile.toNioPath(),
				runAs = any(),
				progress = any()
			)
		}

		val sut = ProtoScreenPresenter(
			project = project,
			protoBuffFile = protoBuffFile,
			dispatcher = coroutineTestRule.testCoroutineDispatchers
		)

		sut.init()
		TestCase.assertEquals(
			sut.stateFlow().value,
			ProtoState(
				protoFile = listOf(protoDefinitionFile),
				loadingState = null,
				treeModel = null
			)
		)
		sut.onRefreshClick(0)
		advanceUntilIdle()
		TestCase.assertEquals(sut.stateFlow().value.protoFile, listOf(protoDefinitionFile))
		TestCase.assertEquals(sut.stateFlow().value.loadingState, JsonTreeLoadingState.NotLoading)
		TestCase.assertNotNull(sut.stateFlow().value.treeModel)
	}

	@Test
	fun test_onRefreshClick_when_adbSync_errors_out() = runTest(coroutineTestRule.mainTestDispatcher) {
		val protoBuffFile = File(this::class.java.getResource("/user_pref.pb")!!.file).toVirtualFile()!!
		val protoDefinitionFile = File(this::class.java.getResource("/user_preferences.proto")!!.file).toVirtualFile()!!

		mockkObject(ProtoUtil)
		every { ProtoUtil.getAllProtoFiles(project) } returns listOf(protoDefinitionFile)

		every { deviceExplorerController.hasActiveDevice() } returns false
		every { deviceExplorerModel.activeDevice } returns null

		coEvery { adbDeviceFileSystem.getEntry(any()) } returns deviceFileEntry
		every { deviceFileEntry.fullPath } returns protoBuffFile.path
		every { deviceFileEntry.size } returns 67
		every { adbDeviceFileSystem.adbFileTransfer } returns adbFileTransfer
		coJustRun {
			adbFileTransfer.downloadFileViaTempLocation(
				remotePath = any(),
				remotePathSize = 67,
				localPath = protoBuffFile.toNioPath(),
				runAs = any(),
				progress = any()
			)
		}

		val sut = ProtoScreenPresenter(
			project = project,
			protoBuffFile = protoBuffFile,
			dispatcher = coroutineTestRule.testCoroutineDispatchers
		)

		sut.init()
		TestCase.assertEquals(
			sut.stateFlow().value,
			ProtoState(
				protoFile = listOf(protoDefinitionFile),
				loadingState = null,
				treeModel = null
			)
		)
		sut.onRefreshClick(0)
		advanceUntilIdle()
		TestCase.assertEquals(sut.stateFlow().value.protoFile, listOf(protoDefinitionFile))
		TestCase.assertEquals(sut.stateFlow().value.loadingState, JsonTreeLoadingState.NotLoading)
		TestCase.assertNull(sut.stateFlow().value.treeModel)
	}

	@Test
	fun test_onRefreshClick_when_no_proto_definition_file_selected() = runTest(coroutineTestRule.mainTestDispatcher) {
		val protoBuffFile = File(this::class.java.getResource("/user_pref.pb")!!.file).toVirtualFile()!!

		mockkObject(ProtoUtil)
		every { ProtoUtil.getAllProtoFiles(project) } returns listOf()

		coEvery { adbDeviceFileSystem.getEntry(any()) } returns deviceFileEntry
		every { deviceFileEntry.fullPath } returns protoBuffFile.path
		every { deviceFileEntry.size } returns 67
		every { adbDeviceFileSystem.adbFileTransfer } returns adbFileTransfer
		coJustRun {
			adbFileTransfer.downloadFileViaTempLocation(
				remotePath = any(),
				remotePathSize = 67,
				localPath = protoBuffFile.toNioPath(),
				runAs = any(),
				progress = any()
			)
		}

		val sut = ProtoScreenPresenter(
			project = project,
			protoBuffFile = protoBuffFile,
			dispatcher = coroutineTestRule.testCoroutineDispatchers
		)

		sut.init()
		TestCase.assertEquals(
			sut.stateFlow().value,
			ProtoState(
				protoFile = listOf(),
				loadingState = null,
				treeModel = null
			)
		)
		sut.onRefreshClick(-1)
		advanceUntilIdle()
		TestCase.assertEquals(
			sut.stateFlow().value,
			ProtoState(
				protoFile = listOf(),
				loadingState = null,
				treeModel = null
			)
		)
	}
}