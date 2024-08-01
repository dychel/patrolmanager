package com.stocktool.stocktool.service;
import com.stocktool.stocktool.repository.RoleRepository;
import com.stocktool.stocktool.entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    RoleRepository roleRepository;

    @Override
    public Role saveRole(Role role) {

        role.setRoleName(role.getRoleName().toUpperCase());
        role.setDateCreate(new Date());
        role.setDateUpdate(new Date());

        return roleRepository.save(role);
    }

    @Override
    public Role updateRole(Long id, Role role) {

        Role roleToUpdate = roleRepository.findByIdRole(id);

        if (roleToUpdate == null)
            return null;

        roleToUpdate.setRoleName(role.getRoleName().toUpperCase());
        roleToUpdate.setDateUpdate(new Date());

        return roleRepository.save(roleToUpdate);
    }

    @Override
    public Role findRoleById(Long id) {
        return roleRepository.findByIdRole(id);
    }

    @Override
    public List<Role> listRoles() {
        return roleRepository.findAll();
    }

    @Override
    public void deleteRoleById(Long id) {
        roleRepository.deleteById(id);
    }
}
