package fu.de190060.user.service.impl;

import fu.de190060.user.common.UserConstants;
import fu.de190060.user.dto.ApiResponse;
import fu.de190060.user.dto.PageDTO;
import fu.de190060.user.dto.UserDTO;
import fu.de190060.user.entity.Role;
import fu.de190060.user.entity.User;
import fu.de190060.user.entity.UserStatus;
import fu.de190060.user.repository.UserRepository;
import fu.de190060.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ApiResponse createUser(UserDTO dto) {
        // status 3 -> duplicate username
        if (userRepository.existsByUsername(dto.getUsername())) {
            return ApiResponse.error(3, "Username is duplicated");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setRole(Role.valueOf(dto.getRole()));
        user.setRatePerPost(dto.getRatePerPost());
        user.setMaxDailyPosts(dto.getMaxDailyPosts());
        user.setLevel(dto.getLevel());
        // Default status is ACTIVE when not provided
        user.setStatus(dto.getStatus() == null ? UserStatus.ACTIVE : UserStatus.valueOf(dto.getStatus()));
        User saved = userRepository.save(user);
        return ApiResponse.ok("User created successfully", toDTO(saved));
    }

    @Override
    @Transactional
    public ApiResponse updateUser(Long userId, UserDTO dto) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ApiResponse.error(4, "User is not found");
        }
        // Duplicate username (excluding the current user)
        if (dto.getUsername() != null
                && userRepository.existsByUsernameAndIdNot(dto.getUsername(), userId)) {
            return ApiResponse.error(3, "Username is duplicated");
        }
        // Partial update: only non-null fields are applied
        if (dto.getUsername() != null) user.setUsername(dto.getUsername());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getRole() != null) user.setRole(Role.valueOf(dto.getRole()));
        if (dto.getRatePerPost() != null) user.setRatePerPost(dto.getRatePerPost());
        if (dto.getMaxDailyPosts() != null) user.setMaxDailyPosts(dto.getMaxDailyPosts());
        if (dto.getLevel() != null) user.setLevel(dto.getLevel());
        if (dto.getStatus() != null) user.setStatus(UserStatus.valueOf(dto.getStatus()));
        User saved = userRepository.save(user);
        return ApiResponse.ok("User updated successfully", toDTO(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse getUserById(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ApiResponse.error(4, "User is not found");
        }
        return ApiResponse.ok("Successful", toDTO(user));
    }

    @Override
    @Transactional
    public ApiResponse deactivateUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ApiResponse.error(4, "User is not found");
        }
        user.setStatus(UserStatus.DEACTIVATED);
        userRepository.save(user);
        return ApiResponse.ok("User is set to DEACTIVATED successfully", null);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse getUserList(Integer page, Integer size, String role, String status) {
        int pageNumber = page == null ? UserConstants.DEFAULT_PAGE : page;
        int pageSize = size == null ? UserConstants.DEFAULT_SIZE : size;
        if (pageNumber < 0) {
            return ApiResponse.error(2, "page must be >= 0");
        }
        if (pageSize < 1 || pageSize > UserConstants.MAX_SIZE) {
            return ApiResponse.error(2, "size must be between 1 and " + UserConstants.MAX_SIZE);
        }
        Role roleFilter = null;
        UserStatus statusFilter = null;
        try {
            if (role != null && !role.isBlank()) roleFilter = Role.valueOf(role);
            if (status != null && !status.isBlank()) statusFilter = UserStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(2, "role or status filter is invalid");
        }

        Page<User> result = userRepository.search(roleFilter, statusFilter,
                PageRequest.of(pageNumber, pageSize, Sort.by("id")));
        List<UserDTO> content = result.getContent().stream().map(this::toDTO).toList();
        PageDTO pageDTO = new PageDTO(
                result.getSize(), result.getNumber(), result.getTotalPages(),
                result.getTotalElements(), result.isFirst(), result.isLast(), content);
        return ApiResponse.ok("Successful", pageDTO);
    }

    private UserDTO toDTO(User u) {
        UserDTO d = new UserDTO();
        d.setUserId(u.getId());
        d.setUsername(u.getUsername());
        d.setEmail(u.getEmail());
        d.setRole(u.getRole().name());
        d.setRatePerPost(u.getRatePerPost());
        d.setMaxDailyPosts(u.getMaxDailyPosts());
        d.setLevel(u.getLevel());
        d.setStatus(u.getStatus().name());
        return d;
    }
}
