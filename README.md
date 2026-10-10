# SENG202 RouteBeer Application

An app that generates a route between multiple bars.

## Authors

- Conrad Wilson (cwi184)
- Alistair Clarke (acl176)
- Neela van Gerwen (nva65, neovyer)
- Theo Baker (tba132)

## Prerequisites

- [JDK 21 or later](https://jdk.java.net/21/)

Gradle does not need to be installed separately. The included Gradle wrapper (`./gradlew`) downloads the correct version automatically.

## Importing the Project (IntelliJ)

IntelliJ has built-in Gradle support. To import the project:

1. Launch IntelliJ and choose **Open** from the start-up window.
2. Select the project folder and click **Open**.
3. If a notification appears in the bottom right asking to load Gradle scripts, click **Load**.

> **Note:** If you hit dependency issues when running the app, or the Gradle prompt doesn't appear, open the Gradle sidebar and click the **Refresh** icon.

## Building and Running

1. Open a terminal in the project directory (e.g. `~/Downloads/Route-Beer`).
2. Build the jar:
```bash
   ./gradlew shadowJar
```
3. Run it:
```bash
   java -jar build/libs/seng202_team5-1.0-SNAPSHOT-all.jar
```

## Features

### Version 2.0

- Log in to a remote database where user and route information are securely stored
- Generate a random route by selecting only a start bar
- Generate a specific route by choosing a start, an end, an approximate distance, and features and themes to sort by
- Save, delete and load generated routes on a cloud account
- Publish and load community routes
- Export a generated route to PNG

### Version 3.0

- Rate other users' routes
- Numbered icons for bars
- Individual toggling of amenities
- Individual bar information display

## Testing

Before running the tests, make sure the database URL ends in `_dev`.

To run the unit, E2E and acceptance tests:

```bash
./gradlew check --continue
```

### Environment File Template

Use the template below, and get the actual values from MatterMost.

```
API_KEY={geoapify_api_key}
DB_PASSWORD={database_password}
DB_URL=jdbc:{database_url}
```

## Highlight features
- Generate a route
    - The user is able to select parameters and generate a personalised route
- Save a route
    - The user is able to save a route to their account to view later. They can also publish this route to a public tab for others to view
- Export a route
    -  The user is able to export a route as an image to be able to view on a night out
