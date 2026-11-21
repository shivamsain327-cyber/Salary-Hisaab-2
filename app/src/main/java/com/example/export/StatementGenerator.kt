package com.example.export

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.LoanType
import com.example.data.TransactionType
import com.example.ui.HisaabDashboardData
import com.example.ui.HisaabViewModel
import java.io.OutputStream
import java.nio.charset.StandardCharsets

object StatementGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 36f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2)

    fun writePdfStatement(outputStream: OutputStream, data: HisaabDashboardData) {
        val doc = PdfDocument()
        var pageNumber = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
        var canvas = page.canvas
        var y = MARGIN

        val brandTeal = Color.rgb(11, 122, 106)
        val darkInk = Color.rgb(18, 48, 43)
        val mutedInk = Color.rgb(93, 122, 116)
        val cardBg = Color.rgb(243, 246, 245)
        val lineGray = Color.rgb(219, 230, 227)
        val badRed = Color.rgb(194, 65, 12)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(230, 255, 255, 255)
            textSize = 11f
        }
        val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = brandTeal
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkInk
            textSize = 10.5f
        }
        val boldBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkInk
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = mutedInk
            textSize = 9.5f
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = lineGray
            strokeWidth = 1f
        }

        fun ensureSpace(neededHeight: Float) {
            if (y + neededHeight > PAGE_HEIGHT - MARGIN) {
                doc.finishPage(page)
                pageNumber++
                page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                canvas = page.canvas
                y = MARGIN
            }
        }

        // Top Brand Header Banner
        fillPaint.color = brandTeal
        canvas.drawRoundRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 66f, 12f, 12f, fillPaint)
        canvas.drawText("Salary Hisaab — Statement", MARGIN + 16f, y + 28f, titlePaint)
        canvas.drawText(
            "Mahina: ${HisaabViewModel.formatMonthLabel(data.selectedMonth)}   |   Tarikh: ${HisaabViewModel.formatDisplayDate(data.todayDate)}",
            MARGIN + 16f,
            y + 48f,
            subtitlePaint
        )
        y += 84f

        // Monthly Summary Box
        fillPaint.color = cardBg
        canvas.drawRoundRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 86f, 10f, 10f, fillPaint)
        val col2X = MARGIN + CONTENT_WIDTH / 2f + 8f
        canvas.drawText("Is mahine ki salary:", MARGIN + 14f, y + 22f, mutedPaint)
        canvas.drawText(HisaabViewModel.formatRupees(data.monthSalary), MARGIN + 14f, y + 37f, boldBodyPaint)

        canvas.drawText("Upar se aaya (Extra):", col2X, y + 22f, mutedPaint)
        canvas.drawText(HisaabViewModel.formatRupees(data.monthExtraIncome), col2X, y + 37f, boldBodyPaint)

        canvas.drawText("Kul kharch:", MARGIN + 14f, y + 58f, mutedPaint)
        canvas.drawText(
            "${HisaabViewModel.formatRupees(data.monthSpent)} (${data.spentPercentInt}%)",
            MARGIN + 14f,
            y + 73f,
            boldBodyPaint
        )

        canvas.drawText("Abhi bacha hai:", col2X, y + 58f, mutedPaint)
        val remPaint = Paint(boldBodyPaint).apply {
            color = if (data.monthRemaining < 0) badRed else brandTeal
        }
        canvas.drawText(
            HisaabViewModel.formatRupees(data.monthRemaining, showNegativeSign = true),
            col2X,
            y + 73f,
            remPaint
        )
        y += 104f

        // Category Breakdown
        if (data.categoryBreakdowns.isNotEmpty()) {
            ensureSpace(40f)
            canvas.drawText("Kahan Kitna Gaya (Category Breakdown)", MARGIN, y, sectionPaint)
            y += 14f
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
            y += 14f
            for (cat in data.categoryBreakdowns) {
                ensureSpace(18f)
                canvas.drawText(cat.info.name, MARGIN + 4f, y, bodyPaint)
                val valStr = "${HisaabViewModel.formatRupees(cat.amount)} (${cat.percentageInt}%)"
                val valW = boldBodyPaint.measureText(valStr)
                canvas.drawText(valStr, PAGE_WIDTH - MARGIN - valW - 4f, y, boldBodyPaint)
                y += 16f
            }
            y += 10f
        }

        // Monthly Transactions Table
        ensureSpace(50f)
        canvas.drawText("Saara Hisaab (${data.monthTransactions.size} entries)", MARGIN, y, sectionPaint)
        y += 14f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += 14f

        if (data.monthTransactions.isEmpty()) {
            canvas.drawText("Is mahine koi kharch ya aamdani darj nahi hai.", MARGIN + 4f, y, mutedPaint)
            y += 20f
        } else {
            // Table header
            canvas.drawText("Tareekh", MARGIN + 4f, y, mutedPaint)
            canvas.drawText("Category / Note", MARGIN + 78f, y, mutedPaint)
            val amtHdr = "Rakam"
            canvas.drawText(amtHdr, PAGE_WIDTH - MARGIN - mutedPaint.measureText(amtHdr) - 4f, y, mutedPaint)
            y += 8f
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
            y += 14f

            for (tx in data.monthTransactions) {
                ensureSpace(22f)
                val dateStr = HisaabViewModel.formatDisplayDate(tx.date)
                val label = buildString {
                    append(tx.category)
                    if (tx.note.isNotBlank()) {
                        append(" - ")
                        append(tx.note)
                    }
                }.take(46)
                val isIn = tx.direction == TransactionType.IN
                val amtStr = "${if (isIn) "+" else "-"}${HisaabViewModel.formatRupees(tx.amount)}"
                val amtPaint = Paint(boldBodyPaint).apply {
                    color = if (isIn) brandTeal else darkInk
                }
                canvas.drawText(dateStr, MARGIN + 4f, y, bodyPaint)
                canvas.drawText(label, MARGIN + 78f, y, bodyPaint)
                canvas.drawText(amtStr, PAGE_WIDTH - MARGIN - amtPaint.measureText(amtStr) - 4f, y, amtPaint)
                y += 17f
            }
            y += 10f
        }

        // Overall Cash & Udhaar Summary
        ensureSpace(90f)
        canvas.drawText("Poora Hisaab & Udhaar Summary", MARGIN, y, sectionPaint)
        y += 14f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += 16f

        canvas.drawText(
            "Haath mein kul paisa: ${HisaabViewModel.formatRupees(data.totalCashInHand, showNegativeSign = true)}",
            MARGIN + 4f,
            y,
            boldBodyPaint
        )
        y += 16f
        canvas.drawText(
            "Mujhe dena hai: ${HisaabViewModel.formatRupees(data.totalDenaHai)}   |   Mujhe milna hai: ${HisaabViewModel.formatRupees(data.totalMilnaHai)}",
            MARGIN + 4f,
            y,
            bodyPaint
        )
        y += 16f

        for (person in data.personSummaries) {
            ensureSpace(18f)
            val status = when {
                person.netBalance > 0.0 -> "Milna hai ${HisaabViewModel.formatRupees(person.netBalance)}"
                person.netBalance < 0.0 -> "Dena hai ${HisaabViewModel.formatRupees(-person.netBalance)}"
                else -> "Hisaab saaf"
            }
            canvas.drawText(person.personName, MARGIN + 4f, y, bodyPaint)
            canvas.drawText(status, PAGE_WIDTH - MARGIN - boldBodyPaint.measureText(status) - 4f, y, boldBodyPaint)
            y += 16f
        }

        doc.finishPage(page)
        doc.writeTo(outputStream)
        doc.close()
    }

    fun writeCsvStatement(outputStream: OutputStream, data: HisaabDashboardData) {
        val sb = StringBuilder()
        // UTF-8 BOM for Excel
        sb.append('\uFEFF')
        sb.appendLine("Salary Hisaab Statement,${csvCell(HisaabViewModel.formatMonthLabel(data.selectedMonth))}")
        sb.appendLine("Salary,${data.monthSalary.toLong()}")
        sb.appendLine("Extra Income,${data.monthExtraIncome.toLong()}")
        sb.appendLine("Total Spent,${data.monthSpent.toLong()}")
        sb.appendLine("Remaining Balance,${data.monthRemaining.toLong()}")
        sb.appendLine("Total Cash In Hand,${data.totalCashInHand.toLong()}")
        sb.appendLine()
        sb.appendLine("Tareekh,Type,Category,Note,Rakam (INR)")
        for (tx in data.monthTransactions) {
            val typeLabel = if (tx.direction == TransactionType.IN) "Paise Aaye" else "Kharch"
            sb.appendLine(
                listOf(
                    csvCell(HisaabViewModel.formatDisplayDate(tx.date)),
                    csvCell(typeLabel),
                    csvCell(tx.category),
                    csvCell(tx.note),
                    tx.amount.toLong().toString()
                ).joinToString(",")
            )
        }
        if (data.personSummaries.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("Udhaar Hisaab - Person,Direction,Original Amount,Paid,Remaining,Date,Due Date,Note")
            for (person in data.personSummaries) {
                for (item in person.loans) {
                    val loan = item.loan
                    val dir = if (loan.direction == LoanType.LIYA) "Liya" else "Diya"
                    sb.appendLine(
                        listOf(
                            csvCell(loan.person),
                            csvCell(dir),
                            loan.amount.toLong().toString(),
                            item.paidAmount.toLong().toString(),
                            item.remainingAmount.toLong().toString(),
                            csvCell(HisaabViewModel.formatDisplayDate(loan.date)),
                            csvCell(if (loan.dueDate.isNotBlank()) HisaabViewModel.formatDisplayDate(loan.dueDate) else ""),
                            csvCell(loan.note)
                        ).joinToString(",")
                    )
                }
            }
        }
        outputStream.write(sb.toString().toByteArray(StandardCharsets.UTF_8))
    }

    fun buildShareableSummaryText(data: HisaabDashboardData): String {
        return buildString {
            appendLine("💸 *Salary Hisaab — ${HisaabViewModel.formatMonthLabel(data.selectedMonth)}*")
            appendLine("Salary: ${HisaabViewModel.formatRupees(data.monthSalary)}")
            appendLine("Upar se aaya: ${HisaabViewModel.formatRupees(data.monthExtraIncome)}")
            appendLine("Kul Kharch: ${HisaabViewModel.formatRupees(data.monthSpent)} (${data.spentPercentInt}%)")
            appendLine("Abhi bacha hai: ${HisaabViewModel.formatRupees(data.monthRemaining, showNegativeSign = true)}")
            appendLine()
            if (data.categoryBreakdowns.isNotEmpty()) {
                appendLine("🍩 *Category-wise Kharch:*")
                data.categoryBreakdowns.forEach { cat ->
                    appendLine("• ${cat.info.emoji} ${cat.info.name}: ${HisaabViewModel.formatRupees(cat.amount)} (${cat.percentageInt}%)")
                }
                appendLine()
            }
            if (data.monthTransactions.isNotEmpty()) {
                appendLine("🧾 *Entries (${data.monthTransactions.size}):*")
                data.monthTransactions.take(25).forEach { tx ->
                    val sign = if (tx.direction == TransactionType.IN) "+" else "-"
                    val label = tx.note.ifBlank { tx.category }
                    appendLine("• ${HisaabViewModel.formatDisplayDate(tx.date)} | $label: $sign${HisaabViewModel.formatRupees(tx.amount)}")
                }
                if (data.monthTransactions.size > 25) {
                    appendLine("...aur ${data.monthTransactions.size - 25} entries")
                }
                appendLine()
            }
            appendLine("👛 Haath mein kul paisa: ${HisaabViewModel.formatRupees(data.totalCashInHand, showNegativeSign = true)}")
            if (data.totalDenaHai > 0.0 || data.totalMilnaHai > 0.0) {
                appendLine("🤝 Dena hai: ${HisaabViewModel.formatRupees(data.totalDenaHai)} | Milna hai: ${HisaabViewModel.formatRupees(data.totalMilnaHai)}")
            }
        }.trim()
    }

    private fun csvCell(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}
