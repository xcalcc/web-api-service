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

package com.xcal.api.security;


import com.xcal.api.entity.User;
import com.xcal.api.exception.AppException;
import com.xcal.api.repository.UserRepository;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.MessagesTemplate;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.util.Optional;
import java.util.UUID;

/**
 * Load user detail service
 */

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CustomUserDetailsService implements UserDetailsService {

    @NonNull
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username){
        log.info("[loadUserByUsername] username: {}", username);
        Optional<User> userOptional = userRepository.findByUsername(username);
        User user;
        if(userOptional.isPresent()){
            user = userOptional.get();
            log.info("[loadUserByUsername] user: {}", user);
        }else{
            user = userRepository.findByEmail(username).orElseThrow(() ->
                    new UsernameNotFoundException(CommonUtil.formatString("[User not found]. username: {}" ,username)));
        }
        return UserPrincipal.create(user);
    }
    public UserDetails loadUserById(String id) throws AppException {
        log.trace("[loadUserById] id: {}", id);
        UUID uuid = UUID.fromString(id);
        return this.loadUserById(uuid);
    }
    public UserDetails loadUserById(UUID id) throws AppException {
        log.trace("[loadUserById] id: {}", id);
        User user = userRepository.findById(id).orElseThrow(() ->
                new AppException(AppException.LEVEL_ERROR, AppException.ERROR_CODE_DATA_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.unifyErrorCode,
                    CommonUtil.formatString("[{}] id: {}", AppException.ErrorCode.E_API_USER_COMMON_NOT_FOUND.messageTemplate, id)));
        return UserPrincipal.create(user);
    }
}
