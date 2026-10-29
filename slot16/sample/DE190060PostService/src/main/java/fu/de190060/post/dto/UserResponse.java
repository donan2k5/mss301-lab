package fu.de190060.post.dto;

/**
 * Typed view of the UserService ApiResponse (same fields, but data is a UserDTO)
 * so the Feign decoder can map the JSON without any extra conversion.
 */
public class UserResponse {

    private int status;
    private String message;
    private UserDTO data;
    private String timestamp;

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public UserDTO getData() { return data; }
    public void setData(UserDTO data) { this.data = data; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
