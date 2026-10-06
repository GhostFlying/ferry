package io.github.ghostflying.ferry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.room.Room
import io.github.ghostflying.ferry.data.FerryDatabase
import io.github.ghostflying.ferry.data.OperationRepository
import io.github.ghostflying.ferry.lifecycle.ForegroundExecutionCoordinator
import io.github.ghostflying.ferry.ui.FerryApp
import io.github.ghostflying.ferry.ui.FerryUiController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var database: FerryDatabase
    private lateinit var controller: FerryUiController
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = Room.databaseBuilder(this, FerryDatabase::class.java, "ferry.db").build()
        val repository = OperationRepository(database.operationDao())
        val coordinator = ForegroundExecutionCoordinator(
            repository = repository,
            scope = activityScope,
            upload = null,
        )
        controller = FerryUiController(repository, coordinator)
        setContent {
            FerryApp(controller)
        }
    }

    override fun onStart() {
        super.onStart()
        if (::controller.isInitialized) activityScope.launch { controller.onStart() }
    }

    override fun onResume() {
        super.onResume()
        if (::controller.isInitialized) activityScope.launch { controller.reload() }
    }

    override fun onStop() {
        if (::controller.isInitialized) activityScope.launch { controller.onStop() }
        super.onStop()
    }

    override fun onDestroy() {
        activityScope.launch {
            try {
                if (::controller.isInitialized) controller.shutdown()
            } finally {
                if (::database.isInitialized) database.close()
                activityScope.cancel()
            }
        }
        super.onDestroy()
    }
}
