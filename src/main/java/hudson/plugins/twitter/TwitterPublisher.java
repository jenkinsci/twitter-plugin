package hudson.plugins.twitter;

import hudson.Extension;
import hudson.Functions;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import hudson.tasks.Mailer;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import twitter4j.AsyncTwitter;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;

/**
 * @author cactusman
 * @author justinedelson
 */
public class TwitterPublisher extends Publisher {

    private static final List<String> VALUES_REPLACED_WITH_NULL = Arrays.asList("", "(Default)",
            "(System Default)");

    private static final Logger LOGGER = Logger.getLogger(TwitterPublisher.class.getName());

    private String id;
    private String password;
    private Boolean onlyOnFailureOrRecovery;
    private Boolean includeUrl;

    private TwitterPublisher(String id, String password, Boolean onlyOnFailureOrRecovery,
            Boolean includeUrl) {
        this.onlyOnFailureOrRecovery = onlyOnFailureOrRecovery;
        this.includeUrl = includeUrl;
        this.id = id;
        this.password = password;
    }

    @DataBoundConstructor
    public TwitterPublisher(String id, String password, String onlyOnFailureOrRecovery,
            String includeUrl) {
        this(cleanToString(id), cleanToString(password), cleanToBoolean(onlyOnFailureOrRecovery),
                cleanToBoolean(includeUrl));
    }

    private static String cleanToString(String string) {
        return VALUES_REPLACED_WITH_NULL.contains(string) ? null : string;
    }

    private static Boolean cleanToBoolean(String string) {
        return (VALUES_REPLACED_WITH_NULL.contains(string) || string == null) ? null : Boolean
                .valueOf(string);
    }

    private static String createTinyUrl(String url) throws IOException {
        HttpClient client = new HttpClient();
        GetMethod gm = new GetMethod("http://tinyurl.com/api-create.php?url="
                + url.replace(" ", "%20"));

        int status = client.executeMethod(gm);
        if (status == HttpStatus.SC_OK) {
            return gm.getResponseBodyAsString();
        } else {
            throw new IOException("Non-OK response code back from tinyurl: " + status);
        }

    }

    public String getId() {
        return id;
    }

    public Boolean getIncludeUrl() {
        return includeUrl;
    }

    public Boolean getOnlyOnFailureOrRecovery() {
        return onlyOnFailureOrRecovery;
    }

    public String getPassword() {
        return password;
    }

    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        return _perform(build, launcher, listener);
    }

    protected <P extends AbstractProject<P, B>, B extends AbstractBuild<P, B>> boolean _perform(
            B build, Launcher launcher, BuildListener listener) {
        if (shouldTweet(build)) {
            try {
                String newStatus = createTwitterStatusMessage(build);
                ((DescriptorImpl) getDescriptor()).updateTwit(id, password, newStatus);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unable to send tweet.", e);
            }
        }
        return true;

    }

    private <P extends AbstractProject<P, B>, B extends AbstractBuild<P, B>> String createTwitterStatusMessage(
            B build) {
        String projectName = build.getProject().getName();
        String result = build.getResult().toString();
        String toblame = "";
        try {
            if (!build.getResult().equals(Result.SUCCESS)) {
                toblame = getUserString(build);
            }
        } catch (Exception e) {
        }
        String tinyUrl = "";
        if (shouldIncludeUrl()) {
            String absoluteBuildURL = ((DescriptorImpl) getDescriptor()).getUrl() + build.getUrl();
            try {
                tinyUrl = createTinyUrl(absoluteBuildURL);
            } catch (Exception e) {
                tinyUrl = "?";
            }
        }
        return String.format("%s%s:%s#%d - %s", toblame, result.substring(0, 4), projectName,
                build.number, tinyUrl);

    }

    private <P extends AbstractProject<P, B>, B extends AbstractBuild<P, B>> String getUserString(
            B build) throws IOException {
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

    protected <P extends AbstractProject<P, B>, B extends AbstractBuild<P, B>> String createStatusWithoutURL(
            B build) {
        String projectName = build.getProject().getName();
        String result = build.getResult().toString();
        return String.format("%s:%s #%d", result, projectName, build.number);
    }

    protected <P extends AbstractProject<P, B>, B extends AbstractBuild<P, B>> String createStatusWithURL(
            B build) throws IOException {
        String projectName = build.getProject().getName();
        String result = build.getResult().toString();
        String absoluteBuildURL = ((DescriptorImpl) getDescriptor()).getUrl() + build.getUrl();
        String tinyUrl = createTinyUrl(absoluteBuildURL);
        return String.format("%s:%s #%d - %s", result, projectName, build.number, tinyUrl);
    }

    /**
     * Detrmine if this build represents a failure or recovery. A build failure
     * includes both failed and unstable builds. A recovery is defined as a
     * successful build that follows a build that was not successful. Always
     * returns false for aborted builds.
     * 
     * @param build the Build object
     * @return true if this build represents a recovery or failure
     */
    protected <P extends AbstractProject<P, B>, B extends AbstractBuild<P, B>> boolean isFailureOrRecovery(
            B build) {
        if (build.getResult() == Result.FAILURE || build.getResult() == Result.UNSTABLE) {
            return true;
        } else if (build.getResult() == Result.SUCCESS) {
            B previousBuild = build.getPreviousBuild();
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

    /**
     * Determine if this build results should be tweeted. Uses the local
     * settings if they are provided, otherwise the global settings.
     * 
     * @param build the Build object
     * @return true if we should tweet this build result
     */
    protected <P extends AbstractProject<P, B>, B extends AbstractBuild<P, B>> boolean shouldTweet(
            B build) {
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

    @Extension
    public static final class DescriptorImpl extends Descriptor<Publisher> {
        private static final Logger LOGGER = Logger.getLogger(DescriptorImpl.class.getName());

        public String id;
        public String password;
        public String hudsonUrl;
        public boolean onlyOnFailureOrRecovery;
        public boolean includeUrl;

        private Class<? extends AsyncTwitter> asyncTwitterClass = AsyncTwitter.class;

        public DescriptorImpl() {
            super(TwitterPublisher.class);
            load();
        }

        @Override
        public boolean configure(StaplerRequest req) throws FormException {
            // set the booleans to false as defaults
            includeUrl = false;
            onlyOnFailureOrRecovery = false;

            req.bindParameters(this, "twitter.");
            hudsonUrl = Mailer.descriptor().getUrl();
            save();
            return super.configure(req);
        }

        @Override
        public String getDisplayName() {
            return "Twitter";
        }

        public String getId() {
            return id;
        }

        public String getPassword() {
            return password;
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
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            if (hudsonUrl == null) {
                // if Hudson URL is not configured yet, infer some default
                hudsonUrl = Functions.inferHudsonURL(req);
                save();
            }
            return super.newInstance(req, formData);
        }

        public void updateTwit(String id, String password, String message) throws Exception {
            if (id == null || password == null) {
                id = this.id;
                password = this.password;
            }

            LOGGER.info("Attempting to update Twitter status to: " + message);

            AsyncTwitter twitter = createAsyncTwitter(id, password);
            twitter.updateAsync(message, new TwitterAdapter() {

                @Override
                public void onException(TwitterException e, int method) {
                    LOGGER.warning("Exception updating Twitter status: " + e.toString());
                }

                @Override
                public void updated(Status statuses) {
                    LOGGER.info("Updated Twitter status: " + statuses.getText());
                }

            });
        }

        private AsyncTwitter createAsyncTwitter(String id, String password) throws Exception {
            Constructor<? extends AsyncTwitter> con = asyncTwitterClass.getConstructor(
                    String.class, String.class);

            return con.newInstance(id, password);
        }
    }
}