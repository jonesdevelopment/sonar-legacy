# ⚡ Sonar Antibot

## Intelligent Minecraft bot :wrench: and exploit :boom: protection

### Building

* [Download latest Waterfall](https://papermc.io/downloads#Waterfall) and put the jar in the project folder
* Rename the `waterfall-xxxx-000.jar` to `waterfall.jar`
* Add a gradle configuration with the arguments `clean shadowJar --stacktrace`
* Run the configuration

### Features
* Bad packet (Netty exploits) filter
* Join bot (Spam and slow join) prevention
  * Changeable re-connect delay
  * First join kick with verification
  * Custom name regex checks
  * Weird in-game behaviour (packets)
* Error message (Exception) filter
* Optimizing connection speed and stability
  * This is done using TCP_FASTOPEN
* Extremely customizable
  * Nearly 500 (452) lines of configurations

> [Read more](https://builtbybit.com/resources/23353/) about Sonar Antibot on the official plugin page

### Supported software
* BungeeCord
  * Waterfall
  * Travertine _(outdated)_
  * HexaCord _(outdated)_
  * _and more ..._
* Custom BungeeCord forks
  * XCord
  * FlameCord
  * BetterBungee
  * _and more ..._

> **Note** It is not guaranteed that all BungeeCord forks are supported.

:heavy_exclamation_mark: **Sonar is now officially discontinued**

This is due to it being mostly finished and jones not having enough time to provide proper support.
The source code will be available on GitHub and the unlicensed, outdated jar can still be downloaded on BuiltByBit.
Sonar being discontinued does **not** mean that you won't be able to get Sonar anywhere.
Sonar will be a community project, which everyone can contribute to.
Issues will not be ignored and bugs will be fixed if jones has time to do so.

> You won't be able to download the **latest version** (on BuiltByBit) anymore

Thank you for everything, especially to the customers and the people I've met along my journey.
