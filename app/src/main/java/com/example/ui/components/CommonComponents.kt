package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.CategoryBreakdown
import com.example.ui.HisaabDashboardData
import com.example.ui.HisaabViewModel
import com.example.ui.theme.ButtonGradientEnd
import com.example.ui.theme.ButtonGradientStart
import com.example.ui.theme.CashGradientEnd
import com.example.ui.theme.CashGradientStart
import com.example.ui.theme.HeroGradientEnd
import com.example.ui.theme.HeroGradientMid
import com.example.ui.theme.HeroGradientStart
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToLong

@Composable
fun HisaabCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (enabled) {
                        Brush.linearGradient(listOf(ButtonGradientStart, ButtonGradientEnd))
                    } else {
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    },
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SegmentedToggle(
    leftLabel: String,
    rightLabel: String,
    isLeftSelected: Boolean,
    onSelectLeft: () -> Unit,
    onSelectRight: () -> Unit,
    leftTag: String,
    rightTag: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SegmentOption(
            label = leftLabel,
            selected = isLeftSelected,
            onClick = onSelectLeft,
            modifier = Modifier
                .weight(1f)
                .testTag(leftTag)
        )
        SegmentOption(
            label = rightLabel,
            selected = !isLeftSelected,
            onClick = onSelectRight,
            modifier = Modifier
                .weight(1f)
                .testTag(rightTag)
        )
    }
}

@Composable
private fun SegmentOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        shadowElevation = if (selected) 3.dp else 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MonthAndSalaryCard(
    selectedMonth: String,
    savedSalary: Double,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSaveSalary: (Double) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var salaryText by remember(selectedMonth) {
        mutableStateOf(
            if (savedSalary > 0.0) savedSalary.roundToLong().toString() else ""
        )
    }

    LaunchedEffect(savedSalary, selectedMonth) {
        val remoteStr = if (savedSalary > 0.0) savedSalary.roundToLong().toString() else ""
        val currentNum = salaryText.toDoubleOrNull() ?: 0.0
        if (currentNum.roundToLong() != savedSalary.roundToLong()) {
            salaryText = remoteStr
        }
    }

    HisaabCard {
        Text(
            text = stringResource(R.string.label_month),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onPrevMonth,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("prev_month_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Pichhla mahina",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = HisaabViewModel.formatMonthLabel(selectedMonth),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("next_month_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Agla mahina",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.label_salary),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = salaryText,
                onValueChange = { input ->
                    val cleaned = input.filter { it.isDigit() }
                    salaryText = cleaned
                },
                placeholder = { Text(stringResource(R.string.hint_salary)) },
                prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onSaveSalary(salaryText.toDoubleOrNull() ?: 0.0)
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("salary_input")
            )
            Surface(
                onClick = {
                    focusManager.clearFocus()
                    onSaveSalary(salaryText.toDoubleOrNull() ?: 0.0)
                },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .height(52.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("save_salary_button")
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Salary save karein",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun HeroBalanceCard(data: HisaabDashboardData) {
    val animatedProgress by animateFloatAsState(
        targetValue = data.spentProgress,
        animationSpec = tween(durationMillis = 450),
        label = "spent_meter"
    )
    val isOver = data.monthRemaining < 0.0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(HeroGradientStart, HeroGradientMid, HeroGradientEnd)
                )
            )
    ) {
        // Decorative highlight circle
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.TopEnd)
                .offset(x = 46.dp, y = (-46).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
        )

        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = stringResource(R.string.label_abhi_bacha),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                color = Color.White.copy(alpha = 0.92f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = HisaabViewModel.formatRupees(data.monthRemaining, showNegativeSign = true),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 34.sp,
                    lineHeight = 38.sp
                ),
                color = if (isOver) Color(0xFFFFD0B0) else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))
            // Progress Meter
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(CircleShape)
                        .background(
                            if (isOver) {
                                Brush.horizontalGradient(listOf(Color(0xFFFF9A62), Color(0xFFFF6B4A)))
                            } else {
                                Brush.horizontalGradient(listOf(Color(0xFFFFE28A), Color.White))
                            }
                        )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stringResource(R.string.label_salary_short)} ${HisaabViewModel.formatRupees(data.monthSalary)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.92f),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${stringResource(R.string.label_upar_se_aaya)} ${HisaabViewModel.formatRupees(data.monthExtraIncome)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.92f),
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stringResource(R.string.label_kharch)} ${HisaabViewModel.formatRupees(data.monthSpent)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.92f),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Kul paise ka ${data.spentPercentInt}% use hua",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.92f),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.16f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${data.smartTipEmoji} ${data.smartTipText}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
fun CashInHandCard(
    totalCashInHand: Double,
    openingCash: Double,
    onSaveOpeningCash: (Double) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var openText by remember {
        mutableStateOf(if (openingCash > 0.0) openingCash.roundToLong().toString() else "")
    }
    LaunchedEffect(openingCash) {
        val remoteStr = if (openingCash > 0.0) openingCash.roundToLong().toString() else ""
        val currentNum = openText.toDoubleOrNull() ?: 0.0
        if (currentNum.roundToLong() != openingCash.roundToLong()) {
            openText = remoteStr
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(colors = listOf(CashGradientStart, CashGradientEnd))
            )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = stringResource(R.string.title_poora_hisaab),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = HisaabViewModel.formatRupees(totalCashInHand, showNegativeSign = true),
                style = MaterialTheme.typography.displayMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.desc_poora_hisaab),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.label_opening_cash),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = openText,
                    onValueChange = { input ->
                        openText = input.filter { it.isDigit() || it == '.' }
                    },
                    placeholder = { Text("0", color = Color.White.copy(alpha = 0.7f)) },
                    prefix = { Text("₹ ", color = Color.White, fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onSaveOpeningCash(openText.toDoubleOrNull() ?: 0.0)
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.2f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.2f),
                        focusedBorderColor = Color.White.copy(alpha = 0.8f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.45f),
                        cursorColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("opening_cash_input")
                )
                Surface(
                    onClick = {
                        focusManager.clearFocus()
                        onSaveOpeningCash(openText.toDoubleOrNull() ?: 0.0)
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.28f),
                    modifier = Modifier
                        .height(52.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("save_opening_cash_button")
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Opening cash save karein",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseDonutChart(
    breakdowns: List<CategoryBreakdown>,
    totalSpent: Double,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(200.dp)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 34.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)

            var startAngle = -90f
            for (slice in breakdowns) {
                val sweep = slice.shareFraction * 360f
                drawArc(
                    color = slice.info.color,
                    startAngle = startAngle,
                    sweepAngle = sweep.coerceAtLeast(1f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
                startAngle += sweep
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.label_kul_kharch),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = HisaabViewModel.formatRupees(totalSpent),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CategoryBreakdownRow(item: CategoryBreakdown) {
    val animatedWidth by animateFloatAsState(
        targetValue = item.barFraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 400),
        label = "cat_bar"
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${item.info.emoji} ${item.info.name}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${HisaabViewModel.formatRupees(item.amount)} · ${item.percentageInt}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedWidth)
                    .clip(CircleShape)
                    .background(item.info.color)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    dateStr: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    allowClear: Boolean = false,
    placeholder: String = "Chunein",
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clickable { showDialog = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (dateStr.isNotBlank()) HisaabViewModel.formatDisplayDate(dateStr) else placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (dateStr.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (allowClear && dateStr.isNotBlank()) {
                    IconButton(
                        onClick = { onDateSelected("") },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Tareekh hatao",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = label,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (showDialog) {
        val initialMillis = remember(dateStr) {
            if (dateStr.isNotBlank()) {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                    sdf.parse(dateStr)?.time
                } catch (_: Exception) {
                    System.currentTimeMillis()
                }
            } else {
                System.currentTimeMillis()
            }
        }
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            onDateSelected(sdf.format(Date(millis)))
                        }
                        showDialog = false
                    }
                ) {
                    Text("Theek hai")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
