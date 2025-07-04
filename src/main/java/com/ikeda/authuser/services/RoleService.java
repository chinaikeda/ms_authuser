package com.ikeda.authuser.services;

import com.ikeda.authuser.enums.RoleType;
import com.ikeda.authuser.models.RoleModel;

public interface RoleService {

    RoleModel findByRoleName(RoleType roleType);
}
