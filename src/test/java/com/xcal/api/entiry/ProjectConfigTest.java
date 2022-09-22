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

package com.xcal.api.entiry;

import com.xcal.api.entity.ProjectConfig;
import com.xcal.api.entity.ProjectConfigAttribute;
import com.xcal.api.util.VariableUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
class ProjectConfigTest {
    @BeforeEach
    void setup() {

    }

    @Test
    void getFirstAttribute_Success() {
        log.info("[getFirstAttribute_Success]");
        ProjectConfig projectConfig = ProjectConfig.builder().attributes((Arrays.asList(
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.nameValue).value("JAVA").build(),
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.nameValue).value("/test/path").build(),
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.nameValue).value("test_queue").build(),
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.nameValue).value("agent").build()
        ))).build();

        Optional<ProjectConfigAttribute> attributeOptional = projectConfig.getFirstAttribute(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE);
        Assertions.assertTrue(attributeOptional.isPresent());
        Assertions.assertEquals("JAVA", attributeOptional.get().getValue());
    }

    @Test
    void getFirstProjectConfigAttribute_NotExist_Success() {
        log.info("[getFirstProjectConfigAttribute_NotExist_Success]");
        ProjectConfig projectConfig = ProjectConfig.builder().attributes((Arrays.asList(
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.nameValue).value("JAVA").build(),
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.RELATIVE_SOURCE_PATH.nameValue).value("/test/path").build(),
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.nameValue).value("test_queue").build(),
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.nameValue).value("agent").build()
        ))).build();
        Assertions.assertFalse(projectConfig.getFirstAttribute(VariableUtil.ProjectConfigAttributeTypeName.UPLOAD_SOURCE).isPresent());
    }

    @Test
    void getFirstProjectConfigAttribute_MultipleAttributeGetFirst_Success() {
        log.info("[getFirstProjectConfigAttribute_MultipleAttributeGetFirst_Success]");
        ProjectConfig projectConfig = ProjectConfig.builder().attributes((Arrays.asList(
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.nameValue).value("JAVA").build(),
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.nameValue).value("c++").build(),
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.nameValue).value("test_queue").build(),
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.nameValue).value("agent").build()
        ))).build();
        Assertions.assertEquals("JAVA", projectConfig.getFirstAttribute(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE).map(ProjectConfigAttribute::getValue).get());
    }

    @Test
    void getProjectConfigAttributes_Success() {
        log.info("[getAttributes_Success]");
        ProjectConfig projectConfig = ProjectConfig.builder().attributes((Arrays.asList(
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.nameValue).value("JAVA").build(),
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.nameValue).value("c++").build(),
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.nameValue).value("test_queue").build(),
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.nameValue).value("agent").build()
        ))).build();

        List<ProjectConfigAttribute> attributes = projectConfig.getAttributes(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE);
        Assertions.assertEquals(2, attributes.size());
        Assertions.assertTrue(attributes.stream().map(ProjectConfigAttribute::getValue).anyMatch("JAVA"::equals));
        Assertions.assertTrue(attributes.stream().map(ProjectConfigAttribute::getValue).anyMatch("c++"::equals));
    }

    @Test
    void getProjectConfigAttributes_NullAttribute() {
        log.info("[getProjectConfigAttributes_NullAttribute]");
        ProjectConfig projectConfig = ProjectConfig.builder().attributes(null).build();

        List<ProjectConfigAttribute> attributes = projectConfig.getAttributes(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE);
        Assertions.assertEquals(0, attributes.size());
    }

    @Test
    void getFirstProjectConfigAttributeValue_Success() {
        log.info("[getFirstProjectConfigAttributeValue_Success]");
        ProjectConfig projectConfig = ProjectConfig.builder().attributes((Arrays.asList(
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.nameValue).value("JAVA").build(),
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.nameValue).value("c++").build(),
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.nameValue).value("test_queue").build(),
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.nameValue).value("agent").build()
        ))).build();

        String attribute = projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE, null);
        Assertions.assertEquals("JAVA", attribute);
    }

    @Test
    void getFirstProjectConfigAttributeValue_NotExistDefaultExist_Success() {
        log.info("[getFirstProjectConfigAttributeValue_Success]");
        ProjectConfig projectConfig = ProjectConfig.builder().attributes((Arrays.asList(
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.nameValue).value("JAVA").build(),
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.nameValue).value("c++").build(),
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.nameValue).value("test_queue").build(),
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.nameValue).value("agent").build()
        ))).build();

        String attribute = projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.BUILD_COMMAND, "NO_COMMAND");
        Assertions.assertEquals("NO_COMMAND", attribute);
    }

    @Test
    void getFirstProjectConfigAttributeValue_NotExistDefaultNull_Success() {
        log.info("[getFirstProjectConfigAttributeValue_Success]");
        ProjectConfig projectConfig = ProjectConfig.builder().attributes((Arrays.asList(
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.nameValue).value("JAVA").build(),
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.LANGUAGE.nameValue).value("c++").build(),
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.JOB_QUEUE_NAME.nameValue).value("test_queue").build(),
                ProjectConfigAttribute.builder()
                        .type(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.type)
                        .name(VariableUtil.ProjectConfigAttributeTypeName.SCAN_TYPE.nameValue).value("agent").build()
        ))).build();

        String attribute = projectConfig.getFirstAttributeValue(VariableUtil.ProjectConfigAttributeTypeName.BUILD_COMMAND, null);
        Assertions.assertNull(attribute);
    }
}
