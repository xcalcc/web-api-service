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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
class CustomUserDetailsServiceTest {

    private UserRepository userRepository;

    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        customUserDetailsService = new CustomUserDetailsService(userRepository);
    }

    @Test
    void loadUserByUsername_Success() {
        log.info("[loadUserByUsername_Success]");
        String username = "user1";
        User user = User.builder().username(username).build();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        assertEquals(username, userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_Fail() {
        log.info("[loadUserByUsername_Fail]");
        String username = "user1";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername(username));
    }

    @Test
    void loadUserByUUId_Success() throws AppException {
        log.info("[loadUserByUUId_Success]");
        UUID uuid = UUID.randomUUID();
        User user = User.builder().id(uuid).build();
        when(userRepository.findById(uuid)).thenReturn(Optional.of(user));
        UserPrincipal userPrincipal = (UserPrincipal)customUserDetailsService.loadUserById(uuid);
        assertEquals(uuid, userPrincipal.getId());
    }

    @Test
    void loadUserByUUId_Fail() {
        log.info("[loadUserByUUId_Fail]");
        UUID uuid = UUID.randomUUID();
        when(userRepository.findById(uuid)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> customUserDetailsService.loadUserById(uuid));
    }

}
