package com.tmukas.filmvault.presentation.movies

import assertk.assertThat
import assertk.assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@DisplayName("Date Formatting - Business Logic")
class DateFormattingTest {

    @ParameterizedTest
    @CsvSource(
        "'2024-01-01', 'January 2024'",
        "'2024-02-15', 'February 2024'",
        "'2024-12-31', 'December 2024'",
        "'2023-06-15', 'June 2023'",
        "'2025-03-10', 'March 2025'"
    )
    @DisplayName("Should format valid dates correctly")
    fun shouldFormatValidDatesCorrectly(inputDate: String, expectedOutput: String) {
        // When
        val formatted = formatReleaseDate(inputDate)

        // Then
        assertThat(formatted).isEqualTo(expectedOutput)
    }

    @ParameterizedTest
    @CsvSource(
        "'invalid-date'",
        "'not-a-date'",
        "'2024/01/01'",
        "''"
    )
    @DisplayName("Should handle invalid date formats gracefully")
    fun shouldHandleInvalidDateFormats(invalidDate: String) {
        // When
        val formatted = formatReleaseDate(invalidDate)

        // Then
        assertThat(formatted).isEqualTo("Unknown")
    }

    @Test
    @DisplayName("Should handle empty date gracefully")
    fun shouldHandleEmptyDateGracefully() {
        // When
        val formatted = formatReleaseDate("")

        // Then
        assertThat(formatted).isEqualTo("Unknown")
    }

    @Test
    @DisplayName("Should handle edge case dates")
    fun shouldHandleEdgeCaseDates() {
        // Given
        val testCases = mapOf(
            "2000-01-01" to "January 2000", // Y2K
            "2024-02-29" to "February 2024", // Leap year
            "1999-12-31" to "December 1999"  // End of century
        )

        // When & Then
        testCases.forEach { (input, expected) ->
            val result = formatReleaseDate(input)
            assertThat(result).isEqualTo(expected)
        }
    }

    @Test
    @DisplayName("Should format different months correctly")
    fun shouldFormatDifferentMonthsCorrectly() {
        // Given
        val testCases = mapOf(
            "2024-01-15" to "January 2024",
            "2024-07-04" to "July 2024",
            "2024-12-25" to "December 2024"
        )

        // When & Then
        testCases.forEach { (input, expected) ->
            val result = formatReleaseDate(input)
            assertThat(result).isEqualTo(expected)
        }
    }

    // Helper method to simulate the private formatReleaseDate method from MoviesViewModel
    private fun formatReleaseDate(releaseDate: String): String {
        if (releaseDate.isBlank()) return "Unknown"

        val input = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        input.isLenient = false // Strict date parsing
        val output = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.ENGLISH)

        return try {
            val date = input.parse(releaseDate)
            if (date != null) {
                val formattedDate = output.format(date)
                formattedDate.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(java.util.Locale.ENGLISH) else it.toString()
                }
            } else {
                "Unknown"
            }
        } catch (_: Exception) {
            "Unknown"
        }
    }
}