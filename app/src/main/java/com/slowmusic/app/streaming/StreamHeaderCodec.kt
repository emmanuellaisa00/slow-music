package com.slowmusic.app.streaming

import android.util.Base64

object StreamHeaderCodec {
    const val PREFIX = "smh1."
    private const val UA_KEY = "__ua__"
    private const val UNIT = '\u001F'
    private const val REC = '\u001E'
    private const val FLAGS = Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP

    fun userAgentKey(): String = UA_KEY

    fun encode(headers: Map<String, String>): String {
        if (headers.isEmpty()) return ""
        val blob = headers.entries.joinToString(REC.toString()) { (k, v) -> "$k$UNIT$v" }
        return PREFIX + Base64.encodeToString(blob.toByteArray(Charsets.UTF_8), FLAGS)
    }

    fun decode(fragment: String?): Map<String, String>? {
        if (fragment == null || !fragment.startsWith(PREFIX)) return null
        return runCatching {
            val raw = Base64.decode(fragment.removePrefix(PREFIX), FLAGS)
            val blob = String(raw, Charsets.UTF_8)
            buildMap {
                blob.split(REC).forEach { record ->
                    val i = record.indexOf(UNIT)
                    if (i > 0) put(record.substring(0, i), record.substring(i + 1))
                }
            }
        }.getOrNull()
    }
}
