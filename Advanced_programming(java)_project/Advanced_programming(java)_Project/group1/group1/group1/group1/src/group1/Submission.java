package group1;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Submission implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String assignmentId;
    private String studentId;
    private List<FileAttachment> attachments;
    private String proposedTitle;
    private String selectedTeacherTitle;
    private Boolean approved;
    private String rejectionMessage;
    private Integer grade;
    private String feedback;
    private LocalDateTime submissionDate;
    private LocalDateTime lastModified;
    private boolean isEdited;
    private LocalDateTime evaluatedDate;
    
    public Submission(String id, String assignmentId, String studentId, String proposedTitle, LocalDateTime submissionDate) {
        this.id = id;
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.proposedTitle = proposedTitle;
        this.selectedTeacherTitle = null;
        this.submissionDate = submissionDate;
        this.attachments = new ArrayList<>();
        this.approved = null;
        this.rejectionMessage = null;
        this.grade = null;
        this.feedback = "";
        this.lastModified = submissionDate;
        this.isEdited = false;
        this.evaluatedDate = null;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAssignmentId() { return assignmentId; }
    public void setAssignmentId(String assignmentId) { this.assignmentId = assignmentId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public List<FileAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<FileAttachment> attachments) { this.attachments = attachments; }
    public void addAttachment(FileAttachment fa) { attachments.add(fa); }
    public void removeAttachment(String fileId) { attachments.removeIf(a -> a.getId().equals(fileId)); }
    public String getProposedTitle() { return proposedTitle; }
    public void setProposedTitle(String proposedTitle) { this.proposedTitle = proposedTitle; }
    public String getSelectedTeacherTitle() { return selectedTeacherTitle; }
    public void setSelectedTeacherTitle(String selectedTeacherTitle) { this.selectedTeacherTitle = selectedTeacherTitle; }
    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }
    public String getRejectionMessage() { return rejectionMessage; }
    public void setRejectionMessage(String rejectionMessage) { this.rejectionMessage = rejectionMessage; }
    public Integer getGrade() { return grade; }
    public void setGrade(Integer grade) { this.grade = grade; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public LocalDateTime getSubmissionDate() { return submissionDate; }
    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
    public boolean isEdited() { return isEdited; }
    public void setEdited(boolean edited) { isEdited = edited; }
    public LocalDateTime getEvaluatedDate() { return evaluatedDate; }
    public void setEvaluatedDate(LocalDateTime evaluatedDate) { this.evaluatedDate = evaluatedDate; }
}