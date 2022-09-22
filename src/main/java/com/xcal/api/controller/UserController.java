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

import com.xcal.api.entity.License;
import com.xcal.api.entity.ScanFile;
import com.xcal.api.entity.User;
import com.xcal.api.entity.UserGroup;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.UserCountDto;
import com.xcal.api.model.dto.UserDto;
import com.xcal.api.model.dto.UserGroupDto;
import com.xcal.api.model.payload.*;
import com.xcal.api.model.payload.v3.SearchIssueGroupRequest;
import com.xcal.api.model.payload.v3.SearchTopAssigneeRequest;
import com.xcal.api.security.CurrentUser;
import com.xcal.api.security.UserPrincipal;
import com.xcal.api.service.FileService;
import com.xcal.api.service.LicenseService;
import com.xcal.api.service.ScanFileService;
import com.xcal.api.service.UserService;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.Dto;
import com.xcal.api.util.ValidErrorCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/user_service/v2")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(tags = "User Service")
public class UserController {

    @NonNull UserService userService;
    @NonNull FileService fileService;
    @NonNull LicenseService licenseService;
    @NonNull ScanFileService scanFileService;
    /**
     * create a new login user
     *
     * @param newUserRequest new user request
     * @param userPrincipal  user principal
     * @return
     * @throws AppException
     */
    @ApiOperation(
            value = "Create user",
            nickname = "createUser",
            notes = "Create user, administrator right is needed",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PostMapping("/user")
    @RolesAllowed("ROLE_ADMIN")
    public ResponseEntity<UserDto> createUser(
            @RequestBody NewUserRequest newUserRequest,
            Locale locale,
            @CurrentUser UserPrincipal userPrincipal
    ) throws AppException {
        log.info("[createUser] newUserRequest username: {}, principal username: {}", newUserRequest.getUsername(), userPrincipal.getUsername());

        License license = this.licenseService.findActiveLicense()
                .orElseThrow(() -> new AppException(
                        AppException.LEVEL_ERROR,
                        AppException.ERROR_CODE_DATA_NOT_FOUND,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        AppException.ErrorCode.E_API_LICENSE_COMMON_NOT_FOUND.unifyErrorCode,
                        AppException.ErrorCode.E_API_LICENSE_COMMON_NOT_FOUND.messageTemplate
                ));
        if ((license.getExpiresOn() != null) && license.getExpiresOn().before(new Date())) {
            throw new AppException(
                    AppException.LEVEL_ERROR,
                    AppException.ERROR_CODE_LICENSE_EXPIRED,
                    HttpURLConnection.HTTP_BAD_REQUEST,
                    AppException.ErrorCode.E_API_LICENSE_COMMON_EXPIRED.unifyErrorCode,
                    AppException.ErrorCode.E_API_LICENSE_COMMON_EXPIRED.messageTemplate
            );
        }

        long currentNumberOfUser = this.userService.getCurrentUserCount();
        if (currentNumberOfUser >= license.getMaxUsers()) {
            throw new AppException(
                    AppException.LEVEL_ERROR,
                    AppException.ERROR_CODE_BAD_REQUEST,
                    HttpURLConnection.HTTP_BAD_REQUEST,
                    AppException.ErrorCode.E_API_USER_CREATEUSERS_EXCEEDLICENSENUMBER.unifyErrorCode,
                    AppException.ErrorCode.E_API_USER_CREATEUSERS_EXCEEDLICENSENUMBER.messageTemplate
            );
        }

        try {
            this.userService.validateUser(newUserRequest);
        } catch (Exception e) {
            throw new AppException(
                    AppException.LEVEL_ERROR,
                    AppException.ERROR_CODE_BAD_REQUEST,
                    HttpURLConnection.HTTP_BAD_REQUEST,
                    AppException.ErrorCode.E_API_USER_VALIDATEUSERS_VALIDATIONFAILED.unifyErrorCode,
                    CommonUtil.formatString(
                            "[{} {}]",
                            AppException.ErrorCode.E_API_USER_VALIDATEUSERS_VALIDATIONFAILED,
                            e.getMessage()
                    ),
                    e
            );
        }

        User user = this.userService.addUser(newUserRequest, userPrincipal.getUser());
        UserDto userDto = UserService.convertUserToDtoWithDetails(user);
        this.userService.sendNewUserEmail(
                user.getUsername(),
                user.getDisplayName(),
                user.getEmail(),
                newUserRequest.getPassword(),
                locale,
                userPrincipal.getUser()
        );
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(userDto);
    }

    /**
     * update user's display name, email address
     *
     * @param id            user id
     * @param userDto       user dto
     * @param userPrincipal user principal
     * @return user dto
     * @throws AppException
     */
    @ApiOperation(value = "Update user",
            nickname = "updateUser",
            notes = "Update user",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PutMapping("/user/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID id, @Valid @ValidErrorCode(AppException.ErrorCode.E_API_USER_UPDATE_VALIDATE_FAIL) @RequestBody UserDto userDto, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[updateUser] user: {}, principal username: {}", userDto, userPrincipal.getUsername());
        User user = this.userService.updateUser(userDto, userPrincipal.getUsername());
        UserDto userDtoReturn = UserService.convertUserToDtoWithDetails(user);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(userDtoReturn);
    }

    /**
     * find user by id
     *
     * @param id            user id
     * @param userPrincipal user principal
     * @return user dto
     * @throws AppException when user not found
     */
    @ApiOperation(value = "Get user",
            nickname = "findUser",
            notes = "Get user by id",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping("/user/{id}")
    public ResponseEntity<UserDto> findUser(@PathVariable UUID id, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[findUser] id: {}, principal username: {}", id, userPrincipal.getUsername());
        UserDto userDto;
        Optional<User> userOptional = this.userService.findById(id);
        if (!userOptional.isPresent()) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.unifyErrorCode,
                    CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.messageTemplate, id));
        } else {
            User user = userOptional.get();
            userDto = UserService.convertUserToDtoWithDetails(user);
        }

        return ResponseEntity
                .status(HttpURLConnection.HTTP_OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(userDto);
    }

    /**
     * delete user by user id
     *
     * @param id            user id
     * @param userPrincipal user principal
     * @return
     */
    @ApiOperation(value = "Delete user",
            nickname = "deleteUser",
            notes = "Delete user by id, administrator right is needed",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @DeleteMapping("/user/{id}")
    @RolesAllowed("ROLE_ADMIN")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id, @CurrentUser UserPrincipal userPrincipal) {
        log.info("[deleteUser] id: {}, principal username: {}", id, userPrincipal.getUsername());
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * list user by page
     *
     * @param pageable      page info
     * @param userPrincipal user principal
     * @return user list by page
     */
    @ApiOperation(value = "List users",
            nickname = "listUsers",
            notes = "List users with paging, default page size 20",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping("/users")
    public ResponseEntity<Page<UserDto>> listUsers(Pageable pageable, @CurrentUser UserPrincipal userPrincipal) {
        log.info("[listUsers] pageable: {}, principal username: {}", pageable, userPrincipal.getUsername());
        Page<User> userPage = userService.findAll(pageable);
        List<UserDto> users = userPage.map(UserService::convertUserToDto).getContent();
        Page<UserDto> page = new PageImpl<>(users, pageable, userPage.getTotalElements());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(page);
    }

    /**
     * list user by page
     *
     * @param pageable      page info
     * @param userPrincipal user principal
     * @return user list by page
     */
    @ApiOperation(value = "List users",
            nickname = "listUsersWithDetails",
            notes = "List users with email, isAdmin, groups using paging, default page size 20",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping("/users/details")
    public ResponseEntity<Page<UserDto>> listUsersWithDetails(Pageable pageable, @CurrentUser UserPrincipal userPrincipal) {
        log.info("[listUsersWithDetails] pageable: {}, principal username: {}", pageable, userPrincipal.getUsername());
        Page<User> userPage = userService.findAll(pageable);
        List<UserDto> users = userPage.map(UserService::convertUserToDtoWithDetails).getContent();
        Page<UserDto> page = new PageImpl<>(users, pageable, userPage.getTotalElements());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(page);
    }

    /**
     * create multiple new users
     *
     * @param newUserRequestList new user request
     * @param userPrincipal      user principal
     * @return Void
     * @throws AppException
     */
    @ApiOperation(value = "create multiple users",
            nickname = "createUsers",
            notes = "create multiple users,administrator right is needed",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping(value = "/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    @RolesAllowed("ROLE_ADMIN")
    @Dto(NewUserResponse.class)
    public ResponseEntity<Void> createUsers(@RequestBody(required = false) List<NewUserRequest> newUserRequestList,
                                            Locale locale, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[createUsers] new user request size: {}, principal username: {}", newUserRequestList.size(), userPrincipal.getUsername());
        License license = this.licenseService.findActiveLicense().orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_LICENSE_COMMON_NOT_FOUND.unifyErrorCode, AppException.ErrorCode.E_API_LICENSE_COMMON_NOT_FOUND.messageTemplate));
        if (license.getExpiresOn() != null && license.getExpiresOn().before(new Date())) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_LICENSE_EXPIRED, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_LICENSE_COMMON_EXPIRED.unifyErrorCode, AppException.ErrorCode.E_API_LICENSE_COMMON_EXPIRED.messageTemplate);
        }
        long currentNumberOfUser = this.userService.getCurrentUserCount();
        if (currentNumberOfUser + newUserRequestList.size() > license.getMaxUsers()) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_USER_CREATEUSERS_EXCEEDLICENSENUMBER.unifyErrorCode, AppException.ErrorCode.E_API_USER_CREATEUSERS_EXCEEDLICENSENUMBER.messageTemplate);
        }
        userService.validateUsers(newUserRequestList, locale);
        userService.addUsers(newUserRequestList, userPrincipal.getUser());
        userService.sendNewUsersEmail(newUserRequestList, locale, userPrincipal.getUser());
        return ResponseEntity.noContent().build();
    }

    /**
     * import users by csv
     *
     * @param file          csv file
     * @param userPrincipal user principal
     * @return void
     * @throws AppException
     */
    @ApiOperation(value = "Import users",
            nickname = "importUsers",
            notes = "Import users with csv file, return all new user imported, administrator right is needed",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PostMapping(value = "/users", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RolesAllowed("ROLE_ADMIN")
    @Dto(NewUserResponse.class)
    public ResponseEntity<Void> importUsers(@RequestPart(value = "upload_file", required = false) MultipartFile file, Locale locale, @RequestParam(value = "file_checksum", required = false) String checksum,
                                            @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[importUsers] file size: {}, checksum: {}, principal username: {}", (file != null ? file.getSize() : 0), checksum, userPrincipal.getUsername());
        if (StringUtils.isNotBlank(checksum)) {
            boolean isFileOk = fileService.checkIntegrityWithCrc32(file, checksum);
            if (!isFileOk) {
                throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_INCONSISTENT, HttpURLConnection.HTTP_CONFLICT, AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.unifyErrorCode,
                        CommonUtil.formatString("[{}] expectedChecksum: {}", AppException.ErrorCode.E_API_FILE_CHECKINTEGRITY_FAILED.messageTemplate, checksum));
            }
        }
        List<NewUserRequest> newUserRequestList = userService.csvToNewUserList(file);
        License license = this.licenseService.findActiveLicense().orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_LICENSE_COMMON_NOT_FOUND.unifyErrorCode, AppException.ErrorCode.E_API_LICENSE_COMMON_NOT_FOUND.messageTemplate));
        if (license.getExpiresOn() != null && license.getExpiresOn().before(new Date())) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_LICENSE_EXPIRED, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_LICENSE_COMMON_EXPIRED.unifyErrorCode,
                    CommonUtil.formatString("[{}]", AppException.ErrorCode.E_API_LICENSE_COMMON_EXPIRED.messageTemplate));
        }
        long currentNumberOfUser = this.userService.getCurrentUserCount();
        if (currentNumberOfUser + newUserRequestList.size() > license.getMaxUsers()) {
            throw new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, AppException.ErrorCode.E_API_USER_CREATEUSERS_EXCEEDLICENSENUMBER.unifyErrorCode, AppException.ErrorCode.E_API_USER_CREATEUSERS_EXCEEDLICENSENUMBER.messageTemplate);
        }
        userService.validateUsers(newUserRequestList, locale);
        userService.addUsers(newUserRequestList, userPrincipal.getUser());
        userService.sendNewUsersEmail(newUserRequestList, locale, userPrincipal.getUser());
        return ResponseEntity.noContent().build();
    }


    /**
     * get current login user info
     *
     * @param userPrincipal user principal
     * @return user dto
     * @throws AppException
     */
    @ApiOperation(value = "Get current user",
            nickname = "currentUser",
            notes = "Get users with current credential",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping("/current")
    @RolesAllowed("ROLE_USER")
    public ResponseEntity<UserDto> currentUser(@CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[currentUser] principal username: {}", userPrincipal.getUsername());
        Optional<User> userOptional = userService.findByUsername(userPrincipal.getUsername());
        User user = userOptional.orElseThrow(() -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.unifyErrorCode,
                CommonUtil.formatString("[{}] username: {}", AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.messageTemplate, userPrincipal.getUsername())));
        UserDto userDto = UserService.convertUserToDtoWithDetails(user);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(userDto);
    }

    /**
     * update user's password by id
     *
     * @param id                    user id
     * @param changePasswordRequest
     * @param userPrincipal         user principal
     * @return void
     * @throws AppException
     */
    @ApiOperation(value = "Update user's password",
            nickname = "updatePassword",
            notes = "Update user's password, only can update current user's password except have administrator right",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PutMapping("/user/{id}/password")
    @RolesAllowed("ROLE_USER")
    public ResponseEntity<Void> updatePassword(
            @ApiParam(value = "uuid of the user", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable String id,
            @Valid @ValidErrorCode(AppException.ErrorCode.E_API_USER_VALIDATE_PASSWORD_VALIDATE_FAIL) @RequestBody ChangePasswordRequest changePasswordRequest,
            @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[updatePassword] id: {}, principal username: {}", id, userPrincipal.getUsername());
        changePasswordRequest.setId(UUID.fromString(id));
        userService.updatePassword(changePasswordRequest, userPrincipal.getUsername());
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "Reset user's password",
            nickname = "resetPassword",
            notes = "Reset user's password, only can reset current user's password except have administrator right",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @DeleteMapping("/user/{id}/password")
    @RolesAllowed("ROLE_USER")
    public ResponseEntity<Void> resetPassword(
            @ApiParam(value = "uuid of the user", example = "d40029be-eda6-4d62-b1ef-d05e2e91a72a", format = "uuid")
            @PathVariable String id,
            @CurrentUser UserPrincipal userPrincipal) {
        log.info("[resetPassword] principal username: {}", userPrincipal.getUsername());
        return ResponseEntity.noContent().build();
    }

    /**
     * create user group
     *
     * @param userGroup     user group entity
     * @param userPrincipal user principal
     * @return user group dto
     * @throws AppException
     */
    @ApiOperation(value = "Create user group",
            nickname = "createUserGroup",
            notes = "Create user group, administrator right is needed",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping("/user_group")
    @RolesAllowed("ROLE_USER")
    @Dto(UserGroupDto.class)
    public ResponseEntity<UserGroup> createUserGroup(@Valid @ValidErrorCode(AppException.ErrorCode.E_API_USERGROUP_CREATE_VALIDATE_FAIL) @RequestBody UserGroup userGroup, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[createUserGroup] userGroup: {}, principal username: {}", userGroup, userPrincipal.getUsername());
        UserGroup result = userService.addUserGroup(userGroup, userPrincipal.getUsername());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    /**
     * find user group by id
     *
     * @param id            user group id
     * @param userPrincipal user principal
     * @return user group dto
     * @throws AppException
     */
    @ApiOperation(value = "Get user group by Id",
            nickname = "findUserGroupById",
            notes = "Get user Group by id, administrator right is needed",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping("/user_group/{id}")
    @Dto(UserGroupDto.class)
    public ResponseEntity<UserGroup> findUserGroupById(@PathVariable UUID id, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[findUserGroupById] id {}", id);
        Optional<UserGroup> userGroupOptional = this.userService.findUserGroupById(id);
        UserGroup userGroup = userGroupOptional.orElseThrow(
                () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_USERGROUP_COMMON_NOTFOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_USERGROUP_COMMON_NOTFOUND.messageTemplate, id))
        );
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(userGroup);
    }

    /**
     * delete user group by id
     *
     * @param id            user group id
     * @param userPrincipal user principal
     * @return
     */
    @ApiOperation(value = "Delete user group by Id",
            nickname = "deleteUserGroup",
            notes = "Delete user group by id, administrator right is needed",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @DeleteMapping("/user_group/{id}")
    @RolesAllowed("ROLE_USER")
    public ResponseEntity<Void> deleteUserGroup(@PathVariable UUID id, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[deleteUserGroup] id: {}, principal username: {}", id.toString(), userPrincipal.getUsername());
        Optional<UserGroup> userGroupOptional = this.userService.findUserGroupById(id);
        UserGroup userGroup = userGroupOptional.orElseThrow(
                () -> new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_USERGROUP_COMMON_NOTFOUND.unifyErrorCode,
                        CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_USERGROUP_COMMON_NOTFOUND.messageTemplate, id))
        );
        userService.deleteUserGroup(userGroup);
        return ResponseEntity.noContent().build();
    }

    /**
     * list user group by page
     *
     * @param pageable      page info
     * @param userPrincipal user principal
     * @return user group list by page
     */
    @ApiOperation(value = "List user groups",
            nickname = "listUserGroups",
            notes = "List user groups with paging, default page size 20",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping("/user_groups")
    @RolesAllowed("ROLE_USER")
    @Dto(UserGroupDto.class)
    public ResponseEntity<Page<UserGroup>> listUserGroups(Pageable pageable, @CurrentUser UserPrincipal userPrincipal) {
        log.info("[listUserGroups] principal username: {}", userPrincipal.getUsername());
        Page<UserGroup> userGroups = this.userService.findAllUserGroup(pageable);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(userGroups);
    }

    /**
     * add user to user group
     *
     * @param id            user group id
     * @param userId        user id
     * @param userPrincipal user principal
     * @return user info
     * @throws AppException
     */
    @ApiOperation(value = "Add user to user group",
            nickname = "addUserToUserGroup",
            notes = "Add user to user group, administrator right is needed",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping("/user_group/{id}/user/{userId}")
    @RolesAllowed("ROLE_ADMIN")
    public ResponseEntity<UserDto> addUserToUserGroup(@PathVariable UUID id, @PathVariable UUID userId, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[addUserToUserGroup] id: {}, userId: {}, principal username: {}", id, userId, userPrincipal.getUsername());
        User user = userService.addUserToUserGroup(id, userId, userPrincipal.getUsername());
        UserDto result = UserService.convertUserToDtoWithDetails(user);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    /**
     * remove user from user group
     *
     * @param id            user group id
     * @param userId        user id
     * @param userPrincipal user principal
     * @return user info
     * @throws AppException
     */
    @ApiOperation(value = "Remove user from user group",
            nickname = "removeUserFromUserGroup",
            notes = "Remove user from user group, administrator right is needed",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @DeleteMapping("/user_group/{id}/user/{userId}")
    @RolesAllowed("ROLE_ADMIN")
    public ResponseEntity<UserDto> removeUserFromUserGroup(@PathVariable UUID id, @PathVariable UUID userId, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[removeUserFromUserGroup] id: {}, userId: {}, principal username: {}", id, userId, userPrincipal.getUsername());
        User user = userService.removeUserFromUserGroup(id, userId, userPrincipal.getUsername());
        UserDto result = UserService.convertUserToDtoWithDetails(user);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    /**
     * update a user status
     *
     * @param id
     * @param status
     * @param userPrincipal user principal
     * @return
     */
    @ApiOperation(value = "Update user status",
            nickname = "updateStatusById",
            notes = "Update user status",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PutMapping("/user/{id}/status/{status}")
    @RolesAllowed("ROLE_ADMIN")
    public ResponseEntity<UserDto> updateStatusById(@PathVariable UUID id, @PathVariable User.Status status, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[updateStatusById] id: {}, status: {}, principal username: {}", id, status, userPrincipal.getUsername());
        User user = userService.updateStatus(id, status, userPrincipal.getUsername());
        UserDto result = UserService.convertUserToDtoWithDetails(user);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    /**
     * Unlock user
     *
     * @param id
     * @param userPrincipal user principal
     * @return
     */
    @ApiOperation(value = "Unlock User",
            nickname = "unlockUser",
            notes = "Unlock User",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping("/user/{id}/action/unlock")
    @RolesAllowed("ROLE_ADMIN")
    public ResponseEntity<UserDto> unlockUser(@PathVariable UUID id,
                                              @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[unlockUser] id: {}, principal username: {}", id, userPrincipal.getUsername());
        User user = userService.unlockUser(id, userPrincipal.getUsername());
        UserDto result = UserService.convertUserToDtoWithDetails(user);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    /**
     * Get Top Assignees
     *
     * @param id
     * @param userPrincipal user principal
     * @return
     */
    @ApiOperation(value = "Top Assignees",
            nickname = "topAssignees",
            notes = "Top Assignees",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping("/project/{id}/top_assignees")
    public ResponseEntity<List<UserCountDto>> getTopAssignees(@PathVariable UUID id, Pageable pageable,
                                                              @RequestBody SearchIssueGroupRequest searchIssueGroupRequest,
                                                              @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getTopAssignees] id: {}, principal username: {}", id, userPrincipal.getUsername());

        List<String> scanFilePaths = null;
        if ((searchIssueGroupRequest.getScanFileIds() != null) && !searchIssueGroupRequest.getScanFileIds().isEmpty()) {
            List<ScanFile> scanFiles = this.scanFileService.findByScanFileIds(searchIssueGroupRequest.getScanFileIds());
            if ((scanFiles != null) && !scanFiles.isEmpty()) {
                scanFilePaths = scanFiles.stream().map(ScanFile::getProjectRelativePath).collect(Collectors.toList());
            }
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(userService.getTopAssignees(
                        id,
                        searchIssueGroupRequest.getScanTaskId(),
                        searchIssueGroupRequest.getRuleCodes(),
                        searchIssueGroupRequest.getRuleSets(),
                        scanFilePaths,
                        searchIssueGroupRequest.getPathCategory(),
                        searchIssueGroupRequest.getCertainty(),
                        searchIssueGroupRequest.getDsrType(),
                        searchIssueGroupRequest.getCriticality(),
                        searchIssueGroupRequest.getSearchValue(),
                        pageable));

    }


    /**
     * Get account config
     * @param userPrincipal user principal
     * @return
     */
    @ApiOperation(value = "Get account config",
            nickname = "getAccountConfig",
            notes = "Get Account config",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping("/config")
    public ResponseEntity<AccountConfigResponse> getAccountConfig(@CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[getAccountConfig] principal username: {}", userPrincipal.getUsername());
        Integer configNumCodeDisplay = userPrincipal.getUser().getConfigNumCodeDisplay();
        AccountConfigResponse accountConfigResponse=AccountConfigResponse.builder()
                .configNumCodeDisplay(configNumCodeDisplay)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(accountConfigResponse);
    }

    /**
     * Update account config
     * @param accountConfigRequest the request body of configs being updated
     * @param userPrincipal user principal
     * @return
     */
    @ApiOperation(value = "Update account config",
            nickname = "updateAccountConfig",
            notes = "Update Account config",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PutMapping("/config")
    public ResponseEntity<Void> updateAccountConfig(@RequestBody AccountConfigRequest accountConfigRequest, @CurrentUser UserPrincipal userPrincipal) throws AppException {
        log.info("[updateAccountConfig] accountConfigRequest:{}, principal username: {}", accountConfigRequest, userPrincipal.getUsername());
        userService.updateConfig(accountConfigRequest.getConfigNumCodeDisplay(), userPrincipal.getUser());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON).build();
    }



}
