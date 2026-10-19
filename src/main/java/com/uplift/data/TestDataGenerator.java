package com.uplift.data;

import com.uplift.config.ConfigReader;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates unique, domain-valid test data for the Irish pension platform.
 *
 * Uniqueness: every value embeds a run id (timestamp to the second + random suffix) plus a
 * per-run sequence number, so repeated or parallel runs never collide.
 */
public final class TestDataGenerator {

    private static final Random RANDOM = new SecureRandom();
    private static final String RUN_ID = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))
            + String.format("%03d", RANDOM.nextInt(1000));
    private static final AtomicInteger SEQUENCE = new AtomicInteger();
    // PPSN check character table: index 0 = W, 1 = A ... 22 = V
    private static final String PPSN_CHECK_LETTERS = "WABCDEFGHIJKLMNOPQRSTUV";

    private TestDataGenerator() {
        // Prevent direct instantiation
    }

    /**
     * Run-unique token, e.g. "261019143005123-7".
     */
    public static String uniqueId() {
        return RUN_ID + "-" + SEQUENCE.incrementAndGet();
    }

    /**
     * Unique alphabetic token for name fields that may reject digits (e.g. 12 -> "BC").
     */
    public static String uniqueLetters() {
        String digits = RUN_ID.substring(RUN_ID.length() - 7) + SEQUENCE.incrementAndGet();
        StringBuilder sb = new StringBuilder();
        for (char c : digits.toCharArray()) {
            sb.append((char) ('A' + (c - '0')));
        }
        return sb.toString();
    }

    public static String prefixed(String value) {
        return ConfigReader.getTestDataPrefix() + value;
    }

    /**
     * Valid PPSN: 7 digits + check letter (mod-23 weighted checksum, weights 8..2), e.g. 1234567T.
     */
    public static String validPpsn() {
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            digits.append(RANDOM.nextInt(10));
        }
        return digits + String.valueOf(ppsnCheckLetter(digits.toString()));
    }

    public static char ppsnCheckLetter(String sevenDigits) {
        int sum = 0;
        for (int i = 0; i < 7; i++) {
            sum += (sevenDigits.charAt(i) - '0') * (8 - i);
        }
        return PPSN_CHECK_LETTERS.charAt(sum % 23);
    }

    public static boolean isValidPpsn(String ppsn) {
        if (ppsn == null || !ppsn.matches("\\d{7}[A-W][A-IW]?")) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 7; i++) {
            sum += (ppsn.charAt(i) - '0') * (8 - i);
        }
        if (ppsn.length() == 9 && ppsn.charAt(8) != 'W') {
            // Newer two-letter PPSNs include the second letter (A=1..I=9) with weight 9
            sum += (ppsn.charAt(8) - 'A' + 1) * 9;
        }
        return ppsn.charAt(7) == PPSN_CHECK_LETTERS.charAt(sum % 23);
    }

    /**
     * Irish mobile in international format: +353 8[3-9] + 7 digits.
     */
    public static String irishMobile() {
        int network = 3 + RANDOM.nextInt(7);
        return "+3538" + network + String.format("%07d", RANDOM.nextInt(10_000_000));
    }

    /**
     * Unique 6-digit company registration number.
     */
    public static String companyRegistrationNumber() {
        return String.valueOf(100000 + RANDOM.nextInt(900000));
    }

    /**
     * Irish IBAN with correct ISO 13616 mod-97 check digits: IE + check + bank code + 6-digit sort code + 8-digit account.
     */
    public static String irishIban(String bankCode) {
        String bban = bankCode + String.format("%06d", RANDOM.nextInt(1_000_000))
                + String.format("%08d", RANDOM.nextInt(100_000_000));
        return "IE" + ibanCheckDigits("IE", bban) + bban;
    }

    public static String ibanCheckDigits(String country, String bban) {
        String rearranged = bban + country + "00";
        StringBuilder numeric = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            numeric.append(Character.isLetter(c) ? String.valueOf(c - 'A' + 10) : String.valueOf(c));
        }
        int check = 98 - new BigInteger(numeric.toString()).mod(BigInteger.valueOf(97)).intValue();
        return String.format("%02d", check);
    }

    public static boolean isValidIban(String iban) {
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        StringBuilder numeric = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            numeric.append(Character.isLetter(c) ? String.valueOf(c - 'A' + 10) : String.valueOf(c));
        }
        return new BigInteger(numeric.toString()).mod(BigInteger.valueOf(97)).intValue() == 1;
    }

    /**
     * Unique email address. With a test inbox configured (inbox.address), a "+alias" of it is returned,
     * so the invite email can be read back; otherwise a reserved example.com address is used.
     */
    public static String email(String tag) {
        String local = ConfigReader.getTestDataPrefix().toLowerCase() + tag + "-" + uniqueId();
        String inbox = ConfigReader.getProperty("inbox.address", ConfigReader.getProperty("inbox.username"));
        if (inbox != null && inbox.contains("@")) {
            String[] parts = inbox.split("@", 2);
            return parts[0] + "+" + local + "@" + parts[1];
        }
        return local + "@example.com";
    }

    public static boolean isInboxConfigured() {
        return ConfigReader.isSet("inbox.username") && ConfigReader.isSet("inbox.password");
    }

    /**
     * Strong, run-unique password for the employer admin account. Never logged.
     */
    public static String strongPassword() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder("Qa!");
        for (int i = 0; i < 12; i++) {
            sb.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        }
        return sb.append("7z").toString();
    }
}
