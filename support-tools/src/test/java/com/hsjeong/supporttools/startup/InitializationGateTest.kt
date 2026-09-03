package com.hsjeong.supporttools.startup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class InitializationGateTest {
    @Test
    fun runOnce_executesOnlyTheFirstSuccessfulBlock() {
        val gate = InitializationGate()
        var calls = 0

        assertTrue(gate.runOnce { calls++ })
        assertFalse(gate.runOnce { calls++ })

        assertEquals(1, calls)
        assertTrue(gate.isInitialized)
    }

    @Test
    fun runOnce_allowsRetryWhenTheBlockFails() {
        val gate = InitializationGate()

        runCatching {
            gate.runOnce { error("first attempt") }
        }

        assertFalse(gate.isInitialized)
        assertTrue(gate.runOnce { })
        assertTrue(gate.isInitialized)
    }

    @Test
    fun runOnce_concurrentCallersExecuteTheBlockAtMostOnce() {
        val gate = InitializationGate()
        val callerCount = 8
        val executor = Executors.newFixedThreadPool(callerCount)
        val callersReady = CountDownLatch(callerCount)
        val start = CountDownLatch(1)
        val blockEntered = CountDownLatch(1)
        val releaseBlock = CountDownLatch(1)
        val skippedCallersReturned = CountDownLatch(callerCount - 1)
        val executions = AtomicInteger()
        val futures = List(callerCount) {
            executor.submit<Boolean> {
                callersReady.countDown()
                check(start.await(5, TimeUnit.SECONDS))

                val executed = gate.runOnce {
                    executions.incrementAndGet()
                    blockEntered.countDown()
                    check(releaseBlock.await(5, TimeUnit.SECONDS))
                }
                if (!executed) skippedCallersReturned.countDown()
                executed
            }
        }

        try {
            assertTrue(callersReady.await(5, TimeUnit.SECONDS))
            start.countDown()
            assertTrue(blockEntered.await(5, TimeUnit.SECONDS))
            assertTrue(skippedCallersReturned.await(5, TimeUnit.SECONDS))
            releaseBlock.countDown()

            val results = futures.map { it.get(5, TimeUnit.SECONDS) }
            assertEquals(1, results.count { it })
            assertEquals(callerCount - 1, results.count { !it })
            assertEquals(1, executions.get())
            assertTrue(gate.isInitialized)
        } finally {
            start.countDown()
            releaseBlock.countDown()
            executor.shutdownNow()
        }
    }

    @Test
    fun runOnce_concurrentFailureAllowsLaterRetry() {
        val gate = InitializationGate()
        val callerCount = 8
        val executor = Executors.newFixedThreadPool(callerCount)
        val callersReady = CountDownLatch(callerCount)
        val start = CountDownLatch(1)
        val failingBlockEntered = CountDownLatch(1)
        val releaseFailingBlock = CountDownLatch(1)
        val skippedCallersReturned = CountDownLatch(callerCount - 1)
        val failingExecutions = AtomicInteger()
        val futures = List(callerCount) {
            executor.submit<Boolean> {
                callersReady.countDown()
                check(start.await(5, TimeUnit.SECONDS))

                val executed = gate.runOnce {
                    failingExecutions.incrementAndGet()
                    failingBlockEntered.countDown()
                    check(releaseFailingBlock.await(5, TimeUnit.SECONDS))
                    error("concurrent failure")
                }
                if (!executed) skippedCallersReturned.countDown()
                executed
            }
        }

        try {
            assertTrue(callersReady.await(5, TimeUnit.SECONDS))
            start.countDown()
            assertTrue(failingBlockEntered.await(5, TimeUnit.SECONDS))
            assertTrue(skippedCallersReturned.await(5, TimeUnit.SECONDS))
            releaseFailingBlock.countDown()

            val outcomes = futures.map { future ->
                runCatching { future.get(5, TimeUnit.SECONDS) }
            }
            val failure = outcomes.single { it.isFailure }.exceptionOrNull()

            assertTrue(failure is ExecutionException)
            assertEquals("concurrent failure", failure?.cause?.message)
            assertEquals(callerCount - 1, outcomes.count { it.getOrNull() == false })
            assertEquals(1, failingExecutions.get())
            assertFalse(gate.isInitialized)

            val retryExecutions = AtomicInteger()
            assertTrue(gate.runOnce { retryExecutions.incrementAndGet() })
            assertEquals(1, retryExecutions.get())
            assertTrue(gate.isInitialized)
        } finally {
            start.countDown()
            releaseFailingBlock.countDown()
            executor.shutdownNow()
        }
    }

    @Test
    fun independentGates_doNotRepeatCompletedSideEffectsAfterLaterFailure() {
        val completedSideEffect = InitializationGate()
        val retryableSideEffect = InitializationGate()
        var completedCalls = 0
        var retryableCalls = 0

        assertTrue(completedSideEffect.runOnce { completedCalls++ })
        runCatching { retryableSideEffect.runOnce { retryableCalls++; error("later stage") } }

        assertFalse(completedSideEffect.runOnce { completedCalls++ })
        assertTrue(retryableSideEffect.runOnce { retryableCalls++ })
        assertEquals(1, completedCalls)
        assertEquals(2, retryableCalls)
    }
}
