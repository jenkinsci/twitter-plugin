package hudson.plugins.twitter;

import hudson.tasks.BuildStep;
import net.sf.json.JSONObject;

import org.apache.commons.collections.IteratorUtils;
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
    public void testDescriptorConfigurationBooleansTrue() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setupAddParameter("twitter.id", "test-user");
        req.setupAddParameter("twitter.password", "test-password");
        req.setupAddParameter("twitter.onlyOnFailureOrRecovery", "on");
        req.setupAddParameter("twitter.includeUrl", "on");
        req.setupGetParameterNames(IteratorUtils.asEnumeration(IteratorUtils
                .arrayIterator(new String[] { "twitter.id", "twitter.password",
                        "twitter.includeUrl", "twitter.onlyOnFailureOrRecovery" })));
        req.setupGetRequestURI("/configure");
        StaplerRequest sreq = HudsonUtil.createStaplerRequest(req);
        TwitterPublisher.DESCRIPTOR.configure(sreq);
        Assert.assertEquals("test-user", TwitterPublisher.DESCRIPTOR.getId());
        Assert.assertEquals("test-password", TwitterPublisher.DESCRIPTOR.getPassword());
        Assert.assertTrue(TwitterPublisher.DESCRIPTOR.isIncludeUrl());
        Assert.assertTrue(TwitterPublisher.DESCRIPTOR.isOnlyOnFailureOrRecovery());
    }

    @Test
    public void testDescriptorConfigurationBooleansFalse() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setupAddParameter("twitter.id", "test-user");
        req.setupAddParameter("twitter.password", "test-password");
        req.setupGetParameterNames(IteratorUtils.asEnumeration(IteratorUtils
                .arrayIterator(new String[] { "twitter.id", "twitter.password" })));
        req.setupGetRequestURI("/configure");
        StaplerRequest sreq = HudsonUtil.createStaplerRequest(req);
        TwitterPublisher.DESCRIPTOR.configure(sreq);
        Assert.assertEquals("test-user", TwitterPublisher.DESCRIPTOR.getId());
        Assert.assertEquals("test-password", TwitterPublisher.DESCRIPTOR.getPassword());
        Assert.assertFalse(TwitterPublisher.DESCRIPTOR.isIncludeUrl());
        Assert.assertFalse(TwitterPublisher.DESCRIPTOR.isOnlyOnFailureOrRecovery());
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

        Assert.assertNull(pub.getIncludeUrl());
        Assert.assertNull(pub.getOnlyOnFailureOrRecovery());
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
        formData.put("onlyOnFailureOrRecovery", false);
        formData.put("includeUrl", true);

        TwitterPublisher pub = (TwitterPublisher) TwitterPublisher.DESCRIPTOR.newInstance(sreq,
                formData);

        Assert.assertEquals("test-id", pub.getId());
        Assert.assertEquals("test-password", pub.getPassword());
        Assert.assertNotNull(pub.getIncludeUrl());
        Assert.assertNotNull(pub.getOnlyOnFailureOrRecovery());
        Assert.assertTrue(pub.getIncludeUrl());
        Assert.assertFalse(pub.getOnlyOnFailureOrRecovery());
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
