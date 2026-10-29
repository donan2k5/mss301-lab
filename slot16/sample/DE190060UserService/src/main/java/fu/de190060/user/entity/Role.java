package fu.de190060.user.entity;

/** Role of a user. Stored as a string (nvarchar) via @Enumerated(EnumType.STRING). */
public enum Role {
    USER, CREATOR, ADMIN
}
