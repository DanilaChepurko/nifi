package ru.comita.nifi;

import lombok.SneakyThrows;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.Relationship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class FSDOperatingCostLoaderProcessorTest extends FSDLoaderProcessorTest {
    private final FSDOperatingCostLoaderProcessor processor = new FSDOperatingCostLoaderProcessor();
    private final ConnectionManager connectionManager = new FsdOperatingCostConnectionManager();

    @BeforeEach
    @SneakyThrows
    public void setUp() {
        super.setUp();
    }

    @Test
    @SneakyThrows
    public void testSuccess() {
        super.testSuccess("ФСД_ЭЗ.xlsx");
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
        return FSDOperatingCostLoaderProcessor.DBCP_SERVICE;
    }

    @Override
    protected Relationship successRelation() {
        return FSDOperatingCostLoaderProcessor.SUCCESS;
    }

}
