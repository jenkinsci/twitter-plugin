package hudson.plugins.twitter;

import hudson.Extension;
import hudson.Functions;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.twitter.messages.AsyncTweetDeliverer;
import hudson.plugins.twitter.messages.DefaultTweetBuilder;
import hudson.plugins.twitter.messages.TweetBuilder;
import hudson.plugins.twitter.messages.TweetDeliverer;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.tasks.Mailer;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author cactusman
 * @author justinedelson
 * @author mikesir87
 */
public class TwitterPublisher extends Notifier {

  private static final Logger LOGGER = Logger.getLogger(TwitterPublisher.class
      .getName());

  private Boolean onlyOnFailureOrRecovery;
  private Boolean includeUrl;

  @DataBoundConstructor
  public TwitterPublisher(String onlyOnFailureOrRecovery, String includeUrl) {
    this.onlyOnFailureOrRecovery = cleanToBoolean(onlyOnFailureOrRecovery);
    this.includeUrl = cleanToBoolean(includeUrl);
  }

  @Override
  public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
      BuildListener listener) {
    if (shouldTweet(build)) {
      try {
        DescriptorImpl descriptor = (DescriptorImpl) getDescriptor();
        TweetBuilder builder = getTweetBuilder();
        String newStatus = builder.generateTweet(build, shouldIncludeUrl());
        descriptor.updateTwit(newStatus);
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Unable to send tweet.", e);
      }
    }
    return true;
  }
  
  protected TweetBuilder getTweetBuilder() {
    return new DefaultTweetBuilder();
  }

  /**
   * Determine if this build results should be tweeted. Uses the local settings
   * if they are provided, otherwise the global settings.
   * 
   * @param build
   *          the Build object
   * @return true if we should tweet this build result
   */
  protected boolean shouldTweet(AbstractBuild<?, ?> build) {
    if (onlyOnFailureOrRecovery == null) {
      if (((DescriptorImpl) getDescriptor()).onlyOnFailureOrRecovery) {
        return isFailureOrRecovery(build);
      } else {
        return true;
      }
    } else if (onlyOnFailureOrRecovery.booleanValue()) {
      return isFailureOrRecovery(build);
    } else {
      return true;
    }
  }

  private static Boolean cleanToBoolean(String string) {
    Boolean result = null;
    if ("true".equals(string) || "Yes".equals(string)) {
      result = Boolean.TRUE;
    } else if ("false".equals(string) || "No".equals(string)) {
      result = Boolean.FALSE;
    }
    return result;
  }

  public Boolean getIncludeUrl() {
    return includeUrl;
  }

  public Boolean getOnlyOnFailureOrRecovery() {
    return onlyOnFailureOrRecovery;
  }

  public BuildStepMonitor getRequiredMonitorService() {
    return BuildStepMonitor.BUILD;
  }

  /**
   * Determine if this build represents a failure or recovery. A build failure
   * includes both failed and unstable builds. A recovery is defined as a
   * successful build that follows a build that was not successful. Always
   * returns false for aborted builds.
   * 
   * @param build
   *          the Build object
   * @return true if this build represents a recovery or failure
   */
  protected boolean isFailureOrRecovery(AbstractBuild<?, ?> build) {
    if (build.getResult() == Result.FAILURE
        || build.getResult() == Result.UNSTABLE) {
      return true;
    } else if (build.getResult() == Result.SUCCESS) {
      AbstractBuild<?, ?> previousBuild = build.getPreviousBuild();
      if (previousBuild != null && previousBuild.getResult() != Result.SUCCESS) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  protected boolean shouldIncludeUrl() {
    if (includeUrl != null) {
      return includeUrl.booleanValue();
    } else {
      return ((DescriptorImpl) getDescriptor()).includeUrl;
    }
  }

  @Extension
  public static final class DescriptorImpl extends
      BuildStepDescriptor<Publisher> {
    
    private static final Logger LOGGER = Logger.getLogger(DescriptorImpl.class
        .getName());

    public String token;
    public String tokenSecret;

    public String hudsonUrl;
    public boolean onlyOnFailureOrRecovery;
    public boolean includeUrl;

    public DescriptorImpl() {
      super(TwitterPublisher.class);
      load();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData)
        throws FormException {
      // set the booleans to false as defaults
      includeUrl = false;
      onlyOnFailureOrRecovery = false;

      req.bindParameters(this, "twitter.");
      hudsonUrl = Mailer.descriptor().getUrl();

      save();
      return super.configure(req, formData);
    }

    public void updateTwit(String message) throws Exception {
      TweetDeliverer deliverer = new AsyncTweetDeliverer(token, tokenSecret);
      deliverer.deliverTweet(message);
    }

    @Override
    public String getDisplayName() {
      return "Twitter";
    }

    public String getToken() {
      return token;
    }

    public String getTokenSecret() {
      return tokenSecret;
    }

    public String getUrl() {
      return hudsonUrl;
    }

    public boolean isIncludeUrl() {
      return includeUrl;
    }

    public boolean isOnlyOnFailureOrRecovery() {
      return onlyOnFailureOrRecovery;
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
      return true;
    }

    @Override
    public Publisher newInstance(StaplerRequest req, JSONObject formData)
        throws FormException {
      if (hudsonUrl == null) {
        // if Hudson URL is not configured yet, infer some default
        hudsonUrl = Functions.inferHudsonURL(req);
        save();
      }
      return super.newInstance(req, formData);
    }

    @Deprecated
    public transient String id;
    @Deprecated
    public transient String password;
  }
}