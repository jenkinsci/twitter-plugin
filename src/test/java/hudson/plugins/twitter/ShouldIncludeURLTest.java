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

        ReflectionHelper.setField(TwitterPublisher.DESCRIPTOR, "includeURL", true);
        Assert.assertTrue(pub.shouldIncludeURL());

        ReflectionHelper.setField(pub, "includeURL", Boolean.FALSE);
        Assert.assertFalse(pub.shouldIncludeURL());

        ReflectionHelper.setField(pub, "includeURL", Boolean.TRUE);
        Assert.assertTrue(pub.shouldIncludeURL());
    }

    @Test
    public void testFalseInDescriptor() throws Exception {
        TwitterPublisher pub = new TwitterPublisher(null, null, null, null);

        ReflectionHelper.setField(TwitterPublisher.DESCRIPTOR, "includeURL", false);
        Assert.assertFalse(pub.shouldIncludeURL());

        ReflectionHelper.setField(pub, "includeURL", Boolean.FALSE);
        Assert.assertFalse(pub.shouldIncludeURL());

        ReflectionHelper.setField(pub, "includeURL", Boolean.TRUE);
        Assert.assertTrue(pub.shouldIncludeURL());
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
