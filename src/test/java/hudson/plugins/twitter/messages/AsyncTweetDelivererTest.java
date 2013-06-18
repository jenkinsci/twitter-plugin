package hudson.plugins.twitter.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import hudson.plugins.twitter.TwitterConstants;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.conf.ConfigurationBuilder;

public class AsyncTweetDelivererTest {

  private AsyncTweetDeliverer deliverer;
  
  /* Tokens from the hudson_test account */
  private String tweet = "This is a test tweet - " + (new Date()).toString();
  public String token = "43746921-jPgX56XvKBvVuG10UxjYKRhUkt10OX0MdNC6D7c7h";
  public String tokenSecret = "NNbADNfXs3ZusOdGSCJAHdPorf7hCOvsw1ZlxRe7Uk";
  private CustomListener listener = new CustomListener();
  
  @Before
  public void setUp() {
    deliverer = new AsyncTweetDeliverer(token, tokenSecret);
    deliverer.setCustomListener(listener);
  }
  
  @Test
  public void testDelivery() {
    deliverer.deliverTweet(tweet);
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {}
    assertTrue(listener.wasSet);
    assertEquals(tweet, listener.message);
  }
  
  /**
   * A custom listener that records that the tweet was sent and then removes it.
   * 
   * @author Michael Irwin (mikesir87)
   */
  private class CustomListener extends TwitterAdapter {
    public boolean wasSet = false;
    public String message = "";
    public void updatedStatus(twitter4j.Status status) {
      wasSet = true;
      message = status.getText();
      removeStatus(status);
    }
    private void removeStatus(Status status) {
      Twitter twitter = (new TwitterFactory()).getInstance(
          new OAuthAuthorization(
            new ConfigurationBuilder()
                .setOAuthConsumerKey(TwitterConstants.CONSUMER_KEY)
                .setOAuthConsumerSecret(TwitterConstants.CONSUMER_SECRET)
                .setOAuthAccessToken(token)
                .setOAuthAccessTokenSecret(tokenSecret)
                .build()
          )
        );
      try {
        twitter.destroyStatus(status.getId());
      } catch (TwitterException e) {
        e.printStackTrace(System.err);
        assertTrue(false);
      }
    }
    
  }
}
