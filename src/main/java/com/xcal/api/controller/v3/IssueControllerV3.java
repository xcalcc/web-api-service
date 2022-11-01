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

package com.xcal.api.controller.v3;

import com.xcal.api.entity.v3.IssueGroupCountDsrRow;
import com.xcal.api.entity.v3.IssueGroupCountRow;
import com.xcal.api.entity.v3.IssueValidation;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.v3.IssueDto;
import com.xcal.api.model.dto.v3.IssueGroupDto;
import com.xcal.api.model.dto.v3.SearchIssueSuggestionDto;
import com.xcal.api.model.payload.IssueGroupCountResponse;
import com.xcal.api.model.payload.IssueGroupCriticalityCountResponse;
import com.xcal.api.model.payload.v3.*;
import com.xcal.api.security.CurrentUser;
import com.xcal.api.security.UserPrincipal;
import com.xcal.api.service.ProjectService;
import com.xcal.api.service.ScanTaskService;
import com.xcal.api.service.v3.IssueServiceV3;
import com.xcal.api.util.CommonUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/issue_service/v3")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(tags = "Issue Service V3")
public class IssueControllerV3 {

    private final ProjectService projectService;
    private final ScanTaskService scanTaskService;
    private final IssueServiceV3 issueService;

    @ApiOperation(
            value = "Search issue group",
            nickname = "searchIssueGroup",
            notes = "return issue group info according to the request parameters. " +
                    "You can get specified issue group info by only provide scan task id and issue group id in request body. " +
                    "Or, you can get many issue groups info by provide scan task id combine with other parameters.",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PostMapping("/search_issue_group")
    public Page<IssueGroupDto> searchIssueGroup(
            @RequestBody SearchIssueGroupRequest searchIssueGroupRequest,
            Pageable pageable,
            Locale locale,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        return this.issueService.searchIssueGroup(searchIssueGroupRequest, pageable, locale, userPrincipal);
    }

    @ApiOperation(
            value = "Get issue group",
            nickname = "getIssueGroup",
            notes = "return issue group info by scan task id and issue group id",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @GetMapping("/scan_task/{scanTaskId}/issue_group/{issueGroupId}")
    public IssueGroupDto getIssueGroup(
            @PathVariable @ApiParam(name = "scanTaskId", value = "scan task id", example = "99aac10b-fff5-44bc-b26d-aa76ba58413e") UUID scanTaskId,
            @PathVariable @ApiParam(name = "issueGroupId", value = "issue group id", example = "C74tP00004") String issueGroupId,
            Locale locale,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        return this.issueService.getIssueGroup(scanTaskId, issueGroupId, locale, userPrincipal);
    }

    @ApiOperation(
            value = "Get issue list",
            nickname = "getIssueList",
            notes = "return issue info list by scan task id and issue group id",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @GetMapping("/scan_task/{scanTaskId}/issue_group/{issueGroupId}/issues")
    public Page<IssueDto> getIssueList(
            @PathVariable @ApiParam(name = "scanTaskId", value = "scan task id", example = "99aac10b-fff5-44bc-b26d-aa76ba58413e") UUID scanTaskId,
            @PathVariable @ApiParam(name = "issueGroupId", value = "issue group id", example = "C74tP00004") String issueGroupId,
            Pageable pageable,
            Locale locale,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        return this.issueService.getIssueList(scanTaskId, issueGroupId, pageable, locale, userPrincipal);
    }

    @ApiOperation(
            value = "Assign issue group",
            nickname = "assignIssueGroup",
            notes = "Assign issue group to specified user",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PostMapping("/issue_group/{issueGroupId}/user/{userId}")
    public IssueGroupDto assignIssueGroup(
            @PathVariable @ApiParam(name = "issueGroupId", value = "issue group id", example = "C74tP00004") String issueGroupId,
            @PathVariable @ApiParam(name = "userId", value = "user id", example = "99aac10b-fff5-44bc-b26d-aa76ba58413e")UUID userId,
            Locale locale,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        return this.issueService.assignIssueGroup(issueGroupId, userId, locale, userPrincipal);
    }

    @ApiOperation(
            value = "Batch assign issue groups to users",
            nickname = "batchAssignIssueGroupsToUsers",
            notes = "Batch assign issue groups to users by providing a list of user id and issue group in request parameter",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PostMapping("/issue_groups/users")
    public ResponseEntity<Void> batchAssignIssueGroupsToUsers(
            @RequestBody AssignIssueGroupRequest assignIssueGroupRequest,
            Locale locale,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        this.issueService.assignIssueGroupsToUsers(assignIssueGroupRequest, locale, userPrincipal);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(
            value = "Get issue group criticality count",
            nickname = "getIssueGroupCriticalityCount",
            notes = "return issue group criticality count",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PostMapping("/issue_group_criticality_count")
    public IssueGroupCriticalityCountResponse getIssueGroupCriticalityCount(
            @RequestBody SearchIssueGroupRequest searchIssueGroupRequest,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        return this.issueService.getIssueGroupCriticalityCount(searchIssueGroupRequest, userPrincipal);
    }

    @ApiOperation(
            value = "Get issue group count",
            nickname = "getIssueGroupCount",
            notes = "Get issue group count",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PostMapping("/issue_group_count")
    public IssueGroupCountResponse getIssueGroupCount(
            @RequestBody SearchIssueGroupRequest searchIssueGroupRequest,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        return this.issueService.getIssueGroupCount(searchIssueGroupRequest, userPrincipal);
    }


    @ApiOperation(
            value = "Get top csv codes",
            nickname = "getTopCsvCodes",
            notes = "return top rule code and corresponding number of issues based on the request parameters",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PostMapping("/top_csv_codes")
    public List<IssueGroupCountRow> getTopCsvCodes(
            @RequestBody TopCsvCodeRequest topCsvCodeRequest,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        return this.issueService.getTopCsvCodes(topCsvCodeRequest);
    }

    @ApiOperation(
            value = "Get issue group count for dsr",
            nickname = "getDsrTopCsvCodes",
            notes = "return new, existing and fixed issue group count",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PostMapping("/top_csv_codes/dsr")
    public IssueGroupCountDsrRow getDsrTopCsvCodes(
            @RequestBody TopCsvCodeRequest topCsvCodeRequest,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        return this.issueService.getTopCsvCodesDsr(topCsvCodeRequest);
    }


    @ApiOperation(
            value = "Search issue group suggestion",
            nickname = "searchIssueGroupSuggestion",
            notes = "Search issue group suggestion",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PostMapping("/search-suggest")
    public Page<SearchIssueSuggestionDto> searchIssueGroupSuggestion(
            @RequestBody SearchIssueGroupRequest searchIssueGroupRequest,
            Pageable pageable,
            Locale locale,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        if(StringUtils.isBlank(searchIssueGroupRequest.getSearchValue())){
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_VALIDATION_CONSTRAINTS_NOTBLANK.unifyErrorCode, AppException.ErrorCode.E_API_VALIDATION_CONSTRAINTS_NOTBLANK.messageTemplate);
        }
        return this.issueService.searchIssueGroupSuggestion(searchIssueGroupRequest, pageable, locale, userPrincipal);
    }

    @ApiOperation(
            value = "Add issue validation",
            nickname = "addIssueValidation",
            notes = "Add issue validation",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PostMapping("/issue_validation")
    public ResponseEntity<IssueValidation> addIssueValidation(
            @RequestBody AddIssueValidationRequest addIssueValidationRequest,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        log.info("[addIssueValidation] addIssueValidationRequest: {}, username: {}", addIssueValidationRequest, userPrincipal.getUsername());

        if(!EnumUtils.isValidEnumIgnoreCase(IssueValidation.Type.class, addIssueValidationRequest.getType())) {
            log.error("[addIssueValidation] valid type values: CUSTOM, DEFAULT. current type value: {}", addIssueValidationRequest.getType());
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                    CommonUtil.formatString("[{}] type: {} ", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate, addIssueValidationRequest.getType()));
        }
        if(!EnumUtils.isValidEnumIgnoreCase(IssueValidation.Action.class, addIssueValidationRequest.getAction())) {
            log.error("[addIssueValidation] valid action values: UNDECIDED, IGNORE, TP, FP. current action value: {}", addIssueValidationRequest.getAction());
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                    CommonUtil.formatString("[{}] action: {} ", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate, addIssueValidationRequest.getAction()));
        }
        if(!EnumUtils.isValidEnumIgnoreCase(IssueValidation.Scope.class, addIssueValidationRequest.getScope())) {
            log.error("[addIssueValidation] valid scope values: PROJECT, USER, GLOBAL. current scope value: {}", addIssueValidationRequest.getScope());
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                    CommonUtil.formatString("[{}] scope: {} ", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate, addIssueValidationRequest.getScope()));
        }

        if(EnumUtils.getEnumIgnoreCase(IssueValidation.Type.class, addIssueValidationRequest.getType()) == IssueValidation.Type.DEFAULT) {
            log.info("[addIssueValidation] when type is DEFAULT, valid project id and scan task id should always be provided.");
            if(addIssueValidationRequest.getProjectId() == null) {
                log.error("[addIssueValidation] when type is DEFAULT, projectId must be provided");
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                        CommonUtil.formatString("[{}] projectId must be provided", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate));
            }
            this.projectService.findById(addIssueValidationRequest.getProjectId())
                    .orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                            CommonUtil.formatString("[{}] project cannot be found, projectId: {}", AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate, addIssueValidationRequest.getProjectId())));

            if(addIssueValidationRequest.getScanTaskId() == null) {
                log.error("[addIssueValidation] when type is DEFAULT, scan task id must be provided");
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                        CommonUtil.formatString("[{}] scanTaskId must be provided", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate));
            }
            this.scanTaskService.findById(addIssueValidationRequest.getScanTaskId())
                    .orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode,
                            CommonUtil.formatString("[{}] scan task cannot be found, scanTaskId: {}", AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate, addIssueValidationRequest.getScanTaskId())));

            log.info("[addIssueValidation] when type is DEFAULT and the issue contains valid rule code, file path, function name, variable name attributes, all these attributes will be used as the key to match issues.");
            // rule code and file path will never be blank, while function name and variable name maybe blank.
            if(StringUtils.isBlank(addIssueValidationRequest.getRuleCode())) {
                log.error("[addIssueValidation] when type is DEFAULT, rule code should not be blank");
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                        CommonUtil.formatString("[{}] when type is DEFAULT, rule code must not be blank", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate));
            }
            if(StringUtils.isBlank(addIssueValidationRequest.getFilePath())) {
                log.error("[addIssueValidation] when type is DEFAULT, file path should not be blank");
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                        CommonUtil.formatString("[{}] when type is DEFAULT, file path must not be blank", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate));
            }
        } else {
            // CUSTOM
            log.info("[addIssueValidation] when type is CUSTOM, action must be IGNORE and at least one of rule code/file path/function name/variable name/line number must be provided");
            if(EnumUtils.getEnumIgnoreCase(IssueValidation.Action.class, addIssueValidationRequest.getAction()) != IssueValidation.Action.IGNORE) {
                log.error("[addIssueValidation] when type is CUSTOM, action must be IGNORE, action: {}", addIssueValidationRequest.getAction());
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                        CommonUtil.formatString("[{}] when type is CUSTOM, action must be IGNORE", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate));
            }
            if(StringUtils.isBlank(addIssueValidationRequest.getRuleCode()) &&
                StringUtils.isBlank(addIssueValidationRequest.getFilePath()) &&
                StringUtils.isBlank(addIssueValidationRequest.getFilePath()) &&
                StringUtils.isBlank(addIssueValidationRequest.getFunctionName()) &&
                StringUtils.isBlank(addIssueValidationRequest.getVariableName()) &&
                    (addIssueValidationRequest.getLineNumber() == null)) {
                log.error("[addIssueValidation] when type is CUSTOM, at least one of rule code/file path/function name/variable name/line number must be provided: {}", addIssueValidationRequest);
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                        CommonUtil.formatString("[{}] when type is CUSTOM, at least one of rule code/file path/function name/variable name/line number must be provided", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate));
            }
        }

        IssueValidation issueValidation = issueService.addIssueValidation(addIssueValidationRequest, userPrincipal.getUsername());

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(issueValidation);
    }


    @ApiOperation(
            value = "List issue validations",
            nickname = "ListIssueValidations",
            notes = "List issue validations",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @GetMapping("/issue_validations")
    public ResponseEntity<Page<IssueValidation>> ListIssueValidations(
            Pageable pageable,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        log.info("[ListIssueValidations] pageable: {}, username: {}", pageable, userPrincipal.getUsername());
        Page<IssueValidation> issueValidations = issueService.listIssueValidations(pageable, userPrincipal.getUsername());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(issueValidations);
    }

    @ApiOperation(
            value = "Search issue validations",
            nickname = "SearchIssueValidations",
            notes = "Search issue validations",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PostMapping("/search_issue_validations")
    public ResponseEntity<Page<IssueValidation>> SearchIssueValidations(
            @RequestBody SearchIssueValidationRequest searchIssueValidationRequest,
            Pageable pageable,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        log.info("[SearchIssueValidations] searchIssueValidationRequest: {}, pageable: {}, username: {}", searchIssueValidationRequest, pageable, userPrincipal.getUsername());
        if(StringUtils.isNotBlank(searchIssueValidationRequest.getType())) {
            if (!EnumUtils.isValidEnumIgnoreCase(IssueValidation.Type.class, searchIssueValidationRequest.getType())) {
                log.error("[SearchIssueValidations] valid type values: CUSTOM, DEFAULT. current type value: {}", searchIssueValidationRequest.getType());
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                        CommonUtil.formatString("[{}] type: {} ", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate, searchIssueValidationRequest.getType()));
            }
        }

        if(StringUtils.isNotBlank(searchIssueValidationRequest.getAction())) {
            if (!EnumUtils.isValidEnumIgnoreCase(IssueValidation.Action.class, searchIssueValidationRequest.getAction())) {
                log.error("[SearchIssueValidations] valid action values: UNDECIDED, IGNORE, TP, FP. current action value: {}", searchIssueValidationRequest.getAction());
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                        CommonUtil.formatString("[{}] action: {} ", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate, searchIssueValidationRequest.getAction()));
            }
        }
        if(StringUtils.isNotBlank(searchIssueValidationRequest.getScope())) {
            if (!EnumUtils.isValidEnumIgnoreCase(IssueValidation.Scope.class, searchIssueValidationRequest.getScope())) {
                log.error("[SearchIssueValidations] valid scope values: PROJECT, USER, GLOBAL. current scope value: {}", searchIssueValidationRequest.getScope());
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                        CommonUtil.formatString("[{}] scope: {} ", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate, searchIssueValidationRequest.getScope()));
            }
        }
        Page<IssueValidation> issueValidations = issueService.searchIssueValidations(searchIssueValidationRequest, pageable, userPrincipal.getUsername());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(issueValidations);

    }

    @ApiOperation(
            value = "Update issue validation",
            nickname = "updateIssueValidation",
            notes = "Update issue validation",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PutMapping("/issue_validation/{id}")
    public ResponseEntity<IssueValidation> updateIssueValidation(
            @ApiParam(value = "id of the issueValidation", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id,
            @RequestBody UpdateIssueValidationRequest updateIssueValidationRequest,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        log.info("[updateIssueValidation] id: {}, updateIssueValidationRequest: {}, username: {}", id, updateIssueValidationRequest, userPrincipal.getUsername());
        if(!id.equals(updateIssueValidationRequest.getId())) {
            log.error("[updateIssueValidation] path variable should has same value with id in request body");
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_CONFLICT, AppException.ErrorCode.E_API_PROJECT_UPDATE_INCONSISTENT.unifyErrorCode,
                    CommonUtil.formatString("[{}] path variable id should has same value with id in request body", AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.messageTemplate));
        }

        if(StringUtils.isBlank(updateIssueValidationRequest.getAction()) && StringUtils.isBlank(updateIssueValidationRequest.getScope())) {
            log.error("[updateIssueValidation] either action or scope must has a value");
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                    CommonUtil.formatString("[{}] either action or scope must has a value", AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.messageTemplate));
        }

        if(StringUtils.isNotBlank(updateIssueValidationRequest.getAction())) {
            if (!EnumUtils.isValidEnumIgnoreCase(IssueValidation.Action.class, updateIssueValidationRequest.getAction())) {
                log.error("[updateIssueValidation] valid action values: UNDECIDED, IGNORE, TP, FP. current action value: {}", updateIssueValidationRequest.getAction());
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                        CommonUtil.formatString("[{}] action: {} ", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate, updateIssueValidationRequest.getAction()));
            }
        }
        if(StringUtils.isNotBlank(updateIssueValidationRequest.getScope())) {
            if (!EnumUtils.isValidEnumIgnoreCase(IssueValidation.Scope.class, updateIssueValidationRequest.getScope())) {
                log.error("[updateIssueValidation] valid scope values: PROJECT, USER, GLOBAL. current scope value: {}", updateIssueValidationRequest.getScope());
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                        CommonUtil.formatString("[{}] scope: {} ", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate, updateIssueValidationRequest.getScope()));
            }
        }

        IssueValidation issueValidation = issueService.updateIssueValidation(id, updateIssueValidationRequest, userPrincipal.getUsername());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(issueValidation);
    }

    @ApiOperation(
            value = "Delete issue validation",
            nickname = "deleteIssueValidation",
            notes = "Delete issue validation",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @DeleteMapping("/issue_validation/{id}")
    public ResponseEntity<Void> deleteIssueValidation(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        log.info("[deleteIssueValidation] id: {}, username: {}", id, userPrincipal.getUsername());
        issueService.deleteIssueValidation(id, userPrincipal.getUsername());
        return ResponseEntity.noContent().build();
    }


}
