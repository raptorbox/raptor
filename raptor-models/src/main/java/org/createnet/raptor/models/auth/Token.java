/*
 * Copyright 2017 FBK/CREATE-NET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.createnet.raptor.models.auth;

import javax.persistence.FetchType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"}, ignoreUnknown = true)
@Document
//@Cacheable(value = true)
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Token implements Serializable {

    public static enum Type {
        LOGIN, DEFAULT
    }

    public static enum TokenType {
        DEFAULT, JWT
    }

    @Id
    private String id;

    @NotNull
    @Size(min = 1)
    private String name;

    @NotNull
    @Size(min = 1)
    @Indexed(unique = true)
    private String token;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotEmpty
    private String secret;

    private boolean enabled = true;

    @JsonIgnore
    private String userId;

    @JsonIgnore
    @Indexed
    private List<String> devices = new ArrayList();

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date created = new Date();

    private Long expires = 1000L * 60 * 60; // default to 60min

    @Enumerated(EnumType.STRING)
    private Type type = Type.DEFAULT;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType = TokenType.DEFAULT;

    public Token() {
    }

    public Token(String name, String secret) {
        this.name = name;
        this.secret = secret;
    }

    public Token(final Token token) {
        this.name = token.getName();
        this.secret = token.getSecret();
        this.expires = token.getExpires();
        this.setDevices(token.getDevices());
        this.enabled = token.getEnabled();
        this.userId = token.getUserId();
    }

    public void merge(Token rawToken) {

        if (rawToken.getDevices() != null && !rawToken.getDevices().isEmpty()) {
            this.setDevices(rawToken.getDevices());
        }

        if (rawToken.getEnabled() != null) {
            this.setEnabled(rawToken.getEnabled());
        }

        if (rawToken.getExpires() != null) {
            this.setExpires(rawToken.getExpires());
        }

        if (rawToken.getName() != null) {
            this.setName(rawToken.getName());
        }

        if (rawToken.getUserId() != null) {
            this.setUserId(rawToken.getUserId());
        }

        if (rawToken.getSecret() != null) {
            this.setSecret(rawToken.getSecret());
        }

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Date getCreated() {
        return created;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    @JsonIgnore
    public Instant getExpiresInstant() {
        if (expires == null) {
            return null;
        }
        return getCreated().toInstant().plusSeconds(expires);
    }

    public Long getExpires() {
        return expires;
    }

    public void setExpires(Long expires) {
        if (expires == 0) {
            expires = 622080000L; //20 years, should be enough for our retirement
        }
        this.expires = expires;
    }

    public boolean isExpired() {
        if (getExpiresInstant() == null) {
            return true;
        }
        return getExpiresInstant().isBefore(Instant.now());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isValid() {
        return isEnabled() && !isExpired();
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @JsonIgnore
    public boolean isLoginToken() {
        return this.getType().equals(Type.LOGIN);
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = TokenType.valueOf(tokenType);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getDevices() {
        return devices;
    }

    public void setDevices(List<String> devices) {
        this.devices.clear();
        this.devices.addAll(devices);
    }

}
