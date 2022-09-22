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

import com.xcal.api.entity.ScanTask;
import com.xcal.api.entity.ScanTaskStatusLog;
import com.xcal.api.exception.AppException;
import com.xcal.api.service.AsyncScanService;
import com.xcal.api.service.ScanTaskService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AsyncExceptionHandlerController implements AsyncUncaughtExceptionHandler {

    @NonNull
    private final ApplicationContext applicationContext;

    private void handleAsyncPrepareAndCallScan(Object... obj) {
        Optional<ScanTask> scanTaskOptional = Arrays.stream(obj).filter(ScanTask.class::isInstance).map(ScanTask.class::cast).findFirst();
        if (scanTaskOptional.isPresent()) {
            ScanTask scanTask = scanTaskOptional.get();
            ScanTaskService scanTaskService = this.applicationContext.getBean("scanTaskService", ScanTaskService.class);
            if(scanTaskService == null) {
                log.error("[handleAsyncPrepareAndCallScan] scanTaskService should not be null");
            } else {
                try {
                    scanTaskService.updateScanTaskStatus(scanTask, ScanTaskStatusLog.Stage.PENDING, ScanTaskStatusLog.Status.FAILED, 100.0,
                            AppException.ErrorCode.E_API_COMMON_COMMON_INTERNAL_ERROR.unifyErrorCode, AppException.ErrorCode.E_API_COMMON_COMMON_INTERNAL_ERROR.messageTemplate, scanTask.getModifiedBy());
                } catch (AppException e) {
                    log.error("[handleAsyncPrepareAndCallScan] unexpected exception: {}", e.getStackTraceString());
                }
            }
        } else {
            log.error("[handleAsyncPrepareAndCallScan] unexpected exception, please check why ScanTask object cannot be found");
        }
    }

    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... obj) {
        log.error("[handleUncaughtException] Exception message: {}", throwable.getMessage());
        log.error("[handleUncaughtException] Method name: {}, Class name: {}", method.getName(), method.getDeclaringClass().getName());
        log.error("[handleUncaughtException] Parameter value: {}", Arrays.asList(obj));

        if(StringUtils.equals("prepareAndCallScan", method.getName()) && method.getDeclaringClass().equals(AsyncScanService.class)) {
            this.handleAsyncPrepareAndCallScan(obj);
        } else {
            log.error("[handleUncaughtException] async method is not handled yet: {}", method.getName());
        }
    }
}
