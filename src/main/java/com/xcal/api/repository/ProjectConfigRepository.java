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

package com.xcal.api.repository;

import com.xcal.api.entity.Project;
import com.xcal.api.entity.ProjectConfig;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectConfigRepository extends JpaRepository<ProjectConfig, UUID> {

    List<ProjectConfig> findByProjectAndName(Project project, String name);

    List<ProjectConfig> findByProject(Project project);

    List<ProjectConfig> findByProjectIsNull();

    Optional<ProjectConfig> findFirst1ByProjectAndStatus(Project project, ProjectConfig.Status status, Sort modifiedOn);

    void deleteByProject(Project project);


    @Query(value = "select pc.*\n" +
            "from scan_task st \n" +
            "left join project_config pc on st.project_config_id=pc.id\n" +
            "left join project_config_attribute pca on pc.id = pca.project_config_id\n" +
            "where \n" +
            "st.project_id=:projectUUID and\n" +
            "pca.name='configId' and\n" +
            "pca.value=:configId \n" +
            "order by st.created_on desc\n" +
            "limit 1",
            nativeQuery = true)
    Optional<ProjectConfig> findLatestProjectConfigByProjectUUIDAndConfigId(@Param("projectUUID")UUID projectUUID, @Param("configId") String configId);


    @Query(value = "select pc.*\n" +
            "from scan_task st\n" +
            "left join project_config pc on st.project_config_id=pc.id\n" +
            "left join project_config_attribute pca on pc.id = pca.project_config_id\n" +
            "where \n" +
            "st.project_id=:projectUUID and\n" +
            "pca.name='repoAction' and\n" +
            "pca.value=:repoAction \n" +
            "order by st.created_on desc\n" +
            "limit 1",
            nativeQuery = true)
    Optional<ProjectConfig> findLatestProjectConfigByProjectUUIDAndRepoAction(@Param("projectUUID")UUID projectUUID, @Param("repoAction") String repoAction);

    @Query(value = "select pca.value from scan_task st \n" +
            "left join project_config pc on st.project_config_id=pc.id\n" +
            "left join project_config_attribute pca on pc.id = pca.project_config_id\n" +
            "where \n" +
            "st.project_id=:projectUUID and\n" +
            "pca.name='configId' \n" +
            "order by pca.value desc\n" +
            "limit 1\n",
            nativeQuery = true)
    Optional<String> findLargestIdByProjectUUID(@Param("projectUUID")UUID projectUUID);

}
