package fu.de190060.post.common;

public final class PostConstants {

    private PostConstants() {
    }

    public static final String STATUS_REGEX = "DRAFT|SCHEDULED|PUBLISHED|CANCELLED";

    // User status coming from UserService
    public static final String USER_ACTIVE = "ACTIVE";

    public static final String DATE_PATTERN = "dd/MM/yyyy";

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_SIZE = 100;
}
