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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xcal.api.util.DtoModelMapper;
import io.opentracing.contrib.java.spring.jaeger.starter.TracerBuilderCustomizer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Locale;

@Slf4j
@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WebMvcConfig implements WebMvcConfigurer {

    @NonNull AppProperties appProperties;

    @NonNull DBMessageInterpolator messageInterpolator;

    @Bean
    public ModelMapper modelMapper() {
        log.info("[modelMapper]");
        return new ModelMapper();
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder().build();
    }

    @Bean
    public JavaMailSender getJavaMailSender() {
        log.info("[getJavaMailSender]");
        AppProperties.Mail mail = appProperties.getMail();

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setProtocol(mail.getProtocol());
        mailSender.setHost(mail.getHost());
        mailSender.setPort(mail.getPort());
        mailSender.setUsername(mail.getUsername());
        mailSender.setPassword(mail.getPassword());
        mailSender.getJavaMailProperties().setProperty("mail.smtp.starttls.enable", mail.getStarttls());

        return mailSender;
    }

    @Bean
    public TracerBuilderCustomizer mdcBuilderCustomizer() {
        return builder -> builder.withScopeManager(new MDCScopeManager());
    }

    @NonNull
    private final ApplicationContext applicationContext;

    @NonNull
    private final EntityManager entityManager;

    private static final long MAX_AGE_SECS = 3600;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        log.info("[addArgumentResolvers] WebMvcConfig add DtoModelMapper");
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().applicationContext(this.applicationContext).build();
        argumentResolvers.add(new DtoModelMapper(objectMapper, entityManager));
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("[addCorsMappings] WebMvcConfig add CORS mappings");
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(MAX_AGE_SECS);
    }

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver localeResolver = new CookieLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        return localeResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("locale");
        registry.addInterceptor(localeChangeInterceptor);
    }


    @Bean
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setMessageInterpolator(messageInterpolator);
        return bean;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }



}
