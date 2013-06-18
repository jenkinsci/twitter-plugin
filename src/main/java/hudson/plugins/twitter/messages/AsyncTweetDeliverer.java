package hudson.plugins.twitter.messages;

import hudson.plugins.twitter.TwitterConstants;

import java.util.logging.Logger;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterListener;
import twitter4j.TwitterMethod;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.conf.ConfigurationBuilder;

public class AsyncTweetDeliverer implements TweetDeliverer {
  
  private static final Logger LOGGER = 
      Logger.getLogger(TweetDeliverer.class.getName());
  
  private String token;
  private String tokenSecret;
  private TwitterListener customListener;
  
  public AsyncTweetDeliverer(String token, String tokenSecret) {
    this.token = token;
    this.tokenSecret = tokenSecret;
  }

  public void deliverTweet(String tweet) {
    AsyncTwitterFactory factory = new AsyncTwitterFactory();
    AsyncTwitter twitter = factory.getInstance(new OAuthAuthorization(
        new ConfigurationBuilder()
            .setOAuthConsumerKey(TwitterConstants.CONSUMER_KEY)
            .setOAuthConsumerSecret(TwitterConstants.CONSUMER_SECRET)
            .setOAuthAccessToken(token)
            .setOAuthAccessTokenSecret(tokenSecret)
            .build()
        )
    );
    twitter.addListener(new TwitterAdapter() {
      @Override
      public void onException(TwitterException e, TwitterMethod method) {
        LOGGER.warning("Exception updating Twitter status: " + e.toString());
      }

      @Override
      public void updatedStatus(Status statuses) {
        LOGGER.info("Updated Twitter status: " + statuses.getText());
      }
    });
    
    if (customListener != null) {
      twitter.addListener(customListener);
    }
    
    twitter.updateStatus(tweet);
  }
  
  protected void setCustomListener(TwitterListener customListener) {
    this.customListener = customListener;
  }
  
}
