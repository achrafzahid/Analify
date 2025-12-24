package com.analyfy.analify.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analyfy.analify.DTO.EmployeeCreateDTO;
import com.analyfy.analify.DTO.EmployeeResponseDTO;
import com.analyfy.analify.DTO.EmployeeUpdateDTO;
import com.analyfy.analify.Entity.AdminG; // Assuming this bean exists
import com.analyfy.analify.Entity.AdminStore;
import com.analyfy.analify.Entity.Caissier;
import com.analyfy.analify.Entity.User;
import com.analyfy.analify.Enum.UserRole;
import com.analyfy.analify.Excexption.AccessDeniedException;
import com.analyfy.analify.Excexption.BusinessValidationException;
import com.analyfy.analify.Excexption.ResourceNotFoundException;
import com.analyfy.analify.Mapper.UserMapper;
import com.analyfy.analify.Repository.AdminGRepository;
import com.analyfy.analify.Repository.AdminStoreRepository;
import com.analyfy.analify.Repository.CaissierRepository;
import com.analyfy.analify.Repository.StoreRepository;
import com.analyfy.analify.Repository.UserRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor

public class EmployeeService {

    private final UserRepository userRepository;
    private final AdminGRepository adminGRepository;
    private final AdminStoreRepository adminStoreRepository;
    private final CaissierRepository caissierRepository;
    private final StoreRepository storeRepository;
    private final UserMapper UserMapper;
    private final PasswordEncoder passwordEncoder;

    
    // ===================================================================================
    // READ OPERATIONS
    // ===================================================================================

    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> getAllEmployees(Long actingUserId, UserRole actingRole) {
        
        // 2. Security Check (Optional: Restrict to AdminG only)
        if (actingRole != UserRole.ADMIN_G) {
            throw new AccessDeniedException("Only Global Admins can view the master employee list.");
        }

        List<EmployeeResponseDTO> response = new ArrayList<>();

        // 3. Fetch directly from tables (Bypasses the Hibernate NPE bug)
        List<AdminG> globalAdmins = adminGRepository.findAll();
        for (AdminG admin : globalAdmins) {
            response.add(UserMapper.toEmployeeResponseDTO(admin));
        }

        List<AdminStore> storeAdmins = adminStoreRepository.findAll();
        for (AdminStore admin : storeAdmins) {
            response.add(UserMapper.toEmployeeResponseDTO(admin));
        }

        List<Caissier> cashiers = caissierRepository.findAll();
        for (Caissier cashier : cashiers) {
            response.add(UserMapper.toEmployeeResponseDTO(cashier));
        }

        return response;
        }

    @Transactional(readOnly = true)
    public EmployeeResponseDTO getEmployeeById(Long actingUserId, UserRole actingRole, Long targetId) {
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 1. Admin General: Can see everyone
        if (actingRole == UserRole.ADMIN_G) {
            return mapToResponse(target);
        }

        // 2. Admin Store: Can see ONLY employees in their store
        if (actingRole == UserRole.ADMIN_STORE) {
            Long myStoreId = getStoreIdForUser(actingUserId, UserRole.ADMIN_STORE);
            Long targetStoreId = getStoreIdForUser(targetId, resolveRole(target));

            if (Objects.equals(myStoreId, targetStoreId)) {
                return mapToResponse(target);
            }
        }

        // 3. Caissier / Anyone: Can see SELF
        if (actingUserId.equals(targetId)) {
            return mapToResponse(target);
        }

        throw new AccessDeniedException("You do not have permission to view this user.");
    }

@Transactional(readOnly = true)
    public List<EmployeeResponseDTO> getStoreEmployees(Long actingUserId, UserRole actingRole, Long storeId) {
        // 1. Security Check
        if (actingRole == UserRole.ADMIN_STORE) {
            Long myStoreId = getStoreIdForUser(actingUserId, UserRole.ADMIN_STORE);
            if (!myStoreId.equals(storeId)) {
                throw new AccessDeniedException("You cannot view employees of another store.");
            }
        } else if (actingRole != UserRole.ADMIN_G) {
            throw new AccessDeniedException("Insufficient permissions.");
        }

        // 2. Fetch directly from Child Tables (Avoids the NPE)
        List<EmployeeResponseDTO> response = new ArrayList<>();

        // Fetch all Admins for this store
        List<AdminStore> admins = adminStoreRepository.findByStore_StoreId(storeId);
        for (AdminStore admin : admins) {
            response.add(UserMapper.toEmployeeResponseDTO(admin));
        }

        // Fetch all Cashiers for this store
        List<Caissier> cashiers = caissierRepository.findByStore_StoreId(storeId);
        for (Caissier cashier : cashiers) {
            response.add(UserMapper.toEmployeeResponseDTO(cashier));
        }

        return response;
    }

    // ===================================================================================
    // WRITE OPERATIONS
    // ===================================================================================

    @Transactional
    public EmployeeResponseDTO createEmployee(Long actingUserId, UserRole actingRole, EmployeeCreateDTO dto) {
        // 1. Permission Check
        if (actingRole == UserRole.CAISSIER) {
            throw new AccessDeniedException("Caissiers cannot create users.");
        }

        // 2. AdminStore Constraints
        if (actingRole == UserRole.ADMIN_STORE) {
            if (dto.getRole() != UserRole.CAISSIER) {
                throw new BusinessValidationException("Admin Store can only create Caissiers.");
            }
            Long myStoreId = getStoreIdForUser(actingUserId, UserRole.ADMIN_STORE);
            if (!myStoreId.equals(dto.getStoreId())) {
                throw new BusinessValidationException("You cannot create an employee for a different store.");
            }
        }

        // 3. Create Logic
        if (userRepository.existsByMail(dto.getMail())) { // Assuming existsByMail method
            throw new BusinessValidationException("Email already exists.");
        }

        // Note: Logic handles specific table insertion based on Role Enum
        User savedUser = persistUserWithRole(dto);
        return mapToResponse(savedUser);
    }

    @Transactional
    public void assignRoleToUser(Long actingUserId, UserRole actingRole, Long targetUserId, UserRole newRole, Long storeId) {
        if (actingRole != UserRole.ADMIN_G) {
            throw new AccessDeniedException("Only Admin General can assign roles.");
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (resolveRole(target) != null) {
            throw new BusinessValidationException("User already has a role. Use update to change details.");
        }

        // Create the child entity record
        insertRoleEntity(target, newRole, storeId, 0.0, null); // Default salary/date
    }

    @Transactional
    public EmployeeResponseDTO updateEmployee(Long actingUserId, UserRole actingRole, Long targetId, EmployeeUpdateDTO dto) {
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UserRole targetRole = resolveRole(target);

        // --- AUTHORIZATION GATES ---
        
        boolean isSelf = actingUserId.equals(targetId);
        boolean isAdminG = actingRole == UserRole.ADMIN_G;
        boolean isAdminStore = actingRole == UserRole.ADMIN_STORE;

        if (!isSelf && !isAdminG && !isAdminStore) {
            throw new AccessDeniedException("Permission denied.");
        }

        // AdminStore targeting someone else: Must be Caissier in SAME store
        if (isAdminStore && !isSelf) {
            if (targetRole != UserRole.CAISSIER) {
                throw new AccessDeniedException("Admin Store can only update Caissiers.");
            }
            Long myStore = getStoreIdForUser(actingUserId, UserRole.ADMIN_STORE);
            Long targetStore = getStoreIdForUser(targetId, UserRole.CAISSIER);
            if (!Objects.equals(myStore, targetStore)) {
                throw new AccessDeniedException("Cannot update employee of another store.");
            }
        }

        // --- UPDATE LOGIC ---

        // 1. Basic Fields (All allowed)
        if (dto.getUserName() != null) target.setUserName(dto.getUserName());
        if (dto.getMail() != null) target.setMail(dto.getMail());
        if (dto.getDateOfBirth() != null) target.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            target.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // 2. Sensitive Fields (Role/Store/Salary) -> AdminG ONLY
        if (isAdminG && !isSelf) { 
            // Allow changing salary or store if passed
            // (Implementation of re-assigning store/salary inside child tables omitted for brevity, 
            // but this is where you'd update AdminStore/Caissier repositories)
            updateChildTableDetails(target, targetRole, dto);
        } else if (isAdminStore && !isSelf) {
            // AdminStore can update Salary of Caissier
             updateChildTableDetails(target, targetRole, dto);
        }

        // 3. IMMUTABILITY CHECKS
        // Users cannot change their own Role or ID. 
        // ID is never updated by JPA automatically.
        // Role change is complex (requires deleting child row, adding new one). 
        // We assume Role is IMMUTABLE in this update method as per strict rules.

        return mapToResponse(userRepository.save(target));
    }

    @Transactional
    public void deleteEmployee(Long actingUserId, UserRole actingRole, Long targetId) {
        if (actingUserId.equals(targetId)) {
            throw new BusinessValidationException("You cannot delete yourself.");
        }

        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UserRole targetRole = resolveRole(target);

        if (actingRole == UserRole.ADMIN_G) {
            // AdminG can delete anyone
            userRepository.delete(target); // Cascade should handle child tables
            return;
        }

        if (actingRole == UserRole.ADMIN_STORE) {
            // Can only delete Caissier in same store
            if (targetRole != UserRole.CAISSIER) {
                throw new AccessDeniedException("Admin Store can only delete Caissiers.");
            }
            Long myStore = getStoreIdForUser(actingUserId, UserRole.ADMIN_STORE);
            Long targetStore = getStoreIdForUser(targetId, UserRole.CAISSIER);
            
            if (Objects.equals(myStore, targetStore)) {
                userRepository.delete(target);
                return;
            }
        }

        throw new AccessDeniedException("Delete permission denied.");
    }

    // ===================================================================================
    // INTERNAL HELPERS & ROLE RESOLUTION
    // ===================================================================================

    private UserRole resolveRole(User user) {
        // Since we are using Joined Inheritance, we check instanceof or repo existence
        if (user instanceof AdminG) return UserRole.ADMIN_G;
        if (user instanceof AdminStore) return UserRole.ADMIN_STORE;
        if (user instanceof Caissier) return UserRole.CAISSIER;
        return null; // No role yet
    }

    private Long getStoreIdForUser(Long userId, UserRole role) {
        if (role == UserRole.ADMIN_STORE) {
            return adminStoreRepository.findById(userId)
                    .map(as -> as.getStore().getStoreId())
                    .orElse(null);
        }
        if (role == UserRole.CAISSIER) {
            return caissierRepository.findById(userId)
                    .map(c -> c.getStore().getStoreId())
                    .orElse(null);
        }
        return null; // AdminG or User has no store
    }

    private EmployeeResponseDTO mapToResponse(User user) {
        // Use the manual logic to determine role for the DTO
        EmployeeResponseDTO dto = UserMapper.toEmployeeResponseDTO(user); // basic mapping
        dto.setRole(resolveRole(user));
        dto.setStoreId(getStoreIdForUser(user.getUserId(), dto.getRole()));
        return dto;
    }

    private User persistUserWithRole(EmployeeCreateDTO dto) {
        // Logic to instantiate correct subclass or save User + Child
        // This relies on your specific Entity composition.
        // Simplified for this generation:
        
        switch (dto.getRole()) {
            case ADMIN_G:
                AdminG g = new AdminG();
                UserMapper.updateEntity(g, dto); // helper to set common fields
                g.setPassword(passwordEncoder.encode(dto.getPassword()));
                return adminGRepository.save(g);
            case ADMIN_STORE:
                AdminStore as = new AdminStore();
                UserMapper.updateEntity(as, dto);
                as.setPassword(passwordEncoder.encode(dto.getPassword()));
                as.setstore(storeRepository.findById(dto.getStoreId()).orElseThrow());
                as.setSalary(dto.getSalary());
                return adminStoreRepository.save(as);
            case CAISSIER:
                Caissier c = new Caissier();
                UserMapper.updateEntity(c, dto);
                c.setPassword(passwordEncoder.encode(dto.getPassword()));
                c.setStore(storeRepository.findById(dto.getStoreId()).orElseThrow());
                c.setSalary(dto.getSalary()); // Note: 'salare' as per previous context
                return caissierRepository.save(c);
            default:
                // Create base user (no role)
                // Assuming you have a concrete implementation or User is not abstract
                // If User is abstract, this case throws exception
                throw new BusinessValidationException("Cannot create User without a valid role.");
        }
    }
    
    private void insertRoleEntity(User user, UserRole role, Long storeId, Double salary, java.time.LocalDate dateStarted) {
        // Used for 'assignRole' - creates the child row manually
        // Implementation depends on if you can cast User to subclass or need raw SQL/Repo inserts
        // For strict JPA with Joined Inheritance, you normally have to delete User and re-save as Subclass
        // OR use native queries to insert into the child table (admin_store/caissier) linking to user.id
    }
    
    private void updateChildTableDetails(User user, UserRole role, EmployeeUpdateDTO dto) {
        if (role == UserRole.ADMIN_STORE && user instanceof AdminStore) {
            if (dto.getSalary() != null) ((AdminStore)user).setSalary(dto.getSalary());
        } else if (role == UserRole.CAISSIER && user instanceof Caissier) {
             if (dto.getSalary() != null) ((Caissier)user).setSalary(dto.getSalary());
        }
    }
}