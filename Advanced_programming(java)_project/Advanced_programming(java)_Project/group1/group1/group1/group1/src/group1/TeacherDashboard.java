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

public class TeacherDashboard extends JFrame {
    
    private Teacher teacher;
    private List<Student> students;
    private List<Teacher> teachers;
    private List<Assignment> assignments;
    private List<Submission> submissions;
    private List<Group> groups;
    private List<ChatMessage> chats;
    private List<Notification> notifications;
    
    private JPanel contentPane;
    private JPanel chatMessagesPanel;
    private JScrollPane chatScrollPane;
    private JTextField chatMessageField;
    private JTable studentsTable;
    private JTable assignmentsTable;
    private JTable submissionsTable;
    private JTable groupsTable;
    private DefaultTableModel studentsTableModel;
    private DefaultTableModel assignmentsTableModel;
    private DefaultTableModel submissionsTableModel;
    private DefaultTableModel groupsTableModel;
    private JLabel notificationBadge;
    
    private Timer notificationTimer;
    private Timer refreshTimer;
    
    private final Color primaryColor = new Color(33, 150, 243);
    private final Color successColor = new Color(76, 175, 80);
    private final Color dangerColor = new Color(244, 67, 54);
    private final Color infoColor = new Color(0, 188, 212);
    private final Color bgColor = new Color(245, 245, 250);
    private final Color fieldBg = Color.WHITE;
    private final Color headerBg = new Color(33, 150, 243);
    
    public TeacherDashboard(Teacher teacher, List<Student> students, List<Teacher> teachers) {
        this.teacher = teacher;
        this.students = students;
        this.teachers = teachers;
        
        if (teacher.getCourse() != null && !teacher.getCourse().isEmpty()) {
            DataStorage.setCurrentCourse(teacher.getCourse());
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
            groups = DataStorage.loadGroups();
            chats = DataStorage.loadChats();
            notifications = DataStorage.loadNotifications();
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
            DataStorage.saveGroups(groups);
            DataStorage.saveChats(chats);
            DataStorage.saveNotifications(notifications);
            DataStorage.saveStudents(students);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void initFrame() {
        setTitle("Teacher Dashboard - " + teacher.getName() + " (" + teacher.getCourse() + ")");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1200, 700));
    }
    
    private void initUI() {
        contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPane.setBackground(bgColor);
        setContentPane(contentPane);
        
        JPanel headerPanel = createHeaderPanel();
        contentPane.add(headerPanel, BorderLayout.NORTH);
        
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        tabs.addTab("Students", createStudentsPanel());
        tabs.addTab("Groups", createGroupsPanel());
        tabs.addTab("Assignments", createAssignmentsPanel());
        tabs.addTab("Submissions", createSubmissionsPanel());
        tabs.addTab("Chat", createChatPanel());
        tabs.addTab("Settings", createSettingsPanel());
        
        contentPane.add(tabs, BorderLayout.CENTER);
        
        refreshStudentsTable();
        refreshGroupsTable();
        refreshAssignmentsTable();
        refreshSubmissionsTable();
        refreshChatMessages();
        refreshNotificationsList();
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(headerBg);
        header.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        JLabel welcomeLabel = new JLabel("Teacher Dashboard - " + teacher.getName() + " (" + teacher.getCourse() + ")");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        
        JButton refreshBtn = createStyledButton("Refresh All", successColor);
        refreshBtn.addActionListener(e -> refreshAll());
        
        notificationBadge = new JLabel("Notifications");
        notificationBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        notificationBadge.setForeground(Color.WHITE);
        notificationBadge.setCursor(new Cursor(Cursor.HAND_CURSOR));
        notificationBadge.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { showNotificationsDialog(); }
        });
        
        JButton logoutBtn = createStyledButton("Logout", new Color(220, 70, 60));
        logoutBtn.addActionListener(e -> logout());
        
        rightPanel.add(refreshBtn);
        rightPanel.add(notificationBadge);
        rightPanel.add(logoutBtn);
        
        header.add(welcomeLabel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(bgColor);
        
        String[] cols = {"ID", "Name", "Email", "Course", "Group", "Submissions", "Status"};
        studentsTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        studentsTable = new JTable(studentsTableModel);
        styleTable(studentsTable);
        
        JScrollPane sp = new JScrollPane(studentsTable);
        sp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(primaryColor, 2),
            "Student Management", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), primaryColor));
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBackground(bgColor);
        
        JButton addBtn = createStyledButton("Add Student", successColor);
        addBtn.addActionListener(e -> showAddStudentDialog());
        
        JButton editBtn = createStyledButton("Edit Student", infoColor);
        editBtn.addActionListener(e -> editStudent());
        
        JButton deleteBtn = createStyledButton("Delete Student", dangerColor);
        deleteBtn.addActionListener(e -> deleteStudent());
        
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        
        panel.add(sp, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createGroupsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(bgColor);
        
        String[] cols = {"Group ID", "Group Name", "Assignment", "Members", "Created"};
        groupsTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        groupsTable = new JTable(groupsTableModel);
        styleTable(groupsTable);
        
        JScrollPane sp = new JScrollPane(groupsTable);
        sp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(infoColor, 2),
            "Group Management", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), infoColor));
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBackground(bgColor);
        
        JButton createBtn = createStyledButton("Create Group", successColor);
        createBtn.addActionListener(e -> showCreateGroupDialog());
        
        JButton assignAssignmentBtn = createStyledButton("Assign Assignment to Group", infoColor);
        assignAssignmentBtn.addActionListener(e -> assignAssignmentToGroup());
        
        JButton deleteBtn = createStyledButton("Delete Group", dangerColor);
        deleteBtn.addActionListener(e -> deleteGroup());
        
        btnPanel.add(createBtn);
        btnPanel.add(assignAssignmentBtn);
        btnPanel.add(deleteBtn);
        
        panel.add(sp, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createAssignmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(bgColor);
        
        String[] cols = {"Title", "Type", "Deadline", "Max Score", "Proposals", "Status", "Files"};
        assignmentsTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        assignmentsTable = new JTable(assignmentsTableModel);
        styleTable(assignmentsTable);
        
        assignmentsTable.getColumn("Files").setCellRenderer(new FileButtonRenderer());
        assignmentsTable.getColumn("Files").setCellEditor(new FileButtonEditor(new JCheckBox(), true));
        
        JScrollPane sp = new JScrollPane(assignmentsTable);
        sp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(successColor, 2),
            "Assignment Management", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), successColor));
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBackground(bgColor);
        
        JButton postBtn = createStyledButton("Post Assignment", successColor);
        postBtn.addActionListener(e -> showPostAssignmentDialog());
        
        JButton assignGroupBtn = createStyledButton("Assign Group to Assignment", infoColor);
        assignGroupBtn.addActionListener(e -> assignGroupToAssignment());
        
        JButton editBtn = createStyledButton("Edit Assignment", infoColor);
        editBtn.addActionListener(e -> editAssignment());
        
        JButton deleteBtn = createStyledButton("Delete Assignment", dangerColor);
        deleteBtn.addActionListener(e -> deleteAssignment());
        
        btnPanel.add(postBtn);
        btnPanel.add(assignGroupBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        
        panel.add(sp, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createSubmissionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(bgColor);
        
        String[] cols = {"Student", "Assignment", "Title/Proposal", "Submission Date", "Status", "Grade", "Files"};
        submissionsTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        submissionsTable = new JTable(submissionsTableModel);
        styleTable(submissionsTable);
        
        submissionsTable.getColumn("Files").setCellRenderer(new FileButtonRenderer());
        submissionsTable.getColumn("Files").setCellEditor(new FileButtonEditor(new JCheckBox(), false));
        
        JScrollPane sp = new JScrollPane(submissionsTable);
        sp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(255, 152, 0), 2),
            "Submission Evaluation", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), new Color(255, 152, 0)));
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBackground(bgColor);
        
        JButton evaluateBtn = createStyledButton("Evaluate", infoColor);
        evaluateBtn.addActionListener(e -> evaluateSubmission());
        
        JButton respondBtn = createStyledButton("Respond to Proposal", successColor);
        respondBtn.addActionListener(e -> respondToProposal());
        
        JButton exportBtn = createStyledButton("Export Grades", new Color(156, 39, 176));
        exportBtn.addActionListener(e -> exportGradesToCSV());
        
        btnPanel.add(evaluateBtn);
        btnPanel.add(respondBtn);
        btnPanel.add(exportBtn);
        
        panel.add(sp, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(bgColor);
        
        JPanel chatHeader = new JPanel(new BorderLayout());
        chatHeader.setBorder(new EmptyBorder(5, 10, 5, 10));
        chatHeader.setBackground(primaryColor);
        
        JLabel chatTitle = new JLabel("Chat with Students");
        chatTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chatTitle.setForeground(Color.WHITE);
        chatHeader.add(chatTitle, BorderLayout.WEST);
        
        chatMessagesPanel = new JPanel();
        chatMessagesPanel.setLayout(new BoxLayout(chatMessagesPanel, BoxLayout.Y_AXIS));
        chatMessagesPanel.setBackground(new Color(240, 248, 255));
        
        chatScrollPane = new JScrollPane(chatMessagesPanel);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        chatScrollPane.getViewport().setBackground(new Color(240, 248, 255));
        
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        inputPanel.setBackground(bgColor);
        
        chatMessageField = new JTextField();
        chatMessageField.setBackground(fieldBg);
        chatMessageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JButton sendBtn = createStyledButton("Send to All", successColor);
        sendBtn.addActionListener(e -> {
            String msg = chatMessageField.getText().trim();
            if (!msg.isEmpty()) {
                sendBroadcastMessage(msg);
                chatMessageField.setText("");
            }
        });
        
        inputPanel.add(chatMessageField, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);
        
        panel.add(chatHeader, BorderLayout.NORTH);
        panel.add(chatScrollPane, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        panel.setBackground(bgColor);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JButton changePass = createStyledButton("Change Password", primaryColor);
        changePass.addActionListener(e -> changeTeacherPassword());
        
        JButton exportBtn = createStyledButton("Export Data", infoColor);
        exportBtn.addActionListener(e -> exportData());
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(changePass, gbc);
        gbc.gridy = 1;
        panel.add(exportBtn, gbc);
        
        return panel;
    }
    
    // ========== STUDENT MANAGEMENT ==========
    
    public void showAddStudentDialog() {
        JDialog dialog = new JDialog(this, "Add Student", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setBackground(bgColor);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField idField = new JTextField(15);
        JTextField nameField = new JTextField(15);
        JTextField emailField = new JTextField(15);
        JTextField programField = new JTextField(15);
        JSpinner semesterSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        JTextField phoneField = new JTextField(15);
        
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Student ID:*"), gbc);
        gbc.gridx = 1;
        dialog.add(idField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Full Name:*"), gbc);
        gbc.gridx = 1;
        dialog.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        dialog.add(emailField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Program:"), gbc);
        gbc.gridx = 1;
        dialog.add(programField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        dialog.add(new JLabel("Semester:"), gbc);
        gbc.gridx = 1;
        dialog.add(semesterSpinner, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        dialog.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        dialog.add(phoneField, gbc);
        
        JButton saveBtn = new JButton("Save");
        saveBtn.setBackground(successColor);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveBtn.addActionListener(e -> {
            String id = idField.getText().trim();
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Student ID required");
                return;
            }
            if (students.stream().anyMatch(s -> s.getId().equals(id))) {
                JOptionPane.showMessageDialog(dialog, "Student ID already exists");
                return;
            }
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name required");
                return;
            }
            
            Student s = new Student(id, "temp123", name, emailField.getText().trim());
            s.setProgram(programField.getText().trim());
            s.setSemester((int) semesterSpinner.getValue());
            s.setPhoneNumber(phoneField.getText().trim());
            s.setCourse(teacher.getCourse());
            s.setTeacherId(teacher.getId());
            s.setMustChangePassword(true);
            students.add(s);
            saveData();
            refreshStudentsTable();
            dialog.dispose();
            showInfo("Student added successfully");
        });
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        dialog.add(saveBtn, gbc);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    public void editStudent() {
        int row = studentsTable.getSelectedRow();
        if (row < 0) {
            showError("Select a student first");
            return;
        }
        
        String sid = (String) studentsTableModel.getValueAt(row, 0);
        Student s = students.stream().filter(st -> st.getId().equals(sid)).findFirst().orElse(null);
        if (s == null) return;
        
        JDialog dialog = new JDialog(this, "Edit Student", true);
        dialog.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField nameField = new JTextField(s.getName(), 15);
        JTextField emailField = new JTextField(s.getEmail(), 15);
        JTextField programField = new JTextField(s.getProgram(), 15);
        JSpinner semesterSpinner = new JSpinner(new SpinnerNumberModel(s.getSemester(), 1, 12, 1));
        JTextField phoneField = new JTextField(s.getPhoneNumber(), 15);
        
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Full Name:*"), gbc);
        gbc.gridx = 1;
        dialog.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        dialog.add(emailField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Program:"), gbc);
        gbc.gridx = 1;
        dialog.add(programField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Semester:"), gbc);
        gbc.gridx = 1;
        dialog.add(semesterSpinner, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        dialog.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        dialog.add(phoneField, gbc);
        
        JButton saveBtn = new JButton("Save");
        saveBtn.setBackground(successColor);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            s.setName(nameField.getText().trim());
            s.setEmail(emailField.getText().trim());
            s.setProgram(programField.getText().trim());
            s.setSemester((int) semesterSpinner.getValue());
            s.setPhoneNumber(phoneField.getText().trim());
            saveData();
            refreshStudentsTable();
            dialog.dispose();
            showInfo("Student updated");
        });
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        dialog.add(saveBtn, gbc);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    public void deleteStudent() {
        int row = studentsTable.getSelectedRow();
        if (row < 0) {
            showError("Select a student");
            return;
        }
        
        String sid = (String) studentsTableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete student " + sid + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            submissions.removeIf(s -> s.getStudentId().equals(sid));
            students.removeIf(s -> s.getId().equals(sid));
            groups.forEach(g -> g.removeStudent(sid));
            saveData();
            refreshStudentsTable();
            refreshGroupsTable();
            refreshSubmissionsTable();
            showInfo("Student deleted");
        }
    }
    
    // ========== GROUP MANAGEMENT ==========
    
    public void showCreateGroupDialog() {
        String groupName = JOptionPane.showInputDialog(this, "Group Name:");
        if (groupName == null || groupName.trim().isEmpty()) return;
        
        List<Student> available = students.stream()
            .filter(s -> groups.stream().noneMatch(g -> g.hasStudent(s.getId())))
            .collect(Collectors.toList());
        
        if (available.isEmpty()) {
            showError("No available students");
            return;
        }
        
        JList<Student> studentList = new JList<>(available.toArray(new Student[0]));
        studentList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        studentList.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Student s = (Student) value;
                return super.getListCellRendererComponent(list, s.getName() + " (" + s.getId() + ")", index, isSelected, cellHasFocus);
            }
        });
        
        int result = JOptionPane.showConfirmDialog(this, new JScrollPane(studentList), "Select Students", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String gid = generateShortId();
            Group newGroup = new Group(gid, null, groupName);
            for (Student s : studentList.getSelectedValuesList()) {
                newGroup.addStudent(s.getId());
                s.setGroupId(gid);
            }
            groups.add(newGroup);
            saveData();
            refreshGroupsTable();
            refreshStudentsTable();
            
            for (Student s : studentList.getSelectedValuesList()) {
                String msg = "Group Arrangement: You have been added to group '" + groupName + "' (ID: " + gid + ")";
                ChatMessage cm = new ChatMessage(generateShortId(), teacher.getId(), "TEACHER", s.getId(), "STUDENT", "INDIVIDUAL", msg);
                chats.add(cm);
            }
            saveData();
            refreshChatMessages();
            showInfo("Group created");
        }
    }
    
    public void assignAssignmentToGroup() {
        int row = groupsTable.getSelectedRow();
        if (row < 0) {
            showError("Select a group first");
            return;
        }
        
        String gidPrefix = (String) groupsTableModel.getValueAt(row, 0);
        Group group = groups.stream().filter(g -> g.getGroupId().startsWith(gidPrefix)).findFirst().orElse(null);
        if (group == null) return;
        
        if (assignments.isEmpty()) {
            showError("No assignments available");
            return;
        }
        
        String[] assignTitles = assignments.stream().map(Assignment::getTitle).toArray(String[]::new);
        String selected = (String) JOptionPane.showInputDialog(this, 
            "Select assignment to assign to group " + group.getGroupName(),
            "Assign Assignment", JOptionPane.QUESTION_MESSAGE, null, assignTitles, assignTitles[0]);
        
        if (selected != null) {
            Assignment a = assignments.stream().filter(ass -> ass.getTitle().equals(selected)).findFirst().orElse(null);
            if (a != null) {
                group.setAssignmentId(a.getId());
                saveData();
                refreshGroupsTable();
                showInfo("Assignment '" + selected + "' assigned to group '" + group.getGroupName() + "'");
            }
        }
    }
    
    public void deleteGroup() {
        int row = groupsTable.getSelectedRow();
        if (row < 0) {
            showError("Select a group");
            return;
        }
        
        String gidPrefix = (String) groupsTableModel.getValueAt(row, 0);
        Group g = groups.stream().filter(gr -> gr.getGroupId().startsWith(gidPrefix)).findFirst().orElse(null);
        if (g == null) return;
        
        int confirm = JOptionPane.showConfirmDialog(this, "Delete group " + g.getGroupName() + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            for (String sid : g.getStudentIds()) {
                Student s = students.stream().filter(st -> st.getId().equals(sid)).findFirst().orElse(null);
                if (s != null) s.setGroupId(null);
            }
            groups.remove(g);
            saveData();
            refreshGroupsTable();
            refreshStudentsTable();
            showInfo("Group deleted");
        }
    }
    
    // ========== ASSIGNMENT MANAGEMENT ==========
    
    public void showPostAssignmentDialog() {
        JDialog d = new JDialog(this, "Post Assignment", true);
        d.setSize(650, 800);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());
        d.getContentPane().setBackground(bgColor);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(bgColor);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int rowY = 0;
        
        // Title
        gbc.gridx = 0; gbc.gridy = rowY; gbc.weightx = 0;
        panel.add(new JLabel("Title:*"), gbc);
        JTextField titleField = new JTextField();
        titleField.setPreferredSize(new Dimension(400, 35));
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(titleField, gbc);
        rowY++;
        
        // Description
        gbc.gridx = 0; gbc.gridy = rowY;
        panel.add(new JLabel("Description:"), gbc);
        JTextArea descArea = new JTextArea(3, 20);
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(400, 80));
        gbc.gridx = 1;
        panel.add(descScroll, gbc);
        rowY++;
        
        // Type
        gbc.gridx = 0; gbc.gridy = rowY;
        panel.add(new JLabel("Type:*"), gbc);
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Assignment", "Project"});
        typeCombo.setPreferredSize(new Dimension(400, 35));
        gbc.gridx = 1;
        panel.add(typeCombo, gbc);
        rowY++;
        
        // Max Score
        gbc.gridx = 0; gbc.gridy = rowY;
        panel.add(new JLabel("Max Score:*"), gbc);
        JSpinner scoreSpinner = new JSpinner(new SpinnerNumberModel(100, 1, 1000, 1));
        scoreSpinner.setPreferredSize(new Dimension(400, 35));
        gbc.gridx = 1;
        panel.add(scoreSpinner, gbc);
        rowY++;
        
        // Deadline
        gbc.gridx = 0; gbc.gridy = rowY;
        panel.add(new JLabel("Deadline:*"), gbc);
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor de = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(de);
        dateSpinner.setValue(java.sql.Date.valueOf(LocalDate.now().plusWeeks(2)));
        dateSpinner.setPreferredSize(new Dimension(400, 35));
        gbc.gridx = 1;
        panel.add(dateSpinner, gbc);
        rowY++;
        
        // Allow Proposal (for Projects)
        JCheckBox proposalCheck = new JCheckBox("Allow Student Title Proposals");
        proposalCheck.setVisible(false);
        typeCombo.addActionListener(ev -> proposalCheck.setVisible("Project".equals(typeCombo.getSelectedItem())));
        gbc.gridx = 0; gbc.gridy = rowY; gbc.gridwidth = 2;
        panel.add(proposalCheck, gbc);
        rowY++;
        
        // Teacher Provided Titles (for Projects)
        JPanel titlesPanel = new JPanel(new BorderLayout());
        titlesPanel.setBorder(BorderFactory.createTitledBorder("Teacher Provided Titles (Optional)"));
        titlesPanel.setVisible(false);
        typeCombo.addActionListener(ev -> titlesPanel.setVisible("Project".equals(typeCombo.getSelectedItem())));
        
        DefaultListModel<String> titlesModel = new DefaultListModel<>();
        JList<String> titlesList = new JList<>(titlesModel);
        JScrollPane titlesScroll = new JScrollPane(titlesList);
        titlesScroll.setPreferredSize(new Dimension(400, 100));
        
        JPanel titlesBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addTitleBtn = new JButton("Add Title");
        JButton removeTitleBtn = new JButton("Remove Title");
        titlesBtnPanel.add(addTitleBtn);
        titlesBtnPanel.add(removeTitleBtn);
        
        titlesPanel.add(titlesScroll, BorderLayout.CENTER);
        titlesPanel.add(titlesBtnPanel, BorderLayout.SOUTH);
        
        List<String> teacherTitles = new ArrayList<>();
        
        addTitleBtn.addActionListener(ev -> {
            String newTitle = JOptionPane.showInputDialog(d, "Enter title option:");
            if (newTitle != null && !newTitle.trim().isEmpty()) {
                teacherTitles.add(newTitle.trim());
                titlesModel.addElement(newTitle.trim());
            }
        });
        
        removeTitleBtn.addActionListener(ev -> {
            int idx = titlesList.getSelectedIndex();
            if (idx >= 0) {
                teacherTitles.remove(idx);
                titlesModel.remove(idx);
            }
        });
        
        gbc.gridx = 0; gbc.gridy = rowY; gbc.gridwidth = 2;
        panel.add(titlesPanel, gbc);
        rowY++;
        
        // Guideline File (for Projects)
        JPanel guidelinePanel = new JPanel(new BorderLayout());
        guidelinePanel.setBorder(BorderFactory.createTitledBorder("Guideline File (Optional)"));
        guidelinePanel.setVisible(false);
        typeCombo.addActionListener(ev -> guidelinePanel.setVisible("Project".equals(typeCombo.getSelectedItem())));
        
        JTextArea guidelineTextArea = new JTextArea(2, 40);
        guidelineTextArea.setEditable(false);
        guidelineTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        guidelineTextArea.setBackground(new Color(255, 255, 200));
        JScrollPane guidelineScroll = new JScrollPane(guidelineTextArea);
        
        JPanel guidelineBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton uploadGuidelineBtn = new JButton("Upload");
        JButton replaceGuidelineBtn = new JButton("Replace");
        JButton removeGuidelineBtn = new JButton("Remove");
        JButton openGuidelineBtn = new JButton("Open");
        JButton downloadGuidelineBtn = new JButton("Download");
        guidelineBtnPanel.add(uploadGuidelineBtn);
        guidelineBtnPanel.add(replaceGuidelineBtn);
        guidelineBtnPanel.add(removeGuidelineBtn);
        guidelineBtnPanel.add(openGuidelineBtn);
        guidelineBtnPanel.add(downloadGuidelineBtn);
        
        guidelinePanel.add(guidelineScroll, BorderLayout.CENTER);
        guidelinePanel.add(guidelineBtnPanel, BorderLayout.SOUTH);
        
        String[] guidelineFileRef = {null};
        
        uploadGuidelineBtn.addActionListener(ev -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(d) == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = fc.getSelectedFile();
                    DataStorage.validateFile(file);
                    byte[] data = Files.readAllBytes(file.toPath());
                    String stored = DataStorage.saveUploadedFile(data, file.getName(), "guidelines");
                    guidelineFileRef[0] = stored;
                    guidelineTextArea.setText(file.getName());
                    showInfo("Guideline file uploaded");
                } catch (Exception ex) {
                    showError("Upload failed: " + ex.getMessage());
                }
            }
        });
        
        replaceGuidelineBtn.addActionListener(ev -> {
            if (guidelineFileRef[0] == null) {
                showError("No guideline file to replace");
                return;
            }
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(d) == JFileChooser.APPROVE_OPTION) {
                try {
                    File newFile = fc.getSelectedFile();
                    DataStorage.validateFile(newFile);
                    byte[] data = Files.readAllBytes(newFile.toPath());
                    String newPath = DataStorage.saveUploadedFile(data, newFile.getName(), "guidelines");
                    DataStorage.deleteUploadedFile(guidelineFileRef[0]);
                    guidelineFileRef[0] = newPath;
                    guidelineTextArea.setText(newFile.getName());
                    showInfo("Guideline file replaced");
                } catch (Exception ex) {
                    showError("Replace failed: " + ex.getMessage());
                }
            }
        });
        
        removeGuidelineBtn.addActionListener(ev -> {
            if (guidelineFileRef[0] != null) {
                DataStorage.deleteUploadedFile(guidelineFileRef[0]);
                guidelineFileRef[0] = null;
                guidelineTextArea.setText("No file selected");
                showInfo("Guideline file removed");
            }
        });
        
        openGuidelineBtn.addActionListener(ev -> {
            if (guidelineFileRef[0] != null) {
                DataStorage.openFile(guidelineFileRef[0]);
            } else {
                showError("No guideline file to open");
            }
        });
        
        downloadGuidelineBtn.addActionListener(ev -> {
            if (guidelineFileRef[0] != null) {
                String name = new File(guidelineFileRef[0]).getName();
                JFileChooser fc = new JFileChooser();
                fc.setSelectedFile(new File(name));
                if (fc.showSaveDialog(d) == JFileChooser.APPROVE_OPTION) {
                    try {
                        Files.copy(Paths.get(guidelineFileRef[0]), fc.getSelectedFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
                        showInfo("Downloaded");
                    } catch (IOException ex) {
                        showError("Download failed");
                    }
                }
            } else {
                showError("No guideline file to download");
            }
        });
        
        gbc.gridx = 0; gbc.gridy = rowY; gbc.gridwidth = 2;
        panel.add(guidelinePanel, gbc);
        rowY++;
        
        // Attachments
        JPanel attachPanel = new JPanel(new BorderLayout());
        attachPanel.setBorder(BorderFactory.createTitledBorder("Attachments (Optional)"));
        
        JTextArea attachTextArea = new JTextArea(4, 40);
        attachTextArea.setEditable(false);
        attachTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        attachTextArea.setBackground(new Color(255, 255, 200));
        JScrollPane attachScroll = new JScrollPane(attachTextArea);
        attachScroll.setPreferredSize(new Dimension(400, 100));
        
        JPanel attachBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addAttachBtn = new JButton("Add Files");
        JButton clearAttachBtn = new JButton("Clear All");
        attachBtnPanel.add(addAttachBtn);
        attachBtnPanel.add(clearAttachBtn);
        
        attachPanel.add(attachScroll, BorderLayout.CENTER);
        attachPanel.add(attachBtnPanel, BorderLayout.SOUTH);
        
        List<FileAttachment> tempAttachments = new ArrayList<>();
        
        addAttachBtn.addActionListener(ev -> {
            JFileChooser fc = new JFileChooser();
            fc.setMultiSelectionEnabled(true);
            if (fc.showOpenDialog(d) == JFileChooser.APPROVE_OPTION) {
                StringBuilder sb = new StringBuilder(attachTextArea.getText());
                for (File file : fc.getSelectedFiles()) {
                    try {
                        DataStorage.validateFile(file);
                        byte[] data = Files.readAllBytes(file.toPath());
                        String stored = DataStorage.saveUploadedFile(data, file.getName(), "assignments");
                        FileAttachment fa = new FileAttachment(generateShortId(), file.getName(), stored);
                        tempAttachments.add(fa);
                        if (sb.length() > 0) sb.append("\n");
                        sb.append("- ").append(file.getName());
                    } catch (Exception ex) {
                        showError("Upload failed: " + ex.getMessage());
                    }
                }
                attachTextArea.setText(sb.toString());
                showInfo(tempAttachments.size() + " file(s) added");
            }
        });
        
        clearAttachBtn.addActionListener(ev -> {
            for (FileAttachment fa : tempAttachments) {
                DataStorage.deleteUploadedFile(fa.getStoredPath());
            }
            tempAttachments.clear();
            attachTextArea.setText("");
            showInfo("All attachments cleared");
        });
        
        gbc.gridx = 0; gbc.gridy = rowY; gbc.gridwidth = 2;
        panel.add(attachPanel, gbc);
        rowY++;
        
        // Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        JButton postBtn = new JButton("Post");
        postBtn.setBackground(successColor);
        postBtn.setForeground(Color.WHITE);
        postBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        postBtn.setPreferredSize(new Dimension(150, 40));
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(dangerColor);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelBtn.setPreferredSize(new Dimension(150, 40));
        
        bottomPanel.add(postBtn);
        bottomPanel.add(cancelBtn);
        
        postBtn.addActionListener(e -> {
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                showError("Title required");
                return;
            }
            
            String type = (String) typeCombo.getSelectedItem();
            java.util.Date utilDate = (java.util.Date) dateSpinner.getValue();
            LocalDate deadline = new java.sql.Date(utilDate.getTime()).toLocalDate();
            
            Assignment a = new Assignment(generateShortId(), title, descArea.getText(), deadline,
                (int) scoreSpinner.getValue(), type, proposalCheck.isSelected(), null, false);
            
            if ("Project".equals(type)) {
                a.setTeacherProvidedTitles(new ArrayList<>(teacherTitles));
                a.setGuidelineFile(guidelineFileRef[0]);
            }
            a.setAttachments(new ArrayList<>(tempAttachments));
            assignments.add(a);
            
            String msg = "NEW " + type + ": " + title + "\nDeadline: " + deadline + "\nMax Score: " + scoreSpinner.getValue();
            ChatMessage cm = new ChatMessage(generateShortId(), teacher.getId(), "TEACHER", "ALL", "STUDENT", "ALL", msg);
            cm.setAssignmentPost(true);
            chats.add(cm);
            
            saveData();
            refreshAssignmentsTable();
            refreshChatMessages();
            showInfo("Assignment posted");
            d.dispose();
        });
        
        cancelBtn.addActionListener(e -> d.dispose());
        
        d.add(panel, BorderLayout.CENTER);
        d.add(bottomPanel, BorderLayout.SOUTH);
        d.setVisible(true);
    }
    
    public void assignGroupToAssignment() {
        int row = assignmentsTable.getSelectedRow();
        if (row < 0) {
            showError("Select an assignment first");
            return;
        }
        
        String title = (String) assignmentsTableModel.getValueAt(row, 0);
        Assignment a = assignments.stream().filter(ass -> ass.getTitle().equals(title)).findFirst().orElse(null);
        if (a == null) return;
        
        if (groups.isEmpty()) {
            showError("No groups available");
            return;
        }
        
        String[] groupNames = groups.stream().map(Group::getGroupName).toArray(String[]::new);
        String selected = (String) JOptionPane.showInputDialog(this, 
            "Select group for assignment " + a.getTitle(),
            "Assign Group", JOptionPane.QUESTION_MESSAGE, null, groupNames, groupNames[0]);
        
        if (selected != null) {
            Group g = groups.stream().filter(gr -> gr.getGroupName().equals(selected)).findFirst().orElse(null);
            if (g != null) {
                g.setAssignmentId(a.getId());
                saveData();
                refreshGroupsTable();
                showInfo("Group '" + selected + "' assigned to assignment '" + a.getTitle() + "'");
            }
        }
    }
    
    public void editAssignment() {
        int row = assignmentsTable.getSelectedRow();
        if (row < 0) {
            showError("Select an assignment");
            return;
        }
        
        Assignment a = assignments.get(row);
        if (a.getDeadline().isBefore(LocalDate.now())) {
            showError("Cannot edit after deadline");
            return;
        }
        
        String newTitle = JOptionPane.showInputDialog(this, "New Title:", a.getTitle());
        if (newTitle != null && !newTitle.trim().isEmpty()) {
            a.setTitle(newTitle.trim());
            saveData();
            refreshAssignmentsTable();
            showInfo("Assignment updated");
        }
    }
    
    public void deleteAssignment() {
        int row = assignmentsTable.getSelectedRow();
        if (row < 0) {
            showError("Select an assignment");
            return;
        }
        
        Assignment a = assignments.get(row);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete " + a.getTitle() + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            for (FileAttachment fa : a.getAttachments()) {
                DataStorage.deleteUploadedFile(fa.getStoredPath());
            }
            if (a.getGuidelineFile() != null) {
                DataStorage.deleteUploadedFile(a.getGuidelineFile());
            }
            assignments.remove(a);
            submissions.removeIf(s -> s.getAssignmentId().equals(a.getId()));
            saveData();
            refreshAssignmentsTable();
            refreshSubmissionsTable();
            showInfo("Assignment deleted");
        }
    }
    
    // ========== SUBMISSION EVALUATION ==========
    
    public void evaluateSubmission() {
        int row = submissionsTable.getSelectedRow();
        if (row < 0) {
            showError("Select a submission");
            return;
        }
        
        String studentInfo = (String) submissionsTableModel.getValueAt(row, 0);
        String studentId = studentInfo.split(" \\(")[1].replace(")", "");
        String assignmentTitle = (String) submissionsTableModel.getValueAt(row, 1);
        
        Submission sub = submissions.stream()
            .filter(s -> s.getStudentId().equals(studentId) && 
                   findAssignmentById(s.getAssignmentId()).getTitle().equals(assignmentTitle) &&
                   s.getGrade() == null)
            .findFirst().orElse(null);
        
        if (sub == null) {
            showError("Already evaluated or not found");
            return;
        }
        
        Assignment a = findAssignmentById(sub.getAssignmentId());
        Student stu = findStudentById(studentId);
        
        if (a == null || stu == null) return;
        
        JSpinner gradeSpinner = new JSpinner(new SpinnerNumberModel(0, 0, a.getMaxScore(), 1));
        JTextArea feedbackArea = new JTextArea(4, 30);
        JCheckBox notifyCheck = new JCheckBox("Notify student", true);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Grade (0-" + a.getMaxScore() + "):"), gbc);
        gbc.gridx = 1;
        panel.add(gradeSpinner, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Feedback:"), gbc);
        gbc.gridx = 1;
        panel.add(new JScrollPane(feedbackArea), gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(notifyCheck, gbc);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Evaluate Submission", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int grade = (int) gradeSpinner.getValue();
            String feedback = feedbackArea.getText().trim();
            if (feedback.isEmpty()) feedback = "No feedback";
            
            sub.setGrade(grade);
            sub.setFeedback(feedback);
            sub.setEvaluatedDate(LocalDateTime.now());
            saveData();
            refreshSubmissionsTable();
            
            if (notifyCheck.isSelected()) {
                String msg = "Your submission for '" + a.getTitle() + "' has been graded!\nGrade: " + grade + "/" + a.getMaxScore() + "\nFeedback: " + feedback;
                ChatMessage cm = new ChatMessage(generateShortId(), teacher.getId(), "TEACHER", stu.getId(), "STUDENT", "INDIVIDUAL", msg);
                chats.add(cm);
                saveData();
                refreshChatMessages();
            }
            showInfo("Grade submitted");
        }
    }
    
    public void respondToProposal() {
        int row = submissionsTable.getSelectedRow();
        if (row < 0) {
            showError("Select a submission");
            return;
        }
        
        String studentInfo = (String) submissionsTableModel.getValueAt(row, 0);
        String studentId = studentInfo.split(" \\(")[1].replace(")", "");
        String assignmentTitle = (String) submissionsTableModel.getValueAt(row, 1);
        
        Submission sub = submissions.stream()
            .filter(s -> s.getStudentId().equals(studentId) && 
                   findAssignmentById(s.getAssignmentId()).getTitle().equals(assignmentTitle) &&
                   s.getProposedTitle() != null && s.getApproved() == null)
            .findFirst().orElse(null);
        
        if (sub == null) {
            showError("No pending proposal");
            return;
        }
        
        Student stu = findStudentById(studentId);
        
        String[] options = {"Approve", "Reject", "Suggest Alternative"};
        int choice = JOptionPane.showOptionDialog(this, 
            "Proposal: " + sub.getProposedTitle() + "\nStudent: " + stu.getName(),
            "Respond to Proposal", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        
        if (choice == 0) {
            sub.setApproved(true);
            String msg = "[APPROVED] Your title proposal '" + sub.getProposedTitle() + "' has been approved!";
            ChatMessage cm = new ChatMessage(generateShortId(), teacher.getId(), "TEACHER", stu.getId(), "STUDENT", "INDIVIDUAL", msg);
            chats.add(cm);
            showInfo("Proposal approved");
        } else if (choice == 1) {
            String reason = JOptionPane.showInputDialog(this, "Rejection reason:");
            if (reason != null && !reason.trim().isEmpty()) {
                sub.setApproved(false);
                sub.setRejectionMessage(reason);
                String msg = "[REJECTED] Your title proposal '" + sub.getProposedTitle() + "' has been rejected.\nReason: " + reason;
                ChatMessage cm = new ChatMessage(generateShortId(), teacher.getId(), "TEACHER", stu.getId(), "STUDENT", "INDIVIDUAL", msg);
                chats.add(cm);
                showInfo("Proposal rejected");
            }
        } else if (choice == 2) {
            String alternative = JOptionPane.showInputDialog(this, "Suggest alternative title:");
            if (alternative != null && !alternative.trim().isEmpty()) {
                sub.setProposedTitle(alternative);
                String msg = "Teacher suggests alternative title: '" + alternative + "'. Please review.";
                ChatMessage cm = new ChatMessage(generateShortId(), teacher.getId(), "TEACHER", stu.getId(), "STUDENT", "INDIVIDUAL", msg);
                chats.add(cm);
                showInfo("Alternative suggested");
            }
        }
        
        saveData();
        refreshSubmissionsTable();
        refreshChatMessages();
    }
    
    // ========== CHAT ==========
    
    public void sendBroadcastMessage(String message) {
        if (message == null || message.trim().isEmpty()) return;
        
        ChatMessage cm = new ChatMessage(generateShortId(), teacher.getId(), "TEACHER", "ALL", "STUDENT", "ALL", message);
        chats.add(cm);
        saveData();
        refreshChatMessages();
        showInfo("Message sent to all students");
    }
    
    // ========== FILE OPERATIONS ==========
    
    public void openAssignmentFile(String title) {
        Assignment a = assignments.stream().filter(ass -> ass.getTitle().equals(title)).findFirst().orElse(null);
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
        
        if (options.isEmpty()) {
            showInfo("No files available");
            return;
        }
        
        String selected = (String) JOptionPane.showInputDialog(this, "Select file:", "Files", 
            JOptionPane.QUESTION_MESSAGE, null, options.toArray(), options.get(0));
        if (selected == null) return;
        
        String[] actions = {"Open", "Download", "Replace", "Delete"};
        String action = (String) JOptionPane.showInputDialog(this, "Action for " + selected, "Action", 
            JOptionPane.QUESTION_MESSAGE, null, actions, actions[0]);
        if (action == null) return;
        
        String path = pathMap.get(selected);
        
        if (action.equals("Open")) {
            DataStorage.openFile(path);
        } else if (action.equals("Download")) {
            downloadFile(path, selected);
        } else if (action.equals("Replace")) {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    File newFile = fc.getSelectedFile();
                    DataStorage.validateFile(newFile);
                    byte[] data = Files.readAllBytes(newFile.toPath());
                    String newPath = DataStorage.saveUploadedFile(data, newFile.getName(), "assignments");
                    DataStorage.deleteUploadedFile(path);
                    
                    if (selected.equals("Guideline File")) {
                        a.setGuidelineFile(newPath);
                    } else {
                        FileAttachment fa = a.getAttachments().stream().filter(f -> f.getFileName().equals(selected)).findFirst().orElse(null);
                        if (fa != null) {
                            fa.setFileName(newFile.getName());
                            fa.setStoredPath(newPath);
                            fa.setLastModified(LocalDateTime.now());
                            fa.setEdited(true);
                        }
                    }
                    saveData();
                    refreshAssignmentsTable();
                    showInfo("File replaced");
                } catch (Exception ex) {
                    showError("Replace failed: " + ex.getMessage());
                }
            }
        } else if (action.equals("Delete")) {
            int confirm = JOptionPane.showConfirmDialog(this, "Delete " + selected + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                DataStorage.deleteUploadedFile(path);
                if (selected.equals("Guideline File")) {
                    a.setGuidelineFile(null);
                } else {
                    FileAttachment fa = a.getAttachments().stream().filter(f -> f.getFileName().equals(selected)).findFirst().orElse(null);
                    if (fa != null) {
                        a.getAttachments().remove(fa);
                    }
                }
                saveData();
                refreshAssignmentsTable();
                showInfo("File deleted");
            }
        }
    }
    
    public void openSubmissionFile(String studentInfo, String assignmentTitle) {
        String studentId = studentInfo.split(" \\(")[1].replace(")", "");
        Submission sub = submissions.stream()
            .filter(s -> s.getStudentId().equals(studentId) && 
                   findAssignmentById(s.getAssignmentId()).getTitle().equals(assignmentTitle))
            .findFirst().orElse(null);
        
        if (sub == null || sub.getAttachments().isEmpty()) {
            showInfo("No files");
            return;
        }
        
        String[] options = sub.getAttachments().stream().map(FileAttachment::getFileName).toArray(String[]::new);
        String selected = (String) JOptionPane.showInputDialog(this, "Select file:", "Files", 
            JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (selected == null) return;
        
        String[] actions = {"Open", "Download"};
        String action = (String) JOptionPane.showInputDialog(this, "Action for " + selected, "Action", 
            JOptionPane.QUESTION_MESSAGE, null, actions, actions[0]);
        if (action == null) return;
        
        FileAttachment fa = sub.getAttachments().stream().filter(f -> f.getFileName().equals(selected)).findFirst().orElse(null);
        if (fa != null) {
            if (action.equals("Open")) {
                DataStorage.openFile(fa.getStoredPath());
            } else if (action.equals("Download")) {
                downloadFile(fa.getStoredPath(), selected);
            }
        }
    }
    
    private void downloadFile(String path, String name) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(name));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Files.copy(Paths.get(path), fc.getSelectedFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
                showInfo("Downloaded: " + fc.getSelectedFile().getName());
            } catch (IOException ex) {
                showError("Download failed");
            }
        }
    }
    
    // ========== SETTINGS ==========
    
    public void changeTeacherPassword() {
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
                AuthService.changePassword(teacher, p1);
                // Update in list
                for (int i = 0; i < teachers.size(); i++) {
                    if (teachers.get(i).getId().equals(teacher.getId())) {
                        teachers.set(i, teacher);
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
        sb.append("Teacher: ").append(teacher.getName()).append("\n");
        sb.append("Course: ").append(teacher.getCourse()).append("\n");
        sb.append("Export Date: ").append(LocalDateTime.now()).append("\n\n");
        sb.append("Students:\n");
        students.forEach(s -> sb.append("- ").append(s.getId()).append(" | ").append(s.getName()).append("\n"));
        sb.append("\nAssignments:\n");
        assignments.forEach(a -> sb.append("- ").append(a.getTitle()).append(" | Due: ").append(a.getDeadline()).append("\n"));
        sb.append("\nSubmissions:\n");
        submissions.forEach(s -> {
            Student stu = findStudentById(s.getStudentId());
            sb.append("- ").append(stu != null ? stu.getName() : s.getStudentId()).append(": ");
            sb.append(s.getGrade() != null ? "Graded - " + s.getGrade() : "Pending").append("\n");
        });
        
        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Export Data", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void exportGradesToCSV() {
        try {
            String csv = DataStorage.exportGradesToCSV(submissions, assignments, students);
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("grades_export_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                Files.write(fc.getSelectedFile().toPath(), csv.getBytes());
                showInfo("Grades exported successfully");
            }
        } catch (IOException ex) {
            showError("Export failed: " + ex.getMessage());
        }
    }
    
    // ========== NOTIFICATIONS ==========
    
    public void showNotificationsDialog() {
        JDialog d = new JDialog(this, "Notifications", true);
        d.setSize(450, 500);
        d.setLocationRelativeTo(this);
        
        DefaultListModel<String> model = new DefaultListModel<>();
        notifications.stream().filter(n -> n.getUserId().equals(teacher.getId())).forEach(n -> {
            model.addElement("[ " + n.getTitle() + " ]\n   " + n.getMessage() + "\n   Time: " + 
                n.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            n.setRead(true);
        });
        
        JList<String> list = new JList<>(model);
        list.setBackground(fieldBg);
        d.add(new JScrollPane(list), BorderLayout.CENTER);
        
        JButton close = new JButton("Close");
        close.setBackground(primaryColor);
        close.setForeground(Color.WHITE);
        close.addActionListener(e -> d.dispose());
        d.add(close, BorderLayout.SOUTH);
        
        d.setVisible(true);
        saveData();
        refreshNotificationsList();
    }
    
    // ========== LOGOUT ==========
    
    public void logout() {
        if (JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            saveData();
            dispose();
            new MainApp();
        }
    }
    
    // ========== REFRESH METHODS ==========
    
    private void refreshStudentsTable() {
        if (studentsTableModel == null) return;
        studentsTableModel.setRowCount(0);
        for (Student s : students) {
            String groupName = groups.stream().filter(g -> g.hasStudent(s.getId())).findFirst().map(Group::getGroupName).orElse("None");
            long subCount = submissions.stream().filter(sub -> sub.getStudentId().equals(s.getId())).count();
            String status = s.isMustChangePassword() ? "Password Required" : "Active";
            studentsTableModel.addRow(new Object[]{s.getId(), s.getName(), s.getEmail(), s.getCourse(), groupName, subCount, status});
        }
    }
    
    private void refreshGroupsTable() {
        if (groupsTableModel == null) return;
        groupsTableModel.setRowCount(0);
        for (Group g : groups) {
            Assignment a = assignments.stream().filter(ass -> ass.getId() != null && ass.getId().equals(g.getAssignmentId())).findFirst().orElse(null);
            String assignTitle = (a != null) ? a.getTitle() : "No assignment";
            String members = String.join(", ", g.getStudentIds());
            if (members.length() > 50) members = members.substring(0, 47) + "...";
            groupsTableModel.addRow(new Object[]{
                g.getGroupId().substring(0, Math.min(8, g.getGroupId().length())),
                g.getGroupName(), assignTitle, g.getSize(), members
            });
        }
    }
    
    private void refreshAssignmentsTable() {
        if (assignmentsTableModel == null) return;
        assignmentsTableModel.setRowCount(0);
        for (Assignment a : assignments) {
            long propCount = submissions.stream()
                .filter(s -> s.getAssignmentId().equals(a.getId()) && s.getProposedTitle() != null && s.getApproved() == null)
                .count();
            String status = a.getDeadline().isBefore(LocalDate.now()) ? "Past Due" : "Active";
            int fileCount = a.getAttachments().size();
            boolean hasGuideline = a.getGuidelineFile() != null;
            int totalFiles = fileCount + (hasGuideline ? 1 : 0);
            assignmentsTableModel.addRow(new Object[]{
                a.getTitle(), a.getType(), a.getDeadline().toString(),
                a.getMaxScore(), propCount, status, totalFiles > 0 ? totalFiles + " file(s)" : "-"
            });
        }
    }
    
    private void refreshSubmissionsTable() {
        if (submissionsTableModel == null) return;
        submissionsTableModel.setRowCount(0);
        for (Submission s : submissions) {
            Student stu = students.stream().filter(st -> st.getId().equals(s.getStudentId())).findFirst().orElse(null);
            Assignment a = assignments.stream().filter(ass -> ass.getId().equals(s.getAssignmentId())).findFirst().orElse(null);
            
            String titleDisplay = "";
            if (s.getSelectedTeacherTitle() != null && !s.getSelectedTeacherTitle().isEmpty()) {
                titleDisplay = "[Selected] " + s.getSelectedTeacherTitle();
            } else if (s.getProposedTitle() != null) {
                titleDisplay = "[Proposed] " + s.getProposedTitle();
                if (s.getApproved() != null) {
                    titleDisplay = s.getApproved() ? "[Approved] " + s.getProposedTitle() : "[Rejected] " + s.getProposedTitle();
                }
            } else {
                titleDisplay = "-";
            }
            
            String status;
            if (s.getGrade() != null) status = "Graded";
            else if (s.getApproved() != null) status = s.getApproved() ? "Approved" : "Rejected";
            else if (s.getProposedTitle() != null) status = "Pending";
            else status = "Submitted";
            
            int fileCount = s.getAttachments().size();
            submissionsTableModel.addRow(new Object[]{
                stu != null ? stu.getName() + " (" + stu.getId() + ")" : s.getStudentId(),
                a != null ? a.getTitle() : "Unknown",
                titleDisplay,
                s.getSubmissionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                status,
                s.getGrade() != null ? s.getGrade() + "/" + (a != null ? a.getMaxScore() : "?") : "-",
                fileCount > 0 ? "Open" : "-"
            });
        }
    }
    
    private void refreshChatMessages() {
        if (chatMessagesPanel == null) return;
        
        SwingUtilities.invokeLater(() -> {
            chatMessagesPanel.removeAll();
            
            List<ChatMessage> relevant = chats.stream()
                .filter(m -> m.getToUserId().equals("ALL") || m.getFromUserId().equals(teacher.getId()))
                .sorted(Comparator.comparing(ChatMessage::getTimestamp))
                .collect(Collectors.toList());
            
            for (ChatMessage msg : relevant) {
                boolean fromMe = msg.getFromUserId().equals(teacher.getId());
                JPanel bubble = createChatBubble(msg, fromMe);
                chatMessagesPanel.add(bubble);
                chatMessagesPanel.add(Box.createVerticalStrut(5));
            }
            
            chatMessagesPanel.revalidate();
            chatMessagesPanel.repaint();
            JScrollBar bar = chatScrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }
    
    private JPanel createChatBubble(ChatMessage msg, boolean fromMe) {
        JPanel bubble = new JPanel(new BorderLayout(5, 5));
        bubble.setOpaque(false);
        
        JPanel msgPanel = new JPanel();
        msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.Y_AXIS));
        
        String sender;
        if (fromMe) {
            sender = "You (Teacher: " + teacher.getName() + " / ID: " + teacher.getId() + ")";
        } else {
            Student stu = students.stream().filter(s -> s.getId().equals(msg.getFromUserId())).findFirst().orElse(null);
            if (stu != null) {
                sender = "Student: " + stu.getName() + " / ID: " + stu.getId();
            } else {
                sender = msg.getFromUserId();
            }
        }
        
        JLabel senderLabel = new JLabel(sender);
        senderLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        senderLabel.setForeground(Color.GRAY);
        
        JTextArea text = new JTextArea(msg.getMessage());
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        text.setBorder(new EmptyBorder(8, 12, 8, 12));
        
        JLabel time = new JLabel(msg.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm")));
        time.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        time.setForeground(Color.GRAY);
        
        if (fromMe) {
            text.setBackground(primaryColor);
            text.setForeground(Color.WHITE);
            msgPanel.add(Box.createVerticalStrut(2));
            msgPanel.add(text);
            msgPanel.add(Box.createVerticalStrut(2));
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            right.setOpaque(false);
            right.add(time);
            msgPanel.add(right);
            bubble.add(msgPanel, BorderLayout.EAST);
        } else {
            text.setBackground(new Color(224, 224, 224));
            text.setForeground(Color.BLACK);
            msgPanel.add(senderLabel);
            msgPanel.add(Box.createVerticalStrut(2));
            msgPanel.add(text);
            msgPanel.add(Box.createVerticalStrut(2));
            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            left.setOpaque(false);
            left.add(time);
            msgPanel.add(left);
            bubble.add(msgPanel, BorderLayout.WEST);
        }
        
        text.setMaximumSize(new Dimension(450, Integer.MAX_VALUE));
        return bubble;
    }
    
    private void refreshNotificationsList() {
        long unread = notifications.stream().filter(n -> n.getUserId().equals(teacher.getId()) && !n.isRead()).count();
        notificationBadge.setText(unread > 0 ? "Notifications (" + unread + ")" : "Notifications");
    }
    
    public void refreshAll() {
        loadData();
        refreshStudentsTable();
        refreshGroupsTable();
        refreshAssignmentsTable();
        refreshSubmissionsTable();
        refreshChatMessages();
        refreshNotificationsList();
        showInfo("All data refreshed");
    }
    
    // ========== HELPER METHODS ==========
    
    private String generateShortId() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private Assignment findAssignmentById(String id) {
        return assignments.stream().filter(a -> a.getId().equals(id)).findFirst().orElse(null);
    }
    
    private Student findStudentById(String id) {
        return students.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }
    
    private void startAutoRefresh() {
        refreshTimer = new Timer(10000, e -> {
            refreshStudentsTable();
            refreshGroupsTable();
            refreshAssignmentsTable();
            refreshSubmissionsTable();
            refreshChatMessages();
            refreshNotificationsList();
        });
        refreshTimer.start();
    }
    
    private void stopAutoRefresh() {
        if (refreshTimer != null) refreshTimer.stop();
    }
    
    private void startNotificationChecker() {
        notificationTimer = new Timer(15000, e -> refreshNotificationsList());
        notificationTimer.start();
    }
    
    private void stopNotificationChecker() {
        if (notificationTimer != null) notificationTimer.stop();
    }
    
    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorderPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return btn;
    }
    
    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(headerBg);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(100, 150, 200));
        table.setGridColor(new Color(210, 210, 220));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }
    
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // ========== CELL RENDERER/EDITOR CLASSES ==========
    
    class FileButtonRenderer extends JButton implements TableCellRenderer {
        public FileButtonRenderer() { setOpaque(true); }
        public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean focus, int row, int col) {
            setText(val == null ? "-" : val.toString());
            if (val != null && !"-".equals(val) && !"0 file(s)".equals(val)) {
                setBackground(successColor);
                setForeground(Color.WHITE);
            }
            return this;
        }
    }
    
    class FileButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int row;
        private JTable table;
        private boolean isAssignment;
        
        public FileButtonEditor(JCheckBox chk, boolean isAssignment) {
            super(chk);
            this.isAssignment = isAssignment;
            button = new JButton();
            button.addActionListener(e -> fireEditingStopped());
        }
        
        public Component getTableCellEditorComponent(JTable table, Object val, boolean isSel, int row, int col) {
            this.table = table;
            this.row = row;
            button.setText("Open");
            button.setBackground(successColor);
            button.setForeground(Color.WHITE);
            return button;
        }
        
        public Object getCellEditorValue() {
            SwingUtilities.invokeLater(() -> {
                if (isAssignment) {
                    String title = (String) table.getModel().getValueAt(row, 0);
                    openAssignmentFile(title);
                } else {
                    String studentInfo = (String) table.getModel().getValueAt(row, 0);
                    String assignmentTitle = (String) table.getModel().getValueAt(row, 1);
                    openSubmissionFile(studentInfo, assignmentTitle);
                }
            });
            return "Open";
        }
        
        public boolean stopCellEditing() { return super.stopCellEditing(); }
    }
}