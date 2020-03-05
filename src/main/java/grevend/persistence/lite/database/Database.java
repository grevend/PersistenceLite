package grevend.persistence.lite.database;

import grevend.persistence.lite.dao.DaoFactory;
import grevend.persistence.lite.dao.memory.InMemoryDaoFactory;
import grevend.persistence.lite.dao.postgresql.PostgresqlDaoFactory;
import grevend.persistence.lite.entity.EntityManager;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;

public class Database {

    public static final String SQL = "sql";
    public static final String MEMORY = "memory";

    private final String type, url, name, user, password;
    private final int version;

    public Database(@NotNull String type, @NotNull String url,
                    @NotNull String name, @NotNull String user, @NotNull String password, int version) {
        this.type = type;
        this.url = url;
        this.name = name;
        this.user = user;
        this.password = password;
        this.version = version;
    }

    public @NotNull String getType() {
        return type;
    }

    public @NotNull String getUrl() {
        return url;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getUser() {
        return user;
    }

    public int getVersion() {
        return version;
    }

    public @NotNull DaoFactory getDaoFactory() {
        return this.type.equals(SQL) ? new PostgresqlDaoFactory(this) : new InMemoryDaoFactory(this);
    }

    public @NotNull EntityManager getEntityManager() {
        return new EntityManager(this);
    }

    public @NotNull Optional<DatabaseMetaData> getMetaData(@NotNull Connection connection) {
        if (this.type.equals(SQL)) {
            try {
                return Optional.ofNullable(connection.getMetaData());
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    public Connection createConnection() throws SQLException, IllegalAccessException {
        if (this.type.equals(SQL)) {
            Properties props = new Properties();
            props.setProperty("user", this.user);
            props.setProperty("password", this.password);
            return DriverManager.getConnection(this.url + this.name, props);
        } else {
            throw new IllegalAccessException("Database type must be SQL to create an sql connection.");
        }
    }

}
