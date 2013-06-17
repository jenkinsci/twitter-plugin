package hudson.plugins.twitter;

import hudson.Extension;
import hudson.Functions;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.tasks.Mailer;

import java.io.IOException;
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
import twitter4j.AsyncTwitterFactory;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.conf.ConfigurationBuilder;

/**
 * @author cactusman
 * @author justinedelson
 * @author mikesir87
 */
public class TwitterPublisher extends Notifier {

	private static final Logger LOGGER = Logger
			.getLogger(TwitterPublisher.class.getName());

	private Boolean onlyOnFailureOrRecovery;
	private Boolean includeUrl;

	private TwitterPublisher(Boolean onlyOnFailureOrRecovery, Boolean includeUrl) {
		this.onlyOnFailureOrRecovery = onlyOnFailureOrRecovery;
		this.includeUrl = includeUrl;
	}

	@DataBoundConstructor
	public TwitterPublisher(String onlyOnFailureOrRecovery, String includeUrl) {
		this(cleanToBoolean(onlyOnFailureOrRecovery),
				cleanToBoolean(includeUrl));
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

	public Boolean getIncludeUrl() {
		return includeUrl;
	}

	public Boolean getOnlyOnFailureOrRecovery() {
		return onlyOnFailureOrRecovery;
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.BUILD;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) {
		if (shouldTweet(build)) {
			try {
				String newStatus = createTwitterStatusMessage(build);
				((DescriptorImpl) getDescriptor()).updateTwit(newStatus);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Unable to send tweet.", e);
			}
		}
		return true;
	}

	private String createTwitterStatusMessage(AbstractBuild<?, ?> build) {
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
		if (shouldIncludeUrl()) {
			String absoluteBuildURL = ((DescriptorImpl) getDescriptor())
					.getUrl() + build.getUrl();
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
				UserTwitterProperty tid = user
						.getProperty(UserTwitterProperty.class);
				if (tid.getTwitterid() != null) {
					userString.append("@").append(tid.getTwitterid())
							.append(" ");
				}
			}
		} else if (changeSet != null) {
			for (Entry entry : changeSet) {
				User user = entry.getAuthor();
				UserTwitterProperty tid = user
						.getProperty(UserTwitterProperty.class);
				if (tid.getTwitterid() != null) {
					userString.append("@").append(tid.getTwitterid())
							.append(" ");
				}
			}
		}
		return userString.toString();
	}

	/**
	 * Determine if this build represents a failure or recovery. A build failure
	 * includes both failed and unstable builds. A recovery is defined as a
	 * successful build that follows a build that was not successful. Always
	 * returns false for aborted builds.
	 * 
	 * @param build
	 *            the Build object
	 * @return true if this build represents a recovery or failure
	 */
	protected boolean isFailureOrRecovery(AbstractBuild<?, ?> build) {
		if (build.getResult() == Result.FAILURE
				|| build.getResult() == Result.UNSTABLE) {
			return true;
		} else if (build.getResult() == Result.SUCCESS) {
			AbstractBuild<?, ?> previousBuild = build.getPreviousBuild();
			if (previousBuild != null
					&& previousBuild.getResult() != Result.SUCCESS) {
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
	 * @param build
	 *            the Build object
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

	@Extension
	public static final class DescriptorImpl extends
			BuildStepDescriptor<Publisher> {
		private static final Logger LOGGER = Logger
				.getLogger(DescriptorImpl.class.getName());

		private static final String CONSUMER_KEY = "8B6nAb0a5QScWxROd5oWA";;
		private static final String CONSUMER_SECRET = 
				"pXO0lgCZYUvix7Ay7YLdsIep38VBiH2cTldOeMj1J5s";

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

		public void updateTwit(String message) throws Exception {
			LOGGER.info("Attempting to update Twitter status to: " + message);

			AsyncTwitterFactory factory = new AsyncTwitterFactory();
			AsyncTwitter twitter = factory.getInstance(new OAuthAuthorization(
					new ConfigurationBuilder()
							.setOAuthConsumerKey(CONSUMER_KEY)
							.setOAuthConsumerSecret(CONSUMER_SECRET)
							.setOAuthAccessToken(token)
							.setOAuthAccessTokenSecret(tokenSecret).build()));
			twitter.addListener(new TwitterAdapter() {
				@Override
				public void onException(TwitterException e, TwitterMethod method) {
					LOGGER.warning("Exception updating Twitter status: "
							+ e.toString());
				}

				@Override
				public void updatedStatus(Status statuses) {
					LOGGER.info("Updated Twitter status: " + statuses.getText());
				}
			});
			twitter.updateStatus(message);
		}

		@Deprecated
		public transient String id;
		@Deprecated
		public transient String password;
	}
}