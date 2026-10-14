package com.example.feldelysium.eggs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.eggDataStore by preferencesDataStore(name = "easter_eggs")

class EggStore(private val context: Context) {

    class Snapshot(val discovered: Set<String>, val daily: Map<String, Set<Int>>, val epochDay: Long)

    suspend fun load(today: Long): Snapshot {
        val prefs = context.eggDataStore.data.first()
        val daily = HashMap<String, MutableSet<Int>>()

        prefs[Keys.DAILY].orEmpty().forEach { entry ->
            val p = entry.split('|')

            if (p.size == 3 && p[1].toLongOrNull() == today) {
                p[2].toIntOrNull()?.let {
                    daily.getOrPut(p[0]) { mutableSetOf() } += it
                }
            }
        }

        return Snapshot(
            discovered = prefs[Keys.DISCOVERED].orEmpty(),
            daily = daily,
            epochDay = today,
        )
    }

    suspend fun markDiscovered(id: String) {
        context.eggDataStore.edit { prefs ->
            prefs[Keys.DISCOVERED] = prefs[Keys.DISCOVERED].orEmpty() + id
        }
    }

    suspend fun markShownToday(id: String, replyIndex: Int, today: Long) {
        context.eggDataStore.edit { prefs ->
            val kept = prefs[Keys.DAILY]
                .orEmpty()
                .filter {
                    it.split('|')
                        .getOrNull(1)
                        ?.toLongOrNull() == today
                }
                .toSet()

            prefs[Keys.DAILY] = kept + "$id|$today|$replyIndex"
        }
    }

    private object Keys {
        val DISCOVERED = stringSetPreferencesKey("discovered")
        val DAILY = stringSetPreferencesKey("daily")
    }
}
