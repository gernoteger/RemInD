package at.jit.remind.core.model.content.database;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import at.jit.remind.core.connector.database.DbAccess;
import at.jit.remind.core.connector.database.DbAccess.JdbcConnectionFailedException;
import at.jit.remind.core.connector.database.DbAccess.JdbcDriverNotFoundException;
import at.jit.remind.core.connector.database.DbAccess.SqlStatementException;
import at.jit.remind.core.context.PropertiesFromResourceProvider;
import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.ListBasedMessageHandler;
import at.jit.remind.core.context.messaging.Message;
import at.jit.remind.core.context.messaging.MessageHandler.MessageLevel;
import at.jit.remind.core.exception.MessageHandlerException;
import at.jit.remind.core.model.RemindModelFeedback;

public class SqlStatementListTest
{
	private static final String jdbcDirver = "org.hsqldb.jdbcDriver";
	private static final String jdbcUrl = "jdbc:hsqldb:mem:dbstatementlisttest";
	private static final String databaseUser = "sa";
	private static final String databasePassword = "";

	private static ListBasedMessageHandler listBasedMessageHandler = new ListBasedMessageHandler();
	private static DbAccess dbAccess = new DbAccess(jdbcDirver, jdbcUrl, databaseUser, databasePassword);

	private static File statementListFile = new File("src/test/resources/database/SqlStatementListTest.sql");

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		RemindContext.getInstance().setPropertiesProvider(new PropertiesFromResourceProvider());
		RemindContext.getInstance().setMessageHandler(listBasedMessageHandler);

		dbAccess.executeSqlStatement("CREATE TABLE testtable (id int GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1) NOT NULL, input varchar(254) NOT NULL);");
	}

	@Before
	public void setUp() throws Exception
	{
		listBasedMessageHandler.getMessageList().clear();
	}

	@After
	public void tearDown() throws JdbcDriverNotFoundException, JdbcConnectionFailedException, SqlStatementException
	{
		dbAccess.executeSqlStatement("DELETE FROM testtable;");
	}

	@Test
	public void canSkipStatement() throws IOException, MessageHandlerException, ClassNotFoundException, SQLException
	{
		listBasedMessageHandler.setFeedback(DatabaseFeedback.SkipStatement);

		SqlStatementList sqlStatementList = new SqlStatementList(statementListFile);
		sqlStatementList.convertSqlFileToStatements();
		sqlStatementList.deploy(dbAccess, new TreeSet<String>());

		boolean isWarning = false;
		for (Message m : listBasedMessageHandler.getMessageList())
		{
			if (MessageLevel.WARNING.equals(m.getLevel()))
			{
				isWarning = true;
			}
		}

		assertSame("Expected row count is 3", 3, getRowCount());
		assertTrue("Status is Warning", isWarning);
	}

	@Test
	public void canAbortDeployment() throws IOException, ClassNotFoundException, SQLException, MessageHandlerException
	{
		listBasedMessageHandler.setFeedback(RemindModelFeedback.Abort);

		try
		{
			SqlStatementList sqlStatementList = new SqlStatementList(statementListFile);
			sqlStatementList.convertSqlFileToStatements();
			sqlStatementList.deploy(dbAccess, new TreeSet<String>());
		}
		catch (MessageHandlerException e)
		{
			// intentionally left blank
		}

		boolean isErrorAndHasNoWarning = false;
		for (Message m : listBasedMessageHandler.getMessageList())
		{
			if (MessageLevel.ERROR.equals(m.getLevel()))
			{
				isErrorAndHasNoWarning = true;
			}
			if (MessageLevel.WARNING.equals(m.getLevel())) // must not contain a WARNING.
			{
				isErrorAndHasNoWarning = false;
				break;
			}
		}

		assertSame("Expected row count is 1", 1, getRowCount());
		assertTrue("Status is Error without warning", isErrorAndHasNoWarning);
	}

	private int getRowCount() throws ClassNotFoundException, SQLException
	{
		Class.forName(jdbcDirver);
		Connection con = DriverManager.getConnection(jdbcUrl, databaseUser, databasePassword);
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT COUNT(1) FROM testtable");
		rs.next();
		int count = rs.getInt(1);
		stmt.close();
		con.close();

		return count;
	}
}
