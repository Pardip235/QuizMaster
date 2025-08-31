package com.pardip.quizmaster.testutil


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher

/**
 * Rules & helpers
 */
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: org.junit.runner.Description?) {
        Dispatchers.setMain(testDispatcher)
    }
    override fun finished(description: org.junit.runner.Description?) {
        Dispatchers.resetMain()
    }
}