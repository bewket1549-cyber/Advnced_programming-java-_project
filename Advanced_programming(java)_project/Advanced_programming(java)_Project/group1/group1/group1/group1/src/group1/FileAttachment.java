package group1;

import java.io.Serializable;
import java.time.LocalDateTime;

public class FileAttachment implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String fileName;
    private String storedPath;
    private LocalDateTime uploadTime;
    private LocalDateTime lastModified;
    private boolean isEdited;
    
    public FileAttachment(String id, String fileName, String storedPath) {
        this.id = id;
        this.fileName = fileName;
        this.storedPath = storedPath;
        this.uploadTime = LocalDateTime.now();
        this.lastModified = this.uploadTime;
        this.isEdited = false;
    }
    
    public String getId() { return id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getStoredPath() { return storedPath; }
    public void setStoredPath(String storedPath) { this.storedPath = storedPath; }
    public LocalDateTime getUploadTime() { return uploadTime; }
    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
    public boolean isEdited() { return isEdited; }
    public void setEdited(boolean edited) { isEdited = edited; }
}