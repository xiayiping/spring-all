package org.xyp.shared.excel;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xyp.shared.excel.model.ExportColumnModel;
import org.xyp.shared.excel.model.ExportSheetModel;
import org.xyp.shared.function.wrapper.ResultOrError;

import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import static org.xyp.shared.excel.model.ExportColumnModel.DEFAULT_COL_WIDTH;


@Slf4j
@Component
public class ExportConfigurationContainer {

    public static final String ATT_NAME = "name";
    public static final String ATT_KEY = "key";
    public static final String ATT_TITLE = "title";
    public static final String ATT_FORMAT = "format";
    public static final String ELE_SHEET = "sheet";
    public static final String ELE_COLUMN = "column";
    public static final String ATT_TITLE_COLOR = "titleColor";
    public static final String ATT_TITLE_BOLD = "titleBold";
    public static final String ATT_TITLE_FONT = "titleFont";
    public static final String ATT_TITLE_FONT_SIZE = "titleFontSize";
    public static final String ATT_ALLOW_DUPLICATE = "allowDuplicate";
    public static final String ATT_START_ROW = "startRow";
    public static final String ATT_COL_INDEX = "index";
    public static final String ATT_COL_WIDTH = "colWidth";
    public static final String ATT_VAL_EMPTY = "empty";

    final String rootPath;

    private final ExportConfigurationContainerHandler handler;
    private final XMLReader configReader;

    private final Map<String, ExportSheetModel> exportMap = new HashMap<>();

    public ExportConfigurationContainer(CorejExcelProperties props) {
        this.handler = new ExportConfigurationContainerHandler();
        this.configReader = ResultOrError.on(() ->
            SAXParserFactory.newInstance().newSAXParser().getXMLReader()).get();
        configReader.setContentHandler(handler);

        File file = ResultOrError.on(() ->
            new DefaultResourceLoader().getResource(props.getExportConfigRoot()).getFile()).get();
        var rootPathLoc = file.getAbsolutePath()
            .replace("\\", "/");
        while (rootPathLoc.endsWith("/")) {
            rootPathLoc = rootPathLoc.substring(0, rootPathLoc.length() - 1);
        }
        rootPath = rootPathLoc;

        ResultOrError.doRun(() ->
                ConfigFunctions.processFile(file, this::processFile))
            .get();
    }

    public void processFile(File file) {
        ConfigFunctions.processFile(file, f -> {
            handler.currentFile = file;
            ResultOrError.doRun(() ->
                    configReader.parse(new InputSource(new FileInputStream(file))))
                .get();
        });
    }

    class ExportConfigurationContainerHandler extends DefaultHandler2 {
        File currentFile = null;
        ExportSheetModel sheetModel = null;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
            if (ELE_SHEET.equals(qName)) {
                String sheetName = attributes.getValue(ATT_NAME);
                val titleColor = attributes.getValue(ATT_TITLE_COLOR);
                String titleBold = attributes.getValue(ATT_TITLE_BOLD);
                String titleFont = attributes.getValue(ATT_TITLE_FONT);
                String titleFontSize = attributes.getValue(ATT_TITLE_FONT_SIZE);
                String startRow = attributes.getValue(ATT_START_ROW);
                List<ExportColumnModel> columnList = new LinkedList<>();

                sheetModel = new ExportSheetModel(sheetName, titleColor,
                    Optional.ofNullable(titleBold).map("true"::equalsIgnoreCase).orElse(false),
                    titleFont,
                    Optional.ofNullable(titleFontSize).map(Short::valueOf).orElse(null),
                    Optional.ofNullable(startRow).map(Integer::valueOf).map(i -> i - 1).orElse(0),
                    columnList
                );

            } else if (ELE_COLUMN.equals(qName)) {
                String key = attributes.getValue(ATT_KEY);
                if (null == key) {
                    throw new SAXException("key cannot be null " + currentFile.getAbsolutePath());
                }
                String title = attributes.getValue(ATT_TITLE);
                String titleStyle = attributes.getValue(ATT_TITLE_COLOR);
                String titleBold = attributes.getValue(ATT_TITLE_BOLD);
                String titleFont = attributes.getValue(ATT_TITLE_FONT);
                String titleFontSize = attributes.getValue(ATT_TITLE_FONT_SIZE);
                String format = attributes.getValue(ATT_FORMAT);
                String colIdx = attributes.getValue(ATT_COL_INDEX);
                String colWidth = attributes.getValue(ATT_COL_WIDTH);
                String empty = attributes.getValue(ATT_VAL_EMPTY);
                boolean allowDuplicate = Optional.ofNullable(attributes.getValue(ATT_ALLOW_DUPLICATE))
                    .map(str -> !"false".equals(str))
                    .orElse(Boolean.TRUE);
                ExportColumnModel columnModel = new ExportColumnModel(
                    key,
                    title,
                    titleStyle,
                    Optional.ofNullable(titleBold).map("true"::equalsIgnoreCase).orElse(null),
                    titleFont,
                    Optional.ofNullable(titleFontSize).map(Short::valueOf).orElse(null),
                    format,
                    Optional.ofNullable(colIdx).map(Integer::valueOf).map(i -> i - 1).orElse(null),
                    Optional.ofNullable(colWidth).map(Integer::valueOf).orElse(DEFAULT_COL_WIDTH),
                    Optional.ofNullable(empty).map(Boolean::valueOf).orElse(false),
                    allowDuplicate);

                sheetModel.getColumnList().add(columnModel);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (ELE_SHEET.equals(qName)) {
                exportMap.put(getConfigName(currentFile), sheetModel);
                currentFile = null;
                sheetModel = null;
            }
        }

        private String getConfigName(File file) {
            return ConfigFunctions.getConfigName(file, rootPath);
        }
    }


    public ExportSheetModel getExportSheetModel(String name) {
        return this.exportMap.get(name);
    }

    public Set<String> getConfigNames() {
        return this.exportMap.keySet();
    }

}
