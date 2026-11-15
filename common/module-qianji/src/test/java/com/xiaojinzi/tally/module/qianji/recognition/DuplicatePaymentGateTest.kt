package com.xiaojinzi.tally.module.qianji.recognition

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class DuplicatePaymentGateTest(private val case: SyntheticPaymentRegression.GateCase) {
    @Test
    fun monotonicGateMaintainsDuplicateContract() {
        case.verify()
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases(): Collection<Array<Any>> = SyntheticPaymentRegression.gateCases.map { arrayOf<Any>(it) }
    }
}
