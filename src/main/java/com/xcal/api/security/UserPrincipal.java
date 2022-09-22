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
import com.xcal.api.entity.UserGroup;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPrincipal implements OAuth2User, UserDetails {

    private UUID id;
    private String username;
    private String email;
    private User user;

    @ToString.Exclude
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    public static UserPrincipal create(User user) {
        log.trace("[create] {}", user);
        List<GrantedAuthority> auths = new ArrayList<>();
        auths.add(new SimpleGrantedAuthority("ROLE_USER"));
        for(UserGroup userGroup : user.getUserGroups().stream().filter(userGroup -> userGroup.getGroupType() == UserGroup.Type.ROLE).collect(Collectors.toList())){
            auths.add(new SimpleGrantedAuthority("ROLE_" + StringUtils.upperCase(userGroup.getGroupName())));
        }
        return UserPrincipal.builder()
                .id(user.getId())
                .user(user)
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .authorities(auths)
                .build();
    }

    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        log.trace("[create] user: {},  attributes: {}", user, attributes);
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
    @Override
    public String getName() {
        return String.valueOf(id);
    }
}
