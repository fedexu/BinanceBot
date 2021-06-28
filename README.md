# BinanceBot

Bot with the aim of evaluating the Crypto market of a specific commercial market to sell and buy at the best price.

The bot make the decisions using various indices such as:
- EMA
- MACD
- RSI

## How it works

Brief diagram explanation:

<p align="center">
  <a target="_blank"><img src="https://user-images.githubusercontent.com/22296699/123637802-b7406000-d81e-11eb-8ef9-0c365afef15d.png" width="800" alt="Logic" /></a>
</p>

The bot use the API key generated from Binance to connect to the desired wallet to manage the balance.

There is an <b>optional</b> integration with Telegram API to check the bot status and information about configured market and buy/sell prices.


## Installation

To install and run the application correctly you need to run those two commands form the project directory:

<code> mvn install:install-file -Dfile=src/main/resources/lib/ta-lib-0.4.0.jar -DgroupId=com.tictactec.ta.lib -DartifactId=tictactec -Dversion=1.0 -Dpackaging=jar</code>

After that is required a file secret.yaml to be created at resource folder with the right API keys desired to be used/integrated with.
