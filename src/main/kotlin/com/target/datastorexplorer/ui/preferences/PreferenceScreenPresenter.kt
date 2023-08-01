package com.target.datastorexplorer.ui.preferences

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.target.datastorexplorer.coroutine.CoroutineDispatchers
import com.target.datastorexplorer.localization.DataStoreBundle
import com.target.datastorexplorer.notification.notifyError
import com.target.datastorexplorer.provider.preferences.PreferencesEditor
import com.target.datastorexplorer.sync.AdbSync
import com.target.datastorexplorer.sync.AdbSyncProgress
import com.target.datastorexplorer.ui.JsonTreeLoadingState
import com.target.datastorexplorer.ui.proto.ProtoScreenPresenter
import com.target.datastorexplorer.util.PreferenceUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class PreferenceScreenPresenter(
	private val project: Project,
	private val preferenceFile: VirtualFile,
	private val dispatcher: CoroutineDispatchers,
) : Disposable {

	private val logger by lazy { Logger.getInstance(ProtoScreenPresenter::class.java) }

	private val coroutineScope = CoroutineScope(dispatcher.main())

	private var stateFlow = MutableStateFlow(PreferenceState())
	fun stateFlow(): StateFlow<PreferenceState> = stateFlow

	private val adbSync = AdbSync(
		project = project,
		fileToSyncWith = preferenceFile,
		dispatchers = dispatcher,
		adbSyncProgress = object : AdbSyncProgress {
			override fun inProgress() {
				stateFlow.update {
					it.copy(loadingState = JsonTreeLoadingState.Loading)
				}
			}

			override fun completed() {
				decodePrefFile()
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
		Disposer.register(PreferencesEditor.parentDisposable, this)
		adbSync.init()
		onRefreshClick()
	}

	fun onRefreshClick(){
		coroutineScope.launch {
			adbSync.downloadFile()
		}
	}

	private var job: Job? = null
	private fun decodePrefFile() {
		job?.cancel()
		job = coroutineScope.launch {
			try {
				stateFlow.update {
					it.copy(
						treeModel = withContext(dispatcher.default()) {
							PreferenceUtil.preferenceFileToTreeModel(
								preferenceFile = preferenceFile
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