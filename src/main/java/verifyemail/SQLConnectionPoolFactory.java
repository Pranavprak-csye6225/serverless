package verifyemail;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

import java.util.logging.Logger;

import static com.mysql.cj.conf.PropertyKey.logger;

public class SQLConnectionPoolFactory{
    private static final Logger logger = Logger.getLogger(SQLConnectionPoolFactory.class.getName());
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASS = System.getenv("DB_PASS");
    private static final String DB_NAME = System.getenv("DB_NAME");
    private static final String INSTANCE_HOST = System.getenv("INSTANCE_HOST");

    public static DataSource createConnectionPool() {
        logger.info("createConnectionPool");
        // The configuration object specifies behaviors for the connection pool.
        HikariConfig config = new HikariConfig();
        // Configure which instance and what database user to connect with.
        config.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s", INSTANCE_HOST, "3306", DB_NAME));
        config.setUsername(DB_USER);
        config.setPassword(DB_PASS);

        return new HikariDataSource(config);
    }
}
