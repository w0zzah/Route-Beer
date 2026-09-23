# SENG202 RouteBeer Application
An app to generate a route between multiple bars

## Authors
- Conrad Wilson (cwi184)
- Alistair Clarke (acl176)
- Neela van Gerwen (nva65, neovyer)
- Theo Baker (tba132, Theo Baker)

## Prerequisites
- JDK >= 21 [click here to get the latest stable OpenJDK release (as of writing this README)](https://jdk.java.net/18/)
- Gradle [Download](https://gradle.org/releases/) and [Install](https://gradle.org/install/)

## Importing Project (Using IntelliJ)
IntelliJ has built-in support for Gradle. To import this project:

- Launch IntelliJ and choose `Open` from the start up window.
- Select the project and click open
- At this point in the bottom right notifications you may be prompted to 'load gradle scripts', If so, click load

**Note:** *If you run into dependency issues when running the app or the Gradle pop up doesn't appear then open the Gradle sidebar and click the Refresh icon.*


## Build Project & Running app
1. Open a command line interface inside the project directory. e.g(~/Downloads/Route-Beer..)
2. Run the command `./gradlew shadowJar` to build the jar.
3. Navigate to the jars location using `cd build/libs`
4. Run the jar using `java -jar seng202_team5-1.0-SNAPSHOT-all.jar`

## Version 2.0 Features:
- Login to a remote database where user and route information are securely stored
- Generate a random route with only a start bar selected
- Generate a specific route with start, end, approximate distance, features and themes to sort by
- Save, delete and load generated route on a cloud account
- Publish & load community routes
- Export generated route to png