package com.target.datastorexplorer.notification

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CancellationException

private const val NOTIFICATION_GROUP = "DataStoreExplorerErrors"

object NotificationHelper {
	fun notifyError(
		project: Project,
		content: String
	) {
		NotificationGroupManager.getInstance()
			.getNotificationGroup(NOTIFICATION_GROUP)
			.createNotification(content, NotificationType.ERROR)
			.notify(project)
	}

	fun notifyInfo(
		project: Project,
		content: String
	) {
		NotificationGroupManager.getInstance()
			.getNotificationGroup(NOTIFICATION_GROUP)
			.createNotification(content, NotificationType.INFORMATION)
			.notify(project)
	}
}

fun Exception.notifyError(project: Project, message: String) {
	if (this !is CancellationException) {
		NotificationHelper.notifyError(
			project,
			message + "-" + this.message
		)
	}
}
