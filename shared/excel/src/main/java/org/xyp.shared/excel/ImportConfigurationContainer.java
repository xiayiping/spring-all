package org.xyp.shared.excel;


import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xyp.shared.excel.model.ImportColumnModel;
import org.xyp.shared.excel.model.ImportSheetModel;
import org.xyp.shared.function.wrapper.ResultOrError;

import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

@Slf4j
@Component
public class ImportConfigurationContainer {

    public static final String ATT_SHEET_NAME = "name";
    public static final String ATT_SHEET_INDEX = "index";
    public static final String ATT_COL_INDEX = "index";
    public static final String ATT_START_ROW = "startRow";
    public static final String ATT_END_ROW = "endRow";

    public static final String ATT_KEY = "key";
    public static final String ATT_TYPE = "type";

    public static final String ELE_SHEET = "sheet";
    public static final String ELE_COLUMN = "column";

    final String rootPath;

    private ImportConfigurationContainerHandler handler = null;
    private XMLReader configReader = null;

    private final Map<String, ImportSheetModel> importMap = new HashMap<>();

    public ImportConfigurationContainer(CorejExcelProperties props) {
        this.handler = new ImportConfigurationContainerHandler();
        this.configReader = ResultOrError.on(() ->
            SAXParserFactory.newInstance().newSAXParser().getXMLReader()).get();
        configReader.setContentHandler(handler);

        File file = ResultOrError.on(() ->
            new DefaultResourceLoader().getResource(props.getImportConfigRoot()).getFile()).get();
        var rootPathLoc = file.getAbsolutePath()
            .replace("\\", "/");
        while (rootPathLoc.endsWith("/")) {
            rootPathLoc = rootPathLoc.substring(0, rootPathLoc.length() - 1);
        }
        rootPath = rootPathLoc;

        ResultOrError.doRun(() -> ConfigFunctions.processFile(file, this::processFile))
            .get();
    }

    public void processFile(File file) {
        ConfigFunctions.processFile(file, f -> {
            handler.currentFile = file;
            ResultOrError.doRun(() -> configReader.parse(new InputSource(new FileInputStream(file))))
                .get();
        });
    }

    class ImportConfigurationContainerHandler extends DefaultHandler2 {
        File currentFile = null;
        ImportSheetModel sheetModel = null;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if (ELE_SHEET.equals(qName)) {
                String sheetName = attributes.getValue(ATT_SHEET_NAME);
                List<ImportColumnModel> columnList = new LinkedList<>();
                String startRowStr = attributes.getValue(ATT_START_ROW);
                String endRowStr = attributes.getValue(ATT_END_ROW);
                String sheetIndexStr = attributes.getValue(ATT_SHEET_INDEX);
                sheetModel = new ImportSheetModel(sheetName,
                    Optional.ofNullable(sheetIndexStr).map(Integer::valueOf).map(i -> i - 1).orElse(0),
                    Optional.ofNullable(startRowStr).map(Integer::valueOf).map(i -> i - 1).orElse(0),
                    Optional.ofNullable(endRowStr).map(Integer::valueOf).map(i -> i - 1).orElse(-1),
                    columnList
                );
            } else if (ELE_COLUMN.equals(qName)) {
                String key = attributes.getValue(ATT_KEY);
                String type = attributes.getValue(ATT_TYPE);
                String idx = attributes.getValue(ATT_COL_INDEX);
                ImportColumnModel columnModel = new ImportColumnModel(
                    key, type,
                    Optional.ofNullable(idx).map(Integer::valueOf).map(i -> i - 1).orElse(null)
                );
                sheetModel.getColumnList().add(columnModel);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (ELE_SHEET.equals(qName)) {
                importMap.put(getConfigName(currentFile), sheetModel);
                currentFile = null;
                sheetModel = null;
            }
        }

        private String getConfigName(File file) {
            return ConfigFunctions.getConfigName(file, rootPath);
        }
    }

    public ImportSheetModel getImportSheetModel(String name) {
        return this.importMap.get(name);
    }

    public Set<String> getConfigNames() {
        return this.importMap.keySet();
    }
}
