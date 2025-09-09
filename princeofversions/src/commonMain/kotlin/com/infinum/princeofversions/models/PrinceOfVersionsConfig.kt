package com.infinum.princeofversions.models

import com.infinum.princeofversions.enums.NotificationType

/**
 * This class holds loaded data from a configuration resource.
 *
 */
public data class PrinceOfVersionsConfig<T>(
    val mandatoryVersion: T?,
    val optionalVersion: T?,
    val optionalNotificationType: NotificationType,
    val metadata: Map<String, String>,
    val requirements: Map<String, String>
)
