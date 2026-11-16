package com.example.cryptoschool.data

enum class StoryVisualType {
    NOTEBOOK_COMIC,
    COINS_DOLLAR,
    WALLET_VS_BANK,
    RAIL_CRASH_ANIM,
    SEED_SHIELD,
    P2P_ESCROW_ANIM,
    ORDER_BOOK_ANIM,
    CANDLE_ANATOMY,
    VOLUME_TREND_ANIM,
    LEVERAGE_SCALE,
    HEALTH_BAR_LIQ
}

enum class LessonInteractionType {
    NONE,
    EXCHANGE_SCREENSHOT_CLICKER,
    NETWORK_MATCH_TRAINER,
    SEED_SCAM_QUIZ,
    P2P_SCAM_FILTER,
    P2P_CHAT_DILEMMA,
    LIMIT_ORDER_TRAINER,
    DRAW_SUPPORT_LINE,
    VOLUME_FAKEOUT_QUIZ,
    FUTURES_SL_GUARD
}

data class GlossaryTerm(
    val key: String,
    val aliases: List<String>,
    val title: String,
    val humanExplanation: String,
    val analogy: String
)

data class StoryCard(
    val title: String,
    val body: String,
    val visualType: StoryVisualType? = null,
    val interaction: LessonInteractionType = LessonInteractionType.NONE
)

data class LessonLevel(
    val id: String,
    val moduleId: Int,
    val numberInModule: Int,
    val title: String,
    val subtitle: String,
    val xpReward: Int,
    val isBossLevel: Boolean = false,
    val cards: List<StoryCard>
)

data class ModuleIsland(
    val id: Int,
    val title: String,
    val subtitle: String,
    val badgeColorHex: Long,
    val requiresSpotExam: Boolean = false
)

data class CandleBar(
    val index: Int,
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val volume: Float
)

data class HistoricalScenario(
    val id: String,
    val title: String,
    val secretContext: String,
    val hint: String,
    val candles: List<CandleBar>
)

data class TradeLogEntry(
    val scenarioTitle: String,
    val side: String,
    val leverage: Int,
    val pnlUsdt: Float,
    val verdict: String
)

data class Achievement(
    val id: String,
    val title: String,
    val desc: String
)
