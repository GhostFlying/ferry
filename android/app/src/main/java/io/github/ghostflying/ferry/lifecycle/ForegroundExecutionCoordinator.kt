package io.github.ghostflying.ferry.lifecycle

import io.github.ghostflying.ferry.data.OperationEntity
import io.github.ghostflying.ferry.data.OperationRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Coordinates one foreground worker without adding a background-service promise. */
class ForegroundExecutionCoordinator(
    private val repository: OperationRepository,
    private val scope: CoroutineScope,
    private val upload: suspend (OperationEntity) -> String,
) {
    private val lifecycleMutex = Mutex()
    private var worker: Job? = null
    private var action: Job? = null
    @Volatile
    private var activeOperationId: String? = null
    @Volatile
    private var stopped = true

    suspend fun onOpen() = lifecycleMutex.withLock {
        stopped = false
        if (worker?.isActive == true) return@withLock
        worker = scope.launch { runLoop() }
    }

    suspend fun onStop() = lifecycleMutex.withLock {
        stopped = true
        repository.markSystemWaiting()
        action?.cancel()
        worker?.cancelAndJoin()
        worker = null
        action = null
        activeOperationId = null
    }

    suspend fun pause(operationId: String): Boolean {
        val changed = repository.pauseAndReport(operationId)
        if (changed && activeOperationId == operationId) action?.cancel()
        return changed
    }

    private suspend fun runLoop() {
        while (currentCoroutineContext().isActive && !stopped) {
            val candidate = repository.eligibleOperations().firstOrNull() ?: return
            if (!repository.claimForUpload(candidate)) continue
            activeOperationId = candidate.id
            try {
                val remoteSha = coroutineScope {
                    val currentAction = async { upload(candidate) }
                    action = currentAction
                    try {
                        currentAction.await().lowercase()
                    } finally {
                        action = null
                    }
                }
                currentCoroutineContext().ensureActive()
                if (!repository.markCompleted(candidate, remoteSha) &&
                    !repository.isManuallyPaused(candidate.id)
                ) {
                    repository.markFailed(candidate, "remote SHA-256 did not match the private copy")
                }
            } catch (cancelled: CancellationException) {
                if (!repository.isManuallyPaused(candidate.id)) throw cancelled
            } catch (failure: Throwable) {
                repository.markFailed(candidate, failure.message ?: failure::class.simpleName.orEmpty())
            } finally {
                action = null
                activeOperationId = null
            }
        }
    }
}
