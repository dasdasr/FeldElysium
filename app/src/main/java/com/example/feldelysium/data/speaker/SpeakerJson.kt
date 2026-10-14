package com.example.feldelysium.data.speaker

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.example.feldelysium.domain.model.SpeakerData
import com.example.feldelysium.domain.model.SpeakerDocument
import com.example.feldelysium.domain.model.SpeakerType
import com.example.feldelysium.domain.model.TagData
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream

class PackFormatException(message: String) : Exception(message)

object SpeakerJson {
    const val FORMAT = "feldelysium.speakers"
    const val VERSION = 1

    fun encode(doc: SpeakerDocument): String = JSONObject().apply {
        put("format", FORMAT)
        put("version", VERSION)
        put("tags", JSONArray().apply {
            doc.tags.forEach { t ->
                put(JSONObject().apply {
                    put("id", t.id); put("name", t.name)
                    t.color?.let { put("color", hex(it)) }
                    t.sound?.let { put("sound", it) }
                })
            }
        })
        put("speakers", JSONArray().apply {
            doc.speakers.forEach { s ->
                put(JSONObject().apply {
                    put("id", s.id); put("name", s.name)
                    s.color?.let { put("color", hex(it)) }
                    s.tagId?.let { put("tag", it) }
                    s.icon?.let { put("icon", it) }
                })
            }
        })
    }.toString(2)

    fun decode(text: String, strict: Boolean): SpeakerDocument {
        val o = JSONObject(text)
        if (strict) {
            if (o.optString("format") != FORMAT) throw PackFormatException("Not a speaker pack")
            if (o.optInt("version", 1) > VERSION) throw PackFormatException("Pack was made by a newer app version")
        }
        val tags = o.optJSONArray("tags").objects().mapNotNull { t ->
            val rawId = t.str("id")
            val name = SpeakerType.normalizeName(t.str("name") ?: rawId.orEmpty())
            val id = SpeakerType.slugOf(rawId ?: name)
            if (name.isEmpty() || id == "custom") null
            else TagData(id, name, parseColor(t.str("color")), t.str("sound")?.trim()?.lowercase())
        }.distinctBy { it.id }

        val speakers = o.optJSONArray("speakers").objects().mapNotNull { s ->
            val name = s.str("name")?.trim()?.take(32).orEmpty()
            if (name.isEmpty()) return@mapNotNull null
            val id = s.str("id")?.trim()?.takeIf { it.isNotEmpty() } ?: SpeakerType.slugOf(name)
            val tag = s.str("tag")?.let { SpeakerType.slugOf(it) }?.takeIf { it != "custom" }
            SpeakerData(
                id,
                name,
                parseColor(s.str("color")),
                tag,
                s.str("icon")?.trim()?.takeIf { it.isNotEmpty() })
        }.distinctBy { it.id }

        return SpeakerDocument(tags, speakers)
    }

    fun hex(argb: Long) = "%08X".format(argb and 0xFFFFFFFFL)

    fun parseColor(raw: String?): Long? {
        val h = raw?.trim()?.removePrefix("#") ?: return null
        val v = h.toLongOrNull(16) ?: return null
        return when (h.length) {
            6 -> 0xFF000000L or v
            8 -> v and 0xFFFFFFFFL
            else -> null
        }
    }

    private fun JSONArray?.objects(): List<JSONObject> =
        if (this == null) emptyList() else (0 until length()).mapNotNull { optJSONObject(it) }

    private fun JSONObject.str(key: String): String? =
        if (has(key) && !isNull(key)) getString(key) else null
}

object SpeakerDocumentSerializer : Serializer<SpeakerDocument> {
    override val defaultValue = SpeakerDocument()

    override suspend fun readFrom(input: InputStream): SpeakerDocument = try {
        val text = input.readBytes().decodeToString()
        if (text.isBlank()) defaultValue else SpeakerJson.decode(text, strict = false)
    } catch (e: Exception) {
        throw CorruptionException("Cannot read speakers", e)
    }

    override suspend fun writeTo(t: SpeakerDocument, output: OutputStream) {
        output.write(SpeakerJson.encode(t).toByteArray())
    }
}
