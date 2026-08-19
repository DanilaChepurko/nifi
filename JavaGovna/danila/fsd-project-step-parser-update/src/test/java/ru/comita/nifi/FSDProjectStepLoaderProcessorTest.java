package ru.comita.nifi;

import lombok.SneakyThrows;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.Relationship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class FSDProjectStepLoaderProcessorTest extends FSDLoaderProcessorTest {
    private final FSDProjectStepLoaderProcessorU processor = new FSDProjectStepLoaderProcessorU();
    private final FsdProjectStepConnectionManager connectionManager = new FsdProjectStepConnectionManager();

    @BeforeEach
    @SneakyThrows
    public void setUp() {
        super.setUp();
    }

    @Test
    @SneakyThrows
    public void testSuccess() {
        super.testSuccess("ФСД Мероприятия Надым синтетика.xlsx");
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
        return FSDProjectStepLoaderProcessorU.DBCP_SERVICE;
    }

    @Override
    protected Relationship successRelation() {
        return FSDProjectStepLoaderProcessorU.SUCCESS;
    }
}
