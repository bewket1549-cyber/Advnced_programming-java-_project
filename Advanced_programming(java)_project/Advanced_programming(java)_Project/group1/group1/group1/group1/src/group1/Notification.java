package group1;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Notification implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String userId;
    private String userRole;
    private String title;
    private String message;
    private String type;
    private LocalDateTime timestamp;
    private boolean isRead;
    
    public Notification(String id, String userId, String userRole, String title, String message, String type) {
        this.id = id;
        this.userId = userId;
        this.userRole = userRole;
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
    }
    
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getUserRole() { return userRole; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}