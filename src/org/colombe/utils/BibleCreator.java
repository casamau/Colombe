package org.colombe.utils;

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map.Entry;

import org.colombe.books.BookStructure;
import org.colombe.books.Books;
import org.colombe.db.DBConnector;

public class BibleCreator
{
	private String sourcePath;
	private DBConnector db;
	private Statement stmt;
	private Books books;

	public BibleCreator(String sourcePath, String bibleName)
	{
		this.sourcePath = sourcePath + File.separatorChar + "OPS";
		db = new DBConnector(bibleName);
	}

	public void createBible() throws SQLException
	{
		createBooks();

		stmt = db.getStatement();
		createTables();
		addDetail();
		addScripture();

		stmt.close();
	}

	private void addScripture()
	{
		LinkedHashMap<String, LinkedHashSet<BookStructure>> bible = books.getBooks();

		insertSQL("BEGIN TRANSACTION;");

		for (Entry<String, LinkedHashSet<BookStructure>> b: bible.entrySet()) {

			LinkedHashSet<BookStructure> data = b.getValue();
			for (BookStructure bs: data)
				addText(bs);
		}

		insertSQL("COMMIT;");
	}

	private void addText(BookStructure bs)
	{
		String scripture = bs.getVerse().replaceAll("\"","\\\"");
		scripture = scripture.replaceAll("\'","\\\'");

		String sql = "insert into bible ("
				+ "'Book', "
				+ "'Chapter', "
				+ "'Verse', "
				+ "'Scripture'"
				+ ") values ("
				+ "'" + bs.getNumBook()    + "', "
				+ "'" + bs.getNumChapter() + "', "
				+ "'" + bs.getNumVerse()   + "', "
				+ "'" + scripture + "'"
				+ ");";

		insertSQL(sql);
	}

	private void addDetail()
	{
		Date today = new Date();
		DateFormat shortDateFormatFR = DateFormat.getDateTimeInstance(
				DateFormat.MEDIUM,
				DateFormat.MEDIUM, new Locale("FR","fr"));

		String date = shortDateFormatFR.format(today);
		String year = date.split(" ")[2];

		String sql = "insert into details ("
				+ "'Description', "
				+ "'Abbreviation', "
				+ "'Comments', "
				+ "'Version', "
				+ "'VersionDate', "
				+ "'PublishDate', "
				+ "'RightToLeft', "
				+ "'OT', "
				+ "'NT', "
				+ "'Strong', "
				+ "'ParagraphIndent', "
				+ "'CustomCSS', "
				+ "'VerseRules'"
				+ ") values ("
				+ "'Bible dite \"à la Colombe\"', "
				+ "'Colombe', "
				+ "'Nouvelle Version Segond révisée 1978 (Colombe)', "
				+ "'2.0', "
				+ "'" + date  + "', "
				+ "'" + year + "', "
				+ "'0', "
				+ "'1', "
				+ "'1', "
				+ "'0', "
				+ "'0', "
				+ "'.filet {font-size:1.8em;} ul {margin-top:0; margin-bottom:0;}', "
				+ "''" // \\^(\\w)	<span class=\'filet\'>$1</span>
				+ ");";

		insertSQL(sql);
	}

	private void createBooks()
	{
		books = new Books(sourcePath);
		books.createBooks();
	}

	private void insertSQL(String sql)
	{
		try {
			stmt.executeUpdate(sql);
		} catch (Exception e) {
			System.out.println(sql);
			e.printStackTrace();
		}
	}

	private void createTables()
	{
		try {
			Statement stmt = db.getStatement();

			// Creation de la table qui contiendra les textes
			String sql = "CREATE TABLE Bible("
					+ "Book INT, "
					+ "Chapter INT, "
					+ "Verse INT, "
					+ "Scripture TEXT, "
					+ "Primary Key(Book,Chapter,Verse))";

			stmt.executeUpdate(sql);

			// Creation de la table qui contiendra le reste
			sql = "CREATE TABLE Details (Description NVARCHAR(255), "
					+ "Abbreviation NVARCHAR(50), "
					+ "Comments TEXT, "
					+ "Version TEXT, "
					+ "VersionDate DATETIME, "
					+ "PublishDate DATETIME, "
					+ "RightToLeft BOOL, "
					+ "OT BOOL, "
					+ "NT BOOL, "
					+ "Strong BOOL, "
					+ "ParagraphIndent INT, "
					+ "CustomCSS TEXT, "
					+ "VerseRules TEXT)";

			stmt.executeUpdate(sql);
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
