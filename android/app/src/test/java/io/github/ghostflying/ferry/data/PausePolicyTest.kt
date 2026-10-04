package io.github.ghostflying.ferry.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PausePolicyTest {
    @Test
    fun manualPauseSurvivesOpen() {
        assertFalse(PausePolicy.canResumeOnOpen(manualPaused = true, phase = "paused"))
    }

    @Test
    fun systemWaitResumesOnOpen() {
        assertTrue(PausePolicy.canResumeOnOpen(manualPaused = false, phase = "waiting"))
    }
}
