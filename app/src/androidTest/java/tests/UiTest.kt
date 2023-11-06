package tests

import org.junit.Before
import tests.actions.UiAction

interface UiTest {

    val uiAction: UiAction

    @Before
    fun setUp()
}