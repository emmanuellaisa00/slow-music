package com.slowmusic.app

import org.mockito.kotlin.*

/**
 * Mockito Kotlin helper for mocking
 */
object MockHelper {
    inline fun <reified T> mock(): T = mock()
}
