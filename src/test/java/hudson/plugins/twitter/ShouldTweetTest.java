package hudson.plugins.twitter;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ShouldTweetTest {

    @Test
    public void testWhenDescriptorTrueAndInstanceNull() throws Exception {
        ReflectionHelper.setField(TwitterPublisher.DESCRIPTOR, "onlyOnFailureOrRecovery", true);

        TwitterPublisher pub = new TwitterPublisher(null, null, null, null);

        assertLimitedTweets(pub);
    }

    @Test
    public void testWhenDescriptorTrueAndInstanceFalse() throws Exception {
        ReflectionHelper.setField(TwitterPublisher.DESCRIPTOR, "onlyOnFailureOrRecovery", true);

        TwitterPublisher pub = new TwitterPublisher(null, null, Boolean.FALSE, null);

        assertAlwaysTweet(pub);
    }

    @Test
    public void testWhenDescriptorTrueAndInstanceTrue() throws Exception {
        ReflectionHelper.setField(TwitterPublisher.DESCRIPTOR, "onlyOnFailureOrRecovery", true);

        TwitterPublisher pub = new TwitterPublisher(null, null, Boolean.TRUE, null);

        assertLimitedTweets(pub);
    }

    @Test
    public void testWhenDescriptorFalseAndInstanceNull() throws Exception {
        ReflectionHelper.setField(TwitterPublisher.DESCRIPTOR, "onlyOnFailureOrRecovery", false);

        TwitterPublisher pub = new TwitterPublisher(null, null, null, null);

        assertAlwaysTweet(pub);
    }

    @Test
    public void testWhenDescriptorFalseAndInstanceFalse() throws Exception {
        ReflectionHelper.setField(TwitterPublisher.DESCRIPTOR, "onlyOnFailureOrRecovery", false);

        TwitterPublisher pub = new TwitterPublisher(null, null, Boolean.FALSE, null);

        assertAlwaysTweet(pub);
    }

    @Test
    public void testWhenDescriptorFalseAndInstanceTrue() throws Exception {
        ReflectionHelper.setField(TwitterPublisher.DESCRIPTOR, "onlyOnFailureOrRecovery", false);

        TwitterPublisher pub = new TwitterPublisher(null, null, Boolean.TRUE, null);

        assertLimitedTweets(pub);
    }

    private void assertAlwaysTweet(TwitterPublisher pub) throws Exception {
        FreeStyleProject prj = createProject();
        FreeStyleBuild successfulBuildAfterNothing = createSuccessfulBuild(prj, null);
        Assert.assertTrue(pub.shouldTweet(successfulBuildAfterNothing));

        FreeStyleBuild successfulBuildAfterSuccess = createSuccessfulBuild(prj,
                successfulBuildAfterNothing);
        Assert.assertTrue(pub.shouldTweet(successfulBuildAfterSuccess));

        FreeStyleBuild successfulBuildAfterFailure = createSuccessfulBuild(prj,
                createFailedBuild(prj));
        Assert.assertTrue(pub.shouldTweet(successfulBuildAfterFailure));

        FreeStyleBuild failedBuild = createFailedBuild(prj);
        Assert.assertTrue(pub.shouldTweet(failedBuild));

        FreeStyleBuild unstableBuild = createUnstableBuild(prj);
        Assert.assertTrue(pub.shouldTweet(unstableBuild));

        FreeStyleBuild abortedBuild = createAbortedBuild(prj);
        Assert.assertTrue(pub.shouldTweet(abortedBuild));
    }

    private void assertLimitedTweets(TwitterPublisher pub) throws Exception {
        FreeStyleProject prj = createProject();
        FreeStyleBuild successfulBuildAfterNothing = createSuccessfulBuild(prj, null);
        Assert.assertFalse(pub.shouldTweet(successfulBuildAfterNothing));

        FreeStyleBuild successfulBuildAfterSuccess = createSuccessfulBuild(prj,
                successfulBuildAfterNothing);
        Assert.assertFalse(pub.shouldTweet(successfulBuildAfterSuccess));

        FreeStyleBuild successfulBuildAfterFailure = createSuccessfulBuild(prj,
                createFailedBuild(prj));
        Assert.assertTrue(pub.shouldTweet(successfulBuildAfterFailure));

        FreeStyleBuild failedBuild = createFailedBuild(prj);
        Assert.assertTrue(pub.shouldTweet(failedBuild));

        FreeStyleBuild unstableBuild = createUnstableBuild(prj);
        Assert.assertTrue(pub.shouldTweet(unstableBuild));

        FreeStyleBuild abortedBuild = createAbortedBuild(prj);
        Assert.assertFalse(pub.shouldTweet(abortedBuild));
    }

    private FreeStyleBuild createFailedBuild(FreeStyleProject prj) throws Exception {
        FreeStyleBuild build = new FreeStyleBuild(prj);
        ReflectionHelper.setField(build, "result", Result.FAILURE);
        return build;
    }

    private FreeStyleBuild createUnstableBuild(FreeStyleProject prj) throws Exception {
        FreeStyleBuild build = new FreeStyleBuild(prj);
        ReflectionHelper.setField(build, "result", Result.UNSTABLE);
        return build;
    }

    private FreeStyleBuild createAbortedBuild(FreeStyleProject prj) throws Exception {
        FreeStyleBuild build = new FreeStyleBuild(prj);
        ReflectionHelper.setField(build, "result", Result.ABORTED);
        return build;
    }

    private FreeStyleBuild createSuccessfulBuild(FreeStyleProject prj, FreeStyleBuild previous)
            throws Exception {
        FreeStyleBuild build = new FreeStyleBuild(prj);
        ReflectionHelper.setField(build, "result", Result.SUCCESS);
        ReflectionHelper.setField(build, "previousBuild", previous);
        return build;
    }

    private FreeStyleProject createProject() {
        return new FreeStyleProject(HudsonUtil.hudson, "test-project");
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
