package com.example.cryptoschool.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class ProgressStore(context: Context) {

    private val prefs = context.getSharedPreferences("cryptoschool_pro_v2", Context.MODE_PRIVATE)

    fun getCompletedLevels(): Set<String> {
        val raw = prefs.getString("completed_levels", "[]") ?: "[]"
        val set = mutableSetOf<String>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) set.add(arr.getString(i))
        } catch (_: Exception) {
        }
        return set
    }

    fun markLevelCompleted(levelId: String, xpGain: Int) {
        val set = getCompletedLevels().toMutableSet()
        val isNew = set.add(levelId)
        val arr = JSONArray()
        set.forEach { arr.put(it) }
        val newXp = getXp() + if (isNew) xpGain else 15
        prefs.edit()
            .putString("completed_levels", arr.toString())
            .putInt("xp", newXp)
            .apply()

        if (levelId == "m1_l3") unlockAchievement("rail_master")
        if (levelId == "m1_l4") unlockAchievement("seed_guardian")
        if (levelId == "m2_l2") unlockAchievement("scam_hunter")
        if (levelId == "m3_l3") unlockAchievement("spot_graduate")
        if (levelId == "m4_l2") unlockAchievement("chart_artist")
        if (levelId == "m5_l3") unlockAchievement("futures_survivor")
        bumpStreakIfNeeded()
    }

    fun getXp(): Int = prefs.getInt("xp", 0)

    fun getStreakDays(): Int = prefs.getInt("streak_days", 1)

    private fun bumpStreakIfNeeded() {
        val lastDay = prefs.getLong("last_day", 0L)
        val today = System.currentTimeMillis() / 86_400_000L
        if (lastDay == 0L) {
            prefs.edit().putLong("last_day", today).putInt("streak_days", 1).apply()
        } else if (today > lastDay) {
            val cur = prefs.getInt("streak_days", 1)
            prefs.edit().putLong("last_day", today).putInt("streak_days", cur + 1).apply()
        }
    }

    fun getDemoBalance(): Float = prefs.getFloat("demo_usdt", 1000f)

    fun setDemoBalance(value: Float) {
        prefs.edit().putFloat("demo_usdt", value.coerceAtLeast(0f)).apply()
    }

    fun resetDemoBalance() {
        prefs.edit().putFloat("demo_usdt", 1000f).apply()
    }

    fun addTradeLog(entry: TradeLogEntry) {
        val current = getTradeLogs().toMutableList()
        current.add(0, entry)
        val trimmed = current.take(15)
        val arr = JSONArray()
        trimmed.forEach {
            val o = JSONObject()
            o.put("sc", it.scenarioTitle)
            o.put("side", it.side)
            o.put("lev", it.leverage)
            o.put("pnl", it.pnlUsdt.toDouble())
            o.put("verdict", it.verdict)
            arr.put(o)
        }
        prefs.edit().putString("trade_logs", arr.toString()).apply()
    }

    fun getTradeLogs(): List<TradeLogEntry> {
        val raw = prefs.getString("trade_logs", "[]") ?: "[]"
        val list = mutableListOf<TradeLogEntry>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    TradeLogEntry(
                        o.getString("sc"),
                        o.getString("side"),
                        o.getInt("lev"),
                        o.getDouble("pnl").toFloat(),
                        o.getString("verdict")
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }

    fun getUnlockedAchievements(): Set<String> {
        val raw = prefs.getString("achievements", "[]") ?: "[]"
        val set = mutableSetOf<String>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) set.add(arr.getString(i))
        } catch (_: Exception) {
        }
        return set
    }

    fun unlockAchievement(id: String) {
        val set = getUnlockedAchievements().toMutableSet()
        if (set.add(id)) {
            val arr = JSONArray()
            set.forEach { arr.put(it) }
            prefs.edit().putString("achievements", arr.toString()).apply()
        }
    }

    fun isCandlesUnlocked(): Boolean {
        val done = getCompletedLevels()
        return done.contains("m4_l1") || done.size >= 8
    }

    fun isSpotExamPassed(): Boolean {
        val done = getCompletedLevels()
        return done.contains("m3_l3")
    }
}
