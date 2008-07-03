package hudson.plugins.twitter;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ShouldIncludeURLTest {

    @Test
    public void testTrueInDescriptor() throws Exception {
        TwitterPublisher pub = new TwitterPublisher(null, null, null, null);

        ReflectionHelper.setField(TwitterPublisher.DESCRIPTOR, "includeUrl", true);
        Assert.assertTrue(pub.shouldIncludeUrl());

        ReflectionHelper.setField(pub, "includeUrl", Boolean.FALSE);
        Assert.assertFalse(pub.shouldIncludeUrl());

        ReflectionHelper.setField(pub, "includeUrl", Boolean.TRUE);
        Assert.assertTrue(pub.shouldIncludeUrl());
    }

    @Test
    public void testFalseInDescriptor() throws Exception {
        TwitterPublisher pub = new TwitterPublisher(null, null, null, null);

        ReflectionHelper.setField(TwitterPublisher.DESCRIPTOR, "includeUrl", false);
        Assert.assertFalse(pub.shouldIncludeUrl());

        ReflectionHelper.setField(pub, "includeUrl", Boolean.FALSE);
        Assert.assertFalse(pub.shouldIncludeUrl());

        ReflectionHelper.setField(pub, "includeUrl", Boolean.TRUE);
        Assert.assertTrue(pub.shouldIncludeUrl());
    }

    @Before
    public void setUp() throws Exception {
        HudsonUtil.init();
    }

    @After
    public void tearDown() throws Exception {
        HudsonUtil.hudson.cleanUp();
        FileUtils.deleteDirectory(HudsonUtil.root);
    }

}
