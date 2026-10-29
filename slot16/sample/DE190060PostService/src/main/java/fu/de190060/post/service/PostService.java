package fu.de190060.post.service;

import fu.de190060.post.dto.ApiResponse;
import fu.de190060.post.dto.PostDTO;

public interface PostService {

    ApiResponse createPost(PostDTO dto);

    ApiResponse updatePost(Long postId, PostDTO dto);

    ApiResponse getPostById(Long postId);

    ApiResponse cancelPost(Long postId);

    ApiResponse getPostList(Integer page, Integer size, String advertiserName, String status);
}
