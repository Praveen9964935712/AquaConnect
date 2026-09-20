package com.aquaconnect.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import com.aquaconnect.backend.entity.Role;
import com.aquaconnect.backend.entity.User;
import com.aquaconnect.backend.entity.UserRole;
import com.aquaconnect.backend.enums.RoleName;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRoleRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Test
    void shouldPersistUserAndRole() {
        User user = userRepository.saveAndFlush(new User("citizen01", "citizen01@example.com", "placeholder-hash"));
        Role role = existingRole(RoleName.CITIZEN);

        UserRole userRole = userRoleRepository.saveAndFlush(new UserRole(user, role));

        assertThat(user.getId()).isNotNull();
        assertThat(role.getId()).isNotNull();
        assertThat(userRole.getId()).isNotNull();
        assertThat(userRoleRepository.findByUserId(user.getId())).hasSize(1);
        assertThat(userRoleRepository.findByUserIdAndRoleId(user.getId(), role.getId())).isPresent();
    }

    @Test
    void shouldPersistRoleRepositoryEntry() {
        Role persisted = existingRole(RoleName.OPERATOR);

        assertThat(persisted.getId()).isNotNull();
        assertThat(roleRepository.findByName(RoleName.OPERATOR)).isPresent();
    }

    @Test
    void shouldPersistUserRepositoryEntry() {
        User persisted = userRepository.saveAndFlush(new User("opsmgr01", "opsmgr01@example.com", "placeholder-hash"));

        assertThat(persisted.getId()).isNotNull();
        assertThat(userRepository.findByUsername("opsmgr01")).isPresent();
        assertThat(userRepository.findByEmail("opsmgr01@example.com")).isPresent();
    }

    @Test
    void shouldRejectDuplicateRoleName() {
        Role existing = existingRole(RoleName.ADMIN);

        assertThatThrownBy(() -> roleRepository.saveAndFlush(new Role(existing.getName(), "Additional admin access")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldRejectDuplicateUserRoleCombination() {
        User user = userRepository.saveAndFlush(new User("fieldeng01", "fieldeng01@example.com", "placeholder-hash"));
        Role role = existingRole(RoleName.FIELD_ENGINEER);

        userRoleRepository.saveAndFlush(new UserRole(user, role));

        assertThatThrownBy(() -> userRoleRepository.saveAndFlush(new UserRole(user, role)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Role existingRole(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.saveAndFlush(new Role(roleName, roleName.name())));
    }
}