package com.analyfy.analify.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.analyfy.analify.DTO.EmployeeCreateDTO;
import com.analyfy.analify.DTO.EmployeeResponseDTO;
import com.analyfy.analify.DTO.EmployeeUpdateDTO;
import com.analyfy.analify.Enum.UserRole;
import com.analyfy.analify.Service.EmployeeService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // SECURITY NOTE:
    // currently actingUser params are passed via Headers.
    // In future JWT impl, remove these args and use:
    // Long actingUserId = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();

    @GetMapping("/getall")
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees(
            @RequestHeader("X-Acting-User-Id") Long actingUserId,
            @RequestHeader("X-Acting-User-Role") UserRole actingUserRole) {
        return ResponseEntity.ok(employeeService.getAllEmployees(actingUserId, actingUserRole));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(
            @RequestHeader("X-Acting-User-Id") Long actingUserId,
            @RequestHeader("X-Acting-User-Role") UserRole actingUserRole,
            @PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(actingUserId, actingUserRole, id));
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<EmployeeResponseDTO>> getStoreEmployees(
            @RequestHeader("X-Acting-User-Id") Long actingUserId,
            @RequestHeader("X-Acting-User-Role") UserRole actingUserRole,
            @PathVariable Long storeId) {
        return ResponseEntity.ok(employeeService.getStoreEmployees(actingUserId, actingUserRole, storeId));
    }

    @PostMapping("/add")
    public ResponseEntity<EmployeeResponseDTO> createEmployee(
            @RequestHeader("X-Acting-User-Id") Long actingUserId,
            @RequestHeader("X-Acting-User-Role") UserRole actingUserRole,
            @RequestBody EmployeeCreateDTO dto) {
        return new ResponseEntity<>(
                employeeService.createEmployee(actingUserId, actingUserRole, dto),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}/assign-role")
    public ResponseEntity<Void> assignRole(
            @RequestHeader("X-Acting-User-Id") Long actingUserId,
            @RequestHeader("X-Acting-User-Role") UserRole actingUserRole,
            @PathVariable Long id,
            @RequestParam UserRole newRole,
            @RequestParam(required = false) Long storeId) {
        employeeService.assignRoleToUser(actingUserId, actingUserRole, id, newRole, storeId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @RequestHeader("X-Acting-User-Id") Long actingUserId,
            @RequestHeader("X-Acting-User-Role") UserRole actingUserRole,
            @PathVariable Long id,
            @RequestBody EmployeeUpdateDTO dto) {
        return ResponseEntity.ok(employeeService.updateEmployee(actingUserId, actingUserRole, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(
            @RequestHeader("X-Acting-User-Id") Long actingUserId,
            @RequestHeader("X-Acting-User-Role") UserRole actingUserRole,
            @PathVariable Long id) {
        employeeService.deleteEmployee(actingUserId, actingUserRole, id);
        return ResponseEntity.noContent().build();
    }
}