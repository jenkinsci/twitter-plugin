package hudson.plugins.twitter.messages;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * A LinkGenerator that uses TinyUrl for its url creation.
 * 
 * @author Michael Irwin (mikesir87)
 */
public class TinyUrlLinkGenerator implements LinkGenerator {

  /**
   * {@inheritDoc}
   */
  public String getShortenedLink(String url) {
    HttpClient client = new HttpClient();
    GetMethod gm = new GetMethod("http://tinyurl.com/api-create.php?url="
        + url.replace(" ", "%20"));

    try {
      int status = client.executeMethod(gm);
      if (status == HttpStatus.SC_OK) {
        return gm.getResponseBodyAsString();
      } else {
        throw new IOException("Non-OK response code back from tinyurl: " 
            + status);
      }
    } catch (IOException e) {
      throw new RuntimeException("Generating of tinyurl link failed", e);
    }
  }
  
}
