package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.YearlyBreakdown
import com.example.ui.theme.OmniFinCalcThemeTokens

@Composable
fun GrowthLineChart(
  breakdown: List<YearlyBreakdown>,
  modifier: Modifier = Modifier
) {
  if (breakdown.isEmpty()) return

  val semantic = OmniFinCalcThemeTokens.colors
  val totalColor = semantic.income
  val investedColor = MaterialTheme.colorScheme.primary
  val surfaceColor = MaterialTheme.colorScheme.surface
  val gridColor = semantic.divider

  val maxVal = breakdown.maxOfOrNull { it.balance } ?: 1.0
  val maxValue = if (maxVal <= 0.0) 1.0 else maxVal

  val progress by animateFloatAsState(
    targetValue = 1f,
    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
    label = "lineChartAnim"
  )

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    border = BorderStroke(1.dp, semantic.cardBorder),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    colors = CardDefaults.cardColors(
      containerColor = surfaceColor
    )
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Wealth Growth Projection",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Year-by-year compounding balance",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Text(
          text = CurrencyFormatter.formatCompact(maxValue),
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            fontFeatureSettings = "tnum"
          ),
          color = totalColor
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        ChartLegendItem(label = "Total Value", color = totalColor)
        ChartLegendItem(label = "Invested", color = investedColor)
      }

      Spacer(modifier = Modifier.height(12.dp))

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(144.dp)
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height
          val padV = 8.dp.toPx()
          val usableH = (h - padV * 2f).coerceAtLeast(1f)
          val count = breakdown.size

          for (i in 0..2) {
            val y = padV + (usableH / 2f) * i
            drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1.dp.toPx())
          }

          if (count < 2) return@Canvas

          val stepX = w / (count - 1)
          val totalPath = Path()
          val investedPath = Path()
          val fillPath = Path()

          val pointsTotal = mutableListOf<Offset>()
          val pointsInvested = mutableListOf<Offset>()

          for (i in 0 until count) {
            val item = breakdown[i]
            val x = i * stepX
            val yTotal = padV + usableH - ((item.balance / maxValue).toFloat() * usableH * progress)
            val yInvested = padV + usableH - ((item.invested / maxValue).toFloat() * usableH * progress)

            pointsTotal.add(Offset(x, yTotal))
            pointsInvested.add(Offset(x, yInvested))
          }

          totalPath.moveTo(pointsTotal[0].x, pointsTotal[0].y)
          fillPath.moveTo(pointsTotal[0].x, padV + usableH)
          fillPath.lineTo(pointsTotal[0].x, pointsTotal[0].y)

          for (i in 1 until pointsTotal.size) {
            val prev = pointsTotal[i - 1]
            val curr = pointsTotal[i]
            val midX = (prev.x + curr.x) / 2
            totalPath.cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
            fillPath.cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
          }

          fillPath.lineTo(pointsTotal.last().x, padV + usableH)
          fillPath.close()

          investedPath.moveTo(pointsInvested[0].x, pointsInvested[0].y)
          for (i in 1 until pointsInvested.size) {
            val prev = pointsInvested[i - 1]
            val curr = pointsInvested[i]
            val midX = (prev.x + curr.x) / 2
            investedPath.cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
          }

          drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
              colors = listOf(
                totalColor.copy(alpha = 0.24f),
                totalColor.copy(alpha = 0.01f)
              ),
              startY = padV,
              endY = padV + usableH
            )
          )

          drawPath(
            path = totalPath,
            color = totalColor,
            style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
          )

          drawPath(
            path = investedPath,
            color = investedColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
          )

          drawCircle(
            color = surfaceColor,
            radius = 5.5.dp.toPx(),
            center = pointsTotal.last()
          )
          drawCircle(
            color = totalColor,
            radius = 3.5.dp.toPx(),
            center = pointsTotal.last()
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "Yr 1",
          style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (breakdown.size > 2) {
          Text(
            text = "Yr ${breakdown.size / 2}",
            style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Text(
          text = "Yr ${breakdown.size}",
          style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}
