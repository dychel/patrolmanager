package com.patrolmanagr.patrolmanagr.controller;
import com.patrolmanagr.patrolmanagr.entity.Role;
import com.patrolmanagr.patrolmanagr.entity.User;
import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.service.RoleService;
import com.patrolmanagr.patrolmanagr.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/v1/patrolmanagr/role/*")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public ResponseEntity<?> createRole(@RequestBody Role roleRequest) {

        if (roleRequest == null)
            return new ResponseEntity<ResponseMessage>(new ResponseMessage("chao", "Verify your information's please !", null), HttpStatus.OK);

        Role roleSaved = roleService.saveRole(roleRequest);

        if (roleSaved == null)
            return new ResponseEntity<ResponseMessage>(new ResponseMessage("chao", "role failed saved", roleSaved), HttpStatus.OK);

        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "role successfully saved", roleSaved), HttpStatus.OK);
    }

    @PutMapping(value = "/update/{id}")
    public ResponseEntity<?> updateRole(@PathVariable(value = "id") Long id, @RequestBody Role roleForm) {

        if (id == null || roleForm == null)
            return new ResponseEntity<ResponseMessage>(new ResponseMessage("chao", "Verify your information's please !", null), HttpStatus.OK);

        Role roleUpdated = roleService.updateRole(id, roleForm);

        if (roleUpdated == null)
            return new ResponseEntity<ResponseMessage>(new ResponseMessage("chao", "role failed updated !", null), HttpStatus.OK);

        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "role successfully updated !", roleUpdated), HttpStatus.OK);
    }

    @GetMapping(value = "/all")
    public ResponseEntity<?> getAllRoles() {
        List<Role> roles = roleService.listRoles();
        if (roles.isEmpty())
            return new ResponseEntity<ResponseMessage>(new ResponseMessage("chao", "list role is empty", null), HttpStatus.OK);

        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "list roles", roles), HttpStatus.OK);
    }

    @GetMapping(value = "/findbyid/{id}")
    public ResponseEntity<?> getRoleById(@PathVariable(value = "id") Long id) {

        if (id == null)
            return new ResponseEntity<ResponseMessage>(new ResponseMessage("chao", "Verify your information's please !", null),
                    HttpStatus.OK);

        Role role = roleService.findRoleById(id);

        if (role == null)
            return new ResponseEntity<ResponseMessage>(new ResponseMessage("chao", "role not found", null),
                    HttpStatus.OK);

        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "role found", role),
                HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable(value = "id") Long id) {

        List<User> usersRole = userService.getUserByRole(id);

        if (!usersRole.isEmpty())
            return new ResponseEntity<ResponseMessage>(new ResponseMessage("chao", "impossible to delete because the role is linked to one or more users. Please delete the user first or change its role.", usersRole),
                    HttpStatus.OK);

        roleService.deleteRoleById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "role successfully deleted !", null), HttpStatus.OK);
    }
}
