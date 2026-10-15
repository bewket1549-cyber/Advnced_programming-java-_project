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

public class StudentDashboardUI {
    
    private JPanel contentPane;
    private JPanel chatMessagesPanel;
    private JScrollPane chatScrollPane;
    private JTextField chatMessageField;
    private JTable assignmentsTable;
    private JTable submissionsTable;
    private JTable resultsTable;
    private JLabel notificationBadge;
    
    public static final Color PRIMARY_COLOR = new Color(33, 150, 243);
    public static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    public static final Color WARNING_COLOR = new Color(255, 152, 0);
    public static final Color DANGER_COLOR = new Color(244, 67, 54);
    public static final Color INFO_COLOR = new Color(0, 188, 212);
    public static final Color BG_COLOR = new Color(245, 245, 250);
    public static final Color HEADER_BG = new Color(33, 150, 243);
    
    private StudentDashboard dashboard;
    
    public StudentDashboardUI(StudentDashboard dashboard) {
        this.dashboard = dashboard;
    }
    
    public void initUI(JPanel contentPane, JTabbedPane tabs, Student student) {
        this.contentPane = contentPane;
        
        JPanel headerPanel = createHeaderPanel(student);
        contentPane.add(headerPanel, BorderLayout.NORTH);
        
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.addTab("Assignments", createAssignmentsPanel());
        tabs.addTab("My Submissions", createSubmissionsPanel());
        tabs.addTab("Results", createResultsPanel());
        tabs.addTab("Chat", createChatPanel());
        tabs.addTab("Settings", createSettingsPanel());
        
        contentPane.add(tabs, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel(Student student) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        JLabel welcomeLabel = new JLabel("Welcome, " + student.getName() + " (" + student.getCourse() + ")");
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
    
    private JPanel createAssignmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(BG_COLOR);
        
        String[] cols = {"Title", "Type", "Description", "Deadline", "Status", "Max Score", "Files"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        assignmentsTable = new JTable(model);
        styleTable(assignmentsTable);
        
        assignmentsTable.getColumn("Files").setCellRenderer(new FileButtonRenderer());
        assignmentsTable.getColumn("Files").setCellEditor(new FileButtonEditor(new JCheckBox(), true));
        
        JScrollPane sp = new JScrollPane(assignmentsTable);
        sp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Available Assignments", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14), PRIMARY_COLOR));
        
        JButton submitBtn = createButton("Submit / Add Files", SUCCESS_COLOR);
        submitBtn.addActionListener(e -> {
            int row = assignmentsTable.getSelectedRow();
            if (row >= 0) dashboard.showSubmissionDialog(row);
            else JOptionPane.showMessageDialog(panel, "Select an assignment first");
        });
        
        panel.add(sp, BorderLayout.CENTER);
        panel.add(submitBtn, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createSubmissionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(BG_COLOR);
        
        String[] cols = {"Assignment", "Submission Date", "Title/Proposal", "Status", "Files", "Modified"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        submissionsTable = new JTable(model);
        styleTable(submissionsTable);
        
        submissionsTable.getColumn("Files").setCellRenderer(new FileButtonRenderer());
        submissionsTable.getColumn("Files").setCellEditor(new FileButtonEditor(new JCheckBox(), false));
        
        JScrollPane sp = new JScrollPane(submissionsTable);
        sp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(INFO_COLOR, 2),
            "My Submissions", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), INFO_COLOR));
        
        panel.add(sp, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(BG_COLOR);
        
        String[] cols = {"Assignment", "Grade", "Max Score", "Percentage", "Feedback", "Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        resultsTable = new JTable(model);
        styleTable(resultsTable);
        
        JScrollPane sp = new JScrollPane(resultsTable);
        sp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(WARNING_COLOR, 2),
            "My Grades", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), WARNING_COLOR));
        
        JButton viewBtn = createButton("View Grades", PRIMARY_COLOR);
        viewBtn.addActionListener(e -> {
            String pass = JOptionPane.showInputDialog(panel, "Enter your password:");
            if (pass != null && pass.equals(dashboard.getStudent().getPassword())) {
                dashboard.refreshAll();
                JOptionPane.showMessageDialog(panel, "Grades loaded");
            } else if (pass != null) {
                JOptionPane.showMessageDialog(panel, "Wrong password");
            }
        });
        
        panel.add(sp, BorderLayout.CENTER);
        panel.add(viewBtn, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(BG_COLOR);
        
        JPanel chatHeader = new JPanel(new BorderLayout());
        chatHeader.setBorder(new EmptyBorder(5, 10, 5, 10));
        chatHeader.setBackground(PRIMARY_COLOR);
        
        JLabel chatTitle = new JLabel("Chat with Teacher");
        chatTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chatTitle.setForeground(Color.WHITE);
        chatHeader.add(chatTitle, BorderLayout.WEST);
        
        chatMessagesPanel = new JPanel();
        chatMessagesPanel.setLayout(new BoxLayout(chatMessagesPanel, BoxLayout.Y_AXIS));
        chatMessagesPanel.setBackground(new Color(245, 245, 250));
        
        chatScrollPane = new JScrollPane(chatMessagesPanel);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        inputPanel.setBackground(BG_COLOR);
        
        chatMessageField = new JTextField();
        chatMessageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JButton sendBtn = createButton("Send to Teacher", SUCCESS_COLOR);
        sendBtn.addActionListener(e -> {
            String msg = chatMessageField.getText().trim();
            if (!msg.isEmpty()) {
                dashboard.sendChatMessage(msg);
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
        changePass.addActionListener(e -> dashboard.changePassword());
        
        JButton exportBtn = createButton("Export Data", INFO_COLOR);
        exportBtn.addActionListener(e -> dashboard.exportData());
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(changePass, gbc);
        gbc.gridy = 1;
        panel.add(exportBtn, gbc);
        
        return panel;
    }
    
    public void refreshAssignmentsTable(List<Assignment> assignments) {
        if (assignmentsTable == null) return;
        DefaultTableModel model = (DefaultTableModel) assignmentsTable.getModel();
        model.setRowCount(0);
        
        for (Assignment a : assignments) {
            boolean isOverdue = a.getDeadline().isBefore(LocalDate.now());
            String status = isOverdue ? "Overdue" : "Active";
            int fileCount = a.getAttachments().size();
            boolean hasFiles = fileCount > 0 || (a.getGuidelineFile() != null) || 
                              (a.getTeacherProvidedTitles() != null && !a.getTeacherProvidedTitles().isEmpty());
            
            model.addRow(new Object[]{
                a.getTitle(), a.getType(), a.getDescription(),
                a.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                status, a.getMaxScore(), hasFiles ? "Open" : "-"
            });
        }
    }
    
    public void refreshSubmissionsTable(List<Submission> submissions, List<Assignment> assignments, Student student) {
        if (submissionsTable == null) return;
        DefaultTableModel model = (DefaultTableModel) submissionsTable.getModel();
        model.setRowCount(0);
        
        List<Submission> mySubmissions = submissions.stream()
            .filter(s -> s.getStudentId().equals(student.getId()))
            .collect(Collectors.toList());
        
        for (Submission s : mySubmissions) {
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
            model.addRow(new Object[]{
                a != null ? a.getTitle() : "Unknown",
                s.getSubmissionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                titleDisplay, status, fileCount > 0 ? "Open" : "-",
                s.isEdited() ? "Edited" : "Original"
            });
        }
    }
    
    public void refreshResultsTable(List<Submission> submissions, List<Assignment> assignments, Student student) {
        if (resultsTable == null) return;
        DefaultTableModel model = (DefaultTableModel) resultsTable.getModel();
        model.setRowCount(0);
        
        List<Submission> graded = submissions.stream()
            .filter(s -> s.getStudentId().equals(student.getId()) && s.getGrade() != null)
            .collect(Collectors.toList());
        
        for (Submission s : graded) {
            Assignment a = assignments.stream().filter(ass -> ass.getId().equals(s.getAssignmentId())).findFirst().orElse(null);
            if (a != null) {
                double perc = (double) s.getGrade() / a.getMaxScore() * 100;
                model.addRow(new Object[]{
                    a.getTitle(), s.getGrade(), a.getMaxScore(), String.format("%.1f%%", perc),
                    s.getFeedback(), s.getEvaluatedDate() != null ? 
                        s.getEvaluatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "-"
                });
            }
        }
    }
    
    public void refreshChatMessages(List<ChatMessage> chats, Student student, List<Teacher> teachers) {
        if (chatMessagesPanel == null) return;
        
        SwingUtilities.invokeLater(() -> {
            chatMessagesPanel.removeAll();
            
            Teacher teacher = teachers.isEmpty() ? null : teachers.get(0);
            
            // Show only messages for this student
            List<ChatMessage> relevant = chats.stream()
                .filter(m -> m.getFromUserId().equals(student.getId()) ||
                            (m.getToUserId().equals(student.getId()) && m.getFromRole().equals("TEACHER")) ||
                            (m.getToUserId().equals("ALL") && m.getFromRole().equals("TEACHER")))
                .sorted(Comparator.comparing(ChatMessage::getTimestamp))
                .collect(Collectors.toList());
            
            for (ChatMessage msg : relevant) {
                boolean fromMe = msg.getFromUserId().equals(student.getId());
                JPanel bubble = createChatBubble(msg, fromMe, student, teacher);
                chatMessagesPanel.add(bubble);
                chatMessagesPanel.add(Box.createVerticalStrut(5));
            }
            
            chatMessagesPanel.revalidate();
            chatMessagesPanel.repaint();
            JScrollBar bar = chatScrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }
    
    private JPanel createChatBubble(ChatMessage msg, boolean fromMe, Student student, Teacher teacher) {
        JPanel bubble = new JPanel(new BorderLayout(5, 5));
        bubble.setOpaque(false);
        
        JPanel msgPanel = new JPanel();
        msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.Y_AXIS));
        
        String sender;
        if (fromMe) {
            sender = "You (Student: " + student.getName() + " / ID: " + student.getId() + ")";
        } else {
            if (teacher != null) {
                sender = "Teacher: " + teacher.getName() + " / ID: " + teacher.getId();
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
        
        text.setMaximumSize(new Dimension(350, Integer.MAX_VALUE));
        return bubble;
    }
    
    public void updateNotificationBadge(List<Notification> notifications, Student student) {
        long unread = notifications.stream().filter(n -> n.getUserId().equals(student.getId()) && !n.isRead()).count();
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
    
    class FileButtonRenderer extends JButton implements TableCellRenderer {
        public FileButtonRenderer() { setOpaque(true); }
        public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean focus, int row, int col) {
            setText(val == null ? "-" : val.toString());
            if (val != null && !"-".equals(val)) {
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
                    String assignmentTitle = (String) table.getModel().getValueAt(row, 0);
                    dashboard.openSubmissionFile(assignmentTitle);
                }
            });
            return "Open Files";
        }
        
        public boolean stopCellEditing() { return super.stopCellEditing(); }
    }
}