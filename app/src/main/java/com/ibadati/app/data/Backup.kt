package com.ibadati.app.data

import org.json.JSONArray
import org.json.JSONObject

/** Converts all records to and from a JSON backup file. */
object Backup {
    private const val FORMAT = "ibadati-backup"
    private const val VERSION = 1

    data class Data(val days: List<DayLog>, val prayers: List<PrayerLog>, val khatmas: List<Khatma>)

    fun toJson(days: List<DayLog>, prayers: List<PrayerLog>, khatmas: List<Khatma>): String {
        val root = JSONObject()
        root.put("format", FORMAT)
        root.put("version", VERSION)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("days", JSONArray().apply {
            days.forEach { d ->
                put(JSONObject().apply {
                    put("day", d.day)
                    put("witr", d.witr)
                    put("qiyam", d.qiyam)
                    put("athkar", d.athkar)
                    put("dua", d.duaMinutes)
                    put("istighfar", d.istighfar)
                    put("salawat", d.salawat)
                    put("kahf", d.kahf)
                    put("quranFrom", d.quranFrom)
                    put("quranTo", d.quranTo)
                    put("khatma", d.khatmaId)
                })
            }
        })
        root.put("prayers", JSONArray().apply {
            prayers.forEach { p ->
                put(JSONObject().apply {
                    put("day", p.day)
                    put("prayer", p.prayer)
                    put("fardh", p.fardh)
                    put("status", p.status)
                    put("before", p.sunnahBefore)
                    put("after", p.sunnahAfter)
                    put("beforeRakaat", p.beforeRakaat)
                    put("afterRakaat", p.afterRakaat)
                })
            }
        })
        root.put("khatmas", JSONArray().apply {
            khatmas.forEach { k ->
                put(JSONObject().apply {
                    put("id", k.id)
                    put("start", k.startDay)
                    if (k.endDay != null) put("end", k.endDay)
                })
            }
        })
        return root.toString(2)
    }

    fun fromJson(text: String): Data? {
        val root = try { JSONObject(text) } catch (e: Exception) { return null }
        if (root.optString("format") != FORMAT) return null

        val days = mutableListOf<DayLog>()
        root.optJSONArray("days")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                days += DayLog(
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
                )
            }
        }
        val prayers = mutableListOf<PrayerLog>()
        root.optJSONArray("prayers")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val idx = o.getInt("prayer")
                if (idx !in 0..4) continue
                prayers += PrayerLog(
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
        }
        val khatmas = mutableListOf<Khatma>()
        root.optJSONArray("khatmas")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                khatmas += Khatma(
                    id = o.getLong("id"),
                    startDay = o.getLong("start"),
                    endDay = if (o.has("end")) o.getLong("end") else null,
                )
            }
        }
        return Data(days, prayers, khatmas)
    }
}
