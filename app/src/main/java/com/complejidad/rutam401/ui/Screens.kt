package com.complejidad.rutam401.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.complejidad.rutam401.BuildConfig
import com.complejidad.rutam401.GameViewModel
import com.complejidad.rutam401.R
import com.complejidad.rutam401.domain.CampusData
import com.complejidad.rutam401.domain.GameState
import com.complejidad.rutam401.domain.Phase
import com.complejidad.rutam401.domain.Place

enum class NfcStatus { READY, DISABLED, UNAVAILABLE }

@Composable
fun AppRoot(viewModel: GameViewModel, nfcStatus: NfcStatus, onOpenNfcSettings: () -> Unit) {
    RutaTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = AppColors.Background) {
            if (viewModel.configOpen) {
                BackHandler { viewModel.closeConfig() }
                ConfigScreen(viewModel, nfcStatus, onOpenNfcSettings)
            } else {
                when (viewModel.state.phase) {
                    Phase.HOME -> HomeScreen(viewModel)
                    Phase.TRAVEL -> TravelScreen(viewModel, nfcStatus, onOpenNfcSettings)
                    Phase.QUESTION -> QuestionScreen(viewModel)
                    Phase.RESULT -> ResultScreen(viewModel)
                    Phase.FINISHED -> FinishScreen(viewModel)
                }
            }
        }
    }
}

@Composable
private fun ScreenColumn(
    background: Color = AppColors.Background,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .safeDrawingPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    container: Color = AppColors.Accent,
    contentColor: Color = AppColors.OnPrimary,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = contentColor),
    ) {
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
private fun WarningBox(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(AppColors.WarningBackground)
            .padding(14.dp),
        color = AppColors.WarningText,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun NfcNotice(nfcStatus: NfcStatus, onOpenNfcSettings: () -> Unit) {
    when (nfcStatus) {
        NfcStatus.READY -> Unit
        NfcStatus.UNAVAILABLE -> WarningBox("Este dispositivo no tiene NFC.")
        NfcStatus.DISABLED -> {
            WarningBox("El NFC está apagado. Actívalo para escanear los tags.")
            OutlinedButton(
                onClick = onOpenNfcSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
            ) {
                Text("Abrir ajustes de NFC", color = AppColors.Text)
            }
        }
    }
}

@Composable
private fun HomeScreen(viewModel: GameViewModel) {
    ScreenColumn(background = AppColors.Primary) {
        Spacer(Modifier.height(24.dp))
        Image(
            painter = painterResource(R.drawable.logo_universidad),
            contentDescription = "Logo de la Universidad Católica de Oriente",
            modifier = Modifier
                .width(220.dp)
                .height(88.dp),
        )
        Text("Carrera de observación NFC", color = AppColors.PrimaryMuted, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Text(stringResource(R.string.app_full_name), color = AppColors.OnPrimary, fontSize = 38.sp, lineHeight = 44.sp, fontWeight = FontWeight.Bold)
        Text(
            "Escanea cada tag, responde y descubre tu siguiente destino.",
            color = AppColors.OnPrimary,
            fontSize = 22.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Medium,
        )
        Text(
            "La primera pregunta te espera aquí en M401. Tienes un solo intento por pregunta " +
                "y siempre terminarás de vuelta en M401.",
            color = AppColors.PrimaryMuted,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        )
        Text("Lugares del recorrido", color = AppColors.OnPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        viewModel.homeOrder.forEach { place ->
            Text("• ${place.displayName}", color = AppColors.OnPrimary, fontSize = 15.sp)
        }
        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            text = "Empezar en M401",
            onClick = viewModel::start,
            container = AppColors.Gold,
            contentColor = AppColors.Text,
        )
        TextButton(
            onClick = viewModel::openConfig,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Text("Configurar tags", color = AppColors.OnPrimary)
        }
    }
}

@Composable
private fun TravelScreen(viewModel: GameViewModel, nfcStatus: NfcStatus, onOpenNfcSettings: () -> Unit) {
    val state = viewModel.state
    val isReturning = state.target == CampusData.home
    var confirmRestart by remember { mutableStateOf(false) }

    ScreenColumn {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.app_name), modifier = Modifier.weight(1f), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.Text)
            Spacer(Modifier.width(12.dp))
            Text("${state.visited.size} de ${CampusData.checkpoints.size} puntos", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextMuted)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AppColors.Primary)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Siguiente destino", color = AppColors.PrimaryMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(state.target.displayName, color = AppColors.OnPrimary, fontSize = 30.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold)
            Text("${state.legMeters} m", color = AppColors.PrimaryMuted, fontSize = 15.sp)
            Text(CampusData.clues.getValue(state.target), color = AppColors.OnPrimary, fontSize = 15.sp, lineHeight = 22.sp)
        }

        RouteProgress(state)

        viewModel.scanMessage?.let { WarningBox(it) }

        if (isReturning) {
            PrimaryButton("Confirmar llegada a M401", viewModel::arrive)
        } else {
            NfcNotice(nfcStatus, onOpenNfcSettings)
            if (nfcStatus == NfcStatus.READY) {
                Text(
                    "Al llegar, acerca tu teléfono al tag del cartel.",
                    modifier = Modifier.fillMaxWidth(),
                    color = AppColors.Text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
            }
            if (BuildConfig.DEBUG && nfcStatus == NfcStatus.UNAVAILABLE) {
                PrimaryButton("Simular lectura del tag", viewModel::simulateScan)
            }
        }

        TextButton(
            onClick = { confirmRestart = true },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Text("Reiniciar recorrido", color = AppColors.TextMuted)
        }
    }

    if (confirmRestart) {
        AlertDialog(
            onDismissRequest = { confirmRestart = false },
            title = { Text("¿Reiniciar el recorrido?") },
            text = { Text("Perderás tu progreso y volverás a la pantalla de inicio.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmRestart = false
                    viewModel.restart()
                }) {
                    Text("Reiniciar")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmRestart = false }) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@Composable
private fun RouteProgress(state: GameState) {
    val isReturning = state.target == CampusData.home
    val pending = state.placeOrder.filter { it != state.target && it !in state.visited }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ProgressRow(CampusData.home.displayName, ProgressStatus.DONE)
        state.visited.forEach { ProgressRow(it.displayName, ProgressStatus.DONE) }
        if (!isReturning) ProgressRow(state.target.displayName, ProgressStatus.CURRENT)
        pending.forEach { ProgressRow(it.displayName, ProgressStatus.PENDING) }
        ProgressRow(
            CampusData.home.displayName,
            if (isReturning) ProgressStatus.CURRENT else ProgressStatus.PENDING,
        )
    }
}

private enum class ProgressStatus(val label: String) {
    DONE("Hecho"),
    CURRENT("Ahora"),
    PENDING("Pendiente"),
}

@Composable
private fun ProgressRow(name: String, status: ProgressStatus) {
    val color = when (status) {
        ProgressStatus.DONE -> AppColors.Text
        ProgressStatus.CURRENT -> AppColors.Accent
        ProgressStatus.PENDING -> AppColors.TextMuted
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(if (status == ProgressStatus.PENDING) AppColors.Border else color),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            name,
            modifier = Modifier.weight(1f),
            color = color,
            fontSize = 15.sp,
            fontWeight = if (status == ProgressStatus.CURRENT) FontWeight.Bold else FontWeight.Medium,
        )
        Text(status.label, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun QuestionScreen(viewModel: GameViewModel) {
    val state = viewModel.state
    val question = CampusData.questions.getValue(state.target)

    ScreenColumn {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(state.target.displayName, modifier = Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.Text)
            Text("Pregunta ${viewModel.questionNumber} de ${viewModel.questionCount}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextMuted)
        }
        Text(question.text, color = AppColors.Text, fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold)
        Text("Tienes un solo intento.", color = AppColors.TextMuted, fontSize = 15.sp)

        question.options.forEachIndexed { index, option ->
            OutlinedButton(
                onClick = { viewModel.answer(index == question.correctIndex) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 68.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(2.dp, AppColors.Border),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = AppColors.Surface,
                    contentColor = AppColors.Text,
                ),
            ) {
                Text(
                    "ABCD"[index].toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Accent,
                )
                Spacer(Modifier.width(14.dp))
                Text(option, modifier = Modifier.weight(1f), fontSize = 16.sp, lineHeight = 22.sp)
            }
        }
    }
}

@Composable
private fun ResultScreen(viewModel: GameViewModel) {
    val state = viewModel.state

    // Same screen for every answer: it never says whether the answer was right or the route is the best.
    ScreenColumn(background = AppColors.Primary) {
        Spacer(Modifier.height(32.dp))
        Text(
            "Respuesta registrada",
            color = AppColors.OnPrimary,
            fontSize = 40.sp,
            lineHeight = 44.sp,
            fontWeight = FontWeight.Bold,
        )
        Text("Tu siguiente destino ya está listo.", color = AppColors.PrimaryMuted, fontSize = 18.sp)
        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AppColors.Surface)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("Siguiente destino", color = AppColors.TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(state.target.displayName, color = AppColors.Text, fontSize = 26.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold)
            Text("${state.legMeters} m", color = AppColors.TextMuted, fontSize = 15.sp)
        }
        PrimaryButton(
            text = "Ver mi pista",
            onClick = viewModel::continueRoute,
            container = AppColors.OnPrimary,
            contentColor = AppColors.Text,
        )
    }
}

@Composable
private fun FinishScreen(viewModel: GameViewModel) {
    val state = viewModel.state
    val route: List<Place> = listOf(CampusData.home) + state.visited + CampusData.home

    ScreenColumn(background = AppColors.Primary) {
        Text("De vuelta en M401", color = AppColors.PrimaryMuted, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Text("Recorrido completo", color = AppColors.OnPrimary, fontSize = 40.sp, lineHeight = 44.sp, fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("${state.correctCount}/${viewModel.questionCount}", "Aciertos", Modifier.weight(1f))
            StatCard(formatDuration(state.finishedAtMillis - state.startedAtMillis), "Tiempo", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("${state.walkedMeters} m", "Tu distancia", Modifier.weight(1f))
            StatCard("${viewModel.optimalMeters} m", "Ruta óptima", Modifier.weight(1f))
        }

        Text("Tu ruta", color = AppColors.OnPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        route.forEachIndexed { index, place ->
            Text("${index + 1}. ${place.displayName}", color = AppColors.OnPrimary, fontSize = 15.sp)
        }

        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            text = "Volver al inicio",
            onClick = viewModel::restart,
            container = AppColors.Gold,
            contentColor = AppColors.Text,
        )
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.PrimarySurface)
            .padding(14.dp),
    ) {
        Text(value, color = AppColors.OnPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(label, color = AppColors.PrimaryMuted, fontSize = 13.sp)
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = millis.coerceAtLeast(0) / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

@Composable
private fun ConfigScreen(viewModel: GameViewModel, nfcStatus: NfcStatus, onOpenNfcSettings: () -> Unit) {
    ScreenColumn {
        Text("Configurar tags", color = AppColors.Text, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text(
            "Toca un punto y acerca el teléfono a su tag para registrarlo.",
            color = AppColors.TextMuted,
            fontSize = 16.sp,
            lineHeight = 22.sp,
        )
        NfcNotice(nfcStatus, onOpenNfcSettings)

        CampusData.checkpoints.forEach { place ->
            val selected = viewModel.placeBeingAssigned == place
            val detail = when {
                selected -> "Acerca el tag ahora"
                place in viewModel.tags -> "Tag registrado"
                else -> "Sin tag"
            }
            OutlinedButton(
                onClick = { viewModel.selectPlaceToAssign(place) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(2.dp, if (selected) AppColors.Accent else AppColors.Border),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = AppColors.Surface,
                    contentColor = AppColors.Text,
                ),
            ) {
                Text(place.displayName, modifier = Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(detail, fontSize = 13.sp, color = if (selected) AppColors.Accent else AppColors.TextMuted)
            }
        }

        PrimaryButton("Listo", viewModel::closeConfig)
    }
}
