package at.xirado.bean.misc;

import at.xirado.bean.main.DiscordBot;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class SQL {


	private static final Logger logger = LoggerFactory.getLogger(SQL.class);

	private static HikariConfig config = new HikariConfig();
	private static HikariDataSource ds;

	public static Connection getConnectionFromPool()
	{
		try
		{
			logger.debug("Gave Connection instance to "+Thread.currentThread().getName());
			return ds.getConnection();
		} catch (SQLException throwables)
		{
			logger.error("Could not get Connection from SQL-Pool!", throwables);
			return null;
		}
	}



	public static void connect() {

		logger.info("Connecting to MySQL-Database...");
		String host = JSONConfig.config.get("Host");
		String database = JSONConfig.config.get("Database");
		String username = JSONConfig.config.get("Username");
		String password = JSONConfig.config.get("Password");
		int port = Integer.parseInt(JSONConfig.config.get("Port"));
		if(!isConnected()) {

			config.setJdbcUrl("jdbc:mariadb://" + host + ":" + port + "/" + database);
			config.setUsername(username);
			config.setPassword(password);
			config.setMaximumPoolSize(20);
			config.setDriverClassName("org.mariadb.jdbc.Driver");
			config.setScheduledExecutor(DiscordBot.getInstance().scheduledExecutorService);
			config.setThreadFactory(DiscordBot.getInstance().namedThreadFactory);
			config.addDataSourceProperty("cachePrepStmts", "true");
			config.addDataSourceProperty("prepStmtCacheSize", "250");
			config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");



			ds = new HikariDataSource(config);
			logger.info("Successfully created Database-Pool!");
		}
		
	}

	public static void disconnect() {
		ds.close();
	}
	
	public static boolean isConnected() {
		return (ds != null);
	}
	
	
	
	
	public static void initKeepAlive() {
		DiscordBot.getInstance().scheduledExecutorService.scheduleAtFixedRate(() ->
		{
			Connection connection = getConnectionFromPool();
			if(connection == null) return;
			try(PreparedStatement ps = connection.prepareStatement("SELECT 1"))
			{
				ps.execute();
			} catch (SQLException e)
			{
				logger.error("SQL-Keepalive failed", e);
			} finally
			{
				Util.closeQuietly(connection);
			}
		}, 1, 1, TimeUnit.HOURS);
	}
}
