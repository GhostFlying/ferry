package io.github.ghostflying.ferry.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ghostflying.ferry.data.OperationEntity
import io.github.ghostflying.ferry.data.OperationRepository
import io.github.ghostflying.ferry.lifecycle.ForegroundExecutionCoordinator
import io.github.ghostflying.ferry.source.SourceStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val FerryTeal = Color(0xFF008F87)
private val FerryInk = Color(0xFF172236)
private val FerryMuted = Color(0xFF5B6A80)
private val FerryWarning = Color(0xFFE58A00)
private val FerryError = Color(0xFFC6283A)

enum class FerryTab(val title: String, val glyph: String) {
    TASKS("任务", "▤"),
    SOURCES("来源", "□"),
    TARGETS("目标", "▣"),
    RULES("规则", "⚙"),
}

data class UiConfigurationSnapshot(
    val sourceLabel: String = "尚未配置",
    val targetLabel: String = "尚未配置",
    val rulesLabel: String = "尚未配置",
)

internal data class ConfigurationScreenModel(
    val title: String,
    val value: String,
    val unavailable: String,
)

internal fun configurationScreenModel(
    tab: FerryTab,
    snapshot: UiConfigurationSnapshot = UiConfigurationSnapshot(),
): ConfigurationScreenModel = when (tab) {
    FerryTab.SOURCES -> error("sources use the source screen")
    FerryTab.TARGETS -> ConfigurationScreenModel("目标", snapshot.targetLabel, "SMB 配置接口待接入")
    FerryTab.RULES -> ConfigurationScreenModel("规则", snapshot.rulesLabel, "规则编辑接口待接入")
    FerryTab.TASKS -> error("tasks do not have a configuration screen")
}

internal data class SourceWarning(val title: String, val body: String)

internal data class SourceScreenModel(
    val label: String,
    val status: String,
    val warning: SourceWarning?,
    val actionLabel: String,
)

private val ReselectWarning = SourceWarning("目录访问权限已失效，请重新选择", "新文件的导入需要访问所选目录，请重新选择以继续。")
private val PocketOtgWarning = SourceWarning("检测到 Pocket 3，但未以 OTG 方式连接", "请在相机下拉菜单「设置 → OTG 连接」后重新连接数据线。")

/**
 * Only states shown in accepted concepts are visible: the selected directory,
 * the A03 v2 expired-grant warning and the A12 Pocket OTG hint. Scan and
 * import progress is logged until its own concept is accepted.
 */
internal fun sourceScreenModel(label: String?, status: SourceStatus): SourceScreenModel {
    if (label == null) return SourceScreenModel("尚未配置", "", warning = null, actionLabel = "选择来源")
    return when (status) {
        SourceStatus.NeedsReselect -> SourceScreenModel(label, "等待授权", ReselectWarning, "重新选择目录")
        SourceStatus.PocketNeedsOtg -> SourceScreenModel(label, "等待 OTG 连接", PocketOtgWarning, "重新选择目录")
        else -> SourceScreenModel(label, "", warning = null, actionLabel = "重新选择目录")
    }
}

data class FerryUiState(
    val tab: FerryTab = FerryTab.TASKS,
    val operations: List<OperationEntity> = emptyList(),
    val selectedOperationId: String? = null,
    val configuration: UiConfigurationSnapshot = UiConfigurationSnapshot(),
    val sourceLabel: String? = null,
    val sourceStatus: SourceStatus = SourceStatus.NotConfigured,
) {
    val showsFirstSetup: Boolean get() = operations.isEmpty()
}

class FerryUiController(
    private val repository: OperationRepository,
    private val coordinator: ForegroundExecutionCoordinator,
) {
    private val mutableState = MutableStateFlow(FerryUiState())
    private val lifecycleMutex = Mutex()
    val state: StateFlow<FerryUiState> = mutableState.asStateFlow()

    suspend fun onStart() = lifecycleMutex.withLock {
        coordinator.onOpen()
        reloadSnapshot()
    }

    suspend fun onStop() = lifecycleMutex.withLock {
        coordinator.onStop()
        reloadSnapshot()
    }

    suspend fun restart() = lifecycleMutex.withLock {
        coordinator.onStop()
        coordinator.onOpen()
        reloadSnapshot()
    }

    suspend fun shutdown() = lifecycleMutex.withLock {
        coordinator.onStop()
    }

    suspend fun reload() = lifecycleMutex.withLock { reloadSnapshot() }

    private suspend fun reloadSnapshot() {
        val operations = repository.allOperations()
        mutableState.update { it.copy(operations = operations) }
    }

    fun updateSource(label: String?, status: SourceStatus) {
        mutableState.update { it.copy(sourceLabel = label, sourceStatus = status) }
    }

    fun selectTab(tab: FerryTab) {
        mutableState.update { it.copy(tab = tab, selectedOperationId = null) }
    }

    fun selectOperation(operationId: String) {
        mutableState.update { it.copy(selectedOperationId = operationId) }
    }

    fun closeOperation() {
        mutableState.update { it.copy(selectedOperationId = null) }
    }

    suspend fun pause(operationId: String) = lifecycleMutex.withLock {
        coordinator.pause(operationId)
        reloadSnapshot()
    }
}

internal object FerryUnsupportedActions {
    const val canResume = false
    const val canRetry = false
    const val canRecheckSpace = false
    const val canSaveConfiguration = false
}

@Composable
fun FerryApp(controller: FerryUiController, onPickSource: () -> Unit = {}) {
    val state by controller.state.collectAsState()
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = FerryTeal,
            onPrimary = Color.White,
            onSurface = FerryInk,
            onSurfaceVariant = FerryMuted,
            error = FerryError,
        ),
    ) {
        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    FerryTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = state.tab == tab,
                            onClick = { controller.selectTab(tab) },
                            icon = { Text(tab.glyph, fontSize = 24.sp) },
                            label = { Text(tab.title) },
                        )
                    }
                }
            },
        ) { padding ->
            Surface(modifier = Modifier.fillMaxSize().padding(padding), color = Color.White) {
                when (state.tab) {
                    FerryTab.TASKS -> TasksScreen(state, controller)
                    FerryTab.SOURCES -> SourceScreen(sourceScreenModel(state.sourceLabel, state.sourceStatus), onPickSource)
                    FerryTab.TARGETS -> ConfigurationScreen(configurationScreenModel(FerryTab.TARGETS, state.configuration))
                    FerryTab.RULES -> ConfigurationScreen(configurationScreenModel(FerryTab.RULES, state.configuration))
                }
            }
        }
    }
}

@Composable
private fun TasksScreen(state: FerryUiState, controller: FerryUiController) {
    val selected = state.selectedOperationId?.let { id -> state.operations.firstOrNull { it.id == id } }
    if (selected != null) {
        OperationDetail(selected, controller)
        return
    }
    if (state.showsFirstSetup) {
        EmptySetup(controller)
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenTitle("Ferry / 任务") }
        item { Text("任务列表", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        items(state.operations, key = { it.id }) { operation ->
            OperationRow(operation) { controller.selectOperation(operation.id) }
        }
    }
}

@Composable
private fun EmptySetup(controller: FerryUiController) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ScreenTitle("Ferry / 任务")
        Spacer(Modifier.height(56.dp))
        Text("还没有任务", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("完成配置后，打开 Ferry 即可开始", color = FerryMuted, modifier = Modifier.padding(top = 12.dp))
        Spacer(Modifier.height(36.dp))
        SetupStep("1", "选择来源", "系统目录授权 · 只读")
        SetupStep("2", "配置目标", "飞牛 SMB · 尚未配置")
        SetupStep("3", "设置规则", "预览匹配与目标路径")
        Spacer(Modifier.height(20.dp))
        Button(onClick = { controller.selectTab(FerryTab.SOURCES) }, modifier = Modifier.fillMaxWidth()) {
            Text("选择来源")
        }
        Spacer(Modifier.height(28.dp))
        HorizontalDivider()
        Spacer(Modifier.height(20.dp))
        Text("工作方式", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        InfoRow("▱", "先在手机保留完整副本")
        InfoRow("✓", "上传后读回校验，通过才完成")
        InfoRow("▷", "仅在前台运行，离开后保留任务")
        InfoRow("Ⅱ", "人工暂停不会自动恢复")
        InfoRow("▣", "相机原文件始终保留")
    }
}

@Composable
private fun SetupStep(number: String, title: String, detail: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(color = Color(0xFFE9EDF2), shape = MaterialTheme.shapes.extraLarge, modifier = Modifier.size(48.dp)) {
            Text(number, modifier = Modifier.padding(top = 11.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 20.sp, color = FerryMuted)
        }
        Spacer(Modifier.width(16.dp))
        Column { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text(detail, color = FerryMuted) }
    }
}

@Composable
private fun OperationRow(operation: OperationEntity, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("▱", fontSize = 28.sp, color = if (operation.phase == "failed") FerryError else FerryMuted)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(operation.sourcePath.substringAfterLast('/'), fontWeight = FontWeight.Bold)
                Text(operationLabel(operation), color = if (operation.phase == "failed") FerryError else FerryMuted)
            }
            Text("›", fontSize = 28.sp, color = FerryMuted)
        }
        HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun OperationDetail(operation: OperationEntity, controller: FerryUiController) {
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        TextButton(onClick = controller::closeOperation, contentPadding = PaddingValues(0.dp)) { Text("‹  返回", fontSize = 18.sp) }
        Text("任务详情", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(22.dp))
        Text(operation.sourcePath.substringAfterLast('/'), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(operationLabel(operation), color = phaseColor(operation), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(20.dp))
        InfoRow("▱", "手机完整副本\n${operation.privateCopy}")
        InfoRow("▣", "目标\n尚未配置")
        InfoRow("⌁", "网络\n待接入目标后显示")
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        Text("阶段记录", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        InfoRow(if (operation.sourceSha256.isNotEmpty()) "✓" else "○", "导入\n${if (operation.sourceSha256.isNotEmpty()) "完整副本已就绪" else "等待导入"}")
        InfoRow(if (operation.phase == "completed") "✓" else "○", "上传与读回校验\n${operationLabel(operation)}")
        if (operation.lastError != null) {
            Text(operation.lastError, color = FerryError, modifier = Modifier.padding(vertical = 12.dp))
        }
        Spacer(Modifier.height(20.dp))
        if (!operation.manualPaused && operation.phase in setOf("imported", "waiting", "uploading", "verifying")) {
            Button(onClick = { scope.launch { controller.pause(operation.id) } }, modifier = Modifier.fillMaxWidth()) {
                Text("Ⅱ  暂停任务")
            }
        } else if (operation.manualPaused) {
            OutlinedButton(onClick = {}, enabled = FerryUnsupportedActions.canResume, modifier = Modifier.fillMaxWidth()) { Text("恢复任务（待接入）") }
        }
        if (operation.phase == "failed") {
            OutlinedButton(onClick = {}, enabled = FerryUnsupportedActions.canRetry, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) { Text("重新上传（待接入）") }
        }
        Spacer(Modifier.height(20.dp))
        Text("Ferry 不会删除相机原文件", color = FerryMuted)
    }
}

@Composable
private fun ConfigurationScreen(model: ConfigurationScreenModel) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        ScreenTitle("Ferry / ${model.title}")
        Spacer(Modifier.height(24.dp))
        Text(model.value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(model.unavailable, color = FerryMuted, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(28.dp))
        HorizontalDivider()
        Spacer(Modifier.height(24.dp))
        InfoRow("ⓘ", "此页面只显示已配置状态；配置接口尚未接入")
        Spacer(Modifier.height(24.dp))
        Button(onClick = {}, enabled = FerryUnsupportedActions.canSaveConfiguration, modifier = Modifier.fillMaxWidth()) { Text("配置（待接入）") }
    }
}

@Composable
private fun SourceScreen(model: SourceScreenModel, onPickSource: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        ScreenTitle("Ferry / 来源")
        Spacer(Modifier.height(24.dp))
        Text(model.label, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (model.status.isNotEmpty()) Text(model.status, color = FerryMuted, modifier = Modifier.padding(top = 8.dp))
        HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))
        model.warning?.let { warning ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("!", fontSize = 28.sp, color = FerryError, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
                Text(warning.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Text(warning.body, color = FerryMuted, modifier = Modifier.padding(start = 40.dp, top = 8.dp))
        }
        InfoRow("□", "已选目录\n${model.label}")
        InfoRow("⚙", "访问方式\nAndroid 系统目录授权")
        InfoRow("⊡", "读取权限\n只读")
        Spacer(Modifier.height(20.dp))
        Button(onClick = onPickSource, modifier = Modifier.fillMaxWidth()) { Text(model.actionLabel) }
        Spacer(Modifier.height(20.dp))
        Text("Ferry 不写入、不改名、不删除来源文件。", color = FerryMuted)
    }
}

@Composable
private fun ScreenTitle(title: String) {
    Text(title, style = MaterialTheme.typography.headlineMedium, color = FerryInk, fontWeight = FontWeight.Normal)
}

@Composable
private fun InfoRow(glyph: String, text: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(glyph, fontSize = 24.sp, color = FerryMuted, modifier = Modifier.width(40.dp))
        Text(text, color = FerryMuted, lineHeight = 24.sp)
    }
}

internal fun operationLabel(operation: OperationEntity): String = when {
    operation.phase == "completed" -> "已完成 · 远端 SHA-256 已验证"
    operation.phase == "failed" && isVerificationFailure(operation.lastError) -> "校验失败 · 远端读回 SHA-256 不一致"
    operation.phase == "failed" -> "任务未完成 · ${operation.lastError.orEmpty()}"
    operation.manualPaused -> "用户暂停 · 打开 App 不会恢复"
    operation.phase == "uploading" -> "正在上传"
    operation.phase == "verifying" -> "内容校验中 · 正在读回远端内容"
    operation.phase == "waiting" -> "等待前台运行"
    operation.phase == "imported" && operation.sourceSha256.isNotEmpty() -> "待上传 · 手机完整副本已就绪"
    else -> "等待导入 · 手机完整副本待就绪"
}

internal fun isVerificationFailure(error: String?): Boolean {
    val value = error.orEmpty().lowercase()
    return value == "remote sha-256 did not match the private copy" ||
        (value.contains("readback") && (value.contains("mismatch") || value.contains("did not match")))
}

private fun phaseColor(operation: OperationEntity): Color = when {
    operation.phase == "failed" -> FerryError
    operation.phase == "waiting" -> FerryWarning
    operation.phase == "completed" -> FerryTeal
    else -> FerryInk
}
