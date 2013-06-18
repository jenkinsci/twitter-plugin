package hudson.plugins.twitter.messages;

import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.User;
import hudson.plugins.twitter.UserTwitterProperty;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;

import java.io.IOException;
import java.util.Set;

import jenkins.model.Jenkins;

/**
 * A default implementation of the TweetBuilder that generates a standard
 * message, whose format is hard-coded into the application.
 * 
 * @author Michael Irwin (mikesir87)
 *
 */
public class DefaultTweetBuilder implements TweetBuilder {

  private static final String TWEET_FORMAT = "%s%s:%s $%d - %s";
  private LinkGenerator linkGenerator;
  
  public DefaultTweetBuilder(LinkGenerator linkGenerator) {
    this.linkGenerator = linkGenerator;
  }
  
  public String generateTweet(AbstractBuild<?, ?> build, boolean includeBuildUrl) {
    String projectName = build.getProject().getName();
    String result = build.getResult().toString();
    String toBlame = getUserString(build);
    String shortenedUrl = "";
    if (includeBuildUrl) {
      String absoluteBuildURL = Jenkins.getInstance().getRootUrl() + 
          build.getUrl();
      try {
        shortenedUrl = linkGenerator.getShortenedLink(absoluteBuildURL);
      } catch (Exception e) {
        shortenedUrl = "?";
      }
    }
    return String.format(TWEET_FORMAT, toBlame, result, projectName,
        build.number, shortenedUrl);
  }
  
  private String getUserString(AbstractBuild<?, ?> build) {
    if (build.getResult().equals(Result.SUCCESS))
      return "";
    
    StringBuilder userString = new StringBuilder();
    Set<User> culprits = build.getCulprits();
    ChangeLogSet<? extends Entry> changeSet = build.getChangeSet();
    
    if (culprits.size() > 0) {
      for (User user : culprits) {
        addUserToBuilder(user, userString);
      }
    } else if (changeSet != null) {
      for (Entry entry : changeSet) {
        addUserToBuilder(entry.getAuthor(), userString);
      }
    }
    return userString.toString();
  }
  
  private void addUserToBuilder(User user, StringBuilder userString) {
    UserTwitterProperty tid = user.getProperty(UserTwitterProperty.class);
    if (tid.getTwitterid() != null) {
      userString.append("@").append(tid.getTwitterid()).append(" ");
    }    
  }
  
}
