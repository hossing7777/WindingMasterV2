package com.windingmaster.presentation.diagram

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle

internal fun DrawScope.drawCentered(
    measurer: TextMeasurer,
    text: String,
    center: Offset,
    style: TextStyle
) {
    val layout = measurer.measure(text, style)
    drawText(
        textLayoutResult = layout,
        topLeft = Offset(center.x - layout.size.width / 2f, center.y - layout.size.height / 2f)
    )
}
