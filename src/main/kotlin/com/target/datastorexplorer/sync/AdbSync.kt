package com.target.datastorexplorer.sync

import com.android.tools.idea.file.explorer.toolwindow.DeviceExplorerController
import com.android.tools.idea.file.explorer.toolwindow.DeviceExplorerModel
import com.android.tools.idea.file.explorer.toolwindow.adbimpl.AdbDeviceFileSystem
import com.android.tools.idea.file.explorer.toolwindow.fs.FileTransferProgress
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.target.datastorexplorer.coroutine.CoroutineDispatchers
import com.target.datastorexplorer.provider.proto.ProtoEditor
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/**
 * This class syncs the downloaded virtual file from device-explorer.
 * @param project the current project
 * @param fileToSyncWith file to sync with, which is .pb or .preferences_pb file.
 * @param dispatchers coroutine dispatchers
 * @param adbSyncProgress callback for notifying the sync status
 */
class AdbSync(
	private val project: Project,
	private val fileToSyncWith: VirtualFile,
	private val dispatchers: CoroutineDispatchers,
	private val adbSyncProgress: AdbSyncProgress
) : Disposable {

	private val logger by lazy { Logger.getInstance(AdbSync::class.java) }

	fun init() {
		Disposer.register(ProtoEditor.parentDisposable, this)
	}

	/**
	 * This will download the file directly from device and will update the existing virtual file.
	 * It will not sync the device explorer view with new updated file.
	 */
	suspend fun downloadFile() {
		return withContext(dispatchers.io()) {
			try {
				adbSyncProgress.inProgress()
				val controller = DeviceExplorerController.getProjectController(project)!!
				val clss: Class<DeviceExplorerController> = controller.javaClass
				val field = clss.getDeclaredField("model")
				field.isAccessible = true
				val deviceExplorerModel: DeviceExplorerModel = field.get(controller) as DeviceExplorerModel

				val adbDeviceFileSystem = (deviceExplorerModel.activeDevice as AdbDeviceFileSystem)
				val deviceFileEntry =
					adbDeviceFileSystem.getEntry("/data/data/${fileToSyncWith.path.substringAfterLast("data/")}")

				adbDeviceFileSystem
					.adbFileTransfer
					.downloadFileViaTempLocation(
						remotePath = deviceFileEntry.fullPath,
						remotePathSize = deviceFileEntry.size,
						localPath = fileToSyncWith.toNioPath(),
						progress = object : FileTransferProgress {
							override fun progress(currentBytes: Long, totalBytes: Long) {}
							override fun isCancelled(): Boolean {
								return !(fileToSyncWith.exists() || isActive)
							}
						},
						runAs = fileToSyncWith.path.substringAfterLast("data/").split("/").first()
					)
				fileToSyncWith.refresh(false, false)
				adbSyncProgress.completed()
			} catch (ex: Exception) {
				logger.debug(ex)
				adbSyncProgress.error(ex)
			}
		}
	}

	override fun dispose() {}
}
