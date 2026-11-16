package com.example.cryptoschool.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cryptoschool.data.*

@Composable
fun RoadmapScreen(
    store: ProgressStore,
    onOpenLesson: (LessonLevel) -> Unit
) {
    val completed = remember(store.getXp()) { store.getCompletedLevels() }
    val spotPassed = store.isSpotExamPassed()
    val allLessons = CurriculumRepository.lessons

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BgDark).padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Карта пути PRO", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    Text("Уроки открываются шаг за шагом, как в Duolingo", color = TextSecondary, fontSize = 12.sp)
                }
                Surface(color = CardDark, shape = RoundedCornerShape(16.dp)) {
                    Text(
                        "${store.getXp()} XP",
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        CurriculumRepository.modules.forEach { mod ->
            item {
                val modLocked = mod.requiresSpotExam && !spotPassed
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardDark)
                        .border(1.dp, Color(mod.badgeColorHex).copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(mod.badgeColorHex)))
                        Spacer(Modifier.width(8.dp))
                        Text(mod.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Text(
                        if (modLocked) "Закрыто: сначала сдайте Экзамен по Споту (Остров 3)" else mod.subtitle,
                        color = if (modLocked) CoralRed else TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    val modLessons = allLessons.filter { it.moduleId == mod.id }
                    modLessons.forEach { lesson ->
                        val globalIndex = allLessons.indexOfFirst { it.id == lesson.id }
                        val prevLesson = if (globalIndex > 0) allLessons[globalIndex - 1] else null
                        val isDone = completed.contains(lesson.id)
                        val isUnlocked = !modLocked && (prevLesson == null || completed.contains(prevLesson.id) || isDone)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isUnlocked) Color(0x16FFFFFF) else Color(0x0AFFFFFF))
                                .clickable(enabled = isUnlocked) { onOpenLesson(lesson) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isDone -> NeonGreen
                                            isUnlocked && lesson.isBossLevel -> AmberGold
                                            isUnlocked -> Color(mod.badgeColorHex)
                                            else -> Color(0x33FFFFFF)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when {
                                        isDone -> Icons.Default.Check
                                        !isUnlocked -> Icons.Default.Lock
                                        lesson.isBossLevel -> Icons.Default.Star
                                        else -> Icons.Default.PlayArrow
                                    },
                                    contentDescription = null,
                                    tint = if (isUnlocked || isDone) Color.Black else TextSecondary
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (lesson.isBossLevel) "БОСС: ${lesson.title}" else lesson.title,
                                    color = if (isUnlocked) Color.White else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(lesson.subtitle, color = TextSecondary, fontSize = 12.sp)
                            }
                            Text("+${lesson.xpReward} XP", color = AmberGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonStoryScreen(
    lesson: LessonLevel,
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    var cardIndex by remember { mutableStateOf(0) }
    val card = lesson.cards[cardIndex]
    var interactionPassed by remember(cardIndex) {
        mutableStateOf(card.interaction == LessonInteractionType.NONE)
    }
    var selectedTerm by remember { mutableStateOf<GlossaryTerm?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().background(BgDark).padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            lesson.cards.forEachIndexed { idx, _ ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (idx <= cardIndex) NeonGreen else Color(0x33FFFFFF))
                )
            }
        }
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${lesson.title} (${cardIndex + 1}/${lesson.cards.size})",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onBack) { Text("Закрыть", color = TextSecondary) }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(CardDark)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(card.title, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)

            // Inline underlined terms right inside the story paragraph!
            HumanClickableStoryText(
                text = card.body,
                onTermClick = { term -> selectedTerm = term }
            )

            Text(
                "💡 Нажмите на любое голубое подчёркнутое слово в тексте для перевода «На человеческом»",
                color = TextSecondary,
                fontSize = 11.sp
            )

            card.visualType?.let { StoryVisualCanvas(it) }

            when (card.interaction) {
                LessonInteractionType.EXCHANGE_SCREENSHOT_CLICKER -> {
                    ExchangeScreenshotClicker(onSuccess = { interactionPassed = true })
                }
                LessonInteractionType.NETWORK_MATCH_TRAINER -> {
                    NetworkMatchMiniTrainer(onPassed = { interactionPassed = true })
                }
                LessonInteractionType.SEED_SCAM_QUIZ -> {
                    SeedScamQuizTrainer(onPassed = { interactionPassed = true })
                }
                LessonInteractionType.P2P_SCAM_FILTER -> {
                    P2PScamFilterTrainer(onPassed = { interactionPassed = true })
                }
                LessonInteractionType.P2P_CHAT_DILEMMA -> {
                    P2PChatDilemmaTrainer(onPassed = { interactionPassed = true })
                }
                LessonInteractionType.LIMIT_ORDER_TRAINER -> {
                    LimitOrderMiniTrainer(onPassed = { interactionPassed = true })
                }
                LessonInteractionType.DRAW_SUPPORT_LINE -> {
                    SupportLineTrainerCanvas(onSuccess = { interactionPassed = true })
                }
                LessonInteractionType.VOLUME_FAKEOUT_QUIZ -> {
                    VolumeFakeoutQuizTrainer(onPassed = { interactionPassed = true })
                }
                LessonInteractionType.FUTURES_SL_GUARD -> {
                    FuturesSlGuardTrainer(onPassed = { interactionPassed = true })
                }
                LessonInteractionType.NONE -> {}
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                if (cardIndex < lesson.cards.lastIndex) {
                    cardIndex++
                } else {
                    onFinish()
                }
            },
            enabled = interactionPassed,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
        ) {
            Text(
                if (cardIndex < lesson.cards.lastIndex) "Дальше" else "Завершить урок (+${lesson.xpReward} XP)",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    selectedTerm?.let { term ->
        ModalBottomSheet(
            onDismissRequest = { selectedTerm = null },
            containerColor = CardDark
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("На человеческом: ${term.title}", color = NeonCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(term.humanExplanation, color = Color.White, fontSize = 15.sp)
                Spacer(Modifier.height(10.dp))
                Surface(color = Color(0x1A00E676), shape = RoundedCornerShape(12.dp)) {
                    Text(
                        "Бытовая аналогия: ${term.analogy}",
                        color = NeonGreen,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun NetworkMatchMiniTrainer(onPassed: () -> Unit) {
    var msg by remember { mutableStateOf("Отправитель: USDT (BEP-20). Выберите сеть получателя:") }
    var ok by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(msg, color = if (ok) NeonGreen else AmberGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        listOf("TRC-20 (Tron)", "BEP-20 (BSC)", "ERC-20 (Ethereum)").forEach { opt ->
            OutlinedButton(
                onClick = {
                    if (opt.startsWith("BEP-20")) {
                        ok = true
                        msg = "Верно! Рельсы BEP-20 = BEP-20 совпали, деньги дойдут за секунды ✓"
                        onPassed()
                    } else {
                        msg = "Опасность! $opt не совпадает с BEP-20 — монеты улетят в трубу!"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(opt, color = Color.White)
            }
        }
    }
}

@Composable
fun SeedScamQuizTrainer(onPassed: () -> Unit) {
    var msg by remember { mutableStateOf("В комментариях под видео кто-то выложил 12 слов от кошелька с 3 500 USDT и просит помочь вывести. Ваши действия?") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(msg, color = AmberGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        OutlinedButton(onClick = {
            msg = "Ловушка! Как только вы закинете туда TRX на комиссию, скрипт мошенника украдёт его за 0.1 сек."
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Зайти в кошелёк и закинуть 20 TRX на газ", color = CoralRed)
        }
        OutlinedButton(onClick = {
            msg = "Абсолютно верно! Это классическая приманка с авто-сборщиком газа ✓"
            onPassed()
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Проигнорировать — это 100% ловушка для кражи комиссии", color = NeonGreen)
        }
    }
}

@Composable
fun VolumeFakeoutQuizTrainer(onPassed: () -> Unit) {
    var msg by remember { mutableStateOf("Цена пробила потолок вверх, но столбики объёма внизу крошечные (почти нулевые). Что это значит?") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(msg, color = AmberGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        OutlinedButton(onClick = {
            msg = "Ошибка! Рост на пустых объёмах — это часто ложный пробой перед разворотом вниз."
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Надо срочно залетать в Лонг на всю котлету", color = CoralRed)
        }
        OutlinedButton(onClick = {
            msg = "Точно! Без крупных объёмов пробой не подтверждён — ждём ✓"
            onPassed()
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Высокий риск ложного пробоя (фейка), не спешим со входом", color = NeonGreen)
        }
    }
}

@Composable
fun P2PScamFilterTrainer(onPassed: () -> Unit) {
    var msg by remember { mutableStateOf("Выберите самый безопасный ордер для покупки USDT:") }
    var ok by remember { mutableStateOf(false) }

    val orders = listOf(
        Triple("FastScam • 89.0₽", "2 сделки • 65% выполнено • без галочки", false),
        Triple("ProMerchant ✓ • 96.5₽", "1 420 сделок • 99.6% выполнено • галочка мерчанта", true),
        Triple("Anonim777 • 92.0₽", "8 сделок • 80% • пишет «оплата на карту третьих лиц»", false)
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(msg, color = if (ok) NeonGreen else Color.White, fontSize = 13.sp)
        orders.forEach { (title, sub, isSafe) ->
            Surface(
                color = Color(0x1AFFFFFF),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().clickable {
                    if (isSafe) {
                        ok = true
                        msg = "Верно! Лучше взять по рыночному курсу у проверенного мерчанта ✓"
                        onPassed()
                    } else {
                        msg = "Опасно! Подозрительно низкий курс и мало сделок — риск скама!"
                    }
                }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(sub, color = TextSecondary, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun P2PChatDilemmaTrainer(onPassed: () -> Unit) {
    var feedback by remember { mutableStateOf("В чат пишут: «Брат, отпусти крипту, деньги щас придут, вот чек в PDF». Ваши действия?") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(feedback, color = AmberGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        OutlinedButton(onClick = {
            feedback = "Критическая ошибка! PDF-чеки рисуют в ботах за 30 секунд."
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Поверить чеку и отпустить USDT", color = CoralRed)
        }
        OutlinedButton(onClick = {
            feedback = "Красава! Пока деньги не пришли на ваш баланс в банке — крипту не отпускаем ✓"
            onPassed()
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Зайти в приложение банка и проверить реальный баланс", color = NeonGreen)
        }
    }
}

@Composable
fun LimitOrderMiniTrainer(onPassed: () -> Unit) {
    var price by remember { mutableStateOf("85000") }
    var amount by remember { mutableStateOf("50") }
    var orderType by remember { mutableStateOf("Market") }
    var msg by remember { mutableStateOf("Задача: Выстави Limit-ордер на покупку BTC по 80000$ на 50 USDT") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(msg, color = AmberGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Market", "Limit").forEach { t ->
                FilterChip(selected = orderType == t, onClick = { orderType = t }, label = { Text(t) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = price, onValueChange = { price = it },
                label = { Text("Цена ($)") }, modifier = Modifier.weight(1f), singleLine = true
            )
            OutlinedTextField(
                value = amount, onValueChange = { amount = it },
                label = { Text("Сумма (USDT)") }, modifier = Modifier.weight(1f), singleLine = true
            )
        }
        Button(onClick = {
            if (orderType == "Limit" && price.trim() == "80000" && amount.trim() == "50") {
                msg = "Идеально! Лимитный ордер выставлен, Спот-экзамен сдан ✓"
                onPassed()
            } else {
                msg = "Проверь: нужен тип Limit, цена 80000 и сумма 50!"
            }
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)) {
            Text("Выставить ордер", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FuturesSlGuardTrainer(onPassed: () -> Unit) {
    var slSet by remember { mutableStateOf(false) }
    var riskPercent by remember { mutableStateOf(10f) }
    var msg by remember { mutableStateOf("Попробуйте открыть Лонг с риском не более 2% и включённым Stop-Loss") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(msg, color = Color.White, fontSize = 12.sp)
        Text("Риск на сделку: ${riskPercent.toInt()}% от банка", color = if (riskPercent <= 2f) NeonGreen else CoralRed, fontWeight = FontWeight.Bold)
        Slider(value = riskPercent, onValueChange = { riskPercent = it }, valueRange = 1f..20f)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = slSet, onCheckedChange = { slSet = it })
            Text("Выставить защитный Stop-Loss", color = Color.White)
        }
        Button(onClick = {
            if (!slSet) {
                msg = "Ошибка! Нельзя входить во фьючерс без Stop-Loss!"
            } else if (riskPercent > 2f) {
                msg = "Ошибка! Риск на сделку больше 2% от банка!"
            } else {
                msg = "Отлично! Защита сработала, риск под контролем ✓"
                onPassed()
            }
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)) {
            Text("Открыть Лонг", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}
