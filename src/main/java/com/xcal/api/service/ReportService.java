/*
   Copyright (C) 2019-2022 Xcalibyte (Shenzhen) Limited.
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.xcal.api.service;

import com.xcal.api.dao.IssueGroupDao;
import com.xcal.api.dao.UserDao;
import com.xcal.api.entity.Issue;
import com.xcal.api.entity.ScanTask;
import com.xcal.api.entity.*;
import com.xcal.api.entity.v3.*;
import com.xcal.api.exception.AppException;
import com.xcal.api.mapper.IssueGroupMapper;
import com.xcal.api.mapper.ProjectSummaryMapper;
import com.xcal.api.model.dto.v3.ProjectSummaryDto;
import com.xcal.api.model.dto.v3.RuleInfoDto;
import com.xcal.api.model.dto.v3.RuleListResponseDto;
import com.xcal.api.model.payload.v3.ExternalCsvReportRequest;
import com.xcal.api.model.payload.v3.ReportPDFResponse;
import com.xcal.api.model.payload.v3.ReportRequest;
import com.xcal.api.model.payload.v3.SearchIssueGroupRequest;
import com.xcal.api.repository.ScanFileRepository;
import com.xcal.api.repository.ScanTaskRepository;
import com.xcal.api.service.v3.ProjectServiceV3;
import com.xcal.api.service.v3.RuleServiceV3;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.DateUtil;
import com.xcal.api.util.StringUtil;
import com.xcal.api.util.VariableUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.knowm.xchart.*;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.internal.series.Series;
import org.knowm.xchart.style.CategoryStyler;
import org.knowm.xchart.style.PieStyler;
import org.knowm.xchart.style.Styler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static com.xcal.api.service.IssueService.SEARCH_VAL_DELIMITER;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReportService {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String VALUE_DELIMITER = ", ";
    @NonNull
    private DataSource dataSource;
    @NonNull
    private MeasureService measureService;
    @NonNull
    private IssueService issueService;
    @NonNull
    private I18nService i18nService;
    @NonNull
    private RuleService ruleService;
    @NonNull
    private ProjectService projectService;
    @NonNull
    private ProjectServiceV3 projectServiceV3;
    @NonNull
    ScanTaskService scanTaskService;
    @NonNull
    private ScanFileService scanFileService;

    private final RuleServiceV3 ruleServiceV3;

    private final IssueGroupDao issueGroupDao;

    private final UserDao userDao;

    private final ProjectSummaryMapper projectSummaryMapper;

    @NonNull ScanTaskRepository scanTaskRepository;

    @NonNull ScanFileRepository scanFileRepository;

    @NonNull IssueGroupMapper issueGroupMapper;

    @Value("${app.ui-host}")
    private String apiHost;

    @Value("${app.ui-port}")
    private String apiPort;

    public enum Format {
        PDF, CSV
    }

    private static final int DEFAULT_PAGE_SIZE = 1000;

    protected InputStream getInputStream(String name) {
        return ReportService.class.getResourceAsStream(name);
    }

    protected void registerFont() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String fontFolder = "/jasperreport/template/resources/fonts/";
        String fontPathBarlow = fontFolder + "BarlowSemiCondensed-Bold.ttf";
        String fontPathBarlowItalic = fontFolder + "BarlowSemiCondensed-Bold.ttf";
        String fontPathBarlowBold = fontFolder + "BarlowSemiCondensed-Bold.ttf";
        String fontPathBarlowBoldItalic = fontFolder + "BarlowSemiCondensed-Bold.ttf";
        String fontPathHan = fontFolder + "SourceHanSansCN-Bold.ttf";
        String fontPathHanBold = fontFolder + "SourceHanSansCN-Normal.ttf";
        try (InputStream fontPathBarlowStream = getInputStream(fontPathBarlow);
             InputStream fontPathBarlowItalicStream = getInputStream(fontPathBarlowItalic);
             InputStream fontPathBarlowBoldStream = getInputStream(fontPathBarlowBold);
             InputStream fontPathBarlowBoldItalicStream = getInputStream(fontPathBarlowBoldItalic);
             InputStream fontPathHanStream = getInputStream(fontPathHan);
             InputStream fontPathHanBoldStream = getInputStream(fontPathHanBold)) {
            if (fontPathBarlowStream != null) {
                ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontPathBarlowStream));
            }
            if (fontPathBarlowItalicStream != null) {
                ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontPathBarlowItalicStream));
            }
            if (fontPathBarlowBoldStream != null) {
                ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontPathBarlowBoldStream));
            }
            if (fontPathBarlowBoldItalicStream != null) {
                ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontPathBarlowBoldItalicStream));
            }
            if (fontPathHanStream != null) {
                ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontPathHanStream));
            }
            if (fontPathHanBoldStream != null) {
                ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontPathHanBoldStream));
            }
        } catch (FontFormatException | IOException e) {
            log.error("[registerFont] font register failed. exception, {}: {}", e.getClass(), e.getMessage());
        }
    }

    public Resource generateIssueSummaryReport(ScanTask scanTask, RuleSet ruleSet, Locale locale, User user) throws AppException {
        log.info("[generateIssueSummaryReport] scanTask: {}, ruleSet: {}, locale: {}", scanTask, ruleSet, locale);
        this.registerFont();
        JasperPrint jasperPrint;
        JasperReport issueSummaryReport = this.compileIssueSummaryReport();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("SCAN_ID", scanTask.getId().toString());
        parameters.put("RULE_SET_ID", ruleSet.getId().toString());
        parameters.put("CURRENT_USER", user.getDisplayName());
        parameters.put("COMPLIANCE_STANDARD", "Xcalibyte");
        parameters.put("SCAN_SUMMARY_MAP", scanTask.getSummary());

        Project project = scanTask.getProject();
        ProjectConfig projectConfig = this.projectService.getLatestActiveProjectConfigByProject(project).orElseThrow(() ->
                new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_REPORT_COMMON_GENERATE_REPORT_ERROR.unifyErrorCode,
                        CommonUtil.formatString("[{}] Missing active project config", AppException.ErrorCode.E_API_REPORT_COMMON_GENERATE_REPORT_ERROR.messageTemplate))
        );
        String language = projectConfig.getFirstAttribute(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE).map(ProjectConfigAttribute::getValue).orElse("");
        parameters.put("LANGUAGE", language);

        List<I18nMessage> i18nMessages = i18nService.getI18nMessageByKeyPrefix("report.", locale);
        CategoryChart priorityChart = this.generatePriorityChart(scanTask, ruleSet, locale, i18nMessages);
        // will also put action count into parameters
        PieChart actionChart = this.generateActionChart(scanTask, ruleSet, locale, parameters);
        PieChart typeChart = this.generateVulnerableChart(scanTask, ruleSet, locale, parameters);
        parameters.put("PRIORITY_CHART", this.convertChartToSvgByteArray(priorityChart));
        parameters.put("ACTION_CHART", this.convertChartToSvgByteArray(actionChart));
        parameters.put("TYPE_CHART", this.convertChartToSvgByteArray(typeChart));

        ResourceBundle bundle = ResourceBundle.getBundle("jasperreport/template/resources/i18n/report_message", locale);
        parameters.put(JRParameter.REPORT_RESOURCE_BUNDLE, bundle);
        parameters.put(JRParameter.REPORT_LOCALE, locale);

        try {
            jasperPrint = JasperFillManager.fillReport(issueSummaryReport, parameters, dataSource.getConnection());
        } catch (JRException | SQLException e) {
            log.error("Error when fillReport", e);
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_REPORT_COMMON_GENERATE_REPORT_ERROR.unifyErrorCode,
                    CommonUtil.formatString("[{}] message: {}", AppException.ErrorCode.E_API_REPORT_COMMON_GENERATE_REPORT_ERROR.messageTemplate, e.getMessage()));
        }
        Resource resource;
        try {
            Path tempDir = Files.createTempDirectory(scanTask.getId().toString()); // <project-name>_defect_report_<print-date>
            String dateString = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now());
            Path tempFile = Files.createFile(Paths.get(tempDir.toString(), scanTask.getProject().getName() + "_defect_report_" + dateString + ".pdf"));
            this.printReportAsPDF(jasperPrint, tempFile.toFile(), "xcalibyte", "Defect summary report", "Scan id: " + scanTask.getId());
            resource = new UrlResource(tempFile.toUri());
        } catch (IOException | JRException e) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_REPORT_COMMON_GENERATE_REPORT_ERROR.unifyErrorCode,
                    CommonUtil.formatString("[{}] message: {}", AppException.ErrorCode.E_API_REPORT_COMMON_GENERATE_REPORT_ERROR.messageTemplate, e.getMessage()), e);
        }
        return resource;
    }

    public void printReportAsPDF(JasperPrint jasperPrint, File file, String author, String title, String subject) throws JRException {
        JRPdfExporter exporter = new JRPdfExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(file));
        SimplePdfReportConfiguration reportConfig = new SimplePdfReportConfiguration();
        reportConfig.setSizePageToContent(false);
        reportConfig.setForceLineBreakPolicy(false);

        SimplePdfExporterConfiguration exportConfig = new SimplePdfExporterConfiguration();
        exportConfig.setMetadataAuthor(author);
        exportConfig.setMetadataTitle(title);
        exportConfig.setMetadataSubject(subject);

        exporter.setConfiguration(reportConfig);
        exporter.setConfiguration(exportConfig);

        exporter.exportReport();
    }

    public JasperReport compileIssueSummaryReport() throws AppException {
        JasperReport jasperReport;
        String localeSuffix = "";
        String reportSuffix = localeSuffix + ".jrxml";
        String compiledReportSuffix = localeSuffix + ".jasper";

        String summaryReportPath = "/jasperreport/template/issue_summary_report" + reportSuffix;

        String subReportActionAssignmentPath = "/jasperreport/template/sub_action_assignment" + reportSuffix;
        String subReportDefectTypeActionPath = "/jasperreport/template/sub_defect_type_action" + reportSuffix;
        String subReportDirectoryPriorityPath = "/jasperreport/template/sub_directory_priority" + reportSuffix;

        try (InputStream issueSummaryReportStream = getInputStream(summaryReportPath);
             InputStream subReportActionAssignmentStream = getInputStream(subReportActionAssignmentPath);
             InputStream subReportDefectTypeActionStream = getInputStream(subReportDefectTypeActionPath);
             InputStream subReportDirectoryPriorityStream = getInputStream(subReportDirectoryPriorityPath)) {

            jasperReport = JasperCompileManager.compileReport(issueSummaryReportStream);
            JRSaver.saveObject(jasperReport, "issue_summary_report" + compiledReportSuffix);
            JRSaver.saveObject(JasperCompileManager.compileReport(subReportActionAssignmentStream), "sub_action_assignment" + compiledReportSuffix);
            JRSaver.saveObject(JasperCompileManager.compileReport(subReportDefectTypeActionStream), "sub_defect_type_action" + compiledReportSuffix);
            JRSaver.saveObject(JasperCompileManager.compileReport(subReportDirectoryPriorityStream), "sub_directory_priority" + compiledReportSuffix);

        } catch (JRException | IOException e) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_REPORT_COMMON_COMPILE_REPORT_ERROR.unifyErrorCode,
                    CommonUtil.formatString("[{}] message: {}", AppException.ErrorCode.E_API_REPORT_COMMON_COMPILE_REPORT_ERROR.messageTemplate, e.getMessage()), e);
        }
        return jasperReport;
    }

    private byte[] convertChartToSvgByteArray(Chart<? extends Styler, ? extends Series> chart) throws AppException {
        byte[] result;
        try {
            Path chartPath = Files.createTempFile("chart", null);
            try (OutputStream os = Files.newOutputStream(chartPath)) {
                VectorGraphicsEncoder.saveVectorGraphic(chart, os, VectorGraphicsEncoder.VectorGraphicsFormat.SVG);
            }
            log.trace("[chartToSvgByteArray] chartPath: {}", chartPath);
            result = Files.readAllBytes(chartPath);
        } catch (IOException e) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_REPORT_COMMON_GENERATE_REPORT_ERROR.unifyErrorCode,
                    CommonUtil.formatString("[{}] message: {}", AppException.ErrorCode.E_API_REPORT_COMMON_GENERATE_REPORT_ERROR.messageTemplate, e.getMessage()), e);
        }
        return result;
    }

    private CategoryChart generatePriorityChart(ScanTask scanTask, RuleSet ruleSet, Locale locale, List<I18nMessage> i18nMessages) {
        CategoryChart categoryChart = new CategoryChartBuilder()
                .width(220)
                .height(165)
                .build();

        long highPriority = this.issueService.countIssueByPriority(scanTask, ruleSet, RuleInformation.Priority.HIGH);
        long mediumPriority = this.issueService.countIssueByPriority(scanTask, ruleSet, RuleInformation.Priority.MEDIUM);
        long lowPriority = this.issueService.countIssueByPriority(scanTask, ruleSet, RuleInformation.Priority.LOW);

        long highPriorityCritical = this.issueService.countIssueByPriorityAndAction(scanTask, ruleSet, RuleInformation.Priority.HIGH, Issue.Action.CRITICAL);
        long mediumPriorityCritical = this.issueService.countIssueByPriorityAndAction(scanTask, ruleSet, RuleInformation.Priority.MEDIUM, Issue.Action.CRITICAL);
        long lowPriorityCritical = this.issueService.countIssueByPriorityAndAction(scanTask, ruleSet, RuleInformation.Priority.LOW, Issue.Action.CRITICAL);

        ArrayList<String> xData = new ArrayList<>(Arrays.asList(
                I18nService.getMessageByKey("report.issue.priority.HIGH", locale, i18nMessages),
                I18nService.getMessageByKey("report.issue.priority.MEDIUM", locale, i18nMessages),
                I18nService.getMessageByKey("report.issue.priority.LOW", locale, i18nMessages)));

        categoryChart.addSeries("CRITICAL", xData, Arrays.asList(highPriorityCritical, mediumPriorityCritical, lowPriorityCritical))
                .setFillColor(new Color(0xF5333F));
        categoryChart.addSeries("NORMAL", xData, Arrays.asList(highPriority - highPriorityCritical, mediumPriority - mediumPriorityCritical, lowPriority - lowPriorityCritical))
                .setFillColor(new Color(0xDDDDDD));

        CategoryStyler styler = categoryChart.getStyler();
        styler.setChartBackgroundColor(Color.WHITE);

        String fontFace = "Barlow Semi Condensed";
        if (Locale.SIMPLIFIED_CHINESE == locale) {
            fontFace = "Source Han Sans CN";
        }
        styler.setBaseFont(new Font(fontFace, Font.BOLD, 12));
        styler.setStacked(true);
        styler.setOverlapped(false);
        styler.setPlotBorderVisible(false);
        styler.setPlotMargin(0);
        styler.setPlotGridHorizontalLinesVisible(false);
        styler.setPlotGridVerticalLinesVisible(false);
        styler.setAxisTickLabelsFont(new Font(fontFace, Font.PLAIN, 8));
        styler.setYAxisMax(NumberUtils.max(highPriority, mediumPriority, lowPriority) * 1.1);
        return categoryChart;
    }

    private PieChart generateActionChart(ScanTask scanTask, RuleSet ruleSet, Locale locale, final Map<String, Object> paramMap) {
        PieChart pieChart = new PieChartBuilder()
                .width(500)
                .height(500)
                .build();

        long pending = this.issueService.countIssueByAction(scanTask, ruleSet, Issue.Action.PENDING);
        long open = this.issueService.countIssueByAction(scanTask, ruleSet, Issue.Action.OPEN);
        long confirmed = this.issueService.countIssueByAction(scanTask, ruleSet, Issue.Action.CONFIRMED);
        long falsePositive = this.issueService.countIssueByAction(scanTask, ruleSet, Issue.Action.FALSE_POSITIVE);
        long waived = this.issueService.countIssueByAction(scanTask, ruleSet, Issue.Action.WAIVED);
        long critical = this.issueService.countIssueByAction(scanTask, ruleSet, Issue.Action.CRITICAL);
        long total = this.issueService.countIssueByAction(scanTask, ruleSet, Issue.Action.values());

        pieChart.addSeries("PENDING", pending).setFillColor(new Color(0xDDDDDD));
        pieChart.addSeries("OPEN", open).setFillColor(new Color(0xDDDDDD));
        pieChart.addSeries("WAIVED", waived).setFillColor(new Color(0x777876));
        pieChart.addSeries("FALSE_POSITIVE", falsePositive).setFillColor(new Color(0x00AEE6));
        pieChart.addSeries("CONFIRMED", confirmed).setFillColor(new Color(0xFF8300));
        pieChart.addSeries("CRITICAL", critical).setFillColor(new Color(0xF5333F));

        paramMap.put("ACTION_COUNT_PENDING", pending);
        paramMap.put("ACTION_COUNT_OPEN", open);
        paramMap.put("ACTION_COUNT_WAIVED", waived);
        paramMap.put("ACTION_COUNT_FALSE_POSITIVE", falsePositive);
        paramMap.put("ACTION_COUNT_CONFIRMED", confirmed);
        paramMap.put("ACTION_COUNT_CRITICAL", critical);
        paramMap.put("ACTION_COUNT_TOTAL", total);

        PieStyler styler = pieChart.getStyler();
        styler.setChartBackgroundColor(Color.WHITE);
        String fontFace = "Barlow Semi Condensed";
        if (Locale.SIMPLIFIED_CHINESE == locale) {
            fontFace = "Source Han Sans CN";
        }
        styler.setChartTitleFont(new Font(fontFace, Font.BOLD, 12));
        styler.setPlotBorderVisible(false);
        styler.setDefaultSeriesRenderStyle(PieSeries.PieSeriesRenderStyle.Donut);
        styler.setHasAnnotations(false);
        styler.setDonutThickness(0.55);
        return pieChart;
    }

    private PieChart generateVulnerableChart(ScanTask scanTask, RuleSet ruleSet, Locale locale, final Map<String, Object> paramMap) {
        PieChart pieChart = new PieChartBuilder()
                .width(500)
                .height(500)
                .build();

        Map<String, Long> valueMap = new HashMap<>();
        Map<String, List<RuleInformation>> ruleInformationMap = this.ruleService.findByRuleSet(ruleSet).stream().collect(Collectors.groupingBy(RuleInformation::getVulnerable));

        for (String vulnerable : ruleInformationMap.keySet()) {
            valueMap.put(vulnerable, this.issueService.countIssueByRuleInformation(scanTask, ruleInformationMap.get(vulnerable)));
        }
        Comparator<Map.Entry<String, Long>> comparator = Comparator.comparingLong(Map.Entry::getValue);
        List<Map.Entry<String, Long>> entries = valueMap.entrySet().stream().sorted(comparator.reversed()).collect(Collectors.toList());
        Long others = 0L;
        List<String> vulnerableTop10 = new ArrayList<>();
        Map<String, Long> vulnerableTop10CountMap = new HashMap<>();
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<String, Long> entry = entries.get(i);
            if (i < 9 && entry.getValue() > 0) {
                pieChart.addSeries(entry.getKey(), entry.getValue());
                vulnerableTop10.add(entry.getKey());
                vulnerableTop10CountMap.put(entry.getKey(), entry.getValue());
            } else {
                others += entry.getValue();
            }
        }
        if (others > 0) {
            pieChart.addSeries("Others", others);
            vulnerableTop10.add("Others");
            vulnerableTop10CountMap.put("Others", others);
        }
        paramMap.put("VUL_TOP_10", vulnerableTop10);
        paramMap.put("VUL_TOP_10_VALUE", vulnerableTop10CountMap);

        PieStyler styler = pieChart.getStyler();
        styler.setChartBackgroundColor(Color.WHITE);
        String fontFace = "Barlow Semi Condensed";
        if (Locale.SIMPLIFIED_CHINESE == locale) {
            fontFace = "Source Han Sans CN";
        }
        styler.setChartTitleFont(new Font(fontFace, Font.BOLD, 12));
        styler.setPlotBorderVisible(false);
        styler.setDefaultSeriesRenderStyle(PieSeries.PieSeriesRenderStyle.Donut);
        styler.setHasAnnotations(false);
        styler.setLegendVisible(false);
        styler.setDonutThickness(0.55);
        styler.setSeriesColors(new Color[]{
                new Color(0xE71D34),
                new Color(0xF06E19),
                new Color(0x129DE0),
                new Color(0x7CBF31),
                new Color(0x14993F),
                new Color(0x808486),
                new Color(0x1A60AD),
                new Color(0xBABCBF),
                new Color(0xE50079),
                new Color(0x7D0F7D)
        });
        return pieChart;
    }

    public Resource generateExternalIssueCsvReport(Project project, ScanTask scanTask, ExternalCsvReportRequest externalCsvReportRequest, String reportType, boolean deltaReport, Integer timezoneInMins, Locale locale) throws AppException {
        log.debug("[generateExternalIssueCsvReport] project:{}, scanTask:{}, externalCsvReportRequest:{}, reportType:{}, timezoneInMins:{}, timezoneInMins:{}, locale:{}", project.getId(), scanTask.getId(),externalCsvReportRequest,reportType,deltaReport,timezoneInMins,locale);
        Map<String, List<String>> ruleSetStandardCsvCodeMap = new HashMap<>(); // key as rule set or standard, value as list of csv code


        RuleListResponseDto ruleListResponseDto = ruleServiceV3.getAllRuleInfo(Locale.ENGLISH);
        List<RuleInfoDto> ruleInfoDtoList = ruleListResponseDto.getRuleInfoDtoList();

        // prepare for rule set
        prepareCodesForRuleSet(ruleSetStandardCsvCodeMap, ruleInfoDtoList);

        // prepare for rule standard
        prepareCodesForRuleStandard(ruleSetStandardCsvCodeMap, ruleInfoDtoList);

        // prepare rule code csv code map
        Map<String, List<String>> ruleCodeCsvCodeMap = new HashMap<>(); // key as rule set or standard, value as csv code
        prepareCodesForRuleCode(ruleInfoDtoList, ruleCodeCsvCodeMap);

        // get csv code over rule set and rule standard filter
        Set<String> csvCodeFromRuleSetStandard = new HashSet<>();
        if(externalCsvReportRequest.getRuleSetAndStandardNames()!=null) {
            for (String ruleSetStandardFilter : externalCsvReportRequest.getRuleSetAndStandardNames()) {
                if (ruleSetStandardCsvCodeMap.containsKey(ruleSetStandardFilter.toUpperCase())) {
                    List<String> csvCodeList = ruleSetStandardCsvCodeMap.get(ruleSetStandardFilter.toUpperCase());
                    csvCodeFromRuleSetStandard.addAll(csvCodeList);
                }
            }
        }else{
            //for all rule set and standard
            for(Map.Entry<String, List<String>> csvCodeMapEntry:ruleSetStandardCsvCodeMap.entrySet()){
                csvCodeFromRuleSetStandard.addAll(csvCodeMapEntry.getValue());
            }
        }
        log.debug("[generateExternalIssueCsvReport] csvCodeFromRuleSetStandard:{}", csvCodeFromRuleSetStandard);

        // get csv code over defect types(rule code)
        Set<String> csvCodeFromRuleFilter = new HashSet<>();
        if(externalCsvReportRequest.getDefectTypes()!=null) {
            for (String defectType : externalCsvReportRequest.getDefectTypes()) {
                if (ruleCodeCsvCodeMap.containsKey(defectType.toUpperCase())) {
                    List<String> csvCodeList = ruleCodeCsvCodeMap.get(defectType.toUpperCase());
                    csvCodeFromRuleFilter.addAll(csvCodeList);
                }
            }
        }else{
            //for all rule code
            for(Map.Entry<String, List<String>> csvCodeMapEntry:ruleCodeCsvCodeMap.entrySet()){
                csvCodeFromRuleFilter.addAll(csvCodeMapEntry.getValue());
            }
        }

        log.debug("[generateExternalIssueCsvReport] csvCodeFromRuleFilter:{}", csvCodeFromRuleFilter);

        // get final csv code filter
        Set<String> finalCsvCodeFilter;
        if (externalCsvReportRequest.getRuleSetAndStandardNames() == null && externalCsvReportRequest.getDefectTypes() == null) {
            finalCsvCodeFilter = csvCodeFromRuleFilter;
            finalCsvCodeFilter.addAll(csvCodeFromRuleSetStandard);
        } else if (externalCsvReportRequest.getRuleSetAndStandardNames() == null) {
            finalCsvCodeFilter = csvCodeFromRuleFilter;
        } else if (externalCsvReportRequest.getDefectTypes() == null) {
            finalCsvCodeFilter = csvCodeFromRuleSetStandard;
        } else {
            finalCsvCodeFilter = csvCodeFromRuleFilter;
            finalCsvCodeFilter.retainAll(csvCodeFromRuleSetStandard);
        }

        log.debug("[generateExternalIssueCsvReport] finalCsvCodeFilter:{}", finalCsvCodeFilter);

        // get filter with Criticality
        List<SearchIssueGroupRequest.RuleCode> ruleCodeFilterList = null;
        if(finalCsvCodeFilter!=null) {
            ruleCodeFilterList = new ArrayList<>();
            if (externalCsvReportRequest.getCriticalities() != null) {
                for (String criticality : externalCsvReportRequest.getCriticalities()) {
                    for (String csvCode : finalCsvCodeFilter) {
                        ruleCodeFilterList.add(
                                SearchIssueGroupRequest.RuleCode.builder()
                                        .csvCode(csvCode)
                                        .criticality(criticality)
                                        .build());
                    }
                }
            } else {
                for (String csvCode : finalCsvCodeFilter) {
                    ruleCodeFilterList.add(
                            SearchIssueGroupRequest.RuleCode.builder()
                                    .csvCode(csvCode)
                                    .criticality(null)
                                    .build());
                }
            }
        }

        log.debug("[generateExternalIssueCsvReport] ruleCodeFilterList:{}", ruleCodeFilterList);

        if(ruleCodeFilterList.isEmpty()){
            ruleCodeFilterList.add(SearchIssueGroupRequest.RuleCode.builder().csvCode("NO_MATCH").criticality("").build());
        }

        ReportRequest reportRequest = ReportRequest.builder()
                .projectId(project.getId())
                .scanTaskId(scanTask.getId())
                .ruleCodes(ruleCodeFilterList) //TODO:
                .ruleSets(null) //TODO:
                .scanFileIds(null)
                .pathCategory(null)
                .certainty(null)
                .dsrType(externalCsvReportRequest.getDsrType())
                .issueGroupId(null)
                .criticality(null)
                .searchValue(externalCsvReportRequest.getSearchValue())
                .ruleSetAndStandardNames(externalCsvReportRequest.getRuleSetAndStandardNames())
                .build();

        boolean needDefectFilter = externalCsvReportRequest.getDefectTypes()!=null;

        return generateIssueCsvReport(scanTask, reportRequest, reportType, deltaReport, timezoneInMins, needDefectFilter, locale);
    }

    private void prepareCodesForRuleCode(List<RuleInfoDto> ruleInfoDtoList, Map<String, List<String>> ruleCodeCsvCodeMap) {
        for (RuleInfoDto ruleInfoDto : ruleInfoDtoList) { // for each rule

            if (ruleInfoDto.getCodes() == null) {
                log.warn("[generateExternalIssueCsvReport] preparing map, rule code is null for {}", ruleInfoDto);
                continue;
            }

            if(!ruleCodeCsvCodeMap.containsKey(ruleInfoDto.getRuleCode().toUpperCase())){
                ruleCodeCsvCodeMap.put(ruleInfoDto.getRuleCode().toUpperCase(), new ArrayList<>());
            }

            for (String csvCode : ruleInfoDto.getCodes()) {
                List<String> csvCodeList= ruleCodeCsvCodeMap.get(ruleInfoDto.getRuleCode().toUpperCase());
                csvCodeList.add(csvCode);
            }
        }
        log.debug("[generateExternalIssueCsvReport] ruleCodeCsvCodeMap:{}", ruleCodeCsvCodeMap);
    }

    private void prepareCodesForRuleStandard(Map<String, List<String>> ruleSetStandardCsvCodeMap, List<RuleInfoDto> ruleInfoDtoList) {
        for (RuleInfoDto ruleInfoDto : ruleInfoDtoList) { // for each rule
            if (ruleInfoDto.getStandard() == null) {
                log.warn("[prepareCodesForRuleStandard] preparing map, rule standard is null for {}", ruleInfoDto);
                continue;
            }

            if (ruleInfoDto.getCodes() == null) {
                log.warn("[prepareCodesForRuleStandard] preparing map, rule code is null for {}", ruleInfoDto);
                continue;
            }

            for (String ruleStandard : ruleInfoDto.getStandard().keySet()) {

                if(!ruleSetStandardCsvCodeMap.containsKey(ruleStandard.toUpperCase())){
                    ruleSetStandardCsvCodeMap.put(ruleStandard.toUpperCase(), new ArrayList<>());
                }

                List<String> csvCodeList = ruleSetStandardCsvCodeMap.get(ruleStandard.toUpperCase());
                for (String csvCode : ruleInfoDto.getCodes()) {
                    csvCodeList.add(csvCode);
                }
            }
        }
        log.debug("[prepareCodesForRuleStandard] ruleSetStandardCsvCodeMap:{}", ruleSetStandardCsvCodeMap);
    }

    private void prepareCodesForRuleSet(Map<String, List<String>> ruleSetStandardCsvCodeMap, List<RuleInfoDto> ruleInfoDtoList) {
        for (RuleInfoDto ruleInfoDto : ruleInfoDtoList) { // for each rule
            if (ruleInfoDto.getRuleSet() == null) {
                log.warn("[prepareCodesForRuleSet] preparing map, rule set is null for {}", ruleInfoDto);
                continue;
            }
            if (ruleInfoDto.getCodes() == null) {
                log.warn("[prepareCodesForRuleSet] preparing map, rule code is null for {}", ruleInfoDto);
                continue;
            }

            // put if not exist
            if(!ruleSetStandardCsvCodeMap.containsKey(ruleInfoDto.getRuleSet().getDisplayName().toUpperCase())){
                ruleSetStandardCsvCodeMap.put(ruleInfoDto.getRuleSet().getDisplayName().toUpperCase(), new ArrayList<>());
            }

            // add to list
            List<String> csvCodeList = ruleSetStandardCsvCodeMap.get(ruleInfoDto.getRuleSet().getDisplayName().toUpperCase());
            for (String csvCode : ruleInfoDto.getCodes()) {
                csvCodeList.add(csvCode);
            }

        }
        log.debug("[prepareCodesForRuleSet] ruleSetStandardCsvCodeMap:{}", ruleSetStandardCsvCodeMap);
    }

    public Resource generateIssueCsvReport(ScanTask scanTask, ReportRequest reportRequest, String reportType, boolean deltaReport, Integer timezoneInMins, boolean needDefectFilter, Locale locale) throws AppException {

        log.info("[generateIssueCsvReport] scanTask: {}, ruleSetId: {}, locale: {}", scanTask, reportRequest.getRuleSets(), locale);
        try {
            String scanModeParam = scanTask.getProjectConfig().getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.SCAN_MODE, "");
            String scanMode = VariableUtil.ScanMode.getEnumByParamValue(scanModeParam).name();
            //validate report type
            validateReportType(reportType, scanMode);

            Path tempDir = Files.createTempDirectory(scanTask.getId().toString());
            String dateString = DateTimeFormatter.ofPattern(DATE_FORMAT).format(LocalDate.now());
            String reportName = scanTask.getProject().getName() + "_defect_report_" + dateString + ".csv";
            reportName = reportName.replaceAll("/", "_");
            reportName = reportName.replaceAll(" ", "_");
            Path tempFile = Files.createFile(Paths.get(tempDir.toString(), reportName));

            SimpleDateFormat formatter = new SimpleDateFormat(DATETIME_FORMAT);


            try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                writer.write(ByteOrderMark.UTF_BOM); // BOM for UTF-*
                List<I18nMessage> i18nMessages = this.i18nService.getI18nMessageByKeyPrefix("report.detail.csv.header", locale);
                List<String> headerList = new ArrayList<>();
                if (deltaReport) {
                    headerList.add("Status");
                }
                headerList.add("ID");
                headerList.add("Type");
                headerList.add("Description");
                if (!reportType.equals(VariableUtil.ReportType.MISRA.name())) {//not misra
                    headerList.add("Certainty");
                    headerList.add("Rule & Standard");
                }
                headerList.add("File");
                headerList.add("Line");
                headerList.add("Function");
                if (!reportType.equals(VariableUtil.ReportType.MISRA.name())) {//not misra
                    headerList.add("Variable");
                }
                headerList.add("Paths");
                if (!reportType.equals(VariableUtil.ReportType.MISRA.name())) {//not misra
                    headerList.add("Risk");
                    headerList.add("Severity");
                    headerList.add("Likelihood");
                    headerList.add("Remediation Cost");
                    headerList.add("Complexity");
                } else {
                    headerList.add("Obligation Level");
                }
                if (deltaReport) {
                    headerList.add("First Detected");
                    headerList.add("First Fixed");
                }
                headerList.add("Assignee");
                headerList.add("URL");
                String[] headers = headerList.toArray(new String[0]);

                Date scanEndDate = DateUtil.dateToTimeZone(scanTask.getScanEndAt(), timezoneInMins);
                String scanEndAt = Optional.ofNullable(scanEndDate).map(formatter::format).orElse("N/A");

                List<String> scanFilePaths = getScanFilePathListFilter(reportRequest);

                String displayScanMode = reportType;

                //project criticality
                String projectCriticality = measureService.getProjectCriticality(reportRequest, scanFilePaths);

                //commitId
                String commitId = scanTask.getProjectConfig().getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.COMMIT_ID, null);

                //baselineCommitId
                String baselineCommitId = scanTask.getProjectConfig().getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.BASELINE_COMMIT_ID, null);

                //compute and make certainty count
                Map<String, String> certaintyCountMap = measureService.getCertaintyCountMap(reportRequest, scanFilePaths);
                //compute and make criticality count
                //Get criticality count
                ReportPDFResponse.IssueCountGroupByCriticality groupByCriticality = measureService.getIssueCountGroupByCriticality(reportRequest, scanFilePaths);
                Integer totalDefectCount = groupByCriticality.getHigh() + groupByCriticality.getMedium() + groupByCriticality.getLow();

                CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);

                printer.printRecord(new String[]{"Project name", scanTask.getProject().getName()});
                printer.printRecord(new String[]{"Scan finished at", scanEndAt});
                printer.printRecord(new String[]{"Scan mode", displayScanMode});
                if (deltaReport) {
                    if (commitId != null) {
                        printer.printRecord(new String[]{"Commit ID", commitId});
                    }
                    if (baselineCommitId != null) {
                        printer.printRecord(new String[]{"Baseline commit ID", baselineCommitId});
                    }
                }
                printer.printRecord(new String[]{"Total defects", totalDefectCount.toString()});
                if (!reportType.equals(VariableUtil.ReportType.MISRA.name())) {//not misra
                    printer.printRecord(new String[]{"Definite defects", Optional.ofNullable(certaintyCountMap.get("D")).orElse("0")});
                    printer.printRecord(new String[]{"Possible defects", Optional.ofNullable(certaintyCountMap.get("M")).orElse("0")});
                }

                if (!reportType.equals(VariableUtil.ReportType.MISRA.name())) {//not misra
                    printer.printRecord(new String[]{"Project risk level", projectCriticality});
                    printer.printRecord(new String[]{"High risk defects", groupByCriticality.getHigh().toString()});
                    printer.printRecord(new String[]{"Medium risk defects", groupByCriticality.getMedium().toString()});
                    printer.printRecord(new String[]{"Low risk defects", groupByCriticality.getLow().toString()});
                } else {
                    printer.printRecord(new String[]{"Mandatory to fix defects", groupByCriticality.getHigh().toString()});
                    printer.printRecord(new String[]{"Required to fix defects", groupByCriticality.getMedium().toString()});
                    printer.printRecord(new String[]{"Advisory to fix defects", groupByCriticality.getLow().toString()});
                }


                Map<String, RuleInfoDto> ruleInfoMap = new HashMap<>();
                RuleListResponseDto ruleListResponseDto = this.ruleServiceV3.getAllRuleInfo(Locale.ENGLISH);

                List<RuleInfoDto> ruleInfos = ruleListResponseDto.getRuleInfoDtoList();
                if (ruleInfos != null) {
                    for (RuleInfoDto ruleInfo : ruleInfos) {
                        if (ruleInfo.getCodes() != null) {
                            for (String code : ruleInfo.getCodes()) {
                                ruleInfoMap.put(code, ruleInfo);
                            }
                        }
                    }
                }

                if (filterIsNotEmpty(reportRequest, reportType, needDefectFilter)) { //filter not empty
                    printer.printRecord(new String[]{"Filter conditions"});

                    if (!StringUtils.isBlank(reportRequest.getSearchValue())) {
                        printer.printRecord(new String[]{"Search terms", reportRequest.getSearchValue()});
                    }

                    if (!reportType.equals(VariableUtil.ReportType.MISRA.name()) && reportRequest.getRuleSetAndStandardNames() != null && !reportRequest.getRuleSetAndStandardNames().isEmpty()) { //non-misra
                        printer.printRecord(new String[]{"Rules & Standards", String.join(VALUE_DELIMITER, reportRequest.getRuleSetAndStandardNames())});
                    } else { //misra, do not have rule set filter
                    }

                    //Defect type
                    if (needDefectFilter && reportRequest.getRuleCodes() != null) {
                        Set<String> ruleFilterSet = new HashSet<>();
                        for (SearchIssueGroupRequest.RuleCode ruleCode : reportRequest.getRuleCodes()) {
                            RuleInfoDto ruleInfoDto = ruleInfoMap.get(ruleCode.getCsvCode());
                            if (ruleInfoDto != null) {
                                ruleFilterSet.add(ruleInfoDto.getRuleCode());
                            }
                        }
                        if (!ruleFilterSet.isEmpty()) {
                            printer.printRecord(new String[]{"Defect types", String.join(VALUE_DELIMITER, ruleFilterSet)});
                        }
                    }
                }

                long newCount = issueGroupMapper.getIssueGroupCount(
                        reportRequest.getProjectId(),
                        reportRequest.getScanTaskId(),
                        reportRequest.getRuleCodes(),
                        reportRequest.getRuleSets(),
                        scanFilePaths,
                        reportRequest.getPathCategory(),
                        reportRequest.getCertainty(),
                        Arrays.asList("N"),
                        reportRequest.getCriticality(),
                        reportRequest.getValidationAction(),
                        StringUtil.splitAndTrim(reportRequest.getSearchValue(),SEARCH_VAL_DELIMITER)
                );

                long fixedCount = issueGroupMapper.getIssueGroupCount(
                        reportRequest.getProjectId(),
                        reportRequest.getScanTaskId(),
                        reportRequest.getRuleCodes(),
                        reportRequest.getRuleSets(),
                        scanFilePaths,
                        reportRequest.getPathCategory(),
                        reportRequest.getCertainty(),
                        Arrays.asList("F"),
                        reportRequest.getCriticality(),
                        reportRequest.getValidationAction(),
                        StringUtil.splitAndTrim(reportRequest.getSearchValue(),SEARCH_VAL_DELIMITER)
                );

                if (deltaReport) {
                    printer.printRecord(new String[]{"New defects", String.valueOf(newCount)});
                    printer.printRecord(new String[]{"Fixed defects", String.valueOf(fixedCount)});
                }

                // separator
                printer.printRecord(new String[]{});
                printer.printRecord(new String[]{});

                //print header
                printer.printRecord(headers);


                if (reportRequest.getIssueGroupId() != null) {
                    //Filter by issue group id
                    try {
                        Optional<IssueGroup> issueGroupOptional = issueGroupDao.getIssueGroup(scanTask.getId(), reportRequest.getIssueGroupId());
                        List<IssueGroup> issueGroups = new ArrayList<>();
                        issueGroupOptional.ifPresent(issueGroup -> issueGroups.add(issueGroup));
                        batchPrintCsvRecords(null, scanTask, timezoneInMins, printer, ruleInfoMap, formatter, reportType, deltaReport, 0, issueGroups);
                    } catch (Exception ignored) {

                    }

                } else {
                    //other filter with batch

                    //write new issue groups
                    String status = "NEW";
                    String dsrFilter = "N";
                    if (reportRequest.getDsrType() == null || reportRequest.getDsrType().contains(dsrFilter)) {
                        List<String> dsrFilterList = Arrays.asList(dsrFilter);
                        queryAndWriteToCSV(scanTask, reportRequest, reportType, deltaReport, timezoneInMins, formatter, scanFilePaths, printer, ruleInfoMap, status, dsrFilterList);
                    }

                    //write fixed issue groups
                    if (deltaReport) {
                        status = "FIXED";
                        dsrFilter = "F";
                        if (reportRequest.getDsrType() == null || reportRequest.getDsrType().contains(dsrFilter)) {
                            List<String> dsrFilterList = Arrays.asList(dsrFilter);
                            queryAndWriteToCSV(scanTask, reportRequest, reportType, deltaReport, timezoneInMins, formatter, scanFilePaths, printer, ruleInfoMap, status, dsrFilterList);
                        }
                    }

                    //write outstanding issue groups
                    status = "OUTSTANDING";
                    dsrFilter = "E";
                    if (reportRequest.getDsrType() == null || reportRequest.getDsrType().contains(dsrFilter)) {
                        List<String> dsrFilterList = Arrays.asList(dsrFilter);
                        queryAndWriteToCSV(scanTask, reportRequest, reportType, deltaReport, timezoneInMins, formatter, scanFilePaths, printer, ruleInfoMap, status, dsrFilterList);
                    }

                }//end if

                printer.close();
            }
            return new UrlResource(tempFile.toUri());
        } catch (IOException e) {
            throw new AppException(
                    AppException.LEVEL_ERROR,
                    AppException.ERROR_CODE_INTERNAL_ERROR,
                    HttpURLConnection.HTTP_INTERNAL_ERROR,
                    AppException.ErrorCode.E_API_REPORT_COMMON_GENERATE_REPORT_ERROR.unifyErrorCode,
                    CommonUtil.formatString(
                            "[{}] message: {}",
                            AppException.ErrorCode.E_API_REPORT_COMMON_GENERATE_REPORT_ERROR.messageTemplate,
                            e.getMessage()
                    ),
                    e
            );
        }
    }

    private boolean filterIsNotEmpty(ReportRequest searchIssueGroupRequest, String reportType, boolean needDefectFilter) {
        return !StringUtils.isBlank(searchIssueGroupRequest.getSearchValue()) ||
                (!reportType.equals(VariableUtil.ReportType.MISRA.name()) && searchIssueGroupRequest.getRuleSetAndStandardNames() != null && !searchIssueGroupRequest.getRuleSetAndStandardNames().isEmpty()) ||
                (needDefectFilter && searchIssueGroupRequest.getRuleCodes() != null && !searchIssueGroupRequest.getRuleCodes().isEmpty());
    }

    private void queryAndWriteToCSV(ScanTask scanTask, SearchIssueGroupRequest searchIssueGroupRequest, String reportType, boolean deltaReport, Integer timezoneInMins, SimpleDateFormat formatter, List<String> scanFilePaths, CSVPrinter printer, Map<String, RuleInfoDto> ruleInfoMap, String status, List<String> dsrFilter) throws IOException {
        log.info("[queryAndWriteToCSV] scanTask:{} reportType:{}, deltaReport:{}, status:{}", scanTask, reportType, deltaReport, status);
        int offset = 0;
        while (true) {

            List<IssueGroup> issueGroups = this.issueGroupDao.getIssueGroupList(
                    searchIssueGroupRequest.getProjectId(),
                    searchIssueGroupRequest.getScanTaskId(),
                    searchIssueGroupRequest.getRuleCodes(),
                    searchIssueGroupRequest.getRuleSets(),
                    scanFilePaths,
                    searchIssueGroupRequest.getPathCategory(),
                    searchIssueGroupRequest.getCertainty(),
                    dsrFilter,
                    searchIssueGroupRequest.getCriticality(),
                    searchIssueGroupRequest.getValidationAction(),
                    searchIssueGroupRequest.getSearchValue(),
                    offset,
                    DEFAULT_PAGE_SIZE
            );

            if (issueGroups.isEmpty()) {
                break;
            }
            offset = batchPrintCsvRecords(status, scanTask, timezoneInMins, printer, ruleInfoMap, formatter, reportType, deltaReport, offset, issueGroups);
        }//end while loop
    }

    void validateReportType(String reportType, String scanMode) throws AppException {
        log.info("[validateReportType] report type:{}, scanMode:{}", reportType, scanMode);
        if (reportType.equals(VariableUtil.ReportType.SINGLE.name())) {
            if (Arrays.asList(VariableUtil.ScanMode.SINGLE.name(), VariableUtil.ScanMode.SINGLE_XSCA.name()).stream().noneMatch(allowed -> allowed.equals(scanMode))) { //not single /single-xsca -> error
                //invalid reportType
                log.warn("[validateReportType] Unsupported report type:{}, scanMode:{}", reportType, scanMode);
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST,
                        HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_REPORT_INVALID_REPORT_TYPE.unifyErrorCode, AppException.ErrorCode.E_API_REPORT_INVALID_REPORT_TYPE.messageTemplate);
            }
        } else if (reportType.equals(VariableUtil.ReportType.CROSS.name())) { //not cross -> error
            if (Arrays.asList(VariableUtil.ScanMode.CROSS.name()).stream().noneMatch(allowed -> allowed.equals(scanMode))) { //not single /single-xsca -> error
                //invalid reportType
                log.warn("[validateReportType] Unsupported report type:{}, scanMode:{}", reportType, scanMode);
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST,
                        HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_REPORT_INVALID_REPORT_TYPE.unifyErrorCode, AppException.ErrorCode.E_API_REPORT_INVALID_REPORT_TYPE.messageTemplate);
            }
        } else if (reportType.equals(VariableUtil.ReportType.MISRA.name())) { //not xsca/signel-xsca ->error
            if (Arrays.asList(VariableUtil.ScanMode.XSCA.name(), VariableUtil.ScanMode.SINGLE_XSCA.name()).stream().noneMatch(allowed -> allowed.equals(scanMode))) { //not single /single-xsca -> error
                //invalid reportType
                log.warn("[validateReportType] Unsupported report type:{}, scanMode:{}", reportType, scanMode);
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST,
                        HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_REPORT_INVALID_REPORT_TYPE.unifyErrorCode, AppException.ErrorCode.E_API_REPORT_INVALID_REPORT_TYPE.messageTemplate);
            }
        } else {
            log.warn("[validateReportType] Unsupported report type:{}, scanMode:{}", reportType, scanMode);
            //Unsupported mode
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST,
                    HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_REPORT_INVALID_REPORT_TYPE.unifyErrorCode, AppException.ErrorCode.E_API_REPORT_INVALID_REPORT_TYPE.messageTemplate);
        }
    }


    private List<String> getScanFilePathListFilter(SearchIssueGroupRequest searchIssueGroupRequest) {
        List<String> scanFilePaths = null;
        if ((searchIssueGroupRequest.getScanFileIds() != null) && !searchIssueGroupRequest.getScanFileIds().isEmpty()) {
            List<ScanFile> scanFiles = this.scanFileService.findByScanFileIds(searchIssueGroupRequest.getScanFileIds());
            if ((scanFiles != null) && !scanFiles.isEmpty()) {
                scanFilePaths = scanFiles.stream().map(ScanFile::getProjectRelativePath).collect(Collectors.toList());
            }
        }
        return scanFilePaths;
    }

    private int batchPrintCsvRecords(String status, ScanTask scanTask, Integer timezoneInMins, CSVPrinter printer, Map<String, RuleInfoDto> ruleInfoMap, SimpleDateFormat formatter, String reportType, boolean deltaReport, int offset, List<IssueGroup> issueGroups) throws IOException {
        for (IssueGroup issueGroup : issueGroups) {
            RuleInfoDto ruleInfo = ruleInfoMap.getOrDefault(issueGroup.getRuleCode(), null);
            String filePath = Optional.ofNullable(issueGroup.getSinkFilePath()).orElse(issueGroup.getSrcFilePath());


            String certainty = null;
            if (issueGroup.getCertainty() != null && issueGroup.getCertainty().equals("M")) {
                certainty = "Possible";
            } else if (issueGroup.getCertainty() != null && issueGroup.getCertainty().equals("D")) {
                certainty = "Definite";
            } else {
                log.warn("[generateIssueCsvReport] unexpected certainty value for issueGroup: {} ", issueGroup);
            }

            List<String> ruleSetStandardList = new ArrayList();
            ruleSetStandardList.add(Optional.ofNullable(ruleInfo).map(RuleInfoDto::getRuleSet).map(RuleInfoDto.RuleSetDto::getDisplayName).orElse(issueGroup.getRuleSet()));
            if (ruleInfo != null && ruleInfo.getStandard() != null && ruleInfo.getStandard().keySet() != null) {
                for (String standardCode : ruleInfo.getStandard().keySet()) {
                    ruleSetStandardList.add(standardCode.toUpperCase());
                }
            }

            String url = CommonUtil.formatString("{}:{}/project/{}/scan/{}/issue/{}",
                    apiHost,
                    apiPort,
                    scanTask.getProject().getProjectId(),
                    scanTask.getId(),
                    issueGroup.getId());

            Date occurTime = DateUtil.dateToTimeZone(issueGroup.getOccurTime(), timezoneInMins);
            Date fixedTime = DateUtil.dateToTimeZone(issueGroup.getFixedTime(), timezoneInMins);

            List<Object> lineList = new ArrayList<>();
            if (deltaReport) {
                lineList.add(status);
            }
            lineList.add(issueGroup.getId());
            lineList.add(Optional.ofNullable(ruleInfo).map(RuleInfoDto::getRuleCode).orElse(""));
            lineList.add(Optional.ofNullable(ruleInfo).map(RuleInfoDto::getName).orElse(""));
            if (!reportType.equals(VariableUtil.ReportType.MISRA.name())) {
                lineList.add(certainty);
                lineList.add(ruleSetStandardList.stream().collect(Collectors.joining(",")));
            }
            lineList.add(Optional.ofNullable(filePath).map(s -> s.replaceAll("^(\\$[ht])?/", "")).orElse(""));
            lineList.add((issueGroup.getSinkLineNo() != 0) ? issueGroup.getSinkLineNo() : issueGroup.getSrcLineNo());
            lineList.add(Optional.ofNullable(issueGroup.getFunctionName()).orElse(""));
            if (!reportType.equals(VariableUtil.ReportType.MISRA.name())) {
                lineList.add(Optional.ofNullable(issueGroup.getVariableName()).orElse(""));
            }
            lineList.add(issueGroup.getIssueCount());
            if (!reportType.equals(VariableUtil.ReportType.MISRA.name())) {
                lineList.add(issueGroup.getCriticalityLevel());
                lineList.add(issueGroup.getSeverity());
                lineList.add(issueGroup.getLikelihood());
                lineList.add(issueGroup.getRemediationCost());
                lineList.add(issueGroup.getComplexity());
            } else {
                //MISRA
                lineList.add(Issue.ObligationLevel.getByIntValue(issueGroup.getCriticality()).name());
            }
            if (deltaReport) {
                lineList.add(Optional.ofNullable(occurTime).map(formatter::format).orElse("N/A"));
                lineList.add(Optional.ofNullable(fixedTime).map(formatter::format).orElse("N/A"));
            }
            lineList.add(Optional.ofNullable(issueGroup.getAssigneeDisplayName()).orElse("Unassigned"));
            lineList.add(url);
            Object[] line = lineList.toArray(new Object[0]);

            printer.printRecord(line);
        }//end for each issue group
        offset += issueGroups.size();
        printer.flush();
        return offset;
    }

    public ReportPDFResponse getPDFReport(SearchIssueGroupRequest searchIssueGroupRequest) throws AppException {
        /*
         * 1. Get project summary
         * 2. Get Issue Count Group By Risk
         * 3. Defects Assigned
         * 4.
         * */

        List<String> scanFilePathListFilter = getScanFilePathListFilter(searchIssueGroupRequest);
        //Get criticality count
        List<IssueGroupCountRow> groupByCriticalityList = issueGroupDao.getIssueGroupCriticalityCount(
                IssueGroupDao.FILTER_CATEGORY_CRITICALITY,
                searchIssueGroupRequest.getProjectId(),
                searchIssueGroupRequest.getScanTaskId(),
                searchIssueGroupRequest.getRuleCodes(),
                searchIssueGroupRequest.getRuleSets(),
                scanFilePathListFilter,
                searchIssueGroupRequest.getPathCategory(),
                searchIssueGroupRequest.getCertainty(),
                searchIssueGroupRequest.getDsrType(),
                searchIssueGroupRequest.getCriticality(),
                null,
                searchIssueGroupRequest.getValidationAction(),
                searchIssueGroupRequest.getSearchValue());
        ReportPDFResponse.IssueCountGroupByCriticality groupByCriticality = getIssueCountGroupByCriticality(groupByCriticalityList);

        ProjectSummaryDto projectSummaryDto = projectServiceV3.getProjectSummary(searchIssueGroupRequest.getProjectId());

        Integer totalDefectCount = groupByCriticality.getHigh() + groupByCriticality.getMedium() + groupByCriticality.getLow();

        ReportPDFResponse.GroupByCriticalityCertainty groupByCriticalityCertainty = getGroupByCriticalityCertainty(searchIssueGroupRequest, totalDefectCount);

        //Unassigned summary
        Map<String, ReportPDFResponse.CountValue> highDefiniteUnassignedMap = getCertaintyCriticalityMap(searchIssueGroupRequest, "D", "H", false, totalDefectCount);
        Map<String, ReportPDFResponse.CountValue> highMaybeUnassignedMap = getCertaintyCriticalityMap(searchIssueGroupRequest, "M", "H", false, totalDefectCount);
        Map<String, ReportPDFResponse.CountValue> mediumDefiniteUnassignedMap = getCertaintyCriticalityMap(searchIssueGroupRequest, "D", "M", false, totalDefectCount);
        Map<String, ReportPDFResponse.CountValue> mediumMaybeUnassignedMap = getCertaintyCriticalityMap(searchIssueGroupRequest, "M", "M", false, totalDefectCount);
        Map<String, ReportPDFResponse.CountValue> lowDefiniteUnassignedMap = getCertaintyCriticalityMap(searchIssueGroupRequest, "D", "L", false, totalDefectCount);
        Map<String, ReportPDFResponse.CountValue> lowMaybeUnassignedMap = getCertaintyCriticalityMap(searchIssueGroupRequest, "M", "L", false, totalDefectCount);

        int totalUnassigned = getTotalUnassigned(highDefiniteUnassignedMap, highMaybeUnassignedMap, mediumDefiniteUnassignedMap, mediumMaybeUnassignedMap, lowDefiniteUnassignedMap, lowMaybeUnassignedMap);

        ReportPDFResponse.GroupByCriticalityCertainty groupByCriticalityCertaintyUnassigned = ReportPDFResponse.GroupByCriticalityCertainty.builder()
                .highD(highDefiniteUnassignedMap)
                .highM(highMaybeUnassignedMap)
                .mediumD(mediumDefiniteUnassignedMap)
                .mediumM(mediumMaybeUnassignedMap)
                .lowD(lowDefiniteUnassignedMap)
                .lowM(lowMaybeUnassignedMap)
                .build();

        ReportPDFResponse.UnassignedData unassigned = ReportPDFResponse.UnassignedData.builder()
                .groupByCriticalityCertainty(groupByCriticalityCertaintyUnassigned)
                .count(totalUnassigned)
                .build();


        //Assigned data
        List<AssigneeCountRow> assignedCountRows = userDao.getAssigneeCriticalityRuleCodeCount(
                searchIssueGroupRequest.getProjectId(),
                searchIssueGroupRequest.getScanTaskId(),
                searchIssueGroupRequest.getRuleCodes(),
                searchIssueGroupRequest.getRuleSets(),
                scanFilePathListFilter,
                searchIssueGroupRequest.getPathCategory(),
                searchIssueGroupRequest.getCertainty(),
                searchIssueGroupRequest.getDsrType(),
                searchIssueGroupRequest.getCriticality(),
                true,
                searchIssueGroupRequest.getValidationAction(),
                searchIssueGroupRequest.getSearchValue());
        Map<String, ReportAssigneeStatisticRow> reportAssigneeStatisticRowMap = getReportAssigneeStatisticRowMap(assignedCountRows);

        List<ReportAssigneeStatisticRow> assignedByUser = new ArrayList<>(reportAssigneeStatisticRowMap.values());
        Integer totalAssignedCount = assignedByUser.stream().map(row -> row.getCounts()).reduce(0, (subTotal, value) -> subTotal + value);


        ReportPDFResponse.AssigneeData assigned = ReportPDFResponse.AssigneeData.builder()
                .count(totalAssignedCount)
                .assignedByUser(assignedByUser)
                .build();

        ReportPDFResponse.GroupByAssignee groupByAssignee = ReportPDFResponse.GroupByAssignee.builder()
                .unassigned(unassigned)
                .assigned(assigned)
                .build();

        Project projectFilter = Project.builder().id(searchIssueGroupRequest.getProjectId()).build();

        Integer previousDefectsCount = null;
        Optional<com.xcal.api.entity.ScanTask> latestScanTask = scanTaskService.getLatestCompletedScanTaskByProject(projectFilter);
        if (latestScanTask.isPresent()) {
            Optional<com.xcal.api.entity.ScanTask> previousCompletedScanTask = scanTaskService.getPreviousCompletedScanTaskByScanTask(latestScanTask.get());
            if (previousCompletedScanTask.isPresent()) {
                previousDefectsCount = Integer.parseInt(previousCompletedScanTask.get().getSummary().get("issues"));
            }
        }

        ReportPDFResponse.CountValue defectsAssigned = ReportPDFResponse.CountValue.builder()
                .count(totalAssignedCount)
                .percentage(getPercentage(totalDefectCount, totalAssignedCount))
                .build();

        List<ReportFileStatisticRow> reportFileStatisticRowList = issueGroupDao.getReportFileStatisticRow(
                searchIssueGroupRequest.getProjectId(),
                searchIssueGroupRequest.getScanTaskId(),
                searchIssueGroupRequest.getRuleCodes(),
                searchIssueGroupRequest.getRuleSets(),
                scanFilePathListFilter,
                searchIssueGroupRequest.getPathCategory(),
                searchIssueGroupRequest.getCertainty(),
                searchIssueGroupRequest.getDsrType(),
                searchIssueGroupRequest.getCriticality(),
                true,
                searchIssueGroupRequest.getValidationAction(),
                searchIssueGroupRequest.getSearchValue()
        );
        for (ReportFileStatisticRow reportFileStatisticRow : reportFileStatisticRowList) {
            reportFileStatisticRow.setPath(reportFileStatisticRow.getPath().replaceAll("^(\\$[ht])?/", ""));
            reportFileStatisticRow.setPercentage(getPercentage(totalDefectCount, reportFileStatisticRow.getCounts()));
        }

        String criticality = projectSummaryMapper.getProjectRisk(searchIssueGroupRequest.getProjectId(),
                searchIssueGroupRequest.getScanTaskId(),
                searchIssueGroupRequest.getRuleCodes(),
                searchIssueGroupRequest.getRuleSets(),
                scanFilePathListFilter,
                searchIssueGroupRequest.getPathCategory(),
                searchIssueGroupRequest.getCertainty(),
                searchIssueGroupRequest.getDsrType(),
                searchIssueGroupRequest.getCriticality(),
                null,
                StringUtil.splitAndTrim(searchIssueGroupRequest.getSearchValue(),SEARCH_VAL_DELIMITER));

        Optional<ScanTask> scanTaskOptional = scanTaskRepository.findById(searchIssueGroupRequest.getScanTaskId());
        ScanTask scanTask = scanTaskOptional.get();
        if (scanTask.getStatus() != ScanTask.Status.COMPLETED) { //not complete -> get last completed
            Optional<ScanTask> previousScanTaskOptional = scanTaskRepository.findPreviousByProjectAndScanTaskAndStatus(searchIssueGroupRequest.getProjectId(), searchIssueGroupRequest.getScanTaskId(), ScanTask.Status.COMPLETED);
            scanTask = previousScanTaskOptional.get();
        }

        return ReportPDFResponse.builder()
                .projectName(projectSummaryDto.getName())
                .reportDate(new Date())
                .scanMode(projectSummaryDto.getScanMode())
                .scanDate(scanTask.getScanEndAt())
                .scanTime(scanTask.getScanEndAt())
                .fileCounts(projectSummaryDto.getSummary().getFileCount())
                .lineCounts(projectSummaryDto.getSummary().getLineCount())
                .projectOwner(projectSummaryDto.getCreatedBy())
                .language(projectSummaryDto.getSummary().getLangList())
                .projectCriticality(criticality)
                .defectsCount(totalDefectCount)
                .previousDefectsCount(previousDefectsCount)
                .issueCountGroupByCriticality(groupByCriticality)
                .defectsAssigned(defectsAssigned)
                .groupByCriticalityCertainty(groupByCriticalityCertainty)
                .groupByFile(reportFileStatisticRowList)
                .groupByAssignee(groupByAssignee)
                .build();

    }

    float getPercentage(int divisor, float dividend) {
        if (divisor == 0) {
            return 0;
        }
        return dividend / divisor * 100;
    }

    public static ReportPDFResponse.IssueCountGroupByCriticality getIssueCountGroupByCriticality(List<IssueGroupCountRow> groupByCriticalityList) {
        log.info("[getIssueCountGroupByCriticality] groupByCriticalityList.size: {}", groupByCriticalityList == null ? 0 : groupByCriticalityList.size());
        ReportPDFResponse.IssueCountGroupByCriticality groupByCriticality = ReportPDFResponse.IssueCountGroupByCriticality.builder()
                .high(0)
                .medium(0)
                .low(0)
                .build();
        for (IssueGroupCountRow issueGroupCountRow : groupByCriticalityList) {
            if (issueGroupCountRow.getCriticality().equals("HIGH")) {
                groupByCriticality.setHigh(Integer.parseInt(issueGroupCountRow.getCount()));
            } else if (issueGroupCountRow.getCriticality().equals("MEDIUM")) {
                groupByCriticality.setMedium(Integer.parseInt(issueGroupCountRow.getCount()));
            } else if (issueGroupCountRow.getCriticality().equals("LOW")) {
                groupByCriticality.setLow(Integer.parseInt(issueGroupCountRow.getCount()));
            }
        }
        log.info("[getIssueCountGroupByCriticality] result groupByCriticality.size: {}", groupByCriticality);
        return groupByCriticality;
    }

    private int getTotalUnassigned(Map<String, ReportPDFResponse.CountValue> highDefiniteUnassignedMap, Map<String, ReportPDFResponse.CountValue> highMaybeUnassignedMap, Map<String, ReportPDFResponse.CountValue> mediumDefiniteUnassignedMap, Map<String, ReportPDFResponse.CountValue> mediumMaybeUnassignedMap, Map<String, ReportPDFResponse.CountValue> lowDefiniteUnassignedMap, Map<String, ReportPDFResponse.CountValue> lowMaybeUnassignedMap) {
        log.info("[getTotalUnassigned] highDefiniteUnassignedMap.size: {}, highMaybeUnassignedMap.size: {}, mediumDefiniteUnassignedMap.size: {}, mediumMaybeUnassignedMap.size: {}, lowDefiniteUnassignedMap.size: {}, lowMaybeUnassignedMap.size: {}, ",
                highDefiniteUnassignedMap == null ? 0 : highDefiniteUnassignedMap.size(),
                highMaybeUnassignedMap == null ? 0 : highMaybeUnassignedMap.size(),
                mediumDefiniteUnassignedMap == null ? 0 : mediumDefiniteUnassignedMap.size(),
                mediumMaybeUnassignedMap == null ? 0 : mediumMaybeUnassignedMap.size(),
                lowDefiniteUnassignedMap == null ? 0 : lowDefiniteUnassignedMap.size(),
                lowMaybeUnassignedMap == null ? 0 : lowMaybeUnassignedMap.size()
        );
        int totalUnassigned = 0;
        totalUnassigned += highDefiniteUnassignedMap.entrySet().stream().map(countValue -> countValue.getValue().getCount()).reduce(0, (subtotal, value) -> subtotal + value);
        totalUnassigned += highMaybeUnassignedMap.entrySet().stream().map(countValue -> countValue.getValue().getCount()).reduce(0, (subtotal, value) -> subtotal + value);

        totalUnassigned += mediumDefiniteUnassignedMap.entrySet().stream().map(countValue -> countValue.getValue().getCount()).reduce(0, (subtotal, value) -> subtotal + value);
        totalUnassigned += mediumMaybeUnassignedMap.entrySet().stream().map(countValue -> countValue.getValue().getCount()).reduce(0, (subtotal, value) -> subtotal + value);

        totalUnassigned += lowDefiniteUnassignedMap.entrySet().stream().map(countValue -> countValue.getValue().getCount()).reduce(0, (subtotal, value) -> subtotal + value);
        totalUnassigned += lowMaybeUnassignedMap.entrySet().stream().map(countValue -> countValue.getValue().getCount()).reduce(0, (subtotal, value) -> subtotal + value);
        log.info("[getTotalUnassigned] result totalUnassigned:{}", totalUnassigned);
        return totalUnassigned;
    }

    private ReportPDFResponse.GroupByCriticalityCertainty getGroupByCriticalityCertainty(SearchIssueGroupRequest searchIssueGroupRequest, Integer totalDefectCount) {
        log.info("[getGroupByCriticalityCertainty] searchIssueGroupRequest: {} totalDefectCount:{}", searchIssueGroupRequest, totalDefectCount);
        Map<String, ReportPDFResponse.CountValue> highDefiniteMap = getCertaintyCriticalityMap(searchIssueGroupRequest, "D", "H", totalDefectCount);
        Map<String, ReportPDFResponse.CountValue> highMaybeMap = getCertaintyCriticalityMap(searchIssueGroupRequest, "M", "H", totalDefectCount);
        Map<String, ReportPDFResponse.CountValue> mediumDefiniteMap = getCertaintyCriticalityMap(searchIssueGroupRequest, "D", "M", totalDefectCount);
        Map<String, ReportPDFResponse.CountValue> mediumMaybeMap = getCertaintyCriticalityMap(searchIssueGroupRequest, "M", "M", totalDefectCount);
        Map<String, ReportPDFResponse.CountValue> lowDefiniteMap = getCertaintyCriticalityMap(searchIssueGroupRequest, "D", "L", totalDefectCount);
        Map<String, ReportPDFResponse.CountValue> lowMaybeMap = getCertaintyCriticalityMap(searchIssueGroupRequest, "M", "L", totalDefectCount);

        ReportPDFResponse.GroupByCriticalityCertainty groupByCriticalityCertainty = ReportPDFResponse.GroupByCriticalityCertainty.builder()
                .highD(highDefiniteMap)
                .highM(highMaybeMap)
                .mediumD(mediumDefiniteMap)
                .mediumM(mediumMaybeMap)
                .lowD(lowDefiniteMap)
                .lowM(lowMaybeMap)
                .build();
        log.info("[getGroupByCriticalityCertainty] result groupByCriticalityCertainty:{}", groupByCriticalityCertainty);
        return groupByCriticalityCertainty;
    }


    private Map<String, ReportPDFResponse.CountValue> getCertaintyCriticalityMap(SearchIssueGroupRequest searchIssueGroupRequest, String certainty, String criticality, int totalDefect) {
        return getCertaintyCriticalityMap(searchIssueGroupRequest, certainty, criticality, null, totalDefect);
    }

    Map<String, ReportPDFResponse.CountValue> getCertaintyCriticalityMap(SearchIssueGroupRequest searchIssueGroupRequest, String
            certainty, String criticality, Boolean assigned, int totalDefect) {
        //compute and make rule code count
        List<IssueGroupCountRow> highDefiniteList = issueGroupDao.getIssueGroupCountWithFilter(
                IssueGroupDao.FILTER_CATEGORY_RULE_CODE,
                searchIssueGroupRequest.getProjectId(),
                searchIssueGroupRequest.getScanTaskId(),
                searchIssueGroupRequest.getRuleCodes(),
                searchIssueGroupRequest.getRuleSets(),
                getScanFilePathListFilter(searchIssueGroupRequest),
                searchIssueGroupRequest.getPathCategory(),
                certainty,
                searchIssueGroupRequest.getDsrType(),
                criticality,
                assigned,
                searchIssueGroupRequest.getValidationAction(),
                searchIssueGroupRequest.getSearchValue()
        );

        return highDefiniteList.stream()
                .collect(Collectors.toMap(IssueGroupCountRow::getRuleCode, issueGroupCountRow -> {
                            int count = Integer.parseInt(issueGroupCountRow.getCount());
                            return ReportPDFResponse.CountValue.builder()
                                    .count(count)
                                    .percentage(getPercentage(totalDefect, count))
                                    .build();
                        }
                ));
    }

    Map<String, ReportAssigneeStatisticRow> getReportAssigneeStatisticRowMap
            (List<AssigneeCountRow> assignedCountRows) {
        Map<String, ReportAssigneeStatisticRow> reportAssigneeStatisticRowMap = new HashMap<>(); //<user id, Report Assignee Statistic Row>
        int totalCount = 0;

        for (AssigneeCountRow assigneeCountRow : assignedCountRows) {
            UUID userUuid = assigneeCountRow.getId();
            String userId = userUuid.toString();

            String userName = assigneeCountRow.getUsername();

            int countForRow = assigneeCountRow.getCount();
            totalCount += countForRow;

            String criticality = assigneeCountRow.getCriticality();
            String ruleCode = assigneeCountRow.getRuleCode();

            //create if not exist
            if (!reportAssigneeStatisticRowMap.containsKey(userId)) {

                ReportAssigneeStatisticRow.User user = ReportAssigneeStatisticRow.User.builder()
                        .name(userName)
                        .build();

                ReportAssigneeStatisticRow.BreakDownByCriticalityAndCsvCode breakDownByRiskAndCsvCod = ReportAssigneeStatisticRow.BreakDownByCriticalityAndCsvCode.builder()
                        .high(new HashMap<>())
                        .medium(new HashMap<>())
                        .low(new HashMap<>())
                        .build();

                ReportAssigneeStatisticRow reportAssigneeStatisticRow = ReportAssigneeStatisticRow.builder()
                        .user(user)
                        .counts(0)
                        .breakDownByCriticalityAndCsvCode(breakDownByRiskAndCsvCod)
                        .build();

                reportAssigneeStatisticRowMap.put(userId, reportAssigneeStatisticRow);
            }


            //provide count
            ReportAssigneeStatisticRow reportAssigneeStatisticRow = reportAssigneeStatisticRowMap.get(userId);
            ReportAssigneeStatisticRow.BreakDownByCriticalityAndCsvCode breakDownByCriticalityAndCsvCode = reportAssigneeStatisticRow.getBreakDownByCriticalityAndCsvCode();
            if (criticality.equals("HIGH")) {
                breakDownByCriticalityAndCsvCode.getHigh().put(ruleCode, countForRow);
            } else if (criticality.equals("MEDIUM")) {
                breakDownByCriticalityAndCsvCode.getMedium().put(ruleCode, countForRow);
            } else {//LOW
                breakDownByCriticalityAndCsvCode.getLow().put(ruleCode, countForRow);
            }
            reportAssigneeStatisticRow.setCounts(reportAssigneeStatisticRow.getCounts() + countForRow);

        }// for all assign count row
        return reportAssigneeStatisticRowMap;
    }

    ReportAssigneeStatisticRow convertToReportAssigneeStatisticRow
            (List<AssigneeCountRow> unassignedCountRows) {
        int totalCount = 0;
        Map<String, Integer> breakdownByCsvCode = new HashMap<>();
        for (AssigneeCountRow assigneeCountRow : unassignedCountRows) {
            int countForRow = assigneeCountRow.getCount();
            String ruleCode = assigneeCountRow.getRuleCode();
            totalCount += countForRow;

            if (!breakdownByCsvCode.containsKey(ruleCode)) {
                breakdownByCsvCode.put(ruleCode, countForRow);
            }
        }
        ReportAssigneeStatisticRow unassigned = ReportAssigneeStatisticRow.builder()
                .user(null)
                .breakdownByCsvCode(breakdownByCsvCode)
                .counts(totalCount)
                .build();
        return unassigned;
    }

}
