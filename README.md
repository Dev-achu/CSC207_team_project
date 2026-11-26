# <Project Name>: Portfolio Tracker & Rebalancer

A portfolio tracking app for beginner and intermediate retail investors who hold a mix of stocks, bonds, and commodities. Users record their holdings, look up securities, see how their portfolio performs against market indexes and inflation, and get a concrete plan for rebalancing back to their target allocation. The app never places trades; it only informs and plans.

## Team

| Member | GitHub | Lead use case | Supports |
|--------|--------|---------------|----------|
| Member A | @harounaiaydeh-dev| UC1: Account & Session | UC2 |
| Ryan | @ryanssaleh | UC2: Portfolio Dashboard | UC1 |
| Diego | @diegoAisa10 | UC3: Stock Lookup & Evaluator | UC4 |
| Achu | @dev-Achu | UC4: Performance vs. Benchmark & Inflation | UC3 |
| Member E | @seanb12345 | UC5: Rebalancing Planner | UC2 |

Every member leads exactly one use case.

## MVP Scope

The MVP is the minimal set below. Anything else is a stretch goal and is only started once all five use cases work end to end.

### UC1: Account & Session (Lead: Haroun)
- **User story:** As an investor, I want to sign up, log in, and log out so my portfolio is private and saved.
- **Main flow:** Enter username and password → validate → create or load the user → show the dashboard. Logout clears the session.
- **Error cases:** Username taken, empty or weak password, wrong credentials, user not found.

### UC2: Portfolio Dashboard (Lead: Ryan)
- **User story:** As an investor, I want to enter my holdings and trades and see total value and allocation by asset class.
- **Main flow:** Add a transaction (ticker, buy/sell, quantity, price, date) → update holdings → fetch current prices → show total value and allocation chart.
- **Error cases:** Unknown ticker, selling more than owned, negative quantity or price, price fetch failure (fall back to last known price).

### UC3: Stock Lookup & Evaluator (Lead: Diego)
- **User story:** As an investor, I want to search a ticker and see its price history and key metrics before I buy.
- **Main flow:** Enter ticker → fetch quote, history, and metrics (e.g. P/E, 52-week range) → display with a price chart → option to add to a portfolio.
- **Error cases:** Ticker not found, API rate limit hit, network failure, missing metrics for the security.

### UC4: Performance vs. Benchmark & Inflation (Lead: Achu)
- **User story:** As an investor, I want to compare my returns to a market index and to inflation, to know if I am actually beating them.
- **Main flow:** Select portfolio, date range, and benchmark (S&P 500, TSX) → compute portfolio return → fetch benchmark and CPI over the same range → show side-by-side chart and summary (nominal and inflation-adjusted).
- **Error cases:** Portfolio has no history in the range, benchmark or CPI data unavailable, invalid date range.
- **Note:** MVP uses simple total return. Time-weighted return and XIRR are stretch goals.

### UC5: Rebalancing Planner (Lead: Sean)
- **User story:** As an investor, I want to set target ratios across asset classes and see exactly what to buy or sell to get back on target.
- **Main flow:** Set targets (sum to 100%) → compute current weights and drift → if drift exceeds a threshold, output buy/sell amounts per asset class (and per holding if feasible) → display the plan.
- **Error cases:** Targets do not sum to 100%, empty portfolio, negative targets, no rebalancing needed (report this clearly).

### Stretch goals (in priority order)
1. Simple and compound interest profit calculator (small, standalone interactor)
2. Brokerage CSV import
3. Cost-basis (adjusted cost base) and realized gains
4. Time-weighted return and XIRR
5. Risk analytics (volatility, max drawdown, Sharpe)

## Core Entities

Entities have no dependency on APIs, databases, or UI.

| Entity | Fields | Notes |
|--------|--------|-------|
| `User` | id, username, passwordHash | |
| `Security` | ticker, name, assetClass | assetClass: STOCK, BOND, COMMODITY, CASH |
| `Transaction` | id, security, type, quantity, price, date | type: BUY or SELL |
| `Holding` | security, quantity, averageCost | derived from transactions |
| `Portfolio` | id, owner, name, transactions, holdings | computes totals and allocation |
| `PricePoint` | security, date, close | |
| `Benchmark` | name, symbol, series of PricePoint | also used for CPI |
| `TargetAllocation` | map of assetClass → weight | validates sum = 100% |
| `RebalanceAction` | security or assetClass, side, amount | |
| `RebalancePlan` | list of RebalanceAction, driftSummary | |

## Architecture

We use Clean Architecture. Dependencies point inward only.

```
UI / View  →  Controller  →  Input Boundary  →  Interactor  →  Entities
                                                    │
                          Output Boundary ← Presenter ← ViewModel ← View
                                                    │
                                          DataAccess interface
                                                    ↑
                              API / DB implementation (outermost layer)
```

Every use case has: `Controller`, `InputBoundary`, `Interactor`, `OutputBoundary`, `Presenter`, `ViewModel`, `View`, `InputData`, `OutputData`, and the DataAccess interfaces it needs.

### Package layout

```
src/
  entity/
  use_case/
    account/
    dashboard/
    stock_lookup/
    performance/
    rebalance/
  interface_adapter/
    account/ dashboard/ stock_lookup/ performance/ rebalance/
  data_access/
    AlphaVantageStockDataAccess (market API, with caching)
    InflationDataGateway    (CPI source)
    UserRepository
    PortfolioRepository
  view/
  app/                      (dependency wiring, entry point)
tests/
  entity/ use_case/ data_access/
```

### Shared DataAccess interfaces
- `StockLookupDataAccessInterface`: quote, price history, key metrics. **Owner: Member C.**
- `InflationDataGateway`: CPI series. **Owner: Member D.**
- `PortfolioRepository`, `UserRepository`: persistence. **Owners: Members B and A.**

Interactors depend only on these interfaces so tests can use in-memory stubs.

## Tech & APIs

- **Language / UI:** <fill in>
- **Market data (UC3):** [Alpha Vantage](https://www.alphavantage.co/documentation/) provides the latest end-of-day quote, up to 100 daily closes, and available fundamentals.
- **Inflation data:** <FRED or Statistics Canada>
- **Persistence:** <file/JSON, SQLite, or similar>
- **Testing:** <JUnit / pytest>, with stubbed data access
- **API keys:** stored in a local `.env` file, never committed. See Setup.

## Setup & Running

1. Clone the repo.
2. Copy `.env.example` to `.env` and add your API keys.
3. Build and run: `<commands>`
4. Run tests: `<commands>`

### Run the stock lookup demo

1. Use Java 11 or newer and Maven. Copy `.env.example` to `.env` and set `ALPHAVANTAGE_API_KEY` to your Alpha Vantage key. You can also set the environment variable directly.
2. Run `mvn compile exec:java "-Dexec.mainClass=app.StockLookupApp"`.
3. Look up a stock such as `AAPL`. Select **Add to portfolio** to record a buy with your quantity, actual trade price, and date. The demo holds purchases in memory for the current session.
4. Run `mvn test` to verify the use case and data access implementation with stubbed market responses.

A new ticker can use three requests. The standard free key allows [25 requests per day](https://www.alphavantage.co/support/api-key/). Successful data is cached in memory for an hour, and the screen labels cached data when a refresh fails. Missing fundamentals appear as **Unavailable**.

## Milestones

| Phase | Goal | Done when |
|-------|------|-----------|
| 1 | Repo, README, entities, package skeleton | Entities have unit tests; all leads have a branch |
| 2 | Interactors and stub UIs per use case | Each interactor passes tests with stub data access implementations |
| 3 | Real data and persistence wired in | Log in → add holding → see value works end to end |
| 4 | Remaining UCs integrated, polish | All five use cases demoable |
| 5 | Stretch goals, bug fixes, final demo | Demo script rehearsed |

## Team Workflow

- `main` is protected; all changes go through pull requests.
- Branch naming: `uc<number>-<short-description>`.
- Each PR needs at least one review from another member.
- Keep entities stable; changes to entities need a heads-up in the team chat.
- Weekly sync: <day/time>. Update the progress checklist below after each one.

## Progress Checklist

- [ ] Repo created and shared with all members
- [ ] README blueprint finalized
- [ ] Use cases assigned
- [ ] Entities implemented and tested
- [ ] UC1 Account & Session
- [ ] UC2 Portfolio Dashboard
- [ ] UC3 Stock Lookup & Evaluator
- [ ] UC4 Performance vs. Benchmark & Inflation
- [ ] UC5 Rebalancing Planner

## Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| API rate limits | Cache responses; use stubs in tests; keep a small set of demo tickers |
| API outage during demo | Fall back to cached data |
| Scope creep | Stretch goals start only after all five UCs work |
| Merge conflicts in shared entities | Lock entities early; small, frequent PRs |
| Uneven workload | Review progress weekly; supporting members help the lead who is behind |
