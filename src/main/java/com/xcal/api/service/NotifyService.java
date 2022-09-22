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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xcal.api.entity.ScanTask;
import com.xcal.api.model.payload.SummaryResponse;
import com.xcal.api.model.payload.v3.SearchIssueGroupRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotifyService {

    @NonNull ObjectMapper om;
    @NonNull MeasureService measureService;

    @Value("${app.notify.service.enabled}")
    public boolean notifyServiceEnabled;

    @Value("${app.notify.service.url}")
    public String notifyServiceUrl;

    public Map<String, String> constructNotifyParam(ScanTask scanTask) throws JsonProcessingException {
        log.info("[constructNotifyParam] scanTaskId: {}", scanTask.getId());
        Map<String, String> notifyParam = new HashMap<>();

        boolean scanSuccess = false;
        if(scanTask.getStatus() == ScanTask.Status.COMPLETED) {
            scanSuccess = true;
        }

        notifyParam.put("success", String.valueOf(scanSuccess));
        notifyParam.put("scanTaskId", scanTask.getId().toString());
        notifyParam.put("projectId", scanTask.getProject().getProjectId());

        notifyParam.put("externalId", scanTask.getId().toString());
        notifyParam.put("repoId", scanTask.getId().toString());
        notifyParam.put("repoPath", scanTask.getSourceRoot() == null ? "": scanTask.getSourceRoot());

        SearchIssueGroupRequest searchIssueGroupRequest=SearchIssueGroupRequest.builder().build();
        searchIssueGroupRequest.setScanTaskId(scanTask.getId());
        //fill in project id if not provided
        if (searchIssueGroupRequest.getProjectId() == null) {
            searchIssueGroupRequest.setProjectId(scanTask.getProject().getId());
        }
        SummaryResponse summaryResponse = this.measureService.retrieveScanSummary(scanTask,searchIssueGroupRequest);
        notifyParam.put("result", om.writeValueAsString(summaryResponse));

        return notifyParam;
    }

    @Async
    public void notifyScanResult(ScanTask scanTask) {
        if(!notifyServiceEnabled) {
            return;
        }

        log.info("[notifyScanResult] scanTaskId: {}, notify url: {}", scanTask.getId(), notifyServiceUrl);

        RequestBody body;
        try {
            Map<String, String> requestParams = constructNotifyParam(scanTask);
            log.debug("[notifyScanResult] requestParams: {}", requestParams);
            body = RequestBody.create(MediaType.parse(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE), om.writeValueAsString(requestParams));
        } catch (JsonProcessingException e) {
            log.error("[notifyScanResult] create request body failed, error message: {}: {}", e.getClass(), e.getMessage());
            log.error("[notifyScanResult] error stack trace info: {}", Arrays.toString(e.getStackTrace()));
            return;
        }

        Request request = new Request.Builder().url(notifyServiceUrl).post(body).build();

        OkHttpClient client = new OkHttpClient();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (response.code() == HttpStatus.SC_OK) {
                log.info("[notifyScanResult] notify successfully， content: {}", response.body().string());
            } else {
                log.error("[notifyScanResult] notify failed, error message: {}", response.message());
            }
        } catch (IOException e) {
            log.error("[notifyScanResult] notify scan result failed, error message: {}: {}", e.getClass(), e.getMessage());
            log.error("[notifyScanResult] error stack trace info: {}", Arrays.toString(e.getStackTrace()));
        } finally {
            if(response != null) {
                response.close();
            }
        }
    }
}
