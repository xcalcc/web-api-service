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

package com.xcal.api.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.xcal.api.util.MessagesTemplate;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties({"stackTrace", "stackTraceString", "suppressed", "cause"})
@ApiModel(description = "Application Exception")
public class AppException extends Exception {

    public static final String LEVEL_ERROR = "ERROR";
    public static final String LEVEL_WARN = "WARN";
    public static final String LEVEL_INFO = "INFO";

    public static final String ERROR_CODE_BAD_REQUEST = "BAD_REQUEST";
    public static final String ERROR_CODE_DATA_ALREADY_EXIST = "DATA_ALREADY_EXIST";
    public static final String ERROR_CODE_INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String ERROR_CODE_INCORRECT_PARAM = "INCORRECT_PARAM";
    public static final String ERROR_CODE_DATA_NOT_FOUND = "DATA_NOT_FOUND";
    public static final String ERROR_CODE_DATA_INCONSISTENT = "DATA_INCONSISTENT";
    public static final String ERROR_CODE_INVALID_FILE_PATH = "INVALID_FILE_PATH";
    public static final String ERROR_CODE_REPORT_NO_VS = "NO_VS";
    public static final String ERROR_CODE_REPORT_TOO_MANY_VS = "TOO_MANY_VS";
    public static final String ERROR_CODE_DATA_INTEGRITY_VS = "DATA_INTEGRITY_VS";
    public static final String ERROR_CODE_VALUE_IS_BLANK = "BLANK_VALUE";
    public static final String ERROR_CODE_UNAUTHORIZED = "UNAUTHORIZED";
    public static final String ERROR_CODE_INVALID_TOKEN = "INVALID_TOKEN";
    public static final String ERROR_CODE_EXPIRED_TOKEN = "EXPIRED_TOKEN";
    public static final String ERROR_CODE_LICENSE_EXPIRED = "LICENSE_EXPIRED";
    public static final String ERROR_CODE_SOURCE_CODE_NOT_UPLOAD = "SOURCE_CODE_NOT_UPLOAD";
    public static final String ERROR_CODE_ARCHIVE_MORE_THAN_ONE_FILE = "ARCHIVE_MORE_THAN_ONE_FILE";


    final String level;

    final Integer responseCode;

    final String errorCode;

    final String unifyErrorCode;

    final String localizedMessage;

    public AppException(String level, String errorCode, Integer responseCode, String unifyErrorCode, String message, String localizedMessage, Throwable cause) {
        super(message, cause);
        this.level = level;
        this.responseCode = responseCode;
        this.errorCode = errorCode;
        this.unifyErrorCode = unifyErrorCode;
        this.localizedMessage = localizedMessage;
    }

    /**
     * All argument Constructor
     *
     * @param level        errorLevel
     * @param errorCode    errorCode
     * @param responseCode responseCode
     * @param message      message
     * @param cause        cause
     */
    public AppException(String level, String errorCode, Integer responseCode, String unifyErrorCode, String message, Throwable cause) {
        super(message, cause);
        this.level = level;
        this.responseCode = responseCode;
        this.errorCode = errorCode;
        this.unifyErrorCode = unifyErrorCode;
        this.localizedMessage = super.getLocalizedMessage();
    }

    /**
     * No cause Constructor
     *
     * @param level        level
     * @param errorCode    errorCode
     * @param responseCode responseCode
     * @param message      message
     */
    public AppException(String level, String errorCode, Integer responseCode, String unifyErrorCode, String message) {
        super(message);
        this.level = level;
        this.responseCode = responseCode;
        this.errorCode = errorCode;
        this.unifyErrorCode = unifyErrorCode;
        this.localizedMessage = super.getLocalizedMessage();
    }

    /**
     * No Level Constructor, Default Level {@value #LEVEL_ERROR} will be used
     *
     * @param errorCode    errorCode
     * @param responseCode responseCode
     * @param message      message
     * @param cause        cause
     */
    public AppException(String errorCode, Integer responseCode, String unifyErrorCode, String message, Throwable cause) {
        super(message, cause);
        this.level = LEVEL_ERROR;
        this.responseCode = responseCode;
        this.errorCode = errorCode;
        this.unifyErrorCode = unifyErrorCode;
        this.localizedMessage = super.getLocalizedMessage();
    }

    /**
     * Return the stacktrace in string format, make use of {@link ExceptionUtils#getStackTrace(Throwable)}
     *
     * @return stacktrace in string
     */
    public String getStackTraceString() {
        return ExceptionUtils.getStackTrace(this);
    }

    @Override
    public String getLocalizedMessage() {
        if (StringUtils.isNotEmpty(this.localizedMessage)) {
            return this.localizedMessage;
        }
        return super.getLocalizedMessage();
    }

    public enum ErrorCode {
        E_API_VALIDATION_CONSTRAINTS_NOTBLANK("0x00B40338",MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_NOTBLANK),
        E_API_VALIDATION_CONSTRAINTS_NOTNULL("0x00B50338",MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_NOTNULL),
        E_API_VALIDATION_CONSTRAINTS_PATTERN("0x00B60338",MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_PATTERN),
        E_API_VALIDATION_CONSTRAINTS_EMAIL("0x00B70338",MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_EMAIL),
        E_API_VALIDATION_CONSTRAINTS_SIZE("0x00B80338",MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_SIZE),
        E_API_VALIDATION_CONSTRAINTS_PORT("0x00B90338",MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_PORT),
        E_API_VALIDATION_CONSTRAINTS_MIN("0x00BA0338",MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_MIN),

        E_API_COMMON_COMMON_INTERNAL_ERROR("0x80BB013A","${e.api.common.common.internal.error}"),
        E_API_COMMON_COMMON_NOT_IMPLEMENT("0x00BC0338","${e.api.common.common.not.implement}"),
        E_API_COMMON_COMMON_INVALID_CONFIG("0x80BD000B","${e.api.common.common.invalid.config}"),
        E_API_COMMON_COMMON_INVALID_STATUS("0x00BE0338","${e.api.common.common.invalid.status}"),
        E_API_COMMON_DTO_CONVERT_PARAMETER_NOT_EXIST("0x00BF0338","${e.api.common.dto.convert.parameter.not.exist}"),
        E_API_COMMON_DTO_INVALID_CONTENT("0x00C00138","${e.api.common.dto.invalid.content}"),
        E_API_COMMON_MISSING_FILE("0x00C10338","${e.api.common.missing.file}"),
        E_API_EMAIL_COMMON_SENDMAIL("0x80C2013A","${e.api.email.common.sendmail}"),
        E_API_EMAIL_COMMON_UNASSIGNED_ISSUE("0x80C3003B","${e.api.email.common.unassigned.issue}"),
        E_API_EMAIL_PREPARE_FAILED("0x00C40338","${e.api.email.prepare.failed}"),
        E_API_FILE_ADD_ALREADY_EXIST("0x00C50338","${e.api.file.add.already.exist}"),
        E_API_FILE_CHECKINTEGRITY_FAILED("0x00C60338","${e.api.file.checkIntegrity.failed}"),
        E_API_FILE_CHECKSUM_NOT_PARSABLE("0x00C70338","${e.api.file.checksum.not.parsable}"),
        E_API_FILE_CALCULATE_HASH_FAILED("0x01580338","${e.api.file.calculate.hash.failed}"),
        E_API_FILE_COMMON_FILEINFORMATION_ALREADY_EXIST("0x00C80338","${e.api.file.common.fileInformation.already.exist}"),
        E_API_FILE_COMMON_FILEINFORMATION_NOT_FOUND("0x00C90338","${e.api.file.common.fileInformation.not.found}"),
        E_API_FILE_COMMON_INVALID_FORMAT("0x00CA0038","${e.api.file.common.invalid.format}"),
        E_API_FILE_COMMON_UNSUPPORTED_VERSION("0x","${e.api.file.common.unsupported.version}"), //TODO:Raymond, add error code and sql when provided
        E_API_FILE_COMMON_INVALID_TYPE("0x00CB0038","${e.api.file.common.invalid.type}"),
        E_API_FILE_COMMON_INVALID_VALUE("0x00CC0038","${e.api.file.common.invalid.value}"),
        E_API_FILE_COMMON_INVALID_STORAGE_TYPE("0x00CD0038","${e.api.file.common.invalid.storage.type}"),
        E_API_FILE_COMMON_NOT_AVAILABLE("0x00CE0038","${e.api.file.common.not.available}"),
        E_API_FILE_COMMON_NOT_FOUND("0x00CF0038","${e.api.file.common.not.found}"),
        E_API_FILE_COMMON_OBTAIN_FAILED("0x00D00000","${e.api.file.common.obtain.failed}"),
        E_API_FILE_COMMON_CREATE_TEMP_FILE_FAILED("0x00D10000","${e.api.file.common.create.temp.file.failed}"),
        E_API_FILE_COMPRESSFILE_COPYCODE_FAILED("0x00D20000","${e.api.file.compressFile.copyCode.failed}"),
        E_API_FILE_COMPRESSFILE_DECOMPRESS_FAILED("0x80D3011A","${e.api.file.compressFile.decompress.failed}"),
        E_API_FILE_COMPRESSFILE_DELETE_EXISTING_FAILED("0x00D40000","${e.api.file.compressFile.delete.existing.failed}"),
        E_API_FILE_COMPRESSFILE_FAILED("0x00D50000","${e.api.file.compressFile.failed}"),
        E_API_FILE_COMPRESSFILE_FILE_NOT_GENERATED("0x00D60000","${e.api.file.compressFile.file.not.generated}"),
        E_API_FILE_COMPRESSFILE_FILE_OR_DIRECTORY_NOT_FOUND("0x00D70138","${e.api.file.compressFile.file.or.directory.not.found}"),
        E_API_FILE_GETFILESTORAGE_FAILED("0x00D80000","${e.api.file.getFileStorage.failed}"),
        E_API_FILE_UPLOAD_FILE_FAILED("0x80D90112","${e.api.file.upload.file.failed}"),
        E_API_FILE_IMPORT_FILE_FAILED("0x80DA0112","${e.api.file.import.file.failed}"),
        E_API_FILE_IMPORTFILEINFO_ROOT_NOT_FOUND("0x00DB0138","${e.api.file.importFileInfo.root.not.found}"),
        E_API_FILE_IMPORTFILEINFO_PARENT_NOT_FOUND("0x00DC0138","${e.api.file.importFileInfo.parent.not.found}"),
        E_API_FILESTORAGE_ADD_ALREADY_EXIST("0x00DD0038","${e.api.fileStorage.add.already.exist}"),
        E_API_FILESTORAGE_COMMON_NOT_FOUND("0x00DE0038","${e.api.fileStorage.common.not.found}"),
        E_API_GIT_COMMON_CLONE_FAILED("0x80DF010A","${e.api.git.common.clone.failed}"),
        E_API_GIT_COMMON_COMMIT_NOT_FOUND("0x80E0000B","${e.api.git.common.commit.not.found}"),
        E_API_GIT_COMMON_GITLABERROR("0x80E1000B","${e.api.git.common.gitlabError}"),
        E_API_GIT_COMMON_GITURL_MISMATCH("0x8159010A","${e.api.git.common.gitUrl.mismatch}"),
        E_API_GIT_COMMON_GITHUBERROR("0x8148000B","${e.api.git.common.githubError}"),
        E_API_GIT_COMMON_LAST_COMMITID_NOT_FOUND("0x80E2000B","${e.api.git.common.last.commitId.not.found}"),
        E_API_GIT_COMMON_PROJECTID_NOT_BLANK("0x80E3000B","${e.api.git.common.projectId.not.blank}"),
        E_API_GIT_COMMON_PROJECTIDORPATH_NOT_BLANK("0x80E4000B","${e.api.git.common.projectIdOrPath.not.blank}"),
        E_API_GIT_GETRAWFILE_FAILED("0x80E5000B","${e.api.git.getRawFile.failed}"),
        E_API_GIT_GETREPO_FAILED("0x80E6010A","${e.api.git.getRepo.failed}"),
        E_API_GIT_GITHUBPROJECTURL_NOT_BLANK("0x80E7000B","${e.api.git.githubProjectUrl.not.blank}"),
        E_API_GIT_GITURL_NOT_BLANK("0x814F000B", "${e.api.git.gitUrl.not.blank}"),
        E_API_ISSUE_COMMON_INVALID_SEVERITY("0x00E80000","${e.api.issue.common.invalid.severity}"),
        E_API_ISSUE_COMMON_NOT_FOUND("0x00E90038","${e.api.issue.common.not.found}"),
        E_API_ISSUE_UPDATEISSUE_INVALID_ACTION("0x00EA0000","${e.api.issue.updateIssue.invalid.action}"),
        E_API_ISSUE_IMPORTISSUE_INVALID_FILE("0x00EB0338","${e.api.issue.importIssue.invalid.file}"),
        E_API_LICENSE_COMMON_EXPIRED("0x80EC003B","${e.api.license.common.expired}"),
        E_API_LICENSE_COMMON_INVALID_LICENSE("0x80ED013A","${e.api.license.common.invalid.license}"),
        E_API_LICENSE_COMMON_NOT_FOUND("0x80EE013A","${e.api.license.common.not.found}"),
        E_API_LICENSE_UPDATE_PUBLIC_KEY_NOT_FOUND("0x80EF013A","${e.api.license.update.public.key.not.found}"),
        E_API_LICENSE_UPDATE_ENCRYPT_AES_KEY_NOT_FOUND("0x80F0013A","${e.api.license.update.encrypt.aes.key.not.found}"),
        E_API_PROJECT_COMMON_NOT_FOUND("0x00F10038","${e.api.project.common.not.found}"),
        E_API_PROJECT_CREATE_ALREADY_EXIST("0x80F2000B","${e.api.project.create.already.exist}"),
        E_API_PROJECT_UPDATE_INCONSISTENT("0x00F30020","${e.api.project.update.inconsistent}"),
        E_API_PROJECT_INVALID_SCAN_MODE("0x0","${e.api.project.invalid.scan.mode}"), //TODO:Raymond, modify error message value in sql file
        E_API_PROJECTCONFIG_CAN_NOT_UPDATE_IN_SCANNING("0x80F4000B","${e.api.projectConfig.can.not.update.in.scanning}"),
        E_API_PROJECTCONFIG_COMMON_NOT_FOUND("0x80F5000B","${e.api.projectConfig.common.not.found}"),
        E_API_PROJECTCONFIG_CREATE_ALREADY_EXIST("0x80F6000B","${e.api.projectConfig.create.already_exist}"),
        E_API_PROJECTCONFIG_NOT_EXIST("0x00F70038","${e.api.projectConfig.not.exist}"),
        E_API_PROJ_CONFIG_ATTR_CMD_NOT_FOUND("0x01710319","${e.api.proj.config.attr.cmd.not.found}"), //TODO:Raymond, modify error message value in sql file
        E_API_PERFORMANCE_QUERY_DATA_FAILED("0x00F80038","${e.api.performance.query.data.failed}"),
        E_API_PERFORMANCE_COPY_LOG_FILE_FAILED("0x00F90338","${e.api.performance.copy.log.file.failed}"),
        E_API_REPORT_INVALID_REPORT_TYPE("0x0","${e.api.project.invalid.report.type}"), //TODO:Raymond, modify error message value in sql file
        E_API_REPORT_COMMON_COMPILE_REPORT_ERROR("0x80FA0122","${e.api.report.common.compile.report.error}"),
        E_API_REPORT_COMMON_GENERATE_REPORT_ERROR("0x80FB0122","${e.api.report.common.generate.report.error}"),
        E_API_RULE_COMMON_ALREADY_EXIST("0x00FC0000","${e.api.rule.common.already.exist}"),
        E_API_RULE_COMMON_CATEGORY_NULL("0x00FD0000","${e.api.rule.common.category.null}"),
        E_API_RULE_COMMON_CODE_NULL("0x00FE0000","${e.api.rule.common.code.null}"),
        E_API_RULE_COMMON_NAME_NULL("0x00FF0000","${e.api.rule.common.name.null}"),
        E_API_RULE_COMMON_NOT_FOUND("0x01000320","${e.api.rule.common.not.found}"),
        E_API_RULE_COMMON_RULESET_NOT_FOUND("0x01010320","${e.api.rule.common.ruleset.not.found}"),
        E_API_RULE_INVALID_SCAN_ENGINE("0x01020320","${e.api.rule.invalid.scan.engine}"),
        E_API_SCANTASK_ADDSCAN_INVALID_OPERATION("0x8103000B","${e.api.scanTask.addScan.invalid.operation}"),
        E_API_SCANTASK_ADDSCAN_EMPTY_PROJECT_UUID("0x814B000B","${e.api.scanTask.addScan.empty.project.uuid}"),
        E_API_SCANTASK_CALLSCAN_CREATEBODY_FAILED("0x01040308","${e.api.scanTask.callScan.createBody.failed}"),
        E_API_SCANTASK_CALLSCAN_EXECUTE_FAILED("0x81050309","${e.api.scanTask.callScan.execute.failed}"),
        E_API_SCANTASK_CAN_NOT_COMPARE_DIFF_PROJECT("0x01060008","${e.api.scanTask.can.not.compare.diff.project}"),
        E_API_SCANTASK_COMMON_NOT_FOUND("0x01070318","${e.api.scanTask.common.not.found}"),
        E_API_SCANTASK_SUMMARY_DATA_INCONSISTENT("0x01080318","${e.api.scanTask.summary.data.inconsistent}"),
        E_API_SCANTASK_CONSTRUCTSCANPARAM_INVALID_PREPROCESSPATH("0x8109000B","${e.api.scanTask.constructScanParam.invalid.preprocessPath}"),
        E_API_SCANTASK_CONSTRUCTSCANPARAM_INVALID_SCANFILEPATH("0x810A000B","${e.api.scanTask.constructScanParam.invalid.scanFilePath}"),
        E_API_SCANTASK_CONSTRUCTSCANPARAM_INVALID_SOURCESTORAGENAME("0x810B000B","${e.api.scanTask.constructScanParam.invalid.sourceStorageName}"),
        E_API_SCANTASK_CONSTRUCTSCANPARAM_INVALID_TOKEN("0x810C000B","${e.api.scanTask.constructScanParam.invalid.token}"),
        E_API_SCANTASK_PREPAREFORSCAN_ONLY_SUPPORT_UPLOAD("0x010D0008","${e.api.scanTask.prepareForScan.only.support.upload}"),
        E_API_SCANTASK_PREPAREFORSCAN_INVALID_FILESTORAGE_TYPE("0x010E0008","${e.api.scanTask.prepareForScan.invalid.fileStorage.type}"),
        E_API_SCANTASK_UPDATE_SCANCONFIG_FAILED("0x810F0309","${e.api.scanTask.update.scanConfig.failed}"),
        E_API_SCANTASK_UPDATE_INCONSISTENT("0x01100318","${e.api.scanTask.update.inconsistent}"),
        E_API_SCANTASK_WITHOUT_DSR("0x","${e.api.scanTask.without.dsr}"),   //TODO:Raymond, add error code and sql when provided
        E_API_SCANTASK_WITHOUT_COMMIT_ID("0x","${e.api.scanTask.without.commit.id}"),   //TODO:Raymond, add error code and sql when provided
        E_API_SCANTASKSTATUS_COMMON_INVALID_STATUS("0x01110318","${e.api.scanTaskStatus.common.invalid.status}"),
        E_API_SCANTASKSTATUS_COMMON_INVALID_STAGE("0x01120318","${e.api.scanTaskStatus.common.invalid.stage}"),
        E_API_SCANTASKSTATUS_COMMON_NOT_FOUND("0x01130318","${e.api.scanTaskStatus.common.not.found}"),
        E_API_SETTING_ADD_ALREADY_EXIST("0x01140018","${e.api.setting.add.already.exist}"),
        E_API_SETTING_COMMON_NOT_FOUND("0x01150018","${e.api.setting.common.not.found}"),
        E_API_SYSTEM_PING_NOT_AVAILABLE("0x01160000","${e.api.system.ping.not.available}"),
        E_API_USER_AUTH_USERNAME_PASSWORD_NOT_CORRECT("0x8117003B","${e.api.user.auth.username.password.not.correct}"),
        E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE("0x01180000","${e.api.user.common.insufficient.privilege}"),
        E_API_USER_COMMON_LOCKED("0x8119013A","${e.api.user.common.locked}"),
        E_API_USER_COMMON_NOT_FOUND("0x011A0038","${e.api.user.common.not.found}"),
        E_API_USER_COMMON_SUSPENDED("0x811B013A","${e.api.user.common.suspended}"),
        E_API_USER_CREATEUSERS_EXCEEDLICENSENUMBER("0x811C003B","${e.api.user.createUsers.exceedLicenseNumber}"),
        E_API_USER_UPDATEPASSWORD_INCORRECT_PASSWORD("0x811D003B","${e.api.user.updatePassword.incorrect.password}"),
        E_API_USER_VALIDATEUSERS_EMAILEXIST("0x811E003B","${e.api.user.validateUsers.emailExist}"),
        E_API_USER_VALIDATEUSERS_USERNAMEEXIST("0x811F003B","${e.api.user.validateUsers.userNameExist}"),
        E_API_USER_VALIDATEUSERS_ROWERROR("0x8120003B","${e.api.user.validateUsers.rowError}"),
        E_API_USER_VALIDATEUSERS_VALIDATIONFAILED("0x8121003B","${e.api.user.validateUsers.validationFailed}"),
        E_API_USERGROUP_ADDUSERGROUPS_ALREADYEXIST("0x01220038","${e.api.userGroup.addUserGroups.alreadyExist}"),
        E_API_USERGROUP_COMMON_CAN_NOT_DELETE("0x01230038","${e.api.userGroup.common.can.not.delete}"),
        E_API_USERGROUP_COMMON_NOTFOUND("0x01240038","${e.api.userGroup.common.notfound}"),
        E_API_SCANTASK_TERMINATED_BY_USER("0x812B011B","${e.api.scanTask.terminateByUser}"),
        E_API_FILE_COMMON_GET_PATH_FAILED("0x012C0339","${e.api.file.common.get.path.failed}"),
        E_API_USER_UPDATE_VALIDATE_FAIL("0x812D000B","${e.api.user.update.validate.fail}"),
        E_API_PROJECT_UPDATE_VALIDATE_FAIL("0x812E000B","${e.api.project.update.validate.fail}"),
        E_API_USER_VALIDATE_PASSWORD_VALIDATE_FAIL("0x812F003B","${e.api.user.validate.password.validate.fail}"),
        E_API_USERGROUP_CREATE_VALIDATE_FAIL("0x8130003B","${e.api.userGroup.create.validate.fail}"),
        E_API_USER_LOGIN_VALIDATE_FAIL("0x8131003B","${e.api.user.login.validate.fail}"),
        E_API_PROJECT_CREATE_VALIDATE_FAIL("0x8132000B","${e.api.project.create.validate.fail}"),
        E_API_PROJECT_NAME_DUPLICATED("0x81ab0003","${e.api.project.name.duplicated}"),
        E_API_PROJECTCONFIG_CREATE_VALIDATE_FAIL("0x8133000B","${e.api.projectConfig.create.validate.fail}"),
        E_API_PRESET_CREATE_VALIDATE_FAIL("0x8134000B","${e.api.preset.create.validate.fail}"),
        E_API_PRESET_UPDATE_VALIDATE_FAIL("0x8135000B","${e.api.preset.update.validate.fail}"),
        E_API_SCANTASKSTATUS_UPDATE_VALIDATE_FAIL("0x8136031B","${e.api.scanTaskStatus.update.validate.fail}"),
        E_API_PRESET_CREATE_SETTING_VALIDATE_FAIL("0x8137000B","${e.api.preset.create.setting.validate.fail}"),
        E_API_PRESET_UPDATE_SETTING_VALIDATE_FAIL("0x8138000B","${e.api.preset.update.setting.validate.fail}"),
        E_API_EMAILCONFIG_UPDATE_SETTING_VALIDATE_FAIL("0x8139000B","${e.api.emailConfig.update.setting.validate.fail}"),
        E_API_CONTACTUS_VALIDATE_FAIL("0x813A033A","${e.api.contactUs.validate.fail}"),
        E_API_FILE_COMMON_SOURCE_CODE_NOT_UPLOAD("0x814A0109","${e.api.file.common.source.code.not.upload}"),
        E_API_FILE_COMMON_ARCHIVE_HAVE_MORE_THAN_ONE_FILE("0x81460023","${e.api.file.common.archive.moreThanOneFile}"),
        E_API_ISSUE_IMPORTISSUEDIFF_INVALID_FILE("0x014E0338","${e.api.issue.importIssueDiff.invalid.file}");


        public final String unifyErrorCode;
        public final String messageTemplate;

        ErrorCode (String unifyErrorCode, String messageTemplate) {
            this.unifyErrorCode = unifyErrorCode;
            this.messageTemplate = messageTemplate;
        }
    }
}
