package com.example.myapplication

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

const val TRAVELLOG_PREFS = "travellog_prefs"
const val TRIPS_KEY = "trips_json"
const val EXTRA_TRIP_ID = "extra_trip_id"

data class TripItem(
    val id: String,
    val title: String,
    val cityName: String,
    val startDateMillis: Long,
    val endDateMillis: Long,
    val coverImageUri: String?,
    val coverImageUrl: String?,
    val createdAtMillis: Long,
    val diaryEntries: List<DiaryEntry> = emptyList()
)

data class DiaryEntry(
    val text: String
)

fun loadTripsFromLocalStorage(context: Context): List<TripItem> {
    val jsonString = context
        .getSharedPreferences(TRAVELLOG_PREFS, Context.MODE_PRIVATE)
        .getString(TRIPS_KEY, null)
        ?: return emptyList()

    return runCatching {
        val jsonArray = JSONArray(jsonString)
        buildList {
            for (index in 0 until jsonArray.length()) {
                val item = jsonArray.optJSONObject(index) ?: continue
                val diaryArray = item.optJSONArray("diaryEntries") ?: JSONArray()
                val diaries = buildList {
                    for (i in 0 until diaryArray.length()) {
                        val diaryObject = diaryArray.optJSONObject(i)
                        if (diaryObject != null) {
                            add(
                                DiaryEntry(
                                    text = diaryObject.optString("text")
                                )
                            )
                        } else {
                            val legacyText = diaryArray.optString(i)
                            if (legacyText.isNotBlank()) {
                                add(DiaryEntry(text = legacyText))
                            }
                        }
                    }
                }

                add(
                    TripItem(
                        id = item.optString("id"),
                        title = item.optString("title"),
                        cityName = item.optString("cityName"),
                        startDateMillis = item.optLong("startDateMillis"),
                        endDateMillis = item.optLong("endDateMillis"),
                        coverImageUri = item.optString("coverImageUri").ifBlank { null },
                        coverImageUrl = item.optString("coverImageUrl").ifBlank { null },
                        createdAtMillis = item.optLong("createdAtMillis"),
                        diaryEntries = diaries
                    )
                )
            }
        }
    }.getOrElse { emptyList() }
}

fun saveTripsToLocalStorage(context: Context, trips: List<TripItem>) {
    val jsonArray = JSONArray()
    trips.forEach { trip ->
        val diaryArray = JSONArray()
        trip.diaryEntries.forEach { entry ->
            val diaryObject = JSONObject()
                .put("text", entry.text)

            diaryArray.put(diaryObject)
        }

        val tripObject = JSONObject()
            .put("id", trip.id)
            .put("title", trip.title)
            .put("cityName", trip.cityName)
            .put("startDateMillis", trip.startDateMillis)
            .put("endDateMillis", trip.endDateMillis)
            .put("coverImageUri", trip.coverImageUri ?: "")
            .put("coverImageUrl", trip.coverImageUrl ?: "")
            .put("createdAtMillis", trip.createdAtMillis)
            .put("diaryEntries", diaryArray)

        jsonArray.put(tripObject)
    }

    context.getSharedPreferences(TRAVELLOG_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(TRIPS_KEY, jsonArray.toString())
        .apply()
}
