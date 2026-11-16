package com.example.cryptoschool.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cryptoschool.data.CurriculumRepository
import com.example.cryptoschool.data.ProgressStore
import com.example.cryptoschool.data.TradeLogEntry
import kotlinx.coroutines.delay

@Composable
fun SandboxScreen(store: ProgressStore) {
    var sandboxSubTab by remember { mutableStateOf(0) } // 0 = Time Machine, 1 = P2P Deal Simulator

    Column(
        modifier = Modifier.fillMaxSize().background(BgDark).padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(CardDark).padding(4.dp)
        ) {
            listOf("Машина времени (График)", "P2P-Тренажёр (Сделка)").forEachIndexed { idx, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (sandboxSubTab == idx) NeonGreen else Color.Transparent)
                        .clickable { sandboxSubTab = idx }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = if (sandboxSubTab == idx) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        if (sandboxSubTab == 0) {
            TimeMachineTerminal(store)
        } else {
            P2PDealSimulator(store)
        }
    }
}

@Composable
fun TimeMachineTerminal(store: ProgressStore) {
    val scenarios = CurriculumRepository.scenarios
    var scenarioIdx by remember { mutableStateOf(0) }
    val currentScenario = scenarios[scenarioIdx]
    val allCandles = currentScenario.candles

    var visibleCount by remember(scenarioIdx) { mutableStateOf(5) }
    var timeframeLabel by remember { mutableStateOf("15М") }
    var autoPlay by remember { mutableStateOf(false) }

    var balance by remember { mutableStateOf(store.getDemoBalance()) }
    var leverage by remember { mutableStateOf(5f) }
    var stakePercent by remember { mutableStateOf(2f) }
    var stopLossEnabled by remember { mutableStateOf(true) }

    var activePositionSide by remember(scenarioIdx) { mutableStateOf<String?>(null) }
    var entryPrice by remember(scenarioIdx) { mutableStateOf<Float?>(null) }
    var stopLossPrice by remember(scenarioIdx) { mutableStateOf<Float?>(null) }
    var takeProfitPrice by remember(scenarioIdx) { mutableStateOf<Float?>(null) }
    var stakeAmount by remember(scenarioIdx) { mutableStateOf(0f) }
    var coachFeedback by remember(scenarioIdx) { mutableStateOf(currentScenario.hint) }

    val candlesShown = allCandles.take(visibleCount)
    val currentPrice = candlesShown.last().close
    val showCandles = store.isCandlesUnlocked()

    fun stepOneCandleForward() {
        if (visibleCount < allCandles.size) {
            visibleCount++
            val nextCandle = allCandles[visibleCount - 1]
            val pNow = nextCandle.close
            if (activePositionSide != null && entryPrice != null) {
                val isLong = activePositionSide == "LONG"
                val pctMove = if (isLong) (pNow - entryPrice!!) / entryPrice!! else (entryPrice!! - pNow) / entryPrice!!
                val pnl = stakeAmount * leverage * pctMove

                val hitSl = stopLossPrice != null && ((isLong && nextCandle.low <= stopLossPrice!!) || (!isLong && nextCandle.high >= stopLossPrice!!))
                val hitTp = takeProfitPrice != null && ((isLong && nextCandle.high >= takeProfitPrice!!) || (!isLong && nextCandle.low <= takeProfitPrice!!))

                if (pnl <= -stakeAmount) {
                    balance = (balance - stakeAmount).coerceAtLeast(0f)
                    store.setDemoBalance(balance)
                    store.unlockAchievement("first_wreck")
                    val msg = "ЛИКВИДАЦИЯ! С плечом x${leverage.toInt()} движение сожгло ставку ${stakeAmount.toInt()} USDT."
                    coachFeedback = msg
                    store.addTradeLog(TradeLogEntry(currentScenario.title, activePositionSide!!, leverage.toInt(), -stakeAmount, "Ликвидация без стопа"))
                    activePositionSide = null
                    entryPrice = null
                    stopLossPrice = null
                    takeProfitPrice = null
                    autoPlay = false
                } else if (hitSl) {
                    val loss = stakeAmount * leverage * 0.02f
                    balance = (balance - loss).coerceAtLeast(0f)
                    store.setDemoBalance(balance)
                    coachFeedback = "Сработал защитный Stop-Loss (-${loss.toInt()} USDT). Депозит спасён!"
                    store.addTradeLog(TradeLogEntry(currentScenario.title, activePositionSide!!, leverage.toInt(), -loss, "Спасён по Stop-Loss"))
                    activePositionSide = null
                    entryPrice = null
                    stopLossPrice = null
                    takeProfitPrice = null
                    autoPlay = false
                } else if (hitTp) {
                    val win = stakeAmount * leverage * 0.04f
                    balance += win
                    store.setDemoBalance(balance)
                    coachFeedback = "Take-Profit! Зафиксирован профит +${win.toInt()} USDT ✓"
                    store.addTradeLog(TradeLogEntry(currentScenario.title, activePositionSide!!, leverage.toInt(), win, "Закрыт по Take-Profit"))
                    activePositionSide = null
                    entryPrice = null
                    stopLossPrice = null
                    takeProfitPrice = null
                    autoPlay = false
                } else {
                    val sign = if (pnl >= 0) "+" else ""
                    coachFeedback = "Сделка $activePositionSide активна: ${sign}${pnl.toInt()} USDT"
                }
            }
        } else {
            autoPlay = false
            coachFeedback = "Раскрыт секрет дня: ${currentScenario.secretContext}"
        }
    }

    LaunchedEffect(autoPlay, visibleCount, scenarioIdx) {
        if (autoPlay && visibleCount < allCandles.size) {
            delay(900L)
            stepOneCandleForward()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Scenario Switcher + Balance
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                scenarios.forEachIndexed { idx, _ ->
                    FilterChip(
                        selected = scenarioIdx == idx,
                        onClick = {
                            autoPlay = false
                            scenarioIdx = idx
                        },
                        label = { Text("День #${idx + 1}") }
                    )
                }
            }
            Surface(color = CardDark, shape = RoundedCornerShape(12.dp)) {
                Text(
                    "${balance.toInt()} USDT",
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        // Timeframe & Chart Mode Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(currentScenario.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("15М", "1Ч").forEach { tf ->
                    Surface(
                        color = if (timeframeLabel == tf) NeonCyan else CardDark,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { timeframeLabel = tf }
                    ) {
                        Text(
                            tf,
                            color = if (timeframeLabel == tf) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        TradingChartCanvas(
            candles = candlesShown,
            showCandles = showCandles,
            entryPrice = entryPrice,
            stopLossPrice = stopLossPrice,
            takeProfitPrice = takeProfitPrice
        )

        Surface(color = CardDark, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("BTC/USDT ($timeframeLabel): $${currentPrice.toInt()} • Свеча ${visibleCount}/${allCandles.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                Text(coachFeedback, color = AmberGold, fontSize = 12.sp)
            }
        }

        // Time Travel Controls: Step + AutoPlay
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    if (visibleCount < allCandles.size) stepOneCandleForward()
                    else visibleCount = 5
                },
                modifier = Modifier.weight(1.4f).height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text(
                    if (visibleCount < allCandles.size) "След. свеча (+$timeframeLabel) ⏩" else "Сначала ↺",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            OutlinedButton(
                onClick = { autoPlay = !autoPlay },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(if (autoPlay) "Пауза ⏸" else "Авто-ход ▶", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Order Panel
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CardDark).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Плечо: x${leverage.toInt()}  |  Ставка: ${stakePercent.toInt()}% (${(balance * stakePercent / 100f).toInt()} USDT)",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Slider(value = leverage, onValueChange = { leverage = it }, valueRange = 1f..50f)
            Slider(value = stakePercent, onValueChange = { stakePercent = it }, valueRange = 1f..25f)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = stopLossEnabled, onCheckedChange = { stopLossEnabled = it })
                Text("Защитный Stop-Loss (-2%) и Take-Profit (+4%)", color = Color.White, fontSize = 12.sp)
            }

            if (activePositionSide == null) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            activePositionSide = "LONG"
                            entryPrice = currentPrice
                            stakeAmount = (balance * stakePercent / 100f).coerceAtLeast(10f)
                            stopLossPrice = if (stopLossEnabled) currentPrice * 0.98f else null
                            takeProfitPrice = if (stopLossEnabled) currentPrice * 1.04f else null
                            coachFeedback = if (!stopLossEnabled && leverage >= 10f) {
                                "Опасно: открыт ЛОНГ x${leverage.toInt()} без Stop-Loss!"
                            } else {
                                "Открыт ЛОНГ от $${currentPrice.toInt()}. Промотайте время!"
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        Text("ЛОНГ (Рост)", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            activePositionSide = "SHORT"
                            entryPrice = currentPrice
                            stakeAmount = (balance * stakePercent / 100f).coerceAtLeast(10f)
                            stopLossPrice = if (stopLossEnabled) currentPrice * 1.02f else null
                            takeProfitPrice = if (stopLossEnabled) currentPrice * 0.96f else null
                            coachFeedback = "Открыт ШОРТ от $${currentPrice.toInt()}. Промотайте время!"
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
                    ) {
                        Text("ШОРТ (Падение)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(
                    onClick = {
                        val isLong = activePositionSide == "LONG"
                        val pctMove = if (isLong) (currentPrice - entryPrice!!) / entryPrice!! else (entryPrice!! - currentPrice) / entryPrice!!
                        val pnl = stakeAmount * leverage * pctMove
                        balance = (balance + pnl).coerceAtLeast(0f)
                        store.setDemoBalance(balance)
                        val sign = if (pnl >= 0) "+" else ""
                        coachFeedback = "Сделка закрыта по рынку: ${sign}${pnl.toInt()} USDT"
                        store.addTradeLog(TradeLogEntry(currentScenario.title, activePositionSide!!, leverage.toInt(), pnl, "Закрыт вручную"))
                        activePositionSide = null
                        entryPrice = null
                        stopLossPrice = null
                        takeProfitPrice = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                ) {
                    Text("Закрыть сделку по рынку", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun P2PDealSimulator(store: ProgressStore) {
    var step by remember { mutableStateOf(0) }
    var bankBalanceReceived by remember { mutableStateOf(false) }
    var statusBanner by remember { mutableStateOf("Симулятор продажи 100 USDT за 9 650 ₽ на карту") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .clip(RoundedCornerShape(20.dp))
            .background(CardDark)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("P2P-Песочница: Продажа 100 USDT", color = NeonCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(statusBanner, color = AmberGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)

        when (step) {
            0 -> {
                Text(
                    "Покупатель «CryptoBuyer_99» открыл ордер на 9 650 ₽. Биржа заморозила ваши 100 USDT в эскроу. В чате ордера пришло сообщение:",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Surface(color = Color(0x22FF1744), shape = RoundedCornerShape(12.dp)) {
                    Text(
                        "💬 Покупатель: «Брат, я перевёл 9 650 ₽ с карты жены, вот чек receipt_9650.pdf. Нажми отпустить скорее, я спешу!»",
                        color = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Text("Статус вашего банковского приложения: 0 ₽ (поступлений НЕТ).", color = CoralRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Button(
                    onClick = {
                        statusBanner = "ОШИБКА! Вы отпустили 100 USDT по поддельному PDF-чеку от 3-го лица!"
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
                ) {
                    Text("Нажать «Отпустить 100 USDT»", color = Color.White)
                }

                Button(
                    onClick = {
                        step = 1
                        statusBanner = "Верно! Вы отказались верить PDF-чеку и потребовали реального зачисления."
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Text("Не отпускать! Написать: «Жду реального прихода в банк»", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            1 -> {
                Text(
                    "Мошенник слился и отменил заказ! Следом открыл сделку проверенный мерчант «UralExchange ✓» (99.8% сделок).",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Surface(color = Color(0x2200E676), shape = RoundedCornerShape(12.dp)) {
                    Text(
                        "🏦 Уведомление вашего банка: Пополнение +9 650 ₽ от владельца аккаунта. Баланс карты реально увеличился!",
                        color = NeonGreen,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Button(
                    onClick = {
                        bankBalanceReceived = true
                        step = 2
                        store.unlockAchievement("scam_hunter")
                        statusBanner = "Сделка успешно завершена! +9 650 ₽ на карте и 0 риска ✓"
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Text("Проверил баланс в банке → Отпустить 100 USDT", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            else -> {
                Text(
                    "Поздравляем! Вы отбили атаку с фейковым чеком и провели чистую P2P-сделку с мерчантом.",
                    color = NeonGreen,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = {
                        step = 0
                        statusBanner = "Симулятор продажи 100 USDT за 9 650 ₽ на карту"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Пройти P2P-симуляцию ещё раз")
                }
            }
        }
    }
}
