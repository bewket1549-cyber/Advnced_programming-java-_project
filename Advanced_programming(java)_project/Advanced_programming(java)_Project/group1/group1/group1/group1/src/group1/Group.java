package group1;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Group implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String groupId;
    private String assignmentId;
    private String groupName;
    private List<String> studentIds;
    private LocalDateTime createdAt;
    
    public Group(String groupId, String assignmentId, String groupName) {
        this.groupId = groupId;
        this.assignmentId = assignmentId;
        this.groupName = groupName;
        this.studentIds = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }
    
    public String getGroupId() { return groupId; }
    public String getAssignmentId() { return assignmentId; }
    public void setAssignmentId(String assignmentId) { this.assignmentId = assignmentId; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public List<String> getStudentIds() { return studentIds; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean addStudent(String studentId) {
        if (!studentIds.contains(studentId)) {
            studentIds.add(studentId);
            return true;
        }
        return false;
    }
    public void removeStudent(String studentId) { studentIds.remove(studentId); }
    public boolean hasStudent(String studentId) { return studentIds.contains(studentId); }
    public int getSize() { return studentIds.size(); }
}