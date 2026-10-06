package io.github.ghostflying.ferry.source

import io.github.ghostflying.ferry.config.StoredConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** USB identity of DJI Osmo Pocket 3, observed on the target Pixel on 2026-10-06. */
const val POCKET3_VENDOR_ID = 0x2ca3
const val POCKET3_PRODUCT_ID = 0x0020

sealed interface SourceStatus {
    data object NotConfigured : SourceStatus
    data object NeedsReselect : SourceStatus

    /**
     * The Pocket enumerated but no readable directory appeared, which happens
     * when the camera is not in OTG mode.
     */
    data object PocketNeedsOtg : SourceStatus
    data object Scanning : SourceStatus
    data class Ready(val summary: ImportSummary) : SourceStatus
    data class Failed(val reason: String) : SourceStatus
}

/**
 * Status for a configured directory. Without OTG mode the Pocket detaches
 * within seconds, so [pocketSeen] is latched for the process and only an
 * accessible directory clears the hint.
 */
fun sourceAvailability(pocketSeen: Boolean, treeAccessible: Boolean): SourceStatus? = when {
    treeAccessible -> null
    pocketSeen -> SourceStatus.PocketNeedsOtg
    else -> SourceStatus.NeedsReselect
}

/** One source pass per foreground open: reconcile, check access, scan, plan and import. */
class SourceStage(
    private val config: () -> StoredConfig?,
    private val sourceTree: () -> SourceTree?,
    private val planner: Planner,
    private val importer: SourceImportCoordinator,
    private val pocketSeen: () -> Boolean,
    private val mountSettleMillis: Long = 8_000,
) {
    private val mutableStatus = MutableStateFlow<SourceStatus>(SourceStatus.NotConfigured)
    val status: StateFlow<SourceStatus> = mutableStatus.asStateFlow()

    suspend fun run() {
        importer.reconcilePartials()
        val tree = sourceTree()
        val stored = config()
        if (tree == null || stored == null) {
            mutableStatus.value = SourceStatus.NotConfigured
            return
        }
        sourceAvailability(pocketSeen(), awaitAccessible(tree))?.let {
            mutableStatus.value = it
            return
        }
        mutableStatus.value = SourceStatus.Scanning
        mutableStatus.value = try {
            SourceStatus.Ready(importer.importPlanned(tree, planner, stored.json, stored.revision))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            // An unplugged source surfaces here as an I/O error; any partial
            // copy was already removed and the next open retries.
            SourceStatus.Failed(failure.message ?: failure::class.java.simpleName)
        }
    }

    /**
     * Right after the Pocket enumerates, the system needs a moment to mount
     * the card, and no mount broadcast arrives for this kind of volume. Give
     * an attached Pocket that time before reporting the OTG hint.
     */
    private suspend fun awaitAccessible(tree: SourceTree): Boolean {
        if (tree.isAccessible()) return true
        if (!pocketSeen()) return false
        repeat((mountSettleMillis / SETTLE_POLL_MILLIS).toInt()) {
            delay(SETTLE_POLL_MILLIS)
            if (tree.isAccessible()) return true
        }
        return false
    }

    private companion object {
        const val SETTLE_POLL_MILLIS = 500L
    }
}
