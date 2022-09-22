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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xcal.api.config.AppProperties;
import com.xcal.api.entity.FileStorage;
import com.xcal.api.entity.ScanTask;
import com.xcal.api.entity.ScanTaskStatusLog;
import com.xcal.api.entity.ScanTaskStatusLog_;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.payload.JaegerData;
import com.xcal.api.model.payload.PrometheusData;
import com.xcal.api.repository.ScanTaskRepository;
import com.xcal.api.repository.ScanTaskStatusLogRepository;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.TracerUtil;
import io.opentracing.Tracer;
import io.opentracing.contrib.java.spring.jaeger.starter.JaegerConfigurationProperties;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Performance service to collection log and download load
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PerformanceService {

    @NonNull ObjectMapper om;
    @NonNull JaegerConfigurationProperties jaegerConfigurationProperties;
    @NonNull AppProperties appProperties;
    @NonNull HttpService httpService;
    @NonNull ScanTaskRepository scanTaskRepository;
    @NonNull ScanTaskStatusLogRepository scanTaskStatusLogRepository;
    @NonNull EmailService emailService;
    @NonNull FileStorageService fileStorageService;
    @NonNull Tracer tracer;

    @Value("${scan.fail.send.email.list}")
    private String scanFailSendEmailList;

    private static String JAEGER_LOG_FILE_NAME = "scan_task_log.json";
    private static String SCAN_TASK_FILE_NAME = "scan_task.json";
    private static String SCAN_TASK_STATUS_LOG_FILE_NAME = "scan_task_status_log.json";

    private static String SCAN_SERVICE_CPU_USAGE_FILE_NAME = "scan_service_CPU.json";
    private static String SCAN_SERVICE_MEM_USAGE_FILE_NAME = "scan_service_MEM.json";

    private static String WEB_MAIN_API_CPU_USAGE_FILE_NAME = "web_main_api_CPU.json";
    private static String WEB_MAIN_API_MEM_USAGE_FILE_NAME = "web_main_api_MEM.json";

    private static String DATABASE_CPU_USAGE_FILE_NAME = "database_CPU.json";
    private static String DATABASE_MEM_USAGE_FILE_NAME = "database_MEM.json";

    private static String XVSA_CPU_USAGE_FILE_NAME = "xvsa_CPU.json";
    private static String XVSA_MEM_USAGE_FILE_NAME = "xvsa_MEM.json";

    private static String KAFKA_CPU_USAGE_FILE_NAME = "kafka_CPU.json";
    private static String KAFKA_MEM_USAGE_FILE_NAME = "kafka_MEM.json";

    private static String JAEGER_CPU_USAGE_FILE_NAME = "jaeger_CPU.json";
    private static String JAEGER_MEM_USAGE_FILE_NAME = "jaeger_MEM.json";

    private static String XVSA_FILE_INFO_NAME = "fileinfo.json";

    private static String LOG_SUB_FOLDER = "log";
    private static String PERFORMANCE_SUB_FOLDER = "performance";
    private static String VOLUME_SCAN = "volume_scan";
    private static String VOLUME_DIAGNOSTIC_NAME = "volume_diagnostic";


    /**
     * Async task to collect performance data and save to disk
     * @param scanTask ScanTask
     */
    @Async
    public void collectPerformanceData(ScanTask scanTask) throws AppException {
        log.info("[collectPerformanceData] scanTaskId: {}", scanTask.getId());
        saveJaegerLog(scanTask);
        savePrometheusData(scanTask);
        saveScanTask(scanTask);
        saveXvsaLog(scanTask);
    }

    private void saveXvsaLog(ScanTask scanTask) throws AppException {
        log.info("[saveXvsaLog] scanTask, id: {}", scanTask.getId());
        FileStorage fileStorage = fileStorageService.findByName(VOLUME_SCAN).orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] volume: {}", AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.messageTemplate, VOLUME_SCAN)));

        Path fileInfoPath = Paths.get(fileStorage.getFileStorageHost(), scanTask.getId().toString(), XVSA_FILE_INFO_NAME);
        Path targetPath = this.getDiagnosticDataStoragePath(scanTask, XVSA_FILE_INFO_NAME, null);
        if (fileInfoPath.toFile().exists()) {
            try {
                FileUtils.copyFile(fileInfoPath.toFile(), targetPath.toFile());
            } catch (IOException e) {
                log.error("[saveXvsaLog] exception: {}", e.getMessage());
            }
        }
    }

    /**
     * get Jaeger data and save to storage
     * @param scanTask the scan task
     */
    private void saveJaegerLog(ScanTask scanTask) throws AppException {
        log.info("[saveJaegerLog] scanTask, Id: {}", scanTask.getId());
        String sourceUrl = "http://{}:{}/api/traces";

        String jaegerUri = CommonUtil.formatString(sourceUrl, jaegerConfigurationProperties.getUdpSender().getHost(), appProperties.getJaeger().getQueryPort());

        Map<String, String> tags = new HashMap<>();
        tags.put("scan_task_id", scanTask.getId().toString());

        try {
//            Map<String, String> param = new HashMap<>();
//            param.put("service", jaegerConfigurationProperties.getServiceName());
//            param.put("tags", URLEncoder.encode(om.writeValueAsString(tags), "UTF-8"));
//            param.put("tags", om.writeValueAsString(tags));
//            param.put("tags", "%7B%22scan_task_id%22%3A%226b342940-082f-4b56-84bd-e8e2cadcc99f%22%7D");
//            param.put("lookback", "2h");
//            param.put("limit", "5");
//            String result = httpService.httpGet(jaegerUri, param);

            jaegerUri += "?service=" + jaegerConfigurationProperties.getServiceName() + "&lookback=2h&limit=50&tags=" + om.writeValueAsString(tags);
            String result = httpService.httpGet(jaegerUri, null);

            writeContentToStoragePath(scanTask, JAEGER_LOG_FILE_NAME, result, LOG_SUB_FOLDER);
        } catch (JsonProcessingException e) {  // | UnsupportedEncodingException
            log.error("[saveJaegerLog] exception: {}", e.getMessage());
            TracerUtil.setTag(tracer,TracerUtil.Tag.ERROR, "true");
        }

    }


    /**
     * get prometheus data and save to storage
     * @param scanTask scanTask
     */
    void savePrometheusData(ScanTask scanTask) throws AppException {
        log.info("[savePrometheusData] scanTask, Id: {}", scanTask.getId());

        saveSinglePrometheusData(appProperties.getPerformance().getPrometheus().getQuery().getScanServiceCpu(), scanTask, SCAN_SERVICE_CPU_USAGE_FILE_NAME);
        saveSinglePrometheusData(appProperties.getPerformance().getPrometheus().getQuery().getScanServiceMem(), scanTask, SCAN_SERVICE_MEM_USAGE_FILE_NAME);

        saveSinglePrometheusData(appProperties.getPerformance().getPrometheus().getQuery().getWebMainApiCpu(), scanTask, WEB_MAIN_API_CPU_USAGE_FILE_NAME);
        saveSinglePrometheusData(appProperties.getPerformance().getPrometheus().getQuery().getWebMainApiMem(), scanTask, WEB_MAIN_API_MEM_USAGE_FILE_NAME);

        saveSinglePrometheusData(appProperties.getPerformance().getPrometheus().getQuery().getDatabaseCpu(), scanTask, DATABASE_CPU_USAGE_FILE_NAME);
        saveSinglePrometheusData(appProperties.getPerformance().getPrometheus().getQuery().getDatabaseMem(), scanTask, DATABASE_MEM_USAGE_FILE_NAME);

        saveSinglePrometheusData(appProperties.getPerformance().getPrometheus().getQuery().getXvsaCpu(), scanTask, XVSA_CPU_USAGE_FILE_NAME);
        saveSinglePrometheusData(appProperties.getPerformance().getPrometheus().getQuery().getXvsaMem(), scanTask, XVSA_MEM_USAGE_FILE_NAME);

        saveSinglePrometheusData(appProperties.getPerformance().getPrometheus().getQuery().getKafkaCpu(), scanTask, KAFKA_CPU_USAGE_FILE_NAME);
        saveSinglePrometheusData(appProperties.getPerformance().getPrometheus().getQuery().getKafkaMem(), scanTask, KAFKA_MEM_USAGE_FILE_NAME);

        saveSinglePrometheusData(appProperties.getPerformance().getPrometheus().getQuery().getJaegerCpu(), scanTask, JAEGER_CPU_USAGE_FILE_NAME);
        saveSinglePrometheusData(appProperties.getPerformance().getPrometheus().getQuery().getJaegerMem(), scanTask, JAEGER_MEM_USAGE_FILE_NAME);
    }

    /**
     * convert scan task object to json and save to storage
     * @param scanTask scanTask
     */
    private void saveScanTask(ScanTask scanTask) throws AppException {
        log.info("[saveScanTask] scanTask, Id: {}", scanTask.getId());
        try {
            String scanTaskJson = new ObjectMapper().writeValueAsString(scanTask);
            writeContentToStoragePath(scanTask, SCAN_TASK_FILE_NAME, scanTaskJson, null);
        } catch (IOException e) {
            log.error("[saveScanTask] exception: {}", e.getMessage());
            TracerUtil.setTag(tracer,TracerUtil.Tag.ERROR, "true");
        }

        try {
            List<ScanTaskStatusLog> scanTaskStatusLogList = scanTaskStatusLogRepository.findByScanTaskId(scanTask.getId(), Sort.by(Sort.Order.asc(ScanTaskStatusLog_.MODIFIED_ON)));
            String scanTaskStatusLogJson = new ObjectMapper().writeValueAsString(scanTaskStatusLogList);
            writeContentToStoragePath(scanTask, SCAN_TASK_STATUS_LOG_FILE_NAME, scanTaskStatusLogJson, null);
        } catch (IOException e) {
            log.error("[saveScanTask] save scan task status log message: {}", e.getMessage());
            TracerUtil.setTag(tracer,TracerUtil.Tag.ERROR, "true");
        }
    }

    /**
     * delete the performance by project id
     * @param projectId projectId
     */
    public void deletePerformanceByProject(String projectId) throws AppException {
        log.info("[deletePerformanceByProject] project id: {}", projectId);
        String path = this.getDiagnosticStoragePath() + File.separator + projectId;
        try {
            FileUtils.deleteDirectory(new File(path));
        } catch (IOException e) {
            log.error("[deletePerformanceByProject] message: {}", e.getMessage());
            TracerUtil.setTag(tracer,TracerUtil.Tag.ERROR, "true");
        }
    }

    /**
     * save a single query prometheus data
     * @param query query
     * @param scanTask scanTask
     * @param filename filename
     */
    private void saveSinglePrometheusData(String query, ScanTask scanTask, String filename) throws AppException {
        String resourcePath = "/api/v1/query_range";
        String url = appProperties.getPerformance().getPrometheus().getQueryUrl() + resourcePath;
        Map<String, String> param = new HashMap<>();
        param.put("query", query);
        param.put("start", Long.toString(scanTask.getScanStartAt().getTime() / 1000));
        param.put("end", Long.toString(scanTask.getScanEndAt().getTime() / 1000L));
        param.put("step", "5");

        String result = httpService.httpGet(url, param);
        writeContentToStoragePath(scanTask, filename, result, PERFORMANCE_SUB_FOLDER);
    }

    /**
     * search the storage path by scan task and file name
     * @param scanTask scanTask
     * @param filename filename
     * @return storage path of the diagnostic data
     */
    private Path getDiagnosticDataStoragePath(ScanTask scanTask, String filename, String subFolder) throws AppException {
        log.info("[getDiagnosticDataStoragePath] scanTask, id: {}, filename: {}, subFolder: {}", scanTask.getId(), filename, subFolder);
        String timestampString = DateFormatUtils.format(System.currentTimeMillis(), "yyyyMMdd");
        String scanTaskDirName = timestampString+"_"+scanTask.getId();
        Path storagePath;
        if(StringUtils.isNotBlank(subFolder)){
            storagePath = Paths.get(this.getDiagnosticStoragePath(), scanTask.getProject().getProjectId(), scanTaskDirName, subFolder, filename);
        }else{
            storagePath = Paths.get(this.getDiagnosticStoragePath(), scanTask.getProject().getProjectId(), scanTaskDirName, filename);
        }
        return storagePath;
    }

    /**
     * write content to the directory
     * @param scanTask scanTask
     * @param filename filename
     * @param content content
     * @param subFolder subFolder
     */
    private void writeContentToStoragePath(ScanTask scanTask, String filename, String content, String subFolder) throws AppException {
        log.info("[writeContentToStoragePath] scanTask id: {}, filename: {}", scanTask.getId(), filename);
        Path filePath = getDiagnosticDataStoragePath(scanTask, filename, subFolder);
        try {
            FileUtils.writeStringToFile(filePath.toFile(), content, Charset.defaultCharset());
        } catch (IOException e) {
            log.error("[writeContentToStoragePath] message: {}", e.getMessage());
            TracerUtil.setTag(tracer,TracerUtil.Tag.ERROR, "true");
        }
    }

    /**
     * query jaeger log
     * @param scanTask scanTask
     * @param projectId projectId
     * @return jaegerData
     */
    public JaegerData queryJaegerLogByScanTask(ScanTask scanTask, String projectId) throws AppException {
        log.info("[queryJaegerLogByScanTask] scanTask id: {}, project id: {}", scanTask.getId(), projectId);
        String data = queryDataByFile(scanTask, PerformanceService.JAEGER_LOG_FILE_NAME);

        JaegerData jaegerData = null;
        try {
            jaegerData = om.readValue(data, JaegerData.class);
        } catch (IOException e) {
            log.error("[queryJaegerLogByScanTaskId] message: {}", e.getMessage());
            TracerUtil.setTag(tracer,TracerUtil.Tag.ERROR, "true");
        }

        return jaegerData;
    }

    /**
     * query prometheus data
     * @param scanTask scanTask
     * @return PrometheusData Map
     */
    public Map<String, PrometheusData> queryPrometheusByScanTask(ScanTask scanTask) throws AppException {
        log.info("[queryPrometheusByScanTaskId] scan task id: {}", scanTask.getId());

        Map<String, PrometheusData> jsonMap = new HashMap<>();
        PrometheusData prometheusData = queryPrometheusValues(scanTask, PerformanceService.SCAN_SERVICE_CPU_USAGE_FILE_NAME);
        jsonMap.put("scan_service_cpu", prometheusData);
        prometheusData = queryPrometheusValues(scanTask, PerformanceService.SCAN_SERVICE_MEM_USAGE_FILE_NAME);
        jsonMap.put("scan_service_mem", prometheusData);

        prometheusData = queryPrometheusValues(scanTask, PerformanceService.WEB_MAIN_API_CPU_USAGE_FILE_NAME);
        jsonMap.put("web_api_cpu", prometheusData);
        prometheusData = queryPrometheusValues(scanTask, PerformanceService.WEB_MAIN_API_MEM_USAGE_FILE_NAME);
        jsonMap.put("web_api_mem", prometheusData);

        prometheusData = queryPrometheusValues(scanTask, PerformanceService.DATABASE_CPU_USAGE_FILE_NAME);
        jsonMap.put("database_cpu", prometheusData);
        prometheusData = queryPrometheusValues(scanTask, PerformanceService.DATABASE_MEM_USAGE_FILE_NAME);
        jsonMap.put("database_mem", prometheusData);

        prometheusData = queryPrometheusValues(scanTask, PerformanceService.KAFKA_CPU_USAGE_FILE_NAME);
        jsonMap.put("kafka_cpu", prometheusData);
        prometheusData = queryPrometheusValues(scanTask, PerformanceService.KAFKA_MEM_USAGE_FILE_NAME);
        jsonMap.put("kafka_mem", prometheusData);

        prometheusData = queryPrometheusValues(scanTask, PerformanceService.XVSA_CPU_USAGE_FILE_NAME);
        jsonMap.put("xvsa_cpu", prometheusData);
        prometheusData = queryPrometheusValues(scanTask, PerformanceService.XVSA_MEM_USAGE_FILE_NAME);
        jsonMap.put("xvsa_mem", prometheusData);

        prometheusData = queryPrometheusValues(scanTask, PerformanceService.JAEGER_CPU_USAGE_FILE_NAME);
        jsonMap.put("jaeger_cpu", prometheusData);
        prometheusData = queryPrometheusValues(scanTask, PerformanceService.JAEGER_MEM_USAGE_FILE_NAME);
        jsonMap.put("jaeger_mem", prometheusData);

        return jsonMap;
    }

    /**
     * query prometheus
     * @param scanTask scanTask
     * @param filename filename
     * @return prometheus Data
     */
    private PrometheusData queryPrometheusValues(ScanTask scanTask, String filename) throws AppException {
        log.info("[queryPrometheusValues] scan task id: {}", scanTask.getId());
        String data = queryDataByFile(scanTask, filename);
        PrometheusData prometheusData = null;
        try {
            prometheusData = om.readValue(data, PrometheusData.class);
        } catch (IOException e) {
            log.error("[queryPrometheusValues] exception: {}", e.getMessage());
            TracerUtil.setTag(tracer,TracerUtil.Tag.ERROR, "true");
        }

        return prometheusData;
    }

    /**
     * get single file content scan task id
     * @param scanTask scanTask
     * @param filename the file name to get
     * @return raw data in the file
     */
    private String queryDataByFile(ScanTask scanTask, String filename) throws AppException {
        log.info("[queryDataByFile] scan task id: {}, filename: {}", scanTask.getId(), filename);
        String data;
        try {
            Path logPath = getLogFolderPathByScanTask(scanTask).orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.unifyErrorCode,
                    CommonUtil.formatString("[{}] volume: {}", AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.messageTemplate, VOLUME_DIAGNOSTIC_NAME)));
            data = FileUtils.readFileToString(logPath.toFile(), Charset.defaultCharset());
        } catch (IOException e) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_PERFORMANCE_QUERY_DATA_FAILED.unifyErrorCode,
                    CommonUtil.formatString("[{}] read file message: {}", AppException.ErrorCode.E_API_PERFORMANCE_QUERY_DATA_FAILED.messageTemplate, e.getMessage()),e);
        }

        return data;
    }

    /**
     * search the performance log fold by scan task id
     * @param scanTask scanTask
     * @return Optional Path
     */
    private Optional<Path> getLogFolderPathByScanTask(ScanTask scanTask) throws AppException {
        log.error("[getLogFolderPathByScanTask] scan task id: {}", scanTask.getId());
        Optional<Path> optionalPath;
        Optional<FileStorage> fileStorageOptional = fileStorageService.findByName(VOLUME_DIAGNOSTIC_NAME);
        FileStorage fileStorage = fileStorageOptional.orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] volume: {}", AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.messageTemplate, VOLUME_DIAGNOSTIC_NAME)));
        try (Stream<Path> walkPathStream = Files.walk(Paths.get(fileStorage.getFileStorageHost(), scanTask.getProject().getProjectId()))) {
            optionalPath = walkPathStream.filter(path -> StringUtils.endsWith(path.toString(), scanTask.getId().toString())).findFirst();
        } catch (IOException e) {
            log.error("[getLogFolderByScanTask] message: {}", e.getMessage());
            optionalPath = Optional.empty();
            TracerUtil.setTag(tracer,TracerUtil.Tag.ERROR, "true");
        }
        return optionalPath;
    }

    /***
     * get the download zip file path.
     * crete the zip file if it does not exist
     * @param scanTask scanTask
     * @return Resource
     * @throws AppException AppException
     */
    public Resource getDownloadFilePath(ScanTask scanTask) throws AppException {
        log.error("[getDownloadFilePath] scan task id: {}", scanTask.getId());
        Path logDirPath = this.getLogFolderPathByScanTask(scanTask).orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_FILE_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] scanTaskId: {}", AppException.ErrorCode.E_API_FILE_COMMON_NOT_FOUND.messageTemplate, scanTask.getId())));

        String logFoldZipPath = logDirPath.toAbsolutePath() + ".zip";
        String[] nameArray = logFoldZipPath.split("/");
        String logFoldZipName = nameArray[nameArray.length - 1];
        File logFoldZip = Paths.get(logFoldZipPath).toFile();
        if (!logFoldZip.exists()) {
            Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.ZIP);
            try {
                archiver.create(logFoldZipName, Paths.get(this.getDiagnosticStoragePath(), scanTask.getProject().getProjectId()).toFile(), logDirPath.toFile());
            } catch (IOException e) {
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILE_COMPRESSFILE_FAILED.unifyErrorCode,
                        CommonUtil.formatString("[{}] {}", AppException.ErrorCode.E_API_FILE_COMPRESSFILE_FAILED.messageTemplate, e.getMessage()),e);
            }
        }

        Resource resource;
        try {
            resource = new UrlResource(logFoldZip.toURI());
        } catch (MalformedURLException e) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILE_COMPRESSFILE_FILE_OR_DIRECTORY_NOT_FOUND.unifyErrorCode,
                    CommonUtil.formatString("[{}] message: {}", AppException.ErrorCode.E_API_FILE_COMPRESSFILE_FILE_OR_DIRECTORY_NOT_FOUND.messageTemplate, e.getMessage()));
        }

        return resource;
    }

    public String saveLogFile(ScanTask scanTask, MultipartFile file) throws AppException {
        log.info("[saveLogFile] scanTask, id: {}, file originalFilename: {}", scanTask.getId(), file.getOriginalFilename());
        File toFile = this.getDiagnosticDataStoragePath(scanTask, file.getOriginalFilename(), LOG_SUB_FOLDER).toFile();
        try {
            FileUtils.writeByteArrayToFile(toFile, file.getBytes());
        } catch (IOException e) {
            log.error("[saveLogFile] exception: {}", e.getMessage());
            TracerUtil.setTag(tracer, TracerUtil.Tag.ERROR, "true");
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_PERFORMANCE_COPY_LOG_FILE_FAILED.unifyErrorCode,
                    CommonUtil.formatString("[{}] scan task id: {}, filename: {}, toFile: {} ", AppException.ErrorCode.E_API_PERFORMANCE_COPY_LOG_FILE_FAILED.messageTemplate, scanTask.getId(), file.getOriginalFilename(), toFile.toString()),e);
        }
        return toFile.getName();
    }

    private String getDiagnosticStoragePath() throws AppException {
        log.info("[getDiagnosticStoragePath] ");
        String volume = appProperties.getPerformance().getDataSavePath();
        FileStorage fileStorage = fileStorageService.findByName(volume).orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] storage: {}", AppException.ErrorCode.E_API_FILESTORAGE_COMMON_NOT_FOUND.messageTemplate, volume)));
        return fileStorage.getFileStorageHost();
    }


}