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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Date;
import javax.persistence.Cacheable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"}, ignoreUnknown = true)
@Entity
@Cacheable(value = true)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "tokens")
public class Token {

    static final long serialVersionUID = 1000000000000002L;

    public static enum Type {
        LOGIN, DEFAULT
    }

    public static enum TokenType {
        DEFAULT, JWT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;

    @NotNull
    @Size(min = 1)
    private String name;

    @NotNull
    @Size(min = 1)
    @Column(unique = true, nullable = false)
    private String token;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(unique = false, nullable = true)
    @NotEmpty
    private String secret;

    @Column(unique = false, nullable = false)
    private boolean enabled = true;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private AclDevice device;

    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date created = new Date();

    @Column(name = "expires")
    private Long expires;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private Type type = Type.DEFAULT;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type")
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
        this.device = token.getDevice();
        this.enabled = token.getEnabled();
        this.user = token.getUser();
    }

    public void merge(Token rawToken) {

        if (rawToken.getDevice() != null) {
            this.setDevice(rawToken.getDevice());
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

        if (rawToken.getUser() != null) {
            this.setUser(rawToken.getUser());
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
        this.expires = expires;
    }

    @JsonIgnore
    public boolean isExpired() {
        if (this.expires == 0) {
            return false;
        }
        if (getExpiresInstant() == null) {
            return true;
        }
        return getExpiresInstant().isBefore(Instant.now());
    }

    public boolean isEnabled() {
        return enabled;
    }
    
    @JsonIgnore
    public boolean isValid() {
        return isEnabled() && !isExpired() && (getUser() != null && getUser().isEnabled());
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

    public AclDevice getDevice() {
        return device;
    }

    public void setDevice(AclDevice device) {
        this.device = device;
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

}
