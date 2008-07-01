package hudson.plugins.twitter;

import hudson.Plugin;
import hudson.tasks.BuildStep;

/**
 * @plugin
 * @author cactusman
 * @author justinedelson
 */
public class PluginImpl extends Plugin {
    public void start() throws Exception {
        BuildStep.PUBLISHERS.addNotifier(TwitterPublisher.DESCRIPTOR);
    }

    @Override
    public void stop() throws Exception {
        BuildStep.PUBLISHERS.remove(TwitterPublisher.DESCRIPTOR);
    }
}
