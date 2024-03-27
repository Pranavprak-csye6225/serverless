package verifyemail;

import com.google.cloud.functions.CloudEventsFunction;
import com.google.events.cloud.pubsub.v1.MessagePublishedData;
import com.google.events.cloud.pubsub.v1.Message;
import com.google.gson.Gson;
import io.cloudevents.CloudEvent;
import java.util.Base64;
import java.util.Objects;
import java.util.logging.Logger;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

public class VerifyEmailSend implements CloudEventsFunction {
  private static final Logger logger = Logger.getLogger(VerifyEmailSend.class.getName());

  @Override
  public void accept(CloudEvent event){
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
    String verificationLink = "http://pranavprakash.me:8080/v1/user/verify?username="+ usernameToken[0] + "&token="+usernameToken[1];
    HttpResponse<JsonNode> request = Unirest.post("https://api.mailgun.net/v3/" + "pranavprakash.me" + "/messages")
			.basicAuth("api", "0ec564171e76d281d141d62bb0512de6-f68a26c9-931a0926")
            .queryString("from", "Webapp <no-reply@pranavprakash.me>")
            .queryString("to", usernameToken[0])
            .queryString("subject", "Email Verification")
            .queryString("html", buildHtmlContent(decodedData, verificationLink))
            .asJson();
    // Log the message
    logger.info("Pub/Sub message: " + decodedData);
  }
  private static String buildHtmlContent(String username, String verificationLink) {
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
