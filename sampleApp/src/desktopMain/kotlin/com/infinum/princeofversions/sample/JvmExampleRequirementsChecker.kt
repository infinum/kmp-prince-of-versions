package com.infinum.princeofversions.sample

import com.infinum.princeofversions.RequirementChecker

private const val THRESHOLD = 5

/**
 * A JVM-specific custom requirement checker that checks if a numeric value from the
 * configuration meets a certain threshold.
 */
class JvmExampleRequirementsChecker : RequirementChecker {
    override fun checkRequirements(value: String?): Boolean {
        val numberFromConfig = value?.toIntOrNull() ?: 0
        return numberFromConfig >= THRESHOLD
    }
}