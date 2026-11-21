package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.LoanType
import com.example.ui.HisaabDashboardData
import com.example.ui.HisaabViewModel
import com.example.ui.LoanWithPayments
import com.example.ui.PersonLoansSummary
import com.example.ui.components.DatePickerField
import com.example.ui.components.GradientButton
import com.example.ui.components.HisaabCard
import com.example.ui.components.SegmentedToggle

@Composable
fun UdhaarScreen(
    data: HisaabDashboardData,
    isSaving: Boolean,
    onAddLoan: (String, LoanType, Double, String, String?, String, () -> Unit) -> Unit,
    onAddLoanPayment: (String, Double, Double) -> Unit,
    onDeleteLoan: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    var loanType by remember { mutableStateOf(LoanType.LIYA) }
    var personText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf(data.todayDate) }
    var dueDateText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var loanIdToDelete by remember { mutableStateOf<String?>(null) }

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
            item(key = "udhaar_summary_and_form") {
                HisaabCard {
                    Text(
                        text = stringResource(R.string.title_udhaar_hisaab),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SummaryMiniCard(
                            label = stringResource(R.string.label_dena_hai),
                            value = HisaabViewModel.formatRupees(data.totalDenaHai),
                            valueColor = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryMiniCard(
                            label = stringResource(R.string.label_milna_hai),
                            value = HisaabViewModel.formatRupees(data.totalMilnaHai),
                            valueColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SummaryMiniCard(
                            label = stringResource(R.string.label_chuka_diya),
                            value = HisaabViewModel.formatRupees(data.totalChukaDiya),
                            valueColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryMiniCard(
                            label = stringResource(R.string.label_wapas_aaya),
                            value = HisaabViewModel.formatRupees(data.totalWapasAaya),
                            valueColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = data.netLoanSummaryText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    SegmentedToggle(
                        leftLabel = stringResource(R.string.tab_udhaar_liya),
                        rightLabel = stringResource(R.string.tab_udhaar_diya),
                        isLeftSelected = loanType == LoanType.LIYA,
                        onSelectLeft = { loanType = LoanType.LIYA },
                        onSelectRight = { loanType = LoanType.DIYA },
                        leftTag = "toggle_udhaar_liya",
                        rightTag = "toggle_udhaar_diya"
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(
                                    if (loanType == LoanType.LIYA) R.string.label_kisse_liya
                                    else R.string.label_kisko_diya
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = personText,
                                onValueChange = { personText = it.take(40) },
                                placeholder = { Text(stringResource(R.string.hint_person)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
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
                                    .testTag("loan_person_input")
                            )
                        }

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
                                placeholder = { Text("1000") },
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
                                    .testTag("loan_amount_input")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DatePickerField(
                            label = stringResource(R.string.label_date),
                            dateStr = dateText,
                            onDateSelected = { dateText = it },
                            modifier = Modifier.weight(1f)
                        )
                        DatePickerField(
                            label = stringResource(R.string.label_due_date),
                            dateStr = dueDateText,
                            onDateSelected = { dueDateText = it },
                            allowClear = true,
                            placeholder = "Optional",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.label_note),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it.take(60) },
                        placeholder = { Text(stringResource(R.string.hint_loan_note)) },
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
                            .testTag("loan_note_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
                    GradientButton(
                        text = stringResource(
                            if (loanType == LoanType.LIYA) R.string.btn_add_liya
                            else R.string.btn_add_diya
                        ),
                        enabled = !isSaving && personText.isNotBlank() && parsedAmount > 0.0,
                        onClick = {
                            focusManager.clearFocus()
                            onAddLoan(
                                personText,
                                loanType,
                                parsedAmount,
                                dateText,
                                dueDateText.takeIf { it.isNotBlank() },
                                noteText
                            ) {
                                personText = ""
                                amountText = ""
                                dueDateText = ""
                                noteText = ""
                            }
                        },
                        modifier = Modifier.testTag("add_loan_button")
                    )
                }
            }

            item(key = "person_loans_list") {
                HisaabCard {
                    Text(
                        text = stringResource(R.string.title_kiska_baaki),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (data.personSummaries.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Handshake,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.empty_loans),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        data.personSummaries.forEachIndexed { index, summary ->
                            if (index > 0) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            PersonLoanSection(
                                summary = summary,
                                onAddPayment = onAddLoanPayment,
                                onRequestDelete = { loanId -> loanIdToDelete = loanId }
                            )
                        }
                    }
                }
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (loanIdToDelete != null) {
        AlertDialog(
            onDismissRequest = { loanIdToDelete = null },
            title = { Text(stringResource(R.string.delete_loan_title)) },
            text = { Text(stringResource(R.string.delete_loan_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteLoan(loanIdToDelete!!)
                        loanIdToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.confirm_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { loanIdToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SummaryMiniCard(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PersonLoanSection(
    summary: PersonLoansSummary,
    onAddPayment: (String, Double, Double) -> Unit,
    onRequestDelete: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = summary.personName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            val badgeText = when {
                summary.netBalance > 0.0 -> "Milna hai ${HisaabViewModel.formatRupees(summary.netBalance)}"
                summary.netBalance < 0.0 -> "Dena hai ${HisaabViewModel.formatRupees(-summary.netBalance)}"
                else -> "Hisaab saaf ✓"
            }
            val badgeColor = when {
                summary.netBalance > 0.0 -> MaterialTheme.colorScheme.primary
                summary.netBalance < 0.0 -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = badgeColor
            )
        }

        summary.loans.forEach { item ->
            LoanItemCard(
                item = item,
                onAddPayment = onAddPayment,
                onRequestDelete = { onRequestDelete(item.loan.id) }
            )
        }
    }
}

@Composable
private fun LoanItemCard(
    item: LoanWithPayments,
    onAddPayment: (String, Double, Double) -> Unit,
    onRequestDelete: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val loan = item.loan
    val isLiya = loan.direction == LoanType.LIYA
    var payText by remember(loan.id) { mutableStateOf("") }
    val progress = if (loan.amount > 0.0) (item.paidAmount / loan.amount).coerceIn(0.0, 1.0).toFloat() else 0f

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = buildString {
                    append(if (isLiya) "Liya " else "Diya ")
                    append(HisaabViewModel.formatRupees(loan.amount))
                    append(" · ")
                    append(HisaabViewModel.formatDisplayDate(loan.date))
                    if (loan.note.isNotBlank()) {
                        append(" · ")
                        append(loan.note)
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (isLiya) "Chuka diya" else "Wapas aaya"} ${HisaabViewModel.formatRupees(item.paidAmount)} · Baaki ${HisaabViewModel.formatRupees(item.remainingAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }

            if (loan.dueDate.isNotBlank()) {
                Text(
                    text = if (item.isOverdue) {
                        "Der ho gayi, ${HisaabViewModel.formatDisplayDate(loan.dueDate)}"
                    } else {
                        "Kab tak: ${HisaabViewModel.formatDisplayDate(loan.dueDate)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (item.isOverdue) FontWeight.Bold else FontWeight.Normal,
                    color = if (item.isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (item.payments.isNotEmpty()) {
                val history = item.payments.joinToString(", ") { p ->
                    "${HisaabViewModel.formatRupees(p.amount)} (${HisaabViewModel.formatDisplayDate(p.date).take(5)})"
                }
                Text(
                    text = history,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (item.remainingAmount > 0.0) {
                    OutlinedTextField(
                        value = payText,
                        onValueChange = { input -> payText = input.filter { it.isDigit() || it == '.' } },
                        placeholder = {
                            Text(
                                text = if (isLiya) "kitna chukaya" else "kitna wapas mila",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("loan_pay_input_${loan.id}")
                    )
                    val payAmt = payText.toDoubleOrNull() ?: 0.0
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (payAmt > 0.0) {
                                onAddPayment(loan.id, payAmt, item.remainingAmount)
                                payText = ""
                            }
                        },
                        enabled = payAmt > 0.0,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("loan_pay_button_${loan.id}")
                    ) {
                        Text(stringResource(R.string.btn_jodo), fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                IconButton(
                    onClick = onRequestDelete,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("delete_loan_${loan.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.confirm_delete),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
