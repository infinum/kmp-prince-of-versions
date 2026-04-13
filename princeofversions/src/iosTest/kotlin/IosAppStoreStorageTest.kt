package com.infinum.princeofversions

import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import platform.Foundation.NSUserDefaults

class IosAppStoreStorageTest {

    private val storage = IosAppStoreStorage()

    @BeforeTest
    fun setUp() {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(STORAGE_KEY)
    }

    @Test
    fun `returns null when no version has been saved`() = runTest {
        assertNull(storage.getLastSavedVersion())
    }

    @Test
    fun `saves and retrieves a version`() = runTest {
        storage.saveVersion("2.0.0")
        assertEquals("2.0.0", storage.getLastSavedVersion())
    }

    @Test
    fun `overwrites previously saved version`() = runTest {
        storage.saveVersion("1.0.0")
        storage.saveVersion("2.0.0")
        assertEquals("2.0.0", storage.getLastSavedVersion())
    }

    companion object {
        private const val STORAGE_KEY = "last_notified_appstore_version"
    }
}
