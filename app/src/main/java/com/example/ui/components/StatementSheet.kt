package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.HisaabDashboardData
import com.example.ui.HisaabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatementDownloadSheet(
    data: HisaabDashboardData,
    onDismiss: () -> Unit,
    onDownloadPdf: () -> Unit,
    onDownloadCsv: () -> Unit,
    onShareText: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.statement_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${HisaabViewModel.formatMonthLabel(data.selectedMonth)} · ${data.monthTransactions.size} entries · Kharch ${HisaabViewModel.formatRupees(data.monthSpent)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            StatementOptionRow(
                icon = Icons.Filled.PictureAsPdf,
                iconTint = Color(0xFFD64545),
                title = stringResource(R.string.statement_pdf_title),
                subtitle = stringResource(R.string.statement_pdf_desc),
                trailingIcon = Icons.Filled.Download,
                onClick = onDownloadPdf,
                modifier = Modifier.testTag("download_pdf_statement")
            )

            StatementOptionRow(
                icon = Icons.Filled.TableChart,
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.statement_csv_title),
                subtitle = stringResource(R.string.statement_csv_desc),
                trailingIcon = Icons.Filled.Download,
                onClick = onDownloadCsv,
                modifier = Modifier.testTag("download_csv_statement")
            )

            StatementOptionRow(
                icon = Icons.Filled.Share,
                iconTint = MaterialTheme.colorScheme.secondary,
                title = stringResource(R.string.statement_share_title),
                subtitle = stringResource(R.string.statement_share_desc),
                trailingIcon = Icons.Filled.Share,
                onClick = onShareText,
                modifier = Modifier.testTag("share_text_statement")
            )
        }
    }
}

@Composable
private fun StatementOptionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    trailingIcon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = trailingIcon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
