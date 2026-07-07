package com.slowmusic.app

import org.mockito.Mockito

object MockHelper {
    inline fun <reified T> mock(): T = Mockito.mock(T::class.java)
}
