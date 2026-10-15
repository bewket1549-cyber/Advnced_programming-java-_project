package group1;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Assignment implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String title;
    private String description;
    private LocalDate deadline;
    private int maxScore;
    private String type;
    private boolean allowsStudentProposal;
    private List<String> teacherProvidedTitles;
    private String guidelineFile;
    private String groupId;
    private boolean isGroupAssignment;
    private LocalDateTime createdAt;
    private List<FileAttachment> attachments;
    
    public Assignment(String id, String title, String description, LocalDate deadline,
                      int maxScore, String type, boolean allowsStudentProposal,
                      String groupId, boolean isGroupAssignment) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.maxScore = maxScore;
        this.type = type;
        this.allowsStudentProposal = allowsStudentProposal;
        this.teacherProvidedTitles = new ArrayList<>();
        this.guidelineFile = null;
        this.groupId = groupId;
        this.isGroupAssignment = isGroupAssignment;
        this.createdAt = LocalDateTime.now();
        this.attachments = new ArrayList<>();
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public int getMaxScore() { return maxScore; }
    public void setMaxScore(int maxScore) { this.maxScore = maxScore; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isAllowsStudentProposal() { return allowsStudentProposal; }
    public void setAllowsStudentProposal(boolean allowsStudentProposal) { this.allowsStudentProposal = allowsStudentProposal; }
    public List<String> getTeacherProvidedTitles() { return teacherProvidedTitles; }
    public void setTeacherProvidedTitles(List<String> teacherProvidedTitles) { this.teacherProvidedTitles = teacherProvidedTitles; }
    public void addTeacherProvidedTitle(String title) { teacherProvidedTitles.add(title); }
    public String getGuidelineFile() { return guidelineFile; }
    public void setGuidelineFile(String guidelineFile) { this.guidelineFile = guidelineFile; }
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public boolean isGroupAssignment() { return isGroupAssignment; }
    public void setGroupAssignment(boolean groupAssignment) { isGroupAssignment = groupAssignment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<FileAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<FileAttachment> attachments) { this.attachments = attachments; }
    public void addAttachment(FileAttachment fa) { attachments.add(fa); }
}