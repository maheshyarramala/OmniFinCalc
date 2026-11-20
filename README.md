# OmniFinCalc

**OmniFinCalc** is an ad-free, privacy-first personal finance dashboard, ledger, analytics engine, and comprehensive financial calculator suite built natively for Android using Kotlin and Jetpack Compose.

It brings together real-time account balances, expenses, income, transfers, budgets, credit card bill tracking, debt management, active loan tracking, group bill splitting, financial reports, and precision calculators in one offline-first application.

---

## Why OmniFinCalc?

* **Zero Ads, Zero Trackers**: No banner ads, no popups, and no third-party tracking SDKs.
* **100% Offline-First**: All transactions, accounts, loan schedules, split bills, and calculation history stay securely on your device.
* **Precision Accounting**: Uses exact decimal/minor-unit financial calculations and atomic Room database transactions to prevent partial or duplicate records.
* **Premium Fintech Design**: Supports Light, Dark, and pure AMOLED themes with high-contrast typography and adaptive navigation.

---

## Key Features

### 1. Personal Finance Dashboard & Analytics
* **Real-Time Overview**: Live total balance, income, expenses, monthly savings, account balances, monthly budget progress, upcoming bills, debt overview, loan overview, and recent transactions.
* **Advanced Reports & Analytics**: Interactive period filters (This week, This month, Last month, Last 3 months, Last 6 months, This year, Custom date range) with real-data charts for income vs. expenses, daily/weekly spending, category breakdowns, cash flow, savings trends, account analytics, budget performance, debt analytics, and loan repayment progress.
* **CSV & PDF Exports**: Export formatted financial statements, EMI amortization schedules, and trip ledgers.

### 2. Accounts, Budgets, Loans, Debts & Split Bill
* **Accounts & Transfers**: Manage Bank, Cash, and Credit Card accounts with atomic account-to-account transfers and credit card bill settlement.
* **Active Loan & EMI Tracker**: Track Home, Car, Personal, and Gadget loans with due date alerts, one-tap EMI logging, prepayment simulations, and amortization breakdowns.
* **Debt Tracker (Lend & Borrow)**: Track money lent and borrowed with settlement syncing and payment reminders.
* **Split Bill**: Multi-group expense splitting with participant shares, simplified settlements, UPI payment deep links, and atomic Finance ledger synchronization.
* **Recurring Bills & Trip Budgets**: Track subscriptions and vacation spending caps.

### 3. Complete Financial & Utility Calculators
* **Basic & Scientific Calculator** with saved history and favorites.
* **EMI & Loan Prepayment Calculator** with interactive amortization schedules.
* **Investment Calculators**: SIP, Step-Up SIP, Lump Sum, Goal Target Planner, Compound Interest, PPF, Fixed Deposit, Stock Average, and Equity Trade P&L.
* **Interest Calculator**: Simple Interest, Compound Interest, Rule of 72, Effective Annual Rate (EAR), and Flat vs. Reducing Rate.
* **Tax, Salary & Utilities**: Income Tax (Old vs. New Regime), CTC to In-Hand Salary, GST Calculator, Currency Converter, Unit Converter, Age & Calendar calculators.

### 4. Backup, Security & Data Reliability
* **Biometric App Lock** for finance records.
* **Universal JSON Backup & Restore** compatible with both legacy OmniCalc and OmniFinCalc backups.
* **Non-Destructive Room Migrations** preserving existing user records across app upgrades.

---

## Technical Stack

* **Language**: Kotlin
* **UI Framework**: Jetpack Compose (Material 3)
* **Architecture**: MVVM + Repository Pattern
* **Local Storage**: Room Database (SQLite) with explicit migrations
* **Concurrency**: Kotlin Coroutines & StateFlow
* **Notifications**: Android NotificationManager & AlarmManager

---

## About the Author

* **Developer**: Mahesh Yarramala
* **Email**: [devops.maheshbabu@gmail.com](mailto:devops.maheshbabu@gmail.com)
* **Instagram**: [@mahesh.yarramala](https://instagram.com/mahesh.yarramala)

---

## Support & Donate

If **OmniFinCalc** helps organize your personal finances and calculations, consider supporting ongoing development:

* **UPI ID**: `maheshyarramala@ybl`
* **Direct UPI Link**: [Donate via UPI](upi://pay?pa=maheshyarramala@ybl&pn=Mahesh%20Yarramala&cu=INR)

---

## Privacy Policy & Terms

* **Privacy**: OmniFinCalc does not collect, transmit, or monetize your personal or financial data. Everything remains on your local device storage.
* **Terms**: Financial calculations and EMI projections are provided for informational and planning purposes. Always verify final figures with your financial institution.
