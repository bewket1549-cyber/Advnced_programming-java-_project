package group1;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class MainApp extends JFrame {
    
    private JComboBox<String> roleCombo;
    private JTextField idField, emailField;
    private JPasswordField passwordField;
    private JComboBox<String> courseCombo;
    private JPanel mainPanel;
    private String selectedCourseName;
    
    public MainApp() {
        initFrame();
        showLogin();
        setupKeyboardShortcuts();
    }
    
    private void initFrame() {
        setTitle("Assignment and Project Submission and Evaluation System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 680);
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    private void showLogin() {
        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(245, 245, 250));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(mainPanel);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel title = new JLabel("Assignment & Project System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(33, 150, 243));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(title, gbc);
        
        JLabel subtitle = new JLabel("Login to continue");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(Color.GRAY);
        gbc.gridy = 1;
        mainPanel.add(subtitle, gbc);
        
        JSeparator sep = new JSeparator();
        gbc.gridy = 2;
        mainPanel.add(sep, gbc);
        
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Role
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(roleLabel, gbc);
        
        roleCombo = new JComboBox<>(new String[]{"Student", "Teacher"});
        roleCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleCombo.setBackground(Color.WHITE);
        roleCombo.addActionListener(e -> toggleCourseSelection());
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainPanel.add(roleCombo, gbc);
        
        // Course Selection
        JLabel courseLabel = new JLabel("Select Course:");
        courseLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.weightx = 0;
        mainPanel.add(courseLabel, gbc);
        
        courseCombo = new JComboBox<>();
        courseCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        courseCombo.setBackground(Color.WHITE);
        courseCombo.addActionListener(e -> onCourseSelected());
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainPanel.add(courseCombo, gbc);
        
        // User ID
        JLabel idLabel = new JLabel("User ID:");
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.weightx = 0;
        mainPanel.add(idLabel, gbc);
        
        idField = new JTextField();
        idField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        idField.setBackground(Color.WHITE);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainPanel.add(idField, gbc);
        
        // Email
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 6;
        mainPanel.add(emailLabel, gbc);
        
        emailField = new JTextField();
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emailField.setBackground(Color.WHITE);
        gbc.gridx = 1;
        mainPanel.add(emailField, gbc);
        
        // Password
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 7;
        mainPanel.add(passLabel, gbc);
        
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        passwordField.setBackground(Color.WHITE);
        gbc.gridx = 1;
        mainPanel.add(passwordField, gbc);
        
        // Default password hint
        JLabel passHint = new JLabel("(Default: temp123 for new students)");
        passHint.setFont(new Font("Segoe UI", Font.ITALIC, 9));
        passHint.setForeground(new Color(100, 100, 100));
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(passHint, gbc);
        
        JCheckBox showPass = new JCheckBox("Show Password");
        showPass.setBackground(new Color(245, 245, 250));
        showPass.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        showPass.addActionListener(e -> passwordField.setEchoChar(showPass.isSelected() ? 0 : '*'));
        gbc.gridy = 9;
        mainPanel.add(showPass, gbc);
        
        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(33, 150, 243));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.addActionListener(e -> performLogin());
        gbc.gridy = 10;
        mainPanel.add(loginBtn, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        buttonPanel.setBackground(new Color(245, 245, 250));
        
        JButton registerBtn = new JButton("Register as Teacher");
        registerBtn.setBackground(new Color(76, 175, 80));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        registerBtn.setFocusPainted(false);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.addActionListener(e -> showTeacherRegistration());
        buttonPanel.add(registerBtn);
        
        JButton forgotPasswordBtn = new JButton("Forgot Password?");
        forgotPasswordBtn.setBackground(new Color(255, 152, 0));
        forgotPasswordBtn.setForeground(Color.WHITE);
        forgotPasswordBtn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        forgotPasswordBtn.setFocusPainted(false);
        forgotPasswordBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPasswordBtn.addActionListener(e -> showPasswordRecovery());
        buttonPanel.add(forgotPasswordBtn);
        
        gbc.gridy = 11;
        mainPanel.add(buttonPanel, gbc);
        
        toggleCourseSelection();
        loadAvailableCourses();
        
        setVisible(true);
    }
    
    private void toggleCourseSelection() {
        boolean isStudent = "Student".equals(roleCombo.getSelectedItem());
        courseCombo.setEnabled(isStudent);
        courseCombo.setVisible(isStudent);
        Component[] components = mainPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel && ((JLabel) comp).getText().equals("Select Course:")) {
                comp.setVisible(isStudent);
                break;
            }
        }
    }
    
    private void loadAvailableCourses() {
        courseCombo.removeAllItems();
        java.util.List<String> courses = DataStorage.discoverAvailableCourses();
        if (courses.isEmpty()) {
            courseCombo.addItem("No courses available");
            courseCombo.setEnabled(false);
        } else {
            for (String course : courses) {
                Teacher teacher = DataStorage.getTeacherInfo(course);
                if (teacher != null) {
                    courseCombo.addItem(course + " - " + teacher.getName());
                } else {
                    courseCombo.addItem(course);
                }
            }
            courseCombo.setSelectedIndex(0);
            onCourseSelected();
        }
    }
    
    private void onCourseSelected() {
        String selected = (String) courseCombo.getSelectedItem();
        if (selected != null && !selected.equals("No courses available")) {
            if (selected.contains(" - ")) {
                selectedCourseName = selected.split(" - ")[0];
            } else {
                selectedCourseName = selected;
            }
        }
    }
    
    private void showTeacherRegistration() {
        mainPanel.removeAll();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(new Color(245, 245, 250));
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel title = new JLabel("Teacher Registration");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(33, 150, 243));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(title, gbc);
        
        JLabel subtitle = new JLabel("Create your teacher account");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subtitle.setForeground(Color.GRAY);
        gbc.gridy = 1;
        mainPanel.add(subtitle, gbc);
        
        JSeparator sep = new JSeparator();
        gbc.gridy = 2;
        mainPanel.add(sep, gbc);
        
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Teacher ID
        JLabel idLabel = new JLabel("Teacher ID *");
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(idLabel, gbc);
        
        JTextField idField = new JTextField();
        idField.setBackground(Color.WHITE);
        idField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainPanel.add(idField, gbc);
        
        // Full Name
        JLabel nameLabel = new JLabel("Full Name *");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.weightx = 0;
        mainPanel.add(nameLabel, gbc);
        
        JTextField nameField = new JTextField();
        nameField.setBackground(Color.WHITE);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainPanel.add(nameField, gbc);
        
        // Department
        JLabel deptLabel = new JLabel("Department *");
        deptLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        gbc.gridx = 0; gbc.gridy = 5;
        mainPanel.add(deptLabel, gbc);
        
        JTextField deptField = new JTextField();
        deptField.setBackground(Color.WHITE);
        deptField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1;
        mainPanel.add(deptField, gbc);
        
        // Course Name
        JLabel courseLabel = new JLabel("Course Name *");
        courseLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        gbc.gridx = 0; gbc.gridy = 6;
        mainPanel.add(courseLabel, gbc);
        
        JTextField courseField = new JTextField();
        courseField.setBackground(Color.WHITE);
        courseField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1;
        mainPanel.add(courseField, gbc);
        
        // Email
        JLabel emailRegLabel = new JLabel("Email *");
        emailRegLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        gbc.gridx = 0; gbc.gridy = 7;
        mainPanel.add(emailRegLabel, gbc);
        
        JTextField emailRegField = new JTextField();
        emailRegField.setBackground(Color.WHITE);
        emailRegField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1;
        mainPanel.add(emailRegField, gbc);
        
        // Password
        JLabel passLabel = new JLabel("Password *");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        gbc.gridx = 0; gbc.gridy = 8;
        mainPanel.add(passLabel, gbc);
        
        JPasswordField passField = new JPasswordField();
        passField.setBackground(Color.WHITE);
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1;
        mainPanel.add(passField, gbc);
        
        // Confirm Password
        JLabel confLabel = new JLabel("Confirm Password *");
        confLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        gbc.gridx = 0; gbc.gridy = 9;
        mainPanel.add(confLabel, gbc);
        
        JPasswordField confField = new JPasswordField();
        confField.setBackground(Color.WHITE);
        confField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1;
        mainPanel.add(confField, gbc);
        
        JCheckBox showPass = new JCheckBox("Show Password");
        showPass.setBackground(new Color(245, 245, 250));
        showPass.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        showPass.addActionListener(e -> {
            char c = showPass.isSelected() ? 0 : '*';
            passField.setEchoChar(c);
            confField.setEchoChar(c);
        });
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(showPass, gbc);
        
        // Password requirements - NO SCROLLPANE
        JTextArea requirements = new JTextArea(5, 30);
        requirements.setText(AuthService.getPasswordRequirements());
        requirements.setEditable(false);
        requirements.setBackground(new Color(255, 243, 224));
        requirements.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        requirements.setLineWrap(true);
        requirements.setWrapStyleWord(true);
        gbc.gridy = 11;
        mainPanel.add(requirements, gbc);
        
        JButton registerBtn = new JButton("Register");
        registerBtn.setBackground(new Color(76, 175, 80));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        registerBtn.setFocusPainted(false);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.addActionListener(e -> {
            try {
                String id = idField.getText().trim();
                String name = nameField.getText().trim();
                String dept = deptField.getText().trim();
                String courseName = courseField.getText().trim();
                String email = emailRegField.getText().trim();
                String pass = new String(passField.getPassword());
                String confirm = new String(confField.getPassword());
                
                if (id.isEmpty() || name.isEmpty() || dept.isEmpty() || courseName.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                    throw new IllegalArgumentException("All fields are required");
                }
                
                if (!pass.equals(confirm)) throw new IllegalArgumentException("Passwords do not match");
                if (!AuthService.isStrongPassword(pass)) throw new IllegalArgumentException(AuthService.getPasswordRequirements());
                if (!AuthService.isValidEmail(email)) throw new IllegalArgumentException("Invalid email");
                
                DataStorage.setCurrentCourse(courseName);
                java.util.List<Teacher> teacherList = DataStorage.loadTeachers();
                if (teacherList.stream().anyMatch(t -> t.getId().equals(id))) {
                    throw new IllegalArgumentException("Teacher ID already exists");
                }
                
                Teacher newTeacher = new Teacher(id, pass, name, email, dept);
                newTeacher.setCourse(courseName);
                teacherList.add(newTeacher);
                DataStorage.saveTeachers(teacherList);
                
                JOptionPane.showMessageDialog(mainPanel, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                showLogin();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainPanel, ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        gbc.gridy = 12;
        mainPanel.add(registerBtn, gbc);
        
        JButton backBtn = new JButton("Back to Login");
        backBtn.setBackground(new Color(100, 100, 100));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> showLogin());
        gbc.gridy = 13;
        mainPanel.add(backBtn, gbc);
        
        mainPanel.revalidate();
        mainPanel.repaint();
        setVisible(true);
    }
    
    private void showPasswordRecovery() {
        JDialog dialog = new JDialog(this, "Password Recovery", true);
        dialog.setSize(380, 280);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(245, 245, 250));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel instruction = new JLabel("Enter your User ID and Course Name");
        instruction.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(instruction, gbc);
        
        gbc.gridy = 1;
        panel.add(new JLabel("User ID:"), gbc);
        JTextField userIdField = new JTextField(12);
        gbc.gridx = 1;
        panel.add(userIdField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Course Name:"), gbc);
        JTextField courseField = new JTextField(12);
        gbc.gridx = 1;
        panel.add(courseField, gbc);
        
        JButton sendBtn = new JButton("Send Recovery Code");
        sendBtn.setBackground(new Color(33, 150, 243));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setFocusPainted(false);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(sendBtn, gbc);
        
        sendBtn.addActionListener(e -> {
            String userId = userIdField.getText().trim();
            String courseName = courseField.getText().trim();
            
            if (userId.isEmpty() || courseName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter both fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            DataStorage.setCurrentCourse(courseName);
            java.util.List<Teacher> teacherList = DataStorage.loadTeachers();
            Teacher teacher = teacherList.stream().filter(t -> t.getId().equals(userId)).findFirst().orElse(null);
            
            if (teacher != null) {
                String token = AuthService.generateRecoveryToken(userId, "TEACHER");
                JOptionPane.showMessageDialog(dialog, 
                    "Recovery Code: " + token + "\n\nThis code expires in 24 hours.\nClick OK to reset password.",
                    "Recovery Code", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                showResetPassword(userId, courseName);
            } else {
                JOptionPane.showMessageDialog(dialog, "No account found", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        dialog.add(panel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
    
    private void showResetPassword(String userId, String courseName) {
        JDialog dialog = new JDialog(this, "Reset Password", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(245, 245, 250));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel instruction = new JLabel("Enter recovery code and new password");
        instruction.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(instruction, gbc);
        
        gbc.gridy = 1;
        panel.add(new JLabel("Recovery Code:"), gbc);
        JTextField tokenField = new JTextField(12);
        gbc.gridx = 1;
        panel.add(tokenField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("New Password:"), gbc);
        JPasswordField newPassField = new JPasswordField(12);
        gbc.gridx = 1;
        panel.add(newPassField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Confirm Password:"), gbc);
        JPasswordField confirmField = new JPasswordField(12);
        gbc.gridx = 1;
        panel.add(confirmField, gbc);
        
        JCheckBox showPass = new JCheckBox("Show Password");
        showPass.setBackground(new Color(245, 245, 250));
        showPass.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        showPass.addActionListener(ev -> {
            char c = showPass.isSelected() ? 0 : '*';
            newPassField.setEchoChar(c);
            confirmField.setEchoChar(c);
        });
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(showPass, gbc);
        
        JTextArea requirements = new JTextArea(4, 30);
        requirements.setText(AuthService.getPasswordRequirements());
        requirements.setEditable(false);
        requirements.setBackground(new Color(255, 243, 224));
        requirements.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        requirements.setLineWrap(true);
        requirements.setWrapStyleWord(true);
        gbc.gridy = 5;
        panel.add(requirements, gbc);
        
        JButton resetBtn = new JButton("Reset Password");
        resetBtn.setBackground(new Color(76, 175, 80));
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setFocusPainted(false);
        gbc.gridy = 6;
        panel.add(resetBtn, gbc);
        
        resetBtn.addActionListener(e -> {
            String token = tokenField.getText().trim().toUpperCase();
            String newPass = new String(newPassField.getPassword());
            String confirm = new String(confirmField.getPassword());
            
            if (token.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!newPass.equals(confirm)) {
                JOptionPane.showMessageDialog(dialog, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!AuthService.verifyRecoveryToken(token)) {
                JOptionPane.showMessageDialog(dialog, "Invalid or expired recovery code", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String tokenUserId = AuthService.getUserIdFromToken(token);
            if (!tokenUserId.equals(userId)) {
                JOptionPane.showMessageDialog(dialog, "Invalid recovery code", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                DataStorage.setCurrentCourse(courseName);
                java.util.List<Teacher> teacherList = DataStorage.loadTeachers();
                Teacher teacher = teacherList.stream().filter(t -> t.getId().equals(userId)).findFirst().orElse(null);
                if (teacher != null) {
                    AuthService.changePassword(teacher, newPass);
                    DataStorage.saveTeachers(teacherList);
                    AuthService.removeRecoveryToken(token);
                    JOptionPane.showMessageDialog(dialog, "Password reset successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        dialog.add(panel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
    
    private void setupKeyboardShortcuts() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "login");
        getRootPane().getActionMap().put("login", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { performLogin(); }
        });
    }
    
    private void performLogin() {
        try {
            String role = (String) roleCombo.getSelectedItem();
            String id = idField.getText().trim();
            String email = emailField.getText().trim();
            String pass = new String(passwordField.getPassword());
            
            if (id.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                throw new IllegalArgumentException("All fields are required");
            }
            
            if ("Teacher".equals(role)) {
                String courseName = JOptionPane.showInputDialog(this, "Enter your Course Name:");
                if (courseName == null || courseName.trim().isEmpty()) {
                    throw new IllegalArgumentException("Course name required");
                }
                
                DataStorage.setCurrentCourse(courseName);
                java.util.List<Teacher> teacherList = DataStorage.loadTeachers();
                Teacher teacher = teacherList.stream()
                    .filter(t -> t.getId().equals(id) && t.getPassword().equals(pass))
                    .findFirst().orElse(null);
                
                if (teacher == null) throw new IllegalArgumentException("Invalid credentials");
                if (!teacher.getEmail().equals(email)) throw new IllegalArgumentException("Email does not match");
                if (teacher.isMustChangePassword()) {
                    changeTeacherPassword(teacher, courseName);
                    return;
                }
                
                java.util.List<Student> students = DataStorage.loadStudents();
                new TeacherDashboard(teacher, students, teacherList);
                dispose();
            } else {
                if (selectedCourseName == null || selectedCourseName.isEmpty()) {
                    throw new IllegalArgumentException("Please select a course");
                }
                
                DataStorage.setCurrentCourse(selectedCourseName);
                java.util.List<Student> studentList = DataStorage.loadStudents();
                java.util.List<Teacher> teacherList = DataStorage.loadTeachers();
                
                Student student = studentList.stream()
                    .filter(s -> s.getId().equals(id) && s.getPassword().equals(pass))
                    .findFirst().orElse(null);
                
                if (student == null) throw new IllegalArgumentException("Invalid credentials or default password is 'temp123' for new students");
                
                if (student.getEmail() == null || student.getEmail().isEmpty()) {
                    student.setEmail(email);
                    DataStorage.saveStudents(studentList);
                    changeStudentPasswordAfterEmail(student);
                    return;
                }
                
                if (!student.getEmail().equals(email)) {
                    throw new IllegalArgumentException("Email does not match");
                }
                
                if (student.isMustChangePassword()) {
                    changeStudentPassword(student);
                    return;
                }
                
                new StudentDashboard(student, studentList, teacherList);
                dispose();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void changeTeacherPassword(Teacher teacher, String courseName) {
        JPasswordField np = new JPasswordField(12);
        JPasswordField cf = new JPasswordField(12);
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
        
        JTextArea req = new JTextArea(4, 30);
        req.setText(AuthService.getPasswordRequirements());
        req.setEditable(false);
        req.setBackground(new Color(255, 243, 224));
        req.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        req.setLineWrap(true);
        req.setWrapStyleWord(true);
        p.add(req, gbc);
        
        int res = JOptionPane.showConfirmDialog(this, p, "Change Password", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String p1 = new String(np.getPassword());
            String p2 = new String(cf.getPassword());
            if (!p1.equals(p2)) {
                showError("Passwords do not match");
                System.exit(0);
            }
            try {
                AuthService.changePassword(teacher, p1);
                DataStorage.setCurrentCourse(courseName);
                java.util.List<Teacher> teacherList = DataStorage.loadTeachers();
                for (int i = 0; i < teacherList.size(); i++) {
                    if (teacherList.get(i).getId().equals(teacher.getId())) {
                        teacherList.set(i, teacher);
                        break;
                    }
                }
                DataStorage.saveTeachers(teacherList);
                JOptionPane.showMessageDialog(this, "Password changed. Please login again.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new MainApp();
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }
    
    private void changeStudentPasswordAfterEmail(Student student) {
        JPasswordField np = new JPasswordField(12);
        JPasswordField cf = new JPasswordField(12);
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
        
        JTextArea req = new JTextArea(4, 30);
        req.setText(AuthService.getPasswordRequirements());
        req.setEditable(false);
        req.setBackground(new Color(255, 243, 224));
        req.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        req.setLineWrap(true);
        req.setWrapStyleWord(true);
        p.add(req, gbc);
        
        int res = JOptionPane.showConfirmDialog(this, p, "Set Password", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String p1 = new String(np.getPassword());
            String p2 = new String(cf.getPassword());
            if (!p1.equals(p2)) {
                showError("Passwords do not match");
                System.exit(0);
            }
            try {
                AuthService.changePassword(student, p1);
                student.setMustChangePassword(false);
                java.util.List<Student> studentList = DataStorage.loadStudents();
                DataStorage.saveStudents(studentList);
                JOptionPane.showMessageDialog(this, "Password set successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                java.util.List<Teacher> teacherList = DataStorage.loadTeachers();
                new StudentDashboard(student, studentList, teacherList);
                dispose();
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }
    
    private void changeStudentPassword(Student student) {
        JPasswordField np = new JPasswordField(12);
        JPasswordField cf = new JPasswordField(12);
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
        
        JTextArea req = new JTextArea(4, 30);
        req.setText(AuthService.getPasswordRequirements());
        req.setEditable(false);
        req.setBackground(new Color(255, 243, 224));
        req.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        req.setLineWrap(true);
        req.setWrapStyleWord(true);
        p.add(req, gbc);
        
        int res = JOptionPane.showConfirmDialog(this, p, "Change Password", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String p1 = new String(np.getPassword());
            String p2 = new String(cf.getPassword());
            if (!p1.equals(p2)) {
                showError("Passwords do not match");
                System.exit(0);
            }
            try {
                AuthService.changePassword(student, p1);
                student.setMustChangePassword(false);
                java.util.List<Student> studentList = DataStorage.loadStudents();
                for (int i = 0; i < studentList.size(); i++) {
                    if (studentList.get(i).getId().equals(student.getId())) {
                        studentList.set(i, student);
                        break;
                    }
                }
                DataStorage.saveStudents(studentList);
                JOptionPane.showMessageDialog(this, "Password changed. Please login again.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new MainApp();
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }
    
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MainApp();
        });
    }
}