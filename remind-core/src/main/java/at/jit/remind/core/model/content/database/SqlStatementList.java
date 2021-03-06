package at.jit.remind.core.model.content.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.input.BOMInputStream;

import at.jit.remind.core.connector.database.DbAccess;
import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.MessageHandler.MessageLevel;
import at.jit.remind.core.exception.MessageHandlerException;
import at.jit.remind.core.util.CharsetDetector;

public class SqlStatementList
{
	private File file;

	private List<AtomicSqlStatement> statementList = new ArrayList<AtomicSqlStatement>();
	private SqlParser sqlParser;
	private CharsetDetector charsetDetector = new CharsetDetector();

	public SqlStatementList(File file) throws MessageHandlerException
	{
		if (file == null)
		{
			throw new MessageHandlerException("No sql file assigned!");
		}

		this.file = file;
		sqlParser = new SqlParser();
	}

	public void convertSqlFileToStatements() throws MessageHandlerException
	{
		sqlParser.parse(this);
	}

	public void deploy(DbAccess dbAccess, Set<String> deploymentDetails) throws MessageHandlerException
	{
		for (AtomicSqlStatement s : statementList)
		{
			try
			{
				s.deploy(dbAccess);
				if(s.gotCorrected())
				{
					deploymentDetails.add("SQL statement fixed");
				}
			}
			catch (MessageHandlerException e)
			{
				if (DatabaseFeedback.SkipStatement.equals(e.getFeedback()))
				{
					RemindContext.getInstance().getMessageHandler().addMessage(MessageLevel.WARNING, "SQL statement skipped!", e.getMessage());
					deploymentDetails.add("Sql statement skipped");

					continue;
				}
				else
				{
					throw e;
				}
			}
		}
	}

	public void addAtomicStatement(AtomicSqlStatement atomicStatement)
	{
		statementList.add(atomicStatement);
	}

	public int size()
	{
		return statementList.size();
	}

	public BufferedReader getFileReader() throws IOException
	{
		String charset = charsetDetector.detectCharset(file);

		return new BufferedReader(new InputStreamReader(new BOMInputStream(new FileInputStream(file)), charset));
	}

	protected File getFile()
	{
		return file;
	}
}
