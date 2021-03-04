package at.Xirado.Bean.Misc;

import at.Xirado.Bean.Logging.Console;
import org.mariadb.jdbc.MariaDbPoolDataSource;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class SQL {


	public static Logger logger = Console.logger;

	private static MariaDbPoolDataSource dataSource = null;

	public static Connection getConnectionFromPool()
	{
		try
		{
			return dataSource.getConnection();
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
			try {
				Class.forName("org.mariadb.jdbc.Driver");
			} catch (ClassNotFoundException e1) {
				logger.error("MariaDB Driver-class not found!", e1);
			}
			try {
				MariaDbPoolDataSource x = new MariaDbPoolDataSource("jdbc:mariadb://" + host + ":" + port + "/" + database +"?user="+ username +"&password="+ password);
				x.setMaxPoolSize(100);
				x.setMinPoolSize(10);
				dataSource = x;
				logger.info("Successfully created Database-Pool!");
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error("MySQL-Connection failed!");
			}
		}
		
	}

	public static void disconnect() {
		dataSource.close();
	}
	
	public static boolean isConnected() {
		return (dataSource != null);
	}
	
	
	
	
	public static void initKeepAlive() {
		ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
		ses.scheduleAtFixedRate(() ->
		{
			try(PreparedStatement ps = dataSource.getConnection().prepareStatement("SELECT 1"))
			{
				ps.execute();
			} catch (SQLException e)
			{
				Console.logger.error("SQL-Keepalive failed", e);
			}
		}, 1, 1, TimeUnit.HOURS);
	}
	
	
}
