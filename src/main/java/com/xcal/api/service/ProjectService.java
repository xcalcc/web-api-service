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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xcal.api.dao.ProjectSummaryDao;
import com.xcal.api.entity.*;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.ProjectConfigDto;
import com.xcal.api.model.dto.ProjectDto;
import com.xcal.api.model.payload.NewProjectRequest;
import com.xcal.api.model.payload.PresetRequest;
import com.xcal.api.repository.*;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.MessagesTemplate;
import com.xcal.api.util.VariableUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProjectService {
    public static final String DEFAULT_CONFIG_NAME = "default";
    public static final String SCAN_MODE = "scanMode";

    @NonNull UserService userService;
    @NonNull ProjectRepository projectRepository;
    @NonNull ProjectConfigRepository projectConfigRepository;
    @NonNull ProjectConfigAttributeRepository projectConfigAttributeRepository;
    @NonNull ObjectMapper om;
    @NonNull ScanTaskRepository scanTaskRepository;
    @NonNull ProjectSummaryDao projectSummaryDao;

    private ProjectConfig createPresetProjectConfig(NewProjectRequest newProjectRequest, List<ProjectConfigAttribute> projectConfigAttributes, String currentUsername) {
        log.debug("[createPresetProjectConfig] newProjectRequest: {}, projectConfigAttributes, size: {}, currentUsername: {}", newProjectRequest, projectConfigAttributes == null ? 0 : projectConfigAttributes.size(), currentUsername);
        Date now = new Date();
        List<ProjectConfigAttribute> projectConfigAttributeList = new ArrayList<>();
        ProjectConfig defaultProjectConfig = ProjectConfig.builder().name(newProjectRequest.getConfigName())
                .attributes(projectConfigAttributeList)
                .status(ProjectConfig.Status.ACTIVE)
                .createdBy(currentUsername)
                .createdOn(now)
                .modifiedBy(currentUsername)
                .modifiedOn(now).build();
        projectConfigAttributeList.addAll(new ArrayList<>(projectConfigAttributes.stream().filter(attr -> VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN == attr.getType()).map(
                attribute -> ProjectConfigAttribute.builder()
                        .projectConfig(defaultProjectConfig)
                        .type(attribute.getType())
                        .name(attribute.getName())
                        .value(attribute.getValue()).build()).collect(Collectors.toList())));

        return defaultProjectConfig;
    }

    // Return projectConfig which also have the project object.
    public ProjectConfig createProject(NewProjectRequest newProjectRequest, String currentUsername) throws AppException {
        log.debug("[createProject] newProjectRequest: {}, currentUsername: {}", newProjectRequest, currentUsername);
        Optional<Project> optionalProject = this.projectRepository.findByProjectId(newProjectRequest.getProjectId());
        if (optionalProject.isPresent()) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_ALREADY_EXIST, HttpURLConnection.HTTP_CONFLICT, AppException.ErrorCode.E_API_PROJECT_CREATE_ALREADY_EXIST.unifyErrorCode, CommonUtil.formatString("[{}] projectId: {}", AppException.ErrorCode.E_API_PROJECT_CREATE_ALREADY_EXIST.messageTemplate, newProjectRequest.getProjectId()));
        }

        //If project name is blank, use projectId as its project name.
        String scanModeParam = newProjectRequest.getScanConfig().get(SCAN_MODE);
        Date now = new Date();
        Project project = Project.builder().projectId(newProjectRequest.getProjectId())
                .name(StringUtils.isBlank(newProjectRequest.getProjectName()) ? newProjectRequest.getProjectId() : newProjectRequest.getProjectName())
                .status(Project.Status.ACTIVE)
                .scanMode(getScanModeFromParam(scanModeParam))
                .createdBy(currentUsername)
                .createdOn(now)
                .modifiedBy(currentUsername)
                .modifiedOn(now).build();

        Project dbProject = this.projectRepository.save(project);
        List<ProjectConfigAttribute> projectConfigAttributes = new ArrayList<>();

        ProjectConfig projectConfig = ProjectConfig.builder().name(StringUtils.isBlank(newProjectRequest.getConfigName()) ? DEFAULT_CONFIG_NAME : newProjectRequest.getConfigName())
                .project(dbProject)
                .attributes(projectConfigAttributes)
                .status(ProjectConfig.Status.ACTIVE)
                .createdBy(currentUsername)
                .createdOn(now)
                .modifiedBy(currentUsername)
                .modifiedOn(now).build();
        for (Map.Entry<String, String> projectAttributeEntry : newProjectRequest.getProjectConfig().entrySet()) {
            if (StringUtils.isNotBlank(projectAttributeEntry.getValue())) {
                projectConfigAttributes.add(ProjectConfigAttribute.builder()
                        .projectConfig(projectConfig)
                        .type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name(projectAttributeEntry.getKey())
                        .value(projectAttributeEntry.getValue())
                        .build());
            }
        }
        for (Map.Entry<String, String> scanAttributeEntry : newProjectRequest.getScanConfig().entrySet()) {
            if (StringUtils.isNotBlank(scanAttributeEntry.getValue())) {
                projectConfigAttributes.add(ProjectConfigAttribute.builder()
                        .projectConfig(projectConfig)
                        .type(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN)
                        .name(scanAttributeEntry.getKey())
                        .value(scanAttributeEntry.getValue())
                        .build());
            }
        }
        ProjectConfig result = this.projectConfigRepository.save(projectConfig);

        //If configName is not blank, save a preset projectConfig
        if (StringUtils.isNotBlank(newProjectRequest.getConfigName())) {
            ProjectConfig defaultProjectConfig = createPresetProjectConfig(newProjectRequest, projectConfigAttributes, currentUsername);
            this.projectConfigRepository.save(defaultProjectConfig);
        }

        return result;
    }

    public boolean isProjectNameExist(String projectName) {
        log.debug("[isProjectNameExist] projectName: {}", projectName);
        return this.projectRepository.existsByName(projectName);
    }

    public boolean isProjectNameExistInOtherProjects(String projectName, String projectId) {
        log.debug("[isProjectNameExistInOtherProjects] projectName: {}, projectId: {}", projectName, projectId);
        return this.projectRepository.existsByNameAndProjectIdNot(projectName, projectId);
    }
    public Optional<Project> findByProjectId(String projectId) {
        log.debug("[findByProjectId] projectId: {}", projectId);
        return this.projectRepository.findByProjectId(projectId);
    }

    public Optional<Project> findById(UUID id) {
        log.debug("[findById] id: {}", id);
        return this.projectRepository.findById(id);
    }

    public Page<Project> findAll(Pageable pageable) {
        log.debug("[findAll] pageable: {}", pageable);
        return projectRepository.findAll(pageable);
    }

    public List<Project> findAll() {
        log.debug("[findAll]");
        return projectRepository.findAll();
    }

    public void deleteProject(UUID id, String currentUsername) throws AppException {
        log.debug("[deleteProject] id: {}", id);
        Optional<Project> optionalProject = this.projectRepository.findByIdAndCreatedBy(id, currentUsername);
        Project project = optionalProject.orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate, id)));
        this.deleteProject(project);
    }

    public void deleteProject(Project project) {
        log.debug("[deleteProject] project: {}", project);
        this.projectRepository.delete(project);
        this.projectConfigRepository.deleteByProject(project);
    }

    public Project updateProject(Project project, String currentUsername) throws AppException {
        log.debug("[updateProject] project: {}, currentUsername: {}", project, currentUsername);
        Project result;
        Optional<Project> projectOptional = null;
        if(project.getId() != null){
            projectOptional = this.projectRepository.findById(project.getId());
        }else if(project.getProjectId() != null){
            projectOptional = this.projectRepository.findByProjectId(project.getProjectId());
        }
        Project dbProject = projectOptional.orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate, project.getId())));

        if (!StringUtils.equals(dbProject.getProjectId(), project.getProjectId())) {
            throw new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_INCONSISTENT, HttpURLConnection.HTTP_CONFLICT, AppException.ErrorCode.E_API_PROJECT_UPDATE_INCONSISTENT.unifyErrorCode,
                    CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_PROJECT_UPDATE_INCONSISTENT.messageTemplate, project.getProjectId()));
        } else {
            if (StringUtils.isNotBlank(project.getName())) {
                dbProject.setName(project.getName());
            }
            if (project.getStatus() != null) {
                dbProject.setStatus(project.getStatus());
            }
            if (project.getCicdFsmState() != null) {
                dbProject.setCicdFsmState(project.getCicdFsmState());
            }
            if(project.getRetentionNum() != null) {
                if ((project.getRetentionNum() >= 0) && (project.getRetentionNum() <= Integer.MAX_VALUE)) {
                    dbProject.setRetentionNum(project.getRetentionNum());
                } else {
                    log.error("[updateProject] the value of retentionNum should between 0 and the max value of int");
                    throw new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.unifyErrorCode,
                            CommonUtil.formatString("[{}] invalid retentionNum value: {}", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_CONFIG.messageTemplate, project.getRetentionNum()));
                }
            }
            dbProject.setModifiedBy(currentUsername);
            dbProject.setModifiedOn(new Date());
            result = this.projectRepository.save(dbProject);
        }
        return result;
    }

    public Project inactiveProject(Project project, String currentUsername) throws AppException {
        log.debug("[inactiveProject] project id: {}, currentUsername: {}", project.getId(), currentUsername);
        return this.updateProjectStatus(project, Project.Status.INACTIVE, currentUsername);
    }

    public Project updateProjectStatus(Project project, Project.Status status, String currentUsername) throws AppException {
        log.debug("[updateProjectStatus] project id: {}, status: {}, currentUsername: {}", project.getId(), status, currentUsername);
        Project result;
        if (status != null) {
            project.setStatus(status);
            project.setModifiedBy(currentUsername);
            project.setModifiedOn(new Date());
            result = this.projectRepository.save(project);
        } else {
            throw new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_STATUS.unifyErrorCode,
                    CommonUtil.formatString("[{}] status is null", AppException.ErrorCode.E_API_COMMON_COMMON_INVALID_STATUS.messageTemplate));
        }
        return result;
    }

    public ProjectConfig createProjectConfig(UUID projectUUID, ProjectConfig projectConfig, String currentUsername) throws AppException {
        log.debug("[createProjectConfig] projectUUID: {}, projectConfig: {}, currentUsername: {}", projectUUID, projectConfig, currentUsername);
        Optional<Project> projectOptional = this.projectRepository.findById(projectUUID);
        Project project = projectOptional.orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate, projectUUID)));

        List<ProjectConfig> projectConfigs = this.projectConfigRepository.findByProjectAndName(project, projectConfig.getName());
        for (ProjectConfig existProjectConfig : projectConfigs) {
            if (Arrays.asList(ProjectConfig.Status.ACTIVE, ProjectConfig.Status.PENDING).contains(existProjectConfig.getStatus())) {
                throw new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_ALREADY_EXIST, HttpURLConnection.HTTP_CONFLICT, AppException.ErrorCode.E_API_PROJECTCONFIG_CREATE_ALREADY_EXIST.unifyErrorCode,
                        CommonUtil.formatString("[{}] id: {}, project: {}, configName: {}, status: {}", AppException.ErrorCode.E_API_PROJECTCONFIG_CREATE_ALREADY_EXIST.messageTemplate, existProjectConfig.getId(), existProjectConfig.getProject(), existProjectConfig.getName(), existProjectConfig.getStatus()));
            }
        }

        Date now = new Date();
        if (StringUtils.isBlank(projectConfig.getName())) {
            projectConfig.setName(DEFAULT_CONFIG_NAME);
        }
        projectConfig.setProject(project);
        projectConfig.setStatus(ProjectConfig.Status.ACTIVE);
        projectConfig.setCreatedBy(currentUsername);
        projectConfig.setCreatedOn(now);
        projectConfig.setModifiedBy(currentUsername);
        projectConfig.setModifiedOn(now);

        projectConfig = this.projectConfigRepository.saveAndFlush(projectConfig);
        return projectConfig;
    }

    public ProjectConfig cloneProjectConfigWithNewAttribute(ProjectConfig projectConfig, List<ProjectConfigAttribute> attributes, ProjectConfig.Status status, String currentUsername) {
        log.debug("[cloneProjectConfigWithNewAttribute] projectConfig: {}, attributes:{}, status: {}, currentUsername: {}", projectConfig, attributes, status, currentUsername);

        Date now = new Date();
        ProjectConfig newProjectConfig = ProjectConfig.builder()
                .name(projectConfig.getName())
                .project(projectConfig.getProject())
                .status(status)
                .createdBy(currentUsername)
                .createdOn(now)
                .modifiedBy(currentUsername)
                .modifiedOn(now)
                .build();

        Map<String, ProjectConfigAttribute> attributeMap = projectConfig.getAttributes().stream().collect(
                Collectors.toMap(attribute -> CommonUtil.formatString("{}-{}", attribute.getType(), attribute.getName()), attribute -> ProjectConfigAttribute.builder()
                        .projectConfig(newProjectConfig)
                        .type(attribute.getType())
                        .name(attribute.getName())
                        .value(attribute.getValue())
                        .build()));

        Map<String, ProjectConfigAttribute> newAttributeMap = attributes.stream().collect(
                Collectors.toMap(attribute -> CommonUtil.formatString("{}-{}", attribute.getType(), attribute.getName()),
                        attribute -> ProjectConfigAttribute.builder()
                                .projectConfig(newProjectConfig)
                                .type(attribute.getType())
                                .name(attribute.getName())
                                .value(attribute.getValue())
                                .build()));
        attributeMap.putAll(newAttributeMap);
        newProjectConfig.setAttributes(new ArrayList<>(attributeMap.values()));

        log.trace("[cloneProjectConfigWithNewAttribute] newProjectConfig: {}", newProjectConfig);
        return newProjectConfig;
    }

    public ProjectConfig saveProjectConfig(ProjectConfig projectConfig, String currentUsername) {
        Date now = new Date();
        projectConfig.setCreatedBy(currentUsername);
        projectConfig.setCreatedOn(now);
        projectConfig.setModifiedBy(currentUsername);
        projectConfig.setModifiedOn(now);
        return this.projectConfigRepository.saveAndFlush(projectConfig);
    }

    public Optional<ProjectConfig> getProjectConfigById(UUID id) {
        log.debug("[getProjectConfigById] id: {}", id);
        return this.projectConfigRepository.findById(id);
    }

    public Optional<Project> getProjectById(UUID uuid) {
        log.info("[getProjectById] uuid: {}", uuid);
        return this.projectRepository.findById(uuid);
    }

    public Optional<Project> getProjectByProjectId(String projectId) {
        log.info("[getProjectByProjectId] projectId: {}", projectId);
        return this.projectRepository.findByProjectId(projectId);
    }

    public Optional<ProjectConfig> getLatestProjectConfigByProjectUuidAndConfigId(UUID projectUUID, Integer configId){
        log.info("[getLatestProjectConfigByProjectUuidAndConfigId] projectUUID: {}, configId:{}", projectUUID, configId);
        return projectConfigRepository.findLatestProjectConfigByProjectUUIDAndConfigId(projectUUID,String.valueOf(configId));
    }

    public Optional<ProjectConfig> getLatestProjectConfigByProjectUUIDAndRepoAction(UUID projectUUID, String repoAction){
        log.info("[getLatestProjectConfigByProjectUUIDAndRepoAction] projectUUID: {}, repoAction:{}", projectUUID, repoAction);
        return projectConfigRepository.findLatestProjectConfigByProjectUUIDAndRepoAction(projectUUID,repoAction);
    }

    public Optional<String> getLargestIdByProjectUUID(UUID projectUUID){
        log.info("[getLargestIdByProjectUUID] projectUUID: {}", projectUUID);
        return projectConfigRepository.findLargestIdByProjectUUID(projectUUID);
    }

    public Optional<ProjectConfig> getLatestActiveProjectConfigByProjectUuid(UUID id) throws AppException {
        log.info("[getLatestActiveProjectConfigConfigByProjectUuid] uuid: {}", id);
        Project project = this.projectRepository.findById(id).orElseThrow(
                () -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] uuid:{}", AppException.ErrorCode.E_API_PROJECT_COMMON_NOT_FOUND.messageTemplate, id)));
        return this.getLatestActiveProjectConfigByProject(project);
    }

    public Optional<ProjectConfig> getLatestActiveProjectConfigByProject(Project project) {
        log.info("[getLatestActiveProjectConfigByProject] project: {}", project);
        return this.projectConfigRepository.findFirst1ByProjectAndStatus(project, ProjectConfig.Status.ACTIVE, Sort.by(Sort.Order.desc("modifiedOn")));
    }

    public List<ProjectConfig> getProjectConfig(Project project) {
        log.info("[getProjectConfig] project: {}", project);
        return this.projectConfigRepository.findByProject(project);
    }

    //List default preset project configs
    public List<ProjectConfig> listDefaultProjectConfigs() {
        log.info("[listDefaultProjectConfigs]");
        return projectConfigRepository.findByProjectIsNull();
    }

    public void deleteProjectConfigByUUID(UUID id) throws AppException {
        log.info("[deleteProjectConfigByUUID] id: {}", id);
        this.projectConfigRepository.findById(id).orElseThrow(() -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECTCONFIG_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_PROJECTCONFIG_COMMON_NOT_FOUND.messageTemplate, id)));
        this.projectConfigRepository.deleteById(id);
    }

    public Page<Project> findByCreatedBy(User user, Pageable pageable) {
        log.info("[findByCreatedBy] user, username: {}, pageable: {}", user.getUsername(), pageable);
        return projectRepository.findByCreatedByOrderByModifiedOnDesc(user.getUsername(), pageable);
    }

    public Page<Project> listProject(User user, Pageable pageable) {
        log.info("[listProject] user, username: {}, pageable: {}", user.getUsername(), pageable);
        Page<Project> result;
        if (UserService.isAdmin(user)) {
            log.debug("[listProject] user is admin, list all project");
            result = this.findAll(pageable);
        } else {
            log.debug("[listProject] user is not admin, list user created project");
            result = this.findByCreatedBy(user, pageable);
        }
        return result;
    }

    public ProjectConfig addPreset(PresetRequest presetRequest, String currentUsername) throws AppException {
        log.info("[addPreset] presetRequest: {}, currentUsername: {}", presetRequest, currentUsername);
        Date now = new Date();
        ProjectConfig projectConfig = ProjectConfig.builder().name(presetRequest.getName())
                .status(ProjectConfig.Status.ACTIVE)
                .createdBy(currentUsername)
                .createdOn(now)
                .modifiedBy(currentUsername)
                .modifiedOn(now).build();
        List<ProjectConfigAttribute> projectConfigAttributes = new ArrayList<>();
        for (Map.Entry<String, String> projectAttributeEntry : presetRequest.getProjectConfig().entrySet()) {
            if (StringUtils.isNotBlank(projectAttributeEntry.getValue())) {
                projectConfigAttributes.add(ProjectConfigAttribute.builder()
                        .projectConfig(projectConfig)
                        .type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name(projectAttributeEntry.getKey())
                        .value(projectAttributeEntry.getValue())
                        .build());
            }
        }
        for (Map.Entry<String, String> scanAttributeEntry : presetRequest.getScanConfig().entrySet()) {
            if (StringUtils.isNotBlank(scanAttributeEntry.getValue())) {
                projectConfigAttributes.add(ProjectConfigAttribute.builder()
                        .projectConfig(projectConfig)
                        .type(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN)
                        .name(scanAttributeEntry.getKey())
                        .value(scanAttributeEntry.getValue())
                        .build());
            }
        }
        projectConfig.setAttributes(projectConfigAttributes);
        this.projectConfigRepository.saveAndFlush(projectConfig);

        //set project scan mode if project exist or project does not have scanMode

        Optional<Project> projectOptional = projectRepository.findById(presetRequest.getId());
        if (projectOptional.isPresent()) {
            Project project = projectOptional.get();
            String scanModeParam = presetRequest.getScanConfig().get(SCAN_MODE);

            validateScanMode(presetRequest, project);

            project.setScanMode(getScanModeFromParam(scanModeParam));
            this.projectRepository.saveAndFlush(project);
        }

        return projectConfig;
    }

    public String getScanModeFromParam(String scanModeParam) {
        if (scanModeParam == null) {
            //if not provided
            log.warn("[getScanModeFromRequest] scan mode value not provided");
            return null;
        }

        return VariableUtil.ScanMode.getEnumByParamValue(scanModeParam).name();
    }

    public ProjectConfig updatePreset(PresetRequest presetRequest, User currentUser) throws AppException {
        log.info("[updatePreset] presetRequest: {}, currentUsername: {}", presetRequest, currentUser.getUsername());
        ProjectConfig projectConfig = this.getProjectConfigById(presetRequest.getId()).orElseThrow(
                () -> new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECTCONFIG_COMMON_NOT_FOUND.unifyErrorCode, CommonUtil.formatString("[{}] uuid: {}", AppException.ErrorCode.E_API_PROJECTCONFIG_COMMON_NOT_FOUND.messageTemplate, presetRequest.getId())));

        // Validation
        Project project = projectConfig.getProject();
        userService.checkAccessRightOrElseThrow(project, currentUser, false, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, project.getId())));

        // Validate scan mode is not changed
        validateScanMode(presetRequest, project);
        String scanModeParam = presetRequest.getScanConfig().get(SCAN_MODE);
        project.setScanMode(getScanModeFromParam(scanModeParam));
        this.projectRepository.saveAndFlush(project);

        List<ScanTask> scanTasks = scanTaskRepository.findByProject(project);
        if (scanTasks.stream().anyMatch(scanTask -> ScanTask.Status.PENDING.equals(scanTask.getStatus()) || ScanTask.Status.PROCESSING.equals(scanTask.getStatus()))) {
            throw new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_PROJECTCONFIG_CAN_NOT_UPDATE_IN_SCANNING.unifyErrorCode,
                    CommonUtil.formatString("[{}] project id: {}", AppException.ErrorCode.E_API_PROJECTCONFIG_CAN_NOT_UPDATE_IN_SCANNING.messageTemplate, project.getId()));
        }
        List<ProjectConfigAttribute> projectConfigAttributes = new ArrayList<>();
        for (Map.Entry<String, String> projectAttributeEntry : presetRequest.getProjectConfig().entrySet()) {
            if (StringUtils.isNotBlank(projectAttributeEntry.getValue())) {
                projectConfigAttributes.add(ProjectConfigAttribute.builder()
                        .projectConfig(projectConfig)
                        .type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name(projectAttributeEntry.getKey())
                        .value(projectAttributeEntry.getValue())
                        .build());
            }
        }
        for (Map.Entry<String, String> scanAttributeEntry : presetRequest.getScanConfig().entrySet()) {
            if (StringUtils.isNotBlank(scanAttributeEntry.getValue())) {
                projectConfigAttributes.add(ProjectConfigAttribute.builder()
                        .projectConfig(projectConfig)
                        .type(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN)
                        .name(scanAttributeEntry.getKey())
                        .value(scanAttributeEntry.getValue())
                        .build());
            }
        }
        this.projectConfigAttributeRepository.deleteInBatch(projectConfig.getAttributes());
        projectConfig.setAttributes(projectConfigAttributes);
        projectConfig.setName(presetRequest.getName());
        projectConfig.setModifiedOn(new Date());
        projectConfig.setModifiedBy(currentUser.getUsername());
        ProjectConfig result = this.projectConfigRepository.save(projectConfig);
        log.trace("[updatePreset] result: {}", result);

        return result;
    }

    private void validateScanMode(PresetRequest presetRequest, Project project) throws AppException {
        String currentScanMode = project.getScanMode();
        String updateToScanMode = getScanModeFromParam(presetRequest.getScanConfig().get(SCAN_MODE));
        if (currentScanMode != null && !currentScanMode.equals(updateToScanMode)) {
            // TODO: Raymond, update to new error message
            throw new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_INCONSISTENT, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_PROJECT_INVALID_SCAN_MODE.unifyErrorCode, CommonUtil.formatString("[{}] uuid: {}", AppException.ErrorCode.E_API_PROJECT_INVALID_SCAN_MODE.messageTemplate, presetRequest.getId()));
        }
    }

    public List<ProjectDto> convertProjectConfigToProjectDto(List<Project> projects) throws AppException {
        log.trace("[convertProjectConfigToProjectDto] projects size: {}", projects.size());
        List<ProjectDto> list = new ArrayList<>();
        for (Project project : projects) {
            ProjectConfig projectConfig = this.getLatestActiveProjectConfigByProjectUuid(project.getId()).orElseThrow(() ->
                    new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECTCONFIG_COMMON_NOT_FOUND.unifyErrorCode,
                            CommonUtil.formatString("[{}] project id: {}", AppException.ErrorCode.E_API_PROJECTCONFIG_COMMON_NOT_FOUND.messageTemplate, project.getId())));
            list.add(convertProjectConfigToProjectDto(projectConfig, om));
        }
        return list;
    }

    public static ProjectDto convertProjectConfigToProjectDto(ProjectConfig projectConfig, ObjectMapper om) {
        return convertProjectToDto(projectConfig.getProject(), projectConfig, om);
    }

    public static ProjectDto convertProjectToDto(@NotNull(message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_NOTNULL) Project project, ProjectConfig projectConfig, ObjectMapper om) {
        log.trace("[convertProjectToDto] project: {}", project);

        boolean needDsr = project.getNeedDsr() != null ? project.getNeedDsr() : false;
        ProjectDto projectDto = ProjectDto.builder()
                .id(project.getId())
                .name(project.getName())
                .projectId(project.getProjectId())
                .summary(project.getSummary())
                .status(project.getStatus().name())
                .needDsr(needDsr)
                .scanMode(project.getScanMode())
                .cicdFsmState(project.getCicdFsmState())
                .baselineCommitId(project.getBaselineCommitId())
                .retentionNum(project.getRetentionNum())
                .createdBy(project.getCreatedBy())
                .createdOn(project.getCreatedOn())
                .modifiedBy(project.getModifiedBy())
                .modifiedOn(project.getModifiedOn())
                .build();
        if (projectConfig != null) {
            ProjectDto.ProjectConfig projectConfigDto = ProjectDto.ProjectConfig.builder()
                    .id(projectConfig.getId())
                    .name(projectConfig.getName())
                    .attributes(new ArrayList<>())
                    .status(projectConfig.getStatus().name())
                    .createdBy(projectConfig.getCreatedBy())
                    .createdOn(projectConfig.getCreatedOn())
                    .modifiedBy(projectConfig.getModifiedBy())
                    .modifiedOn(projectConfig.getModifiedOn())
                    .build();
            projectConfigDto.setProjectConfig(CommonUtil.writeObjectToJsonStringSilently(om,
                    projectConfig.getAttributes().stream()
                            .filter(attribute -> VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT == attribute.getType()).collect(
                            Collectors.toMap(ProjectConfigAttribute::getName, ProjectConfigAttribute::getValue))));

            // When repoAction is TRIAL, some attributes such as commitId will be null.
            // While Collectors.toMap cannot process null values, use below method to assign scan config values.
            // Refer: https://stackoverflow.com/questions/24630963/nullpointerexception-in-collectors-tomap-with-null-entry-values
            Map<String, String> scanConfigMap = new HashMap<>();
            projectConfig.getAttributes().forEach(attribute -> {
                if(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN == attribute.getType()) {
                    scanConfigMap.put(attribute.getName(), attribute.getValue());
                }
            });
            projectConfigDto.setScanConfig(CommonUtil.writeObjectToJsonStringSilently(om, scanConfigMap));

            projectConfigDto.getAttributes().addAll(projectConfig.getAttributes().stream().map(
                    attribute -> ProjectDto.ProjectConfig.Attribute.builder()
                            .id(attribute.getId())
                            .type(attribute.getType().name())
                            .name(attribute.getName())
                            .value(attribute.getValue()).build()).collect(Collectors.toList()));
            projectDto.setProjectConfig(projectConfigDto);
        }
        return projectDto;
    }

    public static ProjectConfigDto convertProjectConfigToDto(ObjectMapper om, ProjectConfig projectConfig) {
        log.trace("[convertProjectConfigToDto] projectConfig, project: {}", projectConfig.getProject());
        ProjectConfigDto.Project project = null;
        if (projectConfig.getProject() != null) {
            project = ProjectConfigDto.Project.builder()
                    .id(projectConfig.getProject().getId())
                    .name(projectConfig.getProject().getName())
                    .projectId(projectConfig.getProject().getProjectId())
                    .status(projectConfig.getProject().getStatus().name())
                    .cicdFsmState(projectConfig.getProject().getCicdFsmState())
                    .baselineCommitId(projectConfig.getProject().getBaselineCommitId())
                    .retentionNum(projectConfig.getProject().getRetentionNum())
                    .createdBy(projectConfig.getProject().getCreatedBy())
                    .createdOn(projectConfig.getProject().getCreatedOn())
                    .modifiedBy(projectConfig.getProject().getModifiedBy())
                    .modifiedOn(projectConfig.getProject().getModifiedOn())
                    .build();
        }
        ProjectConfigDto projectConfigDto = ProjectConfigDto.builder()
                .id(projectConfig.getId())
                .name(projectConfig.getName())
                .project(project)
                .attributes(new ArrayList<>())
                .status(projectConfig.getStatus().name())
                .createdBy(projectConfig.getCreatedBy())
                .createdOn(projectConfig.getCreatedOn())
                .modifiedBy(projectConfig.getModifiedBy())
                .modifiedOn(projectConfig.getModifiedOn())
                .build();
        projectConfigDto.setProjectConfig(CommonUtil.writeObjectToJsonStringSilently(om,
                projectConfig.getAttributes().stream()
                        .filter(attribute -> VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT == attribute.getType()).collect(
                        Collectors.toMap(ProjectConfigAttribute::getName, ProjectConfigAttribute::getValue))));

        // When repoAction is TRIAL, some attributes such as commitId will be null.
        // While Collectors.toMap cannot process null values, use below method to assign scan config values.
        // Refer: https://stackoverflow.com/questions/24630963/nullpointerexception-in-collectors-tomap-with-null-entry-values
        Map<String, String> scanConfigMap = new HashMap<>();
        projectConfig.getAttributes().forEach(attribute -> {
            if(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN == attribute.getType()) {
                scanConfigMap.put(attribute.getName(), attribute.getValue());
            }
        });
        projectConfigDto.setScanConfig(CommonUtil.writeObjectToJsonStringSilently(om, scanConfigMap));

        projectConfigDto.getAttributes().addAll(projectConfig.getAttributes().stream().map(
                attribute -> ProjectConfigDto.Attribute.builder()
                        .id(attribute.getId())
                        .type(attribute.getType().name())
                        .name(attribute.getName())
                        .value(attribute.getValue()).build()).collect(Collectors.toList()));
        return projectConfigDto;
    }

    public ProjectConfig createProject(String projectId, String sourceCodePath, String buildPath, String currentUsername) throws AppException {
        log.info("[createProject] projectId: {}, sourceCodePath: {}, buildPath: {}", projectId, sourceCodePath, buildPath);

        Map<String, String> projectConfigMap = new HashMap<>();
        projectConfigMap.put(VariableUtil.SOURCE_STORAGE_NAME, VariableUtil.AGENT_FILE_STORAGE_NAME);
        projectConfigMap.put(VariableUtil.RELATIVE_SOURCE_PATH, sourceCodePath);
        if (StringUtils.isNotBlank(buildPath)) {
            projectConfigMap.put(VariableUtil.RELATIVE_BUILD_PATH, buildPath);
        } else {
            projectConfigMap.put(VariableUtil.RELATIVE_BUILD_PATH, sourceCodePath);
        }

        NewProjectRequest newProjectRequest = NewProjectRequest.builder().projectId(projectId)
                .projectConfig(projectConfigMap).build();

        return createProject(newProjectRequest, currentUsername);
    }

    public ProjectConfig createNewProjectConfigWithCommitId(ProjectConfig projectConfig, String commitId, String currentUsername) {
        log.info("[createNewProjectConfigWithCommitId] projectConfig, Id: {}, commitId: {}, currentUsername: {}", projectConfig.getId(), commitId, currentUsername);
        Date now = new Date();

        List<ProjectConfigAttribute> projectConfigAttributes = new ArrayList<>();
        ProjectConfig result = ProjectConfig.builder().name(projectConfig.getName())
                .project(projectConfig.getProject())
                .attributes(projectConfigAttributes)
                .status(ProjectConfig.Status.ACTIVE)
                .createdBy(currentUsername)
                .createdOn(now)
                .modifiedBy(currentUsername)
                .modifiedOn(now).build();


        for (ProjectConfigAttribute projectConfigAttribute : projectConfig.getAttributes()) {
            // ignore the previous commit id key/value
            if (!projectConfigAttribute.getName().equalsIgnoreCase(VariableUtil.ProjectConfigAttributeTypeName.COMMIT_ID.nameValue)) {
                projectConfigAttributes.add(ProjectConfigAttribute.builder()
                        .projectConfig(result)
                        .type(projectConfigAttribute.getType())
                        .name(projectConfigAttribute.getName())
                        .value(projectConfigAttribute.getValue())
                        .build());
            }
        }

        // Add commit id info
        projectConfigAttributes.add(ProjectConfigAttribute.builder()
                .projectConfig(result)
                .type(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN)
                .name(VariableUtil.ProjectConfigAttributeTypeName.COMMIT_ID.nameValue)
                .value(commitId)
                .build());

        return result;
    }

    public static Map<String, String> prepareAttributeMapFromProjectConfig(ProjectConfig projectConfig) {
        final Map<String, String> attributes = new HashMap<>();
        projectConfig.getFirstAttribute(VariableUtil.ProjectConfigAttributeTypeName.USERNAME).map(ProjectConfigAttribute::getValue)
                .ifPresent(value -> attributes.put("USERNAME", value));
        projectConfig.getFirstAttribute(VariableUtil.ProjectConfigAttributeTypeName.VCS_TOKEN).map(ProjectConfigAttribute::getValue)
                .ifPresent(value -> attributes.put("PASSWORD", value));
        projectConfig.getFirstAttribute(VariableUtil.ProjectConfigAttributeTypeName.VCS_TOKEN).map(ProjectConfigAttribute::getValue)
                .ifPresent(value -> attributes.put("VCS_TOKEN", value));
        projectConfig.getFirstAttribute(VariableUtil.ProjectConfigAttributeTypeName.GERRIT_PROJECT_ID).map(ProjectConfigAttribute::getValue)
                .ifPresent(value -> attributes.put("GERRIT_PROJECT_ID", value));
        projectConfig.getFirstAttribute(VariableUtil.ProjectConfigAttributeTypeName.BRANCH).map(ProjectConfigAttribute::getValue)
                .ifPresent(value -> attributes.put("BRANCH", value));
        projectConfig.getFirstAttribute(VariableUtil.ProjectConfigAttributeTypeName.GIT_URL).map(ProjectConfigAttribute::getValue)
                .ifPresent(value -> attributes.put("GIT_URL", value));
        return attributes;
    }

    public Project updateProjectSummary(Project project, ScanTask scanTask) {
        MeasureService.updateProjectSummary(project, scanTask);
        return this.projectRepository.saveAndFlush(project);
    }

    public void updateProjectSummary(UUID projectUUID) {
        log.info("[updateProjectSummary] Start renew Project Summary for : {}", projectUUID);
        projectSummaryDao.deleteProjectSummaryWithProjectId(projectUUID);
        projectSummaryDao.insertProjectSummaryWithProjectId(projectUUID);
        log.info("[updateProjectSummary] End renew Project Summary");
    }

    void updateProjectCicdState(ScanTask scanTask, VariableUtil.ProjectConfigAttributeTypeName nextStateAttributeTypeName) throws AppException {
        log.info("[updateProjectCicdState] scanTask:{}, nextStateAttributeTypeName", scanTask);
        if (scanTask == null) {
            log.error("[updateProjectCicdState] Scan task not exist");
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.unifyErrorCode, AppException.ErrorCode.E_API_SCANTASK_COMMON_NOT_FOUND.messageTemplate);
        }

        Project project = scanTask.getProject();
        ProjectConfig projectConfig = scanTask.getProjectConfig();
        if (projectConfig == null) {
            log.error("[updateProjectCicdState] Cannot get project config from scan task:{} ", scanTask.getId());
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECTCONFIG_COMMON_NOT_FOUND.unifyErrorCode, AppException.ErrorCode.E_API_PROJECTCONFIG_COMMON_NOT_FOUND.messageTemplate);
        }

        String cicdFsmState = projectConfig.getFirstAttributeValue(nextStateAttributeTypeName, null); //On success or on fail
        if (cicdFsmState == null) {
            log.error("[updateProjectCicdState] Cannot set next cicd fsm state as value for {} of project config {} is null", nextStateAttributeTypeName.nameValue, projectConfig.getId());
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJ_CONFIG_ATTR_CMD_NOT_FOUND.unifyErrorCode, AppException.ErrorCode.E_API_PROJ_CONFIG_ATTR_CMD_NOT_FOUND.messageTemplate);
        }

        log.info("[updateProjectCicdState] set cicdFsmState to: {}", cicdFsmState);
        project.setCicdFsmState(cicdFsmState);
        updateProject(project, "System");
    }


    void setProjectBaselineOnCD(ScanTask scanTask, Project project) throws AppException {
        log.info("[setProjectBaselineOnCD] scan task: {}", scanTask);
        ProjectConfig projectConfig = scanTask.getProjectConfig();
        if (projectConfig == null) {
            log.error("[setProjectBaselineOnCD] Cannot get project config from scan task:{} ", scanTask.getId());
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_PROJECTCONFIG_COMMON_NOT_FOUND.unifyErrorCode, AppException.ErrorCode.E_API_PROJECTCONFIG_COMMON_NOT_FOUND.messageTemplate);
        }
        String repoAction = projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.REPO_ACTION, null);

        if(repoAction==null){
            log.info("[setProjectBaselineOnCD] repoAction is null, baseline commit id will not be updated");
            return;
        }

        if(!repoAction.equals(VariableUtil.RepoAction.CD.name())){
            log.info("[setProjectBaselineOnCD] repoAction {} is not CD, baseline commit id will not be updated", repoAction);
            return;
        }

        //get and update
        String commitId = projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.COMMIT_ID, null);
        if(StringUtils.isBlank(commitId)){
            log.warn("[setProjectBaselineOnCD] commit id is null/blank, baseline commit id will not be updated");
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SCANTASK_WITHOUT_COMMIT_ID.unifyErrorCode, AppException.ErrorCode.E_API_SCANTASK_WITHOUT_COMMIT_ID.messageTemplate);
        }
        log.info("[setProjectBaselineOnCD] baseline commit id is updated to {}", commitId);
        project.setBaselineCommitId(commitId);

    }

}
