package verifyemail;

import com.google.cloud.functions.CloudEventsFunction;
import com.google.events.cloud.pubsub.v1.MessagePublishedData;
import com.google.events.cloud.pubsub.v1.Message;
import com.google.gson.Gson;
import io.cloudevents.CloudEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Base64;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import kong.unirest.Unirest;

import java.sql.SQLException;

import javax.sql.DataSource;

public class VerifyEmailSend implements CloudEventsFunction {
    private static final Logger logger = Logger.getLogger(VerifyEmailSend.class.getName());
    private static final String API_KEY = System.getenv("API_KEY");

    @Override
    public void accept(CloudEvent event) {
        // Get cloud event data as JSON string
        String cloudEventData = new String(Objects.requireNonNull(event.getData()).toBytes());
        // Decode JSON event data to the Pub/Sub MessagePublishedData type
        Gson gson = new Gson();
        MessagePublishedData data = gson.fromJson(cloudEventData, MessagePublishedData.class);
        // Get the message from the data
        Message message = data.getMessage();
        // Get the base64-encoded data from the message & decode it
        String encodedData = message.getData();
        String decodedData = new String(Base64.getDecoder().decode(encodedData));
        String[] usernameToken = decodedData.split(":");
        String verificationLink = "https://pranavprakash.me/v1/user/verify?username=" + usernameToken[0] + "&token=" + usernameToken[1];
        logger.info("Verification link: " + verificationLink);
        Unirest.post("https://api.mailgun.net/v3/" + "pranavprakash.me" + "/messages")
                .basicAuth("api", API_KEY)
                .queryString("from", "Webapp <no-reply@pranavprakash.me>")
                .queryString("to", usernameToken[0])
                .queryString("subject", "Email Verification")
                .queryString("html", buildHtmlContent(verificationLink))
                .asJson();
        try {
            DataSource pool = SQLConnectionPoolFactory.createConnectionPool();
            try (Connection conn = pool.getConnection()) {
                String query = "UPDATE webapp.user SET expiry_time = DATE_ADD(NOW(), INTERVAL 2 MINUTE) WHERE USERNAME= ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, usernameToken[0]);
                    stmt.execute();
                    logger.info("Updated for user: " + usernameToken[0]);
                }
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error while attempting to update", ex);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while attempting to update", e);
        }
        // Log the message
        logger.info("Pub/Sub message: " + decodedData);
    }

    private static String buildHtmlContent(String verificationLink) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Email Verification</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <p>Hello,</p>\n" +
                "    <p>Thank you for signing up! Please click the link below to verify your email address:</p>\n" +
                "    <p><a href=\"" + verificationLink + "\">Verify Email</a></p>\n" +
                "    <p>If you did not sign up for this account, please ignore this email.</p>\n" +
                "    <p>Best regards,</p>\n" +
                "    <p>Your Company Name</p>\n" +
                "</body>\n" +
                "</html>";
    }
}

