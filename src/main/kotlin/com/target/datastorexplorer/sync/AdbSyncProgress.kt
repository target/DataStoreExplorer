package com.target.datastorexplorer.sync

interface AdbSyncProgress {
	fun inProgress()
	fun completed()
	fun error(ex: Exception)
}
