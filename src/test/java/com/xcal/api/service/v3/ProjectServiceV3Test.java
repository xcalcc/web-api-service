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

package com.xcal.api.service.v3;

import com.xcal.api.dao.ProjectDao;
import com.xcal.api.dao.ScanTaskDao;
import com.xcal.api.entity.*;
import com.xcal.api.entity.v3.ProjectSummary;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.v3.ProjectSummaryDto;
import com.xcal.api.repository.ProjectRepository;
import com.xcal.api.repository.ScanTaskRepository;
import com.xcal.api.repository.v3.ProjectSummaryRepository;
import com.xcal.api.security.UserPrincipal;
import com.xcal.api.service.OrchestrationService;
import com.xcal.api.service.UserService;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.VariableUtil;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Stubber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;

import java.net.HttpURLConnection;
import java.util.*;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.MOCK)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@AutoConfigureMockMvc
public class ProjectServiceV3Test {

    @MockBean
    private UserService userService;

    @MockBean
    private OrchestrationService orchestrationService;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private ProjectDao projectDao;

    @MockBean
    private ProjectSummaryRepository projectSummaryRepository;

    @MockBean
    private ScanTaskRepository scanTaskRepository;

    @MockBean
    private ProjectServiceV3 projectServiceV3;

    @MockBean
    private ScanTaskDao scanTaskDao;


    @BeforeEach
    void setup() {
        this.projectServiceV3 = new ProjectServiceV3(
                this.userService,
                this.orchestrationService,
                this.projectRepository,
                this.projectDao,
                this.projectSummaryRepository,
                this.scanTaskRepository,
                scanTaskDao
        );
    }

    @Test
    void deleteProject_Success() throws AppException {
        Project project = new Project();
        when(projectRepository.findById(any())).thenReturn(Optional.of(project));
        projectServiceV3.deleteProject(UUID.randomUUID(), new UserPrincipal());
    }

    @Test
    void deleteProject_projectNotFound_AppException() throws AppException {
        assertThrows(AppException.class, () -> {

            when(projectRepository.findById(any())).thenReturn(Optional.empty());
            projectServiceV3.deleteProject(UUID.randomUUID(), new UserPrincipal());

        });
    }

    @Test
    void deleteProject_noPermission_AppException() throws AppException {
        assertThrows(AppException.class, () -> {


            List normaUserGroupList = new ArrayList<>();
            normaUserGroupList.add(UserGroup.builder().groupType(UserGroup.Type.ROLE).groupName("admin").build());

            User normalUser = User.builder()
                    .id(UUID.randomUUID())
                    .username("user")
                    .password("password")
                    .displayName("Normal User")
                    .email("normal@test.com")
                    .userGroups(normaUserGroupList)
                    .build();
            UserPrincipal normalUserPrincipal = UserPrincipal.create(normalUser);

            Stubber throwStubber=doThrow(new AppException(AppException.LEVEL_ERROR,
                    AppException.ERROR_CODE_UNAUTHORIZED,
                    HttpURLConnection.HTTP_FORBIDDEN,
                    AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.unifyErrorCode,
                    CommonUtil.formatString(
                            "[{}] projectId: {}",
                            AppException.ErrorCode.E_API_USER_COMMON_INSUFFICIENT_PRIVILEGE.messageTemplate,
                            UUID.randomUUID()
                    )));
            UserService  userServiceStub=throwStubber.when(userService);

            userServiceStub.checkAccessRightOrElseThrow(any(Project.class), any(User.class), anyBoolean(), any(Supplier.class));


            projectServiceV3.deleteProject(UUID.randomUUID(), new UserPrincipal());

        });
    }

    @Test
    void getProjectSummaryList_normal_Success() throws AppException {

        Map<String, String> map1 = new HashMap<>();
        ProjectSummary projectSummary1 = ProjectSummary.builder().projectId("abcd1").name("project 1").summary(map1).status("COMPLETED").needDsr(true).build();
//            ProjectSummary projectSummary2=ProjectSummary.builder().build();
        when(projectSummaryRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Arrays.asList(projectSummary1)));

        List normaUserGroupList = new ArrayList<>();
        normaUserGroupList.add(UserGroup.builder().groupType(UserGroup.Type.ROLE).groupName("admin").build());

        User normalUser = User.builder()
                .id(UUID.randomUUID())
                .username("user")
                .password("password")
                .displayName("Normal User")
                .email("normal@test.com")
                .userGroups(normaUserGroupList)
                .build();
        UserPrincipal normalUserPrincipal = UserPrincipal.create(normalUser);
        Pageable pageable = PageRequest.of(0, 20);
        Page<ProjectSummaryDto> projectSummaryDtoPage = projectServiceV3.getProjectSummaryList(pageable, normalUserPrincipal);
        assertEquals(1, projectSummaryDtoPage.getTotalElements());
    }


    @Test
    void getProjectSummary_normal_Success() throws AppException {

        when(projectSummaryRepository.findById(any())).thenReturn(Optional.of(ProjectSummary.builder().build()));
        ProjectSummaryDto projectSummaryDto = projectServiceV3.getProjectSummary(UUID.randomUUID());
        assertNotNull(projectSummaryDto);
    }

    @Test
    void getProjectSummary_projectSummaryNotFound_throwAppException() throws AppException {

        assertThrows(AppException.class, () -> {

            ProjectSummaryDto projectSummaryDto = projectServiceV3.getProjectSummary(UUID.randomUUID());
            when(projectSummaryRepository.findById(any())).thenReturn(Optional.empty());

        });

    }


    @Test
    void convertToV3_null_returnNull() throws AppException {
        ProjectSummaryDto projectSummaryDto = projectServiceV3.convertToV3(null);
        assertEquals(null, projectSummaryDto);
    }

    @Test
    void convertToV3_allAttributeNull_noException() throws AppException {
        projectServiceV3.convertToV3(ProjectSummary.builder().build());
    }


    @Test
    void convertToV3_normal_returnDto() throws AppException {
        Project project = Project.builder().id(UUID.randomUUID()).needDsr(true).build();

        ScanTask scanTask1 = ScanTask.builder().id(UUID.randomUUID()).project(project).status(ScanTask.Status.COMPLETED).build();
        ScanTask scanTask2 = ScanTask.builder().id(UUID.randomUUID()).project(project).status(ScanTask.Status.COMPLETED).build();
        ScanTask scanTask3 = ScanTask.builder().id(UUID.randomUUID()).project(project).status(ScanTask.Status.COMPLETED).build();

        when(scanTaskRepository.findFirst1ByProject(any(Project.class), any(Sort.class))).thenReturn(Optional.of(scanTask1));
        when(scanTaskRepository.findFirst1ByProjectAndStatus(any(Project.class), eq(ScanTask.Status.COMPLETED), any(Sort.class))).thenReturn(Optional.of(scanTask2));
        when(scanTaskRepository.findPreviousByProjectAndScanTaskAndStatus(any(UUID.class), any(UUID.class), eq(ScanTask.Status.COMPLETED))).thenReturn(Optional.of(scanTask3));

        Map<String, String> summary = new HashMap();

        UUID uuid = UUID.randomUUID();
        ProjectSummaryDto projectSummaryDto = projectServiceV3.convertToV3(ProjectSummary.builder()
                .projectId(uuid.toString())
                .name("test project")
                .summary(summary)
                .status("COMPLETED")
                .needDsr(true)
                .build());


        assertEquals("test project", projectSummaryDto.getName());
        assertEquals("COMPLETED", projectSummaryDto.getStatus());
        assertEquals(uuid.toString(), projectSummaryDto.getProjectId());
    }

    @Test
    void isScanTaskDsr_bothCommitIdExist_true() throws AppException {

        List<ProjectConfigAttribute> projectConfigAttributeList = new ArrayList<>();

        projectConfigAttributeList.add(ProjectConfigAttribute.builder()
                .type(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN)
                .name(VariableUtil.ProjectConfigAttributeTypeName.COMMIT_ID.nameValue)
                .value("aabbcc")
                .build());

        projectConfigAttributeList.add(ProjectConfigAttribute.builder()
                .type(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN)
                .name(VariableUtil.ProjectConfigAttributeTypeName.BASELINE_COMMIT_ID.nameValue)
                .value("aabbcc")
                .build());

        ScanTask scanTask = ScanTask.builder().projectConfig(ProjectConfig.builder().attributes(projectConfigAttributeList).build()).build();
        boolean isScanTaskDsr = ProjectServiceV3.isScanTaskDsr(scanTask);
        assertEquals(true, isScanTaskDsr);

    }

    @Test
    void isScanTaskDsr_commitExistBaselineNotExist_false() throws AppException {

        List<ProjectConfigAttribute> projectConfigAttributeList = new ArrayList<>();

        projectConfigAttributeList.add(ProjectConfigAttribute.builder()
                .type(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN)
                .name(VariableUtil.ProjectConfigAttributeTypeName.COMMIT_ID.nameValue)
                .value("aabbcc")
                .build());

        ScanTask scanTask = ScanTask.builder().projectConfig(ProjectConfig.builder().attributes(projectConfigAttributeList).build()).build();
        boolean isScanTaskDsr = ProjectServiceV3.isScanTaskDsr(scanTask);
        assertEquals(false, isScanTaskDsr);

    }

    @Test
    void isScanTaskDsr_commitNotExistOnlyBaselineExist_false() throws AppException {

        List<ProjectConfigAttribute> projectConfigAttributeList = new ArrayList<>();

        projectConfigAttributeList.add(ProjectConfigAttribute.builder()
                .type(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN)
                .name(VariableUtil.ProjectConfigAttributeTypeName.BASELINE_COMMIT_ID.nameValue)
                .value("aabbcc")
                .build());

        ScanTask scanTask = ScanTask.builder().projectConfig(ProjectConfig.builder().attributes(projectConfigAttributeList).build()).build();
        boolean isScanTaskDsr = ProjectServiceV3.isScanTaskDsr(scanTask);
        assertEquals(false, isScanTaskDsr);
    }

    @Test
    void isScanTaskDsr_commitNull_false() throws AppException {
        List<ProjectConfigAttribute> projectConfigAttributeList = new ArrayList<>();

        projectConfigAttributeList.add(ProjectConfigAttribute.builder()
                .type(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN)
                .name(VariableUtil.ProjectConfigAttributeTypeName.COMMIT_ID.nameValue)
                .value(null)
                .build());
        projectConfigAttributeList.add(ProjectConfigAttribute.builder()
                .type(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN)
                .name(VariableUtil.ProjectConfigAttributeTypeName.BASELINE_COMMIT_ID.nameValue)
                .value("aabbcc")
                .build());

        ScanTask scanTask = ScanTask.builder().projectConfig(ProjectConfig.builder().attributes(projectConfigAttributeList).build()).build();
        boolean isScanTaskDsr = ProjectServiceV3.isScanTaskDsr(scanTask);
        assertEquals(false, isScanTaskDsr);
    }

    @Test
    void isScanTaskDsr_baselineNull_false() throws AppException {
        List<ProjectConfigAttribute> projectConfigAttributeList = new ArrayList<>();

        projectConfigAttributeList.add(ProjectConfigAttribute.builder()
                .type(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN)
                .name(VariableUtil.ProjectConfigAttributeTypeName.COMMIT_ID.nameValue)
                .value("aabbcc")
                .build());
        projectConfigAttributeList.add(ProjectConfigAttribute.builder()
                .type(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN)
                .name(VariableUtil.ProjectConfigAttributeTypeName.BASELINE_COMMIT_ID.nameValue)
                .value(null)
                .build());

        ScanTask scanTask = ScanTask.builder().projectConfig(ProjectConfig.builder().attributes(projectConfigAttributeList).build()).build();
        boolean isScanTaskDsr = ProjectServiceV3.isScanTaskDsr(scanTask);
        assertEquals(false, isScanTaskDsr);
    }

    @Test
    void isScanTaskDsr_bothNull_false() throws AppException {
        List<ProjectConfigAttribute> projectConfigAttributeList = new ArrayList<>();

        projectConfigAttributeList.add(ProjectConfigAttribute.builder()
                .type(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN)
                .name(VariableUtil.ProjectConfigAttributeTypeName.COMMIT_ID.nameValue)
                .value(null)
                .build());
        projectConfigAttributeList.add(ProjectConfigAttribute.builder()
                .type(VariableUtil.ProjectConfigAttributeTypeName.Type.SCAN)
                .name(VariableUtil.ProjectConfigAttributeTypeName.BASELINE_COMMIT_ID.nameValue)
                .value(null)
                .build());

        ScanTask scanTask = ScanTask.builder().projectConfig(ProjectConfig.builder().attributes(projectConfigAttributeList).build()).build();
        boolean isScanTaskDsr = ProjectServiceV3.isScanTaskDsr(scanTask);
        assertEquals(false, isScanTaskDsr);
    }

    @Test
    void isScanTaskDsr_scanTaskNull_false() throws AppException {
        ScanTask scanTask1 = null;
        boolean isScanTaskDsr = ProjectServiceV3.isScanTaskDsr(scanTask1);
        assertEquals(false, isScanTaskDsr);
    }

    @Test
    void isScanTaskDsr_bothCommitIdNotExist_false() throws AppException {
        List<ProjectConfigAttribute> projectConfigAttributeList1 = new ArrayList<>();
        ScanTask scanTask1 = ScanTask.builder().projectConfig(ProjectConfig.builder().attributes(projectConfigAttributeList1).build()).build();
        boolean isScanTaskDsr = ProjectServiceV3.isScanTaskDsr(scanTask1);
        assertEquals(false, isScanTaskDsr);
    }

    @Test
    void getBaselineRuleSetCountMap_noBaselineData_empty(){
        Map<String, String> baselineRuleSetCountMap = new HashMap<>();

        Map<String, String> map= projectServiceV3.getBaselineRuleSetCountMap(baselineRuleSetCountMap);
        assertEquals(0, map.size());
    }

    @Test
    void getBaselineRuleSetCountMap_baseline1RuleSet_1RuleSetData(){
        Map<String, String> baselineRuleSetCountMap = new HashMap<>();
        baselineRuleSetCountMap.put("baseline.M.issues","5");

        Map<String, String> map= projectServiceV3.getBaselineRuleSetCountMap(baselineRuleSetCountMap);
        assertEquals(true, map.containsKey("M"));
    }

    @Test
    void getBaselineRuleSetCountMap_baseline3RuleSet_3RuleSetData(){
        Map<String, String> baselineRuleSetCountMap = new HashMap<>();
        baselineRuleSetCountMap.put("baseline.issues","6");
        baselineRuleSetCountMap.put("baseline.M.issues","1");
        baselineRuleSetCountMap.put("baseline.X.issues","2");
        baselineRuleSetCountMap.put("baseline.S.issues","3");

        Map<String, String> map= projectServiceV3.getBaselineRuleSetCountMap(baselineRuleSetCountMap);

        for(Map.Entry<String,String> entry: map.entrySet()){
            System.out.println(entry.getKey()+" "+entry.getValue());
        }

        assertEquals(3, map.size());
        assertEquals(true, map.containsKey("M"));
        assertEquals(true, map.containsKey("X"));
        assertEquals(true, map.containsKey("S"));

        assertEquals("1", map.get("M"));
        assertEquals("2", map.get("X"));
        assertEquals("3", map.get("S"));

    }
}
