package com.windingmaster.presentation.diagram

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windingmaster.domain.model.SlotAssignment
import com.windingmaster.domain.model.WindingResult
import com.windingmaster.presentation.components.DiagramLegend
import com.windingmaster.presentation.components.ForceLtr
import com.windingmaster.presentation.components.PhaseColors
import com.windingmaster.presentation.components.PhaseStyle
import com.windingmaster.presentation.components.onPhaseColor
import com.windingmaster.presentation.components.rememberPhaseStyle
import kotlin.math.min

private val SlotWidth = 44.dp

@Composable
fun ExtendedDiagram(
    result: WindingResult,
    modifier: Modifier = Modifier,
    selectedSlot: Int? = null,
    onSlotClick: ((Int) -> Unit)? = null,
    showLegend: Boolean = true
) {
    val style = rememberPhaseStyle(result.phases)
    val measurer = rememberTextMeasurer(cacheSize = 256)
    val bySlot = remember(result) { result.slotAssignments.associateBy { it.slotNumber } }
    val starts = remember(result) { result.phaseStarts.withIndex().associate { (idx, slot) -> slot to idx } }
    val clickHandler by rememberUpdatedState(onSlotClick)
    val soft = MaterialTheme.colorScheme.surfaceVariant
    val ink = MaterialTheme.colorScheme.onSurface
    val scroll = rememberScrollState()
    val density = LocalDensity.current
    var viewportPx by remember { mutableIntStateOf(0) }
    val pairedSel = selectedSlot?.let { bySlot[it]?.pairedSlot }

    LaunchedEffect(selectedSlot, viewportPx) {
        if (selectedSlot != null && viewportPx > 0) {
            val w = with(density) { SlotWidth.toPx() }
            scroll.animateScrollTo(((selectedSlot - 0.5f) * w - viewportPx / 2f).toInt().coerceAtLeast(0))
        }
    }

    ForceLtr {
        Column(modifier) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .onSizeChanged { viewportPx = it.width }
                    .horizontalScroll(scroll)
            ) {
                Canvas(
                    Modifier
                        .width(SlotWidth * result.z)
                        .height(276.dp)
                        .clipToBounds()
                        .pointerInput(result.z) {
                            detectTapGestures(onTap = { p ->
                                clickHandler?.let { cb ->
                                    val w = SlotWidth.toPx()
                                    cb((p.x / w).toInt().coerceIn(0, result.z - 1) + 1)
                                }
                            })
                        }
                ) {
                    drawExtended(result, bySlot, style, measurer, starts, selectedSlot, pairedSel, ink, soft)
                }
            }
            if (showLegend) DiagramLegend(style)
        }
    }
}

private fun DrawScope.coilArc(x1: Float, x2: Float, baseY: Float, peak: Float, color: Color, width: Float, dashed: Boolean) {
    val path = Path().apply {
        moveTo(x1, baseY)
        quadraticBezierTo((x1 + x2) / 2f, baseY - 2f * peak, x2, baseY)
    }
    drawPath(
        path, color,
        style = Stroke(
            width = width,
            pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(12f, 9f)) else null
        )
    )
}

private fun DrawScope.drawExtended(
    result: WindingResult,
    bySlot: Map<Int, SlotAssignment>,
    style: PhaseStyle,
    measurer: TextMeasurer,
    starts: Map<Int, Int>,
    selected: Int?,
    pairedSel: Int?,
    ink: Color,
    soft: Color
) {
    val z = result.z
    val w = SlotWidth.toPx()
    val rectH = 72.dp.toPx()
    val baseY = 150.dp.toPx()
    val total = z * w

    if (selected != null) {
        drawRect(PhaseColors.Highlight.copy(alpha = 0.10f), Offset((selected - 1) * w, 0f), Size(w, size.height))
    }
    drawRect(soft, Offset(0f, baseY - 4.dp.toPx()), Size(total, rectH + 8.dp.toPx()))

    val strokeW = 2.dp.toPx()
    val maxPeak = 135.dp.toPx()
    for (a in result.slotAssignments) {
        if (a.direction != 1) continue
        val span = ((a.pairedSlot - a.slotNumber) % z + z) % z
        if (span == 0) continue
        val x1 = (a.slotNumber - 0.5f) * w
        val x2 = x1 + span * w
        val peak = min(span * w * 0.5f, maxPeak)
        val hot = a.slotNumber == selected || a.pairedSlot == selected
        val color = if (hot) PhaseColors.Highlight else style.color(a.phase).copy(alpha = 0.9f)
        val sw = if (hot) strokeW * 1.8f else strokeW
        coilArc(x1, x2, baseY - 2.dp.toPx(), peak, color, sw, dashed = false)
        if (x2 > total) coilArc(x1 - total, x2 - total, baseY - 2.dp.toPx(), peak, color, sw, dashed = true)
    }

    val letterStyle = { c: Color -> TextStyle(color = c, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
    val numberStyle = { c: Color -> TextStyle(color = c, fontSize = 12.sp, fontWeight = FontWeight.Bold) }

    for (i in 1..z) {
        val a = bySlot[i] ?: continue
        val cx = (i - 0.5f) * w
        val color = style.color(a.phase)
        val onColor = onPhaseColor(color)
        val rw = w * 0.78f
        val topLeft = Offset(cx - rw / 2f, baseY)

        if (i == selected || i == pairedSel) {
            val pad = 3.dp.toPx()
            drawRoundRect(
                PhaseColors.Highlight.copy(alpha = if (i == selected) 1f else 0.55f),
                Offset(topLeft.x - pad, baseY - pad), Size(rw + 2 * pad, rectH + 2 * pad),
                CornerRadius(8.dp.toPx())
            )
        }
        drawRoundRect(color, topLeft, Size(rw, rectH), CornerRadius(6.dp.toPx()))

        drawCentered(measurer, i.toString(), Offset(cx, baseY + 12.dp.toPx()), numberStyle(onColor))

        val top = baseY + 28.dp.toPx()
        val bottom = baseY + rectH - 8.dp.toPx()
        val up = a.direction == 1
        val head = if (up) top else bottom
        val tail = if (up) bottom else top
        val wing = 6.dp.toPx()
        val back = if (up) wing else -wing
        val sw = 2.dp.toPx()
        drawLine(onColor, Offset(cx, tail), Offset(cx, head), strokeWidth = sw)
        drawLine(onColor, Offset(cx, head), Offset(cx - wing, head + back), strokeWidth = sw)
        drawLine(onColor, Offset(cx, head), Offset(cx + wing, head + back), strokeWidth = sw)

        drawCentered(measurer, style.label(a.phase), Offset(cx, baseY + rectH + 14.dp.toPx()), letterStyle(color))

        starts[i]?.let { idx ->
            val pillColor = style.colors.getOrElse(idx) { color }
            val pillW = 34.dp.toPx()
            val pillH = 20.dp.toPx()
            val py = baseY + rectH + 40.dp.toPx()
            drawRoundRect(
                pillColor, Offset(cx - pillW / 2f, py - pillH / 2f), Size(pillW, pillH),
                CornerRadius(10.dp.toPx())
            )
            drawCentered(measurer, style.label(idx + 1) + "1", Offset(cx, py), numberStyle(onPhaseColor(pillColor)))
        }
    }
}
