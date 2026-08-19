package ru.comita.lib.service;

import com.github.pjfanning.xlsx.impl.StreamingSheet;
import com.github.pjfanning.xlsx.impl.StreamingWorkbook;
import org.apache.commons.io.IOUtils;
import org.apache.nifi.logging.ComponentLog;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.comita.lib.dto.SchemaName;
import ru.comita.lib.exception.ExcelParserException;
import ru.comita.lib.exception.FsdParseException;
import ru.comita.lib.exception.SummaryException;
import ru.comita.lib.util.FsdParserUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class FSDExcelLoaderService<F extends FSDParserService> {
    private final DictionaryService dictionaryService;
    private final String formCell;
    private final String sheetNum;
    protected F fsdParserService;
    protected ComponentLog componentLog;
    protected final Logger logger = LoggerFactory.getLogger(FSDExcelLoaderService.class);
    protected final Connection connection;

    public FSDExcelLoaderService(String sheetNum,
                                 String formCell,
                                 Connection connection) {
        this.connection = connection;
        this.formCell = formCell;
        this.sheetNum = sheetNum;
        this.dictionaryService = new DictionaryService(connection);
    }

    public void loadExcelToTargetDatabase(String fileName, Map<SchemaName, String> schemaMap, InputStream in) throws ExcelParserException {
        File tmpFile = null;
        try {
            tmpFile = new File(fileName);
            Files.copy(
                    in,
                    tmpFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            IOUtils.closeQuietly(in);

            String form = null;
            int sheetIntNum = Integer.parseInt(sheetNum) - 1;
            try (StreamingWorkbook workbook = FsdParserUtil.getWorkbookFromFile(tmpFile)) {
                ExcelParserService excelParserService = new ExcelParserService(dictionaryService);
                initParser(excelParserService);
                fsdParserService.setComponentLog(componentLog);
                CellReference formCellReference = new CellReference(formCell);
                StreamingSheet sheet = (StreamingSheet) workbook.getSheetAt(sheetIntNum);
                Iterator<Row> rowIterator = sheet.rowIterator();
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    if (row.getRowNum() == formCellReference.getRow()) {
                        form = excelParserService.getCellValueByAddress(row, formCell);
                        break;
                    }
                }
                fsdParserService.initSchemas(form, rowIterator, schemaMap);
            }

            createAndLoadEntities(tmpFile, sheetIntNum);

            if (!fsdParserService.getErrorLog().isEmpty()) {
                throw new SummaryException();
            }

            connection.commit();

        } catch (Exception e) {
            String message = getSummaryExceptionMessage(e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException ex) {
                logger.error(message, e);
                logger.error(ex.getMessage(), ex);
                componentLog.error(ex.getMessage(), ex);
                throw new FsdParseException(ex);
            }
            componentLog.error(message);
            logger.error(message, e);
            throw new FsdParseException(message);
        } finally {
            if (tmpFile != null) {
                //noinspection ResultOfMethodCallIgnored
                tmpFile.delete();
            }
        }
    }

    private String getSummaryExceptionMessage(String additionalMessage) {
        Set<String> errorLogs = fsdParserService.getErrorLog();
        if (additionalMessage != null) {
            errorLogs.add(additionalMessage);
        }
        return String.join(System.lineSeparator(), errorLogs);
    }

    public void clearHeaderAndReferences(String sql, String headerTableName, List<String> referencingTableNames) throws SQLException {
        List<String> headerUuids = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                String headerUUID = resultSet.getString(1);
                if (headerUUID != null && !headerUUID.isEmpty()) {
                    headerUuids.add(headerUUID);
                }
            }
        }
        if (headerUuids.isEmpty()) {
            return;
        }

        for (String headerUUID : headerUuids) {
            for (String table : referencingTableNames) {
                String deleteSql = "delete from " + table + " where header_uuid = '" + headerUUID + "'";
                try (Statement statement = connection.createStatement()) {
                    statement.execute(deleteSql);
                }
            }
            String deleteSql = "delete from " + headerTableName + " where uuid = '" + headerUUID + "'";
            try (Statement statement = connection.createStatement()) {
                statement.execute(deleteSql);
            }
        }
    }

    public void setComponentLog(ComponentLog logger) {
        this.componentLog = logger;
    }

    protected abstract void initParser(ExcelParserService excelParserService);

    protected abstract void createAndLoadEntities(File file, Integer sheetNum) throws SQLException, IOException;
}
