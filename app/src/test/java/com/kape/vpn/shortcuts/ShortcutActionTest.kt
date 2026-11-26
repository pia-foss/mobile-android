package com.kape.vpn.shortcuts

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class ShortcutActionTest {
    @Test
    fun `recognises every shortcut's intent action`() {
        ShortcutAction.entries.forEach { assertEquals(it, ShortcutAction.fromIntentAction(it.intentAction)) }
    }

    @Test
    fun `ignores other actions`() {
        assertNull(ShortcutAction.fromIntentAction("android.intent.action.MAIN"))
        assertNull(ShortcutAction.fromIntentAction(null))
    }
}