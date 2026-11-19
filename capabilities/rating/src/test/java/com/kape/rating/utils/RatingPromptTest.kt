package com.kape.rating.utils

import com.kape.rating.data.RatingDialogType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class RatingPromptTest {
    @Test
    fun `asks for a rating after the third connection`() {
        assertNull(ratingPromptFor(connections = 2, daysSinceNotEnjoying = null))
        assertEquals(RatingDialogType.General, ratingPromptFor(connections = 3, daysSinceNotEnjoying = null))
        assertNull(ratingPromptFor(connections = 4, daysSinceNotEnjoying = null))
    }

    @Test
    fun `asks for a review again after 50 connections and 30 days, if the user wasn't enjoying it`() {
        assertEquals(RatingDialogType.Review, ratingPromptFor(connections = 50, daysSinceNotEnjoying = 30))
        assertNull(ratingPromptFor(connections = 50, daysSinceNotEnjoying = 29))
        assertNull(ratingPromptFor(connections = 49, daysSinceNotEnjoying = 40))
    }

    @Test
    fun `doesn't repeat the first prompt for a user who wasn't enjoying it`() {
        assertNull(ratingPromptFor(connections = 3, daysSinceNotEnjoying = 1))
    }
}