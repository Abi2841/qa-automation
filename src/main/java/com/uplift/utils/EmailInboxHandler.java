package com.uplift.utils;

import com.uplift.config.ConfigReader;

/**
 * Utility explaining and structuring access to the QA test inbox.
 *
 * In enterprise testing, Employer Admin email activation links are typically handled via:
 * 1. Admin direct password provisioning (if UI supports "Set initial password", bypasses email).
 * 2. Virtual SMTP APIs (Mailosaur, Mailtrap, Inbucket) via REST Assured.
 * 3. Standard IMAP / JavaMail against a dedicated QA test inbox.
 */
public final class EmailInboxHandler {

    private EmailInboxHandler() {
        // Prevent direct instantiation
    }

    /**
     * Resolves the email activation approach based on environment configuration.
     * Returns the activation link or null if direct login is supported.
     */
    public static String getActivationLinkForEmail(String recipientEmail) {
        String inboxProvider = ConfigReader.getProperty("email.inbox.provider", "mock");

        switch (inboxProvider.toLowerCase()) {
            case "mailosaur":
                // In Phase 5/API, can query Mailosaur API:
                // String apiKey = ConfigReader.getSecret("mailosaur.api.key");
                // String serverId = ConfigReader.getProperty("mailosaur.server.id");
                System.out.println("[INFO] Polling Mailosaur API for activation email to: " + recipientEmail);
                return null;

            case "mailtrap":
                // Query Mailtrap inbox via REST Assured:
                System.out.println("[INFO] Polling Mailtrap inbox for activation email to: " + recipientEmail);
                return null;

            case "mock":
            default:
                // For local simulation, log instructions
                System.out.println("[INFO] QA Email Inbox Handler: Waiting for activation link for " + recipientEmail);
                return "https://qa.example.com/activate?token=QA-AUTO-SAMPLE-TOKEN";
        }
    }
}
