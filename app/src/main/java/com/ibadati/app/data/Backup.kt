package com.ibadati.app.data

import org.json.JSONArray
import org.json.JSONObject

/** Converts all records to and from a JSON backup file. Book covers are embedded as Base64. */
object Backup {
    private const val FORMAT = "ibadati-backup"
    private const val VERSION = 2

    data class Data(
        val days: List<DayLog>,
        val prayers: List<PrayerLog>,
        val khatmas: List<Khatma>,
        val memorized: List<Memorized> = emptyList(),
        val sinLogs: List<SinLog> = emptyList(),
        val customSins: List<CustomSin> = emptyList(),
        val books: List<Book> = emptyList(),
        val readingLogs: List<ReadingLog> = emptyList(),
        /** Cover file name → Base64 JPEG. */
        val covers: Map<String, String> = emptyMap(),
    )

    private inline fun <T> arr(list: List<T>, f: JSONObject.(T) -> Unit) = JSONArray().apply {
        list.forEach { put(JSONObject().apply { f(it) }) }
    }

    private inline fun <T> JSONObject.list(name: String, f: (JSONObject) -> T?): List<T> {
        val a = optJSONArray(name) ?: return emptyList()
        return (0 until a.length()).mapNotNull { f(a.getJSONObject(it)) }
    }

    fun toJson(d: Data): String {
        val root = JSONObject()
        root.put("format", FORMAT)
        root.put("version", VERSION)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("days", arr(d.days) { x ->
            put("day", x.day); put("witr", x.witr); put("qiyam", x.qiyam); put("athkar", x.athkar)
            put("dua", x.duaMinutes); put("istighfar", x.istighfar); put("salawat", x.salawat); put("kahf", x.kahf)
            put("quranFrom", x.quranFrom); put("quranTo", x.quranTo); put("khatma", x.khatmaId); put("fasted", x.fasted)
        })
        root.put("prayers", arr(d.prayers) { p ->
            put("day", p.day); put("prayer", p.prayer); put("fardh", p.fardh); put("status", p.status)
            put("before", p.sunnahBefore); put("after", p.sunnahAfter)
            put("beforeRakaat", p.beforeRakaat); put("afterRakaat", p.afterRakaat)
        })
        root.put("khatmas", arr(d.khatmas) { k ->
            put("id", k.id); put("start", k.startDay); if (k.endDay != null) put("end", k.endDay)
        })
        root.put("memorized", JSONArray().apply { d.memorized.forEach { put(it.surahId) } })
        root.put("sinLogs", arr(d.sinLogs) { s -> put("day", s.day); put("key", s.sinKey) })
        root.put("customSins", arr(d.customSins) { s -> put("id", s.id); put("name", s.name); put("major", s.major) })
        root.put("books", arr(d.books) { b ->
            put("id", b.id); put("title", b.title); put("author", b.author); put("pages", b.totalPages)
            if (b.cover != null) put("cover", b.cover)
            put("added", b.addedDay); if (b.completedDay != null) put("completed", b.completedDay)
        })
        root.put("readingLogs", arr(d.readingLogs) { r -> put("book", r.bookId); put("day", r.day); put("pages", r.pages) })
        root.put("covers", JSONObject().apply { d.covers.forEach { (k, v) -> put(k, v) } })
        return root.toString()
    }

    fun fromJson(text: String): Data? {
        val root = try { JSONObject(text) } catch (e: Exception) { return null }
        if (root.optString("format") != FORMAT) return null

        val days = root.list("days") { o ->
            DayLog(
                day = o.getLong("day"),
                witr = o.optInt("witr"),
                qiyam = o.optInt("qiyam"),
                athkar = o.optInt("athkar"),
                duaMinutes = o.optInt("dua"),
                istighfar = o.optInt("istighfar"),
                salawat = o.optInt("salawat"),
                kahf = o.optBoolean("kahf"),
                quranFrom = o.optInt("quranFrom"),
                quranTo = o.optInt("quranTo"),
                khatmaId = o.optLong("khatma"),
                fasted = o.optBoolean("fasted"),
            )
        }
        val prayers = root.list("prayers") { o ->
            val idx = o.getInt("prayer")
            if (idx !in 0..4) null else PrayerLog(
                day = o.getLong("day"),
                prayer = idx,
                fardh = o.optBoolean("fardh"),
                status = o.optInt("status"),
                sunnahBefore = o.optBoolean("before"),
                sunnahAfter = o.optBoolean("after"),
                beforeRakaat = o.optInt("beforeRakaat"),
                afterRakaat = o.optInt("afterRakaat"),
            )
        }
        val khatmas = root.list("khatmas") { o ->
            Khatma(o.getLong("id"), o.getLong("start"), if (o.has("end")) o.getLong("end") else null)
        }
        val memorized = root.optJSONArray("memorized")?.let { a ->
            (0 until a.length()).map { Memorized(a.getInt(it)) }.filter { it.surahId in 1..114 }
        } ?: emptyList()
        val sinLogs = root.list("sinLogs") { o -> SinLog(o.getLong("day"), o.getString("key")) }
        val customSins = root.list("customSins") { o -> CustomSin(o.getLong("id"), o.getString("name"), o.optBoolean("major")) }
        val books = root.list("books") { o ->
            Book(
                id = o.getLong("id"),
                title = o.getString("title"),
                author = o.optString("author"),
                totalPages = o.optInt("pages", 1).coerceAtLeast(1),
                cover = if (o.has("cover")) o.getString("cover") else null,
                addedDay = o.optLong("added"),
                completedDay = if (o.has("completed")) o.getLong("completed") else null,
            )
        }
        val readingLogs = root.list("readingLogs") { o -> ReadingLog(o.getLong("book"), o.getLong("day"), o.optInt("pages")) }
        val covers = mutableMapOf<String, String>()
        root.optJSONObject("covers")?.let { c -> c.keys().forEach { k -> covers[k] = c.getString(k) } }
        return Data(days, prayers, khatmas, memorized, sinLogs, customSins, books, readingLogs, covers)
    }
}
