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

package com.xcal.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xcal.api.entity.*;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.IssueDto;
import com.xcal.api.model.dto.ProjectConfigDto;
import com.xcal.api.model.dto.ProjectDto;
import com.xcal.api.model.payload.*;
import com.xcal.api.security.CurrentUser;
import com.xcal.api.security.UserPrincipal;
import com.xcal.api.service.*;
import com.xcal.api.util.*;
import io.opentracing.Tracer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/project_service/v2")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(tags = "Project Service")
public class ProjectController {

    @NonNull ProjectService projectService;
    @NonNull UserService userService;
    @NonNull FileService fileService;
    @NonNull OrchestrationService orchestrationService;
    @NonNull ScanTaskService scanTaskService;
    @NonNull ScanFileService scanFileService;
    @NonNull RuleService ruleService;
    @NonNull IssueService issueService;
    @NonNull MeasureService measureService;

    @NonNull ObjectMapper om;
    @NonNull Tracer tracer;

    @Value("${app.projects.recent.count:5}")
    int numberOfRecentProject;

    /**
     * @param newProjectRequest new project object
     * @return ProjectConfigDto
     */
    @PostMapping("/project")
    @ApiOperation(value = "Create project",
            nickname = "createProject",
            notes = "return created project info",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectDto> createProject(@Valid @ValidErrorCode(AppException.ErrorCode.E_API_PROJECT_CREATE_VALIDATE_FAIL) @RequestBody NewProjectRequest newProjectRequest, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[createProject] newProjectRequest: {}, principal username: {}", newProjectRequest, userPrincipal.getUsername());
        if (newProjectRequest.getProjectConfig() == null || newProjectRequest.getScanConfig() == null) {
            throw new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_VALUE_IS_BLANK, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate);
        }

        // prevent duplicate project name
        String projectName = StringUtils.isBlank(newProjectRequest.getProjectName()) ? newProjectRequest.getProjectId() : newProjectRequest.getProjectName();
        if(projectService.isProjectNameExist(projectName)){
            log.error("[createProject] project name already exist: {}", projectName);
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_PROJECT_NAME_DUPLICATED.unifyErrorCode,
                    CommonUtil.formatString("[{}] project name already exist: {}", AppException.ErrorCode.E_API_PROJECT_NAME_DUPLICATED.messageTemplate, projectName));
        }

        ProjectConfig projectConfig = projectService.createProject(newProjectRequest, userPrincipal.getUsername());
        Project project = projectConfig.getProject();
        TracerUtil.setTag(tracer, TracerUtil.Tag.PROJECT_ID, project.getId());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ProjectService.convertProjectToDto(project, projectConfig, om));
    }

    @GetMapping("/projects")
    @ApiOperation(value = "List projects",
            nickname = "listProjects",
            notes = "List project info with paging, default page size 20. admin user lists all the projects, no-admin users list projects created by them",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<ProjectDto>> listProjects(Pageable pageable, @CurrentUser UserPrincipal userPrincipal) {
        log.info("[listProjects] pageable: {}, principal username: {}", pageable, userPrincipal.getUsername());
        Page<Project> projects = projectService.listProject(userPrincipal.getUser(), pageable);
        List<ProjectDto> projectDtos = new ArrayList<>();
        for (Project project : projects.getContent()) {
            ProjectConfig projectConfig = projectService.getLatestActiveProjectConfigByProject(project).orElse(null);
            projectDtos.add(ProjectService.convertProjectToDto(project, projectConfig, om));
        }
        Page<ProjectDto> projectDtoPage = new PageImpl<>(projectDtos, projects.getPageable(), projects.getTotalElements());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(projectDtoPage);
    }

    /**
     * @param projectId project id
     * @return project object
     */
    @GetMapping("/projectId/{projectId}")
    @ApiOperation(value = "Get project by the project id",
            nickname = "getProjectByProjectId",
            notes = "Retrieve the project with the corresponding project id",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectDto> getProjectByProjectId(
            @ApiParam(value = "project id of the project", example = "jsonc0123456")
            @PathVariable String projectId, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getProjectByProjectId] projectId: {}, principal username: {}", projectId, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.PROJECT_ID, projectId);

        Project project = this.projectService.findByProjectId(projectId).orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate, projectId)));
        ProjectConfig projectConfig = this.projectService.getLatestActiveProjectConfigByProject(project).orElse(null);
        this.userService.checkAccessRightOrElseThrow(project, userPrincipal.getUser(), true, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, projectId)));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ProjectService.convertProjectToDto(project, projectConfig, om));
    }


    /**
     * @param id uuid of project
     * @return project object
     */
    @GetMapping("/project/{id}")
    @ApiOperation(value = "Get project by the id",
            nickname = "getProjectById",
            notes = "Retrieve the project with the corresponding uuid",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectDto> getProjectById(
            @ApiParam(value = "uuid of the project", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getProjectById] id: {}, principal username: {}", id, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.PROJECT_ID, id);
        Project project = this.projectService.findById(id).orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate, id)));
        ProjectConfig projectConfig = this.projectService.getLatestActiveProjectConfigByProject(project).orElse(null);
        this.userService.checkAccessRightOrElseThrow(project, userPrincipal.getUser(), true, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, id)));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ProjectService.convertProjectToDto(project, projectConfig, om));
    }

    /**
     * @param id uuid of project
     * @return http status 204 with no content
     */
    @DeleteMapping("/project/{id}")
    @ApiOperation(value = "Delete project by id",
            nickname = "deleteProject",
            notes = "Delete the project with the corresponding uuid",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteProject(
            @ApiParam(value = "uuid of the project", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id, @RequestParam(defaultValue = "Y") String deleteProjectRecord, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[deleteProject] id: {}, deleteProjectRecord: {}, principal username: {}", id, deleteProjectRecord, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.PROJECT_ID, id);
        Project project = this.projectService.findById(id).orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate, id)));
        this.userService.checkAccessRightOrElseThrow(project, userPrincipal.getUser(), false, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, id)));

        this.orchestrationService.deleteAllInProject(project, StringUtils.equalsIgnoreCase(deleteProjectRecord, "Y"));
        return ResponseEntity.noContent().build();
    }

    /**
     * @param id uuid of the project
     * @return updated project object
     */
    @DeleteMapping("/project/{id}/status")
    @ApiOperation(value = "Inactive project by the id",
            nickname = "inactiveProject",
            notes = "Inactive the project with the corresponding uuid",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectDto> inactiveProject(
            @ApiParam(value = "uuid of the project", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[inactiveProject] id: {}, principal username: {}", id, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.PROJECT_ID, id);
        Project project = this.projectService.findById(id).orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate, id)));
        ProjectConfig projectConfig = this.projectService.getLatestActiveProjectConfigByProject(project).orElse(null);
        this.userService.checkAccessRightOrElseThrow(project, userPrincipal.getUser(), false, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, id)));
        this.projectService.inactiveProject(project, userPrincipal.getUsername());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ProjectService.convertProjectToDto(project, projectConfig, om));
    }

    /**
     * @param id      id of the project
     * @param project project information want to update
     * @return the updated project object
     */
    @PutMapping("/project/{id}")
    @ApiOperation(value = "Update project by the id",
            nickname = "updateProject",
            notes = "Update the project with the corresponding uuid",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectDto> updateProject(
            @ApiParam(value = "uuid of the project", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @Valid @ValidErrorCode(AppException.ErrorCode.E_API_PROJECT_UPDATE_VALIDATE_FAIL) @PathVariable UUID id, @Dto(ProjectDto.class) @RequestBody Project project, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[updateProject] id: {}, project: {}, principal username: {}", id, project, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.PROJECT_ID, project.getId());
        if (!id.equals(project.getId())) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_CONFLICT, AppException.ErrorCode.E_API_PROJECT_UPDATE_INCONSISTENT.unifyErrorCode, CommonUtil.formatString("[{}] id: {}, project id: {}", AppException.ErrorCode.E_API_PROJECT_UPDATE_INCONSISTENT.messageTemplate, id, project.getId()));
        }

        // prevent duplicate project name
        String projectName = project.getName();
        if(StringUtils.isNotBlank(projectName) && projectService.isProjectNameExistInOtherProjects(projectName, project.getProjectId())) {
            log.error("[updateProject] project name already exist: {}", projectName);
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_PROJECT_NAME_DUPLICATED.unifyErrorCode,
                        CommonUtil.formatString("[{}] project name already exist: {}", AppException.ErrorCode.E_API_PROJECT_NAME_DUPLICATED.messageTemplate, projectName));
        }

        Project dbProject = this.projectService.findById(id).orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate, id)));
        this.userService.checkAccessRightOrElseThrow(dbProject, userPrincipal.getUser(), false, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, id)));
        Project result = projectService.updateProject(project, userPrincipal.getUsername());
        ProjectConfig projectConfig = this.projectService.getLatestActiveProjectConfigByProject(result).orElse(null);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ProjectService.convertProjectToDto(result, projectConfig, om));
    }

    /**
     * @param projectId projectId of the project
     * @param project project information want to update
     * @return the updated project object
     */
    @PutMapping("/projectId/{projectId}")
    @ApiOperation(value = "Update project by the project id",
            nickname = "updateProject",
            notes = "Update the project with the corresponding project id",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectDto> updateProject(
            @ApiParam(value = "projectId of the project", example = "jsonc0123456")
            @Valid @ValidErrorCode(AppException.ErrorCode.E_API_PROJECT_UPDATE_VALIDATE_FAIL) @PathVariable String projectId, @Dto(ProjectDto.class) @RequestBody Project project, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[updateProject] projectId: {}, project: {}, principal username: {}", projectId, project, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.PROJECT_ID, project.getId());
        if (!projectId.equals(project.getProjectId())) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_CONFLICT, AppException.ErrorCode.E_API_PROJECT_UPDATE_INCONSISTENT.unifyErrorCode, CommonUtil.formatString("[{}] id: {}, project id: {}", AppException.ErrorCode.E_API_PROJECT_UPDATE_INCONSISTENT.messageTemplate, projectId, project.getProjectId()));
        }
        Project dbProject = this.projectService.findByProjectId(projectId).orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate, projectId)));
        this.userService.checkAccessRightOrElseThrow(dbProject, userPrincipal.getUser(), false, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, projectId)));

        // prevent duplicate project name
        String projectName = project.getName();
        if(StringUtils.isNotBlank(projectName) && projectService.isProjectNameExistInOtherProjects(projectName, project.getProjectId())) {
            log.error("[updateProject] project name already exist: {}", projectName);
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_PROJECT_NAME_DUPLICATED.unifyErrorCode,
                    CommonUtil.formatString("[{}] project name already exist: {}", AppException.ErrorCode.E_API_PROJECT_NAME_DUPLICATED.messageTemplate, projectName));
        }

        Project result = projectService.updateProject(project, userPrincipal.getUsername());
        ProjectConfig projectConfig = this.projectService.getLatestActiveProjectConfigByProject(result).orElse(null);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ProjectService.convertProjectToDto(result, projectConfig, om));
    }


    /**
     * @param id of the project
     * @return projectConfig
     */
    @PostMapping("/project/{id}/config")
    @ApiOperation(value = "Create new project config",
            nickname = "createProjectConfig",
            notes = "Create a new project config",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectConfigDto> createProjectConfig(
            @ApiParam(value = "uuid of the project", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id,
            @ApiParam(value = "config of the project")
            @Dto(ProjectConfigDto.class) @Valid @ValidErrorCode(AppException.ErrorCode.E_API_PROJECTCONFIG_CREATE_VALIDATE_FAIL) @RequestBody ProjectConfig projectConfig, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[createProjectConfig] id: {}, projectConfig: {}, principal username: {}", id, projectConfig, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.PROJECT_ID, id);
        ProjectConfig dbProjectConfig = this.projectService.createProjectConfig(id, projectConfig, userPrincipal.getUsername());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ProjectService.convertProjectConfigToDto(om, dbProjectConfig));
    }

    /**
     * @param id project config id
     */
    @GetMapping("/config/{id}")
    @ApiOperation(value = "Get project config",
            nickname = "getProjectConfig",
            notes = "Get project config with corresponding uuid",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectConfigDto> getProjectConfig(
            @ApiParam(value = "uuid of the project config", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getProjectConfig] id: {}, principal username: {}", id, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.PROJECT_ID, id);
        ProjectConfig dbProjectConfig = this.projectService.getProjectConfigById(id).orElseThrow(
                () -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECTCONFIG_NOT_EXIST.unifyErrorCode, CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_PROJECTCONFIG_NOT_EXIST.messageTemplate, id)));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ProjectService.convertProjectConfigToDto(om, dbProjectConfig));
    }

    /**
     * @param id project uuid
     * @return Latest ProjectConfig
     */
    @GetMapping("/project/{id}/config")
    @ApiOperation(value = "Get the latest project config by project uuid",
            nickname = "getProjectConfigByProjectUUID",
            notes = "Retrieve the latest project config with the corresponding project uuid",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectConfigDto> getProjectConfigByProjectUUID(
            @ApiParam(value = "uuid of the project", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getProjectConfigByProjectUUID] project uuid: {}, principal username: {}", id, userPrincipal.getUsername());
        TracerUtil.setTag(tracer, TracerUtil.Tag.PROJECT_ID, id);

        Project project = this.projectService.getProjectById(id).orElseThrow(
                () -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate, id)));
        Optional<ProjectConfig> projectConfigOptional = this.projectService.getLatestActiveProjectConfigByProject(project);
        TracerUtil.setTag(tracer, TracerUtil.Tag.PROJECT_ID, project.getId());
        ResponseEntity<ProjectConfigDto> result;
        result = projectConfigOptional.map(projectConfig ->
                ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(ProjectService.convertProjectConfigToDto(om, projectConfig)))
                .orElse(ResponseEntity.noContent().build());
        return result;
    }

    /**
     * Get project config
     * @param projectId projectId of the project, not the uuid
     * @param configId id of the client config file
     * @param repoAction eg. CI / CD / TRIAL
     * @return Latest ProjectConfig
     */
    @GetMapping("/project/project_id/{projectId}/config")
    @ApiOperation(value = "Get the latest project config by project id",
            nickname = "getProjectConfigByProjectId",
            notes = "Retrieve the latest project config with project id",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectConfigDto> getProjectConfigByProjectId(
            @ApiParam(value = "id of the project", example = "abc", format = "string")
            @PathVariable String projectId,
            @RequestParam(required = false) Integer configId,
            @RequestParam(required = false) String repoAction,
            @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getProjectConfigByProjectId] projectId: {}, configId: {}, repoAction: {}, principal username: {}", projectId, configId, repoAction, userPrincipal.getUsername());

        Project project = this.projectService.getProjectByProjectId(projectId).orElseThrow(
                () -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate, projectId)));
        TracerUtil.setTag(tracer, TracerUtil.Tag.PROJECT_ID, project.getId());
        Optional<ProjectConfig> projectConfigOptional;

        if(configId != null) {
            projectConfigOptional = this.projectService.getLatestProjectConfigByProjectUuidAndConfigId(project.getId(), configId);
        }else if (repoAction != null){
            projectConfigOptional = this.projectService.getLatestProjectConfigByProjectUUIDAndRepoAction(project.getId(), repoAction);
        }else{
            projectConfigOptional = this.projectService.getLatestActiveProjectConfigByProject(project);
        }

        if(projectConfigOptional.isPresent()){
            ProjectConfig pc = projectConfigOptional.get();

            // set new config id if exist
            String serverSideConfigId = pc.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.CONFIG_ID, null);
            if(serverSideConfigId == null){
                Optional<String> largestStringIdOptional = projectService.getLargestIdByProjectUUID(project.getId());
                int newConfigId = 1;
                if(largestStringIdOptional.isPresent()){
                    newConfigId = Integer.valueOf(largestStringIdOptional.get())+1;
                }

                pc.addAttribute(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN,VariableUtil.ProjectConfigAttributeTypeName.CONFIG_ID, String.valueOf(newConfigId));
            }

            // return default repo action if not exist
            String repoActionFromDB = pc.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.REPO_ACTION, null);
            if(repoActionFromDB == null){
                pc.addAttribute(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN,VariableUtil.ProjectConfigAttributeTypeName.REPO_ACTION,VariableUtil.RepoAction.TRIAL.name());
            }

        }

        return projectConfigOptional.map(projectConfig ->
                ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(ProjectService.convertProjectConfigToDto(om, projectConfig)))
                .orElse(ResponseEntity.noContent().build());

    }

    @GetMapping("/configs")
    @Deprecated
    @ApiOperation(value = "List Default project configs",
            nickname = "listDefaultProjectConfigs",
            notes = "List default project configs, administrator right is needed",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProjectConfigDto>> listDefaultProjectConfigs(@CurrentUser UserPrincipal userPrincipal) {
        log.info("[listDefaultProjectConfigs] principal username: {}", userPrincipal.getUsername());
        List<ProjectConfig> defaultProjectConfigs = this.projectService.listDefaultProjectConfigs();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(defaultProjectConfigs.stream().map(config -> ProjectService.convertProjectConfigToDto(om, config)).collect(Collectors.toList()));
    }

    /**
     * @param id uuid of project config
     */
    @DeleteMapping("/config/{id}")
    @ApiOperation(value = "Delete project config by uuid",
            nickname = "deleteProjectConfig",
            notes = "Delete the project config with the corresponding uuid",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteProjectConfig(
            @ApiParam(value = "uuid of the project config", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable UUID id, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[deleteProjectConfig] id: {}, principal username: {}", id, userPrincipal.getUsername());
        this.projectService.deleteProjectConfigByUUID(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/projects/recent")
    @ApiOperation(value = "List recent projects",
            nickname = "listRecentProjects",
            notes = "List recent projects",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProjectDto>> listRecentProjects(@CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[listRecentProjects] principal username: {}", userPrincipal.getUsername());
        Pageable pageable = PageRequest.of(0, numberOfRecentProject);

        Page<Project> projects = projectService.findByCreatedBy(userPrincipal.getUser(), pageable);
        List<ProjectDto> projectDtoList = this.projectService.convertProjectConfigToProjectDto(projects.getContent());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(projectDtoList);
    }

    /**
     * add new preset
     *
     * @param presetRequest the request body of the preset
     * @param userPrincipal user principal
     * @return Project Configuration
     * @throws AppException when invalid json format
     */
    @PostMapping("/config")
    @Deprecated
    @ApiOperation(value = "add preset",
            nickname = "addPreset",
            notes = "add new preset",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectConfigDto> addPreset(@Valid @ValidErrorCode(AppException.ErrorCode.E_API_PRESET_CREATE_VALIDATE_FAIL) @ApiParam(value = "preset request")
                                                      @RequestBody PresetRequest presetRequest, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[addPreset] presetRequest: {}, principal username: {}", presetRequest, userPrincipal.getUsername());
        ProjectConfig projectConfig = projectService.addPreset(presetRequest, userPrincipal.getUsername());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ProjectService.convertProjectConfigToDto(om, projectConfig));
    }

    /**
     * update preset
     *
     * @param presetRequest the request body of the preset
     * @param userPrincipal user principal
     * @return the updated project configuration
     * @throws AppException when the path id not match with the id inside the project config request body
     */
    @PutMapping("/config/{id}")
    @Deprecated
    @ApiOperation(value = "update preset",
            nickname = "updatePreset",
            notes = "update preset",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectConfigDto> updatePreset(@ApiParam(value = "uuid of the project config", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid", required = true) @PathVariable UUID id,
                                                         @Valid @ValidErrorCode(AppException.ErrorCode.E_API_PRESET_UPDATE_VALIDATE_FAIL) @ApiParam(value = "preset request", required = true) @RequestBody PresetRequest presetRequest, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[updatePreset] id: {}, principal username: {}", presetRequest, userPrincipal.getUsername());
        ProjectConfig projectConfig = projectService.updatePreset(presetRequest, userPrincipal.getUser());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ProjectService.convertProjectConfigToDto(om, projectConfig));
    }

    @PostMapping("/upload_info")
    @Deprecated
    @ApiOperation(value = "upload info",
            nickname = "upload info",
            notes = "upload scan results and file info")
    public ResponseEntity<List<IssueDto>> uploadInfo(@ModelAttribute("projectInfoRequest") ProjectInfoRequest projectInfoRequest, Locale locale, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        //Create project
        //Create scanTask
        //Import file info
        //Import v file
        //Update scan summary
        //Decompress source code file, if sourceCodeFileInfoId is not blank
        log.info("[uploadInfo] projectId: {}, sourceCodeFileInfoId: {}, sourceCodePath: {}, buildPath: {}, fileInfo size: {}, scanResult size:{}, principal username: {}", projectInfoRequest.getProjectId(), projectInfoRequest.getSourceCodeFileInfoId(),
                projectInfoRequest.getSourceCodePath(), projectInfoRequest.getBuildPath(), (projectInfoRequest.getFileInfoFile() != null ? projectInfoRequest.getFileInfoFile().getSize() : 0), (projectInfoRequest.getScanResult() != null ? projectInfoRequest.getScanResult().getSize() : 0), userPrincipal.getUsername());

        ProjectConfig projectConfig = projectService.createProject(projectInfoRequest.getProjectId(), projectInfoRequest.getSourceCodePath(), projectInfoRequest.getBuildPath(), userPrincipal.getUsername());
        Project project = projectConfig.getProject();
        TracerUtil.setTag(tracer, TracerUtil.Tag.PROJECT_ID, project.getId());
        log.info("[uploadInfo] project: {}", project);

        ScanTask scanTask = scanTaskService.addScanTask(projectConfig, userPrincipal.getUsername());
        log.info("[uploadInfo] scanTask: {}", scanTask);

        ImportFileInfoRequest importFileInfoRequest;
        try {
            InputStream is = projectInfoRequest.getFileInfoFile().getInputStream();
            log.info("[uploadInfo] file available: {}", is.available());
            importFileInfoRequest = om.readValue(is, ImportFileInfoRequest.class);
        } catch (IOException e) {
            log.warn("Exception while getting ImportFileInfoRequest", e);
            throw new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_FILE_UPLOAD_FILE_FAILED.unifyErrorCode,
                    CommonUtil.formatString("[{}] fileName: {}, isEmpty: {}", AppException.ErrorCode.E_API_FILE_UPLOAD_FILE_FAILED.messageTemplate, projectInfoRequest.getFileInfoFile().getOriginalFilename(), projectInfoRequest.getFileInfoFile().isEmpty()),e);

        }
        scanFileService.saveFileInfo(scanTask, importFileInfoRequest, userPrincipal.getUsername());

        ImportScanResultRequest importScanResultRequest;
        try {
            InputStream is = projectInfoRequest.getScanResult().getInputStream();
            log.info("[uploadInfo] file available: {}", is.available());
            importScanResultRequest = om.readValue(is, ImportScanResultRequest.class);
        } catch (IOException e) {
            throw new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_FILE_UPLOAD_FILE_FAILED.unifyErrorCode,
                    CommonUtil.formatString("[{}] fileName: {}, isEmpty: {}", AppException.ErrorCode.E_API_FILE_UPLOAD_FILE_FAILED.messageTemplate, projectInfoRequest.getScanResult().getOriginalFilename(), projectInfoRequest.getScanResult().isEmpty()),e);

        }
        List<RuleSet> ruleSets = new ArrayList<>();
        for (ImportScanResultRequest.RuleSet ruleSet : importScanResultRequest.getRuleSets()) {
            this.ruleService.getRuleSetByNameAndVersion(ruleSet.getRuleSet(), ruleSet.getRuleSetVersion()).ifPresent(ruleSets::add);
        }
        List<Issue> issues = this.issueService.importIssueToScanTask(scanTask, importScanResultRequest, userPrincipal.getUsername());
        MeasureService.updateScanSummary(scanTask, issues, ruleSets);
        this.scanTaskService.update(scanTask);
        List<IssueDto> issueResult = this.issueService.convertIssuesToDto(issues, locale);

        if (StringUtils.isNotBlank(projectInfoRequest.getSourceCodeFileInfoId())) {
            fileService.decompressFile(UUID.fromString(projectInfoRequest.getSourceCodeFileInfoId()));
        }

        scanTaskService.updateScanTaskStatus(scanTask, ScanTaskStatusLog.Stage.SCAN_COMPLETE,
                ScanTaskStatusLog.Status.COMPLETED, 100.0, null, "upload file info and scan result successful", userPrincipal.getUsername());
        log.info("[uploadInfo] : Finished");

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(issueResult);
    }

    @ApiOperation(
            value = "Update project and project_config and project_config_attribute",
            nickname = "updateProject",
            notes = "return updated project and its config info",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PutMapping("/project")
    public ResponseEntity<ProjectDto> updateProject(
            @Valid @ValidErrorCode(AppException.ErrorCode.E_API_PROJECT_UPDATE_VALIDATE_FAIL) @RequestBody UpdateProjectRequest updateProjectRequest,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        log.info("[updateProject] updateProjectRequest: {}, principal username: {}", updateProjectRequest, userPrincipal.getUsername());

        Optional<Project> projectOptional = Optional.empty();
        if (updateProjectRequest.getId() != null) {
            projectOptional = this.projectService.getProjectById(updateProjectRequest.getId());
        } else if (!StringUtils.isEmpty(updateProjectRequest.getProjectId())) {
            projectOptional = this.projectService.getProjectByProjectId(updateProjectRequest.getProjectId());
        }

        Project project = projectOptional.orElseThrow(() -> new AppException(
                AppException.LEVEL_ERROR,
                AppException.ERROR_CODE_DATA_NOT_FOUND,
                HttpURLConnection.HTTP_NOT_FOUND,
                AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString(
                        "[{}] id: {} or projectId: {}",
                        AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate,
                        updateProjectRequest.getId(),
                        updateProjectRequest.getProjectId()
                )
        ));

        this.userService.checkAccessRightOrElseThrow(
                project,
                userPrincipal.getUser(),
                false,
                () -> new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_UNAUTHORIZED,
                        HttpURLConnection.HTTP_FORBIDDEN,
                        AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                        CommonUtil.formatString(
                                "[{}] id: {} or projectId: {}",
                                AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate,
                                updateProjectRequest.getId(),
                                updateProjectRequest.getProjectId()
                        )
                )
        );


        // prevent duplicate project name
        String projectName = updateProjectRequest.getProjectName();
        if(StringUtils.isNotBlank(projectName) && projectService.isProjectNameExistInOtherProjects(projectName, updateProjectRequest.getProjectId())) {
            log.error("[updateProject] project name already exist: {}", projectName);
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_PROJECT_NAME_DUPLICATED.unifyErrorCode,
                    CommonUtil.formatString("[{}] project name already exist: {}", AppException.ErrorCode.E_API_PROJECT_NAME_DUPLICATED.messageTemplate, projectName));
        }

        Project updatedProject = this.projectService.updateProject(
                Project.builder()
                        .id(project.getId())
                        .projectId(project.getProjectId())
                        .name(updateProjectRequest.getProjectName())
                        .status(updateProjectRequest.getStatus())
                        .build(),
                userPrincipal.getUsername()
        );

        ProjectConfig projectConfig = this.projectService.getLatestActiveProjectConfigByProject(updatedProject).orElse(null);
        if (projectConfig != null) {
            projectConfig = this.projectService.updatePreset(
                    PresetRequest.builder()
                            .id(projectConfig.getId())
                            .name(updateProjectRequest.getProjectConfigName())
                            .projectConfig(updateProjectRequest.getProjectConfig())
                            .scanConfig(updateProjectRequest.getScanConfig())
                            .build(),
                    userPrincipal.getUser()
            );
        } else {
            projectConfig = this.projectService.addPreset(
                    PresetRequest.builder()
                            .name(updateProjectRequest.getProjectConfigName())
                            .projectConfig(updateProjectRequest.getProjectConfig())
                            .scanConfig(updateProjectRequest.getScanConfig())
                            .build(),
                    userPrincipal.getUsername()
            );
        }

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(ProjectService.convertProjectToDto(updatedProject, projectConfig, this.om));
    }

}
