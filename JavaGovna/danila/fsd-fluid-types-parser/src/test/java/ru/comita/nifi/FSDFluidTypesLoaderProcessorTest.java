package ru.comita.nifi;

import lombok.SneakyThrows;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.Relationship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FSDFluidTypesLoaderProcessorTest extends FSDLoaderProcessorTest {
    private final FSDFluidTypesLoaderProcessor processor = new FSDFluidTypesLoaderProcessor();
    private final FsdFluidTypesConnectionManager connectionManager = new FsdFluidTypesConnectionManager();

    @BeforeEach
    @SneakyThrows
    public void setUp() {
        super.setUp();
    }

    @Test
    @SneakyThrows
    public void testSuccess() {
        super.testSuccess("ФСД_НСИ_Вид флюида.xlsx");
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
        return FSDFluidTypesLoaderProcessor.DBCP_SERVICE;
    }

    @Override
    protected Relationship successRelation() {
        return FSDFluidTypesLoaderProcessor.SUCCESS;
    }
}
