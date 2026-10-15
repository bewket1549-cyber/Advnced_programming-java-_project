package group1;

import java.awt.Desktop;
import javax.swing.*;
import java.sql.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class DataStorage {
    
    private static final String DB_URL_BASE = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC";
    private static final String DB_NAME_PREFIX = "assignment_system_";
    private static String currentCourseName = null;
    private static String currentDbName = null;
    private static final String UPLOAD_DIR = "uploads";
    
    public static final long MAX_FILE_SIZE = 200 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", 
        "jpg", "jpeg", "png", "zip", "rar", "7z"
    ));
    
    public static void setCurrentCourse(String courseName) {
        if (courseName == null || courseName.trim().isEmpty()) {
            return;
        }
        currentCourseName = courseName.trim().toLowerCase().replaceAll("[^a-z0-9]", "_");
        currentDbName = DB_NAME_PREFIX + currentCourseName;
        initializeDatabase();
    }
    
    public static String getCurrentDbName() { return currentDbName; }
    public static String getCurrentCourseName() { return currentCourseName; }
    
    public static List<String> discoverAvailableCourses() {
        List<String> availableCourses = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL_BASE + "?useSSL=false&serverTimezone=UTC", "root", "");
             ResultSet rs = conn.getMetaData().getCatalogs()) {
            while (rs.next()) {
                String dbName = rs.getString("TABLE_CAT");
                if (dbName.startsWith(DB_NAME_PREFIX)) {
                    String courseName = dbName.substring(DB_NAME_PREFIX.length());
                    availableCourses.add(courseName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return availableCourses;
    }
    
    public static Teacher getTeacherInfo(String courseName) {
        if (courseName == null || courseName.trim().isEmpty()) return null;
        String originalCourse = currentCourseName;
        setCurrentCourse(courseName);
        List<Teacher> teachers = loadTeachers();
        if (originalCourse != null) setCurrentCourse(originalCourse);
        return teachers.isEmpty() ? null : teachers.get(0);
    }
    
    private static void initializeDatabase() {
        if (currentDbName == null) return;
        try {
            try (Connection conn = DriverManager.getConnection(DB_URL_BASE + "?useSSL=false&serverTimezone=UTC", "root", "");
                 Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE DATABASE IF NOT EXISTS " + currentDbName);
            }
            Files.createDirectories(Paths.get(UPLOAD_DIR, currentCourseName));
            createTables();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
    
    private static String getDbUrl() {
        return "jdbc:mysql://localhost:3306/" + currentDbName + "?useSSL=false&serverTimezone=UTC";
    }
    
    private static Connection getConnection() throws SQLException {
        if (currentDbName == null) throw new SQLException("No database selected");
        return DriverManager.getConnection(getDbUrl(), "root", "");
    }
    
    private static void createTables() throws SQLException {
        if (currentDbName == null) return;
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS teachers (" +
                "id VARCHAR(50) PRIMARY KEY, password VARCHAR(255) NOT NULL, " +
                "name VARCHAR(100) NOT NULL, email VARCHAR(100) NOT NULL, " +
                "mustChangePassword INT DEFAULT 0, department VARCHAR(100), " +
                "course VARCHAR(100), title VARCHAR(100), phone VARCHAR(20))");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS students (" +
                "id VARCHAR(50) PRIMARY KEY, password VARCHAR(255) NOT NULL, " +
                "name VARCHAR(100) NOT NULL, email VARCHAR(100) NOT NULL, " +
                "mustChangePassword INT DEFAULT 1, program VARCHAR(100), " +
                "course VARCHAR(100), semester INT DEFAULT 1, phone VARCHAR(20), " +
                "groupId VARCHAR(50), teacherId VARCHAR(50))");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS assignments (" +
                "id VARCHAR(50) PRIMARY KEY, title VARCHAR(200), description TEXT, " +
                "deadline VARCHAR(20), maxScore INT, type VARCHAR(20), " +
                "allowsStudentProposal INT, teacherProvidedTitles TEXT, guidelineFile VARCHAR(500), " +
                "groupId VARCHAR(50), isGroupAssignment INT, createdAt VARCHAR(30))");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS assignment_attachments (" +
                "id VARCHAR(50) PRIMARY KEY, assignmentId VARCHAR(50), fileName VARCHAR(200), " +
                "storedPath VARCHAR(500), uploadTime VARCHAR(30), lastModified VARCHAR(30), isEdited INT, " +
                "FOREIGN KEY (assignmentId) REFERENCES assignments(id) ON DELETE CASCADE)");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS submissions (" +
                "id VARCHAR(50) PRIMARY KEY, assignmentId VARCHAR(50), studentId VARCHAR(50), " +
                "proposedTitle VARCHAR(200), selectedTeacherTitle VARCHAR(200), approved INT, " +
                "rejectionMessage TEXT, grade INT, feedback TEXT, submissionDate VARCHAR(30), " +
                "lastModified VARCHAR(30), isEdited INT, evaluatedDate VARCHAR(30), " +
                "UNIQUE KEY unique_submission (assignmentId, studentId))");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS submission_attachments (" +
                "id VARCHAR(50) PRIMARY KEY, submissionId VARCHAR(50), fileName VARCHAR(200), " +
                "storedPath VARCHAR(500), uploadTime VARCHAR(30), lastModified VARCHAR(30), isEdited INT, " +
                "FOREIGN KEY (submissionId) REFERENCES submissions(id) ON DELETE CASCADE)");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS groups_table (" +
                "groupId VARCHAR(50), assignmentId VARCHAR(50), groupName VARCHAR(100), createdAt VARCHAR(30), " +
                "PRIMARY KEY(groupId, assignmentId))");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS group_members (" +
                "groupId VARCHAR(50), studentId VARCHAR(50), PRIMARY KEY(groupId, studentId))");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS chats (" +
                "id VARCHAR(50) PRIMARY KEY, fromUserId VARCHAR(50), fromRole VARCHAR(20), " +
                "toUserId VARCHAR(50), toRole VARCHAR(20), toType VARCHAR(20), " +
                "message TEXT, timestamp VARCHAR(30), isRead INT, assignmentId VARCHAR(50), isAssignmentPost INT)");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS notifications (" +
                "id VARCHAR(50) PRIMARY KEY, userId VARCHAR(50), userRole VARCHAR(20), " +
                "title VARCHAR(200), message TEXT, type VARCHAR(20), timestamp VARCHAR(30), isRead INT)");
        }
    }
    
    public static void validateFile(File file) throws IllegalArgumentException {
        if (file.length() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 200MB limit");
        }
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) throw new IllegalArgumentException("File has no extension");
        String extension = fileName.substring(dotIndex + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("File type not allowed. Allowed: " + String.join(", ", ALLOWED_EXTENSIONS));
        }
    }
    
    public static String saveUploadedFile(byte[] data, String originalFileName, String subFolder) throws IOException {
        if (data.length > MAX_FILE_SIZE) throw new IOException("File size exceeds 200MB limit");
        if (currentCourseName == null) throw new IOException("No course selected");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = timestamp + "_" + originalFileName;
        Path target = Paths.get(UPLOAD_DIR, currentCourseName, subFolder, fileName);
        Files.createDirectories(target.getParent());
        Files.write(target, data);
        return target.toString();
    }
    
    public static byte[] readUploadedFile(String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
    }
    
    public static void deleteUploadedFile(String path) {
        try { 
            if (path != null) {
                Files.deleteIfExists(Paths.get(path));
            }
        } catch (IOException e) { 
            e.printStackTrace(); 
        }
    }
    
    public static void openFile(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            } else {
                JOptionPane.showMessageDialog(null, "File not found: " + path, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Cannot open file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // ========== TEACHER METHODS ==========
    
    public static void saveTeachers(List<Teacher> teachers) {
        if (currentDbName == null) return;
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) { 
                st.execute("DELETE FROM teachers"); 
            }
            String sql = "INSERT INTO teachers (id,password,name,email,mustChangePassword,department,course,title,phone) VALUES (?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (Teacher t : teachers) {
                    pstmt.setString(1, t.getId());
                    pstmt.setString(2, t.getPassword());
                    pstmt.setString(3, t.getName());
                    pstmt.setString(4, t.getEmail());
                    pstmt.setInt(5, t.isMustChangePassword() ? 1 : 0);
                    pstmt.setString(6, t.getDepartment());
                    pstmt.setString(7, t.getCourse());
                    pstmt.setString(8, t.getTitle());
                    pstmt.setString(9, t.getPhoneNumber());
                    pstmt.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }
    
    public static List<Teacher> loadTeachers() {
        List<Teacher> teachers = new ArrayList<>();
        if (currentDbName == null) return teachers;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM teachers")) {
            while (rs.next()) {
                Teacher t = new Teacher(rs.getString("id"), rs.getString("password"),
                    rs.getString("name"), rs.getString("email"), rs.getString("department"));
                t.setCourse(rs.getString("course"));
                t.setTitle(rs.getString("title"));
                t.setPhoneNumber(rs.getString("phone"));
                t.setMustChangePassword(rs.getInt("mustChangePassword") == 1);
                teachers.add(t);
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return teachers;
    }
    
    // ========== STUDENT METHODS ==========
    
    public static void saveStudents(List<Student> students) {
        if (currentDbName == null) return;
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) { 
                st.execute("DELETE FROM students"); 
            }
            String sql = "INSERT INTO students (id,password,name,email,mustChangePassword,program,course,semester,phone,groupId,teacherId) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (Student s : students) {
                    pstmt.setString(1, s.getId());
                    pstmt.setString(2, s.getPassword());
                    pstmt.setString(3, s.getName());
                    pstmt.setString(4, s.getEmail() != null ? s.getEmail() : "");
                    pstmt.setInt(5, s.isMustChangePassword() ? 1 : 0);
                    pstmt.setString(6, s.getProgram());
                    pstmt.setString(7, s.getCourse());
                    pstmt.setInt(8, s.getSemester());
                    pstmt.setString(9, s.getPhoneNumber());
                    pstmt.setString(10, s.getGroupId());
                    pstmt.setString(11, s.getTeacherId());
                    pstmt.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }
    
    public static List<Student> loadStudents() {
        List<Student> students = new ArrayList<>();
        if (currentDbName == null) return students;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM students")) {
            while (rs.next()) {
                Student s = new Student(rs.getString("id"), rs.getString("password"),
                    rs.getString("name"), rs.getString("email"));
                s.setProgram(rs.getString("program"));
                s.setCourse(rs.getString("course"));
                s.setSemester(rs.getInt("semester"));
                s.setPhoneNumber(rs.getString("phone"));
                s.setGroupId(rs.getString("groupId"));
                s.setTeacherId(rs.getString("teacherId"));
                s.setMustChangePassword(rs.getInt("mustChangePassword") == 1);
                students.add(s);
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return students;
    }
    
    // ========== ASSIGNMENT METHODS ==========
    
    public static void saveAssignments(List<Assignment> assignments) {
        if (currentDbName == null) return;
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            // Delete existing attachments first
            try (Statement st = conn.createStatement()) {
                st.execute("DELETE FROM assignment_attachments");
                st.execute("DELETE FROM assignments");
            }
            
            // Insert assignments
            String sql = "INSERT INTO assignments (id,title,description,deadline,maxScore,type,allowsStudentProposal,teacherProvidedTitles,guidelineFile,groupId,isGroupAssignment,createdAt) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (Assignment a : assignments) {
                    pstmt.setString(1, a.getId());
                    pstmt.setString(2, a.getTitle());
                    pstmt.setString(3, a.getDescription());
                    pstmt.setString(4, a.getDeadline() != null ? a.getDeadline().toString() : null);
                    pstmt.setInt(5, a.getMaxScore());
                    pstmt.setString(6, a.getType());
                    pstmt.setInt(7, a.isAllowsStudentProposal() ? 1 : 0);
                    String titles = a.getTeacherProvidedTitles() != null ? String.join(",", a.getTeacherProvidedTitles()) : null;
                    pstmt.setString(8, titles);
                    pstmt.setString(9, a.getGuidelineFile());
                    pstmt.setString(10, a.getGroupId());
                    pstmt.setInt(11, a.isGroupAssignment() ? 1 : 0);
                    pstmt.setString(12, a.getCreatedAt().toString());
                    pstmt.executeUpdate();
                    
                    // Insert attachments for this assignment
                    for (FileAttachment fa : a.getAttachments()) {
                        String attachSql = "INSERT INTO assignment_attachments (id,assignmentId,fileName,storedPath,uploadTime,lastModified,isEdited) VALUES (?,?,?,?,?,?,?)";
                        try (PreparedStatement ps = conn.prepareStatement(attachSql)) {
                            ps.setString(1, fa.getId());
                            ps.setString(2, a.getId());
                            ps.setString(3, fa.getFileName());
                            ps.setString(4, fa.getStoredPath());
                            ps.setString(5, fa.getUploadTime().toString());
                            ps.setString(6, fa.getLastModified().toString());
                            ps.setInt(7, fa.isEdited() ? 1 : 0);
                            ps.executeUpdate();
                        }
                    }
                }
            }
            conn.commit();
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }
    
    public static List<Assignment> loadAssignments() {
        List<Assignment> list = new ArrayList<>();
        if (currentDbName == null) return list;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM assignments")) {
            while (rs.next()) {
                Assignment a = new Assignment(rs.getString("id"), rs.getString("title"),
                    rs.getString("description"), rs.getString("deadline") != null ? LocalDate.parse(rs.getString("deadline")) : null,
                    rs.getInt("maxScore"), rs.getString("type"), rs.getInt("allowsStudentProposal") == 1,
                    rs.getString("groupId"), rs.getInt("isGroupAssignment") == 1);
                String titlesStr = rs.getString("teacherProvidedTitles");
                if (titlesStr != null && !titlesStr.isEmpty()) {
                    a.setTeacherProvidedTitles(new ArrayList<>(Arrays.asList(titlesStr.split(","))));
                }
                a.setGuidelineFile(rs.getString("guidelineFile"));
                a.setAttachments(new ArrayList<>());
                
                // Load attachments
                try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM assignment_attachments WHERE assignmentId=?")) {
                    ps.setString(1, a.getId());
                    ResultSet rs2 = ps.executeQuery();
                    while (rs2.next()) {
                        FileAttachment fa = new FileAttachment(rs2.getString("id"), rs2.getString("fileName"), rs2.getString("storedPath"));
                        if (rs2.getString("lastModified") != null) {
                            fa.setLastModified(LocalDateTime.parse(rs2.getString("lastModified")));
                        }
                        fa.setEdited(rs2.getInt("isEdited") == 1);
                        a.addAttachment(fa);
                    }
                }
                list.add(a);
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return list;
    }
    
    // ========== SUBMISSION METHODS ==========
    
    public static void saveSubmissions(List<Submission> submissions) {
        if (currentDbName == null) return;
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            // Delete existing attachments
            try (Statement st = conn.createStatement()) {
                st.execute("DELETE FROM submission_attachments");
            }
            
            String sql = "INSERT INTO submissions (id,assignmentId,studentId,proposedTitle,selectedTeacherTitle,approved,rejectionMessage,grade,feedback,submissionDate,lastModified,isEdited,evaluatedDate) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "proposedTitle=VALUES(proposedTitle), selectedTeacherTitle=VALUES(selectedTeacherTitle), approved=VALUES(approved), " +
                        "rejectionMessage=VALUES(rejectionMessage), grade=VALUES(grade), feedback=VALUES(feedback), lastModified=VALUES(lastModified), " +
                        "isEdited=VALUES(isEdited), evaluatedDate=VALUES(evaluatedDate)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (Submission s : submissions) {
                    pstmt.setString(1, s.getId());
                    pstmt.setString(2, s.getAssignmentId());
                    pstmt.setString(3, s.getStudentId());
                    pstmt.setString(4, s.getProposedTitle());
                    pstmt.setString(5, s.getSelectedTeacherTitle());
                    pstmt.setObject(6, s.getApproved() == null ? null : (s.getApproved() ? 1 : 0));
                    pstmt.setString(7, s.getRejectionMessage());
                    pstmt.setObject(8, s.getGrade());
                    pstmt.setString(9, s.getFeedback());
                    pstmt.setString(10, s.getSubmissionDate().toString());
                    pstmt.setString(11, s.getLastModified().toString());
                    pstmt.setInt(12, s.isEdited() ? 1 : 0);
                    pstmt.setString(13, s.getEvaluatedDate() != null ? s.getEvaluatedDate().toString() : null);
                    pstmt.executeUpdate();
                    
                    // Insert attachments for this submission
                    for (FileAttachment fa : s.getAttachments()) {
                        String attachSql = "INSERT INTO submission_attachments (id,submissionId,fileName,storedPath,uploadTime,lastModified,isEdited) VALUES (?,?,?,?,?,?,?)";
                        try (PreparedStatement ps = conn.prepareStatement(attachSql)) {
                            ps.setString(1, fa.getId());
                            ps.setString(2, s.getId());
                            ps.setString(3, fa.getFileName());
                            ps.setString(4, fa.getStoredPath());
                            ps.setString(5, fa.getUploadTime().toString());
                            ps.setString(6, fa.getLastModified().toString());
                            ps.setInt(7, fa.isEdited() ? 1 : 0);
                            ps.executeUpdate();
                        }
                    }
                }
            }
            conn.commit();
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }
    
    public static List<Submission> loadSubmissions() {
        List<Submission> list = new ArrayList<>();
        if (currentDbName == null) return list;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM submissions")) {
            while (rs.next()) {
                Submission s = new Submission(rs.getString("id"), rs.getString("assignmentId"),
                    rs.getString("studentId"), rs.getString("proposedTitle"),
                    LocalDateTime.parse(rs.getString("submissionDate")));
                s.setSelectedTeacherTitle(rs.getString("selectedTeacherTitle"));
                Integer grade = rs.getObject("grade") != null ? rs.getInt("grade") : null;
                s.setGrade(grade);
                s.setFeedback(rs.getString("feedback"));
                Boolean approved = rs.getObject("approved") == null ? null : rs.getInt("approved") == 1;
                s.setApproved(approved);
                s.setRejectionMessage(rs.getString("rejectionMessage"));
                s.setLastModified(LocalDateTime.parse(rs.getString("lastModified")));
                s.setEdited(rs.getInt("isEdited") == 1);
                if (rs.getString("evaluatedDate") != null) {
                    s.setEvaluatedDate(LocalDateTime.parse(rs.getString("evaluatedDate")));
                }
                s.setAttachments(new ArrayList<>());
                
                // Load attachments
                try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM submission_attachments WHERE submissionId=?")) {
                    ps.setString(1, s.getId());
                    ResultSet rs2 = ps.executeQuery();
                    while (rs2.next()) {
                        FileAttachment fa = new FileAttachment(rs2.getString("id"), rs2.getString("fileName"), rs2.getString("storedPath"));
                        if (rs2.getString("lastModified") != null) {
                            fa.setLastModified(LocalDateTime.parse(rs2.getString("lastModified")));
                        }
                        fa.setEdited(rs2.getInt("isEdited") == 1);
                        s.addAttachment(fa);
                    }
                }
                list.add(s);
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return list;
    }
    
    // ========== GROUP METHODS ==========
    
    public static void saveGroups(List<Group> groups) {
        if (currentDbName == null) return;
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) {
                st.execute("DELETE FROM groups_table");
                st.execute("DELETE FROM group_members");
            }
            String sql = "INSERT INTO groups_table (groupId,assignmentId,groupName,createdAt) VALUES (?,?,?,?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (Group g : groups) {
                    pstmt.setString(1, g.getGroupId());
                    String assignId = g.getAssignmentId();
                    pstmt.setString(2, assignId == null || assignId.isEmpty() ? "NO_ASSIGNMENT" : assignId);
                    pstmt.setString(3, g.getGroupName());
                    pstmt.setString(4, g.getCreatedAt().toString());
                    pstmt.executeUpdate();
                    for (String sid : g.getStudentIds()) {
                        String mem = "INSERT INTO group_members (groupId,studentId) VALUES (?,?)";
                        try (PreparedStatement ps = conn.prepareStatement(mem)) {
                            ps.setString(1, g.getGroupId());
                            ps.setString(2, sid);
                            ps.executeUpdate();
                        }
                    }
                }
            }
            conn.commit();
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }
    
    public static List<Group> loadGroups() {
        List<Group> groups = new ArrayList<>();
        if (currentDbName == null) return groups;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM groups_table")) {
            while (rs.next()) {
                String assignId = rs.getString("assignmentId");
                if ("NO_ASSIGNMENT".equals(assignId)) assignId = null;
                Group g = new Group(rs.getString("groupId"), assignId, rs.getString("groupName"));
                try (PreparedStatement ps = conn.prepareStatement("SELECT studentId FROM group_members WHERE groupId=?")) {
                    ps.setString(1, g.getGroupId());
                    ResultSet rs2 = ps.executeQuery();
                    while (rs2.next()) g.addStudent(rs2.getString("studentId"));
                }
                groups.add(g);
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return groups;
    }
    
    // ========== CHAT METHODS ==========
    
    public static void saveChats(List<ChatMessage> chats) {
        if (currentDbName == null) return;
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) { 
                st.execute("DELETE FROM chats"); 
            }
            String sql = "INSERT INTO chats (id,fromUserId,fromRole,toUserId,toRole,toType,message,timestamp,isRead,assignmentId,isAssignmentPost) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (ChatMessage c : chats) {
                    pstmt.setString(1, c.getId());
                    pstmt.setString(2, c.getFromUserId());
                    pstmt.setString(3, c.getFromRole());
                    pstmt.setString(4, c.getToUserId());
                    pstmt.setString(5, c.getToRole());
                    pstmt.setString(6, c.getToType());
                    pstmt.setString(7, c.getMessage());
                    pstmt.setString(8, c.getTimestamp().toString());
                    pstmt.setInt(9, c.isRead() ? 1 : 0);
                    pstmt.setString(10, c.getAssignmentId());
                    pstmt.setInt(11, c.isAssignmentPost() ? 1 : 0);
                    pstmt.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }
    
    public static List<ChatMessage> loadChats() {
        List<ChatMessage> list = new ArrayList<>();
        if (currentDbName == null) return list;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM chats ORDER BY timestamp ASC")) {
            while (rs.next()) {
                ChatMessage c = new ChatMessage(rs.getString("id"), rs.getString("fromUserId"),
                    rs.getString("fromRole"), rs.getString("toUserId"), rs.getString("toRole"),
                    rs.getString("toType"), rs.getString("message"));
                c.setAssignmentId(rs.getString("assignmentId"));
                c.setAssignmentPost(rs.getInt("isAssignmentPost") == 1);
                list.add(c);
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return list;
    }
    
    // ========== NOTIFICATION METHODS ==========
    
    public static void saveNotifications(List<Notification> notifications) {
        if (currentDbName == null) return;
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) { 
                st.execute("DELETE FROM notifications"); 
            }
            String sql = "INSERT INTO notifications (id,userId,userRole,title,message,type,timestamp,isRead) VALUES (?,?,?,?,?,?,?,?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (Notification n : notifications) {
                    pstmt.setString(1, n.getId());
                    pstmt.setString(2, n.getUserId());
                    pstmt.setString(3, n.getUserRole());
                    pstmt.setString(4, n.getTitle());
                    pstmt.setString(5, n.getMessage());
                    pstmt.setString(6, n.getType());
                    pstmt.setString(7, n.getTimestamp().toString());
                    pstmt.setInt(8, n.isRead() ? 1 : 0);
                    pstmt.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }
    
    public static List<Notification> loadNotifications() {
        List<Notification> list = new ArrayList<>();
        if (currentDbName == null) return list;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM notifications")) {
            while (rs.next()) {
                Notification n = new Notification(rs.getString("id"), rs.getString("userId"),
                    rs.getString("userRole"), rs.getString("title"), rs.getString("message"), rs.getString("type"));
                list.add(n);
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return list;
    }
    
    public static String exportGradesToCSV(List<Submission> submissions, List<Assignment> assignments, List<Student> students) {
        StringBuilder csv = new StringBuilder();
        csv.append("Student ID,Student Name,Course,Assignment Title,Grade,Max Score,Percentage,Feedback,Evaluation Date\n");
        for (Submission s : submissions) {
            if (s.getGrade() != null) {
                Student student = students.stream().filter(st -> st.getId().equals(s.getStudentId())).findFirst().orElse(null);
                Assignment assignment = assignments.stream().filter(a -> a.getId().equals(s.getAssignmentId())).findFirst().orElse(null);
                if (student != null && assignment != null) {
                    double percentage = (double) s.getGrade() / assignment.getMaxScore() * 100;
                    csv.append(String.format("%s,%s,%s,%s,%d,%d,%.1f%%,%s,%s\n",
                        student.getId(), student.getName(), student.getCourse(), assignment.getTitle(),
                        s.getGrade(), assignment.getMaxScore(), percentage,
                        s.getFeedback().replace(",", ";"),
                        s.getEvaluatedDate() != null ? s.getEvaluatedDate().toString() : ""));
                }
            }
        }
        return csv.toString();
    }
}