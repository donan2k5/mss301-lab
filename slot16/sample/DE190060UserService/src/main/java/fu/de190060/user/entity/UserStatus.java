package fu.de190060.user.entity;

/** Status of a user. Stored as a string (nvarchar) via @Enumerated(EnumType.STRING). */
public enum UserStatus {
    ACTIVE, SUSPENDED, DEACTIVATED
}
