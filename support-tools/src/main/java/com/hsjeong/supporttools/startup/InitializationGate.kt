package com.hsjeong.supporttools.startup

import java.util.concurrent.atomic.AtomicReference

internal class InitializationGate {
    private enum class State { NEW, INITIALIZING, INITIALIZED }
    private val state = AtomicReference(State.NEW)

    val isInitialized: Boolean
        get() = state.get() == State.INITIALIZED

    fun runOnce(block: () -> Unit): Boolean {
        if (!state.compareAndSet(State.NEW, State.INITIALIZING)) return false
        return try {
            block()
            state.set(State.INITIALIZED)
            true
        } catch (throwable: Throwable) {
            state.set(State.NEW)
            throw throwable
        }
    }
}
