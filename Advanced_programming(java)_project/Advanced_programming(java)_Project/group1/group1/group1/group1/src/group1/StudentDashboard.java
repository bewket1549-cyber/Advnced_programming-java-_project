package group1;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.Timer;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class StudentDashboard extends JFrame {
    
    private Student student;
    private List<Student> students;
    private List<Teacher> teachers;
    private List<Assignment> assignments;
    private List<Submission> submissions;
    private List<ChatMessage> chats;
    private List<Notification> notifications;
    private List<Group> groups;
    
    private StudentDashboardUI ui;
    private Timer notificationTimer;
    private Timer refreshTimer;
    
    private final Color primaryColor = new Color(33, 150, 243);
    private final Color successColor = new Color(76, 175, 80);
    private final Color dangerColor = new Color(244, 67, 54);
    private final Color infoColor = new Color(0, 188, 212);
    private final Color bgColor = new Color(245, 245, 250);
    
    public StudentDashboard(Student student, List<Student> students, List<Teacher> teachers) {
        this.student = student;
        this.students = students;
        this.teachers = teachers;
        this.ui = new StudentDashboardUI(this);
        
        if (student.getCourse() != null && !student.getCourse().isEmpty()) {
            DataStorage.setCurrentCourse(student.getCourse());
        } else if (!teachers.isEmpty()) {
            DataStorage.setCurrentCourse(teachers.get(0).getCourse());
        }
        
        loadData();
        initFrame();
        initUI();
        startAutoRefresh();
        startNotificationChecker();
        
        addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                stopAutoRefresh();
                stopNotificationChecker();
                saveData();
            }
        });
        
        setVisible(true);
    }
    
    private void loadData() {
        try {
            assignments = DataStorage.loadAssignments();
            submissions = DataStorage.loadSubmissions();
            chats = DataStorage.loadChats();
            notifications = DataStorage.loadNotifications();
            groups = DataStorage.loadGroups();
            students = DataStorage.loadStudents();
        } catch (Exception e) {
            assignments = new ArrayList<>();
            submissions = new ArrayList<>();
            chats = new ArrayList<>();
            notifications = new ArrayList<>();
            groups = new ArrayList<>();
        }
    }
    
    private void saveData() {
        try {
            DataStorage.saveAssignments(assignments);
            DataStorage.saveSubmissions(submissions);
            DataStorage.saveChats(chats);
            DataStorage.saveNotifications(notifications);
            DataStorage.saveGroups(groups);
            DataStorage.saveStudents(students);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void initFrame() {
        setTitle("Student Dashboard - " + student.getName() + " (" + student.getCourse() + ")");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1350, 900);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1100, 700));
    }
    
    private void initUI() {
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPane.setBackground(bgColor);
        setContentPane(contentPane);
        
        JTabbedPane tabs = new JTabbedPane();
        ui.initUI(contentPane, tabs, student);
    }
    
    public void refreshAll() {
        loadData();
        ui.refreshAssignmentsTable(assignments);
        ui.refreshSubmissionsTable(submissions, assignments, student);
        ui.refreshResultsTable(submissions, assignments, student);
        ui.refreshChatMessages(chats, student, teachers);
        ui.updateNotificationBadge(notifications, student);
    }
    
    public void showSubmissionDialog(int row) {
        if (row < 0) { showError("Select an assignment"); return; }
        
        Assignment selected = assignments.get(row);
        
        if (selected.getDeadline().isBefore(LocalDate.now())) {
            showError("Deadline has passed");
            return;
        }
        
        Submission existing = submissions.stream()
            .filter(s -> s.getAssignmentId().equals(selected.getId()) && s.getStudentId().equals(student.getId()))
            .findFirst().orElse(null);
        
        Teacher teacher = teachers.isEmpty() ? null : teachers.get(0);
        
        if ("Project".equals(selected.getType())) {
            boolean hasTeacherTitles = selected.getTeacherProvidedTitles() != null && !selected.getTeacherProvidedTitles().isEmpty();
            boolean allowsProposal = selected.isAllowsStudentProposal();
            
            if (hasTeacherTitles) {
                String[] titles = selected.getTeacherProvidedTitles().toArray(new String[0]);
                String selectedTitle = (String) JOptionPane.showInputDialog(this, 
                    "Select a title for your project:", "Project Title",
                    JOptionPane.QUESTION_MESSAGE, null, titles, titles[0]);
                
                if (selectedTitle == null) return;
                
                if (existing == null) {
                    existing = new Submission(generateShortId(), selected.getId(), student.getId(), null, LocalDateTime.now());
                    submissions.add(existing);
                }
                existing.setSelectedTeacherTitle(selectedTitle);
                existing.setLastModified(LocalDateTime.now());
                existing.setEdited(true);
                
                ChatMessage cm = new ChatMessage(generateShortId(), student.getId(), "STUDENT",
                    teacher.getId(), "TEACHER", "INDIVIDUAL",
                    "Student " + student.getName() + " (" + student.getId() + ") selected title: '" + selectedTitle + "'");
                chats.add(cm);
                saveData();
                ui.refreshChatMessages(chats, student, teachers);
                
            } else if (allowsProposal) {
                String proposedTitle = JOptionPane.showInputDialog(this, "Propose your project title:", "Title Proposal", JOptionPane.QUESTION_MESSAGE);
                if (proposedTitle == null || proposedTitle.trim().isEmpty()) return;
                
                if (existing == null) {
                    existing = new Submission(generateShortId(), selected.getId(), student.getId(), proposedTitle.trim(), LocalDateTime.now());
                    submissions.add(existing);
                } else {
                    existing.setProposedTitle(proposedTitle.trim());
                    existing.setApproved(null);
                    existing.setLastModified(LocalDateTime.now());
                    existing.setEdited(true);
                }
                
                ChatMessage cm = new ChatMessage(generateShortId(), student.getId(), "STUDENT",
                    teacher.getId(), "TEACHER", "INDIVIDUAL",
                    "Proposal: '" + proposedTitle + "' from " + student.getName() + " (" + student.getId() + ")");
                chats.add(cm);
                saveData();
                ui.refreshChatMessages(chats, student, teachers);
                showInfo("Proposal submitted! Waiting for teacher approval.");
            } else {
                showError("No title options available");
                return;
            }
        }
        
        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(true);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            for (File file : fc.getSelectedFiles()) {
                try {
                    DataStorage.validateFile(file);
                    byte[] data = Files.readAllBytes(file.toPath());
                    String stored = DataStorage.saveUploadedFile(data, file.getName(), "submissions");
                    FileAttachment fa = new FileAttachment(generateShortId(), file.getName(), stored);
                    
                    if (existing == null) {
                        existing = new Submission(generateShortId(), selected.getId(), student.getId(), null, LocalDateTime.now());
                        submissions.add(existing);
                    }
                    
                    existing.addAttachment(fa);
                    existing.setLastModified(LocalDateTime.now());
                    existing.setEdited(true);
                    showInfo("File added: " + file.getName());
                } catch (Exception ex) {
                    showError("Upload failed: " + ex.getMessage());
                }
            }
            saveData();
            refreshAll();
            showInfo("Submission saved");
        }
    }
    
    public void openAssignmentFile(String assignmentTitle) {
        Assignment a = assignments.stream().filter(ass -> ass.getTitle().equals(assignmentTitle)).findFirst().orElse(null);
        if (a == null) return;
        
        List<String> options = new ArrayList<>();
        Map<String, String> pathMap = new HashMap<>();
        
        if (a.getGuidelineFile() != null && !a.getGuidelineFile().isEmpty()) {
            options.add("Guideline File");
            pathMap.put("Guideline File", a.getGuidelineFile());
        }
        for (FileAttachment fa : a.getAttachments()) {
            options.add(fa.getFileName());
            pathMap.put(fa.getFileName(), fa.getStoredPath());
        }
        if (a.getTeacherProvidedTitles() != null && !a.getTeacherProvidedTitles().isEmpty()) {
            options.add("Teacher Provided Titles");
        }
        
        if (options.isEmpty()) { showInfo("No files available"); return; }
        
        String selected = (String) JOptionPane.showInputDialog(this, "Select item:", "Files", 
            JOptionPane.QUESTION_MESSAGE, null, options.toArray(), options.get(0));
        if (selected == null) return;
        
        if (selected.equals("Teacher Provided Titles")) {
            StringBuilder sb = new StringBuilder("Available Titles:\n\n");
            for (String title : a.getTeacherProvidedTitles()) {
                sb.append("- ").append(title).append("\n");
            }
            JTextArea ta = new JTextArea(sb.toString());
            ta.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Titles", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String path = pathMap.get(selected);
        String[] actions = {"Open", "Download"};
        String action = (String) JOptionPane.showInputDialog(this, "Action for " + selected, "Action", 
            JOptionPane.QUESTION_MESSAGE, null, actions, actions[0]);
        if (action == null) return;
        
        if (action.equals("Open")) {
            DataStorage.openFile(path);
        } else if (action.equals("Download")) {
            downloadFileFromPath(path, selected);
        }
    }
    
    public void openSubmissionFile(String assignmentTitle) {
        Submission sub = submissions.stream()
            .filter(s -> {
                Assignment a = findAssignmentById(s.getAssignmentId());
                return a != null && a.getTitle().equals(assignmentTitle) && s.getStudentId().equals(student.getId());
            })
            .findFirst().orElse(null);
        
        if (sub == null) { showInfo("No submission found"); return; }
        if (sub.getAttachments().isEmpty()) { showInfo("No files attached"); return; }
        
        String[] options = sub.getAttachments().stream().map(FileAttachment::getFileName).toArray(String[]::new);
        String selected = (String) JOptionPane.showInputDialog(this, "Select file:", "Files", 
            JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (selected == null) return;
        
        String[] actions = {"Open", "Download", "Replace", "Delete"};
        String action = (String) JOptionPane.showInputDialog(this, "Action for " + selected, "Action", 
            JOptionPane.QUESTION_MESSAGE, null, actions, actions[0]);
        if (action == null) return;
        
        FileAttachment fa = sub.getAttachments().stream().filter(f -> f.getFileName().equals(selected)).findFirst().orElse(null);
        if (fa != null) {
            if (action.equals("Open")) {
                DataStorage.openFile(fa.getStoredPath());
            } else if (action.equals("Download")) {
                downloadFileFromPath(fa.getStoredPath(), selected);
            } else if (action.equals("Replace")) {
                JFileChooser fc = new JFileChooser();
                if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        File newFile = fc.getSelectedFile();
                        DataStorage.validateFile(newFile);
                        byte[] data = Files.readAllBytes(newFile.toPath());
                        String newPath = DataStorage.saveUploadedFile(data, newFile.getName(), "submissions");
                        DataStorage.deleteUploadedFile(fa.getStoredPath());
                        fa.setFileName(newFile.getName());
                        fa.setStoredPath(newPath);
                        fa.setLastModified(LocalDateTime.now());
                        fa.setEdited(true);
                        saveData();
                        refreshAll();
                        showInfo("File replaced: " + newFile.getName());
                    } catch (Exception ex) {
                        showError("Replace failed: " + ex.getMessage());
                    }
                }
            } else if (action.equals("Delete")) {
                int confirm = JOptionPane.showConfirmDialog(this, "Delete " + selected + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    DataStorage.deleteUploadedFile(fa.getStoredPath());
                    sub.getAttachments().remove(fa);
                    saveData();
                    refreshAll();
                    showInfo("File deleted: " + selected);
                }
            }
        }
    }
    
    private void downloadFileFromPath(String path, String name) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(name));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Files.copy(Paths.get(path), fc.getSelectedFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
                showInfo("Downloaded: " + name);
            } catch (IOException ex) {
                showError("Download failed: " + ex.getMessage());
            }
        }
    }
    
    public void sendChatMessage(String message) {
        if (message == null || message.trim().isEmpty()) return;
        
        Teacher teacher = teachers.isEmpty() ? null : teachers.get(0);
        if (teacher == null) { showError("No teacher available"); return; }
        
        ChatMessage cm = new ChatMessage(generateShortId(), student.getId(), "STUDENT", 
            teacher.getId(), "TEACHER", "INDIVIDUAL", message);
        chats.add(cm);
        saveData();
        ui.refreshChatMessages(chats, student, teachers);
        showInfo("Message sent to teacher");
    }
    
    public void changePassword() {
        JPasswordField np = new JPasswordField();
        JPasswordField cf = new JPasswordField();
        JCheckBox showPass = new JCheckBox("Show Password");
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        p.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        p.add(np, gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        p.add(new JLabel("Confirm:"), gbc);
        gbc.gridx = 1;
        p.add(cf, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        p.add(showPass, gbc);
        
        showPass.addActionListener(e -> {
            char c = showPass.isSelected() ? 0 : '*';
            np.setEchoChar(c);
            cf.setEchoChar(c);
        });
        
        JTextArea req = new JTextArea(AuthService.getPasswordRequirements());
        req.setEditable(false);
        req.setBackground(new Color(255, 243, 224));
        req.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        gbc.gridy = 3;
        p.add(new JScrollPane(req), gbc);
        
        if (JOptionPane.showConfirmDialog(this, p, "Change Password", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String p1 = new String(np.getPassword());
            String p2 = new String(cf.getPassword());
            if (!p1.equals(p2)) {
                showError("Passwords don't match");
                return;
            }
            try {
                AuthService.changePassword(student, p1);
                student.setMustChangePassword(false);
                for (int i = 0; i < students.size(); i++) {
                    if (students.get(i).getId().equals(student.getId())) {
                        students.set(i, student);
                        break;
                    }
                }
                saveData();
                showInfo("Password changed successfully! Please login again.");
                dispose();
                new MainApp();
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        }
    }
    
    public void exportData() {
        StringBuilder sb = new StringBuilder();
        sb.append("Student: ").append(student.getName()).append("\n");
        sb.append("ID: ").append(student.getId()).append("\n");
        sb.append("Course: ").append(student.getCourse()).append("\n");
        sb.append("Export: ").append(LocalDateTime.now()).append("\n\n");
        sb.append("Submissions:\n");
        submissions.stream().filter(s -> s.getStudentId().equals(student.getId())).forEach(s -> {
            Assignment a = findAssignmentById(s.getAssignmentId());
            sb.append("- ").append(a != null ? a.getTitle() : "Unknown").append(": ").append(s.getSubmissionDate()).append("\n");
        });
        sb.append("\nGrades:\n");
        submissions.stream().filter(s -> s.getStudentId().equals(student.getId()) && s.getGrade() != null).forEach(s -> {
            Assignment a = findAssignmentById(s.getAssignmentId());
            if (a != null) sb.append("- ").append(a.getTitle()).append(": ").append(s.getGrade()).append("/").append(a.getMaxScore()).append("\n");
        });
        
        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Export", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void showNotificationsDialog() {
        JDialog d = new JDialog(this, "Notifications", true);
        d.setSize(450, 500);
        d.setLocationRelativeTo(this);
        
        DefaultListModel<String> model = new DefaultListModel<>();
        notifications.stream().filter(n -> n.getUserId().equals(student.getId())).forEach(n -> {
            model.addElement(n.getTitle() + "\n" + n.getMessage() + "\n" + n.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            n.setRead(true);
        });
        
        JList<String> list = new JList<>(model);
        d.add(new JScrollPane(list), BorderLayout.CENTER);
        
        JButton close = new JButton("Close");
        close.addActionListener(e -> d.dispose());
        d.add(close, BorderLayout.SOUTH);
        
        d.setVisible(true);
        saveData();
        ui.updateNotificationBadge(notifications, student);
    }
    
    public void logout() {
        if (JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            saveData();
            dispose();
            new MainApp();
        }
    }
    
    public Student getStudent() { return student; }
    
    private String generateShortId() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private Assignment findAssignmentById(String id) {
        return assignments.stream().filter(a -> a.getId().equals(id)).findFirst().orElse(null);
    }
    
    private void startAutoRefresh() {
        refreshTimer = new Timer(15000, e -> refreshAll());
        refreshTimer.start();
    }
    
    private void stopAutoRefresh() {
        if (refreshTimer != null) refreshTimer.stop();
    }
    
    private void startNotificationChecker() {
        notificationTimer = new Timer(10000, e -> ui.updateNotificationBadge(notifications, student));
        notificationTimer.start();
    }
    
    private void stopNotificationChecker() {
        if (notificationTimer != null) notificationTimer.stop();
    }
    
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
}