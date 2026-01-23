package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.entity.Role;

import java.util.List;

public interface RoleService {
    Role saveRole(Role role);
    Role updateRole(Long id, Role role);
    Role findRoleById(Long id);
    List<Role> listRoles();
    void deleteRoleById(Long id);
}
