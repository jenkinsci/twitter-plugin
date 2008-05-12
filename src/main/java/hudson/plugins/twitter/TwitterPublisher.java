package hudson.plugins.twitter;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * @author cactusman
 */
public class TwitterPublisher extends Publisher {

	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	public Descriptor<Publisher> getDescriptor() {
		return DESCRIPTOR;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {
		String projectName = build.getProject().getName();
		String result = build.getResult().toString();
		return DESCRIPTOR.updateTwit(result + ":" + projectName + " #" + build.number);
	}

	public static final class DescriptorImpl extends Descriptor<Publisher>{
		private static final Logger LOGGER = Logger.getLogger(DescriptorImpl.class.getName());
		
		private String id;
		private String password;
		
		private static final String ID_FIELD = "twitter-id";
		private static final String PASSWORD_FIELD = "twitter-password";
		
		protected DescriptorImpl() {
			super(TwitterPublisher.class);
			load();
		}
		
		public String getId() {
			return id;
		}
		public String getPassword() {
			return password;
		}


		@Override
		public boolean configure(StaplerRequest req) throws FormException {
			id = req.getParameter(ID_FIELD);
			password = req.getParameter(PASSWORD_FIELD);
			save();
			return super.configure(req);
		}

		@Override
		public Publisher newInstance(StaplerRequest req, JSONObject formData)
				throws FormException {
			return new TwitterPublisher();
		}

		@Override
		public String getDisplayName() {
			return "Twitter";
		}

		public boolean updateTwit(String mesage){
			Twitter twitter = new Twitter(id, password);
			Status status = null;
			try {
				status = twitter.update(mesage);
			} catch (TwitterException e) {
				LOGGER.warning(e.toString());
			}
			if(status == null){
				return false;
			} else {
				return true;
			}
		}
		
	}
}
