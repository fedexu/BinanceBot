<p align="center">
  <a target="_blank"><img src="https://spring.io/images/projects/spring-edf462fec682b9d48cf628eaf9e19521.svg" width="120" alt="Spring Logo" /></a>
  <a target="_blank"><img src="https://user-images.githubusercontent.com/22296699/123639271-4e59e780-d820-11eb-8f40-5bff71c494d8.png" width="120" alt="Telegram Logo" /></a>
  <a target="_blank"><img src="https://user-images.githubusercontent.com/22296699/123639394-6af61f80-d820-11eb-8f25-3555a2271762.png" width="120" alt="Binance Logo" /></a>
</p>

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/fedexu/love-article/blob/master/LICENSE)
[![sonar qube](https://sonarcloud.io/api/project_badges/measure?project=fedexu_BinanceBot&metric=alert_status)](https://sonarcloud.io/dashboard?id=fedexu_BinanceBot)

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
