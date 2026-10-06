package io.github.ghostflying.ferry.lifecycle

import io.github.ghostflying.ferry.data.OperationEntity
import io.github.ghostflying.ferry.data.OperationRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Coordinates one foreground worker without adding a background-service promise. */
class ForegroundExecutionCoordinator(
    private val repository: OperationRepository,
    private val scope: CoroutineScope,
    private val sourceStage: (suspend () -> Unit)? = null,
    private val sourceDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val upload: (suspend (OperationEntity) -> String)?,
) {
    private val lifecycleMutex = Mutex()
    private var worker: Job? = null
    @Volatile
    private var action: Job? = null
    @Volatile
    private var activeOperationId: String? = null
    @Volatile
    private var stopped = true

    suspend fun onOpen() = lifecycleMutex.withLock {
        stopped = false
        if (worker?.isActive == true) return@withLock
        // No worker is running, so any uploading/verifying row was left by a
        // killed process and must become eligible again.
        repository.recoverInterrupted()
        worker = scope.launch { runLoop() }
    }

    suspend fun onStop() = lifecycleMutex.withLock {
        stopped = true
        // Release rows only after the worker has stopped so a completion
        // recorded before the cancel is not overwritten.
        action?.cancel()
        worker?.cancelAndJoin()
        repository.markSystemWaiting()
        worker = null
        action = null
        activeOperationId = null
    }

    suspend fun pause(operationId: String): Boolean {
        val actionToCancel = lifecycleMutex.withLock {
            val changed = repository.pauseAndReport(operationId)
            if (changed && activeOperationId == operationId) action else null
        }
        val changed = actionToCancel != null || repository.isManuallyPaused(operationId)
        actionToCancel?.cancelAndJoin()
        return changed
    }

    private suspend fun runLoop() = supervisorScope {
        // The source pass runs once per open, before uploads and whether or
        // not an uploader exists; cancelling the worker cancels it.
        sourceStage?.let { withContext(sourceDispatcher) { it() } }
        while (currentCoroutineContext().isActive && !stopped) {
            val uploadAction = upload ?: return@supervisorScope
            val dispatch = lifecycleMutex.withLock {
                if (stopped) return@withLock null
                val candidate = repository.eligibleOperations().firstOrNull() ?: return@withLock null
                if (!repository.claimForUpload(candidate)) return@withLock Dispatch(null, null)
                activeOperationId = candidate.id
                val action = CoroutineScope(currentCoroutineContext()).async(start = CoroutineStart.LAZY) {
                    uploadAction(candidate)
                }
                this@ForegroundExecutionCoordinator.action = action
                Dispatch(candidate, action)
            } ?: return@supervisorScope
            val candidate = dispatch.operation ?: continue
            val currentAction = dispatch.action ?: continue
            try {
                val remoteSha = currentAction.await().lowercase()
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

    private data class Dispatch(
        val operation: OperationEntity?,
        val action: Deferred<String>?,
    )
}
