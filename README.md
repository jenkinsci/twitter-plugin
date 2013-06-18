# Twitter Plugin for Jenkins

This Jenkins plugin gives the ability to publish tweets for the result of a build.

Full documentation can be seen on the wiki page on the [Jenkins Plugin Wiki Page](https://wiki.jenkins-ci.org/display/JENKINS/Twitter+Plugin).

## Changelog

Although it's on the wiki too, having it here allows us to update it as development is being done.  Once the release is made, the wiki can then be updated.

### Version 0.8 (In Development)

### Version 0.7 (June 18, 2013)
- Updated to Twitter4J 3.0.3
- Upgraded Jenkins dependency to 1.445

### Version 0.6 (September 16, 2010)
- support OAuth. [JENKINS-7365](https://hudson.dev.java.net/issues/show_bug.cgi?id=7365)


### Version 0.4 (October 31, 2009)
- Change build number mark　to "$" from "#".
- Fixed validation of input form.
- Fixed [JENKINS-4476](https://hudson.dev.java.net/issues/show_bug.cgi?id=4476).

### Version 0.3 (October 18, 2009)
- Upgraded to Twitter4J 2.0.10.
- Fixed Maven Project Job can use Twitter Plugin.

### Version 0.2 (July 03, 2008)
- Upgraded to Twitter4J 1.0.4.
- Tweets are now sent asynchronously.
- Can now specify a different username/password per project.
- Tweets can optionally include the build URL.
- Tweets can be restricted to only be sent when a build fails or recovers.

### Version 0.1 (May 13, 2008)
- Initial release
