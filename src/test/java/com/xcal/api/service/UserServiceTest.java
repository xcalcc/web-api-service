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
import com.xcal.api.model.dto.UserDto;
import com.xcal.api.model.dto.UserGroupDto;
import com.xcal.api.model.payload.ChangePasswordRequest;
import com.xcal.api.model.payload.NewUserRequest;
import com.xcal.api.model.payload.ValidationResult;
import com.xcal.api.repository.IssueRepository;
import com.xcal.api.repository.LoginFailLogRepository;
import com.xcal.api.repository.UserGroupRepository;
import com.xcal.api.repository.UserRepository;
import com.xcal.api.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.validation.Validation;
import javax.validation.Validator;
import java.net.HttpURLConnection;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Slf4j
class UserServiceTest {
    private UserService userService;
    private UserRepository userRepository;
    private UserGroupRepository userGroupRepository;
    private LoginFailLogRepository loginFailLogRepository;
    private LicenseService licenseService;
    private PasswordEncoder passwordEncoder;
    private IssueRepository issueRepository;

    private UserDao userDao;

    private static final String USER_NAME = "Jason";
    private static final String DISPLAY_NAME = "Jason PK";
    private static final String EMAIL = "jason@gmail.com";

    private UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111113");
    private UUID userGroupId = UUID.fromString("11111111-1111-1111-1112-111111111112");

    private UserGroup.Type groupType = UserGroup.Type.ROLE;

    private String groupName = "user_group";
    private String currentUserName = "user";
    private User currentUser = User.builder().username(currentUserName).displayName("testDispalyName").email("test@xxxx.com").password("12345").userGroups(new ArrayList<>()).build();


    private UserDto userDto = UserDto.builder().username(USER_NAME).displayName(DISPLAY_NAME).email(EMAIL).isAdmin(UserDto.IS_ADMIN_NO).build();
    private String newUsername = "new_jason";
    private String newDisplayName = "new_jason Li";
    private String newEmail = "new_jason@xxx.com";
    private NewUserRequest newUserRequest = NewUserRequest.builder().username(newUsername).displayName(newDisplayName).email(newEmail).isAdmin("N").password("12345").build();
    private NewUserRequest newUserRequestAdmin = NewUserRequest.builder().username(newUsername).displayName(newDisplayName).email(newEmail).isAdmin("Y").password("12345").build();
    private User newUser = User.builder().username(newUsername).displayName(newDisplayName).email(newEmail).password("12345").userGroups(new ArrayList<>()).build();
    private UserGroup adminGroup = UserGroup.builder().id(UUID.randomUUID()).groupType(UserGroup.Type.ROLE).groupName("admin").build();
    private User newUserAdmin = User.builder().username(newUsername).displayName(newDisplayName).email(newEmail).password("12345").userGroups(Collections.singletonList(adminGroup)).build();
    private UserGroup userGroup = UserGroup.builder().id(userGroupId).groupType(groupType).groupName(groupName).build();
    private User user = User.builder().id(userId).username(USER_NAME).displayName(DISPLAY_NAME).email(EMAIL).userGroups(new ArrayList<>(Arrays.asList(adminGroup, userGroup))).build();
    private User user1;
    private List<User> userList = new ArrayList<>();
    private Locale locale;
    private I18nService i18nService;


    @BeforeEach
    void setup() throws AppException {
        locale = Locale.getDefault();
        userRepository = mock(UserRepository.class);
        userGroupRepository = mock(UserGroupRepository.class);
        loginFailLogRepository = mock(LoginFailLogRepository.class);
        issueRepository = mock(IssueRepository.class);
        licenseService = mock(LicenseService.class);
        userDao = mock(UserDao.class);

        SettingService settingService = mock(SettingService.class);
        EmailService emailService = mock(EmailService.class);
        AppProperties appProperties = mock(AppProperties.class);
        ModelMapper modelMapper = new ModelMapper();
        passwordEncoder = mock(PasswordEncoder.class);
        i18nService = mock(I18nService.class);
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        userService = new UserService(userRepository, userGroupRepository, loginFailLogRepository, issueRepository, licenseService, settingService, emailService, appProperties, modelMapper, passwordEncoder, validator, i18nService, userDao);
        user1 = User.builder().id(UUID.randomUUID()).username("jk4").displayName("jk4").password("jk4").email("jk4@qq.com").userGroups(new ArrayList<>()).build();
        User user2 = User.builder().id(UUID.randomUUID()).username("jk5").displayName("jk5").password("jk5").email("jk5@qq.com").userGroups(new ArrayList<>()).build();
        userList = Arrays.asList(user1, user2);
        doNothing().when(emailService).sendTemplateMail(any(), any());
        when(settingService.getEmailServerConfiguration()).thenReturn(AppProperties.Mail.builder().prefix("[XCALBYTE]").from("no-reply@xcalibyte.io").build());
    }

    @Test
    void findByIdTestSuccess() {
        log.info("[findByIdTestSuccess]");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Optional<User> userOptional = userService.findById(user.getId());
        assertTrue(userOptional.isPresent());
        assertEquals(user.getId(), userOptional.get().getId());
        assertEquals(user.getUsername(), userOptional.get().getUsername());
    }

    @Test
    void findByIdTestFail() {
        log.info("[findByIdTestFail]");
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertFalse(userService.findById(id).isPresent());
    }

    @Test
    void findByEmailTestSuccess() {
        log.info("[findByEmailTestSuccess]");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        Optional<User> userOptional = userService.findByEmail(user.getEmail());
        assertTrue(userOptional.isPresent());
        assertEquals(user.getId(), userOptional.get().getId());
        assertEquals(user.getUsername(), userOptional.get().getUsername());
    }

    @Test
    void findByEmailTestFail() {
        log.info("[findByEmailTestFail]");
        String emailNotExist = "NO_EXIST_EMAIL";
        when(userRepository.findByEmail(emailNotExist)).thenReturn(Optional.empty());
        assertFalse(userService.findByEmail(emailNotExist).isPresent());
    }

    @Test
    void findByUsernameTestSuccess() {
        log.info("[findByUsernameTestSuccess]");
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        Optional<User> userOptional = userService.findByUsername(user.getUsername());
        assertTrue(userOptional.isPresent());
        assertEquals(user.getId(), userOptional.get().getId());
        assertEquals(user.getUsername(), userOptional.get().getUsername());
    }

    @Test
    void findByUsernameTestFail() {
        log.info("[findByUsernameTestFail]");
        String username = "NOT_EXIST";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        assertFalse(userService.findByUsername(username).isPresent());
    }

    @Test
    void addUserTestNotAdminSuccess() {
        log.info("[addUserTestNotAdminSuccess]");
        when(userRepository.findByUsername(newUserRequest.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(newUserRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(argThat(user1 -> StringUtils.equalsIgnoreCase(newUserRequest.getUsername(), user1.getUsername())))).thenReturn(newUser);
        when(licenseService.findActiveLicense()).thenReturn(Optional.of(License.builder().maxUsers(10).build()));
        User result = userService.addUser(newUserRequest, user);
        Assertions.assertEquals(newUserRequest.getUsername(), result.getUsername());
        Assertions.assertEquals(newUserRequest.getDisplayName(), result.getDisplayName());
        Assertions.assertEquals(newUserRequest.getEmail(), result.getEmail());
    }

    @Test
    void addUserTestAdminSuccess() {
        log.info("[addUserTestAdminSuccess]");
        when(userRepository.findByUsername(newUserRequestAdmin.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(newUserRequestAdmin.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(argThat(user1 -> StringUtils.equalsIgnoreCase(newUserRequestAdmin.getUsername(), user1.getUsername()))))
                .thenReturn(newUserAdmin);
        when(licenseService.findActiveLicense()).thenReturn(Optional.of(License.builder()
                .maxUsers(10).build()));
        when(userGroupRepository.findByGroupTypeAndGroupName(UserGroup.Type.ROLE, UserService.USER_GROUP_NAME_ADMIN)).thenReturn(Optional.of(UserGroup.builder().build()));
        User result = userService.addUser(newUserRequestAdmin, user);
        Assertions.assertEquals(newUserRequestAdmin.getUsername(), result.getUsername());
        Assertions.assertEquals(newUserRequestAdmin.getDisplayName(), result.getDisplayName());
        Assertions.assertEquals(newUserRequestAdmin.getEmail(), result.getEmail());
        assertEquals(UserGroup.Type.ROLE, result.getUserGroups().get(0).getGroupType());
        assertEquals("admin", result.getUserGroups().get(0).getGroupName());
    }

    @Test
    void addUser_ToLowcase_Success() {
        log.info("[addUser_ToLowcase_Success]");
        String username = "ABC.12";
        NewUserRequest userRequestLocal = NewUserRequest.builder().username(username).displayName(newDisplayName).email(newEmail).isAdmin("N").password("12345").build();
        User expectedUser = User.builder()
                .username(StringUtils.lowerCase(userRequestLocal.getUsername()))
                .displayName(userRequestLocal.getDisplayName())
                .email(userRequestLocal.getEmail())
                .build();
        when(userRepository.findByUsername(userRequestLocal.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(userRequestLocal.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(argThat(user1 -> StringUtils.equalsIgnoreCase(userRequestLocal.getUsername(), user1.getUsername())))).thenReturn(expectedUser);
        when(licenseService.findActiveLicense()).thenReturn(Optional.of(License.builder().maxUsers(10).build()));

        User result = userService.addUser(userRequestLocal, user);
        Assertions.assertEquals(username.toLowerCase(), result.getUsername());
    }

    @Test
    void updateUserTestNotAdminSuccess() throws AppException {
        log.info("[updateUserTestNotAdminSuccess]");
        when(userRepository.findByUsername(userDto.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(argThat(user1 -> StringUtils.equalsIgnoreCase(userDto.getUsername(), user1.getUsername())))).thenReturn(user);
        when(userGroupRepository.findByGroupTypeAndGroupName(UserGroup.Type.ROLE, UserService.USER_GROUP_NAME_ADMIN)).thenReturn(Optional.of(adminGroup));
        User result = userService.updateUser(userDto, anyString());
        Assertions.assertEquals(userDto.getUsername(), result.getUsername());
        Assertions.assertEquals(userDto.getDisplayName(), result.getDisplayName());
        Assertions.assertEquals(userDto.getEmail(), result.getEmail());
    }


    @Test
    void updateUserTestAdminSuccess() throws AppException {
        log.info("[updateUserTestAdminSuccess]");
        userDto = UserDto.builder().username(USER_NAME).displayName(DISPLAY_NAME).email(EMAIL).isAdmin(UserDto.IS_ADMIN_YES).build();
        user = User.builder().id(userId).username(USER_NAME).displayName(DISPLAY_NAME).email(EMAIL).userGroups(new ArrayList<>(Collections.singletonList(userGroup))).build();
        when(userRepository.findByUsername(userDto.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(argThat(user1 -> StringUtils.equalsIgnoreCase(userDto.getUsername(), user1.getUsername())))).thenReturn(user);
        when(userGroupRepository.findByGroupTypeAndGroupName(UserGroup.Type.ROLE, UserService.USER_GROUP_NAME_ADMIN)).thenReturn(Optional.of(adminGroup));
        User result = userService.updateUser(userDto, anyString());
        Assertions.assertEquals(userDto.getUsername(), result.getUsername());
        Assertions.assertEquals(userDto.getDisplayName(), result.getDisplayName());
        Assertions.assertEquals(userDto.getEmail(), result.getEmail());
    }

    @Test
    void updateUserTestNotFoundFail() {
        log.info("[updateUserTestNotFoundFail]");
        when(userRepository.findByUsername(userDto.getUsername())).thenReturn(Optional.empty());
        Assertions.assertThrows(AppException.class, () -> userService.updateUser(userDto, anyString()));
    }

    @Test
    void updateUserTestEmailExistFail() {
        log.info("[updateUserTestEmailExistFail]");
        when(userRepository.findByUsername(userDto.getUsername())).thenReturn(Optional.of(newUser));
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.of(User.builder().username("testUserName2").displayName(newDisplayName).email(newEmail).build()));
        Assertions.assertThrows(AppException.class, () -> userService.updateUser(userDto, anyString()));
    }

    @Test
    void addTestSuccess() throws AppException {
        log.info("[addTestSuccess]");
        when(userGroupRepository.findByGroupTypeAndGroupName(userGroup.getGroupType(), userGroup.getGroupName())).thenReturn(Optional.empty());
        when(userGroupRepository.save(userGroup)).thenReturn(userGroup);
        UserGroup result = userService.addUserGroup(userGroup, currentUserName);
        assertEquals(groupType, result.getGroupType());
        assertEquals(groupName, result.getGroupName());
        assertEquals(currentUserName, result.getCreatedBy());
    }

    @Test
    void addTestUserGroupExistingFail() {
        log.info("[addTestUserGroupExistingFail]");
        when(userGroupRepository.findByGroupTypeAndGroupName(userGroup.getGroupType(), userGroup.getGroupName())).thenReturn(Optional.of(userGroup));
        assertThrows(AppException.class, () -> userService.addUserGroup(userGroup, currentUserName));
    }

    @Test
    void findUserGroupByIdTestSuccess() {
        log.info("[findUserGroupByIdTestSuccess]");
        when(userGroupRepository.findById(userGroupId)).thenReturn(Optional.of(userGroup));
        Optional<UserGroup> result = userService.findUserGroupById(userGroupId);
        assertTrue(result.isPresent());
        assertEquals(userGroupId, result.get().getId());
        assertEquals(groupType, result.get().getGroupType());
        assertEquals(groupName, result.get().getGroupName());
    }

    @Test
    void findUserGroupByIdTestNotFoundFail() {
        log.info("[findUserGroupByIdTestNotFoundFail]");
        when(userGroupRepository.findById(userGroupId)).thenReturn(Optional.empty());
        assertFalse(userService.findUserGroupById(userGroupId).isPresent());
    }

    @Test
    void addUserToUserGroupTestSuccess() throws AppException {
        log.info("[addUserToUserGroupTestSuccess]");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userGroupRepository.findById(userGroupId)).thenReturn(Optional.of(userGroup));
        when(userRepository.save(user)).thenReturn(user);
        User result = userService.addUserToUserGroup(userGroupId, userId, currentUserName);
        assertEquals(userId, result.getId());
    }

    @Test
    void addUserToUserGroupTestUserNotExistingFail() {
        log.info("[addUserToUserGroupTestUserNotExistingFail]");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        when(userGroupRepository.findById(userGroupId)).thenReturn(Optional.of(userGroup));
        when(userRepository.save(user)).thenReturn(user);
        assertThrows(AppException.class, () -> userService.addUserToUserGroup(userGroupId, userId, currentUserName));
    }

    @Test
    void addUserToUserGroupTestUserGroupNotExistingFail() {
        log.info("[addUserToUserGroupTestUserGroupNotExistingFail]");
        user = User.builder().id(userId).username(USER_NAME).displayName(DISPLAY_NAME).email(EMAIL).userGroups(new ArrayList<>()).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userGroupRepository.findById(userGroupId)).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);
        assertThrows(AppException.class, () -> userService.addUserToUserGroup(userGroupId, userId, currentUserName));
    }

    @Test
    void removeUserFromUserGroupTestSuccess() throws AppException {
        log.info("[removeUserFromUserGroupTestSuccess]");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userGroupRepository.findById(userGroupId)).thenReturn(Optional.of(userGroup));
        when(userRepository.save(user)).thenReturn(user);
        User result = userService.removeUserFromUserGroup(userGroupId, userId, currentUserName);
        assertEquals(userId, result.getId());
    }

    @Test
    void removeUserFromUserGroupTestUserNotExistingFail() {
        log.info("[removeUserFromUserGroupTestUserNotExistingFail]");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        when(userGroupRepository.findById(userGroupId)).thenReturn(Optional.of(userGroup));
        when(userRepository.save(user)).thenReturn(user);
        assertThrows(AppException.class, () -> userService.removeUserFromUserGroup(userGroupId, userId, currentUserName));
    }

    @Test
    void findByUsernameOrEmailTestUsernameSuccess() {
        log.info("[findByUsernameOrEmailTestUsernameSuccess]");
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        Optional<User> userOptional = userService.findByUsernameOrEmail(user.getUsername());
        assertTrue(userOptional.isPresent());
        assertEquals(user.getId(), userOptional.get().getId());
        assertEquals(user.getUsername(), userOptional.get().getUsername());
    }


    @Test
    void findByUsernameOrEmailTestEmailSuccess() {
        log.info("[findByUsernameOrEmailTestEmailSuccess]");
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        Optional<User> userOptional = userService.findByUsernameOrEmail(user.getEmail());
        assertTrue(userOptional.isPresent());
        assertEquals(user.getId(), userOptional.get().getId());
        assertEquals(user.getUsername(), userOptional.get().getUsername());
    }

    @Test
    void deleteUserGroupTestSuccess() throws AppException {
        log.info("[deleteUserGroupTestSuccess]");
        doNothing().when(userGroupRepository).delete(any());
        userService.deleteUserGroup(userGroup);
        assertTrue(true);
    }


    @Test
    void deleteUserGroupTestFail() {
        log.info("[deleteUserGroupTestFail]");
        doNothing().when(userGroupRepository).delete(any());
        assertThrows(AppException.class, () -> userService.deleteUserGroup(adminGroup));
    }

    @Test
    void updateStatusTestSuccess() throws AppException {
        log.info("[updateStatusTestSuccess]");
        User expectedReturnUser = User.builder().id(userId).username(USER_NAME).displayName(DISPLAY_NAME).email(EMAIL).status(User.Status.LOCK).userGroups(new ArrayList<>()).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(expectedReturnUser);
        User resultUser = userService.updateStatus(user.getId(), User.Status.LOCK, currentUserName);
        Assertions.assertEquals(resultUser.getUsername(), expectedReturnUser.getUsername());
        Assertions.assertEquals(resultUser.getDisplayName(), expectedReturnUser.getDisplayName());
        Assertions.assertEquals(resultUser.getEmail(), expectedReturnUser.getEmail());
        Assertions.assertEquals(resultUser.getStatus(), expectedReturnUser.getStatus());
    }

    @Test
    void updateStatusTestFail() {
        log.info("[updateStatusTestFail]");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> userService.updateStatus(user.getId(), User.Status.LOCK, currentUserName));
    }

    @Test
    void lockUserTestSuccess() throws AppException {
        log.info("[lockUserTestSuccess]");
        User expectedReturnUser = User.builder().id(userId).username(USER_NAME).displayName(DISPLAY_NAME).email(EMAIL).status(User.Status.LOCK).userGroups(new ArrayList<>()).build();
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(expectedReturnUser);
        userService.lockUser(expectedReturnUser.getUsername());
        assertTrue(true);
    }

    @Test
    void lockUserLockCreatedBySystemTestFail() throws AppException {
        log.info("[lockUserLockCreatedBySystemTestFail]");
        User expectedReturnUser = User.builder().id(userId).username(USER_NAME).displayName(DISPLAY_NAME).email(EMAIL).status(User.Status.LOCK).createdBy("system").userGroups(new ArrayList<>()).build();
        when(userRepository.findByUsername(expectedReturnUser.getUsername())).thenReturn(Optional.of(expectedReturnUser));
        userService.lockUser(expectedReturnUser.getUsername());
        verify(userRepository, times(0)).save(user);
    }

    @Test
    void lockUserTestFail() {
        log.info("[lockUserTestFail]");
        User expectedReturnUser = User.builder().id(userId).username(USER_NAME).displayName(DISPLAY_NAME).email(EMAIL).status(User.Status.LOCK).userGroups(new ArrayList<>()).build();
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> userService.lockUser(expectedReturnUser.getUsername()));
    }

    @Test
    void unlockUserTestSuccess() throws AppException {
        log.info("[unlockUserTestSuccess]");
        User expectedReturnUser = User.builder().id(userId).username(USER_NAME).displayName(DISPLAY_NAME).email(EMAIL).status(User.Status.ACTIVE).userGroups(new ArrayList<>()).build();
        when(userRepository.findById(expectedReturnUser.getId())).thenReturn(Optional.of(expectedReturnUser));
        when(userRepository.save(expectedReturnUser)).thenReturn(expectedReturnUser);
        when(loginFailLogRepository.findByUser(expectedReturnUser)).thenReturn(new ArrayList<>());
        doNothing().when(loginFailLogRepository).deleteInBatch(any());
        userService.unlockUser(expectedReturnUser.getId(), currentUserName);
        assertTrue(true);
    }


    @Test
    void unlockUserTestFail() {
        log.info("[unlockUserTestSuccess]");
        User expectedReturnUser = User.builder().id(userId).username(USER_NAME).displayName(DISPLAY_NAME).email(EMAIL).status(User.Status.ACTIVE).userGroups(new ArrayList<>()).build();
        when(userRepository.findById(expectedReturnUser.getId())).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> userService.unlockUser(expectedReturnUser.getId(), currentUserName));
    }


    @Test
    void deleteLoginFailLogsTestSuccess() {
        log.info("[deleteLoginFailLogsTestSuccess]");
        when(loginFailLogRepository.findByUser(any())).thenReturn(Collections.singletonList(LoginFailLog.builder().id(UUID.randomUUID()).build()));
        doNothing().when(loginFailLogRepository).delete(any());
        userService.deleteLoginFailLogs(user);
        assertTrue(true);
    }


    @Test
    void deleteLoginFailLogsTestFail() {
        log.info("[deleteLoginFailLogsTestFail]");
        when(loginFailLogRepository.findByUser(any())).thenReturn(new ArrayList<>());
        doNothing().when(loginFailLogRepository).delete(any());
        verify(loginFailLogRepository, times(0)).deleteInBatch(any());
    }

    @Test
    void handleLoginFailTestSuccess() throws AppException {
        log.info("[handleLoginFailTestSuccess]");
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(loginFailLogRepository.findByUser(any())).thenReturn(Collections.singletonList(LoginFailLog.builder().id(UUID.randomUUID()).build()));
        when(loginFailLogRepository.saveAndFlush(any())).thenReturn(LoginFailLog.builder().id(UUID.randomUUID()).build());
        userService.maxLoginRetryNo = 10;
        Integer result = userService.handleLoginFail(user.getUsername());
        assertEquals(1, result);
    }

    @Test
    void handleLoginFailTestFail() {
        log.info("[handleLoginFailTestFail]");
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> userService.handleLoginFail(user.getUsername()));
    }

    @Test
    void handleLoginFailTestExceedMaxLoginRetryNoFail() throws AppException {
        log.info("[handleLoginFailTestExceedMaxLoginRetryNoFail]");
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        List<LoginFailLog> loginFailLogs = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            loginFailLogs.add(LoginFailLog.builder().id(UUID.randomUUID()).build());
        }
        when(loginFailLogRepository.findByUser(any())).thenReturn(loginFailLogs);
        when(loginFailLogRepository.saveAndFlush(any())).thenReturn(LoginFailLog.builder().id(UUID.randomUUID()).build());
        userService.maxLoginRetryNo = 10;
        userService.handleLoginFail(user.getUsername());
        verify(userRepository, times(1)).save(argThat(u -> u.getStatus() == User.Status.LOCK));
    }

    @Test
    void isAdminTestSuccess() {
        log.info("[isAdminTestSuccess]");
        assertTrue(UserService.isAdmin(newUserAdmin));
        assertFalse(UserService.isAdmin(user1));
    }

    @Test
    void convertUserToDtoTestSuccess() {
        log.info("[convertUserToDtoTestSuccess]");
        UserDto userDto = UserService.convertUserToDtoWithDetails(newUser);
        assertEquals(newUser.getId(), userDto.getId());
        assertEquals(newUser.getUsername(), userDto.getUsername());
        assertEquals(newUser.getEmail(), userDto.getEmail());
        assertEquals(newUser.getDisplayName(), userDto.getDisplayName());
        assertEquals(newUser.getUserGroups().size(), userDto.getUserGroups().size());
        assertEquals(UserDto.IS_ADMIN_NO, userDto.getIsAdmin());
        UserDto userAdminDto = UserService.convertUserToDtoWithDetails(newUserAdmin);
        assertEquals(newUserAdmin.getId(), userAdminDto.getId());
        assertEquals(newUserAdmin.getUsername(), userAdminDto.getUsername());
        assertEquals(newUserAdmin.getEmail(), userAdminDto.getEmail());
        assertEquals(newUserAdmin.getDisplayName(), userAdminDto.getDisplayName());
        assertEquals(UserDto.IS_ADMIN_YES, userAdminDto.getIsAdmin());
    }

    @Test
    void convertUserGroupToDtoTestSuccess() {
        log.info("[convertUserGroupToDtoTestSuccess]");
        UserGroupDto userGroupDto = UserService.convertUserGroupToDto(userGroup);
        assertEquals(userGroupDto.getId(), userGroup.getId());
        assertEquals(userGroupDto.getGroupType(), userGroup.getGroupType().toString());
        assertEquals(userGroupDto.getGroupName(), userGroup.getGroupName());
    }

    @Test
    void findAllUserGroupTestSuccess() {
        log.info("[findAllUserGroupTestSuccess]");
        Page<UserGroup> pageUserGroup = new PageImpl<>(Arrays.asList(adminGroup, userGroup));
        when(userGroupRepository.findAll(any(Pageable.class))).thenReturn(pageUserGroup);
        assertEquals(pageUserGroup, userService.findAllUserGroup(PageRequest.of(0, 20)));
    }

    @Test
    void findAllTestSuccess() {
        log.info("[findAllTestSuccess]");
        Page<User> pageUser = new PageImpl<>(userList);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(pageUser);
        assertEquals(pageUser, userService.findAll(PageRequest.of(0, 20)));
    }

    @Test
    void simpleSearchTestSuccess() {
        log.info("[simpleSearchTestSuccess]");
        Page<User> pageUser = new PageImpl<>(userList);
        when(userRepository.findAll(any(), any(Pageable.class))).thenReturn(pageUser);
        assertEquals(pageUser, userService.simpleSearch(User.builder().build(), PageRequest.of(0, 20)));
    }

    @Test
    void deleteByIdTestSuccess() {
        log.info("[deleteByIdTestSuccess]");
        UUID userId = UUID.randomUUID();
        when(issueRepository.updateIssueAssignToNullByUserId(userId)).thenReturn(1);
        doNothing().when(userRepository).deleteById(userId);
        assertDoesNotThrow(() -> userService.deleteById(userId));
    }

    @Test
    void updatePasswordTestUserNotFoundException() {
        log.info("[updatePasswordTestUserNotFoundException]");
        UUID userId = UUID.randomUUID();
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder().id(userId).build();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> userService.updatePassword(changePasswordRequest, currentUserName));
    }

    @Test
    void updatePasswordTestIncorrectOldPasswordException() {
        log.info("[updatePasswordTestIncorrectOldPasswordException]");
        UUID userId = UUID.randomUUID();
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder().id(userId).oldPassword("oldPassword").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenReturn(user);
        assertThrows(AppException.class, () -> userService.updatePassword(changePasswordRequest, currentUserName));
    }

    @Test
    void updatePasswordTestSuccess() {
        log.info("[updatePasswordTestSuccess]");
        UUID userId = UUID.randomUUID();
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder().id(userId).oldPassword("jk4").newPassword("newPassword").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user1));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("newPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        assertDoesNotThrow(() -> userService.updatePassword(changePasswordRequest, currentUserName));
    }

    @Test
    void countIssueAssignedToUserTest_ParamScanTask_Success() {
        log.info("[countIssueAssignedToUserTest_ParamScanTask_Success]");
        when(issueRepository.countByScanTaskAndAssignTo(any(ScanTask.class), any(User.class))).thenReturn(4L);
        assertEquals(4L, userService.countIssueAssignedToUser(ScanTask.builder().id(UUID.randomUUID()).build(), user));
    }

    @Test
    void countIssueAssignedToUserTest_ParamProjectSuccess() {
        log.info("[countIssueAssignedToUserTest_ParamProjectSuccess]");
        when(issueRepository.countByScanTaskProjectAndAssignTo(any(Project.class), any(User.class))).thenReturn(5L);
        assertEquals(5L, userService.countIssueAssignedToUser(Project.builder().id(UUID.randomUUID()).build(), user));
    }

    @Test
    void checkAccessRightOrElseThrowTestUserNameEqualSuccess() {
        log.info("[checkAccessRightOrElseThrowTestUserNameEqualSuccess]");
        Project project = Project.builder().projectId("projectId").createdBy(USER_NAME).build();
        assertDoesNotThrow(() -> userService.checkAccessRightOrElseThrow(project, user, false, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, null,
                CommonUtil.formatString("[Insufficient privilege]. project id: {}", project.getProjectId()))));
    }

    @Test
    void checkAccessRightOrElseThrowTestAdminGroupSuccess() {
        log.info("[checkAccessRightOrElseThrowTestAdminGroupSuccess]");
        Project project = Project.builder().projectId("projectId").createdBy("xxx").build();
        user = User.builder().id(userId).username(USER_NAME).displayName(DISPLAY_NAME).email(EMAIL).userGroups(new ArrayList<>(Collections.singletonList(adminGroup))).build();
        assertDoesNotThrow(() -> userService.checkAccessRightOrElseThrow(project, user, false, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, null,
                CommonUtil.formatString("[Insufficient privilege]. project id: {}", project.getProjectId()))));
    }


    @Test
    void checkAccessRightOrElseThrowTestFail() {
        log.info("[checkAccessRightOrElseThrowTestSuccess]");
        Project project = Project.builder().projectId("projectId").createdBy("xxx").build();
        user = User.builder().id(userId).username(USER_NAME).displayName(DISPLAY_NAME).email(EMAIL).userGroups(new ArrayList<>(Collections.singletonList(userGroup))).build();
        assertThrows(AppException.class, () -> userService.checkAccessRightOrElseThrow(project, user, false, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, null,
                CommonUtil.formatString("[Insufficient privilege]. project id: {}", project.getProjectId()))));
    }

    @Test
    void checkAccessRightOrElseThrowTestUserNameEqualProjectCreatorSuccess() {
        log.info("[checkAccessRightOrElseThrowTestUserNameEqualProjectCreatorSuccess]");
        Project project = Project.builder().projectId("projectId").createdBy(USER_NAME).build();
        ScanTask scanTask = ScanTask.builder().project(project).build();
        assertDoesNotThrow(() -> userService.checkAccessRightOrElseThrow(scanTask, user, false, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, null,
                CommonUtil.formatString("[Insufficient privilege]. project id: {}", project.getProjectId()))));
    }

    @Test
    void checkAccessRightOrElseThrowTestAssignIssueGreaterThanZeroSuccess() {
        log.info("[checkAccessRightOrElseThrowTestAssignIssueGreaterThanZeroSuccess]");
        Project project = Project.builder().projectId("projectId").createdBy("xxxxx").build();
        ScanTask scanTask = ScanTask.builder().project(project).build();
        when(issueRepository.countByScanTaskAndAssignTo(any(ScanTask.class), any(User.class))).thenReturn(4L);
        assertDoesNotThrow(() -> userService.checkAccessRightOrElseThrow(scanTask, user, true, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, null,
                CommonUtil.formatString("[Insufficient privilege]. project id: {}", project.getProjectId()))));
    }


    @Test
    void checkAccessRightOrElseThrowTestException() {
        log.info("[checkAccessRightOrElseThrowTestException]");
        Project project = Project.builder().projectId("projectId").createdBy("xxxxx").build();
        ScanTask scanTask = ScanTask.builder().project(project).build();
        when(issueRepository.countByScanTaskAndAssignTo(any(ScanTask.class), any(User.class))).thenReturn(0L);
        assertThrows(AppException.class, () -> userService.checkAccessRightOrElseThrow(scanTask, user1, true, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, null,
                CommonUtil.formatString("[Insufficient privilege]. project id: {}", project.getProjectId()))));
    }

    @Test
    void checkAccessRightOrElseThrow_UserNameEqualIssueAssignTo_DoseNotThrow() {
        log.info("[checkAccessRightOrElseThrowTestUserNameEqualIssueOfProjectCreatorSuccess]");
        Project project = Project.builder().projectId("projectId").createdBy(USER_NAME).build();
        ScanTask scanTask = ScanTask.builder().project(project).build();
        Issue issue = Issue.builder().scanTask(scanTask).assignTo(user1).build();
        assertDoesNotThrow(() -> userService.checkAccessRightOrElseThrow(issue, user1, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, null,
                CommonUtil.formatString("[Insufficient privilege]. project id: {}", project.getProjectId()))));
    }

    @Test
    void checkAccessRightOrElseThrow_UserNameNotEqualProjectCreatorAndNotEqualAssignTo_ThrowAppException() {
        log.info("[checkAccessRightOrElseThrowTestUserNameEqualIssueOfProjectCreatorAndNotEqualToAssignToException]");
        Project project = Project.builder().projectId("projectId").createdBy("xxx").build();
        ScanTask scanTask = ScanTask.builder().project(project).build();
        Issue issue = Issue.builder().scanTask(scanTask).assignTo(User.builder().id(UUID.randomUUID()).build()).build();
        assertThrows(AppException.class, () -> userService.checkAccessRightOrElseThrow(issue, user1, () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, null,
                CommonUtil.formatString("[Insufficient privilege]. project id: {}", project.getProjectId()))));
    }

    @Test
    void getCurrentUserCount_Success() {
        log.info("[getCurrentUserCount_Success]");
        when(userRepository.count()).thenReturn(10L);
        assertEquals(10L, userService.getCurrentUserCount());
    }

    @Test
    void validateUserFormat_UserNameFormatNotCorrect_ReturnFail() {
        log.info("[validateUserFormat_UserNameFormatNotCorrect_ReturnFail]");
        NewUserRequest userRequest = NewUserRequest.builder().username("Je").build();
        ValidationResult validationResult = userService.validateUserFormat(userRequest);
        assertEquals(ValidationResult.Status.FAIL, validationResult.getStatus());
    }

    @Test
    void validateUserFormat_EmailFormatNotCorrect_ReturnFail() {
        log.info("[validateUserFormat_EmailFormatNotCorrect_ReturnFail]");
        NewUserRequest userRequest = NewUserRequest.builder().username("Je").email("xxx").build();
        ValidationResult validationResult = userService.validateUserFormat(userRequest);
        assertEquals(ValidationResult.Status.FAIL, validationResult.getStatus());
    }

    @Test
    void validateUserFormat_FormatIsCorrect_ReturnSuccess() {
        log.info("[validateUserFormat_FormatIsCorrect_ReturnSuccess]");
        NewUserRequest userRequest = NewUserRequest.builder().username("Je12X56").email("xxx@qq.com").password("123").displayName("Je").isAdmin("Y").build();
        ValidationResult validationResult = userService.validateUserFormat(userRequest);
        assertEquals(ValidationResult.Status.SUCCESS, validationResult.getStatus());
    }

    @Test
    void validateUserExist_UserNameExist_ReturnFail() {
        log.info("[validateUserExist_UserNameExist_ReturnFail]");
        when(userRepository.findByUsername(newUserRequest.getUsername())).thenReturn(Optional.of(newUser));
        when(userRepository.findByEmail(newUserRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(argThat(user1 -> StringUtils.equalsIgnoreCase(newUserRequest.getUsername(), user1.getUsername())))).thenReturn(newUser);
        ValidationResult validationResult = userService.validateUserExist(newUserRequest);
        assertEquals(ValidationResult.Status.FAIL, validationResult.getStatus());
    }

    @Test
    void validateUserExist_EmailExist_ReturnFail() {
        log.info("[validateUserExist_EmailExist_ReturnFail]");
        when(userRepository.findByUsername(newUserRequest.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(newUserRequest.getEmail())).thenReturn(Optional.of(newUser));
        when(userRepository.save(argThat(user1 -> StringUtils.equalsIgnoreCase(newUserRequest.getUsername(), user1.getUsername())))).thenReturn(newUser);
        ValidationResult validationResult = userService.validateUserExist(newUserRequest);
        assertEquals(ValidationResult.Status.FAIL, validationResult.getStatus());
    }

    @Test
    void validateUserExist_UserAndEmailNotExist_ReturnSuccess() {
        log.info("[validateUserExist_UserAndEmailNotExist_ReturnSuccess]");
        when(userRepository.findByUsername(newUserRequest.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(newUserRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(argThat(user1 -> StringUtils.equalsIgnoreCase(newUserRequest.getUsername(), user1.getUsername())))).thenReturn(newUser);
        ValidationResult validationResult = userService.validateUserExist(newUserRequest);
        assertEquals(ValidationResult.Status.SUCCESS, validationResult.getStatus());
    }

    @Test
    void validateUser_UserNameExist_ThrowException() {
        log.info("[validateUser_UserNameExist_ThrowException]");
        when(userRepository.findByUsername(newUserRequest.getUsername())).thenReturn(Optional.of(newUser));
        when(userRepository.findByEmail(newUserRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(argThat(user1 -> StringUtils.equalsIgnoreCase(newUserRequest.getUsername(), user1.getUsername())))).thenReturn(newUser);
        AppException appException = assertThrows(AppException.class, () -> userService.validateUser(newUserRequest));
        assertEquals(AppException.LEVEL_ERROR, appException.getLevel());
        assertEquals(AppException.ERROR_CODE_DATA_ALREADY_EXIST, appException.getErrorCode());
    }

    @Test
    void validateUser_UserNameFormatNotCorrect_ThrowException() {
        log.info("[validateUser_UserNameFormatNotCorrect_ThrowException]");
        when(userRepository.findByUsername(newUserRequest.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(newUserRequest.getEmail())).thenReturn(Optional.empty());
        NewUserRequest userRequest = NewUserRequest.builder().username("Je").build();
        AppException appException = assertThrows(AppException.class, () -> userService.validateUser(userRequest));
        assertEquals(AppException.LEVEL_ERROR, appException.getLevel());
        assertEquals(AppException.ERROR_CODE_BAD_REQUEST, appException.getErrorCode());
    }

    @Test
    void validateUser_UserNotExistAndFormatCorrect_Success() {
        log.info("[validateUser_UserNotExistAndFormatCorrect_ThrowException]");
        when(userRepository.findByUsername(newUserRequest.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(newUserRequest.getEmail())).thenReturn(Optional.empty());
        NewUserRequest userRequest = NewUserRequest.builder().username("Je12X56").email("xxx@qq.com").password("123").displayName("Je").isAdmin("Y").build();
        assertDoesNotThrow(() -> userService.validateUser(userRequest));
    }

    @Test
    void csvToNewUserList_Success() throws AppException {
        log.info("[validateUser_UserNameFormatNotCorrect_ThrowException]");
        String csvString = "name,login,password,email,isAdmin\n" +
                "3333,3333,3333,3333@126.com,Y\n" +
                "2222,2222,2222,2222@126.com,N";
        MockMultipartFile csvFile = new MockMultipartFile("upload_file",
                "import.csv",
                MediaType.TEXT_PLAIN_VALUE,
                csvString.getBytes());
        List<NewUserRequest> newUserRequests = userService.csvToNewUserList(csvFile);
        assertEquals(2, newUserRequests.size());
    }

    @Test
    void validateUsers_UserNameExist_ThrowException() {
        log.info("[validateUsers_UserNameExist_ThrowException]");
        NewUserRequest userRequest1 = NewUserRequest.builder().username("Je12X51").email("xxx@qq.com").password("123").displayName("Je").isAdmin("Y").build();
        NewUserRequest userRequest2 = NewUserRequest.builder().username("Je12X52").email("xxx@qq.com").password("123").displayName("Je").isAdmin("Y").build();
        when(userRepository.findByUsername(userRequest1.getUsername().toLowerCase())).thenReturn(Optional.of(newUser));
        when(userRepository.findByEmail(userRequest1.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(userRequest2.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(userRequest2.getEmail())).thenReturn(Optional.empty());
        List<NewUserRequest> newUserRequests = Arrays.asList(userRequest1, userRequest2);
        AppException appException = assertThrows(AppException.class, () -> userService.validateUsers(newUserRequests, Locale.ENGLISH));
        assertEquals(AppException.LEVEL_ERROR, appException.getLevel());
        assertEquals(AppException.ERROR_CODE_BAD_REQUEST, appException.getErrorCode());
    }

    @Test
    void validateUsers_RowOneUserNameExistAndRowTwoFormatNotCorrect_ThrowException() {
        log.info("[validateUsers_UserNameExist_ThrowException]");
        NewUserRequest userRequest1 = NewUserRequest.builder().username("Je12X51").email("xxx@qq.com").password("123").displayName("Je").isAdmin("Y").build();
        NewUserRequest userRequest2 = NewUserRequest.builder().username("Je").email("xxx@qq.com").password("123").displayName("Je").isAdmin("Y").build();
        when(userRepository.findByUsername(userRequest1.getUsername().toLowerCase())).thenReturn(Optional.of(newUser));
        when(userRepository.findByEmail(userRequest1.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(userRequest2.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(userRequest2.getEmail())).thenReturn(Optional.empty());
        List<NewUserRequest> newUserRequests = Arrays.asList(userRequest1, userRequest2);
        AppException appException = assertThrows(AppException.class, () -> userService.validateUsers(newUserRequests, Locale.ENGLISH));
        assertEquals(AppException.LEVEL_ERROR, appException.getLevel());
        assertEquals(AppException.ERROR_CODE_BAD_REQUEST, appException.getErrorCode());
    }

    @Test
    void validateUsers_UserNotExistAndFormatCorrect_Success() {
        log.info("[validateUsers_UserNotExistAndFormatCorrect_Success]");
        NewUserRequest userRequest1 = NewUserRequest.builder().username("Je12X51").email("xxx@qq.com").password("123").displayName("Je").isAdmin("Y").build();
        NewUserRequest userRequest2 = NewUserRequest.builder().username("Je12X52").email("xxx@qq.com").password("123").displayName("Je").isAdmin("Y").build();
        when(userRepository.findByUsername(userRequest1.getUsername().toLowerCase())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(userRequest1.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(userRequest2.getUsername().toLowerCase())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(userRequest2.getEmail())).thenReturn(Optional.empty());
        List<NewUserRequest> newUserRequests = Arrays.asList(userRequest1, userRequest2);
        assertDoesNotThrow(() -> userService.validateUsers(newUserRequests, Locale.ENGLISH));
    }

    @Test
    void sendNewUserEmail_Success() {
        log.info("[sendNewUserEmail_Success]");
        assertDoesNotThrow(() -> userService.sendNewUserEmail(newUser.getUsername(), newUser.getDisplayName(), newUser.getEmail(), newUser.getPassword(), locale, currentUser));
    }

    @Test
    void sendNewUsersEmail_Success() {
        log.info("[sendNewUsersEmail_Success]");
        NewUserRequest userRequest1 = NewUserRequest.builder().username("Je12X51").email("xxx@qq.com").password("123").displayName("Je").isAdmin("Y").build();
        NewUserRequest userRequest2 = NewUserRequest.builder().username("Je12X52").email("xxx@qq.com").password("123").displayName("Je").isAdmin("Y").build();
        List<NewUserRequest> newUserRequests = Arrays.asList(userRequest1, userRequest2);
        assertDoesNotThrow(() -> userService.sendNewUsersEmail(newUserRequests, locale, newUserAdmin));
    }
}
