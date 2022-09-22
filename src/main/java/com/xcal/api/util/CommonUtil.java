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

package com.xcal.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xcal.api.exception.AppException;
import com.xcal.api.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public final class CommonUtil {

    private CommonUtil() {
    }

    private static final ObjectMapper om = new ObjectMapper();
    private static final Pattern SQUARE_BRACE = Pattern.compile("\\[.*?\\]");
    private static final SimpleDateFormat digitOnlyFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    public static String formatString(String string, Object... objects) {
        return MessageFormatter.arrayFormat(string, objects).getMessage();
    }

    public static Map<String, String> convertStringContentToMap(String content) throws AppException {
        Map<String, String> result;
        try {
            result = om.readValue(content, new TypeReference<HashMap<String, String>>() {
            });
        } catch (IOException e) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_COMMON_DTO_INVALID_CONTENT.unifyErrorCode,
                    CommonUtil.formatString("[{}] content: {}", AppException.ErrorCode.E_API_COMMON_DTO_INVALID_CONTENT.messageTemplate, content), e);
        }
        return result;
    }

    public static String writeObjectToJsonStringSilently(ObjectMapper om, @NotNull(message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_NOTNULL) Object input) {
        log.trace("[writeObjectToJsonStringSilently] class of input: {}", input.getClass().getName());
        String result;
        try {
            result = om.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            log.error("[writeObjectToJsonStringSilently] Exception, {}: {}", e.getClass(), e.getMessage());
            result = input instanceof String ? "" : "{}";
        }
        return result;
    }

    public static File writeStringToTempFile(String prefix, String suffix, String content) throws AppException {
        log.info("[writeStringToTempFile] prefix: {}, suffix: {}", prefix, suffix);
        File tmpFile;
        try {
            tmpFile = File.createTempFile(prefix, suffix);
            FileUtils.writeStringToFile(tmpFile, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("[writeStringToTempFile] error message: {}", e.getMessage());
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_FILE_COMMON_CREATE_TEMP_FILE_FAILED.unifyErrorCode,
                    CommonUtil.formatString("[{}] prefix: {}, suffix: {}", AppException.ErrorCode.E_API_FILE_COMMON_CREATE_TEMP_FILE_FAILED.messageTemplate, prefix, suffix),e);
        }
        return tmpFile;
    }

    public static String formatMsgInSquareBrackets(String originalMsg) {
        String result = originalMsg;
        Matcher matcher = SQUARE_BRACE.matcher(originalMsg);
        if (!matcher.find()) {
            result = formatString("[{}]", originalMsg);
        }
        return result;
    }

    public static <T> T retrieveObjectFromMultipartFile(ObjectMapper om, MultipartFile file, TypeReference<T> type) throws AppException, IOException {
        T result;
        File inputFile = FileService.getTempFile(file);
        File sourceFile;
        String suffix = StringUtils.substringAfter(inputFile.getName(), ".").toLowerCase();
        switch (suffix) {
            case VariableUtil.ZIP_STANDAND_SUFFIX:
            case VariableUtil.GZIP_ABBR_SUFFIX:
            case VariableUtil.GZIP_STANDAND_SUFFIX:
                // Extract the archive file to a folder in the path where archive file places
                // Delete the new created folder
                File decompressFolder = FileService.decompress(inputFile.getParent(), digitOnlyFormat.format(new Date()), file.getName());
                // Package tgz archive on Mac OS will contain a hidden file. Knock out the hidden file here.
                File[] listOfFiles = decompressFolder.listFiles(f -> !f.isHidden());
                if (listOfFiles == null || listOfFiles.length != 1) {
                    throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_ARCHIVE_MORE_THAN_ONE_FILE, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_FILE_COMMON_ARCHIVE_HAVE_MORE_THAN_ONE_FILE.unifyErrorCode,
                            CommonUtil.formatString("[{}] Archive should have only one file", AppException.ErrorCode.E_API_FILE_COMMON_ARCHIVE_HAVE_MORE_THAN_ONE_FILE.messageTemplate));
                } else {
                    sourceFile = listOfFiles[0];
                }
                log.debug("[retrieveObjectFromMultipartFile] begin to delete file {}", decompressFolder.getPath());
                boolean isFileDeleted = FileUtils.deleteQuietly(decompressFolder);
                log.debug("[retrieveObjectFromMultipartFile] is file {} deleted: {}", decompressFolder.getPath(), isFileDeleted);
                break;
            default:
                sourceFile = inputFile;
        }

        result = om.readValue(sourceFile, type);
        return result;
    }
}
