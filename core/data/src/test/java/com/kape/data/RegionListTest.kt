package com.kape.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class RegionListTest {
    private fun row(
        name: String,
        favorite: Boolean = false,
    ) = RegionServerItem(RegionItemType.Content(isFavorite = favorite, server = name))

    private val favorites = RegionServerItem<String>(RegionItemType.HeadingFavorites)
    private val all = RegionServerItem<String>(RegionItemType.HeadingAll)

    @Test
    fun `without favorites the rows have no headings`() {
        assertEquals(listOf(row("a"), row("b")), arrangeRegions(listOf("a", "b"), isFavorite = { false }))
    }

    @Test
    fun `favorites come first and are repeated in all by default`() {
        val rows = arrangeRegions(listOf("a", "b"), isFavorite = { it == "b" })

        assertEquals(listOf(favorites, row("b", true), all, row("a"), row("b", true)), rows)
    }

    @Test
    fun `favorites can be left out of all`() {
        val rows = arrangeRegions(listOf("a", "b"), isFavorite = { it == "b" }, includeFavoritesInAll = false)

        assertEquals(listOf(favorites, row("b", true), all, row("a")), rows)
    }

    @Test
    fun `rows can disable the favorite toggle`() {
        val rows = arrangeRegions(listOf("auto"), isFavorite = { false }, canBeFavorite = { false })

        assertEquals(listOf(RegionServerItem(RegionItemType.Content(isFavorite = false, enableFavorite = false, server = "auto"))), rows)
    }

    @Test
    fun `search matches part of the name ignoring case, listing each row once and no headings`() {
        val rows = arrangeRegions(listOf("Berlin", "Bern", "Paris"), isFavorite = { it == "Bern" })

        assertEquals(listOf(row("Bern", true), row("Berlin")), rows.searchByName("BER") { it })
    }

    @Test
    fun `an empty search finds nothing`() {
        assertEquals(emptyList<RegionServerItem<String>>(), listOf(row("a")).searchByName("") { it })
    }
}