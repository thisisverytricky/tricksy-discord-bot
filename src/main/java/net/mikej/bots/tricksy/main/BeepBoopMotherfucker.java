package net.mikej.bots.tricksy.main;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.entities.Activity;
import net.mikej.bots.tricksy.data.MongoContainer;
import net.mikej.bots.tricksy.discord.DiscordClient;
import net.mikej.bots.tricksy.imgur.ImgurContainer;
import net.mikej.bots.tricksy.reddit.RedditBot;

/**
 * Hello world!
 *
 */
public class BeepBoopMotherfucker {
    public static void main(String[] args)
            throws LoginException, InstantiationException, IllegalAccessException, InterruptedException {
        ImgurContainer.init(System.getenv("imgur_token"));
        MongoContainer.init(String.format(
                "mongodb+srv://%s:%s@cluster-inky-bot-yiios.mongodb.net/%s?retryWrites=true&w=majority&readPreference=primaryPreferred",
                System.getenv("mongodb_username"), System.getenv("mongodb_password"),
                System.getenv("mongodb_collection")));

        DiscordClient.init(System.getenv("discord_token"), Activity.watching("Every Word You Say"));
        DiscordClient.getClient().awaitReady();

        RedditBot.init(System.getenv("reddit_username"), System.getenv("reddit_password"),
                System.getenv("reddit_client_id"), System.getenv("reddit_client_secret"));
        RedditBot.watchNewPosts("pen_swap");
    }
}
