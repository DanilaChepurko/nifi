package ru.comita.nifi;

import lombok.SneakyThrows;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.Relationship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class FSD2LoaderProcessorTest extends FSDLoaderProcessorTest {
    private final FSD2uLoaderProcessor processor = new FSD2uLoaderProcessor();
    private final Fsd2ConnectionManager connectionManager = new Fsd2ConnectionManager();

    @BeforeEach
    @SneakyThrows
    public void setUp() {
        super.setUp();
    }

    @Test
    @SneakyThrows
    public void testSuccess() {
        super.testSuccess("2.1_ФСД_Проектные_уровни_добычи_по_пласту.xlsm");
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
        return FSD2uLoaderProcessor.DBCP_SERVICE;
    }

    @Override
    protected Relationship successRelation() {
        return FSD2uLoaderProcessor.SUCCESS;
    }
}
