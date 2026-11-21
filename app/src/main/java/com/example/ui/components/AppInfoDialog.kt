package com.example.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.OmniFinCalcThemeTokens

enum class AppInfoType {
  ABOUT,
  PRIVACY,
  TERMS,
  CONTACT,
  DONATE
}

@Composable
fun AppInfoModalDialog(
  type: AppInfoType,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val semantic = OmniFinCalcThemeTokens.colors

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(22.dp),
    containerColor = MaterialTheme.colorScheme.surface,
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Close", fontWeight = FontWeight.SemiBold)
      }
    },
    title = {
      Text(
        when (type) {
          AppInfoType.ABOUT -> "About OmniFinCalc"
          AppInfoType.PRIVACY -> "Privacy Policy"
          AppInfoType.TERMS -> "Terms & Conditions"
          AppInfoType.CONTACT -> "Contact Us & Feedback"
          AppInfoType.DONATE -> "Support Developer"
        },
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        when (type) {
          AppInfoType.ABOUT -> {
            Text(
              "OmniFinCalc is a privacy-first personal finance dashboard, ledger, analytics engine, and comprehensive financial calculator suite.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(color = semantic.divider)
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              border = BorderStroke(1.dp, semantic.cardBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(
                  "Credits & Developer",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  "Developed by Mahesh Yarramala",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
            Button(
              onClick = {
                launchExternalIntent(
                  context,
                  Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/mahesh.yarramala")),
                  "No app is available to open this link."
                )
              },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Instagram: @mahesh.yarramala", fontWeight = FontWeight.SemiBold)
            }
          }

          AppInfoType.PRIVACY -> {
            Text(
              "Privacy Policy:\n\n" +
                "1. Data Collection: OmniFinCalc operates offline-first. All accounts, transactions, loan schedules, split bills, and calculation history are stored locally on your device.\n\n" +
                "2. Notifications: Notification permission is used exclusively for scheduled bill, EMI, debt, and daily expense reminders.\n\n" +
                "3. Third-party Sharing: Personal and financial records are never sold, rented, or shared with third-party advertisers.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          AppInfoType.TERMS -> {
            Text(
              "Terms & Conditions:\n\n" +
                "1. All calculations, tax estimates, and analytics provided by OmniFinCalc are for informational and personal planning purposes only.\n\n" +
                "2. Interest figures, tax liabilities, and bank EMI deductions should be verified with your respective financial institution.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          AppInfoType.CONTACT -> {
            Text(
              "We value your suggestions and feedback!",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              border = BorderStroke(1.dp, semantic.cardBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(
                  "Email",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  "devops.maheshbabu@gmail.com",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
            Button(
              onClick = {
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                  data = Uri.parse("mailto:devops.maheshbabu@gmail.com")
                  putExtra(Intent.EXTRA_SUBJECT, "OmniFinCalc Feedback / Support")
                }
                launchExternalIntent(context, emailIntent, "No email app is available.")
              },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Send Feedback Email", fontWeight = FontWeight.SemiBold)
            }
          }

          AppInfoType.DONATE -> {
            Text(
              "If OmniFinCalc helped you organize your finances, consider supporting ongoing development!",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              border = BorderStroke(1.dp, semantic.cardBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(
                  "UPI ID",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  "maheshyarramala@ybl",
                  fontWeight = FontWeight.Bold,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedButton(
                onClick = {
                  clipboardManager.setText(AnnotatedString("maheshyarramala@ybl"))
                  Toast.makeText(context, "UPI ID copied.", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, semantic.cardBorder),
                modifier = Modifier.weight(1f)
              ) {
                Text("Copy UPI")
              }
              Button(
                onClick = {
                  val upiIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("upi://pay?pa=maheshyarramala@ybl&pn=Mahesh%20Yarramala&cu=INR")
                  )
                  launchExternalIntent(
                    context,
                    Intent.createChooser(upiIntent, "Pay with UPI"),
                    "No UPI app is available."
                  )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text("Open UPI")
              }
            }
          }
        }
      }
    }
  )
}

private fun launchExternalIntent(
  context: android.content.Context,
  intent: Intent,
  unavailableMessage: String
) {
  try {
    context.startActivity(intent)
  } catch (_: ActivityNotFoundException) {
    Toast.makeText(context, unavailableMessage, Toast.LENGTH_SHORT).show()
  }
}
