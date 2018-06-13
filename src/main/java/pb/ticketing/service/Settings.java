package pb.ticketing.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import com.google.common.base.Strings;

public final class Settings {
	private static Logger LOGGER = Logger.getLogger(Settings.class);
	
	private Settings() {
	}
	
	public static String getSqlServerConnectionString(String databaseServer, String databaseName, String userName, String password)
	{
		if (Strings.isNullOrEmpty(databaseServer)
				|| Strings.isNullOrEmpty(databaseName)
				|| Strings.isNullOrEmpty(userName)
				|| Strings.isNullOrEmpty(password)) {
			LOGGER.error("Database server information is missing.");
			return null;
		}


		return String.format("jdbc:sqlserver://%s;databaseName=%s;user=%s;password=%s;Max Pool Size=50", databaseServer, databaseName, userName, password);
	}
	
	public static Connection getSQLServerConnection(String sqlserverConenctionString) {
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		} catch (ClassNotFoundException e) {
			LOGGER.error(e.getMessage());
			return null;
		}


		if (Strings.isNullOrEmpty(sqlserverConenctionString)) {
			return null;
		}

		Connection sqlConnection = null;
		try {
			sqlConnection = DriverManager.getConnection(sqlserverConenctionString);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
		}

		return sqlConnection;
	}
}
