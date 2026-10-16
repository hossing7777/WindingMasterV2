package com.windingmaster.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.windingmaster.R

object PhaseColors {
    val U = Color(0xFFFFC107)
    val V = Color(0xFF4CAF50)
    val W = Color(0xFFE53935)
    val Main = Color(0xFF2196F3)
    val Aux = Color(0xFFFF9800)
    val Highlight = Color(0xFFFF6F00)
}

@Immutable
data class PhaseStyle(
    val labels: List<String>,
    val names: List<String>,
    val colors: List<Color>
) {
    fun label(phase: Int): String = labels.getOrElse(phase - 1) { "?" }
    fun color(phase: Int): Color = colors.getOrElse(phase - 1) { Color.Gray }
}

@Composable
fun rememberPhaseStyle(phases: Int): PhaseStyle {
    val u = stringResource(R.string.phase_u)
    val v = stringResource(R.string.phase_v)
    val w = stringResource(R.string.phase_w)
    val mShort = stringResource(R.string.phase_main_short)
    val aShort = stringResource(R.string.phase_aux_short)
    val mName = stringResource(R.string.phase_main)
    val aName = stringResource(R.string.phase_aux)
    val fU = stringResource(R.string.legend_phase_fmt, u)
    val fV = stringResource(R.string.legend_phase_fmt, v)
    val fW = stringResource(R.string.legend_phase_fmt, w)
    return remember(phases, u, v, w, mShort, aShort, mName, aName) {
        if (phases == 3) {
            PhaseStyle(
                labels = listOf(u, v, w),
                names = listOf(fU, fV, fW),
                colors = listOf(PhaseColors.U, PhaseColors.V, PhaseColors.W)
            )
        } else {
            PhaseStyle(
                labels = listOf(mShort, aShort),
                names = listOf(mName, aName),
                colors = listOf(PhaseColors.Main, PhaseColors.Aux)
            )
        }
    }
}

fun onPhaseColor(background: Color): Color =
    if (background.luminance() > 0.179f) Color.Black else Color.White

@Composable
fun ForceLtr(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr, content = content)
}

@Composable
fun DiagramLegend(style: PhaseStyle, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            style.colors.forEachIndexed { i, c ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(14.dp).background(c, RoundedCornerShape(3.dp)))
                    Spacer(Modifier.width(6.dp))
                    Text(style.names[i], style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        Text(stringResource(R.string.legend_direction), style = MaterialTheme.typography.labelSmall)
    }
}
