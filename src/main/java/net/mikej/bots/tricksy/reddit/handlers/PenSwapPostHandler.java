package net.mikej.bots.tricksy.reddit.handlers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.awt.Color;
import java.io.IOException;

import net.dean.jraw.models.Submission;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.mikej.bots.tricksy.discord.DiscordClient;
import net.mikej.bots.tricksy.discord.handlers.internal.PaginatableImageHandler;
import net.mikej.bots.tricksy.reddit.PostHandler;
import net.mikej.bots.tricksy.services.ImageService;

public class PenSwapPostHandler extends PostHandler {

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

    private List<TextChannel> penSwapChannels;

    @Override
    public boolean handlesSubreddit(String subreddit) {
        return subreddit.toLowerCase().equals("pen_swap");
    }

    @Override
    public void handlePost(Submission post) {
        final EmbedBuilder eb = getEmbed(post);
        final List<String> images = getImages(post.getSelfText());
        if (images.size() > 0) eb.setImage(images.get(0));
        penSwapChannels.forEach(chan -> {
            if (images.size() > 1) {
                PaginatableImageHandler.registerPagableMessage(eb, chan, images);
            } else {
                chan.sendMessage(eb.build()).queue();
            }
        });
    }

    @Override
    public void init() {
        if (DiscordClient.getClient() != null)
            penSwapChannels = DiscordClient.getClient().getTextChannelsByName("pen-swap", true);
        else penSwapChannels = new ArrayList<>();
    }

    public EmbedBuilder getEmbed(Submission post) {
        EmbedBuilder eb = new EmbedBuilder();

        String trades = post.getAuthorFlairText();
        if (trades == null) trades = "Trades: 0";

        eb.setAuthor(String.format("%s - %s", post.getAuthor(), post.getAuthorFlairText()));
        eb.setTitle(post.getTitle(), post.getUrl());
        if (highlightColors.containsKey(post.getLinkFlairText()))
            eb.setColor(Color.decode(highlightColors.get(post.getLinkFlairText())));

        return eb;
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

}