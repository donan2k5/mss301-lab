package fu.de190060.user.controller;

import fu.de190060.user.common.OnCreate;
import fu.de190060.user.dto.ApiResponse;
import fu.de190060.user.dto.UserDTO;
import fu.de190060.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class DE190060UserController {

    private final UserService userService;

    public DE190060UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createUser(@Validated(OnCreate.class) @RequestBody UserDTO dto) {
        return respond(userService.createUser(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable Long userId, @Valid @RequestBody UserDTO dto) {
        return respond(userService.updateUser(userId, dto), HttpStatus.OK);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long userId) {
        return respond(userService.getUserById(userId), HttpStatus.OK);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse> deactivateUser(@PathVariable Long userId) {
        return respond(userService.deactivateUser(userId), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getUserList(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        return respond(userService.getUserList(page, size, role, status), HttpStatus.OK);
    }

    /** Maps the ApiResponse status to an HTTP code: 1 -> successCode, 2 -> 406, 3 -> 226, 4 -> 404, other -> 500. */
    private ResponseEntity<ApiResponse> respond(ApiResponse body, HttpStatus successCode) {
        HttpStatus http = switch (body.getStatus()) {
            case 1 -> successCode;
            case 2 -> HttpStatus.NOT_ACCEPTABLE;
            case 3 -> HttpStatus.IM_USED;
            case 4 -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return ResponseEntity.status(http).body(body);
    }
}
