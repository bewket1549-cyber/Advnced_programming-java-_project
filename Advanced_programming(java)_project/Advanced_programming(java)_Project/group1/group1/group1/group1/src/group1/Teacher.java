package group1;

import java.util.ArrayList;
import java.util.List;

public class Teacher extends User {
    
    private static final long serialVersionUID = 1L;
    
    private List<String> managedAssignmentIds;
    private String department;
    private String course;
    private String title;
    private String phoneNumber;
    
    public Teacher(String id, String password, String name, String email, String department) {
        super(id, password, name, email, "TEACHER");
        this.managedAssignmentIds = new ArrayList<>();
        this.department = department;
        this.course = "";
        this.title = "Faculty Member";
        this.phoneNumber = "";
    }
    
    public List<String> getManagedAssignmentIds() { return managedAssignmentIds; }
    public void setManagedAssignmentIds(List<String> managedAssignmentIds) { this.managedAssignmentIds = managedAssignmentIds; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void addAssignmentId(String assignmentId) { managedAssignmentIds.add(assignmentId); }
}