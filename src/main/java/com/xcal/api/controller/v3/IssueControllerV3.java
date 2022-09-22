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
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.v3.IssueDto;
import com.xcal.api.model.dto.v3.IssueGroupDto;
import com.xcal.api.model.dto.v3.SearchIssueSuggestionDto;
import com.xcal.api.model.payload.IssueGroupCountResponse;
import com.xcal.api.model.payload.IssueGroupCriticalityCountResponse;
import com.xcal.api.model.payload.v3.AssignIssueGroupRequest;
import com.xcal.api.model.payload.v3.SearchIssueGroupRequest;
import com.xcal.api.model.payload.v3.TopCsvCodeRequest;
import com.xcal.api.security.CurrentUser;
import com.xcal.api.security.UserPrincipal;
import com.xcal.api.service.v3.IssueServiceV3;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

}
