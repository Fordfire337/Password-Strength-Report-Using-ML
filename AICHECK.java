import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.Pattern;
import org.json.JSONObject;

import java.nio.file.*;
import java.io.IOException;
import java.util.*;


public class AICHECK {
	
	
	
    public static void main(String[] args) throws Exception {
    	
    	
    	//enter desired password
        String password = "1Kn0wTh!s!s@k";
        System.out.println("Password Strength Report:");
        System.out.println("------------------------");

        String complexity = checkComplexity(password);
        System.out.println("Complexity: " + complexity);

        String predictability = checkPredictability(password, "C:\\Users\\susie\\eclipse-workspace\\MLStrongPass\\src\\words.txt", "C:\\Users\\susie\\eclipse-workspace\\MLStrongPass\\src\\walk-the-line.txt");
        System.out.println("Predictability: " + predictability);

        JSONObject mlResult = checkWithML(password);
        int mlScore = mlResult.getInt("score");
        System.out.println("ML Model Score (0=weak,2=strong): " + mlScore);
        System.out.println("ML Probabilities: " + mlResult.getJSONArray("probabilities"));

        String breachResult = checkBreach(password);
        System.out.println("Breach Check: " + breachResult);

        String overall = combineScores(complexity, predictability, mlScore, breachResult);
        System.out.println("Overall Password Strength: " + overall);
    }

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

    public static String checkPredictability(String password, String dictionaryFile, String patternsFile) {
        Set<String> dictionary = new HashSet<>();
        Set<String> patterns = new HashSet<>();

        // Load dictionary
        try {
            List<String> dictLines = Files.readAllLines(Paths.get(dictionaryFile));
            for (String line : dictLines) {
                dictionary.add(line.toLowerCase().trim());
            }
        } catch (IOException e) {
            System.out.println("Error loading dictionary: " + e.getMessage());
        }

        // Load patterns
        try {
            List<String> patternLines = Files.readAllLines(Paths.get(patternsFile));
            for (String line : patternLines) {
                patterns.add(line.toLowerCase().trim());
            }
        } catch (IOException e) {
            System.out.println("Error loading patterns: " + e.getMessage());
        }

        String pwLower = password.toLowerCase();

        // Check dictionary
        for (String word : dictionary) {
            if (word.length() < 3) continue; // skip single/double letters
            if (pwLower.contains(word)) {
                return "Weak (dictionary word: " + word + ")";
            }
        }

        // Check patterns
        for (String pattern : patterns) {
            if (pwLower.contains(pattern)) {
                return "Weak (common pattern: " + pattern + ")";
            }
        }

        return "Low predictability";
    }

    private static JSONObject checkWithML(String password) throws IOException {
        URL url = new URL("http://localhost:5000/predict");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        JSONObject jsonInput = new JSONObject();
        jsonInput.put("password", password);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInput.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) response.append(line.trim());
        }

        return new JSONObject(response.toString());
    }

    private static String checkBreach(String password) throws Exception {
        String sha1 = sha1(password).toUpperCase();
        String prefix = sha1.substring(0, 5);
        String suffix = sha1.substring(5);

        URL url = new URL("https://api.pwnedpasswords.com/range/" + prefix);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
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
        for (byte b : result) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static String combineScores(String complexity, String predictability, int mlScore, String breach) {
        int score = 0;
        switch (complexity) {
            case "Strong" -> score += 2;
            case "Moderate" -> score += 1;
        }
        if (predictability.startsWith("Low")) score += 1;
        score += mlScore; // ML score 0-2
        if (breach.startsWith("Compromised")) score -= 2;

        if (score >= 5) return "Strong";
        if (score >= 3) return "Moderate";
        return "Weak";
    }
}
