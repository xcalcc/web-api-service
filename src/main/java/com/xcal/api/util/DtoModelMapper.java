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

package com.xcal.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xcal.api.exception.AppException;
import org.modelmapper.ModelMapper;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DtoModelMapper extends RequestResponseBodyMethodProcessor {
    private static final ModelMapper modelMapper = new ModelMapper();
    private static final List<String> ignoreProperties = Arrays.asList("createdBy", "createdOn","modifiedBy", "modifiedOn");

    private EntityManager entityManager;

    public DtoModelMapper(ObjectMapper objectMapper, EntityManager entityManager) {
        super(Collections.singletonList(new MappingJackson2HttpMessageConverter(objectMapper)));
        this.entityManager = entityManager;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Dto.class);
    }

    @Override
    protected void validateIfApplicable(WebDataBinder binder, MethodParameter parameter) {
        binder.validate();
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Object dto = super.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
        modelMapper.getConfiguration().setPropertyCondition(condition -> !ignoreProperties.contains(condition.getMapping().getLastDestinationProperty().getName()));

        Object id = null;
        if(dto != null){
            id = getEntityId(dto);
        }

        if (id == null) {
            return modelMapper.map(dto, parameter.getParameterType());
        } else {
            Object persistedObject = entityManager.find(parameter.getParameterType(), id);
            logger.debug(CommonUtil.formatString("[resolveArgument] persistedObject: {}",persistedObject));
            if (persistedObject == null) {
                throw new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_COMMON_DTO_CONVERT_PARAMETER_NOT_EXIST.unifyErrorCode,
                        CommonUtil.formatString("{} {}. id: {}", parameter.getParameterType().getSimpleName(), AppException.ErrorCode.E_API_COMMON_DTO_CONVERT_PARAMETER_NOT_EXIST.messageTemplate, id));
            }
            modelMapper.map(dto, persistedObject);
            return persistedObject;
        }
    }

    @Override
    protected Object readWithMessageConverters(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType) throws IOException, HttpMediaTypeNotSupportedException {
        for (Annotation ann : parameter.getParameterAnnotations()) {
            Dto dtoType = AnnotationUtils.getAnnotation(ann, Dto.class);
            if (dtoType != null) {
                return super.readWithMessageConverters(inputMessage, parameter, dtoType.value());
            }
        }
        throw new RuntimeException();
    }

    private Object getEntityId(@NotNull(message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_NOTNULL) Object dto) {
        for (Field field : dto.getClass().getDeclaredFields()) {
            if (field.getAnnotation(Id.class) != null) {
                try {
                    field.setAccessible(true);
                    return field.get(dto);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }
}
