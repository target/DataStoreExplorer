package com.target.datastorexplorer.ui.proto

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.target.datastorexplorer.coroutine.CoroutineDispatchers
import com.target.datastorexplorer.decoder.ProtoDecoder
import com.target.datastorexplorer.localization.DataStoreBundle
import com.target.datastorexplorer.notification.NotificationHelper
import com.target.datastorexplorer.notification.notifyError
import com.target.datastorexplorer.provider.proto.ProtoEditor
import com.target.datastorexplorer.sync.AdbSync
import com.target.datastorexplorer.sync.AdbSyncProgress
import com.target.datastorexplorer.ui.JsonTreeLoadingState
import com.target.datastorexplorer.util.JTreeUtil
import com.target.datastorexplorer.util.ProtoUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ProtoScreenPresenter(
	private val project: Project,
	protoBuffFile: VirtualFile,
	private val dispatcher:CoroutineDispatchers,
) : Disposable {

	private val logger by lazy { Logger.getInstance(ProtoScreenPresenter::class.java) }

	private val coroutineScope = CoroutineScope(dispatcher.main())

	private var stateFlow = MutableStateFlow(ProtoState())
	fun stateFlow(): StateFlow<ProtoState> = stateFlow

	private val protoBuffDecoder = ProtoDecoder(project = project, protoBuffFile = protoBuffFile, dispatchers = dispatcher)
	private val adbSync = AdbSync(
		project = project,
		fileToSyncWith = protoBuffFile,
		dispatchers = dispatcher,
		adbSyncProgress = object : AdbSyncProgress {
			override fun inProgress() {
				stateFlow.update {
					it.copy(loadingState = JsonTreeLoadingState.Loading)
				}
			}

			override fun completed() {
				decodeProtoFile()
			}

			override fun error(ex: Exception) {
				ex.notifyError(project, DataStoreBundle.message("notification.refresh.failed"))
				stateFlow.update {
					it.copy(loadingState = JsonTreeLoadingState.NotLoading)
				}
			}
		}
	)

	fun init() {
		Disposer.register(ProtoEditor.parentDisposable, this)
		DumbService.getInstance(project).runWhenSmart {
			stateFlow.update {
				it.copy(protoFile = ProtoUtil.getAllProtoFiles(project))
			}
		}
		adbSync.init()
	}

	private var selectedIndex = -1

	fun onRefreshClick(index: Int) {
		if (index == -1) {
			NotificationHelper.notifyError(
				project = project,
				content = DataStoreBundle.message("proto.select")
			)
			return
		}
		selectedIndex = index
		coroutineScope.launch {
			adbSync.downloadFile()
		}
	}

	private var job: Job? = null
	private fun decodeProtoFile() {
		job?.cancel()
		job = coroutineScope.launch {
			try {
				stateFlow.update {
					it.copy(
						treeModel =
						withContext(dispatcher.default()) {
							JTreeUtil.jsonToTreeModel(
								protoBuffDecoder.decodeProto(protoDefinitionFile = stateFlow.value.protoFile[selectedIndex])
							)
						}

					)
				}
			} catch (ex: Exception) {
				logger.debug(ex)
				ex.notifyError(project, DataStoreBundle.message("notification.deserialization.failed"))
			} finally {
				stateFlow.update {
					it.copy(
						loadingState = JsonTreeLoadingState.NotLoading
					)
				}
			}
		}
	}

	override fun dispose() {
		coroutineScope.cancel()
	}
}
