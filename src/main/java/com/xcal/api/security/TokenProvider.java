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

import com.xcal.api.config.AppProperties;
import com.xcal.api.util.CommonUtil;
import com.xcal.api.util.TracerUtil;
import com.xcal.api.util.VariableUtil;
import io.jsonwebtoken.*;
import io.opentracing.Tracer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TokenProvider {

    @NonNull private AppProperties appProperties;
    @NonNull private Tracer tracer;
    @NonNull private RedisTemplate<String, String> redisTemplate;

    public String createToken(Authentication authentication) {
        log.debug("[createToken] userPrincipal: {}", ((UserPrincipal) authentication.getPrincipal()).getName());
        return this.createToken(authentication, appProperties.getAuth().getTokenExpirationMsec());
    }

    private String createToken(Authentication authentication, Long periodInMillisecond) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        log.debug("[createToken] userPrincipal: {}, periodInMillisecond: {}", ((UserPrincipal) authentication.getPrincipal()).getName(), periodInMillisecond);
        TracerUtil.setTag(tracer, TracerUtil.Tag.USERNAME, userPrincipal.getUser().getUsername());
        return this.createToken(userPrincipal.getId().toString(), periodInMillisecond);
    }

    public String createToken(String id) {
        log.debug("[createToken] id: {}", id);
        return this.createToken(id, appProperties.getAuth().getTokenExpirationMsec());
    }

    public String createToken(String id, Long periodInMillisecond) {
        log.debug("[createToken] id: {}, periodInMillisecond: {}", id, periodInMillisecond);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + periodInMillisecond);
        return Jwts.builder()
                .setSubject(id)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, appProperties.getAuth().getTokenSecret())
                .compact();
    }

    public String getUserIdFromToken(String token) {
        log.debug("[getUserIdFromToken] token: {}", token);
        Claims claims = Jwts.parser()
                .setSigningKey(appProperties.getAuth().getTokenSecret())
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public Date getExpirationDateFromTokenIgnoreExpiredException(String token) {
        log.debug("[getExpirationDateFromTokenIgnoreExpiredException] token: {}", token);
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(appProperties.getAuth().getTokenSecret())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            claims = e.getClaims();
        }
        return claims.getExpiration();
    }

    public Date getExpirationDateFromToken(String token) {
        log.debug("[getExpirationDateFromToken] token: {}", token);
        Claims claims = Jwts.parser()
                .setSigningKey(appProperties.getAuth().getTokenSecret())
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration();
    }

    public long getExpirationTimeFromToken(String token) {
        log.debug("[getExpirationTimeFromToken] token: {}", token);
        return getExpirationDateFromToken(token).getTime();
    }

    public String getTokenFromRequest(HttpServletRequest request) {
        log.debug("[getTokenFromRequest] request uri: {}", request.getRequestURI());
        String token = null;
        String bearerToken = request.getHeader("Authorization");
        String paramToken = request.getParameter("token");

        if (StringUtils.isNotBlank(bearerToken) && StringUtils.startsWithIgnoreCase(bearerToken, VariableUtil.BEARER_TOKEN_PREFIX)) {
            log.trace("[getTokenFromRequest] bearerToken retrieved from Authorization header");
            token = StringUtils.substringAfter(bearerToken, VariableUtil.BEARER_TOKEN_PREFIX);
            log.trace("[getTokenFromRequest] bearerToken :{}", bearerToken);
            log.trace("[getTokenFromRequest] token :{}", token);
        }else if (StringUtils.isNotBlank(paramToken)){
            log.trace("[getTokenFromRequest] token retrieved from parameter");
            token = paramToken;
        }
        return token;
    }

    public boolean validateToken(String authToken) {
        log.debug("[validateToken] authToken: {}", authToken);
        boolean result = false;
        try {
            if(!this.isBlackListed(authToken)){
                Jwts.parser().setSigningKey(appProperties.getAuth().getTokenSecret()).parseClaimsJws(authToken);
                result = true;
            }
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return result;
    }

    public void invalidateToken(String token, String reason) {
        log.debug("[invalidateToken] token: {}, reason: {}", token, reason);
        String key = CommonUtil.formatString(VariableUtil.BLACKLIST_LOGIN_TOKEN, hashToken(token));
        redisTemplate.opsForValue().set(key, reason);
        Date now = new Date();
        redisTemplate.expire(key, getExpirationTimeFromToken(token) - now.getTime(), TimeUnit.MILLISECONDS);
    }

    public boolean isBlackListed(String token) {
        log.debug("[isBlackListed] token: {}", token);
        boolean result = false;
        String key = CommonUtil.formatString(VariableUtil.BLACKLIST_LOGIN_TOKEN, hashToken(token));
        String reason = redisTemplate.opsForValue().get(key);
        if(StringUtils.isNotBlank(reason)){
            log.info("[isBlackListed] token was blacklisted, reason: {}", reason);
            result = true;
        }
        return result;
    }

    private String hashToken(String token) {
        log.debug("[hashToken] token: {}", token);
        return DigestUtils.md5Hex(token);
    }

}
