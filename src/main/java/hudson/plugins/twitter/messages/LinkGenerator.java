package hudson.plugins.twitter.messages;

/**
 * Defines a shortened-link generator.
 * 
 * @author Michael Irwin (mikesir87)
 */
public interface LinkGenerator {

  /**
   * Generate a shortened link for the provided url.
   * @param url The URL to shrink.
   * @return The shrunken URL.
   */
  String getShortenedLink(String url);
  
}
