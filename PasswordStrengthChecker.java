import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class PasswordStrengthChecker {

    // Simple dictionary for demonstration
    private static Set<String> dictionaryWords = new HashSet<>();

    static {
        try (BufferedReader br = new BufferedReader(new FileReader("dictionary.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                dictionaryWords.add(line.trim().toLowerCase());
            }
        } catch (IOException e) {
            System.out.println("Dictionary file not found, skipping predictability checks.");
        }
    }

    public static void main(String[] args) throws Exception {
        String password = "P@ssw0rd123";

        System.out.println("Password Strength Report:");
        System.out.println("------------------------");
        System.out.println("Length & Complexity: " + checkComplexity(password));
        System.out.println("Predictability: " + checkPredictability(password));
        System.out.println("Breach Check: " + checkBreach(password));
    }

    // 1. Length and complexity
    private static String checkComplexity(String password) {
        int lengthScore = password.length() >= 12 ? 2 : password.length() >= 8 ? 1 : 0;
        int upper = Pattern.compile("[A-Z]").matcher(password).find() ? 1 : 0;
        int lower = Pattern.compile("[a-z]").matcher(password).find() ? 1 : 0;
        int digit = Pattern.compile("[0-9]").matcher(password).find() ? 1 : 0;
        int special = Pattern.compile("[^A-Za-z0-9]").matcher(password).find() ? 1 : 0;

        int score = lengthScore + upper + lower + digit + special;
        if (score >= 6) return "Strong";
        if (score >= 4) return "Moderate";
        return "Weak";
    }

    // 2. Predictability dictionary check
    private static String checkPredictability(String password) {
        String lowerPass = password.toLowerCase();
        for (String word : dictionaryWords) {
            if (lowerPass.contains(word)) {
                return "Weak (contains dictionary word: " + word + ")";
            }
        }
        // add pattern detection (like "1234", "password", "qwerty") here
        return "Low predictability";
    }

    // 3. Breach check using Have I Been Pwned API
    private static String checkBreach(String password) throws Exception {
        String sha1 = sha1(password).toUpperCase();
        String prefix = sha1.substring(0, 5);
        String suffix = sha1.substring(5);

        URL url = new URL("https://api.pwnedpasswords.com/range/" + prefix);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader br = new BufferedReader(new java.io.InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.split(":")[0].equals(suffix)) {
                    return "Compromised! Found in breaches " + line.split(":")[1] + " times.";
                }
            }
        }
        return "Not found in known breaches";
    }

    private static String sha1(String input) throws Exception {
        MessageDigest mDigest = MessageDigest.getInstance("SHA-1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
