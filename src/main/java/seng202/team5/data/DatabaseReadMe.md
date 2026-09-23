# Datbase Methods
*Basic Database Documentation*
## Connecting to the database
**Attempts to form a [Connection](https://docs.oracle.com/javase/8/docs/api/java/sql/Connection.html) to the database using [DriverManager](https://docs.oracle.com/javase/8/docs/api/java/sql/DriverManager.html#getConnection-java.lang.String-)**
```java
public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(URL, USER, PASSWORD);
}
```
#
## Adding to the Database
**String in SQL format where Database Collumns are called in the beginning and their corresponding values are marked with ?**
```java
String query = """
        INSERT INTO routes (user_id, route_name, route_description, total_distance, estimated_time, created_date, route_locations_string)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
```
#
**Try's to create a connection to the database, if successful parse the statement shell into it**
```java
try (Connection connection = DatabaseConnection.getConnection();
     PreparedStatement statement = connection.prepareStatement(query))
```
#
**Set the Java values to be inserted into the database respective of their order specified in "String query = ..."**
```java
    statement.setInt(1, user_id);
    statement.setString(2, route.getName());
    statement.setString(3, route.getDescription())...
```
#
**Represent the amount of rows updated after query**
```java
    return statement.executeUpdate() > 0;
```
## Checking the Database
**Set the query, parse in other java values respective of question mark position**
```java
String query = "SELECT *, route_locations FROM routes WHERE user_id = ?", userId);
```
#
**Try's to create a connection to the database, if successful parse the statement shell into it**
```java
try (Connection connection = DatabaseConnection.getConnection();
     PreparedStatement statement = connection.prepareStatement(query))
```
#
**Create a "ResultSet" that will hold all the information from specified query, then loop through each with a while loop**
```java
try (ResultSet resultSet = statement.executeQuery()) {
```
#
**Use either "while" or "if" to "get all" or "get specifically"**
```java
    while (resultSet.next()) {
    if (resultSet.next()) {
```
#
**Assign Java Values to the values from the database with resultSet.get();**
```java
    int id = resultSet.getInt("id");
    String routeName = resultSet.getString("route_name");
    String description = resultSet.getString("route_description");
```