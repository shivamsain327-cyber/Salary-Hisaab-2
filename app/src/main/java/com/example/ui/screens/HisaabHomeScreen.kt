package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.data.CategoryCatalog
import com.example.data.LedgerTransaction
import com.example.data.TransactionType
import com.example.ui.HisaabDashboardData
import com.example.ui.HisaabViewModel
import com.example.ui.components.DatePickerField
import com.example.ui.components.GradientButton
import com.example.ui.components.HeroBalanceCard
import com.example.ui.components.HisaabCard
import com.example.ui.components.MonthAndSalaryCard
import com.example.ui.components.SegmentedToggle

@Composable
fun HisaabHomeScreen(
    data: HisaabDashboardData,
    isSaving: Boolean,
    onShiftMonth: (Int) -> Unit,
    onSaveSalary: (Double) -> Unit,
    onAddTransaction: (TransactionType, Double, String, String, String, () -> Unit) -> Unit,
    onDeleteTransaction: (String) -> Unit,
    onOpenStatement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    var txType by remember { mutableStateOf(TransactionType.OUT) }
    val categories = remember(txType) {
        if (txType == TransactionType.OUT) CategoryCatalog.expenseCategories else CategoryCatalog.incomeCategories
    }
    var selectedCategory by remember(txType) { mutableStateOf(categories.first()) }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf(data.todayDate) }
    var showCatMenu by remember { mutableStateOf(false) }

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
            item(key = "subtitle") {
                Text(
                    text = stringResource(R.string.app_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item(key = "month_salary") {
                MonthAndSalaryCard(
                    selectedMonth = data.selectedMonth,
                    savedSalary = data.monthSalary,
                    onPrevMonth = { onShiftMonth(-1) },
                    onNextMonth = { onShiftMonth(1) },
                    onSaveSalary = onSaveSalary
                )
            }

            item(key = "hero") {
                HeroBalanceCard(data = data)
            }

            item(key = "quick_add") {
                HisaabCard {
                    SegmentedToggle(
                        leftLabel = stringResource(R.string.tab_kharch),
                        rightLabel = stringResource(R.string.tab_paise_aaye),
                        isLeftSelected = txType == TransactionType.OUT,
                        onSelectLeft = { txType = TransactionType.OUT },
                        onSelectRight = { txType = TransactionType.IN },
                        leftTag = "toggle_kharch",
                        rightTag = "toggle_paise_aaye"
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.label_amount),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = amountText,
                                onValueChange = { input ->
                                    amountText = input.filter { it.isDigit() || it == '.' }
                                },
                                placeholder = { Text("250") },
                                prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next
                                ),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.background,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("tx_amount_input")
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(
                                    if (txType == TransactionType.IN) R.string.label_category_in
                                    else R.string.label_category_out
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.background,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .clickable { showCatMenu = true }
                                        .testTag("tx_category_picker")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${selectedCategory.emoji} ${selectedCategory.name}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = Icons.Filled.ArrowDropDown,
                                            contentDescription = "Category chunein",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = showCatMenu,
                                    onDismissRequest = { showCatMenu = false }
                                ) {
                                    categories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text("${cat.emoji} ${cat.name}") },
                                            onClick = {
                                                selectedCategory = cat
                                                showCatMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.label_note),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { noteText = it.take(60) },
                                placeholder = { Text(stringResource(R.string.hint_note)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.background,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("tx_note_input")
                            )
                        }

                        DatePickerField(
                            label = stringResource(R.string.label_date),
                            dateStr = dateText,
                            onDateSelected = { dateText = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
                    GradientButton(
                        text = stringResource(
                            if (txType == TransactionType.IN) R.string.btn_add_aamdani
                            else R.string.btn_add_kharch
                        ),
                        enabled = !isSaving && parsedAmount > 0.0,
                        onClick = {
                            focusManager.clearFocus()
                            onAddTransaction(
                                txType,
                                parsedAmount,
                                selectedCategory.name,
                                noteText,
                                dateText
                            ) {
                                amountText = ""
                                noteText = ""
                            }
                        },
                        modifier = Modifier.testTag("add_tx_button")
                    )
                }
            }

            item(key = "list_header") {
                HisaabCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.title_saara_hisaab),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            onClick = onOpenStatement,
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("home_statement_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Download,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(R.string.download_statement),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    if (data.monthTransactions.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.empty_transactions),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            data.monthTransactions.forEach { tx ->
                                TransactionRow(
                                    tx = tx,
                                    onDelete = { onDeleteTransaction(tx.id) }
                                )
                            }
                        }
                    }
                }
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun TransactionRow(
    tx: LedgerTransaction,
    onDelete: () -> Unit,
) {
    val info = remember(tx.category, tx.type) {
        CategoryCatalog.infoFor(tx.category, tx.direction)
    }
    val isIncome = tx.direction == TransactionType.IN

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(info.color.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = info.emoji, fontSize = 20.sp)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tx.note.ifBlank { tx.category },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${tx.category} · ${HisaabViewModel.formatDisplayDate(tx.date)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = "${if (isIncome) "+" else "-"}${HisaabViewModel.formatRupees(tx.amount)}",
            style = MaterialTheme.typography.titleMedium,
            color = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )

        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("delete_tx_${tx.id}")
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.confirm_delete),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
