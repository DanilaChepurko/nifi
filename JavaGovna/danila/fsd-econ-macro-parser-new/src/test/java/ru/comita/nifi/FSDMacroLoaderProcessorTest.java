package ru.comita.nifi;

import lombok.SneakyThrows;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.Relationship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class FSDMacroLoaderProcessorTest extends FSDLoaderProcessorTest {
    private final FSDMacroLoaderProcessorEconom processor = new FSDMacroLoaderProcessorEconom();
    private final FsdMacroConnectionManager connectionManager = new FsdMacroConnectionManager();

    @BeforeEach
    @SneakyThrows
    public void setUp() {
        super.setUp();
    }

    @Test
    @SneakyThrows
    public void testSuccess() {
        super.testSuccess("ФСД_Макроокруж (для всех) - 1.xlsx");
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
        return FSDMacroLoaderProcessorEconom.DBCP_SERVICE;
    }

    @Override
    protected Relationship successRelation() {
        return FSDMacroLoaderProcessorEconom.SUCCESS;
    }
}
