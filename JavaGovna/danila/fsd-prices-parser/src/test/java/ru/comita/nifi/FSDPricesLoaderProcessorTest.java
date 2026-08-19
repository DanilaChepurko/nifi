package ru.comita.nifi;

import lombok.SneakyThrows;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.Relationship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FSDPricesLoaderProcessorTest extends FSDLoaderProcessorTest {
    private final FSDPricesLoaderProcessor processor = new FSDPricesLoaderProcessor();
    private final FsdPricesConnectionManager connectionManager = new FsdPricesConnectionManager();

    @BeforeEach
    @SneakyThrows
    public void setUp() {
        super.setUp();
    }

    @Test
    @SneakyThrows
    public void testSuccess() {
        super.testSuccess("ФСД_Цены (по ГДО) - 1.xlsx");
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
        return FSDPricesLoaderProcessor.DBCP_SERVICE;
    }

    @Override
    protected Relationship successRelation() {
        return FSDPricesLoaderProcessor.SUCCESS;
    }
}
