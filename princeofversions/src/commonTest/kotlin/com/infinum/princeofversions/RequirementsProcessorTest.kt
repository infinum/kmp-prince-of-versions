package com.infinum.princeofversions

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RequirementsProcessorTest {

    private lateinit var processor: RequirementsProcessor

    private companion object {
        const val REQUIREMENT_KEY_1 = "key1"
        const val REQUIREMENT_KEY_2 = "key2"
        const val UNREGISTERED_KEY = "unregistered"
    }

    @BeforeTest
    fun setUp() {
        // Processor will be initialized in each test with specific checkers
    }

    @Test
    fun `areRequirementsSatisfied should return true when single requirement is satisfied`() {
        val checker = MockRequirementChecker(shouldPass = true)
        processor = RequirementsProcessor(mapOf(REQUIREMENT_KEY_1 to checker))
        val requirements = mapOf(REQUIREMENT_KEY_1 to "any_value")

        val result = processor.areRequirementsSatisfied(requirements)

        assertTrue(result)
        assertTrue(checker.wasCalledWith("any_value"))
    }

    @Test
    fun `areRequirementsSatisfied should return false when single requirement is not satisfied`() {
        val checker = MockRequirementChecker(shouldPass = false)
        processor = RequirementsProcessor(mapOf(REQUIREMENT_KEY_1 to checker))
        val requirements = mapOf(REQUIREMENT_KEY_1 to "any_value")

        val result = processor.areRequirementsSatisfied(requirements)

        assertFalse(result)
        assertTrue(checker.wasCalledWith("any_value"))
    }

    @Test
    fun `areRequirementsSatisfied should pass null values to checker when requirement value is null`() {
        val checker = MockRequirementChecker(shouldPass = false) // Return value doesn't matter for this test
        processor = RequirementsProcessor(mapOf(REQUIREMENT_KEY_1 to checker))
        val requirements = mapOf(REQUIREMENT_KEY_1 to null)

        processor.areRequirementsSatisfied(requirements)

        assertTrue(checker.wasCalledWith(null)) // The key assertion: processor passed null to checker
    }

    @Test
    fun `areRequirementsSatisfied should return false when no checker is registered for requirement`() {
        val checker = MockRequirementChecker(shouldPass = true)
        processor = RequirementsProcessor(mapOf(REQUIREMENT_KEY_1 to checker))
        val requirements = mapOf(UNREGISTERED_KEY to "some_value")

        val result = processor.areRequirementsSatisfied(requirements)

        assertFalse(result)
        assertFalse(checker.wasCalled())
    }

    @Test
    fun `areRequirementsSatisfied should return false when requirement checker throws exception`() {
        val checker = MockRequirementChecker(shouldThrow = true)
        processor = RequirementsProcessor(mapOf(REQUIREMENT_KEY_1 to checker))
        val requirements = mapOf(REQUIREMENT_KEY_1 to "any_value")

        val result = processor.areRequirementsSatisfied(requirements)

        assertFalse(result)
        assertTrue(checker.wasCalledWith("any_value"))
    }

    @Test
    fun `areRequirementsSatisfied should return true when all multiple requirements are satisfied`() {
        val checker1 = MockRequirementChecker(shouldPass = true)
        val checker2 = MockRequirementChecker(shouldPass = true)
        processor = RequirementsProcessor(
            mapOf(
                REQUIREMENT_KEY_1 to checker1,
                REQUIREMENT_KEY_2 to checker2,
            ),
        )
        val requirements = mapOf(
            REQUIREMENT_KEY_1 to "value1",
            REQUIREMENT_KEY_2 to "value2",
        )

        val result = processor.areRequirementsSatisfied(requirements)

        assertTrue(result)
        assertTrue(checker1.wasCalledWith("value1"))
        assertTrue(checker2.wasCalledWith("value2"))
    }

    @Test
    fun `areRequirementsSatisfied should return false when any of multiple requirements is not satisfied`() {
        val checker1 = MockRequirementChecker(shouldPass = true)
        val checker2 = MockRequirementChecker(shouldPass = false) // This one fails
        processor = RequirementsProcessor(
            mapOf(
                REQUIREMENT_KEY_1 to checker1,
                REQUIREMENT_KEY_2 to checker2,
            ),
        )
        val requirements = mapOf(
            REQUIREMENT_KEY_1 to "value1",
            REQUIREMENT_KEY_2 to "value2",
        )

        val result = processor.areRequirementsSatisfied(requirements)

        assertFalse(result)
        assertTrue(checker1.wasCalledWith("value1"))
        assertTrue(checker2.wasCalledWith("value2"))
    }

    @Test
    fun `areRequirementsSatisfied should return true when requirements map is empty`() {
        val checker = MockRequirementChecker(shouldPass = true)
        processor = RequirementsProcessor(mapOf(REQUIREMENT_KEY_1 to checker))
        val requirements = emptyMap<String, String?>()

        val result = processor.areRequirementsSatisfied(requirements)

        assertTrue(result)
        assertFalse(checker.wasCalled()) // No requirements, so no checkers should be called
    }

    @Test
    fun `areRequirementsSatisfied should return false when some requirements have registered checkers and others do not`() {
        val checker1 = MockRequirementChecker(shouldPass = true)
        processor = RequirementsProcessor(mapOf(REQUIREMENT_KEY_1 to checker1))
        val requirements = mapOf(
            REQUIREMENT_KEY_1 to "value1", // Has checker
            UNREGISTERED_KEY to "value2", // No checker
        )

        val result = processor.areRequirementsSatisfied(requirements)

        assertFalse(result)
        assertTrue(checker1.wasCalledWith("value1"))
    }

    @Test
    fun `areRequirementsSatisfied should handle mixed scenarios with exceptions and failures`() {
        val passingChecker = MockRequirementChecker(shouldPass = true)
        val throwingChecker = MockRequirementChecker(shouldThrow = true)
        processor = RequirementsProcessor(
            mapOf(
                REQUIREMENT_KEY_1 to passingChecker,
                REQUIREMENT_KEY_2 to throwingChecker,
            ),
        )
        val requirements = mapOf(
            REQUIREMENT_KEY_1 to "value1",
            REQUIREMENT_KEY_2 to "value2",
        )

        val result = processor.areRequirementsSatisfied(requirements)

        assertFalse(result)
        assertTrue(passingChecker.wasCalledWith("value1"))
        assertTrue(throwingChecker.wasCalledWith("value2"))
    }

    /**
     * Simple mock requirement checker that returns predictable results for testing processor logic.
     */
    private class MockRequirementChecker(
        private val shouldPass: Boolean = false,
        private val shouldThrow: Boolean = false,
    ) : RequirementChecker {
        private var lastCalledValue: String? = null
        private var wasCalled = false

        override fun checkRequirements(value: String?): Boolean {
            wasCalled = true
            lastCalledValue = value

            if (shouldThrow) {
                throw IllegalStateException("Mock checker exception")
            }

            return shouldPass
        }

        fun wasCalledWith(expectedValue: String?): Boolean {
            return wasCalled && lastCalledValue == expectedValue
        }

        fun wasCalled(): Boolean = wasCalled
    }
}