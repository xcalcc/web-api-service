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
import com.xcal.api.config.WithMockCustomUser;
import com.xcal.api.entity.*;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.ProjectDto;
import com.xcal.api.model.payload.NewProjectRequest;
import com.xcal.api.model.payload.PresetRequest;
import com.xcal.api.model.payload.ProjectInfoRequest;
import com.xcal.api.model.payload.RestResponsePage;
import com.xcal.api.service.*;
import com.xcal.api.util.VariableUtil;
import io.opentracing.Tracer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.net.HttpURLConnection;
import java.util.*;
import java.util.stream.Collectors;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class ProjectControllerTest {

    @NonNull
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private OrchestrationService orchestrationService;

    @MockBean
    private FileService fileService;

    @MockBean
    private ScanTaskService scanTaskService;

    @MockBean
    private ScanFileService scanFileService;

    @MockBean
    private RuleService ruleService;

    @MockBean
    private IssueService issueService;

    @MockBean
    private MeasureService measureService;

    @MockBean
    private ScanStatusService scanStatusService;

    @MockBean
    private CacheService cacheService;

    @NonNull ModelMapper modelMapper;

    @NonNull ObjectMapper om;
    @NonNull Tracer tracer;

    private UUID projectUUID = UUID.randomUUID();
    private UUID projectConfigUUID = UUID.randomUUID();
    private UUID presetId = UUID.randomUUID();

    private final String adminUsername = "admin";
    private final String projectId = "project";
    private final String projectName = "test_project";

    private final String configName = "test_config_name";

    private final String currentUserName = "user";
    private final UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final User currentUser = User.builder().id(userId).username(currentUserName).displayName("testDisplayName").email("test@xxxx.com").password("12345").userGroups(new ArrayList<>()).build();
    private Project project = Project.builder().id(projectUUID).name(projectName).status(Project.Status.ACTIVE).build();
    private List<ProjectConfigAttribute> projectConfigAttributes = Arrays.asList(
            ProjectConfigAttribute.builder().type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                    .name("scanType").value("online_agent").build(),
            ProjectConfigAttribute.builder().type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                    .name("sourceStorageName").value("agent").build(),
            ProjectConfigAttribute.builder().type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                    .name("relativeSourcePath").value("/source/demo_benchmark/c_testcase/advance").build(),
            ProjectConfigAttribute.builder().type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                    .name("relativeBuildPath").value("/source/demo_benchmark/c_testcase/advance").build(),
            ProjectConfigAttribute.builder().type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                    .name("uploadSource").value("true").build());
    private ProjectConfig projectConfig = ProjectConfig.builder()
            .attributes(projectConfigAttributes)
            .project(project)
            .build();
    private UUID scanTaskId = UUID.randomUUID();
    private ScanTask scanTask = ScanTask.builder().id(scanTaskId).project(project).build();
    private UUID issueId1 = UUID.fromString("11111111-1111-1111-1110-111111111111");

    private ScanEngine scanEngine = ScanEngine.builder()
            .id(UUID.randomUUID())
            .name("Xcalibyte")
            .version("1")
            .language("C++")
            .provider("Test provider")
            .providerUrl("Test provider url")
            .url("test url").build();
    private RuleSet ruleSet = RuleSet.builder()
            .id(UUID.randomUUID())
            .name("ruleset_name")
            .version("1")
            .displayName("ruleset display name")
            .scanEngine(scanEngine)
            .language("C++")
            .provider("test provider")
            .providerUrl("test provider url ").build();

    private List<Project> projectList = new ArrayList<>();

    private Map<String, String> projectConfigMap = new LinkedHashMap<String, String>() {
        {
            put("relativePath", "/benchmark/src");
            put("language", "C");
        }
    };
    private Map<String, String> scanConfigMap = new LinkedHashMap<String, String>() {
        {
            put("relativePath", "/benchmark/src");
            put("language", "C");
        }
    };

    @BeforeEach
    void setUp() {
        doNothing().when(cacheService).initCacheRuleInformation();
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void createProjectWithValidParams() throws Exception {
        log.info("[createProjectWithValidParams]");
        final String url = "/api/project_service/v2/project";
        Map<String, String> configContentMap = new LinkedHashMap<String, String>() {
            {
                put("short_form", "lookup");
                put("work_dir", "/benchmark/testcase");
            }
        };

        NewProjectRequest newProjectRequest = NewProjectRequest.builder().projectId(projectId)
                .projectName(projectName).configName(configName).scanConfig(configContentMap).projectConfig(projectConfigMap).build();
        Project project = Project.builder()
                .id(projectUUID)
                .projectId(projectId)
                .name(projectName)
                .status(Project.Status.PENDING)
                .needDsr(true)
                .createdBy(adminUsername)
                .modifiedBy(adminUsername)
                .build();
        ProjectConfig projectConfig = ProjectConfig.builder()
                .attributes(projectConfigAttributes)
                .project(project)
                .status(ProjectConfig.Status.ACTIVE)
                .build();

        when(projectService.createProject(eq(newProjectRequest), any())).thenReturn(projectConfig);
        this.mockMvc.perform(MockMvcRequestBuilders
                .post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newProjectRequest)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(project.getId().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(project.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(project.getStatus().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.projectId").value(project.getProjectId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdBy").value(project.getCreatedBy()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.modifiedBy").value(project.getModifiedBy()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void createProjectWithNullConfigContentShouldReturnInternalServerError() throws Exception {
        log.info("[createProjectWithNullConfigContentShouldReturnInternalServerError]");
        final String url = "/api/project_service/v2/project";
        NewProjectRequest newProjectRequest = NewProjectRequest.builder().projectId(projectId)
                .projectName(projectName).configName(configName).scanConfig(null).build();
        this.mockMvc.perform(MockMvcRequestBuilders
                .post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newProjectRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void createProject_EmptyProjectConfigContent_Success() throws Exception {
        log.info("[createProject_EmptyProjectConfigContent_Success]");
        final String url = "/api/project_service/v2/project";
        Map<String, String> configContentMap = new HashMap<>();
        Project project = Project.builder().projectId(projectId).status(Project.Status.ACTIVE).needDsr(true).build();
        NewProjectRequest newProjectRequest = NewProjectRequest.builder().projectId(projectId)
                .projectName(projectName).configName(configName).scanConfig(configContentMap).build();
        ProjectConfig projectConfig = ProjectConfig.builder()
                .project(project)
                .status(ProjectConfig.Status.ACTIVE)
                .build();
        when(projectService.createProject(any(), any())).thenReturn(projectConfig);

        this.mockMvc.perform(MockMvcRequestBuilders
                .post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newProjectRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void listProjectsWithoutParamsIsOk() throws Exception {
        log.info("[listProjectsWithoutParamsIsOk]");
        final String url = "/api/project_service/v2/projects";
        String projectId1 = "project1";
        String projectName1 = "test_project1";
        String projectId2 = "project2";
        String projectName2 = "test_project2";

        Project project1 = Project.builder().projectId(projectId1).name(projectName1).status(Project.Status.ACTIVE).needDsr(true).build();
        Project project2 = Project.builder().projectId(projectId2).name(projectName2).status(Project.Status.ACTIVE).needDsr(true).build();
        projectList.add(project1);
        projectList.add(project2);
        Pageable pageable = PageRequest.of(0, 20);
        when(projectService.listProject(any(), any())).thenReturn(new RestResponsePage<>(projectList, pageable, projectList.size()));
        this.mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(projectList.size()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].projectId").value(projectId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value(projectName1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].status").value(Project.Status.ACTIVE.toString()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void listProjectsWithParamsIsOk() throws Exception {
        log.info("[listProjectsWithParamsIsOk]");
        final String url = "/api/project_service/v2/projects";
        String projectId1 = "project1";
        String projectName1 = "test_project1";
        String projectId2 = "project2";
        String projectName2 = "test_project2";

        Project project1 = Project.builder().projectId(projectId1).name(projectName1).status(Project.Status.ACTIVE).needDsr(true).build();
        Project project2 = Project.builder().projectId(projectId2).name(projectName2).status(Project.Status.ACTIVE).needDsr(true).build();
        projectList.add(project1);
        projectList.add(project2);

        // find the first page with size value is one
        Pageable pageable0 = PageRequest.of(0, 1);
        when(projectService.listProject(any(), any())).thenReturn(new RestResponsePage<>(projectList.subList(0, 1), pageable0, 2));
        ProjectConfig projectConfig1 = ProjectConfig.builder().project(project1).status(ProjectConfig.Status.ACTIVE).build();
        when(projectService.getLatestActiveProjectConfigByProject(argThat(projectArg -> projectArg.getId() == project1.getId()))).thenReturn(Optional.of(projectConfig1));
        this.mockMvc.perform(get(url)
                .param("page", "0")
                .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].projectId").value(projectId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value(projectName1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].status").value(Project.Status.ACTIVE.toString()));

        // find the second page with size value is one
        Pageable pageable1 = PageRequest.of(1, 1);
        when(projectService.listProject(any(), any())).thenReturn(new RestResponsePage<>(projectList.subList(1, 2), pageable1, 2));

        this.mockMvc.perform(get(url)
                .param("page", "1")
                .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].projectId").value(projectId2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value(projectName2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].status").value(Project.Status.ACTIVE.toString()));
    }

    // UUID should be a 128-bit value which is 32-bit hexadecimal number
    @Test
    @WithMockCustomUser(adminUsername)
    void getProjectByValidId() throws Exception {
        log.info("[getProjectByValidId]");
        Project project = Project.builder().id(projectUUID).projectId(projectId).name(projectName).status(Project.Status.ACTIVE).needDsr(true).createdBy(adminUsername).build();
        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        this.mockMvc.perform(MockMvcRequestBuilders
                .get("/api/project_service/v2/project/{id}", projectUUID))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser()
    void getProjectByValidId_InsufficientPrivilege_ThrowAppException() throws Exception {
        log.info("[getProjectByValidId_InsufficientPrivilege_ThrowAppException]");
        Project project = Project.builder().id(projectUUID).projectId(projectId).name(projectName).status(Project.Status.ACTIVE).createdBy("xxx").build();
        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        this.mockMvc.perform(MockMvcRequestBuilders
                .get("/api/project_service/v2/project/{id}", projectUUID).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    // UUID should be a 128-bit value which is 32-bit hexadecimal number
    @Test
    @WithMockCustomUser(adminUsername)
    void getProjectByInvalidId() throws Exception {
        log.info("[getProjectByInvalidId]");
        when(projectService.findById(projectUUID)).thenReturn(Optional.empty());
        this.mockMvc.perform(MockMvcRequestBuilders
                .get("/api/project_service/v2/project/{id}", projectUUID))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void deleteProjectWithValidId() throws Exception {
        log.info("[deleteProjectWithValidId]");
        final String url = "/api/project_service/v2/project/{id}";
        Project project = Project.builder().id(projectUUID).projectId(projectId).name(projectName).status(Project.Status.ACTIVE).createdBy(adminUsername).build();
        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        doNothing().when(orchestrationService).deleteAllInProject(argThat(p -> p.getId() == projectUUID), eq(true));
        this.mockMvc.perform(MockMvcRequestBuilders
                .delete(url, projectUUID))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void deleteProject_ProjectNotFound_ThrowAppException() throws Exception {
        log.info("[deleteProject_ProjectNotFound_ThrowAppException]");
        final String url = "/api/project_service/v2/project/{id}";
        when(projectService.findById(projectUUID)).thenReturn(Optional.empty());
        this.mockMvc.perform(MockMvcRequestBuilders
                .delete(url, projectUUID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser()
    void deleteProject_InsufficientPrivilege_ThrowAppException() throws Exception {
        log.info("[deleteProject_InsufficientPrivilege_ThrowAppException]");
        final String url = "/api/project_service/v2/project/{id}";
        Project project = Project.builder().id(projectUUID).createdBy("xxx").build();
        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        this.mockMvc.perform(MockMvcRequestBuilders
                .delete(url, projectUUID))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void inactiveProject() throws Exception {
        log.info("[inactiveProject]");
        Project expectedProject = Project.builder()
                .id(projectUUID)
                .projectId(projectId)
                .name(projectName)
                .status(Project.Status.INACTIVE)
                .needDsr(true)
                .createdBy(adminUsername)
                .modifiedBy(adminUsername)
                .build();

        when(projectService.findById(projectUUID)).thenReturn(Optional.of(expectedProject));
        when(projectService.inactiveProject(argThat(project -> StringUtils.equalsIgnoreCase(expectedProject.getId().toString(), project.getId().toString())), any())).thenReturn(expectedProject);
        this.mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/project_service/v2/project/{id}/status", projectUUID))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(expectedProject.getStatus().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.projectId").value(expectedProject.getProjectId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(expectedProject.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(expectedProject.getId().toString()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void inactiveProject_ProjectNotFound_ThrowAppException() throws Exception {
        log.info("[inactiveProject_ProjectNotFound_ThrowAppException]");
        when(projectService.findById(projectUUID)).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/project_service/v2/project/{id}/status", projectUUID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser()
    void inactiveProject_InsufficientPrivilege_ThrowAppException() throws Exception {
        log.info("[inactiveProject_InsufficientPrivilege_ThrowAppException]");
        Project project = Project.builder()
                .id(projectUUID)
                .projectId(projectId)
                .name(projectName)
                .status(Project.Status.INACTIVE)
                .createdBy("xxx")
                .modifiedBy(adminUsername)
                .build();
        when(projectService.findById(projectUUID)).thenReturn(Optional.of(project));
        mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/project_service/v2/project/{id}/status", projectUUID))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void updateProjectWithUUIDParamDifferentFromUUIDGetFromProject() throws Exception {
        log.info("[updateProjectWithUUIDParamDifferentFromUUIDGetFromProject]");
        UUID projectUUID1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID projectUUID2 = UUID.fromString("11111111-1111-1111-1112-111111111111");
        Project initProject = Project.builder()
                .id(projectUUID1)
                .projectId(projectId)
                .name(projectName)
                .status(Project.Status.ACTIVE)
                .createdBy(adminUsername)
                .modifiedBy(adminUsername)
                .build();

        this.mockMvc.perform(MockMvcRequestBuilders
                .put("/api/project_service/v2/project/{id}", projectUUID2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(initProject)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void updateProjectSuccess() throws Exception {
        log.info("[updateProjectSuccess]");
        Project initProject = Project.builder()
                .id(projectUUID)
                .projectId(projectId)
                .name(projectName)
                .status(Project.Status.ACTIVE)
                .needDsr(true)
                .createdBy(adminUsername)
                .modifiedBy(adminUsername)
                .build();

        Project expectedProject = Project.builder()
                .id(projectUUID)
                .projectId(projectId)
                .name(projectName)
                .status(Project.Status.INACTIVE)
                .needDsr(true)
                .createdBy(adminUsername)
                .modifiedBy(adminUsername)
                .build();
        when(projectService.findById(projectUUID)).thenReturn(Optional.of(initProject));
        when(projectService.updateProject(argThat(project -> StringUtils.equalsIgnoreCase(initProject.getId().toString(), project.getId().toString())), any()))
                .thenReturn(expectedProject);
        this.mockMvc.perform(MockMvcRequestBuilders
                .put("/api/project_service/v2/project/{id}", projectUUID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(initProject)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void updateProject_ProjectNotFound_ThrowAppException() throws Exception {
        log.info("[updateProject_ProjectNotFound_ThrowAppException]");
        Project initProject = Project.builder()
                .id(projectUUID)
                .projectId(projectId)
                .name(projectName)
                .status(Project.Status.ACTIVE)
                .createdBy(adminUsername)
                .modifiedBy(adminUsername)
                .build();
        when(projectService.findById(projectUUID)).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders
                .put("/api/project_service/v2/project/{id}", projectUUID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(initProject)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser()
    void updateProject_InsufficientPrivilege_ThrowAppException() throws Exception {
        log.info("[updateProject_InsufficientPrivilege_ThrowAppException]");
        Project initProject = Project.builder()
                .id(projectUUID)
                .projectId(projectId)
                .name(projectName)
                .status(Project.Status.ACTIVE)
                .createdBy("xxx")
                .modifiedBy(adminUsername)
                .build();
        when(projectService.findById(projectUUID)).thenReturn(Optional.of(initProject));
        mockMvc.perform(MockMvcRequestBuilders
                .put("/api/project_service/v2/project/{id}", projectUUID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(initProject)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void listRecentProjects_Success() throws Exception {
        log.info("[listRecentProjects_Success]");
        String projectId1 = "project1";
        String projectName1 = "test_project1";
        String projectId2 = "project2";
        String projectName2 = "test_project2";
        Project project1 = Project.builder().projectId(projectId1).name(projectName1).status(Project.Status.ACTIVE).build();
        Project project2 = Project.builder().projectId(projectId2).name(projectName2).status(Project.Status.ACTIVE).build();
        projectList.add(project1);
        projectList.add(project2);
        List<ProjectDto> projectDtos = Arrays.asList(ProjectDto.builder().projectId(projectId1).name(projectName1).status(Project.Status.ACTIVE.name()).build()
                , ProjectDto.builder().projectId(projectId2).name(projectName2).status(Project.Status.ACTIVE.name()).build());
        Page<Project> pagedProjects = new PageImpl<>(projectList);
        when(projectService.findByCreatedBy(any(), any(Pageable.class))).thenReturn(pagedProjects);
        when(projectService.convertProjectConfigToProjectDto(projectList)).thenReturn(projectDtos);
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/project_service/v2/projects/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].projectId").value(projectId1))
                .andExpect(jsonPath("$.[0].name").value(projectName1))
                .andExpect(jsonPath("$.[0].status").value(Project.Status.ACTIVE.name()))
                .andExpect(jsonPath("$.[1].projectId").value(projectId2))
                .andExpect(jsonPath("$.[1].name").value(projectName2))
                .andExpect(jsonPath("$.[1].status").value(Project.Status.ACTIVE.name()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void createProjectConfigSuccess() throws Exception {
        log.info("[createProjectConfigSuccess]");
        ProjectConfig inputProjectConfig = ProjectConfig.builder().name(configName).attributes(projectConfigAttributes).status(ProjectConfig.Status.ACTIVE).project(null).build();

        Project project = Project.builder()
                .id(projectUUID)
                .projectId(projectId)
                .name(projectName)
                .status(Project.Status.ACTIVE)
                .createdBy(adminUsername)
                .modifiedBy(adminUsername)
                .build();

        ProjectConfig expectedProjectConfig = ProjectConfig.builder()
                .id(projectConfigUUID)
                .name(configName)
                .attributes(projectConfigAttributes)
                .status(ProjectConfig.Status.ACTIVE)
                .project(project)
                .createdBy(adminUsername)
                .modifiedBy(adminUsername)
                .build();

        when(projectService.createProjectConfig(eq(projectUUID), argThat(pi -> StringUtils.equalsIgnoreCase(inputProjectConfig.getName(), pi.getName())), any()))
                .thenReturn(expectedProjectConfig);
        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/project_service/v2/project/{projectUUID}/config", projectUUID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(inputProjectConfig)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedProjectConfig.getId().toString()))
                .andExpect(jsonPath("$.name").value(expectedProjectConfig.getName()))
                .andExpect(jsonPath("$.attributes[*].name", Matchers.containsInAnyOrder(expectedProjectConfig.getAttributes().stream().map(ProjectConfigAttribute::getName).toArray())))
                .andExpect(jsonPath("$.status").value(expectedProjectConfig.getStatus().toString()))
                .andExpect(jsonPath("$.createdBy").value(expectedProjectConfig.getCreatedBy()))
                .andExpect(jsonPath("$.modifiedBy").value(expectedProjectConfig.getModifiedBy()))
                .andExpect(jsonPath("$.project.id").value(expectedProjectConfig.getProject().getId().toString()))
                .andExpect(jsonPath("$.project.projectId").value(expectedProjectConfig.getProject().getProjectId()))
                .andExpect(jsonPath("$.project.name").value(expectedProjectConfig.getProject().getName()));
    }

    // UUID should be a 128-bit value which is 32-bit hexadecimal number
    @Test
    @WithMockCustomUser(value = adminUsername)
    void getProjectConfigWithValidID() throws Exception {
        log.info("[getProjectConfigWithValidID]");
        ProjectConfig projectConfig = ProjectConfig.builder().id(projectConfigUUID).name(configName).status(ProjectConfig.Status.ACTIVE).build();
        when(projectService.getProjectConfigById(projectConfigUUID)).thenReturn(Optional.of(projectConfig));
        this.mockMvc.perform(MockMvcRequestBuilders
                .get("/api/project_service/v2/config/{id}", projectConfigUUID))
                .andExpect(status().isOk());
    }

    // UUID should be a 128-bit value that is 32-bit hexadecimal number
    @Test
    @WithMockCustomUser(value = adminUsername)
    void getProjectConfigWithInValidID() throws Exception {
        log.info("[getProjectConfigWithInValidID]");
        ProjectConfig projectConfig = ProjectConfig.builder().id(projectConfigUUID).name(configName).status(ProjectConfig.Status.ACTIVE).build();
        when(projectService.getProjectConfigById(projectConfigUUID)).thenReturn(Optional.of(projectConfig));
        String invalidUUID = "12345";
        this.mockMvc.perform(MockMvcRequestBuilders
                .get("/api/project_service/v2/config/{id}", invalidUUID))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser(value = adminUsername)
    void getProjectConfigByValidProjectUUIDIsOk() throws Exception {
        log.info("[getProjectConfigByValidProjectUUIDIsOk]");
        Project project = Project.builder()
                .id(projectUUID)
                .projectId(projectId)
                .name(projectName)
                .status(Project.Status.PENDING)
                .createdBy(adminUsername)
                .modifiedBy(adminUsername)
                .build();

        String configName = "config one";
        Optional<ProjectConfig> expectedProjectConfig = Optional.of(ProjectConfig.builder().id(UUID.randomUUID()).name(configName).attributes(projectConfigAttributes).project(project)
                .status(ProjectConfig.Status.ACTIVE).createdBy(adminUsername).modifiedBy(adminUsername).build());

        when(projectService.getProjectById(projectUUID)).thenReturn(Optional.of(project));
        when(projectService.getLatestActiveProjectConfigByProject(project)).thenReturn(expectedProjectConfig);
        this.mockMvc.perform(get("/api/project_service/v2/project/{uuid}/config", projectUUID))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser(value = adminUsername)
    void getProjectConfigByProjectUUID_NotExistProjectUuid_NotFound() throws Exception {
        log.info("[getProjectConfigByProjectUUID_NotExistProjectUuid_NotFound]");
        when(projectService.getLatestActiveProjectConfigByProjectUuid(projectUUID)).thenReturn(Optional.empty());
        this.mockMvc.perform(get("/api/project_service/v2/project/{uuid}/config", projectUUID))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void listDefaultProjectConfigsIsOk() throws Exception {
        log.info("[listDefaultProjectConfigsIsOk]");
        final String url = "/api/project_service/v2/configs";
        String configName1 = "config one";
        String configName2 = "config two";
        String configName3 = "config three";
        List<ProjectConfigAttribute> projectConfigAttributes1 = Arrays.asList(
                ProjectConfigAttribute.builder().type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("scanType").value("online_agent").build(),
                ProjectConfigAttribute.builder().type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("sourceStorageName").value("agent").build(),
                ProjectConfigAttribute.builder().type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeSourcePath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeBuildPath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("uploadSource").value("true").build());
        List<ProjectConfigAttribute> projectConfigAttributes2 = Arrays.asList(
                ProjectConfigAttribute.builder().type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("scanType").value("online_agent").build(),
                ProjectConfigAttribute.builder().type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("sourceStorageName").value("agent").build(),
                ProjectConfigAttribute.builder().type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeSourcePath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeBuildPath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("uploadSource").value("true").build());
        List<ProjectConfigAttribute> projectConfigAttributes3 = Arrays.asList(
                ProjectConfigAttribute.builder().type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("scanType").value("online_agent").build(),
                ProjectConfigAttribute.builder().type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("sourceStorageName").value("agent").build(),
                ProjectConfigAttribute.builder().type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeSourcePath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("relativeBuildPath").value("/source/demo_benchmark/c_testcase/advance").build(),
                ProjectConfigAttribute.builder().type(VariableUtil.ProjectConfigAttributeTypeName.Type.PROJECT)
                        .name("uploadSource").value("true").build());

        List<ProjectConfig> expectedProjectConfigList = Arrays.asList(
                ProjectConfig.builder().id(UUID.randomUUID()).name(configName1).attributes(projectConfigAttributes1).status(ProjectConfig.Status.ACTIVE)
                        .createdBy(adminUsername).modifiedBy(adminUsername).build(),
                ProjectConfig.builder().id(UUID.randomUUID()).name(configName2).attributes(projectConfigAttributes2).status(ProjectConfig.Status.ACTIVE)
                        .createdBy(adminUsername).modifiedBy(adminUsername).build(),
                ProjectConfig.builder().id(UUID.randomUUID()).name(configName3).attributes(projectConfigAttributes3).status(ProjectConfig.Status.ACTIVE)
                        .createdBy(adminUsername).modifiedBy(adminUsername).build()
        );

        when(projectService.listDefaultProjectConfigs()).thenReturn(expectedProjectConfigList);
        this.mockMvc.perform(get(url))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void deleteProjectConfig() throws Exception {
        log.info("[deleteProjectConfig]");
        doNothing().when(projectService).deleteProjectConfigByUUID(projectConfigUUID);
        this.mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/project_service/v2/config/{id}", projectConfigUUID))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void createPresetSuccess() throws Exception {
        log.info("[createPresetSuccess]");
        PresetRequest presetRequest = PresetRequest.builder().id(presetId).name(configName).projectConfig(projectConfigMap).scanConfig(scanConfigMap).build();
        ProjectConfig expectedResult = ProjectConfig.builder().id(presetId).name(configName).attributes(projectConfigAttributes).status(ProjectConfig.Status.ACTIVE).build();
        when(projectService.addPreset(any(PresetRequest.class), any())).thenReturn(expectedResult);

        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/api/project_service/v2/config")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(presetRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResult.getId().toString()))
                .andExpect(jsonPath("$.name").value(expectedResult.getName()));
    }
//
//    @Test
//    @WithMockCustomUser(adminUsername)
//    void createPresetFailNameEmpty() throws Exception {
//        log.info("[createPresetFailNameEmpty]");
//        PresetRequest presetRequest = PresetRequest.builder().id(presetId).projectConfig(projectConfigMap).scanConfig(scanConfigMap).build();
//        this.mockMvc.perform(MockMvcRequestBuilders
//                .post("/api/project_service/v2/config")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(om.writeValueAsString(presetRequest)))
//                .andExpect(status().isBadRequest());
//    }

    @Test
    @WithMockCustomUser(adminUsername)
    void updatePresetSuccess() throws Exception {
        log.info("[updatePresetSuccess]");
        PresetRequest presetRequest = PresetRequest.builder().id(presetId).name(configName).projectConfig(projectConfigMap).scanConfig(scanConfigMap).build();
        ProjectConfig expectedResult = ProjectConfig.builder().id(presetId).name(configName).attributes(projectConfigAttributes).status(ProjectConfig.Status.ACTIVE).build();
        when(projectService.updatePreset(any(PresetRequest.class), any())).thenReturn(expectedResult);
        this.mockMvc.perform(MockMvcRequestBuilders
                .put("/api/project_service/v2/config/{id}", presetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(presetRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResult.getId().toString()))
                .andExpect(jsonPath("$.name").value(expectedResult.getName()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void uploadInfo__InvalidFileinfo_ThrowAppException() throws Exception {
        log.info("[uploadInfo__InvalidFileinfo_ThrowAppException]");
        MockMultipartFile file = new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes());
        ProjectInfoRequest projectInfoRequest = ProjectInfoRequest.builder().fileInfoFile(file).scanResult(file).projectId("aa").sourceCodePath("/aa").buildPath("/aa").build();
        when(projectService.createProject(any(), any(), any(), any())).thenReturn(ProjectConfig.builder().project(Project.builder().build()).build());
        when(scanTaskService.addScanTask(any(ProjectConfig.class), any(String.class))).thenReturn(ScanTask.builder().build());
        mockMvc.perform(post("/api/project_service/v2/upload_info").flashAttr("projectInfoRequest", projectInfoRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_BAD_REQUEST))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_INCORRECT_PARAM));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void uploadInfo__InvalidScanResult_ThrowAppException() throws Exception {
        log.info("[uploadInfo__InvalidScanResult_ThrowAppException]");
        MockMultipartFile fileInfoFile = new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "{\"id\":\"11111111-1111-1111-1112-111111111113\"}".getBytes());
        MockMultipartFile scanResultFile = new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "testing content".getBytes());
        ProjectInfoRequest projectInfoRequest = ProjectInfoRequest.builder().fileInfoFile(fileInfoFile).scanResult(scanResultFile).projectId("aa").sourceCodePath("/aa").buildPath("/aa").build();
        when(projectService.createProject(any(), any(), any(), any())).thenReturn(ProjectConfig.builder().project(Project.builder().build()).build());
        when(scanTaskService.addScanTask(any(ProjectConfig.class), any(String.class))).thenReturn(ScanTask.builder().build());
        mockMvc.perform(post("/api/project_service/v2/upload_info").flashAttr("projectInfoRequest", projectInfoRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_WARN))
                .andExpect(jsonPath("$.responseCode").value(HTTP_BAD_REQUEST))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_INCORRECT_PARAM));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void uploadInfo__ValidFileinfoAndScanResult_Success() throws Exception {
        log.info("[uploadInfo__ValidFileinfoAndScanResult_Success]");
        RuleInformation ruleInformation = RuleInformation.builder()
                .id(UUID.randomUUID())
                .ruleSet(ruleSet)
                .ruleCode("BUILTIN-NPD-D")
                .category("ROBUSTNESS")
                .vulnerable("NPD")
                .name("Null pointer dereference")
                .certainty(RuleInformation.Certainty.D)
                .priority(RuleInformation.Priority.HIGH)
                .severity(RuleInformation.Severity.HIGH)
                .likelihood(RuleInformation.Likelihood.LIKELY)
                .remediationCost(RuleInformation.RemediationCost.LOW)
                .language("c,c++")
                .description("Address of a local variable (stack space) has been passed to the caller, causing illegal memory access")
                .build();
        Issue issue1 = Issue.builder().id(issueId1).issueCode("BUILTIN-NPD-D").scanTask(scanTask)
                .seq("00011")
                .severity(Issue.Severity.HIGH)
                .lineNo(381)
                .columnNo(0)
                .functionName("ngx_vslprintf")
                .variableName("buf")
                .message("The value of the pointer (reference) is 0 (or near zero) and is used to access memory expected to be valid")
                .status(Issue.Status.ACTIVE)
                .action(Issue.Action.CONFIRMED)
                .createdBy(currentUserName)
                .createdOn(new Date())
                .modifiedOn(new Date())
                .modifiedBy(currentUserName)
                .assignTo(currentUser)
                .issueKey("/share/scan/e6ca4143-59e5-40d7-96d7-b2d26221b7d8/src/core/ngx_string.c@ngx_vslprintf@buf@NPD@@@fffffa68")
                .filePath("/share/scan/e6ca4143-59e5-40d7-96d7-b2d26221b7d8/src/core/ngx_string.c")
                .ruleInformation(ruleInformation)
                .issueTraces(new ArrayList<>())
                .build();
        MockMultipartFile fileInfoFile = new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "{}".getBytes());
        MockMultipartFile scanResultFile = new MockMultipartFile("upload_file", "test.txt", MediaType.MULTIPART_FORM_DATA_VALUE, "{}".getBytes());
        ProjectInfoRequest projectInfoRequest = ProjectInfoRequest.builder().fileInfoFile(fileInfoFile).scanResult(scanResultFile).projectId("aa").sourceCodeFileInfoId("11111111-1111-1111-1112-111111111113").sourceCodePath("/aa").buildPath("/aa").build();
        when(projectService.createProject(any(), any(), any(), any())).thenReturn(projectConfig);
        when(scanTaskService.addScanTask(any(ProjectConfig.class), any(String.class))).thenReturn(ScanTask.builder().build());
        when(ruleService.getRuleSetFromImportScanResultRequest(any())).thenReturn(Collections.singletonList(ruleSet));
        when(issueService.importIssueToScanTask(any(), any(), any())).thenReturn(Collections.singletonList(issue1));
        when(issueService.convertIssuesToDto(anyList(), any(Locale.class))).thenAnswer(invocation -> {
            List<Issue> issues = invocation.getArgument(0);
            Map<String, I18nMessage> i18nMessageMap = new HashMap<>();
            return issues.stream().map(issue -> IssueService.convertIssueToDto(issue, i18nMessageMap)).collect(Collectors.toList());
        });
        doNothing().when(fileService).decompressFile(any());
        when(scanStatusService.saveScanTaskStatusLog(any(ScanTask.class), any(), any(), any(), any(), any(), any())).thenReturn(ScanTaskStatusLog.builder().build());
        mockMvc.perform(post("/api/project_service/v2/upload_info").flashAttr("projectInfoRequest", projectInfoRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(issue1.getId().toString()))
                .andExpect(jsonPath("$.[0].issueKey").value(issue1.getIssueKey()))
                .andExpect(jsonPath("$.[0].seq").value(issue1.getSeq()))
                .andExpect(jsonPath("$.[0].issueCategory").value(ruleInformation.getCategory()))
                .andExpect(jsonPath("$.[0].ruleSet").value(ruleInformation.getRuleSet().getName()))
                .andExpect(jsonPath("$.[0].vulnerable").value(ruleInformation.getVulnerable()))
                .andExpect(jsonPath("$.[0].certainty").value(ruleInformation.getCertainty().name()))
                .andExpect(jsonPath("$.[0].issueCode").value(issue1.getIssueCode()))
                .andExpect(jsonPath("$.[0].issueName").value(ruleInformation.getName()))
                .andExpect(jsonPath("$.[0].severity").value(issue1.getSeverity().name()))
                .andExpect(jsonPath("$.[0].likelihood").value(ruleInformation.getLikelihood().name()))
                .andExpect(jsonPath("$.[0].remediationCost").value(ruleInformation.getRemediationCost().name()))
                .andExpect(jsonPath("$.[0].relativePath").value(issue1.getFilePath()))
                .andExpect(jsonPath("$.[0].scanFilePath").value(issue1.getFilePath()))
                .andExpect(jsonPath("$.[0].lineNo").value(issue1.getLineNo()))
                .andExpect(jsonPath("$.[0].columnNo").value(issue1.getColumnNo()))
                .andExpect(jsonPath("$.[0].functionName").value(issue1.getFunctionName()))
                .andExpect(jsonPath("$.[0].variableName").value(issue1.getVariableName()))
                .andExpect(jsonPath("$.[0].message").value(issue1.getMessage()))
                .andExpect(jsonPath("$.[0].status").value(issue1.getStatus().name()))
                .andExpect(jsonPath("$.[0].action").value(issue1.getAction().name()))
                .andExpect(jsonPath("$.[0].createdBy").value(issue1.getCreatedBy()))
                .andExpect(jsonPath("$.[0].modifiedBy").value(issue1.getModifiedBy()))
                .andExpect(jsonPath("$.[0].assignTo.id").value(currentUser.getId().toString()))
                .andExpect(jsonPath("$.[0].assignTo.displayName").value(currentUser.getDisplayName()))
                .andExpect(jsonPath("$.[0].assignTo.email").value(currentUser.getEmail()))
                .andExpect(jsonPath("$.[0].assignTo.id").value(currentUser.getId().toString()))
                .andExpect(jsonPath("$.[0].ruleInformation.id").value(ruleInformation.getId().toString()))
                .andExpect(jsonPath("$.[0].ruleInformation.ruleSet").value(ruleInformation.getRuleSet().getName()))
                .andExpect(jsonPath("$.[0].ruleInformation.ruleSetVersion").value(ruleInformation.getRuleSet().getVersion()))
                .andExpect(jsonPath("$.[0].ruleInformation.ruleCode").value(ruleInformation.getRuleCode()))
                .andExpect(jsonPath("$.[0].ruleInformation.category").value(ruleInformation.getCategory()))
                .andExpect(jsonPath("$.[0].ruleInformation.vulnerable").value(ruleInformation.getVulnerable()))
                .andExpect(jsonPath("$.[0].ruleInformation.name").value(ruleInformation.getName()))
                .andExpect(jsonPath("$.[0].ruleInformation.certainty").value(ruleInformation.getCertainty().name()))
                .andExpect(jsonPath("$.[0].ruleInformation.priority").value(ruleInformation.getPriority().name()))
                .andExpect(jsonPath("$.[0].ruleInformation.severity").value(ruleInformation.getSeverity().name()))
                .andExpect(jsonPath("$.[0].ruleInformation.likelihood").value(ruleInformation.getLikelihood().name()))
                .andExpect(jsonPath("$.[0].ruleInformation.remediationCost").value(ruleInformation.getRemediationCost().name()))
                .andExpect(jsonPath("$.[0].ruleInformation.language").value(ruleInformation.getLanguage()))
                .andExpect(jsonPath("$.[0].ruleInformation.description").value(ruleInformation.getDescription()));
    }
}
