package com.ikeda.authuser.services.impl;

import com.ikeda.authuser.enums.RoleType;
import com.ikeda.authuser.models.RoleModel;
import com.ikeda.authuser.repositories.RoleRepository;
import com.ikeda.authuser.services.RoleService;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {

    final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public RoleModel findByRoleName(RoleType roleType) {
        return roleRepository.findByRoleName(roleType)
                .orElseThrow(() -> new RuntimeException("Error: Role is Not Found."));
    }
}
