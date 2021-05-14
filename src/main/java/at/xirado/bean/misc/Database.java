package at.xirado.bean.misc;

import at.xirado.bean.Bean;
import at.xirado.bean.handlers.SQLHelper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;


public class Database
{


	private static final Logger logger = LoggerFactory.getLogger(Database.class);

	private static final HikariConfig config = new HikariConfig();
	private static HikariDataSource ds;

	public static Connection getConnectionFromPool()
	{
		try
		{
			return ds.getConnection();
		} catch (SQLException throwables)
		{
			logger.error("Could not get Connection from SQL-Pool!", throwables);
			return null;
		}
	}



	public static void connect(Runnable success) {
		Runnable r = () -> {
			if(!isConnected()) {
				String host = Bean.getInstance().config.getString("database.host");
				String database = Bean.getInstance().config.getString("database.database");
				String username = Bean.getInstance().config.getString("database.username");
				String password = Bean.getInstance().config.getString("database.password");
				int port = Bean.getInstance().config.getInt("database.port");
				config.setJdbcUrl("jdbc:mariadb://" + host + ":" + port + "/" + database);
				config.setUsername(username);
				config.setPassword(password);
				config.setMaximumPoolSize(40);
				config.setDriverClassName("org.mariadb.jdbc.Driver");
				config.setScheduledExecutor(Bean.getInstance().scheduledExecutorService);
				config.setThreadFactory(Bean.getInstance().namedThreadFactory);
				config.addDataSourceProperty("cachePrepStmts", "true");
				config.addDataSourceProperty("prepStmtCacheSize", "250");
				config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
				ds = new HikariDataSource(config);
				SQLHelper.createTables();
				success.run();
			}
		};
		Bean.getInstance().scheduledExecutorService.submit(r);
		
	}

	public static void disconnect() {
		ds.close();
	}
	
	public static boolean isConnected() {
		return (ds != null);
	}
}
