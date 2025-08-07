package com.infinum.princeofversions

internal expect class DefaultRequirementChecker : RequirementChecker {
    override fun checkRequirements(value: String): Boolean
}
