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

package com.xcal.api.config;

import com.fasterxml.classmate.TypeResolver;
import com.xcal.api.exception.AppException;
import com.xcal.api.security.UserPrincipal;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.builders.AlternateTypeBuilder;
import springfox.documentation.builders.AlternateTypePropertyBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.AlternateTypeRule;
import springfox.documentation.schema.AlternateTypeRuleConvention;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.*;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.lang.reflect.Type;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;

import static springfox.documentation.schema.AlternateTypeRules.newRule;

@Slf4j
@Configuration
@EnableSwagger2
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SwaggerConfig {

    @NonNull
    private TypeResolver typeResolver;

    @Bean
    public Docket apiDocket() {
        log.info("[apiDocket] add bean: Docket");
        final List<ResponseMessage> globalResponses = Arrays.asList(
                new ResponseMessageBuilder().code(200).message("Successful operation").build(),
                new ResponseMessageBuilder().code(400).message("Bad Request").build(),
                new ResponseMessageBuilder().code(404).message("Not Found").build(),
                new ResponseMessageBuilder().code(500).message("Server Internal Error").build());

        return new Docket(DocumentationType.SWAGGER_2)
                .select().apis(RequestHandlerSelectors.basePackage("com.xcal.api.controller")).build()
                .pathMapping("/")
                .directModelSubstitute(LocalDate.class, String.class)
                .genericModelSubstitutes(ResponseEntity.class)
                .globalResponseMessage(RequestMethod.GET, globalResponses)
                .globalResponseMessage(RequestMethod.POST, globalResponses)
                .globalResponseMessage(RequestMethod.DELETE, globalResponses)
                .globalResponseMessage(RequestMethod.PUT, globalResponses)
                .enableUrlTemplating(true)
                .tags(new Tag("File Service", "Operation about files"))
                .tags(new Tag("Project Service", "Operation about projects"))
                .tags(new Tag("Scan Service", "Operation about scanning"))
                .tags(new Tag("Issue Service", "Operation about issues"))
                .tags(new Tag("Report Service", "Operation about reports"))
                .tags(new Tag("Rule Service", "Operation about rules"))
                .tags(new Tag("User Service", "Operation about users"))
                .tags(new Tag("User Group Service", "Operation about user groups"))
                .tags(new Tag("Authentication Service", "Operation about authentication"))
                .tags(new Tag("License Service", "Operation about licensing"))
                .tags(new Tag("System Service", "System related operation"))
                .tags(new Tag("Authentication Service", "Operation about authentication"))
                .tags(new Tag("Internationalization Service", "Operation about Internationalization message"))
                .additionalModels(typeResolver.resolve(AppException.class))
                .ignoredParameterTypes(Principal.class, UserPrincipal.class);
    }

    @Bean
    UiConfiguration uiConfig() {
        log.info("[uiConfig] add bean: UiConfiguration");
        return UiConfigurationBuilder.builder()
                .deepLinking(true)
                .displayOperationId(true)
                .defaultModelsExpandDepth(1)
                .defaultModelExpandDepth(1)
                .defaultModelRendering(ModelRendering.EXAMPLE)
                .displayRequestDuration(false)
                .docExpansion(DocExpansion.NONE)
                .filter(false)
                .maxDisplayedTags(null)
                .operationsSorter(OperationsSorter.ALPHA)
                .showExtensions(false)
                .tagsSorter(TagsSorter.ALPHA)
                .supportedSubmitMethods(UiConfiguration.Constants.DEFAULT_SUBMIT_METHODS)
                .validatorUrl(null)
                .build();
    }

    @Bean
    public AlternateTypeRuleConvention alternateTypeRuleConvention(
            final TypeResolver resolver,
            final RepositoryRestConfiguration restConfiguration) {
        return new AlternateTypeRuleConvention() {

            @Override
            public int getOrder() {
                return Ordered.HIGHEST_PRECEDENCE;
            }

            @Override
            public List<AlternateTypeRule> rules() {
                return Arrays.asList(
                        newRule(resolver.resolve(Pageable.class), resolver.resolve(pageableMixin(restConfiguration))),
                        newRule(resolver.resolve(Locale.class), resolver.resolve(localeMixin())),
                        newRule(resolver.resolve(Date.class), resolver.resolve(Long.class))
                );
            }
        };
    }

    private Type pageableMixin(RepositoryRestConfiguration restConfiguration) {
        return new AlternateTypeBuilder()
                .fullyQualifiedClassName(
                        String.format("%s.generated.%s",
                                SwaggerConfig.class.getPackage().getName(),
                                Pageable.class.getSimpleName()))
                .withProperties(Arrays.asList(property(Integer.class, restConfiguration.getPageParamName()),
                        property(Integer.class, restConfiguration.getLimitParamName()),
                        property(String.class, restConfiguration.getSortParamName())))
                .build();
    }

    private Type localeMixin() {
        return new AlternateTypeBuilder()
                .fullyQualifiedClassName(
                        String.format("%s.generated.%s",
                                SwaggerConfig.class.getPackage().getName(),
                                Locale.class.getSimpleName()))
                .withProperties(Collections.singletonList(property(String.class, "lang")))
                .build();
    }

    private AlternateTypePropertyBuilder property(Class<?> type, String name) {
        return new AlternateTypePropertyBuilder()
                .withName(name)
                .withType(type)
                .withCanRead(true)
                .withCanWrite(true);
    }
}
