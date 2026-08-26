package ru.comita.nifi;

import lombok.SneakyThrows;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.Relationship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FSDProjectStepEconLoaderProcessorTest extends FSDLoaderProcessorTest {
    private final FSDProjectEconLoaderProcessor processor = new FSDProjectEconLoaderProcessor();
    private final FsdProjectStepEconConnectionManager connectionManager = new FsdProjectStepEconConnectionManager();

    @BeforeEach
    @SneakyThrows
    public void setUp() {
        super.setUp();
    }

    @Test
    @SneakyThrows
    public void testSuccess() {
        super.testSuccess("ФСД_ИП+стройка.xlsx");
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
        return FSDProjectEconLoaderProcessor.DBCP_SERVICE;
    }

    @Override
    protected Relationship successRelation() {
        return FSDProjectEconLoaderProcessor.SUCCESS;
    }
}
