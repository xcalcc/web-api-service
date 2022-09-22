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

import com.xcal.api.exception.AppException;
import com.xcal.api.service.I18nService;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.TracerUtil;
import com.xcal.api.util.ValidErrorCode;
import io.jaegertracing.internal.JaegerSpanContext;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Locale;

import static com.xcal.api.exception.AppException.*;
import static com.xcal.api.exception.AppException.ErrorCode.E_API_COMMON_COMMON_INTERNAL_ERROR;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GlobalControllerAdviser {

    @NonNull Tracer tracer;

    @NonNull I18nService i18nService;

    @Qualifier("localI18nMessageSource")
    @NonNull MessageSource localI18nMessageSource;
    /**
     * <p>
     * General AppException response handler.
     * Return caught AppException
     * </p>
     *
     * @param e appException caught
     * @return AppException
     */
    @ExceptionHandler(AppException.class)
    @ResponseBody
    public ResponseEntity<AppException> handleAppException(AppException e, Locale locale) {
        String spanId = this.activeJaegerSpan();
        log.warn("[handleAppException] spanId: {}, Exception, {}: {}", spanId, e.getClass(), e.getMessage());
        if (this.printStackTrace(e)) {
            if (StringUtils.equalsIgnoreCase(LEVEL_ERROR, e.getLevel())) {
                log.error("[handleAppException] Stack trace: {}", e.getStackTraceString(),e);
            } else if (StringUtils.equalsIgnoreCase(LEVEL_WARN, e.getLevel())) {
                log.warn("[handleAppException] Stack trace: {}", e.getStackTraceString(),e);
            } else {
                log.info("[handleAppException] Stack trace: {}", e.getStackTraceString());
            }
        }
        int httpStatus = HttpURLConnection.HTTP_BAD_REQUEST;
        if (e.getResponseCode() != null) {
            try {
                if (e.getResponseCode() >= 100 && e.getResponseCode() < 600) {
                    httpStatus = e.getResponseCode();
                }
            } catch (IllegalArgumentException iae) {
                log.warn("[handleAppException] response code: {}, {}: {}", e.getResponseCode(), iae.getClass(), iae.getMessage());
            }
        }
        TracerUtil.setTag(tracer, "error", "true");
        String messageInSquareBrackets = CommonUtil.formatMsgInSquareBrackets(e.getMessage());
        String message = messageInSquareBrackets;
        String localizedMessage = "";

        if(StringUtils.isNotEmpty(message)){
            message = i18nService.formatString(messageInSquareBrackets,Locale.ENGLISH);
            localizedMessage = i18nService.formatString(messageInSquareBrackets,locale);
        }
        return ResponseEntity.status(httpStatus).body(new AppException(e.getLevel(), e.getErrorCode(), e.getResponseCode(), e.getUnifyErrorCode(), message, localizedMessage, e));
    }

    private boolean printStackTrace(AppException e) {
        boolean result = true;
        if (StringUtils.isBlank(e.getStackTraceString()) ||
                StringUtils.equalsIgnoreCase(e.getErrorCode(), ERROR_CODE_DATA_NOT_FOUND) ||
                StringUtils.equalsIgnoreCase(e.getErrorCode(), ERROR_CODE_DATA_ALREADY_EXIST)) {
            result = false;
        }
        return result;
    }

    /**
     * <p>
     * General Exception response handler.
     * If the exception is instance of AccessDeniedException, wrap it into AppException
     * Return caught exception and wrap into readable object for frontend.
     * </p>
     *
     * @param e AccessDeniedException caught
     * @return AppException
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ResponseEntity<AppException> handleException(AccessDeniedException e) {
        String spanId = this.activeJaegerSpan();
        log.warn("[handleAppException] spanId: {}, Exception, {}: {}", spanId, e.getClass(), e.getMessage());
        log.error("[handleAppException] Stack trace: {}", ExceptionUtils.getStackTrace(e));
        TracerUtil.setTag(tracer, "error", "true");
        int httpStatus = HttpURLConnection.HTTP_FORBIDDEN;
        return ResponseEntity.status(httpStatus).body(new AppException(LEVEL_ERROR, ERROR_CODE_UNAUTHORIZED, httpStatus, null, CommonUtil.formatString("[{}]",e.getMessage()), e));
    }

    /**
     * <p>
     * General Exception response handler.
     * If the exception is instance of MethodArgumentTypeMismatchException, wrap it into AppException
     * Return caught exception and wrap into readable object for frontend.
     * </p>
     *
     * @param e MethodArgumentTypeMismatchException caught
     * @return AppException
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseBody
    public ResponseEntity<AppException> handleException(MethodArgumentTypeMismatchException e) {
        String spanId = this.activeJaegerSpan();
        log.warn("[handleAppException] spanId: {}, Exception, {}: {}", spanId, e.getClass(), e.getMessage());
        log.error("[handleAppException] Stack trace: {}", ExceptionUtils.getStackTrace(e));
        TracerUtil.setTag(tracer, "error", "true");
        int httpStatus = HttpURLConnection.HTTP_BAD_REQUEST;
        return ResponseEntity.status(httpStatus).body(new AppException(LEVEL_ERROR, ERROR_CODE_BAD_REQUEST, httpStatus, null, CommonUtil.formatString("[{}]",e.getMessage()), e));
    }

    /**
     * <p>
     * General Exception response handler.
     * If the exception is instance of MethodArgumentNotValidException, wrap it into AppException
     * Return caught exception and wrap into readable object for frontend.
     * </p>
     *
     * @param e MethodArgumentNotValidException caught
     * @return AppException
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<AppException> handleMethodArgumentNotValidException(MethodArgumentNotValidException e,Locale locale) {
        String spanId = this.activeJaegerSpan();
        log.warn("[handleAppException] spanId: {}, Exception, {}: {}", spanId, e.getClass(), e.getMessage());
        log.error("[handleAppException] Stack trace: {}", ExceptionUtils.getStackTrace(e));
        TracerUtil.setTag(tracer, "error", "true");
        BindingResult result = e.getBindingResult();
        StringBuilder errorMsg = new StringBuilder() ;
        if (result.hasErrors()) {
            List<FieldError> fieldErrors = result.getFieldErrors();
            fieldErrors.forEach(error -> {
                log.error("field: {}, msg: {}" ,error.getField() , error.getDefaultMessage());
                errorMsg.append(error.getField()).append(error.getDefaultMessage()).append(";");
            });
        }
        String msgContent = errorMsg.toString();
        String message = msgContent;
        String localizedMessage = msgContent;
        String unifyErrorCode = null;
        ValidErrorCode validErrorCode = e.getParameter().getParameterAnnotation(ValidErrorCode.class);
        if (validErrorCode != null) {
            unifyErrorCode = validErrorCode.value().unifyErrorCode;
            message = CommonUtil.formatString("[{}: {}]", i18nService.formatString(validErrorCode.value().messageTemplate, Locale.ENGLISH), msgContent);
            localizedMessage = CommonUtil.formatString("[{}: {}]", i18nService.formatString(validErrorCode.value().messageTemplate, locale), msgContent);
        }else {
            message = CommonUtil.formatString("[{}]",message);
            localizedMessage = CommonUtil.formatString("[{}]",localizedMessage);
        }
        int httpStatus = HttpURLConnection.HTTP_BAD_REQUEST;
        return ResponseEntity.status(httpStatus).body(new AppException(LEVEL_ERROR, ERROR_CODE_BAD_REQUEST, httpStatus, unifyErrorCode, message, localizedMessage, e));
    }

    /**
     * <p>
     * General Exception response handler.
     * If the exception is not instance of AppException, wrap it into AppException
     * Return caught exception and wrap into readable object for frontend.
     * </p>
     *
     * @param e exception caught
     * @return AppException
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<AppException> handleException(Exception e,Locale locale) {
        String spanId = this.activeJaegerSpan();
        log.warn("[handleAppException] spanId: {}, Exception, {}: {}", spanId, e.getClass(), e.getMessage());
        log.error("[handleAppException] Stack trace: {}", ExceptionUtils.getStackTrace(e));
        TracerUtil.setTag(tracer, "error", "true");

        String defaultExceptionDescription=localI18nMessageSource.getMessage("global.exception.message",null,locale);

        int httpStatus = HttpURLConnection.HTTP_INTERNAL_ERROR;
        return ResponseEntity.status(httpStatus).body(new AppException(LEVEL_ERROR, ERROR_CODE_INTERNAL_ERROR, httpStatus, E_API_COMMON_COMMON_INTERNAL_ERROR.unifyErrorCode, CommonUtil.formatString("[{}]",defaultExceptionDescription), e));
    }

    private String activeJaegerSpan() {
        String spanId = "";
        Span activeSpan = tracer.activeSpan();
        if (activeSpan != null) {
            SpanContext spanContext = activeSpan.context();
            if (spanContext instanceof JaegerSpanContext) {
                spanId = ((JaegerSpanContext) spanContext).getTraceId();
            }
        }
        return spanId;
    }
}
