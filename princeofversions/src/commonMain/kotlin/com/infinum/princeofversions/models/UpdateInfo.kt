package com.infinum.princeofversions.models

import com.infinum.princeofversions.enums.NotificationType

/**
 * Represents selected update configuration object based on requirements
 */
internal data class UpdateInfo (
    val requiredVersion: String?,
    val lastVersionAvailable: String?,
    val requirements: Map<String, String>,
    val installedVersion: String,
    val notificationFrequency: NotificationType
) {

    override fun toString(): String = "Info{" +
        "Installed version =" + installedVersion +
        ", Required version ='" + requiredVersion + '\'' +
        ", Last version ='" + lastVersionAvailable + '\'' +
        ", Requirements =" + requirements +
        '}'
}
