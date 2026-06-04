package com.gym.config;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/* Excel 导出工具类，封装表头样式与响应流写入的通用逻辑 */
public class ExcelUtil {

    // 创建工作簿、设置表头样式、写入响应头，返回 Sheet 供调用方填充数据
    public static Workbook start(HttpServletResponse response, String sheetName, String fileName, String[] headers) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fn = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fn + ".xlsx");
        return workbook;
    }

    // 自适应列宽、写入响应流并关闭
    public static void finish(Workbook workbook, HttpServletResponse response) throws Exception {
        Sheet sheet = workbook.getSheetAt(0);
        for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) sheet.autoSizeColumn(i);
        OutputStream out = response.getOutputStream();
        workbook.write(out);
        out.flush();
        workbook.close();
    }
}
