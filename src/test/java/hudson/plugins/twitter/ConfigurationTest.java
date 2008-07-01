package hudson.plugins.twitter;

import hudson.tasks.BuildStep;
import net.sf.json.JSONObject;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.stapler.StaplerRequest;

public class ConfigurationTest {

    @Test
    public void testPlugin() throws Exception {
        Assert.assertFalse(BuildStep.PUBLISHERS.contains(TwitterPublisher.DESCRIPTOR));
        PluginImpl impl = new PluginImpl();
        impl.start();
        Assert.assertTrue(BuildStep.PUBLISHERS.contains(TwitterPublisher.DESCRIPTOR));
        impl.stop();
        Assert.assertFalse(BuildStep.PUBLISHERS.contains(TwitterPublisher.DESCRIPTOR));

    }

    @Test
    public void testDescriptorConfiguration() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setupAddParameter("twitter-id", "test-user");
        req.setupAddParameter("twitter-password", "test-password");
        req.setupAddParameter("twitter-only-on-failure", "true");
        req.setupAddParameter("twitter-include-url", "true");
        req.setupGetRequestURI("/configure");
        StaplerRequest sreq = HudsonUtil.createStaplerRequest(req);
        TwitterPublisher.DESCRIPTOR.configure(sreq);
        Assert.assertTrue(TwitterPublisher.DESCRIPTOR.isIncludeURL());
        Assert.assertTrue(TwitterPublisher.DESCRIPTOR.isOnlyOnFailureAndRecovery());
        Assert.assertEquals("test-user", TwitterPublisher.DESCRIPTOR.getId());
        Assert.assertEquals("test-password", TwitterPublisher.DESCRIPTOR.getPassword());
    }

    @Test
    public void testPublisherConfigurationWithNulls() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setupGetRequestURI("/job/foo/configure");
        req.setupScheme("http");
        req.setupServerName("testserver");
        req.setupGetLocalPort(80);
        req.setupGetContextPath("");

        StaplerRequest sreq = HudsonUtil.createStaplerRequest(req);

        JSONObject formData = new JSONObject();

        TwitterPublisher pub = (TwitterPublisher) TwitterPublisher.DESCRIPTOR.newInstance(sreq,
                formData);

        Assert.assertNull(pub.getIncludeURL());
        Assert.assertNull(pub.getOnlyOnFailureAndRecovery());
        Assert.assertNull(pub.getId());
        Assert.assertNull(pub.getPassword());
    }

    @Test
    public void testPublisherConfigurationWithRealValues() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setupGetRequestURI("/job/foo/configure");
        req.setupScheme("http");
        req.setupServerName("testserver");
        req.setupGetLocalPort(80);
        req.setupGetContextPath("");

        StaplerRequest sreq = HudsonUtil.createStaplerRequest(req);

        JSONObject formData = new JSONObject();
        formData.put("id", "test-id");
        formData.put("password", "test-password");
        formData.put("onlyOnFailureAndRecovery", "false");
        formData.put("includeURL", "true");

        TwitterPublisher pub = (TwitterPublisher) TwitterPublisher.DESCRIPTOR.newInstance(sreq,
                formData);

        Assert.assertEquals("test-id", pub.getId());
        Assert.assertEquals("test-password", pub.getPassword());
        Assert.assertTrue(pub.getIncludeURL());
        Assert.assertFalse(pub.getOnlyOnFailureAndRecovery());
    }

    @Before
    public void setUp() throws Exception {
        HudsonUtil.init();
    }

    @After
    public void tearDown() throws Exception {
        BuildStep.PUBLISHERS.clear();
        HudsonUtil.hudson.cleanUp();
        FileUtils.deleteDirectory(HudsonUtil.root);
    }

}
