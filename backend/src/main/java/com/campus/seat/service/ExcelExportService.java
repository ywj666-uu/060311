package com.campus.seat.service;

import com.campus.seat.dto.SeatMapResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExcelExportService {

    public byte[] exportSeatMap(SeatMapResponse seatMap) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            createSeatMapSheet(workbook, seatMap);
            createStudentSeatBindingSheet(workbook, seatMap);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void createSeatMapSheet(Workbook workbook, SeatMapResponse seatMap) {
        Sheet sheet = workbook.createSheet("座位图");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle allocatedStyle = workbook.createCellStyle();
        allocatedStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        allocatedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        allocatedStyle.setAlignment(HorizontalAlignment.CENTER);
        allocatedStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        allocatedStyle.setBorderBottom(BorderStyle.THIN);
        allocatedStyle.setBorderTop(BorderStyle.THIN);
        allocatedStyle.setBorderLeft(BorderStyle.THIN);
        allocatedStyle.setBorderRight(BorderStyle.THIN);
        allocatedStyle.setWrapText(true);

        CellStyle availableStyle = workbook.createCellStyle();
        availableStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        availableStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        availableStyle.setAlignment(HorizontalAlignment.CENTER);
        availableStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        availableStyle.setBorderBottom(BorderStyle.THIN);
        availableStyle.setBorderTop(BorderStyle.THIN);
        availableStyle.setBorderLeft(BorderStyle.THIN);
        availableStyle.setBorderRight(BorderStyle.THIN);

        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("【讲台】");
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, seatMap.getTotalCols()));

        Row colHeaderRow = sheet.createRow(1);
        colHeaderRow.createCell(0).setCellValue("");
        for (int col = 1; col <= seatMap.getTotalCols(); col++) {
            Cell cell = colHeaderRow.createCell(col);
            cell.setCellValue("第" + col + "列");
            cell.setCellStyle(headerStyle);
        }

        Map<Integer, List<SeatMapResponse.SeatInfo>> rowGroups = seatMap.getSeats().stream()
                .collect(Collectors.groupingBy(SeatMapResponse.SeatInfo::getRowNum));

        for (int row = 1; row <= seatMap.getTotalRows(); row++) {
            Row excelRow = sheet.createRow(row + 1);
            excelRow.setHeightInPoints(36);
            Cell labelCell = excelRow.createCell(0);
            labelCell.setCellValue("第" + row + "排");
            labelCell.setCellStyle(headerStyle);

            List<SeatMapResponse.SeatInfo> rowSeats = rowGroups.get(row);
            if (rowSeats != null) {
                for (SeatMapResponse.SeatInfo seatInfo : rowSeats) {
                    Cell cell = excelRow.createCell(seatInfo.getColNum());
                    if ("ALLOCATED".equals(seatInfo.getStatus()) && seatInfo.getStudentName() != null) {
                        String display = seatInfo.getRowNum() + "-" + seatInfo.getColNum()
                                + "\n" + seatInfo.getStudentName();
                        cell.setCellValue(display);
                        cell.setCellStyle(allocatedStyle);
                    } else {
                        cell.setCellValue(seatInfo.getRowNum() + "-" + seatInfo.getColNum());
                        cell.setCellStyle(availableStyle);
                    }
                }
            }
        }

        for (int i = 0; i <= seatMap.getTotalCols(); i++) {
            sheet.setColumnWidth(i, 4000);
        }
    }

    /**
     * 座位分配明细：含行列号、偏好区域、实际区域、偏好匹配结果
     */
    private void createStudentSeatBindingSheet(Workbook workbook, SeatMapResponse seatMap) {
        Sheet sheet = workbook.createSheet("座位分配明细");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.MEDIUM);

        // 偏好命中样式（绿色背景）
        CellStyle matchStyle = workbook.createCellStyle();
        matchStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        matchStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        matchStyle.setAlignment(HorizontalAlignment.CENTER);

        // 偏好未命中样式（浅红色背景）
        CellStyle mismatchStyle = workbook.createCellStyle();
        mismatchStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        mismatchStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        mismatchStyle.setAlignment(HorizontalAlignment.CENTER);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"座位号", "行号", "列号", "实际区域", "学号", "姓名", "团队",
                "偏好区域", "偏好匹配", "分配状态"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<SeatMapResponse.SeatInfo> sortedSeats = seatMap.getSeats().stream()
                .sorted(Comparator.comparingInt(SeatMapResponse.SeatInfo::getRowNum)
                        .thenComparingInt(SeatMapResponse.SeatInfo::getColNum))
                .collect(Collectors.toList());

        int rowIdx = 1;
        for (SeatMapResponse.SeatInfo seat : sortedSeats) {
            if ("UNAVAILABLE".equals(seat.getStatus())) continue;

            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(seat.getRowNum() + "排" + seat.getColNum() + "列");
            row.createCell(1).setCellValue(seat.getRowNum());
            row.createCell(2).setCellValue(seat.getColNum());
            row.createCell(3).setCellValue(translateArea(seat.getAreaTag()));

            if ("ALLOCATED".equals(seat.getStatus())) {
                row.createCell(4).setCellValue(seat.getStudentId() != null ? seat.getStudentId() : "");
                row.createCell(5).setCellValue(seat.getStudentName() != null ? seat.getStudentName() : "");
                row.createCell(6).setCellValue(seat.getTeamName() != null ? seat.getTeamName() : "—");
                row.createCell(7).setCellValue(translateArea(seat.getPreferredArea()));

                Cell matchCell = row.createCell(8);
                if (seat.isPreferenceMatched()) {
                    matchCell.setCellValue("匹配");
                    matchCell.setCellStyle(matchStyle);
                } else {
                    matchCell.setCellValue("未匹配");
                    matchCell.setCellStyle(mismatchStyle);
                }

                row.createCell(9).setCellValue("已分配");
            } else {
                row.createCell(4).setCellValue("");
                row.createCell(5).setCellValue("");
                row.createCell(6).setCellValue("");
                row.createCell(7).setCellValue("");
                row.createCell(8).setCellValue("");
                row.createCell(9).setCellValue("空闲");
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, 3800);
        }
        sheet.setColumnWidth(0, 4500);
        sheet.setColumnWidth(8, 3200);
    }

    private String translateArea(String areaTag) {
        if (areaTag == null) return "不限";
        return switch (areaTag) {
            case "FRONT" -> "前排";
            case "BACK" -> "后排";
            case "WINDOW" -> "靠窗";
            case "WINDOW_LEFT" -> "靠窗(左)";
            case "WINDOW_RIGHT" -> "靠窗(右)";
            case "MIDDLE" -> "中间";
            case "ANY" -> "不限";
            default -> areaTag;
        };
    }
}
