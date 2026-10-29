package fu.de190060.post.controller;

import fu.de190060.post.common.OnCreate;
import fu.de190060.post.dto.ApiResponse;
import fu.de190060.post.dto.PostDTO;
import fu.de190060.post.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class DE190060PostController {

    private final PostService postService;

    public DE190060PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createPost(@Validated(OnCreate.class) @RequestBody PostDTO dto) {
        return respond(postService.createPost(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse> updatePost(@PathVariable Long postId, @Valid @RequestBody PostDTO dto) {
        return respond(postService.updatePost(postId, dto), HttpStatus.OK);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse> getPostById(@PathVariable Long postId) {
        return respond(postService.getPostById(postId), HttpStatus.OK);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse> cancelPost(@PathVariable Long postId) {
        return respond(postService.cancelPost(postId), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getPostList(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String advertiserName,
            @RequestParam(required = false) String status) {
        return respond(postService.getPostList(page, size, advertiserName, status), HttpStatus.OK);
    }

    /** Maps the ApiResponse status to an HTTP code: 1 -> successCode, 2 -> 406, 4 -> 404, 5 -> 400, other -> 500. */
    private ResponseEntity<ApiResponse> respond(ApiResponse body, HttpStatus successCode) {
        HttpStatus http = switch (body.getStatus()) {
            case 1 -> successCode;
            case 2 -> HttpStatus.NOT_ACCEPTABLE;
            case 4 -> HttpStatus.NOT_FOUND;
            case 5 -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return ResponseEntity.status(http).body(body);
    }
}
