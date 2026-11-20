package com.example.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AccountWithBalance
import com.example.data.local.entity.BankAccount
import com.example.data.local.entity.CreditCardBillReminder
import com.example.data.local.entity.DebtRecord
import com.example.data.local.entity.ExpenseRecord
import com.example.data.local.entity.LoanRecord
import com.example.data.local.entity.SubscriptionRecord
import com.example.data.local.entity.TripRecord
import com.example.domain.finance.MoneyMath
import com.example.domain.notification.CreditCardBillNotificationScheduler
import com.example.ui.components.CurrencyFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Calendar

class FinanceRepository internal constructor(
  context: Context,
  private val database: AppDatabase
) {
  private constructor(context: Context) : this(context, AppDatabase.getDatabase(context.applicationContext))

  private val appContext = context.applicationContext
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val financeDao = database.financeDao()
  private val loanDao = database.loanDao()
  private val debtDao = database.debtDao()

  val rawBankAccounts: Flow<List<BankAccount>> = financeDao.getAllBankAccounts()
  val allExpenses: Flow<List<ExpenseRecord>> = financeDao.getAllExpenses()
  val allLoans: Flow<List<LoanRecord>> = loanDao.getAllLoans()
  val allDebts: Flow<List<DebtRecord>> = debtDao.getAllDebts()

  /**
   * Authoritative centralized StateFlow of bank/cash/card accounts with exact minor-unit balances:
   * currentBalance = openingBalance + sum(INCOME / TRANSFER_IN) - sum(EXPENSE / TRANSFER_OUT)
   */
  val accountsWithBalances: StateFlow<List<AccountWithBalance>> = combine(
    rawBankAccounts,
    allExpenses
  ) { accs, exps ->
    val expenseMinorByAcc = HashMap<Long, Long>(accs.size)
    val incomeMinorByAcc = HashMap<Long, Long>(accs.size)
    for (exp in exps) {
      val accId = exp.bankAccountId ?: continue
      val minor = MoneyMath.toMinorUnits(exp.amount)
      when (exp.type) {
        "EXPENSE", "TRANSFER_OUT" -> {
          expenseMinorByAcc[accId] = (expenseMinorByAcc[accId] ?: 0L) + minor
        }
        "INCOME", "TRANSFER_IN" -> {
          incomeMinorByAcc[accId] = (incomeMinorByAcc[accId] ?: 0L) + minor
        }
      }
    }

    accs.map { acc ->
      val openingMinor = MoneyMath.toMinorUnits(acc.openingBalance)
      val accIncomeMinor = incomeMinorByAcc[acc.id] ?: 0L
      val accExpenseMinor = expenseMinorByAcc[acc.id] ?: 0L
      val currentMinor = openingMinor + accIncomeMinor - accExpenseMinor

      AccountWithBalance(
        account = acc,
        openingBalance = MoneyMath.fromMinorUnits(openingMinor),
        totalIncome = MoneyMath.fromMinorUnits(accIncomeMinor),
        totalExpense = MoneyMath.fromMinorUnits(accExpenseMinor),
        currentBalance = MoneyMath.fromMinorUnits(currentMinor)
      )
    }
  }.flowOn(Dispatchers.Default).stateIn(
    scope = scope,
    started = SharingStarted.Eagerly,
    initialValue = emptyList()
  )

  val totalNetBalance: StateFlow<Double> = combine(accountsWithBalances) { (list) ->
    MoneyMath.sumOfExact(list) {
      if (!it.account.accountType.equals("Credit Card", ignoreCase = true)) it.currentBalance else 0.0
    }
  }.flowOn(Dispatchers.Default).stateIn(
    scope = scope,
    started = SharingStarted.Eagerly,
    initialValue = 0.0
  )

  val totalCreditCardDue: StateFlow<Double> = combine(accountsWithBalances) { (list) ->
    MoneyMath.sumOfExact(list) {
      if (it.account.accountType.equals("Credit Card", ignoreCase = true) && it.currentBalance < 0.0) {
        kotlin.math.abs(it.currentBalance)
      } else {
        0.0
      }
    }
  }.flowOn(Dispatchers.Default).stateIn(
    scope = scope,
    started = SharingStarted.Eagerly,
    initialValue = 0.0
  )

  val totalIncome: StateFlow<Double> = combine(allExpenses) { (exps) ->
    MoneyMath.sumOfExact(exps) { if (it.type == "INCOME") it.amount else 0.0 }
  }.flowOn(Dispatchers.Default).stateIn(
    scope = scope,
    started = SharingStarted.Eagerly,
    initialValue = 0.0
  )

  val totalExpense: StateFlow<Double> = combine(allExpenses) { (exps) ->
    MoneyMath.sumOfExact(exps) { if (it.type == "EXPENSE") it.amount else 0.0 }
  }.flowOn(Dispatchers.Default).stateIn(
    scope = scope,
    started = SharingStarted.Eagerly,
    initialValue = 0.0
  )

  private fun currentMonthBounds(): Pair<Long, Long> {
    val cal = Calendar.getInstance().apply {
      set(Calendar.DAY_OF_MONTH, 1)
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val monthStart = cal.timeInMillis
    cal.add(Calendar.MONTH, 1)
    val nextMonthStart = cal.timeInMillis
    return monthStart to nextMonthStart
  }

  val currentMonthExpenses: StateFlow<Double> = combine(allExpenses) { (exps) ->
    val (monthStart, nextMonthStart) = currentMonthBounds()
    MoneyMath.sumOfExact(exps) {
      if (it.type == "EXPENSE" && it.dateTimestamp in monthStart until nextMonthStart) it.amount else 0.0
    }
  }.flowOn(Dispatchers.Default).stateIn(
    scope = scope,
    started = SharingStarted.Eagerly,
    initialValue = 0.0
  )

  val currentMonthIncome: StateFlow<Double> = combine(allExpenses) { (exps) ->
    val (monthStart, nextMonthStart) = currentMonthBounds()
    MoneyMath.sumOfExact(exps) {
      if (it.type == "INCOME" && it.dateTimestamp in monthStart until nextMonthStart) it.amount else 0.0
    }
  }.flowOn(Dispatchers.Default).stateIn(
    scope = scope,
    started = SharingStarted.Eagerly,
    initialValue = 0.0
  )

  val currentMonthSavings: StateFlow<Double> = combine(
    currentMonthIncome,
    currentMonthExpenses
  ) { inc, exp ->
    MoneyMath.subtract(inc, exp)
  }.flowOn(Dispatchers.Default).stateIn(
    scope = scope,
    started = SharingStarted.Eagerly,
    initialValue = 0.0
  )

  suspend fun insertExpense(expense: ExpenseRecord): Long = withContext(Dispatchers.IO) {
    financeDao.insertExpense(expense.copy(amount = MoneyMath.roundCurrency(expense.amount)))
  }

  /**
   * Atomically deletes an expense and updates linked loan EMI count or debt settlement state.
   */
  suspend fun deleteExpense(expense: ExpenseRecord) = withContext(Dispatchers.IO) {
    database.withTransaction {
      financeDao.deleteExpense(expense)

      if (expense.loanId != null) {
        loanDao.decrementPaidEmis(expense.loanId)
      } else if (expense.title.startsWith("EMI:")) {
        val loanName = expense.title.substringAfter("EMI:").substringBefore("(#").trim()
        if (loanName.isNotBlank()) {
          loanDao.getLoanByName(loanName)?.let { loanDao.decrementPaidEmis(it.id) }
        }
      }

      if (expense.debtId != null) {
        debtDao.unmarkSettled(expense.debtId)
      }
    }
  }

  /**
   * Atomically restores a previously deleted expense and its linked loan/debt state.
   */
  suspend fun restoreExpense(expense: ExpenseRecord) = withContext(Dispatchers.IO) {
    database.withTransaction {
      financeDao.insertExpense(expense)
      if (expense.loanId != null) {
        loanDao.incrementPaidEmis(expense.loanId)
      } else if (expense.title.startsWith("EMI:")) {
        val loanName = expense.title.substringAfter("EMI:").substringBefore("(#").trim()
        if (loanName.isNotBlank()) {
          loanDao.getLoanByName(loanName)?.let { loanDao.incrementPaidEmis(it.id) }
        }
      }
      if (expense.debtId != null) {
        debtDao.markSettled(expense.debtId)
      }
    }
  }

  suspend fun insertBankAccount(account: BankAccount): Long = withContext(Dispatchers.IO) {
    val cleanOpening = MoneyMath.roundCurrency(account.openingBalance)
    financeDao.insertBankAccount(account.copy(openingBalance = cleanOpening, balance = cleanOpening))
  }

  suspend fun addCreditCardWithOptionalReminder(
    bankName: String,
    currentOutstanding: Double,
    billAmount: Double?,
    dueDateEpochDay: Long?,
    accountNumber: String,
    notes: String
  ): Pair<Long, CreditCardBillReminder?> = withContext(Dispatchers.IO) {
    val cleanOutstanding = -MoneyMath.roundCurrency(kotlin.math.abs(currentOutstanding))
    var createdReminder: CreditCardBillReminder? = null
    val accId = database.withTransaction {
      val id = financeDao.insertBankAccount(
        BankAccount(
          bankName = bankName.trim(),
          accountType = "Credit Card",
          accountNumber = accountNumber.trim(),
          openingBalance = cleanOutstanding,
          balance = cleanOutstanding,
          notes = notes.trim()
        )
      )
      if (billAmount != null && billAmount > 0.0 && dueDateEpochDay != null) {
        val reminder = CreditCardBillReminder(
          accountId = id,
          amount = MoneyMath.roundCurrency(billAmount),
          dueDateEpochDay = dueDateEpochDay,
          notificationsEnabled = true
        )
        financeDao.insertCreditCardBillReminder(reminder)
        createdReminder = reminder
      }
      id
    }
    accId to createdReminder
  }

  suspend fun deleteBankAccount(account: BankAccount): Boolean = withContext(Dispatchers.IO) {
    val linkedRecords = financeDao.getAccountLinkedRecordCount(account.id)
    if (linkedRecords > 0) return@withContext false
    database.withTransaction {
      financeDao.deleteCreditCardBillReminder(account.id)
      financeDao.deleteBankAccount(account)
    }
    true
  }

  suspend fun updateBankAccount(account: BankAccount) = withContext(Dispatchers.IO) {
    financeDao.updateBankAccount(account)
  }

  suspend fun updateSubscription(sub: SubscriptionRecord) = withContext(Dispatchers.IO) {
    financeDao.updateSubscription(sub)
  }

  suspend fun updateTrip(trip: TripRecord) = withContext(Dispatchers.IO) {
    financeDao.updateTrip(trip)
  }

  suspend fun updateOpeningBalance(account: BankAccount, newOpening: Double) = withContext(Dispatchers.IO) {
    val clean = MoneyMath.roundCurrency(newOpening)
    financeDao.insertBankAccount(account.copy(openingBalance = clean, balance = clean))
  }

  suspend fun transferFunds(
    fromAccountId: Long,
    toAccountId: Long,
    amount: Double,
    notes: String
  ): Boolean = withContext(Dispatchers.IO) {
    val cleanAmount = MoneyMath.roundCurrency(amount)
    if (fromAccountId == toAccountId || !cleanAmount.isFinite() || cleanAmount <= 0.0) return@withContext false

    val allAccs = financeDao.getAllBankAccounts().first()
    val fromAcc = allAccs.find { it.id == fromAccountId } ?: return@withContext false
    val toAcc = allAccs.find { it.id == toAccountId } ?: return@withContext false

    val timestamp = System.currentTimeMillis()
    database.withTransaction {
      financeDao.insertExpenses(
        listOf(
          ExpenseRecord(
            title = "Transfer to ${toAcc.bankName}",
            amount = cleanAmount,
            categoryName = "Transfer",
            type = "TRANSFER_OUT",
            bankAccountId = fromAccountId,
            bankAccountName = fromAcc.bankName,
            dateTimestamp = timestamp,
            notes = notes.trim()
          ),
          ExpenseRecord(
            title = "Transfer from ${fromAcc.bankName}",
            amount = cleanAmount,
            categoryName = "Transfer",
            type = "TRANSFER_IN",
            bankAccountId = toAccountId,
            bankAccountName = toAcc.bankName,
            dateTimestamp = timestamp + 1,
            notes = notes.trim()
          )
        )
      )
    }
    true
  }

  /**
   * Idempotent, atomic Split Bill ledger sync.
   * Ensures exactly ONE Finance transaction exists per Split Bill item or settlement reference.
   */
  suspend fun syncSplitBillTransaction(
    reference: String,
    title: String,
    amount: Double,
    type: String, // "EXPENSE" or "INCOME"
    account: BankAccount
  ): Long? = withContext(Dispatchers.IO) {
    val cleanTitle = title.trim()
    val cleanAmount = MoneyMath.roundCurrency(amount)
    if (reference.isBlank() || cleanTitle.isBlank() || cleanAmount <= 0.0) return@withContext null

    database.withTransaction {
      val existing = financeDao.getExpenseByNotes(reference)
      val record = existing?.copy(
        title = cleanTitle,
        amount = cleanAmount,
        categoryName = "Split Bill",
        type = type,
        bankAccountId = account.id,
        bankAccountName = account.bankName,
        notes = reference
      ) ?: ExpenseRecord(
        title = cleanTitle,
        amount = cleanAmount,
        categoryName = "Split Bill",
        type = type,
        bankAccountId = account.id,
        bankAccountName = account.bankName,
        notes = reference
      )
      financeDao.insertExpense(record)
    }
  }

  suspend fun deleteSplitBillTransaction(reference: String) = withContext(Dispatchers.IO) {
    if (reference.isBlank()) return@withContext
    database.withTransaction {
      financeDao.getExpenseByNotes(reference)?.let { financeDao.deleteExpense(it) }
    }
  }

  suspend fun addDebtWithOptionalLedger(
    personName: String,
    amount: Double,
    type: String,
    dueDateTimestamp: Long?,
    notes: String,
    bankAccountId: Long?,
    bankAccountName: String,
    adjustBankBalanceNow: Boolean
  ): DebtRecord = withContext(Dispatchers.IO) {
    val cleanAmount = MoneyMath.roundCurrency(amount)
    val now = System.currentTimeMillis()
    database.withTransaction {
      val debt = DebtRecord(
        personName = personName.trim(),
        amount = cleanAmount,
        type = type,
        dateTimestamp = now,
        dueDateTimestamp = dueDateTimestamp,
        isSettled = false,
        notes = notes.trim(),
        bankAccountId = bankAccountId,
        bankAccountName = bankAccountName
      )
      val newId = debtDao.insertDebt(debt)
      if (adjustBankBalanceNow && bankAccountId != null) {
        val ledgerType = if (type == "LENT") "EXPENSE" else "INCOME"
        val title = if (type == "LENT") "Lent to ${personName.trim()}" else "Borrowed from ${personName.trim()}"
        financeDao.insertExpense(
          ExpenseRecord(
            title = title,
            amount = cleanAmount,
            categoryName = "Debt & Lending",
            type = ledgerType,
            bankAccountId = bankAccountId,
            bankAccountName = bankAccountName,
            dateTimestamp = now,
            notes = "Recorded in Debt Manager",
            debtId = newId
          )
        )
      }
      debt.copy(id = newId)
    }
  }

  data class PaidEmiResult(
    val loanId: Long,
    val expenseId: Long,
    val emiNumber: Int,
    val loanName: String,
    val message: String
  )

  /**
   * Atomically pays a monthly EMI for a loan using the explicitly chosen bank account
   * (never silently switching to an unselected account).
   */
  suspend fun payLoanEmi(
    loan: LoanRecord,
    preferredBankAccountId: Long?
  ): Result<PaidEmiResult> = withContext(Dispatchers.IO) {
    val currentLoan = loanDao.getLoanById(loan.id) ?: loan
    if (currentLoan.isFullyPaid) {
      return@withContext Result.failure(Exception("Loan '${currentLoan.loanName}' is already fully paid."))
    }

    val allAccounts = financeDao.getAllBankAccounts().first()
    val explicitAccountId = preferredBankAccountId ?: currentLoan.bankAccountId
    val finalAccount = if (explicitAccountId != null) {
      allAccounts.find { it.id == explicitAccountId }
    } else {
      allAccounts.firstOrNull { !it.accountType.equals("Credit Card", ignoreCase = true) }
        ?: allAccounts.firstOrNull()
    }

    val emiNumber = currentLoan.paidEmisCount + 1
    val emiAmount = MoneyMath.roundCurrency(currentLoan.emiAmount)

    val expenseId = database.withTransaction {
      loanDao.incrementPaidEmis(currentLoan.id)
      financeDao.insertExpense(
        ExpenseRecord(
          title = "EMI: ${currentLoan.loanName} (#$emiNumber)",
          amount = emiAmount,
          categoryName = "Bills & Utilities",
          type = "EXPENSE",
          bankAccountId = finalAccount?.id,
          bankAccountName = finalAccount?.bankName ?: "",
          dateTimestamp = System.currentTimeMillis(),
          notes = "Auto-logged from Loan Manager. ${(currentLoan.tenureMonths - emiNumber).coerceAtLeast(0)} EMIs left.",
          loanId = currentLoan.id
        )
      )
    }

    Result.success(
      PaidEmiResult(
        loanId = currentLoan.id,
        expenseId = expenseId,
        emiNumber = emiNumber,
        loanName = currentLoan.loanName,
        message = "Paid EMI #$emiNumber (${currentLoan.loanName})"
      )
    )
  }

  /**
   * Atomically undoes a previously paid EMI and removes its corresponding Finance ledger record.
   */
  suspend fun undoPayLoanEmi(
    loanId: Long,
    expenseId: Long? = null
  ): Result<String> = withContext(Dispatchers.IO) {
    val loanName = database.withTransaction {
      loanDao.decrementPaidEmis(loanId)
      if (expenseId != null && expenseId > 0) {
        financeDao.getExpenseById(expenseId)?.let { financeDao.deleteExpense(it) }
      } else {
        financeDao.getExpensesByLoanId(loanId).firstOrNull()?.let { financeDao.deleteExpense(it) }
      }
      loanDao.getLoanById(loanId)?.loanName ?: "loan"
    }
    Result.success("Reverted EMI payment for $loanName")
  }

  /**
   * Atomically settles a debt and records the corresponding INCOME/EXPENSE on the selected account.
   * Never silently switches the user's selected bank account.
   */
  suspend fun settleDebt(
    debt: DebtRecord,
    preferredBankAccountId: Long?,
    adjustBankBalance: Boolean
  ): Result<String> = withContext(Dispatchers.IO) {
    if (debt.isSettled) {
      return@withContext Result.success("Debt with ${debt.personName} is already settled")
    }

    val allAccounts = financeDao.getAllBankAccounts().first()
    val cleanAmount = MoneyMath.roundCurrency(debt.amount)
    val now = System.currentTimeMillis()

    val message = database.withTransaction {
      debtDao.markSettled(debt.id, now)

      if (!adjustBankBalance || allAccounts.isEmpty()) {
        return@withTransaction if (!adjustBankBalance) {
          "Debt with ${debt.personName} marked as settled"
        } else {
          "Debt marked settled (no accounts available)"
        }
      }

      val targetAcc = (preferredBankAccountId ?: debt.bankAccountId)
        ?.let { id -> allAccounts.find { it.id == id } }
        ?: allAccounts.firstOrNull { !it.accountType.equals("Credit Card", ignoreCase = true) }
        ?: allAccounts.first()

      if (debt.type == "LENT") {
        financeDao.insertExpense(
          ExpenseRecord(
            title = "Settlement: ${debt.personName} repaid",
            amount = cleanAmount,
            categoryName = "Debt & Lending",
            type = "INCOME",
            bankAccountId = targetAcc.id,
            bankAccountName = targetAcc.bankName,
            dateTimestamp = now,
            notes = "Settled debt from Debt Manager",
            debtId = debt.id
          )
        )
        "Settled! Received into ${targetAcc.bankName}"
      } else {
        financeDao.insertExpense(
          ExpenseRecord(
            title = "Settlement: Repaid ${debt.personName}",
            amount = cleanAmount,
            categoryName = "Debt & Lending",
            type = "EXPENSE",
            bankAccountId = targetAcc.id,
            bankAccountName = targetAcc.bankName,
            dateTimestamp = now,
            notes = "Settled debt from Debt Manager",
            debtId = debt.id
          )
        )
        "Settled! Repaid from ${targetAcc.bankName}"
      }
    }

    Result.success(message)
  }

  /**
   * Atomically pays a credit card bill as a liability transfer (TRANSFER_OUT + TRANSFER_IN).
   * Does not double-count purchases in Total Expenses or Total Income.
   */
  suspend fun payCreditCardBill(
    cardAccountId: Long,
    fromBankAccountId: Long,
    amount: Double,
    notes: String = ""
  ): Result<String> = withContext(Dispatchers.IO) {
    val cleanAmount = MoneyMath.roundCurrency(amount)
    if (!cleanAmount.isFinite() || cleanAmount <= 0.0) {
      return@withContext Result.failure(Exception("Payment amount must be greater than zero."))
    }
    if (cardAccountId == fromBankAccountId) {
      return@withContext Result.failure(Exception("Select a bank or cash account to pay the card bill."))
    }

    val allAccounts = financeDao.getAllBankAccounts().first()
    val cardAcc = allAccounts.find { it.id == cardAccountId }
      ?: return@withContext Result.failure(Exception("Credit card account not found."))
    val fromAcc = allAccounts.find { it.id == fromBankAccountId }
      ?: return@withContext Result.failure(Exception("Payment bank account not found."))

    val timestamp = System.currentTimeMillis()
    val today = LocalDate.now().toEpochDay()

    database.withTransaction {
      financeDao.insertExpenses(
        listOf(
          ExpenseRecord(
            title = "Credit Card Bill: ${cardAcc.bankName}",
            amount = cleanAmount,
            categoryName = "Credit Card Bill",
            type = "TRANSFER_OUT",
            bankAccountId = fromBankAccountId,
            bankAccountName = fromAcc.bankName,
            dateTimestamp = timestamp,
            notes = notes.ifBlank { "Paid bill for ${cardAcc.bankName}" }
          ),
          ExpenseRecord(
            title = "Bill Payment Received (${fromAcc.bankName})",
            amount = cleanAmount,
            categoryName = "Credit Card Bill",
            type = "TRANSFER_IN",
            bankAccountId = cardAccountId,
            bankAccountName = cardAcc.bankName,
            dateTimestamp = timestamp + 1,
            notes = "Bill payment from ${fromAcc.bankName}"
          )
        )
      )
      financeDao.markCreditCardBillPaid(cardAccountId, today)
    }

    CreditCardBillNotificationScheduler.cancel(appContext, cardAccountId)

    Result.success(
      "Paid ${cardAcc.bankName} bill of ${CurrencyFormatter.formatWhole(cleanAmount)} from ${fromAcc.bankName}"
    )
  }

  companion object {
    @Volatile
    private var INSTANCE: FinanceRepository? = null

    fun getInstance(context: Context): FinanceRepository {
      return INSTANCE ?: synchronized(this) {
        val instance = FinanceRepository(context)
        INSTANCE = instance
        instance
      }
    }
  }
}
