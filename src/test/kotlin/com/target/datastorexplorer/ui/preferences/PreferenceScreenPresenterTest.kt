package com.target.datastorexplorer.ui.preferences

import com.android.tools.idea.file.explorer.toolwindow.DeviceExplorerController
import com.android.tools.idea.file.explorer.toolwindow.DeviceExplorerModel
import com.android.tools.idea.file.explorer.toolwindow.adbimpl.AdbDeviceFileSystem
import com.android.tools.idea.file.explorer.toolwindow.adbimpl.AdbFileTransfer
import com.android.tools.idea.file.explorer.toolwindow.fs.DeviceFileEntry
import com.android.tools.idea.util.toVirtualFile
import com.intellij.testFramework.ApplicationRule
import com.intellij.testFramework.ProjectRule
import com.target.datastorexplorer.CoroutineTestRule
import com.target.datastorexplorer.sync.AdbSyncProgress
import com.target.datastorexplorer.ui.JsonTreeLoadingState
import io.mockk.*
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class PreferenceScreenPresenterTest {

	@get:Rule
	val coroutineTestRule = CoroutineTestRule()

	@get:Rule
	val appRule = ApplicationRule()

	@get:Rule
	val projectRule = ProjectRule()

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

	@Before
	fun setUp() {
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
		every { DeviceExplorerController.getProjectController(projectRule.project) } returns deviceExplorerController
	}

	@After
	fun tearDown() {
		unmockkAll()
	}

	@Test
	fun test_onRefreshClick_when_adbSync_and_decoding_succeeds() = runTest(coroutineTestRule.mainTestDispatcher) {
		val preferenceFile = File(this::class.java.getResource("/settings.preferences_pb")!!.file).toVirtualFile()!!

		coEvery { adbDeviceFileSystem.getEntry(any()) } returns deviceFileEntry
		every { deviceFileEntry.fullPath } returns preferenceFile.path
		every { deviceFileEntry.size } returns 67
		every { adbDeviceFileSystem.adbFileTransfer } returns adbFileTransfer
		coJustRun {
			adbFileTransfer.downloadFileViaTempLocation(
				remotePath = any(),
				remotePathSize = 67,
				localPath = preferenceFile.toNioPath(),
				runAs = any(),
				progress = any()
			)
		}

		val sut = PreferenceScreenPresenter(
			project = projectRule.project,
			preferenceFile = preferenceFile,
			dispatcher = coroutineTestRule.testCoroutineDispatchers
		)

		sut.init()
		TestCase.assertEquals(
			sut.stateFlow().value,
			PreferenceState(
				treeModel = null,
				loadingState = null
			)
		)
		advanceUntilIdle()
		TestCase.assertEquals(
			sut.stateFlow().value.loadingState,
			JsonTreeLoadingState.NotLoading
		)
		TestCase.assertNotNull(sut.stateFlow().value.treeModel)
	}

	@Test
	fun test_onRefreshClick_when_adbSync_errors_out() = runTest(coroutineTestRule.mainTestDispatcher) {
		val preferenceFile = File(this::class.java.getResource("/settings.preferences_pb")!!.file).toVirtualFile()!!

		every { deviceExplorerController.hasActiveDevice() } returns false
		every { deviceExplorerModel.activeDevice } returns null

		coEvery { adbDeviceFileSystem.getEntry(any()) } returns deviceFileEntry
		every { deviceFileEntry.fullPath } returns preferenceFile.path
		every { deviceFileEntry.size } returns 67
		every { adbDeviceFileSystem.adbFileTransfer } returns adbFileTransfer
		coJustRun {
			adbFileTransfer.downloadFileViaTempLocation(
				remotePath = any(),
				remotePathSize = 67,
				localPath = preferenceFile.toNioPath(),
				runAs = any(),
				progress = any()
			)
		}

		val sut = PreferenceScreenPresenter(
			project = projectRule.project,
			preferenceFile = preferenceFile,
			dispatcher = coroutineTestRule.testCoroutineDispatchers
		)

		sut.init()
		TestCase.assertEquals(
			sut.stateFlow().value,
			PreferenceState(
				treeModel = null,
				loadingState = null
			)
		)
		advanceUntilIdle()
		TestCase.assertEquals(
			sut.stateFlow().value.loadingState,
			JsonTreeLoadingState.NotLoading
		)
		TestCase.assertNull(sut.stateFlow().value.treeModel)
	}
}
