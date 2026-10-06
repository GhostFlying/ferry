package io.github.ghostflying.ferry

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.room.Room
import io.github.ghostflying.ferry.config.BridgeCore
import io.github.ghostflying.ferry.config.ConfigStore
import io.github.ghostflying.ferry.data.FerryDatabase
import io.github.ghostflying.ferry.data.OperationRepository
import io.github.ghostflying.ferry.lifecycle.ForegroundExecutionCoordinator
import io.github.ghostflying.ferry.source.POCKET3_PRODUCT_ID
import io.github.ghostflying.ferry.source.POCKET3_VENDOR_ID
import io.github.ghostflying.ferry.source.SafSourceTree
import io.github.ghostflying.ferry.source.SourceImportCoordinator
import io.github.ghostflying.ferry.source.SourceStage
import io.github.ghostflying.ferry.source.SourceStatus
import io.github.ghostflying.ferry.ui.FerryApp
import io.github.ghostflying.ferry.ui.FerryUiController
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var database: FerryDatabase
    private lateinit var controller: FerryUiController
    private lateinit var configStore: ConfigStore
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val pickSource = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri == null) return@registerForActivityResult
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        configStore.saveSourceTree(uri.toString())
        // The result arrives after onStart, whose source pass used the old
        // directory, so restart the worker with the new one.
        activityScope.launch { controller.restart() }
    }

    // Without OTG mode the Pocket enumerates for only a few seconds, so both
    // a device-list check on start and attach events are needed to see it.
    private val sourceEvents = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)?.let(::notePocket)
            }
            recheckSource()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = Room.databaseBuilder(this, FerryDatabase::class.java, "ferry.db").build()
        configStore = ConfigStore(configPreferences(this), BridgeCore)
        val repository = OperationRepository(database.operationDao())
        val sourceStage = SourceStage(
            config = { configStore.config },
            sourceTree = { configStore.sourceTreeUri?.let { SafSourceTree(contentResolver, Uri.parse(it)) } },
            planner = BridgeCore,
            importer = SourceImportCoordinator(
                spoolRoot = File(filesDir, "spool"),
                partialRoot = File(filesDir, "spool-partial"),
                repository = repository,
            ),
            pocketSeen = { pocketSeen },
        )
        val coordinator = ForegroundExecutionCoordinator(
            repository = repository,
            scope = activityScope,
            sourceStage = sourceStage::run,
            upload = null,
        )
        controller = FerryUiController(repository, coordinator)
        activityScope.launch {
            sourceStage.status.collect { status ->
                Log.i(TAG, "source status: $status")
                val label = configStore.sourceTreeUri?.let { SafSourceTree.label(Uri.parse(it)) }
                controller.updateSource(label, status)
                if (status is SourceStatus.Ready) controller.reload()
            }
        }
        setContent {
            FerryApp(controller, onPickSource = { pickSource.launch(null) })
        }
    }

    override fun onStart() {
        super.onStart()
        getSystemService(UsbManager::class.java)?.deviceList?.values?.forEach(::notePocket)
        val attach = IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        val mounts = IntentFilter(Intent.ACTION_MEDIA_MOUNTED).apply { addDataScheme("file") }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(sourceEvents, attach, RECEIVER_NOT_EXPORTED)
            registerReceiver(sourceEvents, mounts, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(sourceEvents, attach)
            registerReceiver(sourceEvents, mounts)
        }
        if (::controller.isInitialized) activityScope.launch { controller.onStart() }
    }

    override fun onResume() {
        super.onResume()
        if (::controller.isInitialized) activityScope.launch { controller.reload() }
    }

    override fun onStop() {
        unregisterReceiver(sourceEvents)
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

    private fun notePocket(device: UsbDevice) {
        if (device.vendorId == POCKET3_VENDOR_ID && device.productId == POCKET3_PRODUCT_ID) {
            pocketSeen = true
        }
    }

    /** Runs another source pass when the worker is idle and the app is in the foreground. */
    private fun recheckSource() {
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return
        activityScope.launch { controller.onStart() }
    }

    companion object {
        private const val TAG = "FerrySource"

        /** Latched for the process; only an accessible directory clears the OTG hint. */
        @Volatile
        private var pocketSeen = false

        fun configPreferences(context: Context): SharedPreferences =
            context.getSharedPreferences("ferry_config", Context.MODE_PRIVATE)
    }
}
