package ru.comita.nifi;

import lombok.SneakyThrows;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.Relationship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class FSDFieldLoaderProcessorTest extends FSDLoaderProcessorTest {
    private final ru.comita.nifi.FSDFieldLoaderProcessorU processor = new ru.comita.nifi.FSDFieldLoaderProcessorU();
    private final FsdFieldConnectionManager connectionManager = new FsdFieldConnectionManager();

    @BeforeEach
    @SneakyThrows
    public void setUp() {
        super.setUp();
    }

    @Test
    @SneakyThrows
    public void testSuccess() {
        super.testSuccess("ФСД НСИ Месторождения.xlsx");
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
        return FSDFieldLoaderProcessorU.DBCP_SERVICE;
    }

    @Override
    protected Relationship successRelation() {
        return FSDFieldLoaderProcessorU.SUCCESS;
    }
}
