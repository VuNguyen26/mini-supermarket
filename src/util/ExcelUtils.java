package util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class ExcelUtils {

    public static File chooseSaveXlsxFile(java.awt.Component parent, String defaultName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Excel file");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx"));
        chooser.setSelectedFile(new File(defaultName.endsWith(".xlsx") ? defaultName : defaultName + ".xlsx"));

        int result = chooser.showSaveDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) return null;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".xlsx")) {
            file = new File(file.getParentFile(), file.getName() + ".xlsx");
        }
        return file;
    }

    // rows: mỗi row là List<Object> (String/Number/Boolean/Date...)
    public static void exportXlsx(File file, String sheetName, List<String> headers, List<List<Object>> rows) throws Exception {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet(sheetName != null ? sheetName : "Sheet1");

            int r = 0;

            // Header style
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Header row
            Row hr = sheet.createRow(r++);
            for (int c = 0; c < headers.size(); c++) {
                Cell cell = hr.createCell(c);
                cell.setCellValue(headers.get(c));
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (List<Object> rowData : rows) {
                Row row = sheet.createRow(r++);
                for (int c = 0; c < rowData.size(); c++) {
                    Cell cell = row.createCell(c);
                    Object v = rowData.get(c);
                    setCellValue(cell, v);
                }
            }

            // Auto size
            for (int c = 0; c < headers.size(); c++) {
                sheet.autoSizeColumn(c);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }
        }
    }

    private static void setCellValue(Cell cell, Object v) {
        if (v == null) {
            cell.setBlank();
        } else if (v instanceof Number n) {
            cell.setCellValue(n.doubleValue());
        } else if (v instanceof Boolean b) {
            cell.setCellValue(b);
        } else if (v instanceof java.util.Date d) {
            cell.setCellValue(d);
        } else {
            cell.setCellValue(String.valueOf(v));
        }
    }
}