package ru.comita.lib;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.dbcp.DBCPService;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import ru.comita.lib.dto.SchemaName;
import ru.comita.lib.service.FSDExcelLoaderService;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class FSDLoaderProcessor<L extends FSDExcelLoaderService<?>> extends AbstractProcessor {
    private Set<Relationship> relationships;
    private List<PropertyDescriptor> properties;
    protected L fsdExcelLoaderService;

    public static final Relationship SUCCESS = new Relationship.Builder()
            .name("SUCCESS")
            .description("Успешный парсинг Excel-файла")
            .build();

    public static final Relationship FAILURE = new Relationship.Builder()
            .name("FAILURE")
            .description("Ошибка при парсинге Excel-файла")
            .build();

    public static final PropertyDescriptor FORM_CELL = new PropertyDescriptor.Builder()
            .name("Form Cell")
            .displayName("Form Cell")
            .required(true)
            .description("Ячейка, в которой указана форма")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .defaultValue("B1")
            .build();

    public static final PropertyDescriptor DBCP_SERVICE = new PropertyDescriptor.Builder()
            .name("FSD DBCP service")
            .displayName("FSD DBCP service")
            .required(true)
            .description("Сервис для подключения к целевой БД")
            .identifiesControllerService(DBCPService.class)
            .build();

    @Override
    protected void init(ProcessorInitializationContext context) {
        this.relationships = Set.of(FAILURE, SUCCESS);
        List<PropertyDescriptor> schemaPropertyDescriptors = schemaProperties();
        List<PropertyDescriptor> propertyDescriptors = List.of(FORM_CELL, DBCP_SERVICE);
        this.properties = new ArrayList<>();
        this.properties.addAll(propertyDescriptors);
        this.properties.addAll(schemaPropertyDescriptors);
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        final FlowFile flowfile = session.get();
        if (flowfile == null) {
            context.yield();
            return;
        }

        FlowFile output = session.create(flowfile);
        String fileName = flowfile.getAttribute("filename");

        DBCPService targetDBCPService = context.getProperty(DBCP_SERVICE).asControllerService(DBCPService.class);

        try (Connection connection = targetDBCPService.getConnection()) {
            String formCell = context.getProperty("Form Cell").getValue();
            String sheetNum = context.getProperty("Sheet number").getValue();
            initFsdExcelLoaderService(sheetNum, formCell, connection);
            fsdExcelLoaderService.setComponentLog(getLogger());
            Map<SchemaName, String> schemaMap = getSchemaMap(context);

            session.read(flowfile, in ->
                    fsdExcelLoaderService.loadExcelToTargetDatabase(fileName, schemaMap, in));

            session.remove(flowfile);
            session.transfer(output, SUCCESS);
            getLogger().info("Документ успешно обработан");
        } catch (Throwable e) {
            session.remove(output);
            session.putAttribute(flowfile, "error", e.getLocalizedMessage());
            session.transfer(flowfile, FAILURE);
        }
    }

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    public List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    public abstract List<PropertyDescriptor> schemaProperties();

    public abstract Map<SchemaName, String> getSchemaMap(ProcessContext context);

    public abstract void initFsdExcelLoaderService(String sheetNum,
                                                   String formCell,
                                                   Connection connection);
}