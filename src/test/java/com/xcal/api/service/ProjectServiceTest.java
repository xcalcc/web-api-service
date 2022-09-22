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
import com.xcal.api.model.dto.ProjectDto;
import com.xcal.api.model.payload.NewProjectRequest;
import com.xcal.api.model.payload.PresetRequest;
import com.xcal.api.repository.*;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.VariableUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;

import java.net.HttpURLConnection;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class ProjectServiceTest {
    private ProjectService projectService;
    private ProjectRepository projectRepository;
    private ProjectConfigRepository projectConfigRepository;
    private ProjectConfigAttributeRepository projectConfigAttributeRepository;
    private ScanTaskRepository scanTaskRepository;
    private UserService userService;
    private ProjectSummaryDao projectSummaryDao;
    private ObjectMapper om = new ObjectMapper();
    private String currentUserName = "user";
    private User currentUser = User.builder().username(currentUserName).displayName("testDispalyName").email("test@xxxx.com").password("12345").userGroups(new ArrayList<>()).build();

    private UserGroup adminGroup = UserGroup.builder().groupType(UserGroup.Type.ROLE).groupName("admin").build();
    private User adminUser = User.builder().username("admin name").displayName("admin display name").email("admin@xxx.com").password("12345").userGroups(Collections.singletonList(adminGroup)).build();


    private UUID projectUUID = UUID.fromString("11111111-1111-1111-1111-111111111110");
    private String projectId = "test project id";
    private String projectName = "testProject";
    private Project.Status projectStatus = Project.Status.ACTIVE;
    private String projectCreateBy = "projectCreateBy user";
    private Project project = Project.builder().id(projectUUID).projectId(projectId).name(projectName).status(projectStatus).needDsr(true).scanMode(VariableUtil.ScanMode.SINGLE.name()).createdBy(currentUserName).build();

    private UUID projectUUID1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private String projectId1 = "test project id1";
    private String projectName1 = "testProject1";
    private String projectCreateBy1 = "projectCreateBy user1";
    private Project project1 = Project.builder().id(projectUUID1).projectId(projectId1).name(projectName1).status(projectStatus).needDsr(true).scanMode(VariableUtil.ScanMode.SINGLE.name()).createdBy(projectCreateBy1).build();

    private UUID projectUUID2 = UUID.fromString("11111111-1111-1111-1111-111111111112");
    private String projectId2 = "test project id2";
    private String projectName2 = "testProject2";
    private String projectCreateBy2 = "projectCreateBy user2";
    private Project project2 = Project.builder().id(projectUUID2).projectId(projectId2).name(projectName2).status(projectStatus).needDsr(true).scanMode(VariableUtil.ScanMode.SINGLE.name()).createdBy(projectCreateBy2).build();

    private List<Project> projectList = Arrays.asList(project1, project2);

    private UUID projectConfigUUID = UUID.fromString("11111111-1111-1111-1111-111111111120");
    private String projectConfigName = "testProjectConfig name";
    private ProjectConfig.Status projectConfigStatus = ProjectConfig.Status.ACTIVE;

    private String projectConfigCreateBy = "projectConfigCreateBy user";
    private ProjectConfig projectConfig = ProjectConfig.builder().id(projectConfigUUID).name(projectConfigName).
            status(projectConfigStatus).createdBy(projectConfigCreateBy).modifiedBy(projectConfigCreateBy).project(project).build();
    private Map<String, String> projectConfigMap;
    private Map<String, String> scanConfigMap;

    private UUID projectConfigUUID1 = UUID.fromString("11111111-1111-1111-1111-111111111121");
    private String projectConfigName1 = "testProjectConfig name1";
    private ProjectConfig.Status projectConfigStatus1 = ProjectConfig.Status.ACTIVE;

    private String projectConfigCreateBy1 = "projectConfigCreateBy user1";
    private ProjectConfig projectConfig1 = ProjectConfig.builder().id(projectConfigUUID1).name(projectConfigName1).
            status(projectConfigStatus1).createdBy(projectConfigCreateBy1).project(project1).build();


    private UUID projectConfigUUID2 = UUID.fromString("11111111-1111-1111-1111-111111111122");
    private String projectConfigName2 = "testProjectConfig name2";
    private ProjectConfig.Status projectConfigStatus2 = ProjectConfig.Status.ACTIVE;
    private String projectConfigCreateBy2 = "projectConfigCreateBy user2";
    private ProjectConfig projectConfig2 = ProjectConfig.builder().id(projectConfigUUID2).name(projectConfigName2).
            status(projectConfigStatus2).createdBy(projectConfigCreateBy2).project(project2).build();
    private List<ProjectConfig> projectConfigList = Arrays.asList(projectConfig1, projectConfig2);


    private UUID presetUUID = UUID.fromString("11111111-1111-1111-1111-111111111130");
    private String presetName = "test preset name";
    private PresetRequest presetRequest = PresetRequest.builder().id(presetUUID).name(presetName).build();

    @BeforeEach
    void setUp() {
        projectRepository = mock(ProjectRepository.class);
        projectConfigRepository = mock(ProjectConfigRepository.class);
        projectConfigAttributeRepository = mock(ProjectConfigAttributeRepository.class);
        scanTaskRepository = mock(ScanTaskRepository.class);
        userService = mock(UserService.class);
        projectSummaryDao = mock(ProjectSummaryDao.class);
        projectService = new ProjectService(userService, projectRepository, projectConfigRepository, projectConfigAttributeRepository, om, scanTaskRepository, projectSummaryDao);

        List<ProjectConfigAttribute> projectConfigAttributes = Arrays.asList(
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("sourceStorageName").value("volume_upload_src").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("gitUrl").value("").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeSourcePath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeBuildPath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN)
                        .name("scanMode").value("-single").build()
        );

        List<ProjectConfigAttribute> projectConfigAttributes1 = Arrays.asList(
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("sourceStorageName").value("volume_upload_src").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("gitUrl").value("").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeSourcePath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeBuildPath").value("/source/demo_benchmark/c_testcase/advance").build()
        );
        List<ProjectConfigAttribute> projectConfigAttributes2 = Arrays.asList(
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("sourceStorageName").value("volume_upload_src").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("gitUrl").value("").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeSourcePath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeBuildPath").value("/source/demo_benchmark/c_testcase/advance").build()
        );
        projectConfig.setAttributes(projectConfigAttributes);
        projectConfigMap = projectConfig.getAttributes().stream()
                .filter(attribute -> VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT == attribute.getType()).collect(
                        Collectors.toMap(ProjectConfigAttribute::getName, ProjectConfigAttribute::getValue));
        scanConfigMap = projectConfig.getAttributes().stream()
                .filter(attribute -> VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN == attribute.getType()).collect(
                        Collectors.toMap(ProjectConfigAttribute::getName, ProjectConfigAttribute::getValue));
        projectConfig1.setAttributes(projectConfigAttributes1);
        projectConfig2.setAttributes(projectConfigAttributes2);
    }

    @Test
    void createProjectTestSuccess() throws AppException {
        log.info("[createProjectTestSuccess]");
        NewProjectRequest newProjectRequest = NewProjectRequest.builder().projectId(projectId).configName(projectConfigName).projectConfig(projectConfigMap).scanConfig(scanConfigMap).build();
        when(projectRepository.findByProjectId(projectId)).thenReturn(Optional.empty());
        when(projectRepository.save(argThat(p -> p.getProjectId().equalsIgnoreCase(project.getProjectId())))).thenReturn(project);
        when(projectConfigRepository.save(any())).thenReturn(projectConfig);
        ProjectConfig pc = projectService.createProject(newProjectRequest, currentUserName);
        Project result = pc.getProject();
        assertEquals(project.getId(), result.getId());
        assertEquals(project.getProjectId(), result.getProjectId());
        assertEquals(project.getName(), result.getName());
        assertEquals(project.getStatus(), result.getStatus());
        assertEquals(project.getCreatedBy(), result.getCreatedBy());
    }

    @Test
    void createProjectTestFail() {
        log.info("[createProjectTestFail]");
        NewProjectRequest newProjectRequest = NewProjectRequest.builder().projectId(projectId).configName(projectConfigName).projectConfig(projectConfigMap).scanConfig(scanConfigMap).build();
        when(projectRepository.findByProjectId(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.save(argThat(p -> p.getProjectId().equalsIgnoreCase(project.getProjectId())))).thenReturn(project);
        when(projectConfigRepository.save(any())).thenReturn(projectConfig);
        assertThrows(AppException.class, () -> projectService.createProject(newProjectRequest, currentUserName));
    }

    @Test
    void findByIdTestSuccess() {
        log.info("[findByIdTestSuccess]");
        when(projectRepository.findById(projectUUID)).thenReturn(Optional.of(project));
        Optional<Project> projectOptional = projectService.findById(projectUUID);
        assertTrue(projectOptional.isPresent());
        assertEquals(project.getId(), projectOptional.get().getId());
        assertEquals(project.getProjectId(), projectOptional.get().getProjectId());
        assertEquals(project.getName(), projectOptional.get().getName());
        assertEquals(project.getStatus(), projectOptional.get().getStatus());
        assertEquals(project.getCreatedBy(), projectOptional.get().getCreatedBy());
    }

    @Test
    void findByIdTestFail() {
        log.info("[findByIdTestFail]");
        UUID id = UUID.randomUUID();
        when(projectRepository.findById(id)).thenReturn(Optional.empty());
        Assertions.assertFalse(projectService.findById(id).isPresent());
    }

    @Test
    void findAllTestSuccess() {
        log.info("[findAllTestSuccess] ");
        Pageable pageable = PageRequest.of(0, 20);
        List<Project> projectsResult = Collections.singletonList(project);
        PageImpl<Project> pagedProjects = new PageImpl<>(projectsResult);
        when(projectRepository.findAll(pageable)).thenReturn(pagedProjects);
        Page<Project> resultPagedProjects = projectService.findAll(pageable);
        assertEquals(pagedProjects.getTotalPages(), resultPagedProjects.getTotalPages());
        assertEquals(pagedProjects.getTotalElements(), resultPagedProjects.getTotalElements());
    }


    @Test
    void deleteProjectTestSuccess() {
        log.info("[deleteProjectTestSuccess]");
        doNothing().when(projectRepository).delete(any());
        doNothing().when(projectConfigRepository).delete(any());
        projectService.deleteProject(project);
        assertTrue(true);
    }


    @Test
    void deleteProjectTestFail() {
        log.info("[deleteProjectTestFail]");
        when(projectRepository.findByIdAndCreatedBy(projectUUID, currentUserName)).thenReturn(Optional.empty());
        doNothing().when(projectRepository).delete(any());
        assertThrows(AppException.class, () -> projectService.deleteProject(projectUUID, currentUserName));
    }

    @Test
    void updateProjectTestSuccess() throws AppException {
        log.info("[updateProjectTestSuccess]");
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectRepository.save(argThat(p1 -> StringUtils.equalsIgnoreCase(project.getName(), p1.getName()))))
                .thenReturn(project);
        Project result = projectService.updateProject(project, currentUserName);
        assertEquals(project.getId(), result.getId());
        assertEquals(project.getProjectId(), result.getProjectId());
        assertEquals(project.getName(), result.getName());
        assertEquals(project.getStatus(), result.getStatus());
        assertEquals(project.getCreatedBy(), result.getCreatedBy());
    }

    @Test
    void updateProjectTestNotFoundFail() {
        log.info("[updateProjectTestNotFoundFail]");
        when(projectRepository.findById(projectUUID)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> projectService.updateProject(project, currentUserName));
    }

    @Test
    void updateProjectTestInconsistentFail() {
        log.info("[updateProjectTestInconsistentFail]");
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project1));
        assertThrows(AppException.class, () -> projectService.updateProject(project, currentUserName));
    }


    @Test
    void inactiveProjectTestSuccess() throws AppException {
        log.info("[inactiveProjectTestSuccess]");
        Project expectedReturnProject = Project.builder().id(projectUUID).projectId(projectId).name(projectName).status(Project.Status.INACTIVE).createdBy(projectCreateBy).build();
        when(projectRepository.save(project)).thenReturn(expectedReturnProject);
        projectService.inactiveProject(project, currentUserName);
        assertTrue(true);
    }

    @Test
    void updateProjectStatusTestSuccess() throws AppException {
        log.info("[updateProjectStatusTestSuccess]");
        Project expectedReturnProject = Project.builder().id(projectUUID).projectId(projectId).name(projectName).status(Project.Status.INACTIVE).createdBy(projectCreateBy).build();
        when(projectRepository.save(project)).thenReturn(expectedReturnProject);
        projectService.updateProjectStatus(project, Project.Status.INACTIVE, currentUserName);
        assertTrue(true);
    }

    @Test
    void updateProjectStatusTestFail() {
        log.info("[updateProjectStatusTestFail]");
        assertThrows(AppException.class, () -> projectService.updateProjectStatus(project, null, currentUserName));
    }

    @Test
    void createProjectConfigTestSuccess() throws AppException {
        log.info("[createProjectConfigTestSuccess]");
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectConfigRepository.findByProjectAndName(project, projectConfig.getName())).thenReturn(new ArrayList<>());
        when(projectConfigRepository.saveAndFlush(projectConfig)).thenReturn(projectConfig);
        ProjectConfig resultProjectConfig = projectService.createProjectConfig(projectUUID, projectConfig, currentUserName);
        assertEquals(projectConfig.getId(), resultProjectConfig.getId());
        assertEquals(projectConfig.getName(), resultProjectConfig.getName());
        assertEquals(projectConfig.getStatus(), resultProjectConfig.getStatus());
        assertEquals(projectConfig.getAttributes(), resultProjectConfig.getAttributes());
        assertEquals(projectConfig.getCreatedBy(), resultProjectConfig.getCreatedBy());
    }

    @Test
    void createProjectConfigTestFail() {
        log.info("[createProjectConfigTestFail]");
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectConfigRepository.findByProjectAndName(project, projectConfig.getName())).thenReturn(projectConfigList);
        assertThrows(AppException.class, () -> projectService.createProjectConfig(projectUUID, projectConfig, currentUserName));
    }

    @Test
    void cloneProjectConfigWithNewAttribute_normal_valueSameObjectDiff() {
        ProjectConfig newProjectConfig = projectService.cloneProjectConfigWithNewAttribute(projectConfig, projectConfig.getAttributes(), projectConfigStatus, projectConfig.getCreatedBy());
        assertEquals(projectConfig.getName(), newProjectConfig.getName());
        assertEquals(projectConfig.getProject(), newProjectConfig.getProject());
        assertEquals(projectConfig.getStatus(), newProjectConfig.getStatus());
        assertEquals(projectConfig.getCreatedBy(), newProjectConfig.getCreatedBy());
        assertEquals(projectConfig.getModifiedBy(), newProjectConfig.getModifiedBy());

        List<ProjectConfigAttribute> originalAttributes = projectConfig.getAttributes();
        List<ProjectConfigAttribute> newAttributes = newProjectConfig.getAttributes();

        Map<String, ProjectConfigAttribute> newMap = newAttributes.stream()
                .collect(Collectors.toMap(ProjectConfigAttribute::getName, Function.identity()));

        for (ProjectConfigAttribute pca : originalAttributes) {
            ProjectConfigAttribute newPca = newMap.get(pca.getName());
            assertEquals(pca.getName(), newPca.getName());
            assertEquals(pca.getType(), newPca.getType());
            assertEquals(pca.getValue(), newPca.getValue());

        }

    }

    @Test
    void saveProjectConfig_normal_noError() {
        doReturn(projectConfig).when(projectConfigRepository).saveAndFlush(any());
        assertDoesNotThrow(() -> projectService.saveProjectConfig(projectConfig, projectConfig.getCreatedBy()));
    }

    @Test
    void saveProjectConfig_normal_returnSameObj() {
        doReturn(projectConfig).when(projectConfigRepository).saveAndFlush(any());
        ProjectConfig resultProjectConfig = projectService.saveProjectConfig(projectConfig, projectConfig.getCreatedBy());
        assertEquals(resultProjectConfig, projectConfig);
    }

    @Test
    void saveProjectConfig_RuntimeException_Exception() {
        doThrow(RuntimeException.class).when(projectConfigRepository).saveAndFlush(any());
        assertThrows(RuntimeException.class, () -> projectService.saveProjectConfig(projectConfig, projectConfig.getCreatedBy()));
    }

    @Test
    void getProjectConfigByIdTestSuccess() {
        log.info("[getProjectConfigByIdTestSuccess]");
        when(projectConfigRepository.findById(projectConfigUUID)).thenReturn(Optional.of(projectConfig));
        Optional<ProjectConfig> resultProjectOptional = projectService.getProjectConfigById(projectConfigUUID);
        assertTrue(resultProjectOptional.isPresent());
        assertEquals(projectConfig.getId(), resultProjectOptional.get().getId());
        assertEquals(projectConfig.getName(), resultProjectOptional.get().getName());
        assertEquals(projectConfig.getStatus(), resultProjectOptional.get().getStatus());
        assertEquals(projectConfig.getAttributes(), resultProjectOptional.get().getAttributes());
        assertEquals(projectConfig.getCreatedBy(), resultProjectOptional.get().getCreatedBy());
    }

    @Test
    void getProjectConfigById_NotFount_ReturnOptionalEmpty() {
        log.info("[getProjectConfigByIdTestFail]");
        when(projectConfigRepository.findById(projectConfigUUID)).thenReturn(Optional.empty());
        assertFalse(projectService.getProjectConfigById(projectConfigUUID).isPresent());
    }


    @Test
    void getLatestActiveProjectConfigByProjectUuidTestSuccess() throws AppException {
        log.info("[getLatestActiveProjectConfigByProjectUuidTestSuccess]");
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectConfigRepository.findFirst1ByProjectAndStatus(project, ProjectConfig.Status.ACTIVE, Sort.by(Sort.Order.desc("modifiedOn")))).thenReturn(Optional.of(projectConfig));
        Optional<ProjectConfig> resultProjectConfigOptional = projectService.getLatestActiveProjectConfigByProjectUuid(projectUUID);
        assertTrue(resultProjectConfigOptional.isPresent());
        assertEquals(projectConfig.getId(), resultProjectConfigOptional.get().getId());
        assertEquals(projectConfig.getName(), resultProjectConfigOptional.get().getName());
        assertEquals(projectConfig.getStatus(), resultProjectConfigOptional.get().getStatus());
        assertEquals(projectConfig.getAttributes(), resultProjectConfigOptional.get().getAttributes());
        assertEquals(projectConfig.getCreatedBy(), resultProjectConfigOptional.get().getCreatedBy());
    }

    @Test
    void getLatestActiveProjectConfigByProjectUuidTestFail() {
        log.info("[getLatestActiveProjectConfigByProjectUuidTestFail]");
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> projectService.getLatestActiveProjectConfigByProjectUuid(projectUUID));
    }

    @Test
    void getLatestActiveProjectConfigByProjectTestSuccess() {
        log.info("[getLatestActiveProjectConfigByProjectTestSuccess]");
        when(projectConfigRepository.findFirst1ByProjectAndStatus(project, ProjectConfig.Status.ACTIVE, Sort.by(Sort.Order.desc("modifiedOn")))).thenReturn(Optional.of(projectConfig));
        Optional<ProjectConfig> resultProjectConfigOptional = projectService.getLatestActiveProjectConfigByProject(project);
        assertTrue(resultProjectConfigOptional.isPresent());
        assertEquals(projectConfig.getId(), resultProjectConfigOptional.get().getId());
        assertEquals(projectConfig.getName(), resultProjectConfigOptional.get().getName());
        assertEquals(projectConfig.getStatus(), resultProjectConfigOptional.get().getStatus());
        assertEquals(projectConfig.getAttributes(), resultProjectConfigOptional.get().getAttributes());
        assertEquals(projectConfig.getCreatedBy(), resultProjectConfigOptional.get().getCreatedBy());
    }

    @Test
    void getProjectConfigTestSuccess() {
        log.info("[getProjectConfigTestSuccess]");
        when(projectConfigRepository.findByProject(project)).thenReturn(projectConfigList);
        List<ProjectConfig> resultProjectConfigList = projectService.getProjectConfig(project);
        assertEquals(2, resultProjectConfigList.size());
        assertEquals(projectConfig1.getId(), resultProjectConfigList.get(0).getId());
        assertEquals(projectConfig1.getName(), resultProjectConfigList.get(0).getName());
        assertEquals(projectConfig1.getStatus(), resultProjectConfigList.get(0).getStatus());
        assertEquals(projectConfig1.getAttributes(), resultProjectConfigList.get(0).getAttributes());
        assertEquals(projectConfig1.getCreatedBy(), resultProjectConfigList.get(0).getCreatedBy());
        assertEquals(projectConfig2.getId(), resultProjectConfigList.get(1).getId());
        assertEquals(projectConfig2.getName(), resultProjectConfigList.get(1).getName());
        assertEquals(projectConfig2.getStatus(), resultProjectConfigList.get(1).getStatus());
        assertEquals(projectConfig2.getAttributes(), resultProjectConfigList.get(1).getAttributes());
        assertEquals(projectConfig2.getCreatedBy(), resultProjectConfigList.get(1).getCreatedBy());
    }


    @Test
    void listDefaultProjectConfigsTestSuccess() {
        log.info("[listDefaultProjectConfigsTestSuccess]");
        when(projectConfigRepository.findByProjectIsNull()).thenReturn(projectConfigList);
        List<ProjectConfig> resultProjectConfigList = projectService.listDefaultProjectConfigs();
        assertEquals(2, resultProjectConfigList.size());
        assertEquals(projectConfig1.getId(), resultProjectConfigList.get(0).getId());
        assertEquals(projectConfig1.getName(), resultProjectConfigList.get(0).getName());
        assertEquals(projectConfig1.getStatus(), resultProjectConfigList.get(0).getStatus());
        assertEquals(projectConfig1.getAttributes(), resultProjectConfigList.get(0).getAttributes());
        assertEquals(projectConfig1.getCreatedBy(), resultProjectConfigList.get(0).getCreatedBy());
        assertEquals(projectConfig2.getId(), resultProjectConfigList.get(1).getId());
        assertEquals(projectConfig2.getName(), resultProjectConfigList.get(1).getName());
        assertEquals(projectConfig2.getStatus(), resultProjectConfigList.get(1).getStatus());
        assertEquals(projectConfig2.getAttributes(), resultProjectConfigList.get(1).getAttributes());
        assertEquals(projectConfig2.getCreatedBy(), resultProjectConfigList.get(1).getCreatedBy());
    }


    @Test
    void deleteProjectConfigByUUIDTestSuccess() throws AppException {
        log.info("[deleteProjectConfigByUUIDTestSuccess]");
        when(projectConfigRepository.findById(projectConfigUUID)).thenReturn(Optional.of(projectConfig));
        doNothing().when(projectConfigRepository).deleteById(projectConfigUUID);
        projectService.deleteProjectConfigByUUID(projectConfigUUID);
        assertTrue(true);
    }

    @Test
    void deleteProjectConfigByUUIDTestFail() {
        log.info("[deleteProjectConfigByUUIDTestFail]");
        when(projectConfigRepository.findById(projectConfigUUID)).thenReturn(Optional.empty());
        doNothing().when(projectConfigRepository).deleteById(projectConfigUUID);
        assertThrows(AppException.class, () -> projectService.deleteProjectConfigByUUID(projectConfigUUID));
    }

    @Test
    void findByCreatedBy() {
        log.info("[findByCreatedByTestSuccess]");
        Pageable pageable = PageRequest.of(0, 20);
        Page<Project> pagedProjects = new PageImpl<>(projectList);
        when(projectRepository.findByCreatedByOrderByModifiedOnDesc(currentUser.getUsername(), pageable)).thenReturn(pagedProjects);
        Page<Project> resultPagedProjects = projectService.findByCreatedBy(currentUser, pageable);
        assertEquals(pagedProjects.getTotalPages(), resultPagedProjects.getTotalPages());
        assertEquals(pagedProjects.getTotalElements(), resultPagedProjects.getTotalElements());
    }

    @Test
    void listProjectTestSuccess() {
        log.info("[listProjectTestSuccess]");
        Pageable pageable = PageRequest.of(0, 20);
        PageImpl<Project> pagedProjects = new PageImpl<>(projectList);
        when(projectRepository.findByCreatedByOrderByModifiedOnDesc(currentUser.getUsername(), pageable)).thenReturn(pagedProjects);
        Page<Project> resultPagedProjects = projectService.listProject(currentUser, pageable);
        assertEquals(pagedProjects.getTotalPages(), resultPagedProjects.getTotalPages());
        assertEquals(pagedProjects.getTotalElements(), resultPagedProjects.getTotalElements());
    }

    @Test
    void listProjectTestByAdminSuccess() {
        log.info("[listProjectTestByAdminSuccess]");
        Pageable pageable = PageRequest.of(0, 20);
        PageImpl<Project> pagedProjects = new PageImpl<>(projectList);
        when(projectRepository.findAll(pageable)).thenReturn(pagedProjects);
        Page<Project> resultPagedProjects = projectService.listProject(adminUser, pageable);
        assertEquals(pagedProjects.getTotalPages(), resultPagedProjects.getTotalPages());
        assertEquals(pagedProjects.getTotalElements(), resultPagedProjects.getTotalElements());
    }

    @Test
    void addPresetTestSuccess() throws AppException {
        log.info("[addPresetTestSuccess]");
        when(projectConfigRepository.save(any())).thenReturn(projectConfig);
        PresetRequest inputPresetRequest = PresetRequest.builder().id(presetUUID).name(presetName).projectConfig(projectConfigMap).scanConfig(scanConfigMap).build();
        ProjectConfig resultProjectConfig = projectService.addPreset(inputPresetRequest, currentUserName);
        assertEquals(presetRequest.getName(), resultProjectConfig.getName());
        assertNotEquals(projectConfig.getAttributes().size(), resultProjectConfig.getAttributes().size());
    }

    @Test
    void updatePreset_currenctUserIsProjectCreator_Success() throws AppException {
        log.info("[updatePreset_currenctUserIsProjectCreator_Success]");
        PresetRequest inputPresetRequest = PresetRequest.builder().id(presetUUID).name(presetName).projectConfig(projectConfigMap).scanConfig(scanConfigMap).build();
        when(projectConfigRepository.findById(inputPresetRequest.getId())).thenReturn(Optional.of(projectConfig));
        when(projectConfigRepository.save(any())).thenReturn(projectConfig);
        doNothing().when(projectConfigAttributeRepository).deleteInBatch(any());
        ProjectConfig resultProjectConfig = projectService.updatePreset(inputPresetRequest, currentUser);
        assertEquals(presetRequest.getName(), resultProjectConfig.getName());
        assertEquals(projectConfig.getAttributes(), resultProjectConfig.getAttributes());
    }

    @Test
    void updatePreset_currenctUserIsNotProjectCreatorAndNotAdmin_ThrowAppException() {
        log.info("[updatePreset_currenctUserIsNotProjectCreatorAndNotAdmin_ThrowAppException]");
        PresetRequest inputPresetRequest = PresetRequest.builder().id(presetUUID).name(presetName).projectConfig(projectConfigMap).scanConfig(scanConfigMap).build();
        ProjectConfig projectConfig = ProjectConfig.builder().project(Project.builder().createdBy("otherUser").build()).status(ProjectConfig.Status.ACTIVE).build();
        when(projectConfigRepository.findById(inputPresetRequest.getId())).thenReturn(Optional.of(projectConfig));

        doThrow(new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate, project.getId())))
                .when(userService).checkAccessRightOrElseThrow(eq(projectConfig.getProject()), any(), anyBoolean(), any());

        AppException appException = assertThrows(AppException.class, () -> projectService.updatePreset(inputPresetRequest, currentUser));
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, appException.getResponseCode());
        assertEquals(AppException.LEVEL_ERROR, appException.getLevel());
        assertEquals(AppException.ERROR_CODE_UNAUTHORIZED, appException.getErrorCode());
    }

    @Test
    void updatePreset_ScanTaskIsPending_ThrowAppException() {
        log.info("[updatePreset_ScanTaskIsPending_ThrowAppException]");
        PresetRequest inputPresetRequest = PresetRequest.builder().id(presetUUID).name(presetName).projectConfig(projectConfigMap).scanConfig(scanConfigMap).build();
        when(projectConfigRepository.findById(inputPresetRequest.getId())).thenReturn(Optional.of(projectConfig));
        when(scanTaskRepository.findByProject(project)).thenReturn(Collections.singletonList(ScanTask.builder().status(ScanTask.Status.PENDING).build()));
        AppException appException = assertThrows(AppException.class, () -> projectService.updatePreset(inputPresetRequest, currentUser));
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, appException.getResponseCode());
        assertEquals(AppException.LEVEL_WARN, appException.getLevel());
        assertEquals(AppException.ERROR_CODE_BAD_REQUEST, appException.getErrorCode());
    }

    @Test
    void updatePreset_ScanTaskIsProcessing_ThrowAppException() {
        log.info("[updatePreset_ScanTaskIsProcessing_ThrowAppException]");
        PresetRequest inputPresetRequest = PresetRequest.builder().id(presetUUID).name(presetName).projectConfig(projectConfigMap).scanConfig(scanConfigMap).build();
        when(projectConfigRepository.findById(inputPresetRequest.getId())).thenReturn(Optional.of(projectConfig));
        when(scanTaskRepository.findByProject(project)).thenReturn(Collections.singletonList(ScanTask.builder().status(ScanTask.Status.PROCESSING).build()));
        AppException appException = assertThrows(AppException.class, () -> projectService.updatePreset(inputPresetRequest, currentUser));
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, appException.getResponseCode());
        assertEquals(AppException.LEVEL_WARN, appException.getLevel());
        assertEquals(AppException.ERROR_CODE_BAD_REQUEST, appException.getErrorCode());
    }

    @Test
    void updatePresetTestFail() {
        log.info("[updatePresetTestFail]");
        PresetRequest inputPresetRequest = PresetRequest.builder().id(presetUUID).name(presetName).projectConfig(projectConfigMap).scanConfig(scanConfigMap).build();
        when(projectConfigRepository.findById(inputPresetRequest.getId())).thenReturn(Optional.empty());
        when(projectConfigRepository.save(any())).thenReturn(projectConfig);
        assertThrows(AppException.class, () -> projectService.updatePreset(inputPresetRequest, currentUser));
    }

    @Test
    void convertProjectsToDtoTestSuccess() throws AppException {
        log.info("[convertProjectsToDtoTestSuccess]");
        ProjectDto projectDto1 = ProjectDto.builder().id(projectConfigUUID1).projectId(projectId1).name(projectName1).build();
        ProjectDto projectDto2 = ProjectDto.builder().id(projectConfigUUID2).projectId(projectId2).name(projectName2).build();
        List<ProjectDto> expectedProjectDtoList = Arrays.asList(projectDto1, projectDto2);
        when(projectRepository.findById(projectUUID1)).thenReturn(Optional.of(project1));
        when(projectConfigRepository.findFirst1ByProjectAndStatus(project1, ProjectConfig.Status.ACTIVE, Sort.by(Sort.Order.desc("modifiedOn")))).thenReturn(Optional.of(projectConfig1));
        when(projectRepository.findById(projectUUID2)).thenReturn(Optional.of(project2));
        when(projectConfigRepository.findFirst1ByProjectAndStatus(project2, ProjectConfig.Status.ACTIVE, Sort.by(Sort.Order.desc("modifiedOn")))).thenReturn(Optional.of(projectConfig2));
        List<ProjectDto> resultProjectDtoList = projectService.convertProjectConfigToProjectDto(projectList);
        assertEquals(expectedProjectDtoList.size(), resultProjectDtoList.size());
        assertNotNull(resultProjectDtoList.get(0).getProjectConfig());
        assertNotNull(resultProjectDtoList.get(1).getProjectConfig());
    }

    @Test
    void convertProjectsToDtoTestFail() {
        log.info("[convertProjectsToDtoTestFail]");
        when(projectRepository.findById(projectUUID1)).thenReturn(Optional.empty());
        when(projectRepository.findById(projectUUID2)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> projectService.convertProjectConfigToProjectDto(projectList));
    }

    @Test
    void convertProjectsToDtoTestSuccess1() {
        log.info("[convertProjectsToDtoTestSuccess1]");
        ProjectDto resultProjectDto = ProjectService.convertProjectConfigToProjectDto(projectConfig, om);
        assertNotNull(resultProjectDto.getProjectConfig());
    }

    @Test
    void createProject_WhenBuildPathIsBlank_ShouldUseSourceCodePathAsRelativeBuildPathValue() throws AppException {
        when(projectRepository.findByProjectId(projectId)).thenReturn(Optional.empty());
        when(projectRepository.save(argThat(p -> p.getProjectId().equalsIgnoreCase(project.getProjectId())))).thenReturn(project);
        when(projectConfigRepository.save(Mockito.any(ProjectConfig.class))).thenAnswer(i -> i.getArguments()[0]);
        String sourceCodePath = "/home/xxx/test";
        ProjectConfig projectConfig = projectService.createProject(projectId, sourceCodePath, null, currentUserName);
        assertEquals(projectId, projectConfig.getProject().getProjectId());
        assertEquals(sourceCodePath, projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH, null));
    }

    @Test
    void createProject_WhenBuildPathIsNotBlank_ShouldNotUseSourceCodePathAsRelativeBuildPathValue() throws AppException {
        when(projectRepository.findByProjectId(projectId)).thenReturn(Optional.empty());
        when(projectRepository.save(argThat(p -> p.getProjectId().equalsIgnoreCase(project.getProjectId())))).thenReturn(project);
        when(projectConfigRepository.save(Mockito.any(ProjectConfig.class))).thenAnswer(i -> i.getArguments()[0]);
        String sourceCodePath = "/home/xxx/test";
        String buildPath = "/home/xxx/build";
        ProjectConfig projectConfig = projectService.createProject(projectId, sourceCodePath, buildPath, currentUserName);
        assertEquals(projectId, projectConfig.getProject().getProjectId());
        assertEquals(sourceCodePath, projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH, null));
        assertEquals(buildPath, projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH, null));
        assertNotEquals(sourceCodePath, projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_BUILD_PATH, null));
    }

    @Test
    void createNewProjectConfigWithCommitId_Success() {
        List<ProjectConfigAttribute> projectConfigAttributes = Arrays.asList(
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("sourceStorageName").value("gitlab").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("gitUrl").value("http://gitlab.com/xxxx").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeSourcePath").value("c_testcase/advance").build(),
                ProjectConfigAttribute.builder().projectConfig(projectConfig).type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeBuildPath").value("c_testcase/advance").build()
        );

        ProjectConfig projectConfig = ProjectConfig.builder().id(projectConfigUUID).name(projectConfigName).
                status(projectConfigStatus).createdBy(projectConfigCreateBy).project(project).build();
        projectConfig.setAttributes(projectConfigAttributes);
        String commitId = UUID.randomUUID().toString();
        ProjectConfig result = projectService.createNewProjectConfigWithCommitId(projectConfig, commitId, currentUserName);
        assertEquals(commitId, result.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.COMMIT_ID, null));
        assertEquals(projectConfigAttributes.size() + 1, result.getAttributes().size());
    }

    @Test
    void getScanModeFromParam_single_single() throws AppException {
        assertEquals("SINGLE", projectService.getScanModeFromParam(VariableUtil.ScanMode.SINGLE.paramValue));
    }

    @Test
    void getScanModeFromParam_cross_cross() throws AppException {
        assertEquals("CROSS", projectService.getScanModeFromParam(VariableUtil.ScanMode.CROSS.paramValue));
    }

    @Test
    void getScanModeFromParam_singleXSCA_singleXSCA() throws AppException {
        assertEquals("SINGLE_XSCA", projectService.getScanModeFromParam(VariableUtil.ScanMode.SINGLE_XSCA.paramValue));
    }

    @Test
    void getScanModeFromParam_XSCA_XSCA() throws AppException {
        assertEquals("XSCA", projectService.getScanModeFromParam(VariableUtil.ScanMode.XSCA.paramValue));
    }


    @Test
    void getScanModeFromParam_null_returnNull() throws AppException {
        assertEquals(null, projectService.getScanModeFromParam(null));
    }

    @Test
    void getScanModeFromParam_other_throwAppException() {
        assertThrows(IllegalArgumentException.class, () -> projectService.getScanModeFromParam("abcde"));
    }

    @Test
    void updateProjectSummary_normal_noError() {
        doReturn(project).when(projectRepository).saveAndFlush(any());
        assertDoesNotThrow(() -> projectService.updateProjectSummary(project, ScanTask.builder().build()));
    }

    @Test
    void updateProjectSummary_normal_returnSameObject() {
        doReturn(project).when(projectRepository).saveAndFlush(any());
        Project returnProject = projectService.updateProjectSummary(project, ScanTask.builder().build());
        assertEquals(project, returnProject);
    }

    @Test
    void updateProjectSummary_runtimeException_runtimeException() {
        doThrow(RuntimeException.class).when(projectRepository).saveAndFlush(any());
        assertThrows(RuntimeException.class, () -> projectService.updateProjectSummary(project, ScanTask.builder().build()));

    }

    @Test
    void updateProjectSummaryWithProjectId_normal_noError() {
        doNothing().when(projectSummaryDao).deleteProjectSummaryWithProjectId(any());
        doReturn(1).when(projectSummaryDao).insertProjectSummaryWithProjectId(any());
        assertDoesNotThrow(() -> projectService.updateProjectSummary(UUID.randomUUID()));
    }


    @Test
    void updateProjectSummaryWithProjectId_runtimeExceptionhenDeleteProjectSummaryWithProjectId_runtimeException() {
        doThrow(RuntimeException.class).when(projectSummaryDao).deleteProjectSummaryWithProjectId(any());
        doReturn(1).when(projectSummaryDao).insertProjectSummaryWithProjectId(any());
        assertThrows(RuntimeException.class, () -> projectService.updateProjectSummary(UUID.randomUUID()));

    }

    @Test
    void updateProjectSummaryWithProjectId_runtimeExceptionhenInsertProjectSummaryWithProjectId_runtimeException() {
        doNothing().when(projectSummaryDao).deleteProjectSummaryWithProjectId(any());
        doThrow(RuntimeException.class).when(projectSummaryDao).insertProjectSummaryWithProjectId(any());
        assertThrows(RuntimeException.class, () -> projectService.updateProjectSummary(UUID.randomUUID()));

    }


    @Test
    void updateProjectCicdState_scanTaskNull_appException() {
        ScanTask scanTask = null;
        assertThrows(AppException.class, () -> projectService.updateProjectCicdState(scanTask, VariableUtil.ProjectConfigAttributeTypeName.NEXT_STATE_ON_SUCCESS));
    }

    @Test
    void updateProjectCicdState_projectConfigNull_appException() {
        ScanTask scanTask = ScanTask.builder().build();
        assertThrows(AppException.class, () -> projectService.updateProjectCicdState(scanTask, VariableUtil.ProjectConfigAttributeTypeName.NEXT_STATE_ON_SUCCESS));
    }

    @Test
    void updateProjectCicdState_projectConfigAttributeNull_appException() {
        ScanTask scanTask = ScanTask.builder().projectConfig(ProjectConfig.builder().build()).build();
        assertThrows(AppException.class, () -> projectService.updateProjectCicdState(scanTask, VariableUtil.ProjectConfigAttributeTypeName.NEXT_STATE_ON_SUCCESS));
    }

    @Test
    void updateProjectCicdState_normal_ok() throws AppException {
        List<ProjectConfigAttribute> attrList = new ArrayList<>();
        attrList.add(ProjectConfigAttribute.builder()
                .type(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN)
                .name(VariableUtil.ProjectConfigAttributeTypeName.NEXT_STATE_ON_SUCCESS.nameValue)
                .value("START").build());
        ScanTask scanTask = ScanTask.builder()
                .projectConfig(
                        ProjectConfig.builder().attributes(attrList).build()
                )
                .project(Project.builder().build())
                .build();

        ProjectService projectServiceSpy = spy(projectService);
        doReturn(project).when(projectServiceSpy).updateProject(any(),anyString());
        assertDoesNotThrow(() -> projectServiceSpy.updateProjectCicdState(scanTask, VariableUtil.ProjectConfigAttributeTypeName.NEXT_STATE_ON_SUCCESS));
    }

    @Test
    void setBaselineOnCD_noProjectConfig_appException(){
        ScanTask scanTask = ScanTask.builder()
                .project(Project.builder().build())
                .build();
        Project project = Project.builder().build();
        assertThrows(AppException.class, ()->projectService.setProjectBaselineOnCD(scanTask, project));
    }

    @Test
    void setBaselineOnCD_nullRepoAction_skip(){
        ScanTask scanTask = ScanTask.builder()
                .projectConfig(
                        ProjectConfig.builder().build()
                )
                .project(Project.builder().build())
                .build();
        Project project = Project.builder().build();
        assertDoesNotThrow(()->projectService.setProjectBaselineOnCD(scanTask, project));
    }

    @Test
    void setBaselineOnCD_ciRepoAction_skip(){
        List<ProjectConfigAttribute> attrList = new ArrayList<>();
        attrList.add(ProjectConfigAttribute.builder()
                .type(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN)
                .name(VariableUtil.ProjectConfigAttributeTypeName.REPO_ACTION.nameValue)
                .value("CI").build());
        ScanTask scanTask = ScanTask.builder()
                .projectConfig(
                        ProjectConfig.builder().attributes(attrList).build()
                )
                .project(Project.builder().build())
                .build();
        Project project = Project.builder().build();
        assertDoesNotThrow(()->projectService.setProjectBaselineOnCD(scanTask, project));
    }

    @Test
    void setBaselineOnCD_cdRepoActionWithoutCommitId_AppException(){
        List<ProjectConfigAttribute> attrList = new ArrayList<>();
        attrList.add(ProjectConfigAttribute.builder()
                .type(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN)
                .name(VariableUtil.ProjectConfigAttributeTypeName.REPO_ACTION.nameValue)
                .value("CD").build());
        ScanTask scanTask = ScanTask.builder()
                .projectConfig(
                        ProjectConfig.builder().attributes(attrList).build()
                )
                .project(Project.builder().build())
                .build();
        Project project = Project.builder().build();
        assertThrows(AppException.class, ()->projectService.setProjectBaselineOnCD(scanTask, project));
    }

    @Test
    void setBaselineOnCD_cdRepoActionWithoutCommitId_updateSuccessfully(){
        List<ProjectConfigAttribute> attrList = new ArrayList<>();
        attrList.add(ProjectConfigAttribute.builder()
                .type(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN)
                .name(VariableUtil.ProjectConfigAttributeTypeName.REPO_ACTION.nameValue)
                .value("CD").build());
        attrList.add(ProjectConfigAttribute.builder()
                .type(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN)
                .name(VariableUtil.ProjectConfigAttributeTypeName.COMMIT_ID.nameValue)
                .value("abcd").build());
        ScanTask scanTask = ScanTask.builder()
                .projectConfig(
                        ProjectConfig.builder().attributes(attrList).build()
                )
                .project(Project.builder().build())
                .build();
        Project project = Project.builder().build();
        assertDoesNotThrow(()->projectService.setProjectBaselineOnCD(scanTask, project));
        assertEquals("abcd",project.getBaselineCommitId());
    }
}
