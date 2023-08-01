package com.target.datastorexplorer.sync

import com.android.tools.idea.file.explorer.toolwindow.DeviceExplorerController
import com.android.tools.idea.file.explorer.toolwindow.DeviceExplorerModel
import com.android.tools.idea.file.explorer.toolwindow.adbimpl.AdbDeviceFileSystem
import com.android.tools.idea.file.explorer.toolwindow.adbimpl.AdbFileTransfer
import com.android.tools.idea.file.explorer.toolwindow.fs.DeviceFileEntry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.target.datastorexplorer.CoroutineTestRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdbSyncTest : BasePlatformTestCase() {

	@get:Rule
	val coroutineTestRule = CoroutineTestRule()

	@MockK
	lateinit var adbSyncProgress: AdbSyncProgress

	@MockK
	lateinit var deviceExplorerController: DeviceExplorerController

	@MockK
	lateinit var deviceExplorerModel: DeviceExplorerModel

	@MockK
	lateinit var adbDeviceFileSystem: AdbDeviceFileSystem

	@MockK
	lateinit var fileToSyncWith: VirtualFile

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
	}

	@Test
	fun test_downloadFile_should_pass() = runTest(coroutineTestRule.mainTestDispatcher) {
		adbSyncProgress.apply {
			every { inProgress() } just Runs
			every { completed() } just Runs
			every { error(any()) } just Runs
		}

		every { deviceExplorerController.hasActiveDevice() } returns true
		every { deviceExplorerModel.activeDevice } returns adbDeviceFileSystem
		every { DeviceExplorerController.Companion.getProjectController(project) } returns deviceExplorerController

		val path =
			"/Users/SomeLocation/ProjectName/build/idea-sandbox/system/device-explorer/samsung SM-S901E/data/data/com.example.myapplication/files/datastore/user_pref.pb"
		every { fileToSyncWith.path } returns path
		coEvery {
			adbDeviceFileSystem.getEntry("/data/data/com.example.myapplication/files/datastore/user_pref.pb")
		} returns deviceFileEntry
		every { deviceFileEntry.fullPath } returns "/data/data/com.example.myapplication/files/datastore/user_pref.pb"
		every { deviceFileEntry.size } returns 67
		every { adbDeviceFileSystem.adbFileTransfer } returns adbFileTransfer
		coJustRun {
			adbFileTransfer.downloadFileViaTempLocation(
				remotePath = "/data/data/com.example.myapplication/files/datastore/user_pref.pb",
				remotePathSize = 67,
				localPath = fileToSyncWith.toNioPath(),
				runAs = "com.example.myapplication",
				progress = any()
			)
		}
		every { fileToSyncWith.refresh(false, false) } just Runs

		val sut = AdbSync(
			project = project,
			fileToSyncWith = fileToSyncWith,
			dispatchers = coroutineTestRule.testCoroutineDispatchers,
			adbSyncProgress = adbSyncProgress
		)

		sut.downloadFile()

		verifyOrder {
			adbSyncProgress.inProgress()
			adbSyncProgress.completed()
		}
	}

	@Test
	fun test_downloadFile_should_fail_when_no_active_device() =
		runTest(coroutineTestRule.mainTestDispatcher) {
			adbSyncProgress.apply {
				every { inProgress() } just Runs
				every { completed() } just Runs
				every { error(any()) } just Runs
			}

			every { deviceExplorerController.hasActiveDevice() } returns false
			every { deviceExplorerModel.activeDevice } returns null
			every { DeviceExplorerController.Companion.getProjectController(project) } returns deviceExplorerController
			val path =
				"/Users/SomeLocation/ProjectName/build/idea-sandbox/system/device-explorer/samsung SM-S901E/data/data/com.example.myapplication/files/datastore/user_pref.pb"

			every { fileToSyncWith.path } returns path
			coEvery {
				adbDeviceFileSystem.getEntry("/data/data/com.example.myapplication/files/datastore/user_pref.pb")
			} returns deviceFileEntry
			every { deviceFileEntry.fullPath } returns "/data/data/com.example.myapplication/files/datastore/user_pref.pb"
			every { deviceFileEntry.size } returns 67
			every { adbDeviceFileSystem.adbFileTransfer } returns adbFileTransfer
			coJustRun {
				adbFileTransfer.downloadFileViaTempLocation(
					remotePath = "/data/data/com.example.myapplication/files/datastore/user_pref.pb",
					remotePathSize = 67,
					localPath = fileToSyncWith.toNioPath(),
					runAs = "com.example.myapplication",
					progress = any()
				)
			}
			every { fileToSyncWith.refresh(false, false) } just Runs

			val sut = AdbSync(
				project = project,
				fileToSyncWith = fileToSyncWith,
				dispatchers = coroutineTestRule.testCoroutineDispatchers,
				adbSyncProgress = adbSyncProgress
			)

			sut.downloadFile()

			verifyOrder {
				adbSyncProgress.inProgress()
				adbSyncProgress.error(any())
			}
		}

	override fun tearDown() {
		super.tearDown()
		unmockkObject(DeviceExplorerController::class)
		unmockkAll()
	}
}
