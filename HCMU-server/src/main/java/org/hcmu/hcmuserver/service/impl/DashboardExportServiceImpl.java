package org.hcmu.hcmuserver.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmucommon.utils.AliOssUtil;
import org.hcmu.hcmupojo.dto.AppointmentDashboardDTO;
import org.hcmu.hcmupojo.dto.DashboardExportDTO;
import org.hcmu.hcmupojo.dto.DepartmentDashboardDTO;
import org.hcmu.hcmupojo.dto.DoctorDashboardDTO;
import org.hcmu.hcmupojo.vo.AppointmentDashboardVO;
import org.hcmu.hcmupojo.vo.DepartmentDashboardVO;
import org.hcmu.hcmupojo.vo.DoctorDashboardVO;
import org.hcmu.hcmuserver.service.AppointmentDashboardService;
import org.hcmu.hcmuserver.service.DashboardExportService;
import org.hcmu.hcmuserver.service.DepartmentDashboardService;
import org.hcmu.hcmuserver.service.DoctorDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 大屏数据导出服务实现
 */
@Service
@Slf4j
public class DashboardExportServiceImpl implements DashboardExportService {

    @Autowired
    private DepartmentDashboardService departmentDashboardService;

    @Autowired
    private DoctorDashboardService doctorDashboardService;

    @Autowired
    private AppointmentDashboardService appointmentDashboardService;

    @Autowired
    private AliOssUtil aliOssUtil;

    @Override
    public Result<String> exportDashboardData(DashboardExportDTO exportDTO) {
        log.info("开始导出大屏数据，参数: {}", exportDTO);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // 创建样式
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // 1. 导出科室数据
            exportDepartmentData(workbook, exportDTO, titleStyle, headerStyle, dataStyle);

            // 2. 导出医生数据
            exportDoctorData(workbook, exportDTO, titleStyle, headerStyle, dataStyle);

            // 3. 导出号源数据
            exportAppointmentData(workbook, exportDTO, titleStyle, headerStyle, dataStyle);

            // 将工作簿写入输出流
            workbook.write(outputStream);
            byte[] excelBytes = outputStream.toByteArray();

            // 生成文件名
            String fileName = "大屏数据导出_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + "_" + UUID.randomUUID().toString().substring(0, 8) + ".xlsx";

            // 上传到云端
            String fileUrl = aliOssUtil.upload(excelBytes, fileName);
            log.info("大屏数据导出成功，文件地址: {}", fileUrl);

            return Result.success(fileUrl);

        } catch (IOException e) {
            log.error("导出大屏数据失败", e);
            return Result.error("导出失败: " + e.getMessage());
        }
    }

    /**
     * 导出科室数据
     */
    private void exportDepartmentData(Workbook workbook, DashboardExportDTO exportDTO,
                                     CellStyle titleStyle, CellStyle headerStyle, CellStyle dataStyle) {
        Sheet sheet = workbook.createSheet("科室数据");
        int rowNum = 0;

        // 标题
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        String title = String.format("科室可视化大屏数据 (时间范围: %s | 排行榜显示: Top %d)",
                exportDTO.getTimeRange().getDescription(),
                exportDTO.getRankLimit());
        titleCell.setCellValue(title);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
        titleRow.setHeightInPoints(30);

        rowNum++; // 空行

        // 1. 科室负荷统计
        Result<DepartmentDashboardVO.LoadStatisticsVO> loadResult = departmentDashboardService.getDepartmentLoadStatistics();
        if (loadResult.getData() != null) {
            DepartmentDashboardVO.LoadStatisticsVO loadData = loadResult.getData();

            Row sectionTitleRow = sheet.createRow(rowNum++);
            Cell sectionTitleCell = sectionTitleRow.createCell(0);
            sectionTitleCell.setCellValue("科室负荷统计");
            sectionTitleCell.setCellStyle(headerStyle);

            Row loadHeaderRow = sheet.createRow(rowNum++);
            String[] loadHeaders = {"科室总数", "高负荷科室", "中负荷科室", "低负荷科室", "空闲科室"};
            for (int i = 0; i < loadHeaders.length; i++) {
                Cell cell = loadHeaderRow.createCell(i);
                cell.setCellValue(loadHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            Row loadDataRow = sheet.createRow(rowNum++);
            loadDataRow.createCell(0).setCellValue(loadData.getTotalDepartments());
            loadDataRow.createCell(1).setCellValue(loadData.getHighLoadDepartments());
            loadDataRow.createCell(2).setCellValue(loadData.getMediumLoadDepartments());
            loadDataRow.createCell(3).setCellValue(loadData.getLowLoadDepartments());
            loadDataRow.createCell(4).setCellValue(loadData.getIdleDepartments());
            for (int i = 0; i < 5; i++) {
                loadDataRow.getCell(i).setCellStyle(dataStyle);
            }

            rowNum++; // 空行

            // 各负荷等级科室详情
            exportDepartmentList(sheet, "高负荷科室列表", loadData.getHighLoadDepartmentList(), headerStyle, dataStyle, rowNum);
            rowNum += loadData.getHighLoadDepartmentList().size() + 3;

            exportDepartmentList(sheet, "中负荷科室列表", loadData.getMediumLoadDepartmentList(), headerStyle, dataStyle, rowNum);
            rowNum += loadData.getMediumLoadDepartmentList().size() + 3;

            exportDepartmentList(sheet, "低负荷科室列表", loadData.getLowLoadDepartmentList(), headerStyle, dataStyle, rowNum);
            rowNum += loadData.getLowLoadDepartmentList().size() + 3;

            exportDepartmentList(sheet, "空闲科室列表", loadData.getIdleDepartmentList(), headerStyle, dataStyle, rowNum);
            rowNum += loadData.getIdleDepartmentList().size() + 3;
        }

        // 2. 科室预约排行
        DepartmentDashboardDTO.AppointmentRankDTO rankDTO = new DepartmentDashboardDTO.AppointmentRankDTO();
        rankDTO.setTimeRange(exportDTO.getTimeRange());
        rankDTO.setLimit(exportDTO.getRankLimit());

        Result<DepartmentDashboardVO.AppointmentRankVO> rankResult = departmentDashboardService.getDepartmentAppointmentRank(rankDTO);
        if (rankResult.getData() != null && rankResult.getData().getRankList() != null) {
            Row sectionTitleRow = sheet.createRow(rowNum++);
            Cell sectionTitleCell = sectionTitleRow.createCell(0);
            sectionTitleCell.setCellValue("科室预约排行榜 (时间范围: " + exportDTO.getTimeRange().getDescription() + ")");
            sectionTitleCell.setCellStyle(headerStyle);

            Row rankHeaderRow = sheet.createRow(rowNum++);
            String[] rankHeaders = {"排名", "科室ID", "科室名称", "预约数量"};
            for (int i = 0; i < rankHeaders.length; i++) {
                Cell cell = rankHeaderRow.createCell(i);
                cell.setCellValue(rankHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            for (DepartmentDashboardVO.AppointmentRankItemVO item : rankResult.getData().getRankList()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getRank());
                row.createCell(1).setCellValue(item.getDepartmentId());
                row.createCell(2).setCellValue(item.getDepartmentName());
                row.createCell(3).setCellValue(item.getAppointmentCount());
                for (int i = 0; i < 4; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
        }

        // 自动调整列宽
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
        }
    }

    /**
     * 导出科室列表
     */
    private void exportDepartmentList(Sheet sheet, String title, java.util.List<DepartmentDashboardVO.DepartmentInfoVO> list,
                                     CellStyle headerStyle, CellStyle dataStyle, int startRow) {
        int rowNum = startRow;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(headerStyle);

        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("科室ID");
        headerRow.createCell(1).setCellValue("科室名称");
        headerRow.getCell(0).setCellStyle(headerStyle);
        headerRow.getCell(1).setCellStyle(headerStyle);

        for (DepartmentDashboardVO.DepartmentInfoVO dept : list) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dept.getDepartmentId());
            row.createCell(1).setCellValue(dept.getDepartmentName());
            row.getCell(0).setCellStyle(dataStyle);
            row.getCell(1).setCellStyle(dataStyle);
        }
    }

    /**
     * 导出医生数据
     */
    private void exportDoctorData(Workbook workbook, DashboardExportDTO exportDTO,
                                 CellStyle titleStyle, CellStyle headerStyle, CellStyle dataStyle) {
        Sheet sheet = workbook.createSheet("医生数据");
        int rowNum = 0;

        // 标题
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        String title = String.format("医生可视化大屏数据 (时间范围: %s | 排行榜显示: Top %d)",
                exportDTO.getTimeRange().getDescription(),
                exportDTO.getRankLimit());
        titleCell.setCellValue(title);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
        titleRow.setHeightInPoints(30);

        rowNum++; // 空行

        // 1. 医生就诊量排行
        DoctorDashboardDTO.DoctorVisitRankDTO visitRankDTO = new DoctorDashboardDTO.DoctorVisitRankDTO();
        visitRankDTO.setTimeRange(exportDTO.getTimeRange());
        visitRankDTO.setLimit(exportDTO.getRankLimit());

        Result<DoctorDashboardVO.DoctorVisitRankVO> visitRankResult = doctorDashboardService.getDoctorVisitRank(visitRankDTO);
        if (visitRankResult.getData() != null && visitRankResult.getData().getRankList() != null) {
            Row sectionTitleRow = sheet.createRow(rowNum++);
            Cell sectionTitleCell = sectionTitleRow.createCell(0);
            sectionTitleCell.setCellValue("医生就诊量排行榜 (时间范围: " + exportDTO.getTimeRange().getDescription() + ")");
            sectionTitleCell.setCellStyle(headerStyle);

            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"排名", "医生ID", "医生姓名", "就诊数量"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (DoctorDashboardVO.DoctorVisitRankItemVO item : visitRankResult.getData().getRankList()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getRank());
                row.createCell(1).setCellValue(item.getDoctorUserId());
                row.createCell(2).setCellValue(item.getDoctorName());
                row.createCell(3).setCellValue(item.getVisitCount());
                for (int i = 0; i < 4; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
        }

        rowNum += 2; // 空行

        // 2. 医生收入排行
        DoctorDashboardDTO.DoctorIncomeRankDTO incomeRankDTO = new DoctorDashboardDTO.DoctorIncomeRankDTO();
        incomeRankDTO.setTimeRange(exportDTO.getTimeRange());
        incomeRankDTO.setLimit(exportDTO.getRankLimit());

        Result<DoctorDashboardVO.DoctorIncomeRankVO> incomeRankResult = doctorDashboardService.getDoctorIncomeRank(incomeRankDTO);
        if (incomeRankResult.getData() != null && incomeRankResult.getData().getRankList() != null) {
            Row sectionTitleRow = sheet.createRow(rowNum++);
            Cell sectionTitleCell = sectionTitleRow.createCell(0);
            sectionTitleCell.setCellValue("医生收入排行榜 (时间范围: " + exportDTO.getTimeRange().getDescription() + ")");
            sectionTitleCell.setCellStyle(headerStyle);

            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"排名", "医生ID", "医生姓名", "总收入(元)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (DoctorDashboardVO.DoctorIncomeRankItemVO item : incomeRankResult.getData().getRankList()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getRank());
                row.createCell(1).setCellValue(item.getDoctorUserId());
                row.createCell(2).setCellValue(item.getDoctorName());
                row.createCell(3).setCellValue(item.getTotalIncome() != null ? item.getTotalIncome().doubleValue() : 0.0);
                for (int i = 0; i < 4; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
        }

        rowNum += 2; // 空行

        // 3. 医生预约率统计
        DoctorDashboardDTO.DoctorAppointmentRateDTO rateDTO = new DoctorDashboardDTO.DoctorAppointmentRateDTO();
        rateDTO.setTimeRange(exportDTO.getTimeRange());

        Result<DoctorDashboardVO.DoctorAppointmentRateVO> rateResult = doctorDashboardService.getDoctorAppointmentRate(rateDTO);
        if (rateResult.getData() != null && rateResult.getData().getRateList() != null) {
            Row sectionTitleRow = sheet.createRow(rowNum++);
            Cell sectionTitleCell = sectionTitleRow.createCell(0);
            sectionTitleCell.setCellValue("医生预约率统计 (时间范围: " + exportDTO.getTimeRange().getDescription() + ")");
            sectionTitleCell.setCellStyle(headerStyle);

            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"医生ID", "医生姓名", "完成数量", "取消数量", "爽约数量", "完成率(%)", "取消率(%)", "爽约率(%)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (DoctorDashboardVO.DoctorAppointmentRateItemVO item : rateResult.getData().getRateList()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getDoctorUserId());
                row.createCell(1).setCellValue(item.getDoctorName());
                row.createCell(2).setCellValue(item.getCompletedCount());
                row.createCell(3).setCellValue(item.getCancelledCount());
                row.createCell(4).setCellValue(item.getNoShowCount());
                row.createCell(5).setCellValue(item.getCompletedRate() != null ? item.getCompletedRate().doubleValue() : 0.0);
                row.createCell(6).setCellValue(item.getCancelledRate() != null ? item.getCancelledRate().doubleValue() : 0.0);
                row.createCell(7).setCellValue(item.getNoShowRate() != null ? item.getNoShowRate().doubleValue() : 0.0);
                for (int i = 0; i < 8; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
        }

        // 自动调整列宽
        for (int i = 0; i < 8; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
        }
    }

    /**
     * 导出号源数据
     */
    private void exportAppointmentData(Workbook workbook, DashboardExportDTO exportDTO,
                                      CellStyle titleStyle, CellStyle headerStyle, CellStyle dataStyle) {
        Sheet sheet = workbook.createSheet("号源数据");
        int rowNum = 0;

        // 标题
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        String title = String.format("号源可视化大屏数据 (时间范围: %s)",
                exportDTO.getTimeRange().getDescription());
        titleCell.setCellValue(title);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
        titleRow.setHeightInPoints(30);

        rowNum++; // 空行

        // 1. 号源统计
        AppointmentDashboardDTO.AppointmentStatisticsDTO statisticsDTO = new AppointmentDashboardDTO.AppointmentStatisticsDTO();
        statisticsDTO.setTimeRange(exportDTO.getTimeRange());

        Result<AppointmentDashboardVO.AppointmentStatisticsVO> statisticsResult = appointmentDashboardService.getAppointmentStatistics(statisticsDTO);
        if (statisticsResult.getData() != null) {
            AppointmentDashboardVO.AppointmentStatisticsVO data = statisticsResult.getData();

            Row sectionTitleRow = sheet.createRow(rowNum++);
            Cell sectionTitleCell = sectionTitleRow.createCell(0);
            sectionTitleCell.setCellValue("号源统计 (时间范围: " + exportDTO.getTimeRange().getDescription() + ")");
            sectionTitleCell.setCellStyle(headerStyle);

            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"待支付", "已预约", "已完成", "已取消", "已爽约", "总收入(元)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            Row dataRow = sheet.createRow(rowNum++);
            dataRow.createCell(0).setCellValue(data.getPendingPaymentCount());
            dataRow.createCell(1).setCellValue(data.getBookedCount());
            dataRow.createCell(2).setCellValue(data.getCompletedCount());
            dataRow.createCell(3).setCellValue(data.getCancelledCount());
            dataRow.createCell(4).setCellValue(data.getNoShowCount());
            dataRow.createCell(5).setCellValue(data.getTotalRevenue() != null ? data.getTotalRevenue().doubleValue() : 0.0);
            for (int i = 0; i < 6; i++) {
                dataRow.getCell(i).setCellStyle(dataStyle);
            }
        }

        rowNum += 2; // 空行

        // 2. 预约趋势
        AppointmentDashboardDTO.AppointmentTrendDTO trendDTO = new AppointmentDashboardDTO.AppointmentTrendDTO();
        trendDTO.setTimeRange(exportDTO.getTimeRange());

        Result<AppointmentDashboardVO.AppointmentTrendVO> trendResult = appointmentDashboardService.getAppointmentTrend(trendDTO);
        if (trendResult.getData() != null && trendResult.getData().getTrendData() != null) {
            Row sectionTitleRow = sheet.createRow(rowNum++);
            Cell sectionTitleCell = sectionTitleRow.createCell(0);
            sectionTitleCell.setCellValue("预约量趋势 (时间范围: " + exportDTO.getTimeRange().getDescription() + ")");
            sectionTitleCell.setCellStyle(headerStyle);

            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("时段/日期");
            headerRow.createCell(1).setCellValue("预约数量");
            headerRow.getCell(0).setCellStyle(headerStyle);
            headerRow.getCell(1).setCellStyle(headerStyle);

            for (AppointmentDashboardVO.TrendDataPoint point : trendResult.getData().getTrendData()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(point.getPeriodLabel());
                row.createCell(1).setCellValue(point.getCount());
                row.getCell(0).setCellStyle(dataStyle);
                row.getCell(1).setCellStyle(dataStyle);
            }
        }

        // 自动调整列宽
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
        }
    }

    /**
     * 创建标题样式
     */
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 创建数据样式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}