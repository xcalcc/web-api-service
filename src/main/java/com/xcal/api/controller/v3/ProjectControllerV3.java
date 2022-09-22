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

import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.v3.ProjectSummaryDto;
import com.xcal.api.model.payload.FirstTimeScanResponse;
import com.xcal.api.security.CurrentUser;
import com.xcal.api.security.UserPrincipal;
import com.xcal.api.service.v3.ProjectServiceV3;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/project_service/v3")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(tags = "Project Service V3")
public class ProjectControllerV3 {

	private final ProjectServiceV3 projectService;

	@ApiOperation(
			value = "Delete project by id",
			nickname = "deleteProject",
			notes = "Delete the project with the corresponding uuid",
			consumes = MediaType.APPLICATION_JSON_VALUE
	)
	@DeleteMapping("/project/{id}")
	public ResponseEntity<Void> deleteProject(
			@PathVariable @ApiParam(name = "id", value = "project uuid", example = "99aac10b-fff5-44bc-b26d-aa76ba58413e") UUID id,
			@CurrentUser UserPrincipal userPrincipal
	) throws AppException {
		this.projectService.deleteProject(id, userPrincipal);
		return ResponseEntity.noContent().build();
	}

	@ApiOperation(
			value = "Get project summary list",
			nickname = "getProjectSummaryList",
			notes = "Get project summary list",
			consumes = MediaType.APPLICATION_JSON_VALUE
	)
	@GetMapping("/project_summaries")
	public Page<ProjectSummaryDto> getProjectSummaryList(
			Pageable pageable,
			@CurrentUser UserPrincipal userPrincipal
	) throws AppException {
		return this.projectService.getProjectSummaryList(pageable, userPrincipal);
	}


	@ApiOperation(
			value = "check whether this is first time scan for project by id",
			nickname = "getFirstTimeScan",
			notes = "return the first time scan status for project by its uuid",
			consumes = MediaType.APPLICATION_JSON_VALUE
	)
	@GetMapping("/is_first_scan")
	public ResponseEntity<FirstTimeScanResponse> getFirstTimeScan(
			@RequestParam("id") @ApiParam(name = "id", value = "project uuid", example = "99aac10b-fff5-44bc-b26d-aa76ba58413e") UUID id,
			@CurrentUser UserPrincipal userPrincipal
	) throws AppException {
		FirstTimeScanResponse firstTimeScanResponse=FirstTimeScanResponse.builder()
				.isFirstTime(this.projectService.getFirstTimeScan(id, userPrincipal))
				.build();
		return ResponseEntity.ok().body(firstTimeScanResponse);
	}

}
