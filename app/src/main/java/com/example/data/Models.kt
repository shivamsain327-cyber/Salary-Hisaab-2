package com.example.data

import androidx.annotation.Keep
import androidx.compose.ui.graphics.Color
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class TransactionType {
    OUT,
    IN;

    companion object {
        fun fromRaw(raw: String): TransactionType =
            entries.firstOrNull { it.name == raw } ?: OUT
    }
}

enum class LoanType {
    LIYA,
    DIYA;

    companion object {
        fun fromRaw(raw: String): LoanType =
            entries.firstOrNull { it.name == raw } ?: LIYA
    }
}

@Keep
@IgnoreExtraProperties
data class UserLedgerSettings(
    @DocumentId val id: String = "",
    val userId: String = "",
    val openingCash: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class MonthlySalary(
    @DocumentId val id: String = "",
    val userId: String = "",
    val monthKey: String = "",
    val amount: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class LedgerTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val type: String = TransactionType.OUT.name,
    val amount: Double = 0.0,
    val category: String = "",
    val note: String = "",
    val date: String = "",
    val monthKey: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    val direction: TransactionType
        get() = TransactionType.fromRaw(type)
}

@Keep
@IgnoreExtraProperties
data class UdhaarLoan(
    @DocumentId val id: String = "",
    val userId: String = "",
    val person: String = "",
    val type: String = LoanType.LIYA.name,
    val amount: Double = 0.0,
    val date: String = "",
    val dueDate: String = "",
    val note: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    val direction: LoanType
        get() = LoanType.fromRaw(type)
}

@Keep
@IgnoreExtraProperties
data class LoanPayment(
    @DocumentId val id: String = "",
    val userId: String = "",
    val loanId: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

data class CategoryInfo(
    val name: String,
    val emoji: String,
    val color: Color,
)

object CategoryCatalog {
    val expenseCategories = listOf(
        CategoryInfo("Khana-peena", "🍛", Color(0xFFE07A1F)),
        CategoryInfo("Kiraya / EMI", "🏠", Color(0xFF6D5BD0)),
        CategoryInfo("Bijli-paani-bill", "💡", Color(0xFF2A8FD6)),
        CategoryInfo("Aana-jaana", "🚌", Color(0xFF0B9D7A)),
        CategoryInfo("Shopping", "🛍️", Color(0xFFD6458A)),
        CategoryInfo("Ilaaj / dawai", "💊", Color(0xFFD64545)),
        CategoryInfo("Bachche / padhai", "📚", Color(0xFFA3A31C)),
        CategoryInfo("Ghoomna-film", "🎬", Color(0xFF8A5A2B)),
        CategoryInfo("Ghar bhejna", "📮", Color(0xFF3B7D4F)),
        CategoryInfo("Baaki", "📦", Color(0xFF7A8A87)),
    )

    val incomeCategories = listOf(
        CategoryInfo("Extra kaam", "🛠️", Color(0xFF2F9E44)),
        CategoryInfo("Tohfa / gift", "🎁", Color(0xFFC2A000)),
        CategoryInfo("Udhaar wapas", "🔁", Color(0xFF2A8FD6)),
        CategoryInfo("Byaaj / kiraya mila", "🏦", Color(0xFF0B9D7A)),
        CategoryInfo("Baaki aamdani", "✨", Color(0xFF7A8A87)),
    )

    private val allMap = (expenseCategories + incomeCategories).associateBy { it.name }

    fun infoFor(category: String, type: TransactionType): CategoryInfo {
        return allMap[category] ?: CategoryInfo(
            name = category.ifBlank { if (type == TransactionType.IN) "Baaki aamdani" else "Baaki" },
            emoji = if (type == TransactionType.IN) "💰" else "💸",
            color = Color(0xFF7A8A87)
        )
    }
}
