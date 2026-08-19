package ru.comita.nifi;

import lombok.SneakyThrows;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.Relationship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class FSDFacilityLoaderProcessorTest extends FSDLoaderProcessorTest {
    private final FSDFacilityLoaderProcessorU processor = new FSDFacilityLoaderProcessorU();
    private final FsdFacilitiesConnectionManager connectionManager = new FsdFacilitiesConnectionManager();

    @BeforeEach
    @SneakyThrows
    public void setUp() {
        super.setUp();
    }

    @Test
    @SneakyThrows
    public void testSuccess() {
        super.testSuccess("ФСД НСИ Площадки.xlsx");
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
        return FSDFacilityLoaderProcessorU.DBCP_SERVICE;
    }

    @Override
    protected Relationship successRelation() {
        return FSDFacilityLoaderProcessorU.SUCCESS;
    }
}
