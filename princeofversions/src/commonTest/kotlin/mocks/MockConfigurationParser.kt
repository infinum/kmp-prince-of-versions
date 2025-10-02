package mocks

import com.infinum.princeofversions.BaseConfigurationParser
import com.infinum.princeofversions.BasePrinceOfVersionsConfig

/**
 * Mock implementation of [BaseConfigurationParser] for testing purposes.
 */
internal class MockConfigurationParser<T>(
    private var config: BasePrinceOfVersionsConfig<T>? = null,
) : BaseConfigurationParser<T> {

    override fun parse(value: String): BasePrinceOfVersionsConfig<T> {
        return config ?: throw IllegalStateException("Config not set")
    }

    fun setConfig(config: BasePrinceOfVersionsConfig<T>) {
        this.config = config
    }
}