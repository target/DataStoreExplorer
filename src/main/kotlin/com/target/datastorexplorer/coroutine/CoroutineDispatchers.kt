package com.target.datastorexplorer.coroutine

import kotlinx.coroutines.CoroutineDispatcher

interface CoroutineDispatchers {
	fun main(): CoroutineDispatcher
	fun default(): CoroutineDispatcher
	fun io(): CoroutineDispatcher
	fun unconfined(): CoroutineDispatcher
}
