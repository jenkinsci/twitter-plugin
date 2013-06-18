package hudson.plugins.twitter.messages;

/**
 * A TweetDeliverer is the channel through which a tweet gets delivered.
 * 
 * @author Michael Irwin (mikesir87)
 */
public interface TweetDeliverer {

  /**
   * Deliver the provided tweet.
   * @param tweet The tweet that should be delivered
   */
  void deliverTweet(String tweet);
  
}
