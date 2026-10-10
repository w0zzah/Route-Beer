package seng202.team5.data;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Connect's to the UC hosted database.
 * *URL:* "jdbc:dbtype://serverurl/databaseName"
 * *USER:* Username required to sign in
 * *PASSWORD:* Password required to sign in
 */
public class DatabaseConnection {
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    private static final String URL = dotenv.get("DB_URL");
    private static final String USER = "seng202team5";
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");

    /**
     * Attempts to form a connection the database using predefined values
     *
     * @return A connection to the database allowing you to execute query's
     * @throws SQLException the sql exception
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
