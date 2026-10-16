package com.windingmaster.presentation.diagram

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.windingmaster.R
import com.windingmaster.domain.model.PhaseCode
import com.windingmaster.domain.model.SlotAssignment
import com.windingmaster.domain.model.WindingResult
import com.windingmaster.presentation.components.DiagramLegend
import com.windingmaster.presentation.components.ForceLtr
import com.windingmaster.presentation.components.PhaseColors
import com.windingmaster.presentation.components.PhaseStyle
import com.windingmaster.presentation.components.onPhaseColor
import com.windingmaster.presentation.components.rememberPhaseStyle
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

private class RingGeometry(val center: Offset, val radius: Float, val width: Float)

private fun ringGeometry(w: Float, h: Float): RingGeometry {
    val half = min(w, h) / 2f
    val width = half * 0.15f
    return RingGeometry(Offset(w / 2f, h / 2f), half * 0.84f - width / 2f, width)
}

private fun slotAngleDeg(slot: Int, z: Int): Float = -90f + (slot - 0.5f) * 360f / z

private fun polar(center: Offset, r: Float, deg: Float): Offset {
    val rad = Math.toRadians(deg.toDouble())
    return Offset(center.x + r * cos(rad).toFloat(), center.y + r * sin(rad).toFloat())
}

// رنگ‌های سیم (مطابق Extended)
private val MainWireColor = Color(0xFF0D47A1)
private val AuxWireColor  = Color(0xFFE65100)
private val SelectedColor = Color(0xFFD32F2F)
private val PairedColor   = Color(0xFFE040FB)
private val WireHighlight = Color(0xFF00C853)

@Composable
fun WindingDiagram(
    result: WindingResult,
    modifier: Modifier = Modifier,
    selectedSlot: Int? = null,
    onSlotTap: ((Int) -> Unit)? = null,
    showLegend: Boolean = true,
    showMain: Boolean = true,
    showAux: Boolean = true
) {
    val style = rememberPhaseStyle(result.phases)
    val textMeasurer = rememberTextMeasurer(cacheSize = 256)
    val bySlot = remember(result) { result.slotAssignments.associateBy { it.slotNumber } }
    val tapHandler by rememberUpdatedState(onSlotTap)
    val ringBg = MaterialTheme.colorScheme.surfaceVariant
    val ink = MaterialTheme.colorScheme.onSurface
    var zoom by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    val z = result.z
    val pairedSel = selectedSlot?.let { bySlot[it]?.pairedSlot }

    ForceLtr {
        Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clipToBounds()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, panChange, zoomChange, _ ->
                            zoom = (zoom * zoomChange).coerceIn(0.6f, 6f)
                            pan += panChange
                        }
                    }
                    .pointerInput(z) {
                        detectTapGestures(onTap = { p ->
                            val cb = tapHandler
                            if (cb != null) {
                                val g = ringGeometry(size.width.toFloat(), size.height.toFloat())
                                val local = g.center + (p - pan - g.center) / zoom
                                val dx = local.x - g.center.x
                                val dy = local.y - g.center.y
                                val r = hypot(dx, dy)
                                if (abs(r - g.radius) <= g.width * 0.8f) {
                                    val theta = ((Math.toDegrees(atan2(dy, dx).toDouble()) + 90.0) % 360.0 + 360.0) % 360.0
                                    val slot = floor(theta / (360.0 / z)).toInt() + 1
                                    cb(slot.coerceIn(1, z))
                                }
                            }
                        })
                    }
            ) {
                drawStator(
                    result, bySlot, style, textMeasurer, zoom, pan,
                    selectedSlot, pairedSel, ringBg, ink, showMain, showAux
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.diagram_zoom_hint),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { zoom = 1f; pan = Offset.Zero }) {
                    Text(stringResource(R.string.diagram_reset_view))
                }
            }
            if (showLegend) DiagramLegend(style)
        }
    }
}

private fun DrawScope.drawStator(
    result: WindingResult,
    bySlot: Map<Int, SlotAssignment>,
    style: PhaseStyle,
    measurer: TextMeasurer,
    zoom: Float,
    pan: Offset,
    selected: Int?,
    pairedSel: Int?,
    ringBg: Color,
    ink: Color,
    showMain: Boolean,
    showAux: Boolean
) {
    val g = ringGeometry(size.width, size.height)
    val z = result.z
    val stepDeg = 360f / z
    val gap = min(1.5f, stepDeg * 0.2f)

    withTransform({
        translate(pan.x, pan.y)
        scale(zoom, zoom, pivot = g.center)
    }) {
        drawCircle(ringBg, radius = g.radius, center = g.center, style = Stroke(g.width * 1.15f))

        // قوس‌های کویل (فقط سیم — با فیلتر showMain/showAux)
        val inner = g.radius - g.width * 0.65f
        for (a in result.slotAssignments) {
            val isMainGroup = a.phase == PhaseCode.MAIN || (a.phase == 1 && result.phases == 1)
            val isAuxGroup = a.phase == PhaseCode.AUX
            if (isMainGroup && !showMain) continue
            if (isAuxGroup && !showAux) continue

            // برای concentric از نیمه‌ی دوم صرف‌نظر کن
            if (a.pairedSlot < a.slotNumber && result.isConcentric) continue

            val p1 = polar(g.center, inner, slotAngleDeg(a.slotNumber, z))
            val p2 = polar(g.center, inner, slotAngleDeg(a.pairedSlot, z))
            val isSelectedArc = selected != null && a.slotNumber == selected
            val isPairedArc = pairedSel != null && a.slotNumber == pairedSel
            val hot = isSelectedArc || isPairedArc

            val color = when {
                isSelectedArc -> WireHighlight
                isPairedArc   -> WireHighlight.copy(alpha = 0.5f)
                isAuxGroup    -> AuxWireColor.copy(alpha = 0.8f)
                else          -> MainWireColor.copy(alpha = 0.8f)
            }
            drawLine(
                color = color,
                start = p1, end = p2,
                strokeWidth = if (hot) g.width * 0.14f else g.width * 0.06f
            )
        }

        // شیارها (همیشه نمایش داده می‌شوند)
        val arcTopLeft = Offset(g.center.x - g.radius, g.center.y - g.radius)
        val arcSize = Size(g.radius * 2f, g.radius * 2f)
        for (i in 1..z) {
            val a = bySlot[i] ?: continue
            val start = -90f + (i - 1) * stepDeg + gap / 2f
            val sweep = stepDeg - gap
            if (i == selected || i == pairedSel) {
                drawArc(
                    color = (if (i == selected) SelectedColor else PairedColor).copy(alpha = if (i == selected) 1f else 0.7f),
                    startAngle = start - 0.3f, sweepAngle = sweep + 0.6f, useCenter = false,
                    topLeft = arcTopLeft, size = arcSize, style = Stroke(g.width * 1.45f)
                )
            }
            drawArc(
                color = style.color(a.phase),
                startAngle = start, sweepAngle = sweep, useCenter = false,
                topLeft = arcTopLeft, size = arcSize, style = Stroke(g.width)
            )
        }

        // شماره‌ی شیار و علامت جهت
        val arcLen = 2f * PI.toFloat() * g.radius * zoom / z
        val every = ceil(22f / arcLen).toInt().coerceAtLeast(1)
        val numberStyle = TextStyle(fontSize = (g.width * 0.42f).toSp(), fontWeight = FontWeight.Bold)
        val mr = g.width * 0.14f
        for (i in 1..z) {
            if ((i - 1) % every != 0 && i != selected) continue
            val a = bySlot[i] ?: continue
            val c = polar(g.center, g.radius, slotAngleDeg(i, z))
            drawCentered(measurer, i.toString(), c, numberStyle.copy(color = onPhaseColor(style.color(a.phase))))
            if (every == 1) {
                val m = polar(g.center, g.radius + g.width * 0.95f, slotAngleDeg(i, z))
                if (a.direction == 1) {
                    drawCircle(ink, mr, m)
                } else {
                    drawLine(ink, m + Offset(-mr, -mr), m + Offset(mr, mr), strokeWidth = mr * 0.6f)
                    drawLine(ink, m + Offset(-mr, mr), m + Offset(mr, -mr), strokeWidth = mr * 0.6f)
                }
            }
        }

        drawCentered(
            measurer, "${result.z} / ${result.poles}", g.center,
            TextStyle(fontSize = (g.width * 0.9f).toSp(), fontWeight = FontWeight.Bold, color = ink)
        )
    }
}
