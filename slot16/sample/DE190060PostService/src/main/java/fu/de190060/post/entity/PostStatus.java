package fu.de190060.post.entity;

/** Status of a post campaign. Stored as a string (nvarchar) via @Enumerated(EnumType.STRING). */
public enum PostStatus {
    DRAFT, SCHEDULED, PUBLISHED, CANCELLED
}
