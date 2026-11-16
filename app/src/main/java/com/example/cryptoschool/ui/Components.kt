package com.example.cryptoschool.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cryptoschool.data.CandleBar
import com.example.cryptoschool.data.CurriculumRepository
import com.example.cryptoschool.data.GlossaryTerm
import com.example.cryptoschool.data.StoryVisualType
import kotlin.math.abs

val BgDark = Color(0xFF090C12)
val CardDark = Color(0xFF141A24)
val NeonGreen = Color(0xFF00E676)
val NeonCyan = Color(0xFF00E5FF)
val CoralRed = Color(0xFFFF1744)
val AmberGold = Color(0xFFFFAB00)
val TextSecondary = Color(0xFF9EA7B8)

@Composable
fun HumanClickableStoryText(
    text: String,
    onTermClick: (GlossaryTerm) -> Unit
) {
    val terms = CurriculumRepository.glossary
    val annotated = remember(text) {
        buildAnnotatedString {
            append(text)
            val lower = text.lowercase()
            for (term in terms) {
                for (alias in term.aliases) {
                    var startIdx = lower.indexOf(alias)
                    while (startIdx >= 0) {
                        val endIdx = startIdx + alias.length
                        addStyle(
                            style = SpanStyle(
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.Underline
                            ),
                            start = startIdx,
                            end = endIdx
                        )
                        addStringAnnotation(
                            tag = "TERM",
                            annotation = term.key,
                            start = startIdx,
                            end = endIdx
                        )
                        startIdx = lower.indexOf(alias, endIdx)
                    }
                }
            }
        }
    }

    ClickableText(
        text = annotated,
        style = TextStyle(
            color = Color(0xFFE6ECF2),
            fontSize = 16.sp,
            lineHeight = 24.sp
        ),
        onClick = { offset ->
            annotated.getStringAnnotations(tag = "TERM", start = offset, end = offset)
                .firstOrNull()?.let { ann ->
                    val found = terms.firstOrNull { it.key == ann.item }
                    if (found != null) onTermClick(found)
                }
        }
    )
}

@Composable
fun ExchangeScreenshotClicker(onSuccess: () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "pulse")
    val radiusAnim by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(tween(750), RepeatMode.Reverse),
        label = "r"
    )
    var clickedCorrectly by remember { mutableStateOf(false) }
    var wrongTapHint by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            if (clickedCorrectly) "Отлично! Мышечная память закреплена ✓"
            else "Задание: нажмите на пульсирующее поле «Сеть перевода» на экране биржи:",
            color = if (clickedCorrectly) NeonGreen else AmberGold,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        // Simulated darkened Bybit/MEXC Withdrawal Screen
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0A0E16))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Вывод USDT (Bybit / MEXC)", color = Color(0x66FFFFFF), fontSize = 12.sp, fontWeight = FontWeight.Bold)

            // Dimmed Address field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x14FFFFFF))
                    .clickable { wrongTapHint = true }
                    .padding(12.dp)
            ) {
                Text("Адрес кошелька: 0x71C...89A (Не трогать)", color = Color(0x55FFFFFF), fontSize = 12.sp)
            }

            // Highlighted Target: Network Selection Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (clickedCorrectly) Color(0x3300E676) else Color(0x2200E5FF))
                    .border(
                        width = (2f * radiusAnim).dp,
                        color = if (clickedCorrectly) NeonGreen else NeonCyan,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        clickedCorrectly = true
                        wrongTapHint = false
                        onSuccess()
                    }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Сеть перевода (Chain Type)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            if (clickedCorrectly) "Выбрано: BSC (BEP-20) • Комиссия 0$" else "Нажмите сюда, чтобы выбрать сеть!",
                            color = if (clickedCorrectly) NeonGreen else NeonCyan,
                            fontSize = 11.sp
                        )
                    }
                    Text("👆", fontSize = 20.sp)
                }
            }

            // Dimmed Amount field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x14FFFFFF))
                    .clickable { wrongTapHint = true }
                    .padding(12.dp)
            ) {
                Text("Сумма вывода: 100 USDT", color = Color(0x55FFFFFF), fontSize = 12.sp)
            }
        }

        if (wrongTapHint && !clickedCorrectly) {
            Text("Мимо! Нажмите именно на подсвеченное голубой рамкой поле «Сеть перевода».", color = CoralRed, fontSize = 12.sp)
        }
    }
}

@Composable
fun StoryVisualCanvas(type: StoryVisualType) {
    val infinite = rememberInfiniteTransition(label = "vis")
    val pulse by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label = "p"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(165.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0E131D))
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        when (type) {
            StoryVisualType.RAIL_CRASH_ANIM -> {
                Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    val w = size.width
                    val h = size.height
                    drawLine(NeonGreen, Offset(20f, h * 0.25f), Offset(w - 20f, h * 0.25f), strokeWidth = 6f)
                    drawCircle(NeonGreen, radius = 13f, center = Offset(20f + (w - 40f) * pulse, h * 0.25f))

                    drawLine(AmberGold, Offset(20f, h * 0.65f), Offset(w * 0.52f, h * 0.65f), strokeWidth = 6f)
                    drawLine(CoralRed, Offset(w * 0.68f, h * 0.65f), Offset(w - 20f, h * 0.65f), strokeWidth = 6f)
                    val cx = 20f + (w * 0.60f) * pulse
                    val cy = if (cx < w * 0.52f) h * 0.65f else h * 0.65f + (cx - w * 0.52f) * 1.4f
                    drawCircle(CoralRed, radius = 13f, center = Offset(cx, cy.coerceAtMost(h - 10f)))
                }
                Text(
                    "BEP-20 → BEP-20 (Доехал)  |  BEP-20 → TRC-20 (В трубу!)",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(6.dp)
                )
            }

            StoryVisualType.ORDER_BOOK_ANIM -> {
                Row(modifier = Modifier.fillMaxSize().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.SpaceEvenly) {
                        Text("Покупатели (Биды)", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        listOf(0.85f, 0.65f, 0.45f).forEachIndexed { i, f ->
                            val animW = (f + 0.1f * ((pulse + i * 0.3f) % 1f)).coerceAtMost(1f)
                            Box(modifier = Modifier.fillMaxWidth(animW).height(18.dp).background(Color(0x4400E676), RoundedCornerShape(4.dp)))
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.SpaceEvenly) {
                        Text("Продавцы (Аски)", color = CoralRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        listOf(0.50f, 0.72f, 0.90f).forEachIndexed { i, f ->
                            val animW = (f - 0.1f * ((pulse + i * 0.2f) % 1f)).coerceAtLeast(0.2f)
                            Box(modifier = Modifier.fillMaxWidth(animW).height(18.dp).background(Color(0x44FF1744), RoundedCornerShape(4.dp)))
                        }
                    }
                }
            }

            StoryVisualType.CANDLE_ANATOMY -> {
                Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    val cx = size.width * 0.28f
                    drawLine(NeonGreen, Offset(cx, 16f), Offset(cx, size.height - 16f), strokeWidth = 5f)
                    drawRoundRect(
                        color = NeonGreen,
                        topLeft = Offset(cx - 24f, size.height * 0.28f),
                        size = Size(48f, size.height * 0.44f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }
                Column(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 18.dp)) {
                    Text("• Верхняя тень = Макс. цена", color = Color.White, fontSize = 12.sp)
                    Text("• Тело = Открытие / Закрытие", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("• Нижняя тень = Мин. цена", color = Color.White, fontSize = 12.sp)
                }
            }

            StoryVisualType.VOLUME_TREND_ANIM -> {
                Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    val bars = listOf(0.25f, 0.30f, 0.22f, 0.85f, 0.95f, 0.78f)
                    val step = size.width / bars.size
                    bars.forEachIndexed { i, b ->
                        val col = if (i >= 3) NeonGreen else TextSecondary
                        val bh = b * size.height * 0.75f
                        drawRoundRect(
                            color = col,
                            topLeft = Offset(i * step + step * 0.2f, size.height - bh),
                            size = Size(step * 0.6f, bh),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                }
                Text(
                    "Высокие зелёные столбики объёма = в рынок зашли крупные деньги",
                    color = NeonGreen,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
                )
            }

            StoryVisualType.HEALTH_BAR_LIQ -> {
                val hp = (1f - pulse).coerceIn(0.05f, 1f)
                Column(modifier = Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Здоровье маржи: ${(hp * 100).toInt()}%", color = if (hp < 0.3f) CoralRed else Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(10.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(22.dp).clip(RoundedCornerShape(11.dp)).background(Color(0x33FFFFFF))) {
                        Box(modifier = Modifier.fillMaxWidth(hp).fillMaxHeight().background(if (hp < 0.3f) CoralRed else NeonGreen))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(if (hp < 0.15f) "ЛИКВИДАЦИЯ! Депозит сгорел" else "Без Stop-Loss цена съедает залог до нуля", color = TextSecondary, fontSize = 11.sp)
                }
            }

            else -> {
                val label = when (type) {
                    StoryVisualType.NOTEBOOK_COMIC -> "Комикс: 1 тетрадь у 50 соседей = нельзя подделать перевод"
                    StoryVisualType.COINS_DOLLAR -> "Bitcoin: скачет +-10%  |  USDT: железно 1.00 $"
                    StoryVisualType.WALLET_VS_BANK -> "Биржа = Удобный банк  |  Кошелёк = Сейф с 12 словами"
                    StoryVisualType.SEED_SHIELD -> "Seed-фраза (12 слов): Только бумага, никаких скриншотов!"
                    StoryVisualType.P2P_ESCROW_ANIM -> "Эскроу-гарант: Биржа держит USDT под замком до оплаты"
                    StoryVisualType.LEVERAGE_SCALE -> "Весы плеча: x50 даёт ликвидацию при -2% движения цены!"
                    else -> ""
                }
                Text(
                    text = label,
                    color = NeonCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(18.dp)
                )
            }
        }
    }
}

@Composable
fun SupportLineTrainerCanvas(onSuccess: () -> Unit) {
    var userYFraction by remember { mutableStateOf(0.25f) }
    var checked by remember { mutableStateOf(false) }
    var passed by remember { mutableStateOf(false) }
    val points = listOf(0.30f, 0.55f, 0.72f, 0.45f, 0.71f, 0.35f, 0.73f, 0.25f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Проведите пальцем по графику и поставьте линию «пола» (поддержки):", color = Color.White, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0E131D))
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        userYFraction = (change.position.y / size.height).coerceIn(0.1f, 0.9f)
                        checked = false
                    }
                }
        ) {
            val stepX = size.width / (points.size - 1)
            val path = Path()
            points.forEachIndexed { idx, py ->
                val x = idx * stepX
                val y = py * size.height
                if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color = NeonCyan, style = Stroke(width = 5f))

            val ly = userYFraction * size.height
            drawLine(
                color = if (passed) NeonGreen else AmberGold,
                start = Offset(0f, ly),
                end = Offset(size.width, ly),
                strokeWidth = 6f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f))
            )
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                checked = true
                if (abs(userYFraction - 0.72f) <= 0.09f) {
                    passed = true
                    onSuccess()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = if (passed) NeonGreen else NeonCyan)
        ) {
            Text(
                if (passed) "Точно в яблочко! Пол найден ✓" else "Проверить уровень",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
        if (checked && !passed) {
            Text("Опустите линию туда, откуда график трижды отскакивал наверх!", color = CoralRed, fontSize = 12.sp)
        }
    }
}

@Composable
fun TradingChartCanvas(
    candles: List<CandleBar>,
    showCandles: Boolean,
    entryPrice: Float?,
    stopLossPrice: Float?,
    takeProfitPrice: Float?
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0E131D))
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        if (candles.isEmpty()) return@Canvas
        val minP = candles.minOf { it.low } * 0.995f
        val maxP = candles.maxOf { it.high } * 1.005f
        val range = (maxP - minP).coerceAtLeast(1f)

        fun yFor(price: Float): Float = size.height - ((price - minP) / range) * size.height * 0.85f - size.height * 0.05f

        val stepX = size.width / candles.size.coerceAtLeast(1)

        if (!showCandles) {
            val path = Path()
            candles.forEachIndexed { idx, c ->
                val x = idx * stepX + stepX / 2f
                val y = yFor(c.close)
                if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color = NeonCyan, style = Stroke(width = 5f))
        } else {
            val maxVol = candles.maxOf { it.volume }.coerceAtLeast(1f)
            candles.forEachIndexed { idx, c ->
                val cx = idx * stepX + stepX / 2f
                val isGreen = c.close >= c.open
                val col = if (isGreen) NeonGreen else CoralRed

                val vh = (c.volume / maxVol) * (size.height * 0.16f)
                drawRect(
                    color = col.copy(alpha = 0.28f),
                    topLeft = Offset(cx - stepX * 0.25f, size.height - vh),
                    size = Size(stepX * 0.5f, vh)
                )

                drawLine(col, Offset(cx, yFor(c.high)), Offset(cx, yFor(c.low)), strokeWidth = 3f)

                val topY = yFor(maxOf(c.open, c.close))
                val botY = yFor(minOf(c.open, c.close))
                drawRect(
                    color = col,
                    topLeft = Offset(cx - stepX * 0.28f, topY),
                    size = Size(stepX * 0.56f, (botY - topY).coerceAtLeast(4f))
                )
            }
        }

        entryPrice?.let {
            val y = yFor(it).coerceIn(0f, size.height)
            drawLine(AmberGold, Offset(0f, y), Offset(size.width, y), strokeWidth = 3f)
        }
        stopLossPrice?.let {
            val y = yFor(it).coerceIn(0f, size.height)
            drawLine(CoralRed, Offset(0f, y), Offset(size.width, y), strokeWidth = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)))
        }
        takeProfitPrice?.let {
            val y = yFor(it).coerceIn(0f, size.height)
            drawLine(NeonGreen, Offset(0f, y), Offset(size.width, y), strokeWidth = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)))
        }
    }
}
