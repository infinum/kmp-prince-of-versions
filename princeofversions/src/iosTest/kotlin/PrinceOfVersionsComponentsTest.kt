package com.infinum.princeofversions

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SystemVersionRequirementCheckerIosTest {

    private val checker = SystemVersionRequirementChecker()

    @Test
    fun null_or_empty_is_false() {
        assertFalse(checker.checkRequirements(null))
        assertFalse(checker.checkRequirements(""))
    }

    @Test
    fun very_low_requirement_is_true() {
        assertTrue(checker.checkRequirements("1.0.0"))
        assertTrue(checker.checkRequirements("0"))
    }

    @Test
    fun absurdly_high_requirement_is_false() {
        assertFalse(checker.checkRequirements("999.0.0"))
    }
}
