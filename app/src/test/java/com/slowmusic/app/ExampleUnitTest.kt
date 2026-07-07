package com.slowmusic.app

import com.slowmusic.app.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun songModelStoresCoreMetadata() {
        val song = Song(
            id = "1",
            title = "Test Song",
            artist = "Test Artist",
            album = "Test Album",
            albumArtUrl = null,
            previewUrl = null,
            streamUrl = null,
            duration = 180_000,
            genre = "Pop",
            releaseDate = "2026"
        )

        assertEquals("Test Song", song.title)
        assertEquals("Test Artist", song.artist)
        assertFalse(song.isDownloaded)
    }
}
