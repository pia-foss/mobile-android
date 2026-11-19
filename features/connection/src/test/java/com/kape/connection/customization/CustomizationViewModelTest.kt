package com.kape.connection.customization

import com.kape.connection.domain.usecase.ObserveScreenElementsUseCase
import com.kape.connection.domain.usecase.ScreenElements
import com.kape.contracts.Router
import com.kape.customization.data.Element
import com.kape.localprefs.api.CustomizationStore
import com.kape.localprefs.data.customization.ScreenElement
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class CustomizationViewModelTest {
    private val elements =
        listOf(
            ScreenElement(Element.VpnRegionSelection, "region"),
            ScreenElement(Element.QuickConnect, "quick"),
            ScreenElement(Element.Traffic, "traffic"),
        )
    private val prefs = mockk<CustomizationStore>(relaxed = true)
    private val router = mockk<Router>(relaxed = true)
    private lateinit var viewModel: CustomizationViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val observe = mockk<ObserveScreenElementsUseCase> { every { this@mockk() } returns flowOf(ScreenElements(elements, elements)) }
        viewModel = CustomizationViewModel(observe, prefs, router)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `moving a tile reorders the list`() {
        viewModel.onMove(fromIndex = 2, toIndex = 0)

        assertEquals(listOf("traffic", "region", "quick"), viewModel.items.value.map { it.name })
    }

    @Test
    fun `toggling hides and shows a tile`() {
        viewModel.toggleVisibility(Element.QuickConnect)
        assertEquals(false, viewModel.items.value[1].isVisible)

        viewModel.toggleVisibility(Element.QuickConnect)
        assertEquals(true, viewModel.items.value[1].isVisible)
    }

    @Test
    fun `saving stores the edits, then closes the screen`() {
        viewModel.onMove(fromIndex = 0, toIndex = 1)

        viewModel.save()

        coVerifyOrder {
            prefs.setElements(listOf(elements[1], elements[0], elements[2]))
            router.navigateBack()
        }
    }
}