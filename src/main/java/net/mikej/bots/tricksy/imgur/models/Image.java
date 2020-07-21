package net.mikej.bots.tricksy.imgur.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Image {
    private final String link;
    
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Image(@JsonProperty("link") final String link) {
        this.link = link;
    }

    public String getLink() { return link; }
}