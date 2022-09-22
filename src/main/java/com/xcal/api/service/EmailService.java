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
import com.xcal.api.util.CommonUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EmailService {

    @NonNull SettingService settingService;
    @NonNull SpringTemplateEngine templateEngine;
    @NonNull JavaMailSender javaMailSender;
    @NonNull AppProperties appProperties;

    static String XCAL_SUPPORT_EMAIL = "xcal.support.email";

    static String XCAL_SUPPORT_EMAIL_TITLE = "xcal.support.email.title";

    public void contactUs(ContactUsRequest contactUsRequest, String currentUsername) throws AppException {
        log.debug("[contactUs] message: {}, currentUsername: {}", contactUsRequest, currentUsername);
        String content = CommonUtil.formatString("{} \n\n FROM: {} \n EMAIL: {}",
                contactUsRequest.getContent(), contactUsRequest.getName(), contactUsRequest.getEmail());
        Setting supportEmailSetting = settingService.findByKey(XCAL_SUPPORT_EMAIL).orElseThrow(
                () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SETTING_COMMON_NOT_FOUND.unifyErrorCode,
        CommonUtil.formatString("[{}] settingKey: {}", AppException.ErrorCode.E_API_SETTING_COMMON_NOT_FOUND.messageTemplate, XCAL_SUPPORT_EMAIL)));

        Setting supportEmailTitleSetting = settingService.findByKey(XCAL_SUPPORT_EMAIL_TITLE).orElseThrow(
                () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_SETTING_COMMON_NOT_FOUND.unifyErrorCode,
        CommonUtil.formatString("[{}] settingKey: {}", AppException.ErrorCode.E_API_SETTING_COMMON_NOT_FOUND.messageTemplate, XCAL_SUPPORT_EMAIL_TITLE)));
        this.sendMail(supportEmailSetting.getSettingValue(), supportEmailTitleSetting.getSettingValue(), content, currentUsername);
    }

    @Async
    public void sendTemplateMail(SendEmailRequest sendEmailRequest, String currentUsername) throws AppException {
        log.info("[sendTemplateMail] sendEmailRequest[subject: {}, to: {}],  currentUsername: {}", sendEmailRequest.getSubject(), sendEmailRequest.getTo(), currentUsername);
        Context context = new Context();
        context.setVariables(sendEmailRequest.getModel());
        String html = "";
        if (StringUtils.isNotBlank(sendEmailRequest.getTemplateName())) {
            html = templateEngine.process(sendEmailRequest.getTemplateName(), context);
        }
        MimeMessage message = this.prepareMimeMessage(sendEmailRequest.getFrom(), sendEmailRequest.getTo(), sendEmailRequest.getSubject(), html, sendEmailRequest.getAttachments());
        this.sendEmail(message);
        log.info("[sendTemplateMail] email sent, to: {}", sendEmailRequest.getTo());
    }

    void sendMail(String toEmail, String subject, String message, String currentUsername) throws AppException {
        log.info("[sendMail] toEmail: {}, subject: {}, message: {}, currentUsername: {}", toEmail, subject, message, currentUsername);
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        if (StringUtils.isNotBlank(toEmail) && toEmail.contains(",")) {
            mailMessage.setTo(toEmail.split(","));
        } else {
            mailMessage.setTo(toEmail);
        }
        mailMessage.setSubject(subject);
        mailMessage.setText(message);
        mailMessage.setFrom(appProperties.getMail().getFrom());
        this.sendEmail(mailMessage);
    }

    private void sendEmail(SimpleMailMessage message) throws AppException {
        this.updateJavaMailSender();
        try {
            this.javaMailSender.send(message);
        } catch (MailException e) {
            throw new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_EMAIL_COMMON_SENDMAIL.unifyErrorCode,
                    CommonUtil.formatString("[{}] MessagingException: {}", AppException.ErrorCode.E_API_EMAIL_COMMON_SENDMAIL.messageTemplate, e.getMessage()),e);
        }
    }

    private void sendEmail(MimeMessage message) throws AppException {
        this.updateJavaMailSender();
        try {
            this.javaMailSender.send(message);
        } catch (MailException e) {
            throw new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_EMAIL_COMMON_SENDMAIL.unifyErrorCode,
                    CommonUtil.formatString("[{}] MessagingException: {}", AppException.ErrorCode.E_API_EMAIL_COMMON_SENDMAIL.messageTemplate, e.getMessage()),e);
        }
    }

    private MimeMessage prepareMimeMessage(String from, String to, String subject, String content, Map<String, File> attachments) throws AppException {
        MimeMessage mimeMessage;
        try {
            MimeMessageHelper helper = new MimeMessageHelper(javaMailSender.createMimeMessage(), MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
            if (StringUtils.isNotBlank(to) && to.contains(",")) {
                helper.setTo(to.split(","));
            } else {
                helper.setTo(to);
            }
            helper.setText(content, true);
            helper.setSubject(subject);
            helper.setFrom(from);
            if (attachments != null) {
                for (Map.Entry<String, File> entry : attachments.entrySet()) {
                    helper.addAttachment(entry.getKey(), entry.getValue());
                }
            }
            mimeMessage = helper.getMimeMessage();
        } catch (MessagingException e) {
            throw new AppException(AppException.LEVEL_WARN, AppException.ERROR_CODE_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, AppException.ErrorCode.E_API_EMAIL_PREPARE_FAILED.unifyErrorCode,
                    CommonUtil.formatString("[{}] MessagingException: {}", AppException.ErrorCode.E_API_EMAIL_PREPARE_FAILED.messageTemplate, e.getMessage()),e);
        }
        return mimeMessage;
    }

    private void updateJavaMailSender() {
        this.settingService.updateEmailServerConfiguration();
        ((JavaMailSenderImpl)javaMailSender).setProtocol(appProperties.getMail().getProtocol());
        ((JavaMailSenderImpl)javaMailSender).setHost(appProperties.getMail().getHost());
        ((JavaMailSenderImpl)javaMailSender).setPort(appProperties.getMail().getPort());
        ((JavaMailSenderImpl)javaMailSender).setUsername(appProperties.getMail().getUsername());
        ((JavaMailSenderImpl)javaMailSender).setPassword(appProperties.getMail().getPassword());
        if (StringUtils.equalsIgnoreCase("true", appProperties.getMail().getStarttls())) {
            ((JavaMailSenderImpl)javaMailSender).getJavaMailProperties().setProperty("mail.smtp.starttls.enable", appProperties.getMail().getStarttls());
        } else {
            ((JavaMailSenderImpl)javaMailSender).getJavaMailProperties().setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
    }
}
