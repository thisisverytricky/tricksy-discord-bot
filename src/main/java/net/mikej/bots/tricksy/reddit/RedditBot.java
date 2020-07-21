package net.mikej.bots.tricksy.reddit;

import java.awt.Color;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.pagination.DefaultPaginator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.mikej.bots.tricksy.discord.DiscordClient;
import net.mikej.bots.tricksy.discord.handlers.internal.PaginatableImageHandler;
import net.mikej.bots.tricksy.services.ImageService;

public class RedditBot implements Runnable {
    private List<String> seenPosts = new ArrayList<>();
    private List<TextChannel> penSwapChannels;
    private RedditClient _client;
    private final Pattern urlPattern = Pattern.compile("(?:\\(|\\b)(http[^\\)\\s]+)(?:\\)|\\b)");
    private final LinkedHashMap<String, String> highlightColors = new LinkedHashMap<String, String>() {
        private static final long serialVersionUID = 6629892383458502076L;

        {
            put("WTS-OPEN", "#00ff00");
            put("WTT-OPEN", "#e403f2");
            put("WTB-OPEN", "#ffc0cb");
            put("OPEN - Reduced!", "#82cafa");
            put("GIVEAWAY", "#ffb000");
        }
    };

    public RedditBot(String username, String password, String clientId, String clientSecret) {
        Credentials oauthCreds = Credentials.script(username, password, clientId, clientSecret);
        UserAgent userAgent = new UserAgent("bot", "net.mikej.bots.tricksy", "1.0.0", "thisisverytricky");
        _client = OAuthHelper.automatic(new OkHttpNetworkAdapter(userAgent), oauthCreds);

        penSwapChannels = DiscordClient.getClient().getTextChannelsByName("pen-swap", true);

        init();
        ScheduledExecutorService schedular = Executors.newScheduledThreadPool(1);
        schedular.scheduleAtFixedRate(this, 0, 2, TimeUnit.SECONDS);
    }

    public static RedditBot init(String username, String password, String clientId, String clientSecret) {
        return new RedditBot(username, password, clientId, clientSecret);
    }

    private void init() {
        DefaultPaginator<Submission> newPosts = getNewPosts();
        for (Submission post : newPosts.next()) {
            seenPosts.add(post.getFullName());
        }
    }

    public DefaultPaginator<Submission> getNewPosts() {
        return _client.subreddit("pen_swap").posts().sorting(SubredditSort.NEW).limit(100).build();
    }

    @Override
    public void run() {
        try {
            for (Submission post : getNewPosts().next()) {
                if (seenPosts.contains(post.getFullName())) continue;
                seenPosts.add(post.getFullName());

                final EmbedBuilder eb = getEmbed(post);
                final List<String> images = getImages(post.getSelfText());
                if (images.size() > 0) eb.setImage(images.get(0));
                penSwapChannels.forEach(chan -> {
                    chan.sendMessage(eb.build()).queue(msg -> {
                        if (images.size() > 1) {
                            PaginatableImageHandler.registerPagableMessage(eb, msg, images);
                        }
                    });
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public List<String> getImages(String text) {
        List<String> imageUrls = new ArrayList<>();

        List<String> urls = new ArrayList<>();
        Matcher matcher = urlPattern.matcher(text);
        while (matcher.find()) {
            urls.add(matcher.group(1));
        }

        urls.forEach(url -> {
            try {
                imageUrls.addAll(ImageService.getImages(url));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return imageUrls;
    }

    public EmbedBuilder getEmbed(Submission post) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setAuthor(String.format("%s - %s", post.getAuthor(), post.getAuthorFlairText()));
        eb.setTitle(post.getTitle(), post.getUrl());
        if (highlightColors.containsKey(post.getLinkFlairText()))
            eb.setColor(Color.decode(highlightColors.get(post.getLinkFlairText())));

        return eb;
    }
}