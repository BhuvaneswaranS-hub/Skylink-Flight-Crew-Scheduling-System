package util;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Driver;
import java.sql.SQLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class DatabaseConnection {
    private static final String URL =
            "jdbc:mysql://localhost:3306/crew_management";

    private static final String USER = System.getenv().getOrDefault("SKYLINK_DB_USER", "");

    private static final String PASSWORD = System.getenv().getOrDefault("SKYLINK_DB_PASSWORD", "");
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    private static final String DRIVER_JAR = "mysql-connector-j-26.7.0.jar";
    private static volatile ClassLoader bundledDriverLoader;

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            return connectWithBundledDriver(e);
        }
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("No suitable driver"))
                return connectWithBundledDriver(e);
            throw e;
        }
    }

    private static Connection connectWithBundledDriver(Exception original) throws SQLException {
        try {
            Driver driver = loadBundledDriver();
            Properties properties = new Properties();
            properties.setProperty("user", USER);
            properties.setProperty("password", PASSWORD);
            Connection connection = driver.connect(URL, properties);
            if (connection == null) throw new SQLException("Bundled MySQL driver does not recognize the configured JDBC URL.");
            return connection;
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException("MySQL Connector/J could not be loaded. Expected lib/" + DRIVER_JAR + " beside the application.", original == null ? e : original);
        }
    }

    private static Driver loadBundledDriver() throws Exception {
        ClassLoader loader = bundledDriverLoader;
        if (loader == null) {
            synchronized (DatabaseConnection.class) {
                loader = bundledDriverLoader;
                if (loader == null) {
                    Path jar = findDriverJar();
                    if (jar == null) throw new SQLException("Connector JAR not found. Expected lib/" + DRIVER_JAR + " beside the application.");
                    URL url = jar.toUri().toURL();
                    loader = new URLClassLoader(new URL[]{url}, DatabaseConnection.class.getClassLoader());
                    bundledDriverLoader = loader;
                }
            }
        }
        Class<?> driverType = Class.forName(DRIVER_CLASS, true, loader);
        return (Driver) driverType.getDeclaredConstructor().newInstance();
    }

    private static Path findDriverJar() {
        List<Path> candidates = new ArrayList<>();
        candidates.add(Paths.get(System.getProperty("user.dir", "."), "lib", DRIVER_JAR));
        try {
            Path classes = Paths.get(DatabaseConnection.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path base = Files.isDirectory(classes) ? classes : classes.getParent();
            if (base != null) {
                candidates.add(base.resolve("lib").resolve(DRIVER_JAR));
                if (base.getParent() != null) candidates.add(base.getParent().resolve("lib").resolve(DRIVER_JAR));
            }
        } catch (Exception ignored) { }
        for (Path candidate : candidates) if (Files.isRegularFile(candidate)) return candidate;
        return null;
    }
}
