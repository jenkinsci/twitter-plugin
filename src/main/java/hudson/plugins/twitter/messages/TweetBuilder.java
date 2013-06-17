package hudson.plugins.twitter.messages;

import hudson.model.AbstractBuild;

/**
 * Interface that defines a utility that can create the messages that are sent
 * in the Tweet.
 * 
 * @author Michael Irwin (mikesir87)
 */
public interface TweetBuilder {

  /**
   * Generate a Tweet based on the provided build.
   * @param build The build to base the Tweet on.
   * @param includeBuildUrl True if the build URL should be included
   * @return The tweet message that can be used.
   */
  String generateTweet(AbstractBuild<?, ?> build, boolean includeBuildUrl);
  
}
