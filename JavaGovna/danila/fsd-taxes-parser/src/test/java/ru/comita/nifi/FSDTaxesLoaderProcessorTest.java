package ru.comita.nifi;

import lombok.SneakyThrows;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.Relationship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FSDTaxesLoaderProcessorTest extends FSDLoaderProcessorTest {
    private final FSDTaxesLoaderProcessor processor = new FSDTaxesLoaderProcessor();
    private final FsdTaxesConnectionManager connectionManager = new FsdTaxesConnectionManager();

    @BeforeEach
    @SneakyThrows
    public void setUp() {
        super.setUp();
    }

    @Test
    @SneakyThrows
    public void testSuccess() {
        super.testSuccess("ФСД_Налоги (для всех).xlsx");
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
        return FSDTaxesLoaderProcessor.DBCP_SERVICE;
    }

    @Override
    protected Relationship successRelation() {
        return FSDTaxesLoaderProcessor.SUCCESS;
    }
}
