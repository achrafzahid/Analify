package com.analyfy.analify.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
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
    // Long userId = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();

    @GetMapping("/getall")
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees(
        @RequestAttribute("userId") Long userId,
        @RequestAttribute("role") UserRole role) {
        return ResponseEntity.ok(employeeService.getAllEmployees(userId, role));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(
        @RequestAttribute("userId") Long userId,
        @RequestAttribute("role") UserRole role,
            @PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(userId, role, id));
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<EmployeeResponseDTO>> getStoreEmployees(
        @RequestAttribute("userId") Long userId,
        @RequestAttribute("role") UserRole role,
            @PathVariable Long storeId) {
        return ResponseEntity.ok(employeeService.getStoreEmployees(userId, role, storeId));
    }

    @PostMapping("/add")
    public ResponseEntity<EmployeeResponseDTO> createEmployee(
        @RequestAttribute("userId") Long userId,
        @RequestAttribute("role") UserRole role,
            @RequestBody EmployeeCreateDTO dto) {
        return new ResponseEntity<>(
                employeeService.createEmployee(userId, role, dto),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}/assign-role")
    public ResponseEntity<Void> assignRole(
        @RequestAttribute("userId") Long userId,
        @RequestAttribute("role") UserRole role,
            @PathVariable Long id,
            @RequestParam UserRole newRole,
            @RequestParam(required = false) Long storeId) {
        employeeService.assignRoleToUser(userId, role, id, newRole, storeId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
        @RequestAttribute("userId") Long userId,
        @RequestAttribute("role") UserRole role,
            @PathVariable Long id,
            @RequestBody EmployeeUpdateDTO dto) {
        return ResponseEntity.ok(employeeService.updateEmployee(userId, role, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(
        @RequestAttribute("userId") Long userId,
        @RequestAttribute("role") UserRole role,
            @PathVariable Long id) {
        employeeService.deleteEmployee(userId, role, id);
        return ResponseEntity.noContent().build();
    }
}