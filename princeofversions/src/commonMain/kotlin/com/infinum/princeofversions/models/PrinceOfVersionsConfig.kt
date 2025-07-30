package com.infinum.princeofversions.models

import com.infinum.princeofversions.enums.NotificationType

/**
 * This class holds loaded data from a configuration resource.
 *
 */
internal data class PrinceOfVersionsConfig(
    val mandatoryVersion: String?,
    val optionalVersion: String?,
    val optionalNotificationType: NotificationType,
    val metadata: Map<String, String>,
    val requirements: Map<String, String>
)
