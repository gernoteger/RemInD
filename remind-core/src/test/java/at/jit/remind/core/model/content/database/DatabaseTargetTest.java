package at.jit.remind.core.model.content.database;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import at.jit.remind.core.context.PropertiesFromResourceProvider;
import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.ListBasedMessageHandler;
import at.jit.remind.core.exception.MessageHandlerException;
import at.jit.remind.core.model.RemindModelFeedback;
import at.jit.remind.core.model.content.io.FileSystemLocation;
import at.jit.remind.core.xml.FileSystem;
import at.jit.remind.core.xml.Source;

public class DatabaseTargetTest
{
    private static final String environment = "DEV";
    private static final String sid = "REMINDTESTING";
    private static final String schema = "remindtest";

    private static ListBasedMessageHandler listBasedMessageHandler = new ListBasedMessageHandler();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RemindContext.getInstance().setPropertiesProvider(new PropertiesFromResourceProvider());
        RemindContext.getInstance().setMessageHandler(listBasedMessageHandler);

        listBasedMessageHandler.setFeedback(RemindModelFeedback.Abort);
    }

    @Test
    public void canConvertCorrectSourceFileToStatementList() throws MessageHandlerException, IOException
    {
        DatabaseTarget databaseTarget = new DatabaseTarget(environment, sid, schema);
        String sourcePath = System.getProperty("java.io.tmpdir") + "/" + "DatabaseTargetTestSourceFile.sql";
        String sqlFileAsString = FileUtils.readFileToString(FileUtils.toFile(DatabaseTargetTest.class.getResource("/database/DatabaseTargetTestSourceFile.sql")));

        generateSourceFile(sourcePath, sqlFileAsString);
        FileSystemLocation fileSystemLocation = generateFileSystemLocation(sourcePath);

        SqlStatementList list = databaseTarget.convert(fileSystemLocation);

        assertSame("List size must be 5", 5, list.size());
    }

    @Test
    public void canConvertIncorrectSourceFileToStatementList() throws MessageHandlerException, IOException
    {
        DatabaseTarget databaseTarget = new DatabaseTarget(environment, sid, schema);
        String sourcePath = System.getProperty("java.io.tmpdir") + "/" + "DatabaseTargetTestSourceFile.sql";
        String sqlFileAsString = FileUtils.readFileToString(FileUtils.toFile(DatabaseTargetTest.class.getResource("/database/DatabaseTargetTestSourceFile2.sql")));

        generateSourceFile(sourcePath, sqlFileAsString);
        FileSystemLocation fileSystemLocation = generateFileSystemLocation(sourcePath);

        SqlStatementList list = databaseTarget.convert(fileSystemLocation);

        assertSame("List size must be 5", 5, list.size());
    }

    @Test
    public void canDeploy() throws IOException
    {
        DatabaseTarget databaseTarget = new DatabaseTarget(environment, sid, schema);
        String sourcePath = System.getProperty("java.io.tmpdir") + "/" + "DatabaseTargetTestSourceFile.sql";
        String sqlFileAsString = FileUtils.readFileToString(FileUtils.toFile(DatabaseTargetTest.class.getResource("/database/DatabaseTargetTestSourceFile.sql")));

        generateSourceFile(sourcePath, sqlFileAsString);
        FileSystemLocation fileSystemLocation = generateFileSystemLocation(sourcePath);

        try
        {
            SqlStatementList list = databaseTarget.convert(fileSystemLocation);
            databaseTarget.deploy(list);
            // NOSONAR
            // No fail means test success
        }
        catch (MessageHandlerException e)
        {
            fail(e.getMessage());
        }
    }

    @Test
    public void canCorrectErroneousSQL() throws IOException
    {
        DatabaseTarget databaseTarget = new DatabaseTarget(environment, sid, schema);
        FixStatementFeedback feedback = new FixStatementFeedback("insert into erroneusTestTable values(1, 'asdf')");
        listBasedMessageHandler.setFeedback(feedback);
        String sourcePath = System.getProperty("java.io.tmpdir") + "/" + "ErroneousSqlTest.sql";

        String sqlFileAsString = FileUtils.readFileToString(FileUtils.toFile(DatabaseTargetTest.class.getResource("/database/ErroneousSqlTest.sql")));
        generateSourceFile(sourcePath, sqlFileAsString);
        FileSystemLocation fileSystemLocation = generateFileSystemLocation(sourcePath);

        try
        {
            SqlStatementList list = databaseTarget.convert(fileSystemLocation);
            databaseTarget.deploy(list);

            // NOSONAR
            // No fail means test success
        }
        catch (MessageHandlerException e)
        {
            fail(e.getMessage());
        }
    }

    @Test
    public void canValidate() throws MessageHandlerException
    {
        try
        {
            DatabaseTarget databaseTarget = new DatabaseTarget(environment, sid, schema);
            databaseTarget.validate();

            // NOSONAR
            // No fail means test success
        }
        catch (MessageHandlerException e)
        {
            fail(e.getMessage());
        }
    }

    @Ignore
    @Test
    public void canValidateOracleDb() throws MessageHandlerException
    {
        try
        {
            DatabaseTarget target = new DatabaseTarget("QM", "ORACLE-QM", "");
            target.validate();

            // NOSONAR
            // No fail means test success
        }
        catch (MessageHandlerException e)
        {
            fail(e.getMessage());
        }
    }

    @Ignore
    @Test
    public void canDeployWithSingleCommentAtFirstRow() throws IOException
    {
        DatabaseTarget databaseTarget = new DatabaseTarget("QM", "ORACLE-QM", "");
        String sourcePath = System.getProperty("java.io.tmpdir") + "/" + "SQLParserTestSingleCommentFirstLine.sql";
        String sqlFileAsString = FileUtils.readFileToString(FileUtils.toFile(DatabaseTargetTest.class.getResource("/database/SQLParserTestSingleCommentFirstLine.sql")));

        generateSourceFile(sourcePath, sqlFileAsString);
        FileSystemLocation fileSystemLocation = generateFileSystemLocation(sourcePath);

        try
        {
            SqlStatementList list = databaseTarget.convert(fileSystemLocation);
            databaseTarget.deploy(list);

            // NOSONAR
            // No fail means test success
        }
        catch (MessageHandlerException e)
        {
            fail(e.getMessage());
        }
    }

    @Ignore
    @Test
    public void canDeployJavaCode() throws IOException
    {
        DatabaseTarget databaseTarget = new DatabaseTarget("QM", "ORACLE-QM", "");
        String sourcePath = System.getProperty("java.io.tmpdir") + "/" + "DatabaseTargetTestJavaSource.sql";
        String sqlFileAsString = FileUtils.readFileToString(FileUtils.toFile(DatabaseTargetTest.class.getResource("/database/DatabaseTargetTestJavaSource.sql")));

        generateSourceFile(sourcePath, sqlFileAsString);
        FileSystemLocation fileSystemLocation = generateFileSystemLocation(sourcePath);

        try
        {
            SqlStatementList list = databaseTarget.convert(fileSystemLocation);
            databaseTarget.deploy(list);

            // NOSONAR
            // No fail means test success
        }
        catch (MessageHandlerException e)
        {
            fail(e.getMessage());
        }
    }

    @Ignore
    @Test
    public void canDeployUmlauteWithDIfferentEncodings() throws IOException
    {
        DatabaseTarget databaseTarget = new DatabaseTarget("QM", "ORACLE-QM", "");
        String sourcePath = System.getProperty("java.io.tmpdir") + "/" + "DatabaseTargetTestISO8859.sql";
        String sqlFileAsString = FileUtils.readFileToString(FileUtils.toFile(DatabaseTargetTest.class.getResource("/database/DatabaseTargetTestISO8859.sql")));

        generateSourceFile(sourcePath, sqlFileAsString);
        FileSystemLocation fileSystemLocation = generateFileSystemLocation(sourcePath);

        try
        {
            SqlStatementList list = databaseTarget.convert(fileSystemLocation);
            databaseTarget.deploy(list);

            // NOSONAR
            // No fail means test success
        }
        catch (MessageHandlerException e)
        {
            fail(e.getMessage());
        }
    }

    @Ignore
    @Test
    public void canDeployOracleDb() throws IOException
    {
        DatabaseTarget databaseTarget = new DatabaseTarget("QM", "ORACLE-QM", "");
        String sourcePath = System.getProperty("java.io.tmpdir") + "/" + "DatabaseTargetOracleTestSourceFile.sql";
        String sqlFileAsString = FileUtils.readFileToString(FileUtils.toFile(DatabaseTargetTest.class.getResource("/database/DatabaseTargetOracleTestSourceFile.sql")));

        generateSourceFile(sourcePath, sqlFileAsString);
        FileSystemLocation fileSystemLocation = generateFileSystemLocation(sourcePath);

        try
        {
            SqlStatementList list = databaseTarget.convert(fileSystemLocation);
            databaseTarget.deploy(list);

            // NOSONAR
            // No fail means test success
        }
        catch (MessageHandlerException e)
        {
            fail(e.getMessage());
        }
    }

    @Ignore
    @Test
    public void canDeploySlashesToOracleDb() throws IOException
    {
        listBasedMessageHandler.setFeedback(RemindModelFeedback.Abort);

        DatabaseTarget databaseTarget = new DatabaseTarget("QM", "ORACLE-QM", "");
        String sourcePath = System.getProperty("java.io.tmpdir") + "/" + "DatabaseTargetOracleTestSourceFile.sql";
        String sqlFileAsString = FileUtils.readFileToString(FileUtils.toFile(DatabaseTargetTest.class.getResource("/database/SQLParserTestSlash2.sql")));

        generateSourceFile(sourcePath, sqlFileAsString);
        FileSystemLocation fileSystemLocation = generateFileSystemLocation(sourcePath);

        try
        {
            SqlStatementList list = databaseTarget.convert(fileSystemLocation);
            databaseTarget.deploy(list);

            // NOSONAR
            // No fail means test success
        }
        catch (MessageHandlerException e)
        {
            fail(e.getMessage());
        }
    }

    @Ignore
    @Test
    public void canDeployCommentsWithHyphensAndBackslashesToOracleDb() throws IOException
    {
        DatabaseTarget databaseTarget = new DatabaseTarget("QM", "ORACLE-QM", "");
        String sourcePath = System.getProperty("java.io.tmpdir") + "/" + "OracleBug1559Source.sql";
        String sqlFileAsString = FileUtils.readFileToString(FileUtils.toFile(DatabaseTargetTest.class.getResource("/database/DatabaseTargetOracleBug1559.sql")));

        generateSourceFile(sourcePath, sqlFileAsString);
        FileSystemLocation fileSystemLocation = generateFileSystemLocation(sourcePath);

        try
        {
            SqlStatementList list = databaseTarget.convert(fileSystemLocation);
            databaseTarget.deploy(list);

            // NOSONAR
            // No fail means test success
        }
        catch (MessageHandlerException e)
        {
            fail(e.getMessage());
        }
    }

    @Ignore
    @Test
    public void canHandleMultipleSemicolonsOnNextLine() throws IOException
    {
        DatabaseTarget databaseTarget = new DatabaseTarget("QM", "ORACLE-QM", "");
        String sourcePath = System.getProperty("java.io.tmpdir") + "/" + "DatabaseTargetTestMultipleSemicolonsInNextLine.sql";
        String sqlFileAsString = FileUtils.readFileToString(FileUtils.toFile(DatabaseTargetTest.class.getResource("/database/DatabaseTargetTestMultipleSemicolonsInNextLine.sql")));

        generateSourceFile(sourcePath, sqlFileAsString);
        FileSystemLocation fileSystemLocation = generateFileSystemLocation(sourcePath);

        try
        {
            SqlStatementList list = databaseTarget.convert(fileSystemLocation);
            databaseTarget.deploy(list);

            // NOSONAR
            // No fail means test success
        }
        catch (MessageHandlerException e)
        {
            fail(e.getMessage());
        }
    }

    @Ignore
    @Test
    public void invalidCharacterTest() throws IOException
    {
        DatabaseTarget databaseTarget = new DatabaseTarget("QM", "ORACLE-QM", "");
        String sourcePath = System.getProperty("java.io.tmpdir") + "/" + "RemInD_create_table4_utf8_mit_bom.sql";
        String sqlFileAsString = FileUtils.readFileToString(FileUtils.toFile(DatabaseTargetTest.class.getResource("/database/RemInD_create_table4_utf8_mit_bom.sql")));

        generateSourceFile(sourcePath, sqlFileAsString);
        FileSystemLocation fileSystemLocation = generateFileSystemLocation(sourcePath);

        try
        {
            SqlStatementList list = databaseTarget.convert(fileSystemLocation);
            databaseTarget.deploy(list);

            // NOSONAR
            // No fail means test success
        }
        catch (MessageHandlerException e)
        {
            fail(e.getMessage());
        }
    }
    
    @Ignore
    @Test
    public void commentWithinStatementTest() throws IOException
    {
        DatabaseTarget databaseTarget = new DatabaseTarget("QM", "ORACLE-QM", "");
        String sourcePath = System.getProperty("java.io.tmpdir") + "/" + "SQLParserTestMultilineCommentWithinStatement.sql";
        String sqlFileAsString = FileUtils.readFileToString(FileUtils.toFile(DatabaseTargetTest.class.getResource("/database/SQLParserTestMultilineCommentWithinStatement.sql")));

        generateSourceFile(sourcePath, sqlFileAsString);
        FileSystemLocation fileSystemLocation = generateFileSystemLocation(sourcePath);

        try
        {
            SqlStatementList list = databaseTarget.convert(fileSystemLocation);
            databaseTarget.deploy(list);

            // NOSONAR
            // No fail means test success
        }
        catch (MessageHandlerException e)
        {
            fail(e.getMessage());
        }      
    }

    @Test(expected = MessageHandlerException.class)
    public void canHandleValidationError() throws MessageHandlerException
    {
        DatabaseTarget wrongDatabaseTarget = new DatabaseTarget(environment, sid, "foo");
        wrongDatabaseTarget.validate();
    }
    
    //This test is here to execute any sql file you need.
    @Ignore
    @Test
    public void exexcuteCurrentSqlScript() throws IOException
    {
        DatabaseTarget databaseTarget = new DatabaseTarget("QM", "ORACLE-QM", "");
        String sourcePath = System.getProperty("java.io.tmpdir") + "/" + "SQLParserTestEndTagBehaviour.sql";
        String sqlFileAsString = FileUtils.readFileToString(FileUtils.toFile(DatabaseTargetTest.class.getResource("/database/SQLParserTestEndTagBehaviour.sql")));

        generateSourceFile(sourcePath, sqlFileAsString);
        FileSystemLocation fileSystemLocation = generateFileSystemLocation(sourcePath);

        try
        {
            SqlStatementList list = databaseTarget.convert(fileSystemLocation);
            databaseTarget.deploy(list);

            // NOSONAR
            // No fail means test success
        }
        catch (MessageHandlerException e)
        {
            fail(e.getMessage());
        }              
    }

    private void generateSourceFile(String sourcePath, String content) throws IOException
    {
        File file = new File(sourcePath);
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write(content);
        out.close();
        file.deleteOnExit();
    }

    private FileSystemLocation generateFileSystemLocation(String sourcePath)
    {
        FileSystem fileSystem = new FileSystem();
        fileSystem.setPath(sourcePath);

        Source source = new Source();
        source.setFileSystem(fileSystem);

        return new FileSystemLocation(sourcePath, true);
    }
}
