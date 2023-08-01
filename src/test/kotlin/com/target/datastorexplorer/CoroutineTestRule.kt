package com.target.datastorexplorer

import com.target.datastorexplorer.coroutine.CoroutineDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineTestRule(
	val mainTestDispatcher: TestDispatcher = StandardTestDispatcher(),
	val defaultTestDispatcher: TestDispatcher = StandardTestDispatcher(mainTestDispatcher.scheduler),
	val ioTestDispatcher: TestDispatcher = StandardTestDispatcher(mainTestDispatcher.scheduler),
	val unconfinedTestDispatcher: TestDispatcher = StandardTestDispatcher(mainTestDispatcher.scheduler),
) : TestWatcher() {

	val testCoroutineDispatchers = object : CoroutineDispatchers {
		override fun main(): CoroutineDispatcher = mainTestDispatcher
		override fun default(): CoroutineDispatcher = defaultTestDispatcher
		override fun io(): CoroutineDispatcher = ioTestDispatcher
		override fun unconfined(): CoroutineDispatcher = unconfinedTestDispatcher
	}

	override fun starting(description: Description) {
		Dispatchers.setMain(mainTestDispatcher)
	}

	override fun finished(description: Description) {
		Dispatchers.resetMain()
	}
}