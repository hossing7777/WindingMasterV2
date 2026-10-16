package com.windingmaster.domain.usecase

import com.windingmaster.domain.calculator.WindingCalculator
import com.windingmaster.domain.model.ValidationError
import com.windingmaster.domain.model.WindingInput
import com.windingmaster.domain.model.WindingResult
import javax.inject.Inject

sealed interface CalcOutcome {
    data class Success(val result: WindingResult) : CalcOutcome
    data class Invalid(val error: ValidationError) : CalcOutcome
}

class CalculateWindingUseCase @Inject constructor(
    private val calculator: WindingCalculator
) {
    operator fun invoke(input: WindingInput): CalcOutcome =
        calculator.validate(input)?.let { CalcOutcome.Invalid(it) }
            ?: CalcOutcome.Success(calculator.calculate(input))
}
