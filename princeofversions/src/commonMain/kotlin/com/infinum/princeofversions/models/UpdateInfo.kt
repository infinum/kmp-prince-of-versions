package com.infinum.princeofversions.models

import com.infinum.princeofversions.enums.NotificationType

/**
 * Represents selected update configuration object based on requirements
 */
internal data class UpdateInfo<T>(
    val requiredVersion: T?,
    val lastVersionAvailable: T?,
    val requirements: Map<String, String>,
    val installedVersion: T,
    val notificationFrequency: NotificationType
) {

    override fun toString(): String =
        "Info{Installed version =$installedVersion, Required version ='$requiredVersion'," +
            " Last version ='$lastVersionAvailable', Requirements =$requirements}"
}
