Solana Alpha Tracker
A Spring Boot application designed to filter through the noise of the Solana blockchain and identify high-potential trades. Originally built for Pump.fun/Raydium scanning, it is transitioning into a high-signal Alpha Wallet Tracker.


🚀 Current Features
The current version acts as a market scanner for newly tracked tokens:
Data Aggregation: Pulls the last 30 created tokens via the DexScreener API.
Market Analysis: Filters tokens based on Market Cap thresholds.
Volume Tracking: Analyzes trading volume across 5-minute and 1-hour intervals to find momentum.
Instant Alerts: Sends real-time "Potential Buy" notifications to a Telegram Bot.


🛠 Tech Stack
Framework: Spring Boot (Java)
APIs: DexScreener, Telegram Bot API
Blockchain: Solana



🏗 Roadmap: The "Alpha" Shift
The project is currently evolving from a general scanner into a targeted Wallet Tracker. The next phase will deprecate the DexScreener scan in favor of:
Helius Webhooks: Real-time monitoring of 20+ "Alpha" (high-win rate) wallets.
Buy Detection: A scheduler to parse webhook data and identify specific entry points from these wallets.
Refined Notifications: Pings the Telegram bot only when "Alpha" wallets take a position within a specific timeframe.
⚙️ Getting Started
(Note: Ensure you have your Telegram Bot Token and Helius API Key ready.)
