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

import com.xcal.api.config.AppProperties;
import com.xcal.api.entity.Setting;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.payload.ContactUsRequest;
import com.xcal.api.model.payload.SendEmailRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static com.xcal.api.service.EmailService.XCAL_SUPPORT_EMAIL;
import static com.xcal.api.service.EmailService.XCAL_SUPPORT_EMAIL_TITLE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Slf4j
class EmailServiceTest {
    private EmailService emailService;
    private SettingService settingService;
    private JavaMailSenderImpl javaMailSender;
    private SpringTemplateEngine templateEngine;
    private AppProperties appProperties;
    private String currentUserName = "user";

    @BeforeEach
    void setup() {
        settingService = mock(SettingService.class);
        templateEngine = mock(SpringTemplateEngine.class);
        javaMailSender = mock(JavaMailSenderImpl.class);
        appProperties = mock(AppProperties.class);
        AppProperties.Mail mailSetting = AppProperties.Mail.builder()
                .protocol("smtp")
                .host("smtpdm.aliyun.com")
                .port(465)
                .username("no-reply@xcalibyte.io")
                .password("xxxxxx")
                .starttls("false")
                .build();
        when(appProperties.getMail()).thenReturn(mailSetting);
        when(javaMailSender.getJavaMailProperties()).thenReturn(new Properties());
        doNothing().when(settingService).updateEmailServerConfiguration();
        emailService = new EmailService(settingService, templateEngine, javaMailSender, appProperties);
    }

    @Test
    void contactUsTestSuccess() {
        log.info("[contactUsTestSuccess]");
        ContactUsRequest contactUsRequest = ContactUsRequest.builder().name("testName").email("testxxx@to.com").content("want to know more").build();
        when(settingService.findByKey(XCAL_SUPPORT_EMAIL)).thenReturn(Optional.of(Setting.builder().settingValue("support@testxxx.com").build()));
        when(settingService.findByKey(XCAL_SUPPORT_EMAIL_TITLE)).thenReturn(Optional.of(Setting.builder().settingValue("test title").build()));
        doNothing().when(emailService.javaMailSender).send((SimpleMailMessage) any());
        assertDoesNotThrow(() -> emailService.contactUs(contactUsRequest, currentUserName));
    }

    @Test
    void contactUsTestSupportEmailNotFoundFail() {
        log.info("[contactUsTestSupportEmailNotFoundFail]");
        ContactUsRequest contactUsRequest = ContactUsRequest.builder().name("testName").email("testxxx@to.com").content("want to know more").build();
        when(settingService.findByKey(XCAL_SUPPORT_EMAIL)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> emailService.contactUs(contactUsRequest, currentUserName));
    }

    @Test
    void contactUsTestSupportEmailTitleNotFoundFail() {
        log.info("[contactUsTestSupportEmailTitleNotFoundFail]");
        ContactUsRequest contactUsRequest = ContactUsRequest.builder().name("testName").email("testxxx@to.com").content("want to know more").build();
        when(settingService.findByKey(XCAL_SUPPORT_EMAIL)).thenReturn(Optional.of(Setting.builder().settingValue("support@testxxx.com").build()));
        when(settingService.findByKey(XCAL_SUPPORT_EMAIL_TITLE)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> emailService.contactUs(contactUsRequest, currentUserName));
    }

    @Test
    void sendTemplateMailTestExceptionFail() {
        log.info("[sendTemplateMailTestExceptionFail]");
        Map<String, Object> model = new HashMap<>();
        model.put("username", currentUserName);
        SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                .from("test@from.com")
                .to("test@to.com")
                .model(model)
                .subject("test subject")
                .templateName("testTemplate")
                .build();
        Context context = new Context();
        context.setVariables(sendEmailRequest.getModel());
        when(javaMailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));
        when(templateEngine.process(anyString(), any(IContext.class))).thenReturn("fake result");
        doThrow(MailAuthenticationException.class).when(javaMailSender).send(any(MimeMessage.class));
        assertThrows(AppException.class, () -> emailService.sendTemplateMail(sendEmailRequest, currentUserName));
    }

    @Test
    void sendTemplateMailTestPrepareMimeMessageFail() {
        log.info("[sendTemplateMailTestExceptionFail]");
        Map<String, Object> model = new HashMap<>();
        model.put("username", currentUserName);
        SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                .from("test@from.com")
                .to("")
                .model(model)
                .subject("test subject")
                .templateName("testTemplate")
                .build();
        Context context = new Context();
        context.setVariables(sendEmailRequest.getModel());
        when(javaMailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));
        when(templateEngine.process(anyString(), any(IContext.class))).thenReturn("fake result");
        assertThrows(AppException.class, () -> emailService.sendTemplateMail(sendEmailRequest, currentUserName));
    }


    @Test
    void sendTemplateMailTestSuccess(){
        log.info("[sendTemplateMailTestSuccess]");
        Map<String, Object> model = new HashMap<>();
        model.put("username", currentUserName);
        SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                .from("test@from.com").to("test@to.com").model(model).subject("test subject").templateName("testTemplate").build();
        Context context = new Context();
        context.setVariables(sendEmailRequest.getModel());
        when(javaMailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));
        when(templateEngine.process(anyString(), any(IContext.class))).thenReturn("fake result");
        assertDoesNotThrow(() -> emailService.sendTemplateMail(sendEmailRequest, currentUserName));
    }

    @Test
    void sendMessageTestSuccess() {
        log.info("[sendMessageTestSuccess]");
        doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));
        assertDoesNotThrow(() -> emailService.sendMail("test to", "test subject", "test Message", currentUserName));
    }

    @Test
    void sendMessageTestFail() {
        log.info("[sendMessageTestFail]");
        doThrow(MailSendException.class).when(javaMailSender).send(any(SimpleMailMessage.class));
        assertThrows(AppException.class, () -> emailService.sendMail("test to", "test subject", "test Message", currentUserName));
    }

    @Test
    void sendSimpleEmailTestSuccessWithStarttls() {
        log.info("[sendSimpleEmailTestSuccessWithStarttls]");
        AppProperties.Mail mailSetting = AppProperties.Mail.builder()
                .protocol("smtp")
                .host("smtpdm.aliyun.com")
                .port(465)
                .username("no-reply@xcalibyte.io")
                .password("xxxxxx")
                .starttls("true")
                .build();
        when(appProperties.getMail()).thenReturn(mailSetting);
        assertDoesNotThrow(() -> emailService.sendMail("to@email.com","subject","content", "anyUser"));
    }
}
