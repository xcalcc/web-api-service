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

import com.xcal.api.util.Dto;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMappingJacksonResponseBodyAdvice;

import java.util.Collection;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DtoMapperResponseBodyAdvice extends AbstractMappingJacksonResponseBodyAdvice {

    @NonNull
    ModelMapper modelMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return super.supports(returnType, converterType) && returnType.hasMethodAnnotation(Dto.class);
    }

    @Override
    protected void beforeBodyWriteInternal(MappingJacksonValue bodyContainer, MediaType contentType, MethodParameter returnType,
                                           ServerHttpRequest request, ServerHttpResponse response) {
        Dto ann = returnType.getMethodAnnotation(Dto.class);
        Assert.state(ann != null, "No Dto annotation");
        Class<?> dtoType = ann.value();
        Object value = bodyContainer.getValue();
        Object returnValue;
        log.info("[beforeBodyWriteInternal] value class: {}, dtoType: {}", value.getClass(), dtoType);
        if (value instanceof Page) {
            returnValue = ((Page<?>) value).map(it -> modelMapper.map(it, dtoType));
        } else if (value instanceof Collection) {
            returnValue = ((Collection<?>) value).stream().map(it -> modelMapper.map(it, dtoType));
        } else {
            returnValue = modelMapper.map(value, dtoType);
        }
        bodyContainer.setValue(returnValue);
    }
}
