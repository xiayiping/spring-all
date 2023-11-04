package org.xyp.demo.api;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class WoidPoi2 {
    public static void main(String[] args) throws IOException {

        try (
                FileInputStream fis = new FileInputStream("d:/tex/Incorp-quest-template-2.doc");
//                FileInputStream fis = new FileInputStream("d:/tex/Ab.doc");
                POIFSFileSystem poifs = new POIFSFileSystem(fis);
                HWPFDocument document = new HWPFDocument(poifs);) {

            Range range = document.getRange();

            String searchText = "Your search text";

//        for (int i = 0; i < range.numCharacterRuns(); i++) {
//            String text = range.getCharacterRun(i).text();
//            System.out.println("Found: " + text);
//        }

            System.out.println("total range " + range.numSections());
            for (int t = 0; t < range.numSections(); t++) {
                // Iterate through sections
                System.out.println("    total paragraphs in section " + t + " is " + range.numParagraphs());
                for (int i = 0; i < range.numParagraphs(); i++) {
                    // Iterate through paragraphs
                    org.apache.poi.hwpf.usermodel.Paragraph paragraph = range.getParagraph(i);

                    for (int c = 0; c < paragraph.numCharacterRuns(); c++) {
                        var run = paragraph.getCharacterRun(c);
                        String text = run.text();
                        System.out.println("Found: " + text);
//                        if (text.contains("signature")) {
//                            run.replaceText(text, "aaaahahahaha");
//                        }
                    }
//                if (paragraph.isInTable()) {
//                    // If the paragraph is inside a table
//
//
//                    var table = paragraph.getTable(paragraph.getParagraph(0));
//                    int numTableRows = table.numRows();
//                    for (int row = 0; row < numTableRows; row++) {
//
//                        int numTableCells = table.getRow(row).numCells();
//                        for (int cell = 0; cell < numTableCells; cell++) {
//                            String cellText = table.getRow(row).getCell(cell).text();
////                            if (cellText.contains(searchText)) {
//                                System.out.println("Found in table: " + cellText);
////                            }
//                        }
//                    }
//                } else {
//                    String text = paragraph.text();
////                    if (text != null && text.contains(searchText)) {
//                        System.out.println("Found: " + text);
////                    }
//                }
                }
            }

            try (FileOutputStream fos = new FileOutputStream("d:/tex/document.doc");) {

                document.write(fos);
            }
        }
    }
}
