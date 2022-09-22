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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xcal.api.config.WithMockCustomUser;
import com.xcal.api.entity.License;
import com.xcal.api.entity.User;
import com.xcal.api.entity.UserGroup;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.UserDto;
import com.xcal.api.model.payload.ChangePasswordRequest;
import com.xcal.api.model.payload.NewUserRequest;
import com.xcal.api.model.payload.RestResponsePage;
import com.xcal.api.service.LicenseService;
import com.xcal.api.service.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.net.HttpURLConnection;
import java.util.*;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private LicenseService licenseService;

    @NonNull ObjectMapper om;

    private User user;
    private final UUID userId = UUID.fromString("11111111-1111-1111-1112-111111111111");
    private final String username = "jason";
    private final String displayName = "jason Li";
    private final String email = username + "@gmail.com";

    private UserDto userUpdated;
    private final String displayNameUpdated = "jason Li";
    private final String emailUpdated = username + "@gmail.com";

    private NewUserRequest newUserRequest;

    private List<NewUserRequest> newUserRequestList;

    private UserGroup userGroup;
    private UserGroup userGroup1;
    private UserGroup userGroup2;
    private List<UserGroup> userGroupList;


    private final String adminUsername = "admin";

    @BeforeEach
    void setup() throws AppException {
        user = User.builder().id(userId).username(username).displayName(displayName).email(email).status(User.Status.ACTIVE).userGroups(new ArrayList<>()).build();
        newUserRequest = NewUserRequest.builder().username(username).displayName(displayName).email(email).isAdmin("N").password("12345").build();
        newUserRequestList = Arrays.asList(NewUserRequest.builder().build()
                , NewUserRequest.builder().build()
                , NewUserRequest.builder().build());
        userUpdated = UserDto.builder().username(username).displayName(displayNameUpdated).email(emailUpdated).isAdmin("N").build();
        userGroup = UserGroup.builder().id(UUID.randomUUID()).groupName("test group").groupType(UserGroup.Type.ROLE).createdBy(adminUsername).description("this is test group").build();
        userGroup1 = UserGroup.builder().id(UUID.randomUUID()).groupName("test group1").groupType(UserGroup.Type.ROLE).createdBy(adminUsername).description("this is test group1").build();
        userGroup2 = UserGroup.builder().id(UUID.randomUUID()).groupName("test group2").groupType(UserGroup.Type.ROLE).createdBy(adminUsername).description("this is test group2").build();
        userGroupList = Arrays.asList(userGroup1, userGroup2);
        when(userService.addUser(argThat(user -> StringUtils.equals(newUserRequest.getUsername(), user.getUsername())), any())).thenReturn(user);
        when(userService.updateUser(argThat(user -> StringUtils.equals(userUpdated.getUsername(), user.getUsername())), any())).thenReturn(user);
        when(userService.findById(user.getId())).thenReturn(Optional.of(user));
        when(licenseService.findActiveLicense()).thenReturn(Optional.of(License.builder()
                .maxUsers(10).build()));
    }

    @Test
    @WithMockCustomUser()
    void createUser_WithNonAdminUser_ReturnAccessDeniedException() throws Exception {
        log.info("[createUser_WithNonAdminUser_ReturnAccessDeniedException] user id {}", userId);
        mockMvc.perform(post("/api/user_service/v2/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(newUserRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("[Access is denied]"));
    }


    @Test
    @WithMockCustomUser(adminUsername)
    void createUserTestSuccess() throws Exception {
        log.info("[createUserTestSuccess] user id {}", userId);
        mockMvc.perform(post("/api/user_service/v2/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(newUserRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void createUser_licenseExpired_throwException() throws Exception {
        log.info("[createUserTestSuccess] user id {}", userId);
        when(this.licenseService.findActiveLicense()).thenReturn(Optional.empty());
        mockMvc.perform(post("/api/user_service/v2/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(newUserRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }


    @Test
    @WithMockCustomUser(adminUsername)
    void createUser_invalidUser_throwException() throws Exception {
        log.info("[createUserTestSuccess] user id {}", userId);
        doThrow(new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_ALREADY_EXIST, HttpURLConnection.HTTP_CONFLICT, null, ""))
                .when(this.userService).validateUser(ArgumentMatchers.any(NewUserRequest.class));
        mockMvc.perform(post("/api/user_service/v2/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(newUserRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_BAD_REQUEST))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_BAD_REQUEST));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void createUserGroup_InputValidUserGroup_ReturnUserGroup() throws Exception {
        log.info("[createUserGroup_InputValidUserGroup_ReturnUserGroup] user id {}", userId);
        when(userService.addUserGroup(userGroup, adminUsername)).thenReturn(userGroup);
        mockMvc.perform(post("/api/user_service/v2/user_group")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(userGroup))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userGroup.getId().toString()))
                .andExpect(jsonPath("$.groupType").value(userGroup.getGroupType().name()))
                .andExpect(jsonPath("$.groupName").value(userGroup.getGroupName()))
                .andExpect(jsonPath("$.description").value(userGroup.getDescription()))
                .andExpect(jsonPath("$.createdBy").value(userGroup.getCreatedBy()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void findUserGroupById_InputUserGroupId_ReturnUserGroup() throws Exception {
        log.info("[findUserGroupById_InputUserGroupId_ReturnUserGroup] user id {}", userId);
        when(userService.findUserGroupById(userGroup.getId())).thenReturn(Optional.of(userGroup));
        mockMvc.perform(get("/api/user_service/v2/user_group/{uuid}", userGroup.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userGroup.getId().toString()))
                .andExpect(jsonPath("$.groupType").value(userGroup.getGroupType().name()))
                .andExpect(jsonPath("$.groupName").value(userGroup.getGroupName()))
                .andExpect(jsonPath("$.description").value(userGroup.getDescription()))
                .andExpect(jsonPath("$.createdBy").value(userGroup.getCreatedBy()));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void findUserGroupById_UserGroupNotFound_ThrowException() throws Exception {
        log.info("[findUserGroupById_UserGroupNotFound_ThrowException] user id {}", userId);
        when(userService.findUserGroupById(userGroup.getId())).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/user_service/v2/user_group/{uuid}", userGroup.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void createUsers_LicenseExpired_ThrowException() throws Exception {
        log.info("[createUsers_LicenseExpired_ThrowException]");
        when(licenseService.findActiveLicense()).thenReturn(Optional.of(License.builder().expiresOn(new Date(new Date().getTime() - 100)).build()));
        mockMvc.perform(post("/api/user_service/v2/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(newUserRequestList))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_BAD_REQUEST))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_LICENSE_EXPIRED));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void createUsers_ExceedLicenseNUmber_ThrowException() throws Exception {
        log.info("[createUsers_ExceedLicenseNUmber_ThrowException]");
        when(licenseService.findActiveLicense()).thenReturn(Optional.of(License.builder().maxUsers(10).expiresOn(new Date(new Date().getTime() + 3600)).build()));
        when(userService.getCurrentUserCount()).thenReturn(10L);
        mockMvc.perform(post("/api/user_service/v2/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(newUserRequestList))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_BAD_REQUEST))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_BAD_REQUEST));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void createUsers_ContentTypeIsApplicationJson_ReturnSuccessNewUserResponseList() throws Exception {
        log.info("[createUsers_ContentTypeIsApplicationJson_ReturnSuccessNewUserResponseList] user id {}", userId);
        when(licenseService.findActiveLicense()).thenReturn(Optional.of(License.builder().maxUsers(10).expiresOn(new Date(new Date().getTime() + 3600)).build()));
        when(userService.getCurrentUserCount()).thenReturn(1L);
        mockMvc.perform(post("/api/user_service/v2/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(newUserRequestList))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser()
    void createUsers_WithNonAdminUser_ReturnAccessDeniedException() throws Exception {
        log.info("[createUsers_WithNonAdminUser_ReturnAccessDeniedException] user id {}", userId);
        mockMvc.perform(post("/api/user_service/v2/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(newUserRequestList))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("[Access is denied]"));
    }

    @Test
    @WithMockCustomUser()
    void importUsers_WithNonAdminUser_ReturnAccessDeniedException() throws Exception {
        log.info("[importUsers_WithNonAdminUser_ReturnAccessDeniedException]");
        mockMvc.perform(multipart("/api/user_service/v2/users").file(new MockMultipartFile("upload_file",
                "user_import.csv",
                "text/plain",
                "test".getBytes()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("[Access is denied]"));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void importUsers_LicenseExpired_ThrowException() throws Exception {
        log.info("[importUsers_LicenseExpired_ThrowException]");
        when(licenseService.findActiveLicense()).thenReturn(Optional.of(License.builder().expiresOn(new Date(new Date().getTime() - 100)).build()));
        mockMvc.perform(multipart("/api/user_service/v2/users").file(new MockMultipartFile("upload_file",
                "user_import.csv",
                "text/plain",
                "test".getBytes()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_BAD_REQUEST))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_LICENSE_EXPIRED));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void importUsers_ExceedLicenseNUmber_ThrowException() throws Exception {
        log.info("[importUsers_ExceedLicenseNUmber_ThrowException]");
        when(licenseService.findActiveLicense()).thenReturn(Optional.of(License.builder().maxUsers(10).expiresOn(new Date(new Date().getTime() + 3600)).build()));
        when(userService.getCurrentUserCount()).thenReturn(10L);
        when(userService.csvToNewUserList(any())).thenReturn(newUserRequestList);
        mockMvc.perform(multipart("/api/user_service/v2/users").file(new MockMultipartFile("upload_file",
                "user_import.csv",
                "text/plain",
                "test".getBytes()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_BAD_REQUEST))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_BAD_REQUEST));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void importUsers_NoActiveLicence_ThrowException() throws Exception {
        log.info("[importUsers_NoActiveLicence_ThrowException]");
        when(licenseService.findActiveLicense()).thenReturn(Optional.empty());
        mockMvc.perform(multipart("/api/user_service/v2/users").file(new MockMultipartFile("upload_file",
                "user_import.csv",
                "text/plain",
                "test".getBytes()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void importUsers_Success() throws Exception {
        log.info("[importUsers_Success] user id {}", userId);
        when(licenseService.findActiveLicense()).thenReturn(Optional.of(License.builder().maxUsers(10).expiresOn(new Date(new Date().getTime() + 3600)).build()));
        when(userService.getCurrentUserCount()).thenReturn(1L);
        mockMvc.perform(multipart("/api/user_service/v2/users").file(new MockMultipartFile("upload_file",
                "user_import.csv",
                "text/plain",
                "test".getBytes()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void createUser_LicenseExpired_ThrowException() throws Exception {
        log.info("[createUser_LicenseExpired_ThrowException]");
        NewUserRequest newUserRequest = NewUserRequest.builder().displayName(displayName).email(email).isAdmin("N").password("12345").build();
        when(licenseService.findActiveLicense()).thenReturn(Optional.of(License.builder().expiresOn(new Date(new Date().getTime() - 100)).build()));
        mockMvc.perform(post("/api/user_service/v2/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(newUserRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_BAD_REQUEST))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_LICENSE_EXPIRED));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void createUser_ExceedLicenseNumber_ThrowException() throws Exception {
        log.info("[createUser_ExceedLicenseNumber_ThrowException]");
        NewUserRequest newUserRequest = NewUserRequest.builder().displayName(displayName).email(email).isAdmin("N").password("12345").build();
        when(licenseService.findActiveLicense()).thenReturn(Optional.of(License.builder().maxUsers(10).expiresOn(new Date(new Date().getTime() + 3600)).build()));
        when(userService.getCurrentUserCount()).thenReturn(10L);
        mockMvc.perform(post("/api/user_service/v2/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(newUserRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_BAD_REQUEST))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_BAD_REQUEST));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void updateUserTestSuccess() throws Exception {
        log.info("[updateUserTestSuccess] user id {}", userId);
        mockMvc.perform(put("/api/user_service/v2/user/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(userUpdated))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void updateUser_ValidationFailed_ThrowException() throws Exception {
        log.info("[updateUser_ValidationFailed_ThrowException] user id {}", userId);
        UserDto noUsername = UserDto.builder().displayName(displayNameUpdated).email(emailUpdated).isAdmin("N").build();
        mockMvc.perform(put("/api/user_service/v2/user/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(noUsername))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        UserDto noDisplayName = UserDto.builder().username(username).email(emailUpdated).isAdmin("N").build();
        mockMvc.perform(put("/api/user_service/v2/user/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(noDisplayName))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        UserDto noEmail = UserDto.builder().username(username).displayName(displayNameUpdated).isAdmin("N").build();
        mockMvc.perform(put("/api/user_service/v2/user/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(noEmail))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        UserDto noIsAdmin = UserDto.builder().username(username).displayName(displayNameUpdated).email(emailUpdated).build();
        mockMvc.perform(put("/api/user_service/v2/user/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(noIsAdmin))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    @WithMockCustomUser(adminUsername)
    void findUserTest() throws Exception {
        log.info("[findUserTest] user id {}", userId);
        mockMvc.perform(get("/api/user_service/v2/user/{id}", userId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void findUserTestFail() throws Exception {
        log.info("[findUserTestFail] user id {}", userId);
        when(userService.findById(userId)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/user_service/v2/user/{id}", userId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void deleteUserTest() throws Exception {
        log.info("[deleteUserTest] user id {}", userId);
        mockMvc.perform(delete("/api/user_service/v2/user/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser()
    void deleteUser_WithNonAdminUser_ReturnAccessDeniedException() throws Exception {
        log.info("[deleteUser_WithNonAdminUser_ReturnAccessDeniedException] user id {}", userId);
        mockMvc.perform(delete("/api/user_service/v2/user/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("[Access is denied]"));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void userListTest() throws Exception {
        log.info("[userListTest] ");
        Pageable pageable = PageRequest.of(0, 20);
        List<User> usersResult = Collections.singletonList(user);
        when(userService.findAll(pageable)).thenReturn(new RestResponsePage<>(usersResult, pageable, usersResult.size()));

        mockMvc.perform(get("/api/user_service/v2/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(userId.toString()))
                .andExpect(jsonPath("$.content[0].displayName").value(displayName));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void currentUserTest() throws Exception {
        log.info("[currentUserTest] principal user: {}", adminUsername);
        User principalTestUser = User.builder().username(adminUsername).userGroups(new ArrayList<>()).build();
        when(userService.findByUsername(adminUsername)).thenReturn(Optional.of(principalTestUser));
        mockMvc.perform(get("/api/user_service/v2/current")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(adminUsername));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void currentUser_UserNotFound_ThrowException() throws Exception {
        log.info("[currentUser_UserNotFound_ThrowException] principal user: {}", adminUsername);
        when(userService.findByUsername(adminUsername)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/user_service/v2/current")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void updatePasswordTestSuccess() throws Exception {
        log.info("[updateUserTestSuccess] user id {}", userId);
        ChangePasswordRequest request = ChangePasswordRequest.builder().id(userId).newPassword("123456").build();
        doNothing().when(userService).updatePassword(argThat(changePasswordRequest -> StringUtils.equals(changePasswordRequest.getId().toString(), userId.toString())), any());
        mockMvc.perform(put("/api/user_service/v2/user/{id}/password", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void updatePassword_NoParam_ThrowException() throws Exception {
        log.info("[updatePassword_NoParam_ThrowException] user id {}", userId);
        mockMvc.perform(put("/api/user_service/v2/user/{id}/password", userId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void resetPassword_InputUserId_ReturnHttpSuccessAndNothing() throws Exception {
        log.info("[resetPassword_InputUserId_ReturnHttpSuccessAndNothing] user id {}", userId);
        doNothing().when(userService).updatePassword(argThat(changePasswordRequest -> StringUtils.equals(changePasswordRequest.getId().toString(), userId.toString())), any());
        mockMvc.perform(delete("/api/user_service/v2/user/{id}/password", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void deleteUserGroup_InputUserGroupId_ReturnHttpSuccessAndNothing() throws Exception {
        log.info("[deleteUserGroup_InputUserGroupId_ReturnHttpSuccessAndNothing] user id {}", userId);
        when(userService.findUserGroupById(eq(userGroup.getId()))).thenReturn(Optional.of(userGroup));
        doNothing().when(userService).deleteUserGroup(userGroup);
        mockMvc.perform(delete("/api/user_service/v2/user_group/{id}", userGroup.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void deleteUserGroup_UserGroupNotFound_ThrowException() throws Exception {
        log.info("[deleteUserGroup_UserGroupNotFound_ThrowException] user id {}", userId);
        when(userService.findUserGroupById(eq(userGroup.getId()))).thenReturn(Optional.empty());
        doNothing().when(userService).deleteUserGroup(userGroup);
        mockMvc.perform(delete("/api/user_service/v2/user_group/{id}", userGroup.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_DATA_NOT_FOUND));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void listUserGroups_ReturnPageOfAllUserGroup() throws Exception {
        log.info("[listUserGroups_ReturnPageOfAllUserGroup] user id {}", userId);
        Pageable pageable0 = PageRequest.of(0, 1);
        when(userService.findAllUserGroup(ArgumentMatchers.any(Pageable.class))).thenReturn(new RestResponsePage<>(userGroupList.subList(0, 1), pageable0, 2));
        mockMvc.perform(get("/api/user_service/v2/user_groups")
                .param("page", "0")
                .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(userGroup1.getId().toString()))
                .andExpect(jsonPath("$.content[0].groupName").value(userGroup1.getGroupName()))
                .andExpect(jsonPath("$.content[0].groupType").value(userGroup1.getGroupType().name()))
                .andExpect(jsonPath("$.content[0].description").value(userGroup1.getDescription()))
                .andExpect(jsonPath("$.content[0].createdBy").value(userGroup1.getCreatedBy()));
        Pageable pageable1 = PageRequest.of(1, 1);
        when(userService.findAllUserGroup(ArgumentMatchers.any(Pageable.class))).thenReturn(new RestResponsePage<>(userGroupList.subList(1, 2), pageable1, 2));

        mockMvc.perform(get("/api/user_service/v2/user_groups")
                .param("page", "1")
                .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(userGroup2.getId().toString()))
                .andExpect(jsonPath("$.content[0].groupName").value(userGroup2.getGroupName()))
                .andExpect(jsonPath("$.content[0].groupType").value(userGroup2.getGroupType().name()))
                .andExpect(jsonPath("$.content[0].description").value(userGroup2.getDescription()))
                .andExpect(jsonPath("$.content[0].createdBy").value(userGroup2.getCreatedBy()));
    }


    @Test
    @WithMockCustomUser(adminUsername)
    void addUserToUserGroup_InputUserGroupIdAndUserId_ReturnUserDto() throws Exception {
        log.info("[addUserToUserGroup_ReturnUserDto] user id {}", userId);
        when(userService.addUserToUserGroup(any(), any(), any())).thenReturn(user);
        mockMvc.perform(post("/api/user_service/v2/user_group/{id}/user/{userId}", userGroup.getId(), user.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.displayName").value(displayName));
    }

    @Test
    @WithMockCustomUser()
    void addUserToUserGroup_WithNonAdminUser_ReturnAccessDeniedException() throws Exception {
        log.info("[addUserToUserGroup_WithNonAdminUser_ReturnAccessDeniedException] user id {}", userId);
        mockMvc.perform(post("/api/user_service/v2/user_group/{id}/user/{userId}", userGroup.getId(), user.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("[Access is denied]"));
    }

    @Test
    @WithMockCustomUser()
    void removeUserFromUserGroup_WithNonAdminUser_ReturnAccessDeniedException() throws Exception {
        log.info("[removeUserFromUserGroup_WithNonAdminUser_ReturnAccessDeniedException] user id {}", userId);
        mockMvc.perform(delete("/api/user_service/v2/user_group/{id}/user/{userId}", userGroup.getId(), user.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("[Access is denied]"));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void removeUserFromUserGroup_InputUserGroupIdAndUserId_ReturnUserDto() throws Exception {
        log.info("[addUserToUserGroup_ReturnUserDto] user id {}", userId);
        when(userService.removeUserFromUserGroup(any(), any(), any())).thenReturn(user);
        mockMvc.perform(delete("/api/user_service/v2/user_group/{id}/user/{userId}", userGroup.getId(), user.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.displayName").value(displayName));
    }

    @Test
    @WithMockCustomUser()
    void updateStatusById_WithNonAdminUser_ReturnAccessDeniedException() throws Exception {
        log.info("[updateStatusById_WithNonAdminUser_ReturnAccessDeniedException] user id {}", userId);
        mockMvc.perform(put("/api/user_service/v2/user/{id}/status/{status}", user.getId(), User.Status.ACTIVE)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("[Access is denied]"));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void updateStatusById_InputUserIdAndStatus_ReturnUserDto() throws Exception {
        log.info("[addUserToUserGroup_ReturnUserDto] user id {}", userId);
        when(userService.updateStatus(ArgumentMatchers.any(UUID.class), any(), any())).thenReturn(user);
        mockMvc.perform(put("/api/user_service/v2/user/{id}/status/{status}", user.getId(), User.Status.ACTIVE)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.displayName").value(displayName));
    }

    @Test
    @WithMockCustomUser(adminUsername)
    void unlockUser_InputUserId_ReturnUserDto() throws Exception {
        log.info("[addUserToUserGroup_ReturnUserDto] user id {}", userId);
        when(userService.unlockUser(ArgumentMatchers.any(UUID.class), any())).thenReturn(user);
        mockMvc.perform(post("/api/user_service/v2/user/{id}/action/unlock", user.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.displayName").value(displayName));
    }

    @Test
    @WithMockCustomUser()
    void unlockUser_WithNonAdminUser_ReturnAccessDeniedException() throws Exception {
        log.info("[unlockUser_WithNonAdminUser_ReturnAccessDeniedException] user id {}", userId);
        mockMvc.perform(post("/api/user_service/v2/user/{id}/action/unlock", user.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.level").value(AppException.LEVEL_ERROR))
                .andExpect(jsonPath("$.responseCode").value(HttpURLConnection.HTTP_FORBIDDEN))
                .andExpect(jsonPath("$.errorCode").value(AppException.ERROR_CODE_UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("[Access is denied]"));
    }

    @Test
    @WithMockCustomUser()
    void listUsersWithDetails_Success() throws Exception {
        log.info("[unlockUser_WithNonAdminUser_ReturnAccessDeniedException] user id {}", userId);
        Date now = new Date();
        User user1 = User.builder().id(UUID.randomUUID()).username("test1").displayName("Test 1").email("test1@email.com").userGroups(Collections.singletonList(userGroup1)).status(User.Status.ACTIVE)
                .createdBy("system").createdOn(now).modifiedBy("system").modifiedOn(now).build();
        User user2 = User.builder().id(UUID.randomUUID()).username("test2").displayName("Test 2").email("test2@email.com").userGroups(Collections.singletonList(userGroup1)).status(User.Status.ACTIVE)
                .createdBy("system").createdOn(now).modifiedBy("system").modifiedOn(now).build();
        User user3 = User.builder().id(UUID.randomUUID()).username("test3").displayName("Test 3").email("test3@email.com").userGroups(Collections.singletonList(userGroup2)).status(User.Status.ACTIVE)
                .createdBy("system").createdOn(now).modifiedBy("system").modifiedOn(now).build();
        when(userService.findAll(ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(Arrays.asList(user1, user2, user3), PageRequest.of(1, 10),3));
        mockMvc.perform(get("/api/user_service/v2//users/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[*].username", containsInAnyOrder(user1.getUsername(), user2.getUsername(), user3.getUsername())))
                .andExpect(jsonPath("$.content[*].displayName", containsInAnyOrder(user1.getDisplayName(), user2.getDisplayName(), user3.getDisplayName())))
                .andExpect(jsonPath("$.content[*].email", containsInAnyOrder(user1.getEmail(), user2.getEmail(), user3.getEmail())));

    }
}
