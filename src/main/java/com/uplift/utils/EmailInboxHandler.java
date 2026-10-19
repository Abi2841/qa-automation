package com.uplift.utils;

import com.uplift.config.ConfigReader;
import jakarta.mail.BodyPart;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.search.RecipientStringTerm;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.FluentWait;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads invite emails from the test inbox over IMAP (e.g. a Gmail account with an App Password).
 *
 * Each run sends invites to a unique "+alias" of the inbox address (see TestDataGenerator.email), so the
 * message for this run is found by recipient and is never confused with older invites.
 * Polling uses FluentWait (no Thread.sleep) and stops as soon as the email arrives.
 */
public final class EmailInboxHandler {

    private static final Pattern LINK = Pattern.compile("https?://[^\\s\"'<>]+");

    private EmailInboxHandler() {
        // Prevent direct instantiation
    }

    /**
     * Simple value object for a received email.
     */
    public static final class Email {
        private final String subject;
        private final String html;
        private final String text;

        Email(String subject, String html, String text) {
            this.subject = subject;
            this.html = html;
            this.text = text;
        }

        public String getSubject() {
            return subject;
        }

        /**
         * Raw body (HTML when available) - used to find links.
         */
        public String getRawBody() {
            return html != null ? html : text;
        }

        /**
         * Readable body with HTML tags, styles and scripts removed - used to find codes.
         */
        public String getPlainText() {
            if (text != null) {
                return text;
            }
            return html.replaceAll("(?is)<(style|script)[^>]*>.*?</\\1>", " ")
                    .replaceAll("(?s)<[^>]+>", " ")
                    .replace("&nbsp;", " ")
                    .replaceAll("\\s+", " ");
        }
    }

    /**
     * Waits for the first email sent to {@code recipient} on or after {@code sentAfter}.
     *
     * @throws TimeoutException if nothing arrives within inbox.timeout.seconds
     */
    public static Email waitForEmail(String recipient, Instant sentAfter) {
        int timeout = Integer.parseInt(ConfigReader.getProperty("inbox.timeout.seconds", "180"));
        int poll = Integer.parseInt(ConfigReader.getProperty("inbox.poll.seconds", "5"));
        // Allow for clock differences between this machine and the mail server
        Date notBefore = Date.from(sentAfter.minusSeconds(120));

        return new FluentWait<>(recipient)
                .withTimeout(Duration.ofSeconds(timeout))
                .pollingEvery(Duration.ofSeconds(poll))
                .withMessage("No email to " + recipient + " arrived within " + timeout + "s")
                .until(r -> findLatest(r, notBefore));
    }

    /**
     * Opens and closes an IMAP connection with the configured inbox login (used by the preflight check).
     */
    public static void verifyLogin() throws MessagingException {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        try (Store store = Session.getInstance(props).getStore("imaps")) {
            store.connect(ConfigReader.getRequired("inbox.imap.host"),
                    Integer.parseInt(ConfigReader.getProperty("inbox.imap.port", "993")),
                    ConfigReader.getRequired("inbox.username"),
                    ConfigReader.getRequired("inbox.password"));
        }
    }

    private static Email findLatest(String recipient, Date notBefore) {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        Session session = Session.getInstance(props);
        try (Store store = session.getStore("imaps")) {
            store.connect(ConfigReader.getRequired("inbox.imap.host"),
                    Integer.parseInt(ConfigReader.getProperty("inbox.imap.port", "993")),
                    ConfigReader.getRequired("inbox.username"),
                    ConfigReader.getRequired("inbox.password"));
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            try {
                Message[] messages = inbox.search(new RecipientStringTerm(Message.RecipientType.TO, recipient));
                for (int i = messages.length - 1; i >= 0; i--) {
                    Message message = messages[i];
                    Date received = message.getReceivedDate();
                    if (received == null || !received.before(notBefore)) {
                        return new Email(message.getSubject(), findPart(message, "text/html"),
                                findPart(message, "text/plain"));
                    }
                }
                return null;
            } finally {
                inbox.close(false);
            }
        } catch (MessagingException | IOException e) {
            // Transient IMAP problems: FluentWait polls again until the timeout
            System.err.println("[WARN] Inbox poll failed: " + e.getMessage());
            return null;
        }
    }

    private static String findPart(Part part, String mimeType) throws MessagingException, IOException {
        if (part.isMimeType(mimeType) && part.getContent() instanceof String) {
            return (String) part.getContent();
        }
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                String found = findPart(bodyPart, mimeType);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /**
     * Extracts the verification code using inbox.code.regex (first capture group).
     */
    public static String extractVerificationCode(Email email) {
        Pattern pattern = Pattern.compile(ConfigReader.getProperty("inbox.code.regex", "(?i)code\\D{0,40}?(\\d{4,8})"));
        Matcher matcher = pattern.matcher(email.getPlainText());
        if (!matcher.find()) {
            throw new IllegalStateException("No verification code matching '" + pattern
                    + "' in email '" + email.getSubject() + "'");
        }
        return matcher.group(1);
    }

    /**
     * Returns the first link in the email that points at the given portal host
     * (falls back to the first link that looks like an invite/verification link).
     */
    public static String extractLink(Email email, String portalUrl) {
        String host = URI.create(portalUrl).getHost();
        String fallback = null;
        Matcher matcher = LINK.matcher(email.getRawBody());
        while (matcher.find()) {
            String link = matcher.group().replace("&amp;", "&");
            if (host != null && link.contains(host)) {
                return link;
            }
            if (fallback == null && link.matches("(?i).*(invite|verif|activat|token|register|signup|set-?password).*")) {
                fallback = link;
            }
        }
        if (fallback != null) {
            return fallback;
        }
        throw new IllegalStateException("No portal link found in email '" + email.getSubject() + "'");
    }
}
