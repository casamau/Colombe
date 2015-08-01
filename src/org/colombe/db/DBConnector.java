package org.colombe.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnector {

	private Connection dbConnector = null;
	private String bibleName = "";

	public DBConnector(String biblename)
	{
		this.bibleName = biblename;
	}

	private void checkConnection()
	{
		if (dbConnector == null) {

			try {
				Class.forName("org.sqlite.JDBC");
				dbConnector = DriverManager.getConnection("jdbc:sqlite:" + bibleName);
			} catch ( Exception e ) {
				System.err.println( e.getClass().getName() + ": " + e.getMessage() );
				System.exit(0);
			}
		}
	}


	public Statement getStatement() throws SQLException
	{
		checkConnection();
		return dbConnector.createStatement();
	}
}
