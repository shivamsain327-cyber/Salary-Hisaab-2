package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CategoryCatalog
import com.example.data.CategoryInfo
import com.example.data.HisaabRepository
import com.example.data.LedgerTransaction
import com.example.data.LoanPayment
import com.example.data.LoanType
import com.example.data.MonthlySalary
import com.example.data.TransactionType
import com.example.data.UdhaarLoan
import com.example.data.UserLedgerSettings
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class CategoryBreakdown(
    val info: CategoryInfo,
    val amount: Double,
    val shareFraction: Float,
    val barFraction: Float,
    val percentageInt: Int,
)

data class LoanWithPayments(
    val loan: UdhaarLoan,
    val payments: List<LoanPayment>,
    val paidAmount: Double,
    val remainingAmount: Double,
    val isOverdue: Boolean,
)

data class PersonLoansSummary(
    val personName: String,
    val netBalance: Double, // > 0 means Milna hai, < 0 means Dena hai
    val loans: List<LoanWithPayments>,
)

data class MonthlyTrendPoint(
    val monthKey: String,
    val shortLabel: String,
    val inflow: Double,
    val expense: Double,
)

data class HisaabDashboardData(
    val selectedMonth: String,
    val todayDate: String,
    val monthSalary: Double,
    val monthExtraIncome: Double,
    val monthSpent: Double,
    val monthTotalAvailable: Double,
    val monthRemaining: Double,
    val spentProgress: Float,
    val spentPercentInt: Int,
    val savingsRatePercent: Int,
    val smartTipEmoji: String,
    val smartTipText: String,
    val monthTransactions: List<LedgerTransaction>,
    val categoryBreakdowns: List<CategoryBreakdown>,
    val sixMonthTrend: List<MonthlyTrendPoint>,
    val openingCash: Double,
    val totalCashInHand: Double,
    val totalDenaHai: Double,
    val totalMilnaHai: Double,
    val totalChukaDiya: Double,
    val totalWapasAaya: Double,
    val overdueLoansCount: Int,
    val netLoanSummaryText: String,
    val personSummaries: List<PersonLoansSummary>,
)

private data class RawLedgerStreams(
    val settings: UserLedgerSettings?,
    val salaries: List<MonthlySalary>,
    val transactions: List<LedgerTransaction>,
    val loans: List<UdhaarLoan>,
    val payments: List<LoanPayment>,
)

class HisaabViewModel(
    private val repository: HisaabRepository,
    private val currentUserId: String,
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(currentMonthKey())
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private fun <T> Flow<T>.unwrapFirestoreErrors(label: String): Flow<Result<T>> =
        map { Result.success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing $label", rootError)
                emit(Result.failure(rootError))
            }

    private val rawStreamsFlow: Flow<Result<RawLedgerStreams>> = combine(
        repository.observeSettings(currentUserId).unwrapFirestoreErrors("settings"),
        repository.observeSalaries(currentUserId).unwrapFirestoreErrors("salaries"),
        repository.observeTransactions(currentUserId).unwrapFirestoreErrors("transactions"),
        repository.observeLoans(currentUserId).unwrapFirestoreErrors("loans"),
        repository.observeLoanPayments(currentUserId).unwrapFirestoreErrors("loanPayments"),
    ) { settingsRes, salariesRes, txRes, loansRes, paysRes ->
        val firstFailure = settingsRes.exceptionOrNull()
            ?: salariesRes.exceptionOrNull()
            ?: txRes.exceptionOrNull()
            ?: loansRes.exceptionOrNull()
            ?: paysRes.exceptionOrNull()
        if (firstFailure != null) {
            Result.failure(firstFailure)
        } else {
            Result.success(
                RawLedgerStreams(
                    settings = settingsRes.getOrNull(),
                    salaries = salariesRes.getOrNull().orEmpty(),
                    transactions = txRes.getOrNull().orEmpty(),
                    loans = loansRes.getOrNull().orEmpty(),
                    payments = paysRes.getOrNull().orEmpty(),
                )
            )
        }
    }

    val uiState: StateFlow<UiState<HisaabDashboardData>> = combine(
        rawStreamsFlow,
        _selectedMonth,
    ) { streamsResult, month ->
        streamsResult.fold(
            onSuccess = { raw ->
                UiState.Success(computeDashboard(raw, month))
            },
            onFailure = { err ->
                UiState.Error(err.localizedMessage ?: "Data load nahi ho paaya")
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading
    )

    fun selectMonth(monthKey: String) {
        _selectedMonth.value = monthKey
    }

    fun shiftMonth(deltaMonths: Int) {
        _selectedMonth.value = addMonths(_selectedMonth.value, deltaMonths)
    }

    fun saveMonthlySalary(monthKey: String, amount: Double) {
        viewModelScope.launch {
            val res = repository.setMonthlySalary(monthKey, amount)
            if (res.isFailure) {
                _events.emit("Salary save nahi hui: ${res.exceptionOrNull()?.localizedMessage ?: "error"}")
            }
        }
    }

    fun saveOpeningCash(amount: Double) {
        viewModelScope.launch {
            val res = repository.setOpeningCash(amount)
            if (res.isFailure) {
                _events.emit("Opening cash save nahi hua: ${res.exceptionOrNull()?.localizedMessage ?: "error"}")
            }
        }
    }

    fun addTransaction(
        type: TransactionType,
        amount: Double,
        category: String,
        note: String,
        date: String,
        onAdded: () -> Unit = {},
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val res = repository.addTransaction(type, amount, category, note, date)
            _isSaving.value = false
            if (res.isSuccess) {
                val txMonth = date.take(7)
                if (txMonth.length == 7 && txMonth != _selectedMonth.value) {
                    _selectedMonth.value = txMonth
                }
                _events.emit(if (type == TransactionType.IN) "Aamdani jodi gayi" else "Kharch joda gaya")
                onAdded()
            } else {
                _events.emit("Entry save nahi hui: ${res.exceptionOrNull()?.localizedMessage ?: "error"}")
            }
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            val res = repository.deleteTransaction(id)
            if (res.isSuccess) {
                _events.emit("Hisaab se hata diya gaya")
            } else {
                _events.emit("Hata nahi paaye: ${res.exceptionOrNull()?.localizedMessage ?: "error"}")
            }
        }
    }

    fun addLoan(
        person: String,
        type: LoanType,
        amount: Double,
        date: String,
        dueDate: String?,
        note: String,
        onAdded: () -> Unit = {},
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val res = repository.addLoan(person, type, amount, date, dueDate, note)
            _isSaving.value = false
            if (res.isSuccess) {
                _events.emit("Udhaar joda gaya")
                onAdded()
            } else {
                _events.emit("Udhaar save nahi hua: ${res.exceptionOrNull()?.localizedMessage ?: "error"}")
            }
        }
    }

    fun addLoanPayment(loanId: String, amount: Double, maxRemaining: Double) {
        viewModelScope.launch {
            val clamped = min(amount, maxRemaining)
            if (clamped <= 0.0) return@launch
            val res = repository.addLoanPayment(loanId, clamped, currentDateStr())
            if (res.isSuccess) {
                _events.emit("Bhugtaan joda gaya")
            } else {
                _events.emit("Bhugtaan save nahi hua: ${res.exceptionOrNull()?.localizedMessage ?: "error"}")
            }
        }
    }

    fun deleteLoan(loanId: String) {
        viewModelScope.launch {
            val res = repository.deleteLoanAndPayments(loanId)
            if (res.isSuccess) {
                _events.emit("Udhaar hata diya gaya")
            } else {
                _events.emit("Udhaar hata nahi paaye: ${res.exceptionOrNull()?.localizedMessage ?: "error"}")
            }
        }
    }

    private fun computeDashboard(raw: RawLedgerStreams, month: String): HisaabDashboardData {
        val today = currentDateStr()
        val salaryMap = raw.salaries.associate { it.monthKey to it.amount }
        val monthSalary = salaryMap[month] ?: 0.0

        val monthTx = raw.transactions
            .filter { it.monthKey == month || it.date.startsWith(month) }
            .sortedWith(
                compareByDescending<LedgerTransaction> { it.date }
                    .thenByDescending { it.createdAt?.seconds ?: 0L }
                    .thenByDescending { it.createdAt?.nanoseconds ?: 0 }
            )

        val monthExpenses = monthTx.filter { it.direction == TransactionType.OUT }
        val monthIncome = monthTx.filter { it.direction == TransactionType.IN }.sumOf { it.amount }
        val monthSpent = monthExpenses.sumOf { it.amount }
        val monthTotal = monthSalary + monthIncome
        val monthRemaining = monthTotal - monthSpent

        val progress = when {
            monthTotal > 0.0 -> (monthSpent / monthTotal).coerceIn(0.0, 1.0).toFloat()
            monthSpent > 0.0 -> 1f
            else -> 0f
        }
        val pctInt = if (monthTotal > 0.0) ((monthSpent / monthTotal) * 100.0).roundToLong().toInt() else 0

        val catTotals = mutableMapOf<String, Double>()
        for (tx in monthExpenses) {
            catTotals[tx.category] = (catTotals[tx.category] ?: 0.0) + tx.amount
        }
        val sortedCats = catTotals.entries.sortedByDescending { it.value }
        val topCatVal = sortedCats.firstOrNull()?.value ?: 1.0
        val breakdowns = sortedCats.map { (catName, amt) ->
            val info = CategoryCatalog.infoFor(catName, TransactionType.OUT)
            val share = if (monthSpent > 0.0) (amt / monthSpent).toFloat() else 0f
            val bar = if (topCatVal > 0.0) (amt / topCatVal).toFloat() else 0f
            val catPct = if (monthSpent > 0.0) ((amt / monthSpent) * 100.0).roundToLong().toInt() else 0
            CategoryBreakdown(
                info = info,
                amount = amt,
                shareFraction = share,
                barFraction = bar,
                percentageInt = catPct
            )
        }

        // Smart Tip parity with HTML
        val tipText = when {
            monthTotal <= 0.0 -> "Pehle upar apni salary daalo, tab hisaab sahi dikhega."
            breakdowns.isNotEmpty() -> {
                val top = breakdowns.first()
                var base = "Sabse zyada kharch \"${top.info.name}\" par hua: ${formatRupees(top.amount)} (${top.percentageInt}%)."
                if (month == today.take(7) && monthRemaining > 0.0) {
                    val cal = Calendar.getInstance()
                    val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
                    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    val remDays = max(1, daysInMonth - dayOfMonth + 1)
                    base += " Bache hue $remDays din ke liye roz ${formatRupees(monthRemaining / remDays)} tak kharch kar sakte ho."
                }
                if (monthRemaining < 0.0) {
                    base = "Salary se ${formatRupees(-monthRemaining)} zyada kharch ho gaya. $base"
                }
                base
            }
            else -> "Pehla kharch jodo, phir yahan salah dikhegi."
        }
        val rawPct = if (monthTotal > 0.0) (monthSpent / monthTotal) * 100.0 else 0.0
        val tipEmoji = when {
            monthRemaining < 0.0 -> "🚨"
            rawPct > 75.0 -> "🤔"
            monthSpent > 0.0 -> "😎"
            else -> "👋"
        }

        // Total cash in hand across all months + loans
        val openingCash = raw.settings?.openingCash ?: 0.0
        var cashInHand = openingCash + salaryMap.values.sum()
        for (tx in raw.transactions) {
            cashInHand += if (tx.direction == TransactionType.IN) tx.amount else -tx.amount
        }

        // Loans & Repayments
        val paymentsByLoan = raw.payments
            .sortedWith(
                compareBy<LoanPayment> { it.date }
                    .thenBy { it.createdAt?.seconds ?: 0L }
            )
            .groupBy { it.loanId }

        var denaHai = 0.0
        var milnaHai = 0.0
        var chukaDiya = 0.0
        var wapasAaya = 0.0

        val personMap = linkedMapOf<String, Pair<String, MutableList<LoanWithPayments>>>()
        val personNets = mutableMapOf<String, Double>()

        val sortedLoans = raw.loans.sortedWith(
            compareByDescending<UdhaarLoan> { it.date }
                .thenByDescending { it.createdAt?.seconds ?: 0L }
        )

        for (loan in sortedLoans) {
            val pays = paymentsByLoan[loan.id].orEmpty()
            val paid = pays.sumOf { it.amount }
            val rem = max(0.0, loan.amount - paid)
            val overdue = loan.dueDate.isNotBlank() && rem > 0.0 && loan.dueDate < today

            if (loan.direction == LoanType.LIYA) {
                denaHai += rem
                chukaDiya += paid
                cashInHand += (loan.amount - paid)
            } else {
                milnaHai += rem
                wapasAaya += paid
                cashInHand += (paid - loan.amount)
            }

            val key = loan.person.trim().lowercase(Locale.ROOT)
            val entry = personMap.getOrPut(key) { loan.person.trim() to mutableListOf() }
            entry.second.add(
                LoanWithPayments(
                    loan = loan,
                    payments = pays,
                    paidAmount = paid,
                    remainingAmount = rem,
                    isOverdue = overdue
                )
            )
            val delta = if (loan.direction == LoanType.LIYA) -rem else rem
            personNets[key] = (personNets[key] ?: 0.0) + delta
        }

        val net = milnaHai - denaHai
        val netLoanSummary = when {
            raw.loans.isEmpty() -> "Koi udhaar jodo, yahan lena-dena ka total dikhega."
            net > 0.0 -> "Sab milakar logon se aapko ${formatRupees(net)} milna hai."
            net < 0.0 -> "Sab milakar aapko ${formatRupees(-net)} dena hai."
            else -> "Lena-dena barabar hai."
        }

        val personSummaries = personMap.entries
            .map { (key, pair) ->
                PersonLoansSummary(
                    personName = pair.first,
                    netBalance = personNets[key] ?: 0.0,
                    loans = pair.second
                )
            }
            .sortedByDescending { abs(it.netBalance) }

        val overdueCount = personSummaries.sumOf { summary ->
            summary.loans.count { it.isOverdue }
        }

        val savingsRate = if (monthTotal > 0.0 && monthRemaining > 0.0) {
            ((monthRemaining / monthTotal) * 100.0).roundToLong().toInt()
        } else {
            0
        }

        val sixMonthTrend = (-5..0).map { offset ->
            val mKey = addMonths(month, offset)
            val mSalary = salaryMap[mKey] ?: 0.0
            val mTx = raw.transactions.filter { it.monthKey == mKey || it.date.startsWith(mKey) }
            val mExtraIn = mTx.filter { it.direction == TransactionType.IN }.sumOf { it.amount }
            val mOut = mTx.filter { it.direction == TransactionType.OUT }.sumOf { it.amount }
            MonthlyTrendPoint(
                monthKey = mKey,
                shortLabel = formatShortMonthLabel(mKey),
                inflow = mSalary + mExtraIn,
                expense = mOut
            )
        }

        return HisaabDashboardData(
            selectedMonth = month,
            todayDate = today,
            monthSalary = monthSalary,
            monthExtraIncome = monthIncome,
            monthSpent = monthSpent,
            monthTotalAvailable = monthTotal,
            monthRemaining = monthRemaining,
            spentProgress = progress,
            spentPercentInt = pctInt,
            savingsRatePercent = savingsRate,
            smartTipEmoji = tipEmoji,
            smartTipText = tipText,
            monthTransactions = monthTx,
            categoryBreakdowns = breakdowns,
            sixMonthTrend = sixMonthTrend,
            openingCash = openingCash,
            totalCashInHand = cashInHand,
            totalDenaHai = denaHai,
            totalMilnaHai = milnaHai,
            totalChukaDiya = chukaDiya,
            totalWapasAaya = wapasAaya,
            overdueLoansCount = overdueCount,
            netLoanSummaryText = netLoanSummary,
            personSummaries = personSummaries,
        )
    }

    companion object {
        private const val TAG = "HisaabVM"
        private const val STOP_TIMEOUT_MILLIS = 5000L

        fun currentDateStr(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return sdf.format(Calendar.getInstance().time)
        }

        fun currentMonthKey(): String = currentDateStr().substring(0, 7)

        fun addMonths(monthKey: String, delta: Int): String {
            val parts = monthKey.split("-")
            val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
            val month = (parts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.add(Calendar.MONTH, delta)
            return SimpleDateFormat("yyyy-MM", Locale.US).format(cal.time)
        }

        fun formatMonthLabel(monthKey: String): String {
            return try {
                val parser = SimpleDateFormat("yyyy-MM", Locale.US)
                val date = parser.parse(monthKey) ?: return monthKey
                SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(date)
            } catch (_: Exception) {
                monthKey
            }
        }

        fun formatShortMonthLabel(monthKey: String): String {
            return try {
                val parser = SimpleDateFormat("yyyy-MM", Locale.US)
                val date = parser.parse(monthKey) ?: return monthKey
                SimpleDateFormat("MMM", Locale.ENGLISH).format(date)
            } catch (_: Exception) {
                monthKey.takeLast(2)
            }
        }

        fun formatDisplayDate(dateStr: String): String {
            val parts = dateStr.split("-")
            return if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else dateStr
        }

        fun formatRupees(amount: Double, showNegativeSign: Boolean = false): String {
            val absRounded = abs(amount).roundToLong()
            val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN"))
            val formatted = "₹${formatter.format(absRounded)}"
            return if (showNegativeSign && amount < 0.0) "-$formatted" else formatted
        }
    }
}
