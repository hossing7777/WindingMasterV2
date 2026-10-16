package com.windingmaster.presentation.diagram

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windingmaster.domain.model.PhaseCode
import com.windingmaster.domain.model.SlotAssignment
import com.windingmaster.domain.model.WindingResult
import com.windingmaster.presentation.components.DiagramLegend
import com.windingmaster.presentation.components.ForceLtr
import com.windingmaster.presentation.components.PhaseStyle
import com.windingmaster.presentation.components.onPhaseColor
import com.windingmaster.presentation.components.rememberPhaseStyle
import kotlin.math.abs
import kotlin.math.min

private val MinSlotWidth = 26.dp
private val MaxSlotWidth = 46.dp

// رنگ‌های هایلایت
private val SelectedColor = Color(0xFFD32F2F)
private val PairedColor   = Color(0xFFE040FB)
private val WireColor     = Color(0xFF00C853)

// رنگ‌های سیم برای هر گروه (متفاوت از رنگ شیار)
private val MainWireColor = Color(0xFF0D47A1)  // آبی تیره
private val AuxWireColor  = Color(0xFFE65100)  // نارنجی تیره

@Composable
fun ExtendedDiagram(
    result: WindingResult,
    modifier: Modifier = Modifier,
    selectedSlot: Int? = null,
    onSlotClick: ((Int) -> Unit)? = null,
    showLegend: Boolean = true,
    showMain: Boolean = true,
    showAux: Boolean = true
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

    val selAssign = selectedSlot?.let { bySlot[it] }
    val pairedSel = selAssign?.pairedSlot
    // نمایش انتخابی به ترتیب صعودی (کوچک‌تر اول)
    val orderedSel = if (selectedSlot != null && pairedSel != null && pairedSel < selectedSlot) pairedSel else selectedSlot
    val orderedPair = if (selectedSlot != null && pairedSel != null && pairedSel < selectedSlot) selectedSlot else pairedSel

    BoxWithConstraints(modifier.fillMaxWidth()) {
        val availableWidth: Dp = maxWidth - 8.dp
        val idealWidth: Dp = availableWidth / result.z
        val slotWidth: Dp = idealWidth.coerceIn(MinSlotWidth, MaxSlotWidth)
        val needsScroll = slotWidth * result.z > availableWidth

        LaunchedEffect(selectedSlot, pairedSel, viewportPx, needsScroll) {
            if (needsScroll && selectedSlot != null && viewportPx > 0) {
                val w = with(density) { slotWidth.toPx() }
                val mid = ((selectedSlot + (pairedSel ?: selectedSlot)) / 2f)
                scroll.animateScrollTo((mid * w - viewportPx / 2f).toInt().coerceAtLeast(0))
            }
        }

        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .onSizeChanged { viewportPx = it.width }
                    .then(if (needsScroll) Modifier.horizontalScroll(scroll) else Modifier)
            ) {
                Canvas(
                    Modifier
                        .width(slotWidth * result.z)
                        .height(276.dp)
                        .clipToBounds()
                        .pointerInput(result.z, slotWidth, showMain, showAux) {
                            detectTapGestures(onTap = { p ->
                                clickHandler?.let { cb ->
                                    val w = slotWidth.toPx()
                                    cb((p.x / w).toInt().coerceIn(0, result.z - 1) + 1)
                                }
                            })
                        }
                ) {
                    drawExtended(
                        result, bySlot, style, measurer, starts,
                        selectedSlot, pairedSel, ink, soft,
                        slotWidth.toPx(), showMain, showAux
                    )
                }
            }
            if (orderedSel != null && orderedPair != null) {
                SlotRelationCard(
                    orderedSel, orderedPair, bySlot, style,
                    isMainPair = (bySlot[orderedSel]?.phase == PhaseCode.MAIN) ||
                                (bySlot[orderedSel]?.phase == 1)
                )
            }
            if (showLegend) DiagramLegend(style)
        }
    }
}

@Composable
private fun SlotRelationCard(
    selected: Int,
    paired: Int,
    bySlot: Map<Int, SlotAssignment>,
    style: PhaseStyle,
    isMainPair: Boolean
) {
    val span = abs(paired - selected)
    val selPhase = bySlot[selected]?.let { style.label(it.phase) } ?: "?"
    val pairedPhase = bySlot[paired]?.let { style.label(it.phase) } ?: "?"
    val wireColor = if (isMainPair) MainWireColor else AuxWireColor
    val wireLabel = if (isMainPair) "سیم اصلی" else "سیم فرعی"

    Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("شیار $selected", color = SelectedColor, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium)
                Text("  ←→  ", style = MaterialTheme.typography.titleMedium)
                Text("شیار $paired", color = PairedColor, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium)
            }
            Text("گام کویل: $span     |     فازها: $selPhase → $pairedPhase",
                style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
            Row(Modifier.padding(top = 6.dp)) {
                LegendDot(SelectedColor, "انتخاب‌شده")
                LegendDot(PairedColor, "مقابل", Modifier.padding(start = 12.dp))
                LegendDot(wireColor, wireLabel, Modifier.padding(start = 12.dp))
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.width(14.dp).height(14.dp)) {
            drawRoundRect(color, size = size, cornerRadius = CornerRadius(3f))
        }
        Text(label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 4.dp))
    }
}

private fun DrawScope.coilArc(
    x1: Float, x2: Float, baseY: Float, peak: Float,
    color: Color, width: Float, dashed: Boolean
) {
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
    soft: Color,
    slotW: Float,
    showMain: Boolean,
    showAux: Boolean
) {
    val z = result.z
    val w = slotW
    val rectH = 72.dp.toPx()
    val baseY = 150.dp.toPx()
    val total = z * w

    if (selected != null) {
        drawRect(SelectedColor.copy(alpha = 0.10f), Offset((selected - 1) * w, 0f), Size(w, size.height))
    }
    if (pairedSel != null) {
        drawRect(PairedColor.copy(alpha = 0.10f), Offset((pairedSel - 1) * w, 0f), Size(w, size.height))
    }
    drawRect(soft, Offset(0f, baseY - 4.dp.toPx()), Size(total, rectH + 8.dp.toPx()))

    // ============ قوس‌های کویل (با رفع باگ Aux) ============
    val strokeW = 2.dp.toPx()
    val maxPeak = 135.dp.toPx()

    for (a in result.slotAssignments) {
        val isMainGroup = a.phase == PhaseCode.MAIN || a.phase == 1 && result.phases == 1
        val isAuxGroup = a.phase == PhaseCode.AUX

        // فیلتر گروه‌های مخفی
        if (isMainGroup && !showMain) continue
        if (isAuxGroup && !showAux) continue

        // فقط یک بار هر جفت را رسم کن
        if (a.pairedSlot <= a.slotNumber) continue

        val span = ((a.pairedSlot - a.slotNumber) % z + z) % z
        if (span == 0) continue
        val x1 = (a.slotNumber - 0.5f) * w
        val x2 = x1 + span * w
        val peak = min(span * w * 0.5f, maxPeak)

        val isSelectedArc = selected != null && a.slotNumber == selected
        val isPairedArc = pairedSel != null && a.slotNumber == pairedSel
        val hot = isSelectedArc || isPairedArc

        // رنگ: اگر انتخاب شده → سبز روشن، وگرنه رنگ سیم بر اساس گروه
        val color = when {
            isSelectedArc -> WireColor
            isPairedArc   -> WireColor.copy(alpha = 0.5f)
            isAuxGroup    -> AuxWireColor.copy(alpha = 0.85f)
            else          -> MainWireColor.copy(alpha = 0.85f)
        }
        val sw = if (hot) strokeW * 2.4f else strokeW
        coilArc(x1, x2, baseY - 2.dp.toPx(), peak, color, sw, dashed = false)
        if (x2 > total) coilArc(x1 - total, x2 - total, baseY - 2.dp.toPx(), peak, color, sw, dashed = true)
    }

    val letterStyle = { c: Color -> TextStyle(color = c, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
    val numberStyle = { c: Color -> TextStyle(color = c, fontSize = 11.sp, fontWeight = FontWeight.Bold) }

    for (i in 1..z) {
        val a = bySlot[i] ?: continue
        val isMainGroup = a.phase == PhaseCode.MAIN || (a.phase == 1 && result.phases == 1)
        val isAuxGroup = a.phase == PhaseCode.AUX
        if (isMainGroup && !showMain) continue
        if (isAuxGroup && !showAux) continue

        val cx = (i - 0.5f) * w
        val color = style.color(a.phase)
        val onColor = onPhaseColor(color)
        val rw = w * 0.82f
        val topLeft = Offset(cx - rw / 2f, baseY)

        when (i) {
            selected -> {
                val pad = 4.dp.toPx()
                drawRoundRect(SelectedColor, Offset(topLeft.x - pad, baseY - pad),
                    Size(rw + 2 * pad, rectH + 2 * pad), CornerRadius(8.dp.toPx()))
            }
            pairedSel -> {
                val pad = 4.dp.toPx()
                drawRoundRect(PairedColor, Offset(topLeft.x - pad, baseY - pad),
                    Size(rw + 2 * pad, rectH + 2 * pad), CornerRadius(8.dp.toPx()))
            }
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

        if (w > 30f) {
            drawCentered(measurer, style.label(a.phase), Offset(cx, baseY + rectH + 14.dp.toPx()), letterStyle(color))
        }

        starts[i]?.let { idx ->
            val pillColor = style.colors.getOrElse(idx) { color }
            val pillW = 30.dp.toPx()
            val pillH = 18.dp.toPx()
            val py = baseY + rectH + (if (w > 30f) 40.dp.toPx() else 20.dp.toPx())
            drawRoundRect(pillColor, Offset(cx - pillW / 2f, py - pillH / 2f), Size(pillW, pillH),
                CornerRadius(9.dp.toPx()))
            drawCentered(measurer, style.label(idx + 1) + "1", Offset(cx, py), numberStyle(onPhaseColor(pillColor)))
        }
    }
}
