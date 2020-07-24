package net.mikej.bots.tricksy.reddit.api.things;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Flair {
  private final UserFlair[] innerFlairs;

  public Flair(@JsonProperty("users") final UserFlair[] innerFlairs) {
    this.innerFlairs = innerFlairs;
  }

  public UserFlair getFlair() {
    return innerFlairs.length == 0 ? null : innerFlairs[0];
  }

  public UserFlair[] getUserFlairs() { return innerFlairs; }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class UserFlair {
    final String flairText;
    final String user;
    final String flairCssClass;
    final String name;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public UserFlair(@JsonProperty("flair_text") final String flairText, @JsonProperty("user") String user,
        @JsonProperty("flair_css_class") String flairCssClass, @JsonProperty("name") final String name) {
      this.flairText = flairText;
      this.user = user;
      this.flairCssClass = flairCssClass;
      this.name = name;
    }

    public String getFlairText() {
      return this.flairText;
    }

    public String getUser() {
      return this.user;
    }

    public String getFlairCssClass() {
      return this.flairCssClass;
    }

    public String getName() { return this.name; }
  }
}