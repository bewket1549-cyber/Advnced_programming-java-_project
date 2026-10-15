package group1;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ChatMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String fromUserId;
    private String fromRole;
    private String toUserId;
    private String toRole;
    private String toType;
    private String message;
    private LocalDateTime timestamp;
    private boolean isRead;
    private String assignmentId;
    private boolean isAssignmentPost;
    
    public ChatMessage(String id, String fromUserId, String fromRole, String toUserId, String toRole, String toType, String message) {
        this.id = id;
        this.fromUserId = fromUserId;
        this.fromRole = fromRole;
        this.toUserId = toUserId;
        this.toRole = toRole;
        this.toType = toType;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
        this.assignmentId = null;
        this.isAssignmentPost = false;
    }
    
    public String getId() { return id; }
    public String getFromUserId() { return fromUserId; }
    public String getFromRole() { return fromRole; }
    public String getToUserId() { return toUserId; }
    public String getToRole() { return toRole; }
    public String getToType() { return toType; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public String getAssignmentId() { return assignmentId; }
    public void setAssignmentId(String assignmentId) { this.assignmentId = assignmentId; }
    public boolean isAssignmentPost() { return isAssignmentPost; }
    public void setAssignmentPost(boolean assignmentPost) { isAssignmentPost = assignmentPost; }
}