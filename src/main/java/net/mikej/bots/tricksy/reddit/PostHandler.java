package net.mikej.bots.tricksy.reddit;

import net.dean.jraw.models.Submission;

public abstract class PostHandler {
    public abstract boolean handlesSubreddit(String subreddit);
    public abstract void handlePost(Submission post);
    public abstract void init();
}