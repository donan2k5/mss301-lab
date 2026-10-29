package fu.de190060.user.common;

public final class UserConstants {

    private UserConstants() {
    }

    public static final String ROLE_REGEX = "USER|CREATOR|ADMIN";
    public static final String STATUS_REGEX = "ACTIVE|SUSPENDED|DEACTIVATED";

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_SIZE = 100;
}
