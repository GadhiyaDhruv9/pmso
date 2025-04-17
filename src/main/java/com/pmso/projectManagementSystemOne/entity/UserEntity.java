package com.pmso.projectManagementSystemOne.entity;

import com.pmso.projectManagementSystemOne.utils.CommonUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class UserEntity extends CommonUtil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long userId;

    @Column(name = "username", nullable = false, unique = true)
    @NotBlank(message = "Username is required")
    private String username;

    @Column(name = "password", nullable = false)
    @NotBlank(message = "Password is required")
    private String password;

    @Column(name = "role_names")
    private String roleNames;

    // RELATIONS

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_entity_id"),
            inverseJoinColumns = @JoinColumn(name = "roles_id")
    )
    private List<Role> roles = new ArrayList<>();

    @Column(name = "email")
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    public void addRole(Role role) {
        if (role != null && !this.roles.contains(role)) {
            this.roles.add(role);
            updateRoleNames();
        }
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRoleNames(String roleNames) {
        this.roleNames = roleNames;
    }

    public void removeRole(Role role) {
        if (role != null && this.roles.contains(role)) {
            this.roles.remove(role);
            updateRoleNames();
        }
    }

    private void updateRoleNames() {
        this.roleNames = this.roles.stream()
                .map(Role::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }


    private String jwtToken;

}