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

import com.xcal.api.entity.User;
import com.xcal.api.entity.UserGroup;
import com.xcal.api.security.UserPrincipal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.*;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    private final Map<String, UserPrincipal> principleMap = new HashMap<>();

    public WithMockCustomUserSecurityContextFactory (){
        User adminUser = User.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .password("password")
                .displayName("Admin")
                .email("admin@test.com")
                .userGroups(Collections.singletonList(UserGroup.builder().groupType(UserGroup.Type.ROLE).groupName("admin").build()))
                .build();
        UserPrincipal adminUserPrincipal = UserPrincipal.create(adminUser);
        User xcalAdminUser = User.builder()
                .id(UUID.randomUUID())
                .username("xcaladmin")
                .password("password")
                .displayName("Xcal Admin")
                .email("xcaladmin@test.com")
                .userGroups(Collections.singletonList(UserGroup.builder().groupType(UserGroup.Type.ROLE).groupName("xcaladmin").build()))
                .build();
        UserPrincipal xcalAdminUserPrincipal = UserPrincipal.create(xcalAdminUser);
        User normalUser = User.builder()
                .id(UUID.randomUUID())
                .username("user")
                .password("password")
                .displayName("Normal User")
                .email("normal@test.com")
                .userGroups(new ArrayList<>())
                .build();
        UserPrincipal normalUserPrincipal = UserPrincipal.create(normalUser);
        principleMap.put("user", normalUserPrincipal);
        principleMap.put("admin", adminUserPrincipal);
        principleMap.put("xcaladmin", xcalAdminUserPrincipal);
    }
    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser withUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        String username = StringUtils.isNotBlank(withUser.username())? withUser.username() : withUser.value();

        UserPrincipal principal = principleMap.get(username);
        if(principal == null){
            principal =  principleMap.get("user");
        }
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, "password", principal.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }
}
