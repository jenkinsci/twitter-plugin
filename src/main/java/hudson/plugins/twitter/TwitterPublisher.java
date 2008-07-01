package hudson.plugins.twitter;

import hudson.Functions;
import hudson.Launcher;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Project;
import hudson.model.Result;
import hudson.tasks.Mailer;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.lang.reflect.Constructor;
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

    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    private static final Logger LOGGER = Logger.getLogger(TwitterPublisher.class.getName());

    private String id;

    private String password;

    private Boolean onlyOnFailureAndRecovery;

    private Boolean includeURL;

    /**
     * {@stapler-constructor}
     */
    @DataBoundConstructor
    public TwitterPublisher(String id, String password, Boolean onlyOnFailureAndRecovery,
            Boolean includeURL) {
        this.onlyOnFailureAndRecovery = onlyOnFailureAndRecovery;
        this.includeURL = includeURL;
        this.id = id;
        this.password = password;
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

    public Descriptor<Publisher> getDescriptor() {
        return DESCRIPTOR;
    }

    public String getId() {
        return id;
    }

    public Boolean getIncludeURL() {
        return includeURL;
    }

    public Boolean getOnlyOnFailureAndRecovery() {
        return onlyOnFailureAndRecovery;
    }

    public String getPassword() {
        return password;
    }

    /**
     * This bit of generics hackery is taken from the hudson.tasks.MailSender
     * class.
     * 
     * {@inheritDoc}
     */
    @Override
    public boolean perform(Build build, Launcher launcher, BuildListener listener) {
        return _perform(build, launcher, listener);
    }

    protected <P extends Project<P, B>, B extends Build<P, B>> boolean _perform(B build,
            Launcher launcher, BuildListener listener) {
        if (shouldTweet(build)) {

            String newStatus = null;
            if (shouldIncludeURL()) {
                try {
                    newStatus = createStatusWithURL(build);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Unable to tinyfy url", e);
                    newStatus = createStatusWithoutURL(build);
                }
            } else {
                newStatus = createStatusWithoutURL(build);
            }

            try {
                DESCRIPTOR.updateTwit(id, password, newStatus);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unable to send tweet.", e);
            }
        }
        return true;

    }

    protected <P extends Project<P, B>, B extends Build<P, B>> String createStatusWithoutURL(B build) {
        String projectName = build.getProject().getName();
        String result = build.getResult().toString();
        return String.format("%s:%s #%d", result, projectName, build.number);
    }

    protected <P extends Project<P, B>, B extends Build<P, B>> String createStatusWithURL(B build)
            throws IOException {
        String projectName = build.getProject().getName();
        String result = build.getResult().toString();
        String absoluteBuildURL = DESCRIPTOR.getUrl() + build.getUrl();
        String tinyUrl = createTinyUrl(absoluteBuildURL);
        return String.format("%s:%s #%d - %s", result, projectName, build.number, tinyUrl);
    }

    /**
     * Detrmine if this build represents a failure or recovery. A build failure
     * includes both failed and unstable builds. A recovery is defined as a
     * successful build that follows a build that was not successful. Always
     * returns false for aborted builds.
     * 
     * @param build
     *            the Build object
     * @return true if this build represents a recovery or failure
     */
    protected <P extends Project<P, B>, B extends Build<P, B>> boolean isFailureOrRecovery(B build) {
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

    protected boolean shouldIncludeURL() {
        if (includeURL != null) {
            return includeURL.booleanValue();
        } else {
            return DESCRIPTOR.includeURL;
        }
    }

    /**
     * Determine if this build results should be tweeted. Uses the local
     * settings if they are provided, otherwise the global settings.
     * 
     * @param build
     *            the Build object
     * @return true if we should tweet this build result
     */
    protected <P extends Project<P, B>, B extends Build<P, B>> boolean shouldTweet(B build) {
        if (onlyOnFailureAndRecovery == null) {
            if (DESCRIPTOR.onlyOnFailureAndRecovery) {
                return isFailureOrRecovery(build);
            } else {
                return true;
            }
        } else if (onlyOnFailureAndRecovery.booleanValue()) {
            return isFailureOrRecovery(build);
        } else {
            return true;
        }
    }

    public static final class DescriptorImpl extends Descriptor<Publisher> {
        private static final Logger LOGGER = Logger.getLogger(DescriptorImpl.class.getName());

        private static final String ID_FIELD = "twitter-id";

        private static final String PASSWORD_FIELD = "twitter-password";

        private static final String ONLY_ON_FAILURE = "twitter-only-on-failure";

        private static final String INCLUDE_URL_FIELD = "twitter-include-url";

        private Class<? extends AsyncTwitter> asyncTwitterClass = AsyncTwitter.class;
        private String id;
        private String password;
        private String hudsonUrl;

        private boolean onlyOnFailureAndRecovery;
        private boolean includeURL;

        protected DescriptorImpl() {
            super(TwitterPublisher.class);
            load();
        }

        @Override
        public boolean configure(StaplerRequest req) throws FormException {
            id = req.getParameter(ID_FIELD);
            password = req.getParameter(PASSWORD_FIELD);
            onlyOnFailureAndRecovery = Boolean.parseBoolean(req.getParameter(ONLY_ON_FAILURE));
            includeURL = Boolean.parseBoolean(req.getParameter(INCLUDE_URL_FIELD));
            hudsonUrl = Mailer.DESCRIPTOR.getUrl();
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

        public boolean isIncludeURL() {
            return includeURL;
        }

        public boolean isOnlyOnFailureAndRecovery() {
            return onlyOnFailureAndRecovery;
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            if (hudsonUrl == null) {
                // if Hudson URL is not configured yet, infer some default
                hudsonUrl = Functions.inferHudsonURL(req);
                save();
            }
            return req.bindJSON(clazz, formData);
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