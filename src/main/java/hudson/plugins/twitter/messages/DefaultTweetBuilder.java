package hudson.plugins.twitter.messages;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.User;
import hudson.plugins.twitter.UserTwitterProperty;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
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
  
  public String generateTweet(AbstractBuild<?, ?> build, boolean includeBuildUrl) {
    String projectName = build.getProject().getName();
    String result = build.getResult().toString();
    String toblame = "";
    try {
      if (!build.getResult().equals(Result.SUCCESS)) {
        toblame = getUserString(build);
      }
    } catch (Exception ignore) {
    }
    String tinyUrl = "";
    if (includeBuildUrl) {
      String absoluteBuildURL = Jenkins.getInstance().getRootUrl() + 
          build.getUrl();
      try {
        tinyUrl = createTinyUrl(absoluteBuildURL);
      } catch (Exception e) {
        tinyUrl = "?";
      }
    }
    return String.format("%s%s:%s $%d - %s", toblame, result, projectName,
        build.number, tinyUrl);
  }
  
  private String getUserString(AbstractBuild<?, ?> build) throws IOException {
    StringBuilder userString = new StringBuilder("");
    Set<User> culprits = build.getCulprits();
    ChangeLogSet<? extends Entry> changeSet = build.getChangeSet();
    if (culprits.size() > 0) {
      for (User user : culprits) {
        UserTwitterProperty tid = user.getProperty(UserTwitterProperty.class);
        if (tid.getTwitterid() != null) {
          userString.append("@").append(tid.getTwitterid()).append(" ");
        }
      }
    } else if (changeSet != null) {
      for (Entry entry : changeSet) {
        User user = entry.getAuthor();
        UserTwitterProperty tid = user.getProperty(UserTwitterProperty.class);
        if (tid.getTwitterid() != null) {
          userString.append("@").append(tid.getTwitterid()).append(" ");
        }
      }
    }
    return userString.toString();
  }

  private static String createTinyUrl(String url) throws IOException {
    HttpClient client = new HttpClient();
    GetMethod gm = new GetMethod("http://tinyurl.com/api-create.php?url="
        + url.replace(" ", "%20"));

    int status = client.executeMethod(gm);
    if (status == HttpStatus.SC_OK) {
      return gm.getResponseBodyAsString();
    } else {
      throw new IOException("Non-OK response code back from tinyurl: " 
          + status);
    }
  }

}
