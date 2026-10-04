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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

data class FerryUiState(
    val tab: FerryTab = FerryTab.TASKS,
    val operations: List<OperationEntity> = emptyList(),
    val selectedOperationId: String? = null,
    val configuration: UiConfigurationSnapshot = UiConfigurationSnapshot(),
)

class FerryUiController(
    private val repository: OperationRepository,
    private val coordinator: ForegroundExecutionCoordinator,
) {
    private val mutableState = MutableStateFlow(FerryUiState())
    val state: StateFlow<FerryUiState> = mutableState.asStateFlow()

    suspend fun onOpen() {
        coordinator.onOpen()
        reload()
    }

    suspend fun reload() {
        mutableState.value = mutableState.value.copy(operations = repository.allOperations())
    }

    fun selectTab(tab: FerryTab) {
        mutableState.value = mutableState.value.copy(tab = tab, selectedOperationId = null)
    }

    fun selectOperation(operationId: String) {
        mutableState.value = mutableState.value.copy(selectedOperationId = operationId)
    }

    fun closeOperation() {
        mutableState.value = mutableState.value.copy(selectedOperationId = null)
    }

    suspend fun pause(operationId: String) {
        coordinator.pause(operationId)
        reload()
    }
}

@Composable
fun FerryApp(controller: FerryUiController) {
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
                    FerryTab.SOURCES -> ConfigurationScreen("来源", state.configuration.sourceLabel, "系统目录授权待接入")
                    FerryTab.TARGETS -> ConfigurationScreen("目标", state.configuration.targetLabel, "SMB 配置接口待接入")
                    FerryTab.RULES -> ConfigurationScreen("规则", state.configuration.rulesLabel, "规则编辑接口待接入")
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
    if (state.operations.isEmpty()) {
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
            OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) { Text("恢复任务（待接入）") }
        }
        if (operation.phase == "failed") {
            OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) { Text("重新上传（待接入）") }
        }
        Spacer(Modifier.height(20.dp))
        Text("Ferry 不会删除相机原文件", color = FerryMuted)
    }
}

@Composable
private fun ConfigurationScreen(title: String, value: String, unavailable: String) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        ScreenTitle("Ferry / $title")
        Spacer(Modifier.height(24.dp))
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(unavailable, color = FerryMuted, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(28.dp))
        HorizontalDivider()
        Spacer(Modifier.height(24.dp))
        InfoRow("ⓘ", "此页面只显示已配置状态；配置接口尚未接入")
        Spacer(Modifier.height(24.dp))
        Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) { Text("配置（待接入）") }
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
    operation.phase == "uploading" || operation.phase == "verifying" -> "内容校验中 · 不填造进度"
    operation.phase == "waiting" -> "等待前台运行 · 手机完整副本已就绪"
    else -> "导入中 · 手机完整副本待就绪"
}

internal fun isVerificationFailure(error: String?): Boolean {
    val value = error.orEmpty().lowercase()
    return listOf("readback", "sha-256", "sha256", "mismatch", "校验").any(value::contains)
}

private fun phaseColor(operation: OperationEntity): Color = when {
    operation.phase == "failed" -> FerryError
    operation.phase == "waiting" -> FerryWarning
    operation.phase == "completed" -> FerryTeal
    else -> FerryInk
}
