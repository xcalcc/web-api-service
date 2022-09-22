package com.xcal.api.service;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.xcal.api.entity.*;
import com.xcal.api.util.VariableUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.mock;

@Slf4j
public class NotifyServiceTest {
    private NotifyService notifyService;
    @NonNull MeasureService measureService;

    private UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private UUID projectUUID = UUID.fromString("11111111-1111-1111-1111-111111111114");
    private final UUID scanTaskId = UUID.fromString("11111111-1111-1110-1111-111111111111");
    private User user = User.builder().id(userId).status(User.Status.ACTIVE).build();
    private Project project = Project.builder().id(projectUUID).projectId("12345").name("prj name").build();
    ProjectConfig projectConfig = ProjectConfig.builder().id(projectUUID).project(project)
            .attributes(Collections.singletonList(ProjectConfigAttribute.builder()
                    .type(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.type)
                    .name(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.nameValue)
                    .value("")
                    .build())).build();
    private ScanTask scanTask = ScanTask.builder().id(scanTaskId).status(ScanTask.Status.PENDING).project(project).projectConfig(projectConfig).build();


    @BeforeEach
    void setUp() {
        ObjectMapper om = new ObjectMapper();
        measureService = mock(MeasureService.class);
        notifyService = new NotifyService(om, measureService);
    }
    @Test
    void checkNotifyScanStatus() {
        notifyService.notifyScanResult(scanTask);
    }

}
