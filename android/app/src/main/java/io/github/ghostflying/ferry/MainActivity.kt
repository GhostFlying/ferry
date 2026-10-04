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
            upload = { error("upload action is not wired in the pre-device UI build") },
        )
        controller = FerryUiController(repository, coordinator)
        setContent {
            FerryApp(controller)
        }
        activityScope.launch { controller.onOpen() }
    }

    override fun onResume() {
        super.onResume()
        if (::controller.isInitialized) activityScope.launch { controller.reload() }
    }

    override fun onDestroy() {
        activityScope.cancel()
        if (::database.isInitialized) database.close()
        super.onDestroy()
    }
}
