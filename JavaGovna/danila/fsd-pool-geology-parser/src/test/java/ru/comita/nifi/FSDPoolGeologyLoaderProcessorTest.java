package ru.comita.nifi;

import lombok.SneakyThrows;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.Relationship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FSDPoolGeologyLoaderProcessorTest extends FSDLoaderProcessorTest {
    private final FSDPoolGeologyLoaderProcessor processor = new FSDPoolGeologyLoaderProcessor();
    private final FsdPoolGeologyConnectionManager connectionManager = new FsdPoolGeologyConnectionManager();

    @BeforeEach
    @SneakyThrows
    public void setUp() {
        super.setUp();
    }

    @Test
    @SneakyThrows
    public void testSuccess() {
        super.testSuccess("ФСД_НСИ_Геология месторождения.xlsx");
    }

    @Override
    protected AbstractProcessor processor() {
        return processor;
    }

    @Override
    protected ConnectionManager connectionManager() {
        return connectionManager;
    }

    @Override
    protected PropertyDescriptor dbcpProperty() {
        return FSDPoolGeologyLoaderProcessor.DBCP_SERVICE;
    }

    @Override
    protected Relationship successRelation() {
        return FSDPoolGeologyLoaderProcessor.SUCCESS;
    }
}
