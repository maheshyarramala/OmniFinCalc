package com.example.domain.finance

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Exact decimal / integer minor-unit (paise/cents) financial arithmetic utilities.
 * Eliminates binary floating-point drift when summing balances, ledgers, splits, and analytics.
 */
object MoneyMath {
  private val HUNDRED = BigDecimal(100)

  fun toMinorUnits(amount: Double): Long {
    if (!amount.isFinite()) return 0L
    return BigDecimal.valueOf(amount)
      .multiply(HUNDRED)
      .setScale(0, RoundingMode.HALF_UP)
      .longValueExact()
  }

  fun fromMinorUnits(minorUnits: Long): Double {
    return BigDecimal.valueOf(minorUnits)
      .divide(HUNDRED, 2, RoundingMode.HALF_UP)
      .toDouble()
  }

  fun roundCurrency(amount: Double): Double {
    if (!amount.isFinite()) return 0.0
    return fromMinorUnits(toMinorUnits(amount))
  }

  fun sum(values: Iterable<Double>): Double {
    var totalMinor = 0L
    for (v in values) {
      totalMinor += toMinorUnits(v)
    }
    return fromMinorUnits(totalMinor)
  }

  inline fun <T> sumOfExact(items: Iterable<T>, selector: (T) -> Double): Double {
    var totalMinor = 0L
    for (item in items) {
      totalMinor += toMinorUnits(selector(item))
    }
    return fromMinorUnits(totalMinor)
  }

  fun add(a: Double, b: Double): Double = fromMinorUnits(toMinorUnits(a) + toMinorUnits(b))

  fun subtract(a: Double, b: Double): Double = fromMinorUnits(toMinorUnits(a) - toMinorUnits(b))

  fun percentage(part: Double, total: Double): Double {
    val totalMinor = toMinorUnits(total)
    if (totalMinor <= 0L) return 0.0
    val partBd = BigDecimal.valueOf(toMinorUnits(part))
    val totalBd = BigDecimal.valueOf(totalMinor)
    return partBd.multiply(HUNDRED)
      .divide(totalBd, 2, RoundingMode.HALF_UP)
      .toDouble()
  }
}
