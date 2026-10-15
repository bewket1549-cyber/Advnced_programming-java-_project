package group1;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class TeacherDashboardUI {
    
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
    
    public static final Color PRIMARY_COLOR = new Color(33, 150, 243);
    public static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    public static final Color WARNING_COLOR = new Color(255, 152, 0);
    public static final Color DANGER_COLOR = new Color(244, 67, 54);
    public static final Color INFO_COLOR = new Color(0, 188, 212);
    public static final Color BG_COLOR = new Color(245, 245, 250);
    public static final Color HEADER_BG = new Color(33, 150, 243);
    
    private TeacherDashboard dashboard;
    
    public TeacherDashboardUI(TeacherDashboard dashboard) {
        this.dashboard = dashboard;
    }
    
    public void initUI(JPanel contentPane, JTabbedPane tabs, Teacher teacher) {
        this.contentPane = contentPane;
        
        JPanel headerPanel = createHeaderPanel(teacher);
        contentPane.add(headerPanel, BorderLayout.NORTH);
        
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.addTab("Students", createStudentsPanel());
        tabs.addTab("Groups", createGroupsPanel());
        tabs.addTab("Assignments", createAssignmentsPanel());
        tabs.addTab("Submissions", createSubmissionsPanel());
        tabs.addTab("Chat", createChatPanel());
        tabs.addTab("Settings", createSettingsPanel());
        
        contentPane.add(tabs, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel(Teacher teacher) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        JLabel welcomeLabel = new JLabel("Teacher Dashboard - " + teacher.getName() + " (" + teacher.getCourse() + ")");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        
        JButton refreshBtn = createButton("Refresh", SUCCESS_COLOR);
        refreshBtn.addActionListener(e -> dashboard.refreshAll());
        
        notificationBadge = new JLabel("Notifications");
        notificationBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        notificationBadge.setForeground(Color.WHITE);
        notificationBadge.setCursor(new Cursor(Cursor.HAND_CURSOR));
        notificationBadge.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { dashboard.showNotificationsDialog(); }
        });
        
        JButton logoutBtn = createButton("Logout", new Color(220, 70, 60));
        logoutBtn.addActionListener(e -> dashboard.logout());
        
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
        panel.setBackground(BG_COLOR);
        
        String[] cols = {"ID", "Name", "Email", "Course", "Group", "Submissions", "Status"};
        studentsTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        studentsTable = new JTable(studentsTableModel);
        styleTable(studentsTable);
        
        JScrollPane sp = new JScrollPane(studentsTable);
        sp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Student Management", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), PRIMARY_COLOR));
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBackground(BG_COLOR);
        
        JButton addBtn = createButton("Add Student", SUCCESS_COLOR);
        addBtn.addActionListener(e -> dashboard.showAddStudentDialog());
        
        JButton editBtn = createButton("Edit Student", INFO_COLOR);
        editBtn.addActionListener(e -> dashboard.editStudent());
        
        JButton deleteBtn = createButton("Delete Student", DANGER_COLOR);
        deleteBtn.addActionListener(e -> dashboard.deleteStudent());
        
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
        panel.setBackground(BG_COLOR);
        
        String[] cols = {"Group ID", "Group Name", "Assignment", "Members", "Created"};
        groupsTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        groupsTable = new JTable(groupsTableModel);
        styleTable(groupsTable);
        
        JScrollPane sp = new JScrollPane(groupsTable);
        sp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(INFO_COLOR, 2),
            "Group Management", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), INFO_COLOR));
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBackground(BG_COLOR);
        
        JButton createBtn = createButton("Create Group", SUCCESS_COLOR);
        createBtn.addActionListener(e -> dashboard.showCreateGroupDialog());
        
        JButton assignAssignmentBtn = createButton("Assign Assignment to Group", INFO_COLOR);
        assignAssignmentBtn.addActionListener(e -> dashboard.assignAssignmentToGroup());
        
        JButton deleteBtn = createButton("Delete Group", DANGER_COLOR);
        deleteBtn.addActionListener(e -> dashboard.deleteGroup());
        
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
        panel.setBackground(BG_COLOR);
        
        String[] cols = {"Title", "Type", "Deadline", "Max Score", "Proposals", "Status", "Files"};
        assignmentsTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        assignmentsTable = new JTable(assignmentsTableModel);
        styleTable(assignmentsTable);
        
        assignmentsTable.getColumn("Files").setCellRenderer(new FileButtonRenderer());
        assignmentsTable.getColumn("Files").setCellEditor(new FileButtonEditor(new JCheckBox(), true));
        
        JScrollPane sp = new JScrollPane(assignmentsTable);
        sp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(SUCCESS_COLOR, 2),
            "Assignment Management", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), SUCCESS_COLOR));
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBackground(BG_COLOR);
        
        JButton postBtn = createButton("Post Assignment", SUCCESS_COLOR);
        postBtn.addActionListener(e -> dashboard.showPostAssignmentDialog());
        
        JButton assignGroupBtn = createButton("Assign Group to Assignment", INFO_COLOR);
        assignGroupBtn.addActionListener(e -> dashboard.assignGroupToAssignment());
        
        JButton editBtn = createButton("Edit Assignment", INFO_COLOR);
        editBtn.addActionListener(e -> dashboard.editAssignment());
        
        JButton deleteBtn = createButton("Delete Assignment", DANGER_COLOR);
        deleteBtn.addActionListener(e -> dashboard.deleteAssignment());
        
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
        panel.setBackground(BG_COLOR);
        
        String[] cols = {"Student", "Assignment", "Title/Proposal", "Submission Date", "Status", "Grade", "Files"};
        submissionsTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        submissionsTable = new JTable(submissionsTableModel);
        styleTable(submissionsTable);
        
        submissionsTable.getColumn("Files").setCellRenderer(new FileButtonRenderer());
        submissionsTable.getColumn("Files").setCellEditor(new FileButtonEditor(new JCheckBox(), false));
        
        JScrollPane sp = new JScrollPane(submissionsTable);
        sp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(WARNING_COLOR, 2),
            "Submission Evaluation", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), WARNING_COLOR));
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBackground(BG_COLOR);
        
        JButton evaluateBtn = createButton("Evaluate", INFO_COLOR);
        evaluateBtn.addActionListener(e -> dashboard.evaluateSubmission());
        
        JButton respondBtn = createButton("Respond to Proposal", SUCCESS_COLOR);
        respondBtn.addActionListener(e -> dashboard.respondToProposal());
        
        JButton exportBtn = createButton("Export Grades", new Color(156, 39, 176));
        exportBtn.addActionListener(e -> dashboard.exportGradesToCSV());
        
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
        panel.setBackground(BG_COLOR);
        
        JPanel chatHeader = new JPanel(new BorderLayout());
        chatHeader.setBorder(new EmptyBorder(5, 10, 5, 10));
        chatHeader.setBackground(PRIMARY_COLOR);
        
        JLabel chatTitle = new JLabel("Chat with Students");
        chatTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chatTitle.setForeground(Color.WHITE);
        chatHeader.add(chatTitle, BorderLayout.WEST);
        
        chatMessagesPanel = new JPanel();
        chatMessagesPanel.setLayout(new BoxLayout(chatMessagesPanel, BoxLayout.Y_AXIS));
        chatMessagesPanel.setBackground(new Color(240, 248, 255));
        
        chatScrollPane = new JScrollPane(chatMessagesPanel);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        inputPanel.setBackground(BG_COLOR);
        
        chatMessageField = new JTextField();
        chatMessageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JButton sendBtn = createButton("Send to All Students", SUCCESS_COLOR);
        sendBtn.addActionListener(e -> {
            String msg = chatMessageField.getText().trim();
            if (!msg.isEmpty()) {
                dashboard.sendBroadcastMessage(msg);
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
        panel.setBackground(BG_COLOR);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JButton changePass = createButton("Change Password", PRIMARY_COLOR);
        changePass.addActionListener(e -> dashboard.changeTeacherPassword());
        
        JButton exportBtn = createButton("Export Data", INFO_COLOR);
        exportBtn.addActionListener(e -> dashboard.exportData());
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(changePass, gbc);
        gbc.gridy = 1;
        panel.add(exportBtn, gbc);
        
        return panel;
    }
    
    public void refreshStudentsTable(List<Student> students, List<Group> groups, List<Submission> submissions) {
        if (studentsTableModel == null) return;
        studentsTableModel.setRowCount(0);
        for (Student s : students) {
            String groupName = groups.stream().filter(g -> g.hasStudent(s.getId())).findFirst().map(Group::getGroupName).orElse("None");
            long subCount = submissions.stream().filter(sub -> sub.getStudentId().equals(s.getId())).count();
            String status = s.isMustChangePassword() ? "Password Required" : "Active";
            studentsTableModel.addRow(new Object[]{s.getId(), s.getName(), s.getEmail(), s.getCourse(), groupName, subCount, status});
        }
    }
    
    public void refreshGroupsTable(List<Group> groups, List<Assignment> assignments) {
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
    
    public void refreshAssignmentsTable(List<Assignment> assignments, List<Submission> submissions) {
        if (assignmentsTableModel == null) return;
        assignmentsTableModel.setRowCount(0);
        for (Assignment a : assignments) {
            long propCount = submissions.stream()
                .filter(s -> s.getAssignmentId().equals(a.getId()) && s.getProposedTitle() != null && s.getApproved() == null)
                .count();
            String status = a.getDeadline().isBefore(LocalDate.now()) ? "Past Due" : "Active";
            int fileCount = a.getAttachments().size();
            assignmentsTableModel.addRow(new Object[]{
                a.getTitle(), a.getType(), a.getDeadline().toString(),
                a.getMaxScore(), propCount, status, fileCount > 0 ? fileCount + " file(s)" : "-"
            });
        }
    }
    
    public void refreshSubmissionsTable(List<Submission> submissions, List<Student> students, List<Assignment> assignments) {
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
    
    public void refreshChatMessages(List<ChatMessage> chats, Teacher teacher, List<Student> students) {
        if (chatMessagesPanel == null) return;
        
        SwingUtilities.invokeLater(() -> {
            chatMessagesPanel.removeAll();
            
            // Show ALL messages (from teacher to ALL, and from any student)
            List<ChatMessage> relevant = chats.stream()
                .filter(m -> m.getToUserId().equals("ALL") || 
                            m.getFromUserId().equals(teacher.getId()) ||
                            (m.getToRole().equals("TEACHER") && m.getToUserId().equals(teacher.getId())))
                .sorted(Comparator.comparing(ChatMessage::getTimestamp))
                .collect(Collectors.toList());
            
            for (ChatMessage msg : relevant) {
                boolean fromMe = msg.getFromUserId().equals(teacher.getId());
                JPanel bubble = createChatBubble(msg, fromMe, teacher, students);
                chatMessagesPanel.add(bubble);
                chatMessagesPanel.add(Box.createVerticalStrut(5));
            }
            
            chatMessagesPanel.revalidate();
            chatMessagesPanel.repaint();
            JScrollBar bar = chatScrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }
    
    private JPanel createChatBubble(ChatMessage msg, boolean fromMe, Teacher teacher, List<Student> students) {
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
        senderLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
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
            text.setBackground(PRIMARY_COLOR);
            text.setForeground(Color.WHITE);
            msgPanel.add(Box.createVerticalStrut(2));
            msgPanel.add(text);
            msgPanel.add(Box.createVerticalStrut(2));
            
            JPanel timeRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            timeRow.setOpaque(false);
            timeRow.add(time);
            msgPanel.add(timeRow);
            
            bubble.add(msgPanel, BorderLayout.EAST);
        } else {
            text.setBackground(new Color(224, 224, 224));
            text.setForeground(Color.BLACK);
            msgPanel.add(senderLabel);
            msgPanel.add(Box.createVerticalStrut(2));
            msgPanel.add(text);
            msgPanel.add(Box.createVerticalStrut(2));
            
            JPanel timeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            timeRow.setOpaque(false);
            timeRow.add(time);
            msgPanel.add(timeRow);
            
            bubble.add(msgPanel, BorderLayout.WEST);
        }
        
        text.setMaximumSize(new Dimension(450, Integer.MAX_VALUE));
        return bubble;
    }
    
    public void refreshNotificationsList(List<Notification> notifications, Teacher teacher) {
        long unread = notifications.stream().filter(n -> n.getUserId().equals(teacher.getId()) && !n.isRead()).count();
        notificationBadge.setText(unread > 0 ? "Notifications (" + unread + ")" : "Notifications");
    }
    
    private JButton createButton(String text, Color bg) {
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
        table.getTableHeader().setBackground(HEADER_BG);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(100, 150, 200));
        table.setGridColor(new Color(210, 210, 220));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }
    
    public DefaultTableModel getStudentsTableModel() { return studentsTableModel; }
    public DefaultTableModel getGroupsTableModel() { return groupsTableModel; }
    public JTable getStudentsTable() { return studentsTable; }
    public JTable getGroupsTable() { return groupsTable; }
    public JTable getAssignmentsTable() { return assignmentsTable; }
    public JTable getSubmissionsTable() { return submissionsTable; }
    
    class FileButtonRenderer extends JButton implements TableCellRenderer {
        public FileButtonRenderer() { setOpaque(true); }
        public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean focus, int row, int col) {
            setText(val == null ? "-" : val.toString());
            if (val != null && !"-".equals(val) && !"0 file(s)".equals(val)) {
                setBackground(SUCCESS_COLOR);
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
            button.setText("Open Files");
            button.setBackground(SUCCESS_COLOR);
            button.setForeground(Color.WHITE);
            return button;
        }
        
        public Object getCellEditorValue() {
            SwingUtilities.invokeLater(() -> {
                if (isAssignment) {
                    String title = (String) table.getModel().getValueAt(row, 0);
                    dashboard.openAssignmentFile(title);
                } else {
                    String studentInfo = (String) table.getModel().getValueAt(row, 0);
                    String assignmentTitle = (String) table.getModel().getValueAt(row, 1);
                    dashboard.openSubmissionFile(studentInfo, assignmentTitle);
                }
            });
            return "Open Files";
        }
        
        public boolean stopCellEditing() { return super.stopCellEditing(); }
    }
}