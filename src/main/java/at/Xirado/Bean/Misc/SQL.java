package at.Xirado.Bean.Misc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class SQL {


	public static Connection con = null;
	public static String host = null;
	public static int port = 3306;
	public static String database = null;
	public static String password = null;
	public static String username = null;
	
	
	public static void connect() {
		host = JSONConfig.config.get("Host");
		database = JSONConfig.config.get("Database");
		username = JSONConfig.config.get("Username");
		password = JSONConfig.config.get("Password");
		port = Integer.parseInt(JSONConfig.config.get("Port"));
		if(!isConnected()) {
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database+"?autoReconnect=true&useUnicode=yes&serverTimezone=UTC", username, password);
				System.out.println("MySQL connected successfully");
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("MySQL connection failed");
			}
		}
		
	}

	public static void disconnect() {
		if(isConnected()) {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static boolean isConnected() {
		return (con != null);
	}
	
	
	
	
	public static void initKeepAlive() {
		ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
		ses.scheduleAtFixedRate(new Runnable() {
		    @Override
		    public void run() {
		    	try
				{
					SQL.con.prepareStatement("SELECT 1").execute();
				} catch (SQLException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		}, 1, 1, TimeUnit.HOURS);
	}
	
	
}
