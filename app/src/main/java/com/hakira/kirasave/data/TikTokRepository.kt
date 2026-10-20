package com.hakira.kirasave.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class TikTokException(message: String, cause: Throwable? = null) : Exception(message, cause)

class TikTokRepository(
    private val client: OkHttpClient,
    private val settings: SettingsRepository
) {
    private val apiLock = Mutex()
    private var lastApiCall = 0L

    data class NormalizedInput(
        val url: String,
        val idOnly: Boolean = false,
        val videoId: String? = null
    )

    suspend fun analyze(rawInput: String): Result<TikTokMedia> = withContext(Dispatchers.IO) {
        try {
            val normalized = normalizeInput(rawInput)
                ?: return@withContext Result.failure(TikTokException("Link TikTok tidak valid."))
            val canonicalUrl = if (normalized.idOnly) normalized.url else resolveShortLink(normalized.url)

            var deviceError: Throwable? = null
            if (!normalized.idOnly) {
                try {
                    parseFromWeb(canonicalUrl)?.let { return@withContext Result.success(it) }
                } catch (t: Throwable) {
                    deviceError = t
                }
            }

            try {
                Result.success(fetchFromTikWm(canonicalUrl))
            } catch (t: Throwable) {
                val detail = when {
                    deviceError != null -> "Parser perangkat gagal, lalu layanan cadangan juga gagal."
                    else -> "Layanan cadangan sedang tidak dapat dijangkau."
                }
                Result.failure(TikTokException("$detail ${t.message ?: ""}".trim(), t))
            }
        } catch (t: Throwable) {
            Result.failure(TikTokException(friendlyMessage(t), t))
        }
    }

    suspend fun stories(rawQuery: String): Result<List<TikTokMedia>> = withContext(Dispatchers.IO) {
        try {
            val handle = rawQuery.trim().removePrefix("@").trim()
            if (handle.isBlank()) return@withContext Result.failure(TikTokException("Masukkan username TikTok."))
            // The free endpoint accepts unique_id directly. This avoids the separate
            // user/info lookup, which is frequently blocked by Cloudflare.
            val response = if (handle.all { it.isDigit() }) {
                apiGet(
                    "https://www.tikwm.com/api/user/story",
                    mapOf("user_id" to handle)
                )
            } else {
                apiGet(
                    "https://www.tikwm.com/api/user/story",
                    mapOf("unique_id" to handle)
                )
            }
            if (response.optInt("code", -1) != 0) {
                throw TikTokException(response.optString("msg", "Stories tidak tersedia."))
            }
            val videos = response.optJSONObject("data")?.optJSONArray("videos")
                ?: JSONArray()
            val result = buildList {
                for (i in 0 until videos.length()) {
                    val item = videos.optJSONObject(i) ?: continue
                    parseStoryItem(item, handle)?.let(::add)
                }
            }
            Result.success(result)
        } catch (t: Throwable) {
            Result.failure(TikTokException(friendlyMessage(t), t))
        }
    }

    private suspend fun fetchFromTikWm(url: String): TikTokMedia {
        val response = apiGet(
            "https://www.tikwm.com/api/",
            mapOf("url" to url, "hd" to "1")
        )
        if (response.optInt("code", -1) != 0) {
            val msg = response.optString("msg", "Video tidak dapat diproses.")
            throw TikTokException(translateApiError(msg))
        }
        val data = response.optJSONObject("data")
            ?: throw TikTokException("Respons video kosong.")
        return parseTikWmItem(data, url)
    }

    private suspend fun apiGet(baseUrl: String, params: Map<String, String>): JSONObject {
        val builder = baseUrl.toHttpUrlOrNull()?.newBuilder()
            ?: throw TikTokException("Alamat layanan tidak valid.")
        params.forEach { (key, value) -> builder.addQueryParameter(key, value) }
        val url = builder.build()

        var lastError: Throwable? = null
        repeat(3) { attempt ->
            throttleApi()
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", ANDROID_UA)
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Referer", "https://www.tikwm.com/")
                    .get()
                    .build()
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    if (response.code == 429 || response.code == 403 || response.code >= 500) {
                        lastError = IOException("HTTP ${response.code}")
                        delay(900L * (attempt + 1))
                        return@use
                    }
                    if (!response.isSuccessful) {
                        throw IOException("HTTP ${response.code}")
                    }
                    if (!body.trimStart().startsWith("{")) {
                        lastError = IOException("Respons bukan JSON")
                        delay(900L * (attempt + 1))
                        return@use
                    }
                    return JSONObject(body)
                }
            } catch (t: Throwable) {
                lastError = t
                delay(700L * (attempt + 1))
            }
        }
        throw TikTokException("Layanan sedang sibuk. Coba lagi beberapa saat.", lastError)
    }

    private suspend fun throttleApi() {
        apiLock.withLock {
            val elapsed = System.currentTimeMillis() - lastApiCall
            if (elapsed < 1_050L) delay(1_050L - elapsed)
            lastApiCall = System.currentTimeMillis()
        }
    }

    private fun parseTikWmItem(data: JSONObject, originalUrl: String): TikTokMedia {
        val authorJson = data.optJSONObject("author") ?: JSONObject()
        val images = mutableListOf<String>()
        data.optJSONArray("images")?.let { array ->
            for (i in 0 until array.length()) {
                when (val value = array.opt(i)) {
                    is String -> if (value.startsWith("http")) images += value
                    is JSONObject -> {
                        val url = value.optString("url")
                        if (url.startsWith("http")) images += url
                    }
                }
            }
        }

        val play = data.optString("play").takeIf { it.startsWith("http") }
        val hdPlay = data.optString("hdplay").takeIf { it.startsWith("http") }
        val music = data.optString("music").takeIf { it.startsWith("http") }
        val title = data.optString("title").ifBlank { "Video TikTok" }
        val id = data.optString("id").ifBlank { extractVideoId(originalUrl) ?: System.currentTimeMillis().toString() }

        if (play == null && hdPlay == null && images.isEmpty()) {
            throw TikTokException("Media tidak ditemukan pada postingan ini.")
        }

        return TikTokMedia(
            videoId = id,
            sourceUrl = originalUrl,
            title = title,
            author = TikTokAuthor(
                id = authorJson.optString("id"),
                handle = authorJson.optString("unique_id"),
                nickname = authorJson.optString("nickname").ifBlank {
                    authorJson.optString("unique_id")
                },
                avatarUrl = authorJson.optString("avatar").takeIf { it.startsWith("http") }
            ),
            coverUrl = data.optString("cover").takeIf { it.startsWith("http") },
            durationSeconds = data.optInt("duration", 0),
            hdUrl = hdPlay ?: play,
            sdUrl = play,
            audioUrl = music ?: data.optJSONObject("music_info")
                ?.optString("play")
                ?.takeIf { it.startsWith("http") },
            imageUrls = images,
            hdSize = data.optLong("hd_size", 0L).takeIf { it > 0 } ?: data.optLong("size", 0L),
            sdSize = data.optLong("size", 0L),
            stats = TikTokStats(
                views = data.optLong("play_count", 0L),
                likes = data.optLong("digg_count", 0L),
                comments = data.optLong("comment_count", 0L),
                shares = data.optLong("share_count", 0L)
            ),
            source = "TikWM"
        )
    }

    private fun parseStoryItem(item: JSONObject, fallbackHandle: String): TikTokMedia? {
        val videoObject = item.optJSONObject("video")
        val play = item.optString("play").takeIf { it.startsWith("http") }
            ?: item.optString("video").takeIf { it.startsWith("http") }
            ?: videoObject?.optString("playAddr")?.takeIf { it.startsWith("http") }
        val hd = item.optString("hdplay").takeIf { it.startsWith("http") }
            ?: videoObject?.optString("playAddr")?.takeIf { it.startsWith("http") }
        val cover = item.optString("cover").takeIf { it.startsWith("http") }
            ?: videoObject?.optString("cover")?.takeIf { it.startsWith("http") }
        val authorJson = item.optJSONObject("author") ?: JSONObject()
        val images = mutableListOf<String>()
        item.optJSONArray("images")?.let { array ->
            for (i in 0 until array.length()) {
                when (val value = array.opt(i)) {
                    is String -> value.takeIf { it.startsWith("http") }?.let(images::add)
                    is JSONObject -> value.optString("url")
                        .takeIf { it.startsWith("http") }
                        ?.let(images::add)
                }
            }
        }
        if (play == null && hd == null && images.isEmpty()) return null
        val id = item.optString("id").ifBlank {
            item.optString("aweme_id").ifBlank {
                item.optString("video_id").ifBlank {
                    "story-${System.currentTimeMillis()}-${images.size}"
                }
            }
        }
        return TikTokMedia(
            videoId = id,
            sourceUrl = item.optString("share_url", "https://www.tiktok.com/@$fallbackHandle"),
            title = item.optString("title").ifBlank { "Stories @$fallbackHandle" },
            author = TikTokAuthor(
                id = authorJson.optString("id"),
                handle = authorJson.optString("unique_id").ifBlank { fallbackHandle },
                nickname = authorJson.optString("nickname").ifBlank { fallbackHandle },
                avatarUrl = authorJson.optString("avatar").takeIf { it.startsWith("http") }
            ),
            coverUrl = cover,
            durationSeconds = item.optInt("duration", videoObject?.optInt("duration", 0) ?: 0),
            hdUrl = hd ?: play,
            sdUrl = play,
            imageUrls = images,
            hdSize = item.optLong("hd_size", 0L),
            sdSize = item.optLong("size", 0L),
            stats = TikTokStats(
                views = item.optLong("play_count", 0L),
                likes = item.optLong("digg_count", 0L),
                comments = item.optLong("comment_count", 0L),
                shares = item.optLong("share_count", 0L)
            ),
            source = "TikWM Stories",
            isStory = true
        )
    }

    private fun parseFromWeb(url: String): TikTokMedia? {
        val html = getText(url)
        val json = extractBootstrapJson(html) ?: return null

        val scope = json.optJSONObject("__DEFAULT_SCOPE__")
        val item = scope
            ?.optJSONObject("webapp.video-detail")
            ?.optJSONObject("itemInfo")
            ?.optJSONObject("itemStruct")
            ?: run {
                val itemModule = json.optJSONObject("ItemModule") ?: return@run null
                val firstKey = itemModule.keys().asSequence().firstOrNull() ?: return@run null
                itemModule.optJSONObject(firstKey)
            }
            ?: return null

        val video = item.optJSONObject("video") ?: return null
        val bitrateInfo = video.optJSONArray("bitrateInfo")
        var bestUrl: String? = null
        var bestBitrate = -1
        if (bitrateInfo != null) {
            for (i in 0 until bitrateInfo.length()) {
                val entry = bitrateInfo.optJSONObject(i) ?: continue
                val bitrate = entry.optInt("Bitrate", 0)
                val playAddr = entry.opt("PlayAddr")
                val candidate = when (playAddr) {
                    is String -> playAddr
                    is JSONObject -> playAddr.firstUrl()
                    else -> null
                }
                if (candidate != null && bitrate > bestBitrate) {
                    bestBitrate = bitrate
                    bestUrl = candidate
                }
            }
        }

        // Only accept the on-device route when a native bitrate stream exists.
        // The ordinary playAddr is often the watermarked stream.
        if (bestUrl == null) return null

        val author = item.optJSONObject("author") ?: JSONObject()
        val stats = item.optJSONObject("stats") ?: JSONObject()
        val images = mutableListOf<String>()
        item.optJSONObject("imagePost")?.optJSONArray("images")?.let { array ->
            for (i in 0 until array.length()) {
                val image = array.optJSONObject(i) ?: continue
                image.optJSONObject("imageURL")?.firstUrl()?.let(images::add)
            }
        }

        val id = item.optString("id").ifBlank {
            extractVideoId(url) ?: System.currentTimeMillis().toString()
        }
        val cover = video.optString("originCover").takeIf { it.startsWith("http") }
            ?: video.optString("cover").takeIf { it.startsWith("http") }
        val music = item.optJSONObject("music")?.optString("playUrl")?.takeIf { it.startsWith("http") }

        return TikTokMedia(
            videoId = id,
            sourceUrl = url,
            title = item.optString("desc").ifBlank { "Video TikTok" },
            author = TikTokAuthor(
                id = author.optString("id"),
                handle = author.optString("uniqueId"),
                nickname = author.optString("nickname").ifBlank { author.optString("uniqueId") },
                avatarUrl = author.optString("avatarThumb").takeIf { it.startsWith("http") }
            ),
            coverUrl = cover,
            durationSeconds = video.optInt("duration", 0),
            hdUrl = bestUrl,
            sdUrl = bestUrl,
            audioUrl = music,
            imageUrls = images,
            stats = TikTokStats(
                views = stats.optLong("playCount", 0L),
                likes = stats.optLong("diggCount", 0L),
                comments = stats.optLong("commentCount", 0L),
                shares = stats.optLong("shareCount", 0L)
            ),
            source = "Parser perangkat"
        )
    }

    private fun JSONObject.firstUrl(): String? {
        optString("UrlList").takeIf { it.startsWith("http") }?.let { return it }
        val list = optJSONArray("UrlList") ?: return null
        for (i in 0 until list.length()) {
            val value = list.optString(i)
            if (value.startsWith("http")) return value
        }
        return null
    }

    private fun extractBootstrapJson(html: String): JSONObject? {
        val patterns = listOf(
            Regex(
                """<script[^>]+id=["']__UNIVERSAL_DATA_FOR_REHYDRATION__["'][^>]*>(.*?)</script>""",
                RegexOption.DOT_MATCHES_ALL
            ),
            Regex(
                """<script[^>]+id=["']SIGI_STATE["'][^>]*>(.*?)</script>""",
                RegexOption.DOT_MATCHES_ALL
            )
        )
        for (pattern in patterns) {
            val match = pattern.find(html) ?: continue
            try {
                return JSONObject(match.groupValues[1])
            } catch (_: Throwable) {
                // Try the next bootstrap payload.
            }
        }
        return null
    }

    private fun getText(url: String): String {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", DESKTOP_UA)
            .header("Accept-Language", "id-ID,id;q=0.9,en-US;q=0.8,en;q=0.7")
            .header("Accept", "text/html,application/xhtml+xml")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
            return response.body?.string().orEmpty()
        }
    }

    private fun resolveShortLink(url: String): String {
        val parsed = url.toHttpUrlOrNull() ?: return url
        if (parsed.host !in SHORT_HOSTS) return url
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", ANDROID_UA)
            .get()
            .build()
        return try {
            client.newCall(request).execute().use { response -> response.request.url.toString() }
        } catch (_: Throwable) {
            url
        }
    }

    private fun normalizeInput(raw: String): NormalizedInput? {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return null
        if (trimmed.matches(Regex("\\d{15,25}"))) {
            return NormalizedInput(
                url = "https://www.tiktok.com/@i/video/$trimmed",
                idOnly = true,
                videoId = trimmed
            )
        }
        val found = Regex("""https?://[^\s]+""").find(trimmed)?.value
            ?: Regex("""(?:www\.|m\.|vm\.|vt\.)?tiktok\.com/[^\s]+""")
                .find(trimmed)?.value?.let { "https://$it" }
            ?: return null
        val normalized = found.trimEnd('.', ',', ')', ']', '"', '\'')
        val parsed = normalized.toHttpUrlOrNull() ?: return null
        val host = parsed.host.lowercase()
        if (host != "tiktok.com" && !host.endsWith(".tiktok.com")) return null
        val id = extractVideoId(normalized)
        return NormalizedInput(normalized, idOnly = false, videoId = id)
    }

    fun extractVideoId(url: String): String? {
        return Regex("""/video/(\d{15,25})""").find(url)?.groupValues?.getOrNull(1)
            ?: Regex("""/v/(\d{15,25})""").find(url)?.groupValues?.getOrNull(1)
    }

    private fun translateApiError(message: String): String {
        val lower = message.lowercase()
        return when {
            "url" in lower && ("invalid" in lower || "required" in lower) -> "Link TikTok tidak valid."
            "private" in lower -> "Video ini privat atau tidak dapat diakses."
            "region" in lower -> "Video dibatasi untuk wilayah tertentu."
            "not found" in lower || "unavailable" in lower -> "Video tidak ditemukan atau sudah dihapus."
            "limit" in lower || "too many" in lower -> "Batas layanan tercapai. Tunggu sebentar lalu coba lagi."
            else -> message
        }
    }

    private fun friendlyMessage(t: Throwable): String = when (t) {
        is TikTokException -> t.message ?: "Terjadi kesalahan."
        is IOException -> "Koneksi bermasalah. Periksa internet lalu coba lagi."
        else -> t.message ?: "Terjadi kesalahan yang tidak diketahui."
    }

    private companion object {
        const val ANDROID_UA =
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
        const val DESKTOP_UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
        val SHORT_HOSTS = setOf("vm.tiktok.com", "vt.tiktok.com", "m.tiktok.com")
    }
}
