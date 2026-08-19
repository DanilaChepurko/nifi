package ru.comita.lib.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.nifi.logging.ComponentLog;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.comita.lib.dto.SchemaName;
import ru.comita.lib.exception.SchemaException;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public abstract class FSDParserService {

    protected final ExcelParserService excelParserService;
    @Getter
    private final Set<String> errorLog = new HashSet<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Setter
    protected ComponentLog componentLog;
    protected final Logger logger = LoggerFactory.getLogger(FSDParserService.class);

    public FSDParserService(ExcelParserService excelParserService) {
        this.excelParserService = excelParserService;
    }

    protected abstract void initSchemas(String form, Iterator<Row> rowIterator, Map<SchemaName, String> schemaMap) throws IOException;

    protected <S> S initSchema(String schema,
                               String form,
                               TypeReference<HashMap<String, S>> typeRef) throws JsonProcessingException {
        HashMap<String, S> parsedSchemaMap = objectMapper.readValue(schema, typeRef);
        return parsedSchemaMap.get(form);
    }

    protected <S> S initSchema(String schema,
                               TypeReference<S> typeRef) {
        try {
            return objectMapper.readValue(schema, typeRef);
        } catch (Exception e) {
            throw new SchemaException("Не удалось получить схему: " + typeRef.getType().getTypeName());
        }
    }
}
