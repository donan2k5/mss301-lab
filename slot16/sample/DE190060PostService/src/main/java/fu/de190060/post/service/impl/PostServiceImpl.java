package fu.de190060.post.service.impl;

import feign.FeignException;
import fu.de190060.post.client.UserClient;
import fu.de190060.post.common.PostConstants;
import fu.de190060.post.dto.ApiResponse;
import fu.de190060.post.dto.PageDTO;
import fu.de190060.post.dto.PostDTO;
import fu.de190060.post.dto.PostDetailDTO;
import fu.de190060.post.dto.UserDTO;
import fu.de190060.post.dto.UserResponse;
import fu.de190060.post.entity.Post;
import fu.de190060.post.entity.PostStatus;
import fu.de190060.post.repository.PostRepository;
import fu.de190060.post.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserClient userClient;

    public PostServiceImpl(PostRepository postRepository, UserClient userClient) {
        this.postRepository = postRepository;
        this.userClient = userClient;
    }

    @Override
    @Transactional
    public ApiResponse createPost(PostDTO dto) {
        // 1. Date validation (status 2 -> 406)
        LocalDate startDate = toLocalDate(dto.getStartDate());
        LocalDate endDate = toLocalDate(dto.getEndDate());
        if (!endDate.isAfter(startDate)) {
            return ApiResponse.error(2, "endDate must be after startDate");
        }
        // 2. The user must exist (status 4 -> 404)
        UserDTO user = fetchUser(dto.getUserId());
        if (user == null) {
            return ApiResponse.error(4, "User ID is not found");
        }
        // 3. The user must be ACTIVE (status 5 -> 400)
        if (!PostConstants.USER_ACTIVE.equals(user.getStatus())) {
            return ApiResponse.error(5, "User is not ACTIVE for posting");
        }
        // 4. numberOfPosts must not exceed the user's daily limit (status 2 -> 406)
        if (dto.getNumberOfPosts() > user.getMaxDailyPosts()) {
            return ApiResponse.error(2, "numberOfPosts must not exceed the user's maxDailyPosts");
        }

        Post post = new Post();
        post.setAdvertiserName(dto.getAdvertiserName());
        post.setAdvertiserEmail(dto.getAdvertiserEmail());
        post.setAdvertiserPhone(dto.getAdvertiserPhone());
        post.setStartDate(startDate);
        post.setEndDate(endDate);
        post.setNumberOfPosts(dto.getNumberOfPosts());
        post.setUserId(dto.getUserId());
        post.setTotalCost(computeTotal(user.getRatePerPost(), startDate, endDate));
        // Default status is SCHEDULED when not provided
        post.setStatus(dto.getStatus() == null ? PostStatus.SCHEDULED : PostStatus.valueOf(dto.getStatus()));
        Post saved = postRepository.save(post);
        return ApiResponse.ok("Post created successfully", toDTO(saved));
    }

    @Override
    @Transactional
    public ApiResponse updatePost(Long postId, PostDTO dto) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return ApiResponse.error(4, "Post is not found");
        }

        // Validate the EFFECTIVE values before touching the managed entity; mutating it first
        // and returning early would still let dirty-checking flush the bad values on commit.
        LocalDate start = dto.getStartDate() != null ? toLocalDate(dto.getStartDate()) : post.getStartDate();
        LocalDate end = dto.getEndDate() != null ? toLocalDate(dto.getEndDate()) : post.getEndDate();
        if (!end.isAfter(start)) {
            return ApiResponse.error(2, "endDate must be after startDate");
        }

        boolean userChanged = dto.getUserId() != null && !dto.getUserId().equals(post.getUserId());
        Long userId = dto.getUserId() != null ? dto.getUserId() : post.getUserId();
        UserDTO user = fetchUser(userId);
        if (user == null) {
            return ApiResponse.error(4, "User ID is not found");
        }
        if (userChanged && !PostConstants.USER_ACTIVE.equals(user.getStatus())) {
            return ApiResponse.error(5, "User is not ACTIVE for posting");
        }
        int numberOfPosts = dto.getNumberOfPosts() != null ? dto.getNumberOfPosts() : post.getNumberOfPosts();
        if (numberOfPosts > user.getMaxDailyPosts()) {
            return ApiResponse.error(2, "numberOfPosts must not exceed the user's maxDailyPosts");
        }

        // Partial update: only non-null fields are applied
        if (dto.getAdvertiserName() != null) post.setAdvertiserName(dto.getAdvertiserName());
        if (dto.getAdvertiserEmail() != null) post.setAdvertiserEmail(dto.getAdvertiserEmail());
        if (dto.getAdvertiserPhone() != null) post.setAdvertiserPhone(dto.getAdvertiserPhone());
        if (dto.getStatus() != null) post.setStatus(PostStatus.valueOf(dto.getStatus()));
        post.setStartDate(start);
        post.setEndDate(end);
        post.setNumberOfPosts(numberOfPosts);
        post.setUserId(userId);
        // Recalculate with the user's current rate
        post.setTotalCost(computeTotal(user.getRatePerPost(), start, end));
        Post saved = postRepository.save(post);
        return ApiResponse.ok("Post updated successfully", toDTO(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse getPostById(Long postId) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return ApiResponse.error(4, "Post is not found");
        }
        return ApiResponse.ok("Successful", toDetailDTO(post, new HashMap<>()));
    }

    @Override
    @Transactional
    public ApiResponse cancelPost(Long postId) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return ApiResponse.error(4, "Post is not found");
        }
        post.setStatus(PostStatus.CANCELLED);
        postRepository.save(post);
        return ApiResponse.ok("Post is cancelled successfully", null);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse getPostList(Integer page, Integer size, String advertiserName, String status) {
        int pageNumber = page == null ? PostConstants.DEFAULT_PAGE : page;
        int pageSize = size == null ? PostConstants.DEFAULT_SIZE : size;
        if (pageNumber < 0) {
            return ApiResponse.error(2, "page must be >= 0");
        }
        if (pageSize < 1 || pageSize > PostConstants.MAX_SIZE) {
            return ApiResponse.error(2, "size must be between 1 and " + PostConstants.MAX_SIZE);
        }
        PostStatus statusFilter = null;
        if (status != null && !status.isBlank()) {
            try {
                statusFilter = PostStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                return ApiResponse.error(2, "status filter is invalid");
            }
        }
        String nameFilter = (advertiserName == null || advertiserName.isBlank()) ? null : advertiserName;

        Page<Post> result = postRepository.search(nameFilter, statusFilter,
                PageRequest.of(pageNumber, pageSize, Sort.by("postId")));
        // Cache users per request so several posts of one user cost a single Feign call
        Map<Long, UserDTO> userCache = new HashMap<>();
        List<PostDetailDTO> content = result.getContent().stream()
                .map(p -> toDetailDTO(p, userCache)).toList();
        PageDTO pageDTO = new PageDTO(
                result.getSize(), result.getNumber(), result.getTotalPages(),
                result.getTotalElements(), result.isFirst(), result.isLast(), content);
        return ApiResponse.ok("Successful", pageDTO);
    }

    /** Calls UserService through OpenFeign; returns null when the user does not exist (HTTP 404). */
    private UserDTO fetchUser(Long userId) {
        try {
            UserResponse response = userClient.getUserById(userId);
            if (response == null || response.getStatus() != 1) {
                return null;
            }
            return response.getData();
        } catch (FeignException.NotFound ex) {
            // Any other FeignException (503, timeout...) propagates to GlobalExceptionHandler -> 500
            return null;
        }
    }

    /** totalCost = ratePerPost x number of days (endDate - startDate, at least 1). */
    private BigDecimal computeTotal(BigDecimal ratePerPost, LocalDate start, LocalDate end) {
        long days = ChronoUnit.DAYS.between(start, end);
        if (days < 1) {
            days = 1;
        }
        return ratePerPost.multiply(BigDecimal.valueOf(days));
    }

    // Request dates (dd/MM/yyyy) are parsed and written by Jackson in UTC, so convert in UTC both ways;
    // the result never depends on the JVM time zone.
    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
    }

    private Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneOffset.UTC).toInstant());
    }

    private PostDTO toDTO(Post p) {
        PostDTO d = new PostDTO();
        d.setPostId(p.getPostId());
        d.setAdvertiserName(p.getAdvertiserName());
        d.setAdvertiserEmail(p.getAdvertiserEmail());
        d.setAdvertiserPhone(p.getAdvertiserPhone());
        d.setStartDate(toDate(p.getStartDate()));
        d.setEndDate(toDate(p.getEndDate()));
        d.setNumberOfPosts(p.getNumberOfPosts());
        d.setTotalCost(p.getTotalCost());
        d.setStatus(p.getStatus().name());
        d.setUserId(p.getUserId());
        return d;
    }

    private PostDetailDTO toDetailDTO(Post p, Map<Long, UserDTO> userCache) {
        PostDetailDTO d = new PostDetailDTO();
        d.setPostId(p.getPostId());
        d.setAdvertiserName(p.getAdvertiserName());
        d.setAdvertiserEmail(p.getAdvertiserEmail());
        d.setAdvertiserPhone(p.getAdvertiserPhone());
        d.setStartDate(toDate(p.getStartDate()));
        d.setEndDate(toDate(p.getEndDate()));
        d.setNumberOfPosts(p.getNumberOfPosts());
        d.setTotalCost(p.getTotalCost());
        d.setStatus(p.getStatus().name());
        d.setUser(userCache.computeIfAbsent(p.getUserId(), this::fetchUser));
        return d;
    }
}
