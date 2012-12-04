package hudson.plugins.twitter;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.plugins.twitter.TwitterPublisher.DescriptorImpl;
import hudson.tasks.Builder;

import java.io.IOException;

import org.jvnet.hudson.test.FailureBuilder;
import org.jvnet.hudson.test.HudsonTestCase;

public class ShouldTweetTest extends HudsonTestCase {

    public void testWhenDescriptorTrueAndInstanceNull() throws Exception {
        DescriptorImpl descriptor = hudson.getDescriptorByType(DescriptorImpl.class);
        descriptor.onlyOnFailureOrRecovery = true;

        TwitterPublisher pub = new TwitterPublisher(null, null,null);

        assertLimitedTweets(pub);
    }

    public void testWhenDescriptorTrueAndInstanceFalse() throws Exception {
        DescriptorImpl descriptor = hudson.getDescriptorByType(DescriptorImpl.class);
        descriptor.onlyOnFailureOrRecovery = true;

        TwitterPublisher pub = new TwitterPublisher("false", null, null);

        assertAlwaysTweet(pub);
    }

    public void testWhenDescriptorTrueAndInstanceTrue() throws Exception {
        DescriptorImpl descriptor = hudson.getDescriptorByType(DescriptorImpl.class);
        descriptor.onlyOnFailureOrRecovery = true;

        TwitterPublisher pub = new TwitterPublisher("true", null, null);

        assertLimitedTweets(pub);
    }

    public void testWhenDescriptorFalseAndInstanceNull() throws Exception {
        DescriptorImpl descriptor = hudson.getDescriptorByType(DescriptorImpl.class);
        descriptor.onlyOnFailureOrRecovery = false;

        TwitterPublisher pub = new TwitterPublisher(null, null,null);

        assertAlwaysTweet(pub);
    }

    public void testWhenDescriptorFalseAndInstanceFalse() throws Exception {
        DescriptorImpl descriptor = hudson.getDescriptorByType(DescriptorImpl.class);
        descriptor.onlyOnFailureOrRecovery = false;

        TwitterPublisher pub = new TwitterPublisher("false", null, null);

        assertAlwaysTweet(pub);
    }

    public void testWhenDescriptorFalseAndInstanceTrue() throws Exception {
        DescriptorImpl descriptor = hudson.getDescriptorByType(DescriptorImpl.class);
        descriptor.onlyOnFailureOrRecovery = false;

        TwitterPublisher pub = new TwitterPublisher("true", null, null);

        assertLimitedTweets(pub);
    }

    private void assertAlwaysTweet(TwitterPublisher pub) throws Exception {
        FreeStyleBuild successfulBuildAfterNothing = createSuccessfulBuild();
        assertTrue(pub.shouldTweet(successfulBuildAfterNothing));

        FreeStyleBuild successfulBuildAfterSuccess = createSuccessfulBuildAfterSuccess();
        assertTrue(pub.shouldTweet(successfulBuildAfterSuccess));

        FreeStyleBuild successfulBuildAfterFailure = createSuccessfulBuildAfterFailure();
        assertTrue(pub.shouldTweet(successfulBuildAfterFailure));

        FreeStyleBuild failedBuild = createFailedBuild();
        assertTrue(pub.shouldTweet(failedBuild));

        FreeStyleBuild unstableBuild = createUnstableBuild();
        assertTrue(pub.shouldTweet(unstableBuild));

        FreeStyleBuild abortedBuild = createAbortedBuild();
        assertTrue(pub.shouldTweet(abortedBuild));
    }

    private FreeStyleBuild createSuccessfulBuild() throws Exception {
        FreeStyleProject prj = createFreeStyleProject();
        return prj.scheduleBuild2(0).get();
    }

    private FreeStyleBuild createSuccessfulBuildAfterFailure() throws Exception {
        FreeStyleProject prj = createFreeStyleProject();
        prj.getBuildersList().add(new FailureBuilder());
        prj.scheduleBuild2(0).get();
        prj.getBuildersList().clear();
        return prj.scheduleBuild2(0).get();
    }

    private FreeStyleBuild createSuccessfulBuildAfterSuccess() throws Exception {
        FreeStyleProject prj = createFreeStyleProject();
        prj.scheduleBuild2(0).get();
        return prj.scheduleBuild2(0).get();
    }

    private void assertLimitedTweets(TwitterPublisher pub) throws Exception {
        FreeStyleBuild successfulBuildAfterNothing = createSuccessfulBuild();
        assertFalse(pub.shouldTweet(successfulBuildAfterNothing));

        FreeStyleBuild successfulBuildAfterSuccess = createSuccessfulBuildAfterSuccess();
        assertFalse(pub.shouldTweet(successfulBuildAfterSuccess));

        FreeStyleBuild successfulBuildAfterFailure = createSuccessfulBuildAfterFailure();
        assertTrue(pub.shouldTweet(successfulBuildAfterFailure));

        FreeStyleBuild failedBuild = createFailedBuild();
        assertTrue(pub.shouldTweet(failedBuild));

        FreeStyleBuild unstableBuild = createUnstableBuild();
        assertTrue(pub.shouldTweet(unstableBuild));

        FreeStyleBuild abortedBuild = createAbortedBuild();
        assertFalse(pub.shouldTweet(abortedBuild));
    }

    private FreeStyleBuild createFailedBuild() throws Exception {
        return createBuild(new FailureBuilder());
    }

    private FreeStyleBuild createBuild(Builder b) throws Exception {
        FreeStyleProject prj = createFreeStyleProject();
        prj.getBuildersList().add(b);
        return prj.scheduleBuild2(0).get();
    }

    private FreeStyleBuild createUnstableBuild() throws Exception {
        return createBuild(new UnstableBuilder());
    }

    private FreeStyleBuild createAbortedBuild() throws Exception {
        return createBuild(new AbortedBuilder());
    }

    public static class UnstableBuilder extends Builder {
        public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
                throws InterruptedException, IOException {
            build.setResult(Result.UNSTABLE);
            return false;
        }
    }

    public static class AbortedBuilder extends Builder {
        public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
                throws InterruptedException, IOException {
            build.setResult(Result.ABORTED);
            return false;
        }
    }

}
