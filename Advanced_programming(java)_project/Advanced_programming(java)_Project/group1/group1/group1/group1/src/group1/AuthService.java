package group1;

import java.util.List;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthService {
    
    private static final Pattern STRONG_PASSWORD = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
    );
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static Map<String, PasswordRecoveryToken> recoveryTokens = new HashMap<>();
    
    public static boolean loginTeacher(String id, String password, List<Teacher> teachers) {
        return teachers.stream().anyMatch(t -> t.getId().equals(id) && t.getPassword().equals(password));
    }
    
    public static Teacher getTeacher(String id, List<Teacher> teachers) {
        return teachers.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
    }
    
    public static boolean loginStudent(String id, String password, List<Student> students) {
        return students.stream().anyMatch(s -> s.getId().equals(id) && s.getPassword().equals(password));
    }
    
    public static Student getStudent(String id, List<Student> students) {
        return students.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }
    
    public static boolean isStrongPassword(String password) {
        return password != null && STRONG_PASSWORD.matcher(password).matches();
    }
    
    public static void changePassword(User user, String newPassword) throws IllegalArgumentException {
        if (!isStrongPassword(newPassword)) {
            throw new IllegalArgumentException(getPasswordRequirements());
        }
        user.setPassword(newPassword);
        user.setMustChangePassword(false);
    }
    
    public static String getPasswordRequirements() {
        return "Password Requirements:\n\n" +
               "- Minimum 8 characters\n" +
               "- At least one uppercase letter (A-Z)\n" +
               "- At least one lowercase letter (a-z)\n" +
               "- At least one digit (0-9)\n" +
               "- At least one special character (@#$%^&+=)\n" +
               "- No spaces allowed";
    }
    
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    public static boolean isValidId(String id) {
        return id != null && !id.trim().isEmpty() && !id.contains(" ");
    }
    
    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty();
    }
    
    public static String generateRecoveryToken(String userId, String userRole) {
        String token = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        PasswordRecoveryToken recoveryToken = new PasswordRecoveryToken(token, userId, userRole);
        recoveryTokens.put(token, recoveryToken);
        return token;
    }
    
    public static boolean verifyRecoveryToken(String token) {
        PasswordRecoveryToken recoveryToken = recoveryTokens.get(token);
        if (recoveryToken == null) return false;
        return !recoveryToken.isExpired();
    }
    
    public static String getUserIdFromToken(String token) {
        PasswordRecoveryToken recoveryToken = recoveryTokens.get(token);
        return recoveryToken != null ? recoveryToken.getUserId() : null;
    }
    
    public static String getUserRoleFromToken(String token) {
        PasswordRecoveryToken recoveryToken = recoveryTokens.get(token);
        return recoveryToken != null ? recoveryToken.getUserRole() : null;
    }
    
    public static void removeRecoveryToken(String token) {
        recoveryTokens.remove(token);
    }
    
    private static class PasswordRecoveryToken {
        private String token;
        private String userId;
        private String userRole;
        private long timestamp;
        private static final long EXPIRY_HOURS = 24;
        
        public PasswordRecoveryToken(String token, String userId, String userRole) {
            this.token = token;
            this.userId = userId;
            this.userRole = userRole;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getUserId() { return userId; }
        public String getUserRole() { return userRole; }
        
        public boolean isExpired() {
            return (System.currentTimeMillis() - timestamp) > (EXPIRY_HOURS * 3600000);
        }
    }
}