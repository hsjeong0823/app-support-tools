package com.hsjeong.supporttools.startup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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
