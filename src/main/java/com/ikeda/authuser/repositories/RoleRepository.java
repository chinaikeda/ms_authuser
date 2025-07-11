package com.ikeda.authuser.repositories;

import com.ikeda.authuser.enums.RoleType;
import com.ikeda.authuser.models.RoleModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<RoleModel, UUID> {
    Optional<RoleModel> findByRoleName(RoleType name);
}
