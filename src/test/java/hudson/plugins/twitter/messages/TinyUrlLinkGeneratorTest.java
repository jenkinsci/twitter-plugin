package hudson.plugins.twitter.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for the {@link TinyUrlLinkGenerator} class.
 * 
 * @author Michael Irwin (mikesir87)
 */
public class TinyUrlLinkGeneratorTest {

  private TinyUrlLinkGenerator generator;
  
  @Before
  public void setUp() {
    generator = new TinyUrlLinkGenerator();
  }
  
  @Test
  public void testLinkBuilding() throws Exception {
    String url = "http://twitter.com/hudson_test";
    String returnedUrl = generator.getShortenedLink(url);
    assertTrue(returnedUrl.length() < url.length());
    
    HttpURLConnection con = 
        (HttpURLConnection) (new URL(returnedUrl).openConnection());
    con.setInstanceFollowRedirects(false);
    con.connect();
    assertEquals(HttpStatus.SC_MOVED_PERMANENTLY, con.getResponseCode());
    assertEquals(url, con.getHeaderField("Location"));
  }
}
