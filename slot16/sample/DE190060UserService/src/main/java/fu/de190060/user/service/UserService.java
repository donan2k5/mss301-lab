package fu.de190060.user.service;

import fu.de190060.user.dto.ApiResponse;
import fu.de190060.user.dto.UserDTO;

public interface UserService {

    ApiResponse createUser(UserDTO dto);

    ApiResponse updateUser(Long userId, UserDTO dto);

    ApiResponse getUserById(Long userId);

    ApiResponse deactivateUser(Long userId);

    ApiResponse getUserList(Integer page, Integer size, String role, String status);
}
