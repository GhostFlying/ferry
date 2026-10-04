package io.github.ghostflying.ferry.data

object PausePolicy {
    fun phaseWhenLeavingForeground(manualPaused: Boolean): String =
        if (manualPaused) "paused" else "waiting"

    fun canResumeOnOpen(manualPaused: Boolean, phase: String): Boolean =
        !manualPaused && phase == "waiting"
}
