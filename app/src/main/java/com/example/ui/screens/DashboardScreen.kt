package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CategoryCatalog
import com.example.data.LedgerTransaction
import com.example.data.TransactionType
import com.example.ui.HisaabDashboardData
import com.example.ui.HisaabViewModel
import com.example.ui.MonthlyTrendPoint
import com.example.ui.components.CategoryBreakdownRow
import com.example.ui.components.HeroBalanceCard
import com.example.ui.components.HisaabCard
import kotlin.math.max

@Composable
fun DashboardScreen(
    data: HisaabDashboardData,
    userName: String,
    onShiftMonth: (Int) -> Unit,
    onGoToHisaab: () -> Unit,
    onGoToCharts: () -> Unit,
    onGoToUdhaar: () -> Unit,
    onOpenStatement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstName = remember(userName) {
        userName.trim().substringBefore(" ").takeIf { it.isNotBlank() }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "dash_greeting_and_month") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (firstName != null) "Namaste, $firstName 👋" else "Namaste 👋",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Aapke poore hisaab ki jhalak",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ) {
                            IconButton(
                                onClick = { onShiftMonth(-1) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("dash_prev_month")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Pichhla mahina",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = "${HisaabViewModel.formatShortMonthLabel(data.selectedMonth)} ${data.selectedMonth.take(4)}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(
                                onClick = { onShiftMonth(1) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("dash_next_month")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Agla mahina",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            item(key = "dash_hero") {
                HeroBalanceCard(data = data)
            }

            item(key = "dash_quick_actions") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionPill(
                        icon = Icons.Filled.AddCircleOutline,
                        label = "Kharch Jodo",
                        tint = MaterialTheme.colorScheme.primary,
                        onClick = onGoToHisaab,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dash_action_add")
                    )
                    QuickActionPill(
                        icon = Icons.Filled.Handshake,
                        label = "Udhaar",
                        tint = MaterialTheme.colorScheme.tertiary,
                        onClick = onGoToUdhaar,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dash_action_udhaar")
                    )
                    QuickActionPill(
                        icon = Icons.Filled.Download,
                        label = "Statement",
                        tint = MaterialTheme.colorScheme.secondary,
                        onClick = onOpenStatement,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dash_action_statement")
                    )
                }
            }

            item(key = "dash_kpi_grid") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiTile(
                            icon = Icons.Filled.AccountBalanceWallet,
                            iconTint = MaterialTheme.colorScheme.primary,
                            label = stringResource(R.string.dash_kpi_salary),
                            value = HisaabViewModel.formatRupees(data.monthSalary),
                            subtext = if (data.monthSalary <= 0.0) "Salary set karein" else HisaabViewModel.formatMonthLabel(data.selectedMonth),
                            onClick = onGoToHisaab,
                            modifier = Modifier.weight(1f)
                        )
                        KpiTile(
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            iconTint = Color(0xFF2F9E44),
                            label = stringResource(R.string.dash_kpi_extra),
                            value = "+${HisaabViewModel.formatRupees(data.monthExtraIncome)}",
                            subtext = "Upar se aaya",
                            onClick = onGoToHisaab,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiTile(
                            icon = Icons.Filled.Savings,
                            iconTint = MaterialTheme.colorScheme.secondary,
                            label = stringResource(R.string.dash_kpi_savings),
                            value = "${data.savingsRatePercent}%",
                            subtext = "Cash: ${HisaabViewModel.formatRupees(data.totalCashInHand, showNegativeSign = true)}",
                            onClick = onGoToCharts,
                            modifier = Modifier.weight(1f)
                        )
                        val netUdhaar = data.totalMilnaHai - data.totalDenaHai
                        KpiTile(
                            icon = Icons.Filled.Handshake,
                            iconTint = if (netUdhaar < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            label = "Net Udhaar",
                            value = when {
                                netUdhaar > 0 -> "+${HisaabViewModel.formatRupees(netUdhaar)}"
                                netUdhaar < 0 -> "-${HisaabViewModel.formatRupees(-netUdhaar)}"
                                else -> "₹0"
                            },
                            subtext = when {
                                netUdhaar > 0 -> "Milna hai"
                                netUdhaar < 0 -> "Dena hai"
                                else -> "Hisaab saaf"
                            },
                            onClick = onGoToUdhaar,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (data.overdueLoansCount > 0) {
                item(key = "dash_overdue_alert") {
                    Surface(
                        onClick = onGoToUdhaar,
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WarningAmber,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${data.overdueLoansCount} udhaar ki tareekh nikal gayi hai",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Udhaar tab mein jaakar check karein",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            item(key = "dash_6_month_trend") {
                HisaabCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.dash_trend_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            LegendDot(color = MaterialTheme.colorScheme.primary, label = stringResource(R.string.dash_trend_in))
                            LegendDot(color = MaterialTheme.colorScheme.tertiary, label = stringResource(R.string.dash_trend_out))
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    SixMonthBarChart(
                        points = data.sixMonthTrend,
                        inColor = MaterialTheme.colorScheme.primary,
                        outColor = MaterialTheme.colorScheme.tertiary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            if (data.categoryBreakdowns.isNotEmpty()) {
                item(key = "dash_top_categories") {
                    HisaabCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.dash_top_cats),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(onClick = onGoToCharts) {
                                Text(stringResource(R.string.dash_view_all))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            data.categoryBreakdowns.take(3).forEach { breakdown ->
                                CategoryBreakdownRow(item = breakdown)
                            }
                        }
                    }
                }
            }

            item(key = "dash_recent_transactions") {
                HisaabCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.dash_recent_tx),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(onClick = onGoToHisaab) {
                            Text(stringResource(R.string.dash_view_all))
                        }
                    }
                    if (data.monthTransactions.isEmpty()) {
                        Text(
                            text = stringResource(R.string.empty_transactions),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            data.monthTransactions.take(4).forEach { tx ->
                                RecentTransactionRow(tx = tx)
                            }
                        }
                    }
                }
            }

            item(key = "dash_bottom_spacer") {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun QuickActionPill(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.minimumInteractiveComponentSize()
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun KpiTile(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    subtext: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SixMonthBarChart(
    points: List<MonthlyTrendPoint>,
    inColor: Color,
    outColor: Color,
    trackColor: Color,
) {
    val maxVal = remember(points) {
        max(1.0, points.maxOfOrNull { max(it.inflow, it.expense) } ?: 1.0)
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            if (points.isEmpty()) return@Canvas
            val slotW = size.width / points.size
            val barW = (slotW * 0.24f).coerceIn(8.dp.toPx(), 18.dp.toPx())
            val gap = 4.dp.toPx()
            val chartH = size.height

            points.forEachIndexed { idx, p ->
                val centerX = slotW * idx + slotW / 2f
                val inH = ((p.inflow / maxVal) * (chartH - 8.dp.toPx())).toFloat()
                val outH = ((p.expense / maxVal) * (chartH - 8.dp.toPx())).toFloat()

                // Subtle background track pillars
                drawRoundRect(
                    color = trackColor.copy(alpha = 0.45f),
                    topLeft = Offset(centerX - barW - gap / 2f, 0f),
                    size = Size(barW, chartH),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
                drawRoundRect(
                    color = trackColor.copy(alpha = 0.45f),
                    topLeft = Offset(centerX + gap / 2f, 0f),
                    size = Size(barW, chartH),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                if (inH > 0f) {
                    val h = inH.coerceAtLeast(4.dp.toPx())
                    drawRoundRect(
                        color = inColor,
                        topLeft = Offset(centerX - barW - gap / 2f, chartH - h),
                        size = Size(barW, h),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
                if (outH > 0f) {
                    val h = outH.coerceAtLeast(4.dp.toPx())
                    drawRoundRect(
                        color = outColor,
                        topLeft = Offset(centerX + gap / 2f, chartH - h),
                        size = Size(barW, h),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            points.forEach { p ->
                Text(
                    text = p.shortLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RecentTransactionRow(tx: LedgerTransaction) {
    val info = remember(tx.category, tx.type) {
        CategoryCatalog.infoFor(tx.category, tx.direction)
    }
    val isIn = tx.direction == TransactionType.IN
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(info.color.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = info.emoji, fontSize = 16.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tx.note.ifBlank { tx.category },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = HisaabViewModel.formatDisplayDate(tx.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "${if (isIn) "+" else "-"}${HisaabViewModel.formatRupees(tx.amount)}",
            style = MaterialTheme.typography.titleMedium,
            color = if (isIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
