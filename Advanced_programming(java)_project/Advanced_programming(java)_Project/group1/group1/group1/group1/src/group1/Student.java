package group1;

import java.util.ArrayList;
import java.util.List;

public class Student extends User {
    
    private static final long serialVersionUID = 1L;
    
    private List<String> submittedAssignmentIds;
    private List<String> results;
    private String groupId;
    private List<String> notifications;
    private String program;
    private String course;
    private int semester;
    private String phoneNumber;
    private String teacherId;
    
    public Student(String id, String password, String name, String email) {
        super(id, password, name, email, "STUDENT");
        this.submittedAssignmentIds = new ArrayList<>();
        this.results = new ArrayList<>();
        this.notifications = new ArrayList<>();
        this.groupId = null;
        this.program = "General";
        this.course = "";
        this.semester = 1;
        this.phoneNumber = "";
        this.teacherId = "";
    }
    
    public List<String> getSubmittedAssignmentIds() { return submittedAssignmentIds; }
    public void setSubmittedAssignmentIds(List<String> submittedAssignmentIds) { this.submittedAssignmentIds = submittedAssignmentIds; }
    public List<String> getResults() { return results; }
    public void setResults(List<String> results) { this.results = results; }
    public List<String> getNotifications() { return notifications; }
    public void setNotifications(List<String> notifications) { this.notifications = notifications; }
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    public void addResult(String result) { results.add(result); }
    public void addSubmittedAssignment(String assignmentId) { submittedAssignmentIds.add(assignmentId); }
    public void addNotification(String notification) { notifications.add(notification); }
    public void clearNotifications() { notifications.clear(); }
}