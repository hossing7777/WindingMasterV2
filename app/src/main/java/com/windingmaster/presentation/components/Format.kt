package com.windingmaster.presentation.components

import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor

fun fmt(d: Double): String =
    if (d == floor(d) && abs(d) < 1e9) d.toLong().toString()
    else String.format(Locale.US, "%.2f", d).trimEnd('0').trimEnd('.')
