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

import com.xcal.api.entity.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrchestrationRepository extends RepositoryCustom{

    @NonNull EntityManager em;
    @NonNull ScanFileRepository scanFileRepository;

    public void deleteProject(Project project, boolean deleteRecord) {
        log.info("[deleteProject] project, id: {}, deleteRecord: {}", project.getId(), deleteRecord);
        this.deleteIssueTraceInProject(project);
        this.deleteIssueInProject(project);
        this.deleteScanTaskStatusLogInProject(project);
        this.deleteScanFileInProject(project);
        this.deleteScanTaskInProject(project);
        if(deleteRecord){
            this.deleteProjectConfigInProject(project);
            log.trace("[deleteProject] Deleting project record in project with em, id: {}", project.getId());
            em.refresh(project);
            em.remove(project);
        }
        log.trace("[deleteProject] Flushing the changes to database");
        em.flush();
    }

    private void deleteIssueTraceInProject(Project project){
        log.trace("[deleteIssueTraceInProject] Deleting issueTrace record in project, id: {}", project.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<IssueTrace> deleteIssueTraceQuery = cb.createCriteriaDelete(IssueTrace.class);
        Root<IssueTrace> issueTraceRoot = deleteIssueTraceQuery.from(IssueTrace.class);
        Subquery<IssueTrace> issueTraceSubQuery = deleteIssueTraceQuery.subquery(IssueTrace.class);
        Root<IssueTrace> issueTraceSubQueryRoot = issueTraceSubQuery.from(IssueTrace.class);
        issueTraceSubQuery.select(issueTraceSubQueryRoot);
        issueTraceSubQuery.where(cb.equal(issueTraceSubQueryRoot.get(IssueTrace_.ISSUE).get(Issue_.SCAN_TASK).get(ScanTask_.PROJECT), project));
        deleteIssueTraceQuery.where(issueTraceRoot.in(issueTraceSubQuery));
        em.createQuery(deleteIssueTraceQuery).executeUpdate();
    }

    private void deleteIssueInProject(Project project){
        log.trace("[deleteIssueInProject] Deleting issue record in project, id: {}", project.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<Issue> deleteIssueQuery = cb.createCriteriaDelete(Issue.class);
        Root<Issue> issueRoot = deleteIssueQuery.from(Issue.class);
        Subquery<Issue> issueSubQuery = deleteIssueQuery.subquery(Issue.class);
        Root<Issue> issueSubQueryRoot = issueSubQuery.from(Issue.class);
        issueSubQuery.select(issueSubQueryRoot);
        issueSubQuery.where(cb.equal(issueSubQueryRoot.get(Issue_.SCAN_TASK).get(ScanTask_.PROJECT), project));
        deleteIssueQuery.where(issueRoot.in(issueSubQuery));
        em.createQuery(deleteIssueQuery).executeUpdate();
    }

    private void deleteScanTaskStatusLogInProject(Project project){
        log.trace("[deleteScanTaskStatusLogInProject] Deleting scanTaskStatusLog record in project, id: {}", project.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<ScanTaskStatusLog> deleteScanTaskStatusLogQuery = cb.createCriteriaDelete(ScanTaskStatusLog.class);
        Root<ScanTaskStatusLog> scanTaskStatusLogRoot = deleteScanTaskStatusLogQuery.from(ScanTaskStatusLog.class);
        Subquery<ScanTaskStatusLog> scanTaskStatusLogSubQuery = deleteScanTaskStatusLogQuery.subquery(ScanTaskStatusLog.class);
        Root<ScanTaskStatusLog> scanTaskStatusLogSubQueryRoot = scanTaskStatusLogSubQuery.from(ScanTaskStatusLog.class);
        scanTaskStatusLogSubQuery.select(scanTaskStatusLogSubQueryRoot);
        scanTaskStatusLogSubQuery.where(cb.equal(scanTaskStatusLogSubQueryRoot.get(ScanTaskStatusLog_.SCAN_TASK).get(ScanTask_.PROJECT), project));
        deleteScanTaskStatusLogQuery.where(scanTaskStatusLogRoot.in(scanTaskStatusLogSubQuery));
        em.createQuery(deleteScanTaskStatusLogQuery).executeUpdate();
    }

    private void deleteScanFileInProject(Project project){
        log.trace("[deleteScanFileInProject] Deleting scanFile record in project, id: {}", project.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<ScanFile> deleteScanFileQuery = cb.createCriteriaDelete(ScanFile.class);
        Root<ScanFile> scanFileRoot = deleteScanFileQuery.from(ScanFile.class);
        Subquery<ScanFile> scanFileSubQuery = deleteScanFileQuery.subquery(ScanFile.class);
        Root<ScanFile> scanFileSubQueryRoot = scanFileSubQuery.from(ScanFile.class);
        scanFileSubQuery.select(scanFileSubQueryRoot);
        scanFileSubQuery.where(cb.equal(scanFileSubQueryRoot.get(ScanFile_.SCAN_TASK).get(ScanTask_.PROJECT), project));
        deleteScanFileQuery.where(scanFileRoot.in(scanFileSubQuery));
        em.createQuery(deleteScanFileQuery).executeUpdate();
    }

    private void deleteProjectConfigInProject(Project project){
        log.trace("[deleteProjectConfigInProject] Deleting projectConfig record in project, id: {}", project.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<ProjectConfig> deleteProjectConfigQuery = cb.createCriteriaDelete(ProjectConfig.class);
        Root<ProjectConfig> projectConfigRoot = deleteProjectConfigQuery.from(ProjectConfig.class);
        Subquery<ProjectConfig> projectConfigSubQuery = deleteProjectConfigQuery.subquery(ProjectConfig.class);
        Root<ProjectConfig> projectConfigSubQueryRoot = projectConfigSubQuery.from(ProjectConfig.class);
        projectConfigSubQuery.select(projectConfigSubQueryRoot);
        projectConfigSubQuery.where(cb.equal(projectConfigSubQueryRoot.get(ProjectConfig_.PROJECT), project));
        deleteProjectConfigQuery.where(projectConfigRoot.in(projectConfigSubQuery));
        em.createQuery(deleteProjectConfigQuery).executeUpdate();
    }
    private void deleteScanTaskInProject(Project project){
        log.trace("[deleteScanTaskInProject] Query the whole scanTask record in project for delete with em, id: {}", project.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ScanTask> scanTaskQuery = cb.createQuery(ScanTask.class);
        Root<ScanTask> scanTaskRoot = scanTaskQuery.from(ScanTask.class);
        scanTaskRoot.fetch(ScanTask_.SUMMARY, JoinType.LEFT);
        scanTaskRoot.fetch(ScanTask_.PROJECT, JoinType.LEFT);
        scanTaskQuery.where(cb.equal(scanTaskRoot.get(ScanTask_.PROJECT), project));
        List<ScanTask> scanTasks = em.createQuery(scanTaskQuery).getResultList();
        for(ScanTask scanTask: scanTasks){
            log.trace("[deleteProject] Deleting scanTask, id: {}", scanTask.getId());
            em.remove(scanTask);
        }
    }




    public void deleteScanTask(ScanTask scanTask, boolean deleteRecord, User currentUser) {
        log.info("[deleteScanTask] scanTask, id: {}, deleteRecord: {}", scanTask.getId(), deleteRecord);
        this.deleteIssueTraceInScanTask(scanTask);
        this.deleteIssueInScanTask(scanTask);
        this.deleteScanTaskStatusLogInScanTask(scanTask);
        this.deleteScanFileInScanTask(scanTask);
        if(deleteRecord){
            log.trace("[deleteScanTask] Deleting scanTask record with em, id: {}", scanTask.getId());
            em.refresh(scanTask);
            em.remove(scanTask);
        }else{
            scanTask.setSummary(new HashMap<>());
            scanTask.setStatus(ScanTask.Status.PENDING);
            scanTask.setModifiedBy(currentUser.getUsername());
            scanTask.setModifiedOn(new Date());
            em.merge(scanTask);
        }
        log.trace("[deleteScanTask] Flushing the changes to database");
        em.flush();
    }

    private void deleteIssueTraceInScanTask(ScanTask scanTask){
        log.trace("[deleteIssueTraceInScanTask] Deleting issueTrace record in scanTask, id: {}", scanTask.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<IssueTrace> deleteIssueTraceQuery = cb.createCriteriaDelete(IssueTrace.class);
        Root<IssueTrace> issueTraceRoot = deleteIssueTraceQuery.from(IssueTrace.class);
        Subquery<IssueTrace> issueTraceSubQuery = deleteIssueTraceQuery.subquery(IssueTrace.class);
        Root<IssueTrace> issueTraceSubQueryRoot = issueTraceSubQuery.from(IssueTrace.class);
        issueTraceSubQuery.select(issueTraceSubQueryRoot);
        issueTraceSubQuery.where(cb.equal(issueTraceSubQueryRoot.get(IssueTrace_.ISSUE).get(Issue_.SCAN_TASK), scanTask));
        deleteIssueTraceQuery.where(issueTraceRoot.in(issueTraceSubQuery));
        em.createQuery(deleteIssueTraceQuery).executeUpdate();
    }

    private void deleteIssueInScanTask(ScanTask scanTask){
        log.trace("[deleteIssueInScanTask] Deleting issue record in scanTask, id: {}", scanTask.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<Issue> deleteIssueQuery = cb.createCriteriaDelete(Issue.class);
        Root<Issue> issueRoot = deleteIssueQuery.from(Issue.class);
        Subquery<Issue> issueSubQuery = deleteIssueQuery.subquery(Issue.class);
        Root<Issue> issueSubQueryRoot = issueSubQuery.from(Issue.class);
        issueSubQuery.select(issueSubQueryRoot);
        issueSubQuery.where(cb.equal(issueSubQueryRoot.get(Issue_.SCAN_TASK), scanTask));
        deleteIssueQuery.where(issueRoot.in(issueSubQuery));
        em.createQuery(deleteIssueQuery).executeUpdate();
    }

    private void deleteScanTaskStatusLogInScanTask(ScanTask scanTask){
        log.trace("[deleteScanTaskStatusLogInScanTask] Deleting scanTaskStatusLog record in scanTask, id: {}", scanTask.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<ScanTaskStatusLog> deleteScanTaskStatusLogQuery = cb.createCriteriaDelete(ScanTaskStatusLog.class);
        Root<ScanTaskStatusLog> scanTaskStatusLogRoot = deleteScanTaskStatusLogQuery.from(ScanTaskStatusLog.class);
        Subquery<ScanTaskStatusLog> scanTaskStatusLogSubQuery = deleteScanTaskStatusLogQuery.subquery(ScanTaskStatusLog.class);
        Root<ScanTaskStatusLog> scanTaskStatusLogSubQueryRoot = scanTaskStatusLogSubQuery.from(ScanTaskStatusLog.class);
        scanTaskStatusLogSubQuery.select(scanTaskStatusLogSubQueryRoot);
        scanTaskStatusLogSubQuery.where(cb.equal(scanTaskStatusLogSubQueryRoot.get(ScanTaskStatusLog_.SCAN_TASK), scanTask));
        deleteScanTaskStatusLogQuery.where(scanTaskStatusLogRoot.in(scanTaskStatusLogSubQuery));
        em.createQuery(deleteScanTaskStatusLogQuery).executeUpdate();
    }

    private void deleteScanFileInScanTask(ScanTask scanTask){
        log.trace("[deleteScanFileInScanTask] Deleting scanFile record in scanTask, id: {}", scanTask.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<ScanFile> deleteScanFileQuery = cb.createCriteriaDelete(ScanFile.class);
        Root<ScanFile> scanFileRoot = deleteScanFileQuery.from(ScanFile.class);
        Subquery<ScanFile> scanFileSubQuery = deleteScanFileQuery.subquery(ScanFile.class);
        Root<ScanFile> scanFileSubQueryRoot = scanFileSubQuery.from(ScanFile.class);
        scanFileSubQuery.select(scanFileSubQueryRoot);
        scanFileSubQuery.where(cb.equal(scanFileSubQueryRoot.get(ScanFile_.SCAN_TASK), scanTask));
        deleteScanFileQuery.where(scanFileRoot.in(scanFileSubQuery));
        em.createQuery(deleteScanFileQuery).executeUpdate();
    }
}
