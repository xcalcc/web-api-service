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
import com.xcal.api.dao.UserDao;
import com.xcal.api.entity.*;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.UserCountDto;
import com.xcal.api.model.dto.UserDto;
import com.xcal.api.model.dto.UserGroupDto;
import com.xcal.api.model.payload.ChangePasswordRequest;
import com.xcal.api.model.payload.NewUserRequest;
import com.xcal.api.model.payload.SendEmailRequest;
import com.xcal.api.model.payload.ValidationResult;
import com.xcal.api.model.payload.v3.SearchIssueGroupRequest;
import com.xcal.api.repository.IssueRepository;
import com.xcal.api.repository.LoginFailLogRepository;
import com.xcal.api.repository.UserGroupRepository;
import com.xcal.api.repository.UserRepository;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.MessagesTemplate;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserService {

    static final String USER_GROUP_NAME_ADMIN = "admin";

    @Value("${app.auth.maxLoginRetryNo:10}")
    Integer maxLoginRetryNo;
    @NonNull UserRepository userRepository;
    @NonNull UserGroupRepository userGroupRepository;
    @NonNull LoginFailLogRepository loginFailLogRepository;
    @NonNull IssueRepository issueRepository;
    @NonNull LicenseService licenseService;
    @NonNull SettingService settingService;
    @NonNull EmailService emailService;
    @NonNull AppProperties appProperties;
    @NonNull ModelMapper modelMapper;
    @NonNull PasswordEncoder passwordEncoder;
    @NonNull Validator validator;
    @NonNull I18nService i18nService;
    @NonNull UserDao userDao;

    public Optional<User> findByUsernameOrEmail(String loginName) {
        log.debug("[findByUsernameOrEmail] loginName: {}", loginName);
        Optional<User> userOptional = this.findByUsername(loginName);
        if (!userOptional.isPresent()) {
            userOptional = this.findByEmail(loginName);
        }
        return userOptional;
    }

    public Optional<User> findById(UUID id) {
        log.debug("[findById] id: {}", id);
        return userRepository.findById(id);
    }

    Optional<User> findByEmail(String email) {
        log.debug("[findByEmail] email: {}", email);
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        log.trace("[findByUsername] username: {}", username);
        return userRepository.findByUsername(username);
    }

    public User addUser(NewUserRequest newUserRequest, User currentUser) {
        log.debug("[addUser] newUserRequest: {}", newUserRequest);
        Date now = new Date();
        User user = User.builder().password(passwordEncoder.encode(newUserRequest.getPassword()))
                .displayName(newUserRequest.getDisplayName())
                .username(newUserRequest.getUsername())
                .email(newUserRequest.getEmail())
                .status(User.Status.ACTIVE)
                .userGroups(new ArrayList<>())
                .createdOn(now)
                .createdBy(currentUser.getUsername())
                .modifiedOn(now)
                .modifiedBy(currentUser.getUsername())
                .build();

        if (UserDto.IS_ADMIN_YES.equals(newUserRequest.getIsAdmin())) {
            Optional<UserGroup> userGroup = this.retrieveAdminUserGroup();
            if (userGroup.isPresent()) {
                user.getUserGroups().add(userGroup.get());
            }
        }
        user = userRepository.save(user);
        return user;
    }

    public void validateUser(NewUserRequest newUserRequest) throws AppException {
        log.debug("[validateUser] newUserRequest: {}", newUserRequest);
        ValidationResult validateUserExistResult = this.validateUserExist(newUserRequest);
        if (validateUserExistResult.getStatus() != ValidationResult.Status.SUCCESS) {
            throw validateUserExistResult.getException();
        }
        ValidationResult validateUserFormatResult = this.validateUserFormat(newUserRequest);
        if (validateUserFormatResult.getStatus() != ValidationResult.Status.SUCCESS) {
            throw validateUserFormatResult.getException();
        }
    }

    public ValidationResult validateUserExist(NewUserRequest newUserRequest) {
        log.debug("[validateUserExist] NewUserRequest: {},", newUserRequest);
        List<String> messageList = new ArrayList<>();
        if (userRepository.findByUsername(newUserRequest.getUsername().toLowerCase()).isPresent()) {
            messageList.add(CommonUtil.formatString("{}, username:{}", AppException.ErrorCode.E_API_USER_VALIDATEUSERS_USERNAMEEXIST.messageTemplate, newUserRequest.getUsername()));
        }
        if (userRepository.findByEmail(newUserRequest.getEmail()).isPresent()) {
            messageList.add(CommonUtil.formatString("{}, email:{}", AppException.ErrorCode.E_API_USER_VALIDATEUSERS_EMAILEXIST.messageTemplate, newUserRequest.getEmail()));
        }
        ValidationResult validationResult;
        if (messageList.isEmpty()) {
            validationResult = ValidationResult.builder().status(ValidationResult.Status.SUCCESS).message("").build();
        } else {
            String message = StringUtils.join(messageList, ";");
            AppException appException = new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_ALREADY_EXIST, HttpURLConnection.HTTP_CONFLICT, null, message);
            validationResult = ValidationResult.builder().status(ValidationResult.Status.FAIL).message(message).exception(appException).build();
        }
        return validationResult;
    }

    public ValidationResult validateUserFormat(NewUserRequest newUserRequest) {
        log.debug("[validateUserFormat] NewUserRequest: {},", newUserRequest);
        List<String> messageList = new ArrayList<>();
        Set<ConstraintViolation<NewUserRequest>> constraintViolations = validator.validate(newUserRequest);
        for (ConstraintViolation<NewUserRequest> constraintViolation : constraintViolations) {
            messageList.add(CommonUtil.formatString("{} {}", constraintViolation.getPropertyPath(), constraintViolation.getMessage()));
        }
        ValidationResult validationResult;
        if (messageList.isEmpty()) {
            validationResult = ValidationResult.builder().status(ValidationResult.Status.SUCCESS).message("").build();
        } else {
            String message = StringUtils.join(messageList, ";");
            AppException appException = new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, null, message);
            validationResult = ValidationResult.builder().status(ValidationResult.Status.FAIL).message(message).exception(appException).build();
        }
        return validationResult;
    }

    public void sendNewUserEmail(String username, String displayName, String email, String password, Locale locale, User currentUser) throws AppException {
        log.info("[sendNewUserEmail] username: {}, displayName: {}, email: {}, locale: {}, currentUserName: {}", username, displayName, email, locale, currentUser.getUsername());

        Map<String, Object> model = new HashMap<>();
        model.put("username", username);
        model.put("displayName", displayName);
        model.put("password", password);
        model.put("uiHost", String.format(
                "%s://%s:%d",
                appProperties.getUiProtocol(),
                appProperties.getUiHost(),
                appProperties.getUiPort()
        ));

        AppProperties.Mail mailSetting = this.settingService.getEmailServerConfiguration();
        SendEmailRequest request = SendEmailRequest.builder()
                .from(mailSetting.getFrom())
                .to(email)
                .subject(CommonUtil.formatString(
                        (locale == Locale.SIMPLIFIED_CHINESE) ?
                                AppProperties.EMAIL_SUBJECT_PATTERN_NEW_USER_ZH_CN :
                                AppProperties.EMAIL_SUBJECT_PATTERN_NEW_USER,
                        displayName
                ))
                .templateName((locale == Locale.SIMPLIFIED_CHINESE) ?
                        AppProperties.EMAIL_TEMPLATE_PREFIX_NEW_USER_ZH_CN :
                        AppProperties.EMAIL_TEMPLATE_PREFIX_NEW_USER)
                .model(model)
                .build();

        this.emailService.sendTemplateMail(request, currentUser.getUsername());
    }

    public User updateUser(UserDto userDto, String currentUsername) throws AppException {
        log.debug("[updateUser] user: {}", userDto);

        Optional<User> userOptional = this.findByUsername(userDto.getUsername());
        User user = userOptional.orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] username: {}", AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.messageTemplate, userDto.getUsername())));

        Optional<User> emailUser = this.findByEmail(userDto.getEmail());
        if (emailUser.isPresent() && !StringUtils.equalsIgnoreCase(emailUser.get().getUsername(), userDto.getUsername())) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_ALREADY_EXIST, HttpURLConnection.HTTP_CONFLICT, AppException.ErrorCode.E_API_USER_VALIDATEUSERS_EMAILEXIST.unifyErrorCode,
                    CommonUtil.formatString("[{}] email: {}", AppException.ErrorCode.E_API_USER_VALIDATEUSERS_EMAILEXIST.messageTemplate, userDto.getEmail()));
        }
        Date now = new Date();
        user.setDisplayName(userDto.getDisplayName());
        user.setEmail(userDto.getEmail());
        user.setModifiedOn(now);
        user.setModifiedBy(currentUsername);

        boolean found = false;
        Optional<UserGroup> adminUserGroup = this.retrieveAdminUserGroup();
        if (adminUserGroup.isPresent()) {
            Iterator<UserGroup> userGroupIterator = user.getUserGroups().iterator();
            while (userGroupIterator.hasNext()) {
                UserGroup userGroup = userGroupIterator.next();
                if (adminUserGroup.get().getId().equals(userGroup.getId())) {
                    if (UserDto.IS_ADMIN_NO.equals(userDto.getIsAdmin())) {
                        userGroupIterator.remove();
                    }
                    found = true;
                }
            }
            if (UserDto.IS_ADMIN_YES.equals(userDto.getIsAdmin()) && !found) {
                user.getUserGroups().add(adminUserGroup.get());
            }
        }
        user = userRepository.save(user);
        return user;
    }

    public Page<User> findAll(Pageable pageable) {
        log.debug("[findAll] pageable: {}", pageable);
        return userRepository.findAll(pageable);
    }

    public List<NewUserRequest> csvToNewUserList(MultipartFile file) throws AppException {
        log.debug("[csvToUserList] file size: {}", file != null ? file.getSize() : 0);
        List<NewUserRequest> users = new ArrayList<>();
        if (file == null) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_INCORRECT_PARAM, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_COMMON_MISSING_FILE.unifyErrorCode, AppException.ErrorCode.E_API_COMMON_MISSING_FILE.messageTemplate);
        }
        try (InputStreamReader isr = new InputStreamReader(file.getInputStream())) {
            Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(isr);
            int c = 1;
            for (CSVRecord record : records) {
                if (c == 1) { // the first line is header, so skip
                    c++;
                    continue;
                }
                NewUserRequest newUserRequest = NewUserRequest.builder().displayName(record.get(0))
                        .username(record.get(1))
                        .password(record.get(2))
                        .email(record.get(3))
                        .isAdmin(record.get(4))
                        .build();
                users.add(newUserRequest);
                log.debug(newUserRequest.toString());
            }
        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_FILE_COMMON_INVALID_FORMAT.unifyErrorCode, AppException.ErrorCode.E_API_FILE_COMMON_INVALID_FORMAT.messageTemplate);
        }
        return users;
    }

    Page<User> simpleSearch(User searchUser, Pageable pageable) {
        ExampleMatcher em = ExampleMatcher.matching();
        em = em.withIgnoreCase()
                .withIgnorePaths("userGroups")
                .withMatcher("id", matcher -> matcher.ignoreCase().contains())
                .withMatcher("username", matcher -> matcher.ignoreCase().contains())
                .withMatcher("displayName", matcher -> matcher.ignoreCase().contains())
                .withMatcher("email", matcher -> matcher.ignoreCase().contains());
        return this.userRepository.findAll(Example.of(searchUser, em), pageable);
    }

    public void deleteById(UUID id) {
        log.debug("[deleteById] id: {}", id);
        int updateIssueCount = issueRepository.updateIssueAssignToNullByUserId(id);
        log.debug("[deleteById] update {} issues set assign to null", updateIssueCount);
        userRepository.deleteById(id);
    }

    public void updatePassword(ChangePasswordRequest changePasswordRequest, String currentUsername) throws AppException {
        log.debug("[updatePassword] id: {}, principal username: {}", changePasswordRequest.getId(), currentUsername);
        User user;
        Optional<User> userOptional = this.findById(changePasswordRequest.getId());
        if (!userOptional.isPresent()) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.unifyErrorCode,
                    CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.messageTemplate, changePasswordRequest.getId()));
        } else {
            user = userOptional.get();
        }
        if (StringUtils.isNoneEmpty(changePasswordRequest.getOldPassword())) {
            if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_INCONSISTENT, HttpURLConnection.HTTP_CONFLICT, AppException.ErrorCode.E_API_USER_UPDATEPASSWORD_INCORRECT_PASSWORD.unifyErrorCode, AppException.ErrorCode.E_API_USER_UPDATEPASSWORD_INCORRECT_PASSWORD.messageTemplate);
            }
        }
        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        user.setModifiedOn(new Date());
        user.setModifiedBy(currentUsername);
        userRepository.save(user);
    }

    public List<User> addUsers(List<NewUserRequest> newUserRequestList, User currentUser) {
        log.debug("[addUsers] newUserRequestList size: {}", newUserRequestList.size());
        List<User> users = new ArrayList<>();
        Date now = new Date();
        for (NewUserRequest newUserRequest : newUserRequestList) {
            User user = User.builder().password(passwordEncoder.encode(newUserRequest.getPassword()))
                    .displayName(newUserRequest.getDisplayName())
                    .username(newUserRequest.getUsername())
                    .email(newUserRequest.getEmail())
                    .status(User.Status.ACTIVE)
                    .userGroups(new ArrayList<>())
                    .createdOn(now)
                    .createdBy(currentUser.getUsername())
                    .modifiedOn(now)
                    .modifiedBy(currentUser.getUsername())
                    .build();
            if (UserDto.IS_ADMIN_YES.equals(newUserRequest.getIsAdmin())) {
                Optional<UserGroup> userGroup = this.retrieveAdminUserGroup();
                userGroup.ifPresent(group -> user.getUserGroups().add(group));
            }
            users.add(user);
        }
        return userRepository.saveAll(users);
    }

    public void validateUsers(List<NewUserRequest> newUserRequestList, final Locale locale) throws AppException {
        log.debug("[validateUsers] NewUserRequest size: {}", newUserRequestList.size());
        List<String> validationMessageList = new ArrayList<>();
        // rowErrorMsgHeader
        // en(Default): row ${num}:${msg}
        // ch_CN: 第 {num} 行:${msg}
        String key = AppException.ErrorCode.E_API_USER_VALIDATEUSERS_ROWERROR.messageTemplate;
        Matcher matcher = I18nService.regWithKeyPattern.matcher(key);
        while (matcher.find()) {
            key = matcher.group(1);
        }
        String rowLocalizedTemplate = i18nService.getMessageByKey(key, locale);

        List<I18nMessage> validateUserMessage = this.i18nService.getI18nMessageByKeyPrefix("e.api.user.", locale);
        if (validateUserMessage.isEmpty()) {
            validateUserMessage = this.i18nService.getI18nMessageByKeyPrefix("e.api.user.", Locale.ENGLISH);
        }
        Map<String, I18nMessage> validateUserMessageMap = validateUserMessage.stream().collect(Collectors.toMap(I18nMessage::getKey, i -> i));

        for (int i = 0; i < newUserRequestList.size(); i++) {
            ValidationResult validationUserExistResult = this.validateUserExist(newUserRequestList.get(i));
            if (validationUserExistResult.getStatus() != ValidationResult.Status.SUCCESS) {
                Map<String, String> errorMsgMap = new HashMap<>();
                errorMsgMap.put("num", String.valueOf(i + 1));
                errorMsgMap.put("msg", validationUserExistResult.getMessage());
                validationMessageList.add(I18nService.formatString(rowLocalizedTemplate, errorMsgMap, validateUserMessageMap));
            } else {
                ValidationResult validationUserFormatResult = this.validateUserFormat(newUserRequestList.get(i));
                if (validationUserFormatResult.getStatus() != ValidationResult.Status.SUCCESS) {
                    Map<String, String> errorMsgMap = new HashMap<>();
                    errorMsgMap.put("num", String.valueOf(i + 1));
                    errorMsgMap.put("msg", validationUserFormatResult.getMessage());
                    validationMessageList.add(I18nService.formatString(rowLocalizedTemplate, errorMsgMap, null));
                }
            }
        }
        if (!validationMessageList.isEmpty()) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_USER_VALIDATEUSERS_VALIDATIONFAILED.unifyErrorCode, CommonUtil.formatString("[{} {}]", AppException.ErrorCode.E_API_USER_VALIDATEUSERS_VALIDATIONFAILED.messageTemplate, StringUtils.join(validationMessageList, ";")));
        }
    }

    public UserGroup addUserGroup(UserGroup userGroup, String currentUsername) throws AppException {
        log.debug("[addUserGroup] userGroup: {}", userGroup);

        Optional<UserGroup> optionalUserGroup = userGroupRepository.findByGroupTypeAndGroupName(userGroup.getGroupType(), userGroup.getGroupName());
        if (optionalUserGroup.isPresent()) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_ALREADY_EXIST, HttpURLConnection.HTTP_CONFLICT, AppException.ErrorCode.E_API_USERGROUP_ADDUSERGROUPS_ALREADYEXIST.unifyErrorCode,
                    CommonUtil.formatString("[{}] groupType: {} ,groupName: {}", AppException.ErrorCode.E_API_USERGROUP_ADDUSERGROUPS_ALREADYEXIST.messageTemplate, userGroup.getGroupType(), userGroup.getGroupName()));
        }
        Date now = new Date();
        userGroup.setCreatedOn(now);
        userGroup.setModifiedOn(now);
        userGroup.setCreatedBy(currentUsername);
        userGroup.setModifiedBy(currentUsername);
        return this.userGroupRepository.save(userGroup);
    }

    public Optional<UserGroup> findUserGroupById(UUID id) {
        log.debug("[findUserGroupById] id: {}", id);
        return userGroupRepository.findById(id);
    }

    public Page<UserGroup> findAllUserGroup(Pageable pageable) {
        log.debug("[findAllUserGroup] pageable: {}", pageable);
        return this.userGroupRepository.findAll(pageable);
    }

    public void deleteUserGroup(UserGroup userGroup) throws AppException {
        log.debug("[deleteUserGroupById] userGroup: {}", userGroup);
        if (UserGroup.Type.ROLE == userGroup.getGroupType()
                && StringUtils.equalsIgnoreCase(USER_GROUP_NAME_ADMIN, userGroup.getGroupName())) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_USERGROUP_COMMON_CAN_NOT_DELETE.unifyErrorCode,
                    CommonUtil.formatString("[{}] group: {} ", AppException.ErrorCode.E_API_USERGROUP_COMMON_CAN_NOT_DELETE.messageTemplate, USER_GROUP_NAME_ADMIN));
        }
        userGroupRepository.delete(userGroup);
    }

    public User addUserToUserGroup(UUID groupId, UUID userId, String currentUsername) throws AppException {
        log.debug("[addUserToUserGroup] groupId: {}, userId: {}, principal username: {}", groupId, userId, currentUsername);
        User user = userRepository.findById(userId).orElseThrow(
                () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_USERGROUP_COMMON_NOTFOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_USERGROUP_COMMON_NOTFOUND.messageTemplate, userId))
        );

        boolean alreadyInGroup = user.getUserGroups().stream().anyMatch(userGroup -> userGroup.getId().equals(groupId));

        if (!alreadyInGroup) {
            Optional<UserGroup> userGroupOptional = this.findUserGroupById(groupId);
            UserGroup userGroup = userGroupOptional.orElseThrow(
                    () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_USERGROUP_COMMON_NOTFOUND.unifyErrorCode,
                            CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_USERGROUP_COMMON_NOTFOUND.messageTemplate, groupId))
            );
            user.getUserGroups().add(userGroup);
            user = userRepository.save(user);
        }
        return user;
    }

    public User removeUserFromUserGroup(UUID groupId, UUID userId, String currentUsername) throws AppException {
        log.debug("[removeUserFromUserGroup] groupId: {}, userId: {}, principal username: {}", groupId, userId, currentUsername);
        User user = userRepository.findById(userId).orElseThrow(
                () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.messageTemplate, userId))
        );

        List<UserGroup> list = user.getUserGroups();
        Iterator<UserGroup> iterator = list.iterator();
        boolean drop = false;
        while (iterator.hasNext()) {
            if (iterator.next().getId().equals(groupId)) {
                iterator.remove();
                drop = true;
                break;
            }
        }
        if (drop) {
            user = userRepository.save(user);
        }
        return user;
    }

    private Optional<UserGroup> retrieveAdminUserGroup() {
        log.debug("[retrieveAdminUserGroup]");
        return userGroupRepository.findByGroupTypeAndGroupName(UserGroup.Type.ROLE, UserService.USER_GROUP_NAME_ADMIN);
    }

    public User updateStatus(UUID id, User.Status status, String currentUsername) throws AppException {
        log.debug("[updateStatus] id: {}, status: {}", id, status);
        User user = this.findById(id).orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.messageTemplate, id)));
        return this.updateStatus(user, status, currentUsername);
    }

    private User updateStatus(User user, User.Status status, String currentUsername) {
        log.debug("[updateStatus] user: {}, status: {}", user, status);
        user.setStatus(status);
        user.setModifiedBy(currentUsername);
        user.setModifiedOn(new Date());

        return userRepository.save(user);
    }

    void lockUser(String loginName) throws AppException {
        log.info("[lockUser] loginName: {}", loginName);

        Optional<User> userOptional = this.findByUsernameOrEmail(loginName);
        User user = userOptional.orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] loginName: {}", AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.messageTemplate, loginName)));
        if (!StringUtils.equalsIgnoreCase("system", user.getCreatedBy())) {
            this.updateStatus(user, User.Status.LOCK, "system");
        } else {
            log.error("[lockUser] trying to lock system account. id:{}, username: {}", user.getId(), user.getUsername());
        }
    }

    public User unlockUser(UUID id, String currentUsername) throws AppException {
        log.info("[unlockUser] id: {}, currentUsername: {}", id, currentUsername);
        User user = this.updateStatus(id, User.Status.ACTIVE, currentUsername);
        this.deleteLoginFailLogs(user);
        return user;
    }

    public void deleteLoginFailLogs(User user) {
        log.info("[deleteLoginFailLogs] user: {}", user);
        List<LoginFailLog> loginFailLogs = this.findLoginFailLogByUser(user);
        if (!loginFailLogs.isEmpty()) {
            this.deleteLoginFailLogs(loginFailLogs);
        }
    }

    private void deleteLoginFailLogs(List<LoginFailLog> loginFailLogs) {
        log.info("[deleteLoginFailLogs] loginFailLogs size: {}", loginFailLogs.size());
        this.loginFailLogRepository.deleteInBatch(loginFailLogs);
    }

    public Integer handleLoginFail(String loginName) throws AppException {
        log.info("[handleLoginFail] loginName: {}", loginName);
        Integer loginFailCount = this.addLoginFailLog(loginName, "Invalid password.");
        if (loginFailCount >= maxLoginRetryNo) {
            this.lockUser(loginName);
        }
        return loginFailCount;
    }

    private Integer addLoginFailLog(String loginName, String reason) throws AppException {
        log.info("[addLoginFailLog] loginName: {}, reason: {}", loginName, reason);

        Optional<User> userOptional = this.findByUsernameOrEmail(loginName);
        User user = userOptional.orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] loginName: {}", AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.messageTemplate, loginName)));
        return this.addLoginFailLog(user, reason);
    }

    private Integer addLoginFailLog(User user, String reason) {
        log.info("[addLoginFailLog] user: {}, reason: {}", user, reason);

        LoginFailLog loginFailLog = LoginFailLog.builder().user(user).reason(reason).createdOn(new Date()).build();
        this.saveLoginFailLog(loginFailLog);
        return getLoginFailLogCount(user);
    }

    private LoginFailLog saveLoginFailLog(LoginFailLog loginFailLog) {
        log.info("[saveLoginFailLog] loginFailLog: {}", loginFailLog);
        return this.loginFailLogRepository.saveAndFlush(loginFailLog);
    }

    private Integer getLoginFailLogCount(User user) {
        log.info("[getLoginFailLogCount] user: {}", user);
        List<LoginFailLog> loginFailLogs = this.findLoginFailLogByUser(user);
        return loginFailLogs.size();
    }

    private List<LoginFailLog> findLoginFailLogByUser(User user) {
        log.info("[findLoginFailLogByUser] user: {}", user);
        return this.loginFailLogRepository.findByUser(user);
    }

    public static boolean isAdmin(@NotNull(message = MessagesTemplate.E_API_VALIDATION_CONSTRAINTS_NOTNULL) User user) {
        log.trace("[isAdmin] user: {}", user.getUsername());
        return user.getUserGroups().stream().anyMatch(userGroup -> UserGroup.Type.ROLE.equals(userGroup.getGroupType()) && StringUtils.equalsAnyIgnoreCase(USER_GROUP_NAME_ADMIN, userGroup.getGroupName()));
    }


    Long countIssueAssignedToUser(ScanTask scanTask, User user) {
        log.debug("[countIssueAssignedToUser] scanTask id: {}, user: {}", scanTask.getId(), user.getUsername());
        return this.issueRepository.countByScanTaskAndAssignTo(scanTask, user);
    }

    Long countIssueAssignedToUser(Project project, User user) {
        log.debug("[countIssueAssignedToUser] project id: {}, user: {}", project.getId(), user.getUsername());
        return this.issueRepository.countByScanTaskProjectAndAssignTo(project, user);
    }

    public <X extends Throwable> void checkAccessRightOrElseThrow(Project project, User user, boolean checkAssigned, Supplier<? extends X> exceptionSupplier) throws X {
        log.debug("[checkAccessRightOrElseThrow] project id: {}, user: {}", project.getId(), user.getUsername());
        if (!hadAccessRightForProject(project, user, checkAssigned)) {
            throw exceptionSupplier.get();
        }
    }

    public <X extends Throwable> void checkAccessRightOrElseThrow(ScanTask scanTask, User user, boolean checkAssigned, Supplier<? extends X> exceptionSupplier) throws X {
        log.debug("[checkAccessRightOrElseThrow] scanTask id: {}, user: {}", scanTask.getId(), user.getUsername());
        if (!hadAccessRightForScanTask(scanTask, user, checkAssigned)) {
            throw exceptionSupplier.get();
        }
    }

    public <X extends Throwable> void checkAccessRightOrElseThrow(Issue issue, User user, Supplier<? extends X> exceptionSupplier) throws X {
        log.debug("[checkAccessRightOrElseThrow] issue id: {}, user: {}", issue.getId(), user.getUsername());
        if (!hadAccessRightForIssue(issue, user)) {
            throw exceptionSupplier.get();
        }
    }

    private boolean hadAccessRightForProject(Project project, User user, boolean checkAssigned) {
        log.debug("[hadAccessRightForProject] project created by: {}, user: {}", project.getCreatedBy(), user.getUsername());
        boolean hasRight = false;
        if (isAdmin(user)) {
            hasRight = true;
        } else if (StringUtils.equalsIgnoreCase(user.getUsername(), project.getCreatedBy())) {
            hasRight = true;
        } else if (checkAssigned) {
            Long numberOfIssueAssignToUser = this.countIssueAssignedToUser(project, user);
            log.debug("[hadAccessRight] project, id: {}, user: {}, numberOfIssueAssignToUser: {}", project.getId(), user.getUsername(), numberOfIssueAssignToUser);
            if (numberOfIssueAssignToUser > 0) {
                hasRight = true;
            }
        }
        return hasRight;
    }

    private boolean hadAccessRightForScanTask(ScanTask scanTask, User user, boolean checkAssigned) {
        log.debug("[hadAccessRightForScanTask] project created by: {}, scanTask id:{}, user: {}", scanTask.getProject().getCreatedBy(), scanTask.getId(), user.getUsername());
        boolean hasRight = false;
        if (isAdmin(user)) {
            hasRight = true;
        } else if (StringUtils.equalsIgnoreCase(user.getUsername(), scanTask.getProject().getCreatedBy())) {
            hasRight = true;
        } else if (checkAssigned) {
            Long numberOfIssueAssignToUser = this.countIssueAssignedToUser(scanTask, user);
            log.debug("[hadAccessRight] scanTask, id: {}, user: {}, numberOfIssueAssignToUser: {}", scanTask.getId(), user.getUsername(), numberOfIssueAssignToUser);
            if (numberOfIssueAssignToUser > 0) {
                hasRight = true;
            }
        }
        return hasRight;
    }

    private boolean hadAccessRightForIssue(Issue issue, User user) {
        log.debug("[hadAccessRightForIssue] project created by: {}, user: {}, issue assigned to: {}", issue.getScanTask().getProject().getCreatedBy(), user.getUsername(), issue.getAssignTo() != null ? issue.getAssignTo().getUsername() : null);
        boolean hasRight = false;
        if (isAdmin(user)) {
            hasRight = true;
        } else if (StringUtils.equalsIgnoreCase(user.getUsername(), issue.getScanTask().getProject().getCreatedBy())) {
            hasRight = true;
        } else if (issue.getAssignTo() != null && user.getId().equals(issue.getAssignTo().getId())) {
            hasRight = true;
        }
        return hasRight;
    }


    /**
     * map to dto
     *
     * @param user User
     * @return user dto
     */
    public static UserDto convertUserToDto(@NonNull User user) {
        return convertUserToDto(user, false);
    }

    /**
     * map to dto
     *
     * @param user User
     * @return user dto
     */
    public static UserDto convertUserToDtoWithDetails(@NonNull User user) {
        return convertUserToDto(user, true);
    }

    /**
     * map to dto
     *
     * @param user           User
     * @param includeDetails include username, email, userGroup or isAdmin
     * @return user dto
     */
    public static UserDto convertUserToDto(@NonNull User user, boolean includeDetails) {
        UserDto result = UserDto.builder()
                .id(user.getId())
                .displayName(user.getDisplayName())
                .build();
        if (includeDetails) {
            String isAdmin = isAdmin(user) ? UserDto.IS_ADMIN_YES : UserDto.IS_ADMIN_NO;
            result.setUsername(user.getUsername());
            result.setEmail(user.getEmail());
            result.setUserGroups(user.getUserGroups().stream().map(UserService::convertUserGroupToDto).collect(Collectors.toList()));
            result.setIsAdmin(isAdmin);
            result.setCreatedOn(user.getCreatedOn());
            result.setCreatedBy(user.getCreatedBy());
            user.setModifiedOn(user.getModifiedOn());
            result.setModifiedBy(user.getModifiedBy());
        }
        return result;
    }

    static UserGroupDto convertUserGroupToDto(UserGroup userGroup) {
        return UserGroupDto.builder()
                .id(userGroup.getId())
                .groupName(userGroup.getGroupName())
                .description(userGroup.getDescription())
                .groupType(userGroup.getGroupType().name())
                .createdBy(userGroup.getCreatedBy())
                .createdOn(userGroup.getCreatedOn())
                .modifiedBy(userGroup.getModifiedBy())
                .modifiedOn(userGroup.getModifiedOn())
                .build();
    }

    public long getCurrentUserCount() {
        log.info("[getCurrentUserCount]");
        return this.userRepository.count();
    }

    public void sendNewUsersEmail(List<NewUserRequest> newUserRequestList, Locale locale, User currentUser) throws AppException {
        log.info("[sendNewUsersEmail] newUserRequestList: {}", newUserRequestList.size());
        for (NewUserRequest newUserRequest : newUserRequestList) {
            this.sendNewUserEmail(newUserRequest.getUsername(), newUserRequest.getDisplayName(), newUserRequest.getEmail(), newUserRequest.getPassword(), locale, currentUser);
        }
    }

    public List<UserCountDto> getTopAssignees( UUID projectId,
                                               UUID scanTaskId,
                                               List<SearchIssueGroupRequest.RuleCode> ruleCodes,
                                               List<String> ruleSets,
                                               List<String> filePaths,
                                               String pathCategory,
                                               String certainty,
                                               List<String> dsrType,
                                               String criticality,
                                               String validationAction,
                                               String searchValue,
                                               Pageable pageable) {
        return userDao.getTopAssignees(
                projectId,
                scanTaskId,
                ruleCodes,
                ruleSets,
                filePaths,
                pathCategory,
                certainty,
                dsrType,
                criticality,
                validationAction,
                searchValue,
                pageable
        );
    }

    public void updateConfig(Integer configNumCodeDisplay, User user) {
        user.setConfigNumCodeDisplay(configNumCodeDisplay);
        userRepository.save(user);
    }
}
