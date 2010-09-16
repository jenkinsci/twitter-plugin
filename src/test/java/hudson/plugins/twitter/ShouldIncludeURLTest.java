package hudson.plugins.twitter;

import hudson.plugins.twitter.TwitterPublisher.DescriptorImpl;

import org.junit.Assert;
import org.jvnet.hudson.test.HudsonTestCase;

public class ShouldIncludeURLTest extends HudsonTestCase {

    public void testTrueInDescriptor() throws Exception {
        DescriptorImpl descriptor = hudson.getDescriptorByType(DescriptorImpl.class);
        descriptor.includeUrl = true;

        TwitterPublisher pub = new TwitterPublisher(null, null);
        Assert.assertTrue(pub.shouldIncludeUrl());

        ReflectionHelper.setField(pub, "includeUrl", Boolean.FALSE);
        Assert.assertFalse(pub.shouldIncludeUrl());

        ReflectionHelper.setField(pub, "includeUrl", Boolean.TRUE);
        Assert.assertTrue(pub.shouldIncludeUrl());
    }

    public void testFalseInDescriptor() throws Exception {
        DescriptorImpl descriptor = hudson.getDescriptorByType(DescriptorImpl.class);
        descriptor.includeUrl = false;

        TwitterPublisher pub = new TwitterPublisher(null, null);

        Assert.assertFalse(pub.shouldIncludeUrl());

        ReflectionHelper.setField(pub, "includeUrl", Boolean.FALSE);
        Assert.assertFalse(pub.shouldIncludeUrl());

        ReflectionHelper.setField(pub, "includeUrl", Boolean.TRUE);
        Assert.assertTrue(pub.shouldIncludeUrl());
    }

}
