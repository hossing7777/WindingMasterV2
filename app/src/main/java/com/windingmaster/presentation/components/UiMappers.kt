package com.windingmaster.presentation.components

import androidx.annotation.StringRes
import com.windingmaster.R
import com.windingmaster.domain.model.CalcWarning
import com.windingmaster.domain.model.ConnectionType
import com.windingmaster.domain.model.ValidationError
import com.windingmaster.domain.model.WindingType

@StringRes
fun ValidationError.messageRes(): Int = when (this) {
    ValidationError.SLOTS_OUT_OF_RANGE -> R.string.err_slots_out_of_range
    ValidationError.SLOTS_ODD -> R.string.err_slots_odd
    ValidationError.POLES_INVALID -> R.string.err_poles_invalid
    ValidationError.FREQUENCY_INVALID -> R.string.err_frequency_invalid
    ValidationError.PHASES_INVALID -> R.string.err_phases_invalid
    ValidationError.PITCH_OUT_OF_RANGE -> R.string.err_pitch_out_of_range
}

@StringRes
fun CalcWarning.messageRes(): Int = when (this) {
    CalcWarning.FRACTIONAL_WINDING -> R.string.warn_fractional_winding
    CalcWarning.FRACTIONAL_PITCH -> R.string.warn_fractional_pitch
    CalcWarning.SLOTS_NOT_DIVISIBLE_BY_3 -> R.string.warn_slots_not_divisible_by_3
}

@StringRes
fun WindingType.labelRes(): Int = when (this) {
    WindingType.FULL_CONCENTRIC -> R.string.wt_full_concentric
    WindingType.FULL_SERIES -> R.string.wt_full_series
    WindingType.FRACTIONAL_CONCENTRIC -> R.string.wt_fractional_concentric
    WindingType.FRACTIONAL_SERIES -> R.string.wt_fractional_series
}

@StringRes
fun ConnectionType.labelRes(): Int = when (this) {
    ConnectionType.STAR -> R.string.connection_star
    ConnectionType.DELTA -> R.string.connection_delta
}
