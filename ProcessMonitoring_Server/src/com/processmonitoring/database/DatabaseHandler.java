package com.processmonitoring.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class DatabaseHandler {
	private static Connection mConnection;
	private static Statement mStatement;
	private static PreparedStatement preparedStatement;

	public static void initDatabase() {
		try {
			// Get a connection to database
			mConnection = DriverManager.getConnection("jdbc:mysql://localhost/?user=root&password=12345");
			mStatement = mConnection.createStatement();

			createDatabase();

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	private static void createDatabase() {
		try {
			mStatement.executeUpdate(
					"CREATE DATABASE IF NOT EXISTS process_monitoring_database DEFAULT CHARACTER SET utf8");

			String sql = "CREATE TABLE IF NOT EXISTS process_monitoring_database.account ( "
					 + " account_id INT NOT NULL AUTO_INCREMENT, "
					 + " login VARCHAR(50) NOT NULL, "
					 + " password VARCHAR(50) NOT NULL, "
					 + " masterkey VARCHAR(50) NOT NULL, "
					 + " PRIMARY KEY (account_id), "
					 + " UNIQUE INDEX account_id_UNIQUE (account_id ASC), "
					 + " UNIQUE INDEX login_UNIQUE (login ASC)) "
					 + " ENGINE = InnoDB "
					 + " DEFAULT CHARACTER SET = utf8;";

			mStatement.executeUpdate(sql);
			sql = "CREATE TABLE IF NOT EXISTS process_monitoring_database.devices ( "
					+ " user_id INT NOT NULL AUTO_INCREMENT, " + " account_id INT NOT NULL, "
					+ " user_name VARCHAR(50) NOT NULL, " + " token VARCHAR(255) NULL, " + " apps MEDIUMTEXT NULL, "
					+ " latitude FLOAT NULL, " + " longtitude FLOAT NULL, " + " PRIMARY KEY ( user_id ), "
					+ " UNIQUE INDEX user_id_UNIQUE (user_id ASC), " + " INDEX account_id_idx (account_id ASC), "
					+ " CONSTRAINT account_id " + " FOREIGN KEY ( account_id ) "
					+ " REFERENCES process_monitoring_database.account ( account_id ) " + " ON DELETE NO ACTION "
					+ " ON UPDATE NO ACTION) " + " ENGINE = InnoDB " + " DEFAULT CHARACTER SET = utf8;";

			mStatement.executeUpdate(sql);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void addAccount(String login, String password, String mKey) {
		String sql = "INSERT INTO process_monitoring_database.account " + "(`login`, `password`, `masterkey`) VALUES (?, ?, ?)";
		try {
			preparedStatement = mConnection.prepareStatement(sql);
			preparedStatement.setString(1, login);
			preparedStatement.setString(2, password);
			preparedStatement.setString(3, mKey);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void addDevice(String login, String userName, String token, String apps, double latitude,
			double longtitude) {
		int accountID = getAccountID(login);

		String sql = "INSERT INTO process_monitoring_database.devices "
				+ "(account_id, user_name, token, apps, latitude, longtitude) " + "VALUES (?, ?, ?, ?, ?, ?);";

		try {
			preparedStatement = mConnection.prepareStatement(sql);
			preparedStatement.setInt(1, accountID);
			preparedStatement.setString(2, userName);
			preparedStatement.setString(3, token);
			preparedStatement.setString(4, apps);
			preparedStatement.setDouble(5, latitude);
			preparedStatement.setDouble(6, longtitude);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static int getAccountID(String login) {
		int accountID = -1;
		try {
			String sql = "SELECT * FROM process_monitoring_database.account WHERE login=?";
			preparedStatement = mConnection.prepareStatement(sql);
			preparedStatement.setString(1, login);

			ResultSet rs = preparedStatement.executeQuery();
			rs.next();
			accountID = rs.getInt("account_id");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return accountID;
	}

	public static Map<Integer, String> getDevices(String login) {
		Map<Integer, String> listDevices = new HashMap<Integer, String>();
		try {
			int accountID = getAccountID(login);

			if (accountID != -1) {
				String sql = "SELECT * FROM process_monitoring_database.devices WHERE account_id=?";
				PreparedStatement preparedStatement = mConnection.prepareStatement(sql);
				preparedStatement.setInt(1, accountID);

				ResultSet rs = preparedStatement.executeQuery();
				while (rs.next()) {
					// System.out.println(rs.getInt("user_id") + " " +
					// rs.getString("user_name"));
					listDevices.put(rs.getInt("user_id"), rs.getString("user_name"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return listDevices;
	}

	public static String getListApps(int userID) {
		String listApps = null;
		try {
			String sql = "SELECT * FROM process_monitoring_database.devices WHERE user_id=?";
			preparedStatement = mConnection.prepareStatement(sql);
			preparedStatement.setInt(1, userID);

			ResultSet rs = preparedStatement.executeQuery();
			rs.next();
			listApps = rs.getString("apps");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return listApps;
	}

	public static void updateDeviceInfo(int userID, String token, String apps, double latitude, double longtitude) {
		String sql = "UPDATE process_monitoring_database.devices SET token=?, apps=?, latitude=?, longtitude=? WHERE user_id=?";

		try {
			preparedStatement = mConnection.prepareStatement(sql);
			preparedStatement.setString(1, token);
			preparedStatement.setString(2, apps);
			preparedStatement.setDouble(3, latitude);
			preparedStatement.setDouble(4, longtitude);
			preparedStatement.setInt(5, userID);
			preparedStatement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void updateListApps(int userID, String list) {
		String sql = "UPDATE process_monitoring_database.devices SET apps=? WHERE user_id=?";

		try {
			preparedStatement = mConnection.prepareStatement(sql);
			preparedStatement.setString(1, list);
			preparedStatement.setInt(2, userID);

			preparedStatement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static String getToken(int userID) {
		String token = null;
		try {
			String sql = "SELECT * FROM process_monitoring_database.devices WHERE user_id=?";
			preparedStatement = mConnection.prepareStatement(sql);
			preparedStatement.setInt(1, userID);

			ResultSet rs = preparedStatement.executeQuery();
			rs.next();

			token = rs.getString("token");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return token;
	}

	public static void deleteDevice(int userID) {
		try {
			String sql = "DELETE FROM process_monitoring_database.devices WHERE user_id=?";
			preparedStatement = mConnection.prepareStatement(sql);
			preparedStatement.setInt(1, userID);

			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static boolean checkAuthentication(String mLogin, String mPassword) {
		boolean status = false;
		try {
			String sql = "SELECT * FROM process_monitoring_database.account WHERE login=?";
			preparedStatement = mConnection.prepareStatement(sql);
			preparedStatement.setString(1, mLogin);

			ResultSet rs = preparedStatement.executeQuery();
			System.out.println("DatabaseHandler----------- ::: " + mLogin);
			while (rs.next()) {
				String password = rs.getString("password");
				System.out.println("password ::: " + password);
				if (password.equals(mPassword)) {
					return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return status;
	}

	/**
	 * returns false if login is already taken 
	 * true - add new account
	 */
	public static boolean checkRegistration(String mLogin) {
		try {
			String sql = "SELECT * FROM process_monitoring_database.account WHERE login=?";
			preparedStatement = mConnection.prepareStatement(sql);
			preparedStatement.setString(1, mLogin);
			ResultSet rs = preparedStatement.executeQuery();
			if (rs.next()) {
				String login = rs.getString(2);
				if (mLogin.equals(login)) {
					return false;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
}
