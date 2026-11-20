package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OmniFinCalcThemeTokens
import kotlin.math.max

data class BarComparisonPoint(
  val label: String,
  val income: Double,
  val expense: Double
)

data class TrendSeriesPoint(
  val label: String,
  val value: Double
)

data class CategoryBreakdownSlice(
  val name: String,
  val amount: Double,
  val percentage: Double,
  val color: Color
)

@Composable
fun IncomeExpenseBarChart(
  points: List<BarComparisonPoint>,
  modifier: Modifier = Modifier
) {
  val semantic = OmniFinCalcThemeTokens.colors
  val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
  val maxValue = points.maxOfOrNull { max(it.income, it.expense) }?.coerceAtLeast(1.0) ?: 1.0

  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      ChartLegendItem(label = "Income", color = semantic.income)
      ChartLegendItem(label = "Expenses", color = semantic.expense)
    }

    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .height(156.dp)
    ) {
      val chartHeight = size.height - 12.dp.toPx()
      val chartTop = 6.dp.toPx()

      // Subtle horizontal gridlines
      for (i in 0..3) {
        val y = chartTop + (chartHeight / 3f) * i
        drawLine(
          color = gridColor,
          start = Offset(0f, y),
          end = Offset(size.width, y),
          strokeWidth = 1.dp.toPx()
        )
      }

      if (points.isEmpty()) return@Canvas
      val groupWidth = size.width / points.size.toFloat()
      val barWidth = (groupWidth * 0.28f).coerceIn(6.dp.toPx(), 18.dp.toPx())
      val gap = 4.dp.toPx()

      points.forEachIndexed { index, pt ->
        val centerX = groupWidth * index + groupWidth / 2f
        val incomeRatio = (pt.income / maxValue).toFloat().coerceIn(0f, 1f)
        val expenseRatio = (pt.expense / maxValue).toFloat().coerceIn(0f, 1f)

        val incomeHeight = (chartHeight * incomeRatio).coerceAtLeast(if (pt.income > 0) 4.dp.toPx() else 0f)
        val expenseHeight = (chartHeight * expenseRatio).coerceAtLeast(if (pt.expense > 0) 4.dp.toPx() else 0f)
        val corner = CornerRadius(5.dp.toPx(), 5.dp.toPx())

        if (incomeHeight > 0f) {
          drawRoundRect(
            color = semantic.income,
            topLeft = Offset(centerX - barWidth - gap / 2f, chartTop + chartHeight - incomeHeight),
            size = Size(barWidth, incomeHeight),
            cornerRadius = corner
          )
        }
        if (expenseHeight > 0f) {
          drawRoundRect(
            color = semantic.expense,
            topLeft = Offset(centerX + gap / 2f, chartTop + chartHeight - expenseHeight),
            size = Size(barWidth, expenseHeight),
            cornerRadius = corner
          )
        }
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly
    ) {
      points.forEach { pt ->
        Text(
          text = pt.label,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}

@Composable
fun FintechAreaLineChart(
  points: List<TrendSeriesPoint>,
  lineColor: Color,
  modifier: Modifier = Modifier
) {
  val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
  val surfaceColor = MaterialTheme.colorScheme.surface
  if (points.isEmpty()) return

  val minVal = points.minOf { it.value }.coerceAtMost(0.0)
  val maxVal = points.maxOf { it.value }.coerceAtLeast(1.0)
  val range = (maxVal - minVal).takeIf { it > 0.0 } ?: 1.0

  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .height(144.dp)
    ) {
      val padV = 12.dp.toPx()
      val padH = 10.dp.toPx()
      val usableW = (size.width - padH * 2f).coerceAtLeast(1f)
      val usableH = (size.height - padV * 2f).coerceAtLeast(1f)

      for (i in 0..3) {
        val y = padV + (usableH / 3f) * i
        drawLine(gridColor, Offset(padH, y), Offset(size.width - padH, y), strokeWidth = 1.dp.toPx())
      }

      val stepX = if (points.size > 1) usableW / (points.size - 1).toFloat() else 0f
      val coords = points.mapIndexed { index, pt ->
        val x = if (points.size == 1) size.width / 2f else padH + stepX * index
        val norm = ((pt.value - minVal) / range).toFloat().coerceIn(0f, 1f)
        val y = padV + usableH * (1f - norm)
        Offset(x, y)
      }

      if (coords.size >= 2) {
        val linePath = Path().apply {
          moveTo(coords.first().x, coords.first().y)
          for (i in 1 until coords.size) {
            val prev = coords[i - 1]
            val curr = coords[i]
            val midX = (prev.x + curr.x) / 2f
            cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
          }
        }

        val fillPath = Path().apply {
          addPath(linePath)
          lineTo(coords.last().x, padV + usableH)
          lineTo(coords.first().x, padV + usableH)
          close()
        }

        drawPath(
          path = fillPath,
          brush = Brush.verticalGradient(
            colors = listOf(lineColor.copy(alpha = 0.24f), lineColor.copy(alpha = 0.01f)),
            startY = padV,
            endY = padV + usableH
          )
        )
        drawPath(
          path = linePath,
          color = lineColor,
          style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
      }

      coords.forEach { pt ->
        drawCircle(color = surfaceColor, radius = 4.5.dp.toPx(), center = pt)
        drawCircle(color = lineColor, radius = 3.dp.toPx(), center = pt)
      }
    }

    val labelIndices = when {
      points.size <= 6 -> points.indices.toList()
      else -> listOf(0, points.size / 3, (points.size * 2) / 3, points.lastIndex).distinct()
    }
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      labelIndices.forEach { idx ->
        Text(
          text = points[idx].label,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1
        )
      }
    }
  }
}

@Composable
fun FintechCategoryDonutBreakdown(
  slices: List<CategoryBreakdownSlice>,
  centerLabel: String,
  centerValue: String,
  modifier: Modifier = Modifier
) {
  val total = slices.sumOf { it.amount }.coerceAtLeast(0.0)
  val trackColor = MaterialTheme.colorScheme.surfaceVariant

  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(168.dp),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.size(152.dp)) {
        val strokeWidth = 18.dp.toPx()
        val inset = strokeWidth / 2f
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val topLeft = Offset(inset, inset)

        if (total <= 0.0 || slices.isEmpty()) {
          drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
          )
        } else {
          var startAngle = -90f
          slices.forEach { slice ->
            val sweep = ((slice.amount / total) * 360.0).toFloat()
            if (sweep > 0.5f) {
              drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = (sweep - 2f).coerceAtLeast(1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
              )
              startAngle += sweep
            }
          }
        }
      }

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = centerLabel,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = centerValue,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontFeatureSettings = "tnum"
          ),
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      slices.take(6).forEach { slice ->
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .clip(CircleShape)
                  .background(slice.color)
              )
              Text(
                text = slice.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
            Text(
              text = "${CurrencyFormatter.format(slice.amount)} (${String.format(java.util.Locale.US, "%.1f", slice.percentage)}%)",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontFeatureSettings = "tnum"
              ),
              color = MaterialTheme.colorScheme.onSurface
            )
          }
          LinearProgressIndicator(
            progress = { (slice.percentage / 100.0).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = slice.color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
          )
        }
      }
    }
  }
}

@Composable
fun ChartLegendItem(label: String, color: Color) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    Box(
      modifier = Modifier
        .size(9.dp)
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
