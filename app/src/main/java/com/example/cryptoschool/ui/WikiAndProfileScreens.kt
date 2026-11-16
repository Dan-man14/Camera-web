package com.example.cryptoschool.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun CheatSheetsScreen() {
    var query by remember { mutableStateOf("") }
    var check1 by remember { mutableStateOf(false) }
    var check2 by remember { mutableStateOf(false) }
    var check3 by remember { mutableStateOf(false) }
    var check4 by remember { mutableStateOf(false) }

    val filtered = CurriculumRepository.glossary.filter {
        query.isBlank() ||
            it.title.contains(query, ignoreCase = true) ||
            it.humanExplanation.contains(query, ignoreCase = true) ||
            it.analogy.contains(query, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BgDark).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Шпаргалки и Словарь", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Поиск (спред, газ, сид, фандинг...)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CardDark).padding(14.dp)
            ) {
                Text("Чек-лист перед переводом крипты (4 пункта)", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = check1, onCheckedChange = { check1 = it })
                    Text("1. Сеть отправителя и получателя совпадают (BEP-20 = BEP-20)", color = Color.White, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = check2, onCheckedChange = { check2 = it })
                    Text("2. Сверены первые 4 и последние 4 символа адреса", color = Color.White, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = check3, onCheckedChange = { check3 = it })
                    Text("3. Для крупной суммы сначала отправлен тест на 1–2$", color = Color.White, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = check4, onCheckedChange = { check4 = it })
                    Text("4. Есть запас монеты сети для оплаты газа (TRX / BNB)", color = Color.White, fontSize = 12.sp)
                }
            }
        }

        items(filtered) { term ->
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(CardDark).padding(14.dp)
            ) {
                Text(term.title, color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))
                Text(term.humanExplanation, color = Color.White, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Text("На человеческом: ${term.analogy}", color = AmberGold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ProfileScreen(store: ProgressStore) {
    val unlocked = store.getUnlockedAchievements()
    val doneCount = store.getCompletedLevels().size
    val totalCount = CurriculumRepository.lessons.size
    val tradeLogs = store.getTradeLogs()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BgDark).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Профиль и статистика", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Ударный режим", "${store.getStreakDays()} дн.", AmberGold, Modifier.weight(1f))
                StatCard("Опыт", "${store.getXp()} XP", NeonGreen, Modifier.weight(1f))
                StatCard("Уроки", "$doneCount/$totalCount", NeonCyan, Modifier.weight(1f))
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CardDark).padding(16.dp)
            ) {
                Text("Демо-счёт Машины времени", color = TextSecondary, fontSize = 12.sp)
                Text("${store.getDemoBalance().toInt()} USDT", color = NeonGreen, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (store.isCandlesUnlocked()) "Статус графика: Японские свечи + Объёмы (Разблокировано ✓)"
                    else "Статус графика: Простая линия новичка (Пройдите Остров 4 для свечей)",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }

        if (tradeLogs.isNotEmpty()) {
            item {
                Text("Последние сделки в Машине времени", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            items(tradeLogs.take(5)) { log ->
                val isWin = log.pnlUsdt >= 0f
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(CardDark).padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${log.side} x${log.leverage} • ${log.verdict}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(log.scenarioTitle, color = TextSecondary, fontSize = 11.sp)
                    }
                    Text(
                        "${if (isWin) "+" else ""}${log.pnlUsdt.toInt()} USDT",
                        color = if (isWin) NeonGreen else CoralRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        item {
            Text("Ачивки (${unlocked.size}/${CurriculumRepository.achievements.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        items(CurriculumRepository.achievements) { ach ->
            val isOpen = unlocked.contains(ach.id)
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(CardDark).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (isOpen) "🏆" else "🔒", fontSize = 22.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(ach.title, color = if (isOpen) AmberGold else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(ach.desc, color = TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(14.dp)).background(CardDark).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = TextSecondary, fontSize = 11.sp)
    }
}
