package hudson.plugins.twitter;

import hudson.Extension;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * UserProperty class which contains a user's twitter id.
 * 
 * @author landir
 */
@ExportedBean(defaultVisibility = 999)
public class UserTwitterProperty extends UserProperty {

    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    private String twitterid;

    public UserTwitterProperty() {
    }

    @DataBoundConstructor
    public UserTwitterProperty(String twitterid) {
        this.twitterid = twitterid;
    }

    public UserPropertyDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Exported
    public User getUser() {
        return user;
    }

    @Exported
    public String getTwitterid() {
        return twitterid;
    }

    public void setTwitterid(String twitterid) {
        this.twitterid = twitterid;
    }

    @Extension
    public static final class DescriptorImpl extends UserPropertyDescriptor {
        public DescriptorImpl() {
            super(UserTwitterProperty.class);
        }

        @Override
        public String getDisplayName() {
            return "Twitter User Name";
        }

        @Override
        public UserTwitterProperty newInstance(StaplerRequest req, JSONObject formData)
                throws hudson.model.Descriptor.FormException {
            if (formData.has("twitterid")) {
                return req.bindJSON(UserTwitterProperty.class, formData);
            } else {
                return new UserTwitterProperty();
            }
        }

        @Override
        public UserProperty newInstance(User user) {
            return null;
        }
    }
}
