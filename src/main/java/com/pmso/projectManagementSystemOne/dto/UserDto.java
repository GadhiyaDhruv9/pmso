package com.pmso.projectManagementSystemOne.dto;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Objects;

public final class UserDto {
    private final Long id;
    private final String username;
    private final String email;
    private final String roles;

    public UserDto(
            Long id,
            String username,
            String email,
            String roles
    ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }

    public Long id() {
        return id;
    }

    public String username() {
        return username;
    }

    public String email() {
        return email;
    }

    public String roles() {
        return roles;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (UserDto) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.username, that.username) &&
                Objects.equals(this.email, that.email) &&
                Objects.equals(this.roles, that.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, email, roles);
    }

    @Override
    public String toString() {
        return "UserDto[" +
                "id=" + id + ", " +
                "username=" + username + ", " +
                "email=" + email + ", " +
                "roles=" + roles + ']';
    }
}
