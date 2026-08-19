package ru.comita.nifi;

import lombok.SneakyThrows;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.Relationship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class FSDProjectEquipmentLoaderProcessorTest extends FSDLoaderProcessorTest {
    private final FSDProjectEquipmentLoaderProcessor processor = new FSDProjectEquipmentLoaderProcessor();
    private final FsdProjectEquipmentConnectionManager connectionManager = new FsdProjectEquipmentConnectionManager();

    @BeforeEach
    @SneakyThrows
    public void setUp() {
        super.setUp();
    }

    @Test
    @SneakyThrows
    public void testSuccess() {
        super.testSuccess("ФСД_НСИ_Оборудование.xlsx");
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
        return FSDProjectEquipmentLoaderProcessor.DBCP_SERVICE;
    }

    @Override
    protected Relationship successRelation() {
        return FSDProjectEquipmentLoaderProcessor.SUCCESS;
    }
}
