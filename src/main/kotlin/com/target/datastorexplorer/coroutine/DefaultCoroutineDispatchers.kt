package com.target.datastorexplorer.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class DefaultCoroutineDispatchers : CoroutineDispatchers {
	override fun main(): CoroutineDispatcher = Dispatchers.Main

	override fun default(): CoroutineDispatcher = Dispatchers.Default

	override fun io(): CoroutineDispatcher = Dispatchers.IO

	override fun unconfined(): CoroutineDispatcher = Dispatchers.Unconfined
}
