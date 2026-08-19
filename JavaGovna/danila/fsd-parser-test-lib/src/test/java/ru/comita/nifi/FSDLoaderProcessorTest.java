package ru.comita.nifi;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.dbcp.DBCPService;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public abstract class FSDLoaderProcessorTest {
    private TestRunner runner;
    private DBCPService targetService;

    public void setUp() throws InitializationException {
        targetService = new DBCPServiceSQLServerImpl("target", connectionManager());
        runner = TestRunners.newTestRunner(processor());
        runner.addControllerService("target", targetService);
        runner.enableControllerService(targetService);
        runner.setProperty(dbcpProperty(), "target");
    }

    public void testSuccess(String fileName) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(fileName)) {
            assertNotNull(in);
            connectionManager().initTables(targetService.getConnection());

            runner.enqueue(in);
            runner.run(1);
            runner.assertQueueEmpty();

            List<MockFlowFile> flowfiles = runner.getFlowFilesForRelationship(successRelation());
            assertEquals(1, flowfiles.size());

            MockFlowFile flowfile = flowfiles.get(0);
            assertNotNull(flowfile);

            runner.clearTransferState();
            connectionManager().clearAllTables(targetService.getConnection());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class DBCPServiceSQLServerImpl extends AbstractControllerService
            implements DBCPService {
        private final String identifier;
        private final ConnectionManager connectionManager;

        public DBCPServiceSQLServerImpl(String identifier, ConnectionManager connectionManager) {
            this.identifier = identifier;
            this.connectionManager = connectionManager;
        }

        @Override
        public String getIdentifier() {
            return identifier;
        }

        @Override
        public Connection getConnection() throws ProcessException {
            try {
                return connectionManager.getConnection();
            } catch (final Exception e) {
                throw new ProcessException("getConnection failed: " + e);
            }
        }
    }

    protected abstract AbstractProcessor processor();
    protected abstract ConnectionManager connectionManager();
    protected abstract PropertyDescriptor dbcpProperty();
    protected abstract Relationship successRelation();
}
