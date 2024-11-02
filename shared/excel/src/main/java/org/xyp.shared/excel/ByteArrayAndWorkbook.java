package org.xyp.shared.excel;


import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.xyp.shared.function.FunctionException;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;

public class ByteArrayAndWorkbook implements Closeable {
    public final ByteArrayInputStream inputStream;
    public final XSSFWorkbook workbook;

    public ByteArrayAndWorkbook(byte[] ba) {
        this.inputStream = new ByteArrayInputStream(ba);
        try {
            this.workbook = new XSSFWorkbook(inputStream);
        } catch (IOException e) {
            this.close();
            throw new FunctionException(e);
        }
    }


    @Override
    public void close() {
        try {
            if (null != workbook) {
                workbook.close();
            }
            closeInputStream();
        } catch (IOException e) {
            closeInputStream();
            throw new FunctionException(e);
        }
    }

    private void closeInputStream() {
        try {
            if (null != inputStream) {
                inputStream.close();
            }
        } catch (IOException e) {
            throw new FunctionException(e);
        }
    }
}
