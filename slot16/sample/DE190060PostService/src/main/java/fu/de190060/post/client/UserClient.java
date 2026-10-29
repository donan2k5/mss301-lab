package fu.de190060.post.client;

import fu.de190060.post.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Declarative HTTP client for UserService (base url comes from user.service.url). */
@FeignClient(name = "user-service", url = "${user.service.url}")
public interface UserClient {

    @GetMapping("/api/users/{userId}")
    UserResponse getUserById(@PathVariable("userId") Long userId);
}
