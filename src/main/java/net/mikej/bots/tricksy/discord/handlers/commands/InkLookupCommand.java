package net.mikej.bots.tricksy.discord.handlers.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;

import org.bson.BsonArray;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.conversions.Bson;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.mikej.bots.tricksy.data.MongoContainer;
import net.mikej.bots.tricksy.discord.handlers.CommandHandler;
import net.mikej.bots.tricksy.discord.handlers.internal.PaginatableImageHandler;
import net.mikej.bots.tricksy.models.Ink;
import net.mikej.bots.tricksy.services.ImageService;

public class InkLookupCommand extends CommandHandler {

    private static String inkPattern = "\\[\\[([^\\]]+)\\]\\]"; // "\\!\\{([^\\}]+)\\}"; // 

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot())
            return;

        Matcher m = Pattern.compile(inkPattern).matcher(event.getMessage().getContentRaw());
        while (m.find()) {
            try {
                List<String> inkUrls = new ArrayList<>();
                Ink ink = getInk(m.group(1));
                inkUrls.addAll(ImageService.getImages(ink.getPrimaryImage()));

                if (ink.getAlternateImages() != null) {
                    ink.getAlternateImages().forEach(alternateImg -> {
                        try {
                            inkUrls.addAll(ImageService.getImages(alternateImg));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }

                if (inkUrls.size() == 0)
                    continue;

                final EmbedBuilder eb = new EmbedBuilder();
                eb.setImage(inkUrls.get(0));
                if (ink.getSubmittedBy() != null)
                    eb.setAuthor(String.format("submitted by %s", ink.getSubmittedBy()));
                if (ink.getReviewLink() != null)
                    eb.setDescription(String.format("[Read Review](%s)", ink.getReviewLink()));
                eb.setTitle(ink.getFullName(), ink.getPrimaryImage());

                event.getChannel().sendMessage(eb.build()).queue(message -> {
                    if (inkUrls.size() > 1)
                        PaginatableImageHandler.registerPagableMessage(eb, message, inkUrls);
                });

            } catch (Exception ex) {

            }
        }
    }

    private Ink getInk(String searchTerm) {
        MongoCollection<Ink> inkCollection = MongoContainer.getClient().getDatabase("inky_bot").getCollection("inks",
                Ink.class);
        BsonDocument search = new BsonDocument("$search",
                new BsonDocument("phrase", new BsonDocument("query", new BsonString(searchTerm))
                        .append("path",
                                new BsonArray(
                                        Arrays.asList(new BsonString("fullName"), new BsonString("alternateNames"))))
                        .append("slop", new BsonInt32(3))));

        Bson match = Aggregates.match(new BsonDocument("approved", new BsonBoolean(true)));
        Bson limit = Aggregates.limit(1);

        List<Ink> results = inkCollection.aggregate(Arrays.asList(search, match, limit)).into(new ArrayList<>());
        return results.size() > 0 ? results.get(0) : null;
    }

    @Override
    public String getCommandName() {
        return "Ink Swatch Search";
    }

    @Override
    public String getShortHelp() {
        return "Type {INK NAME} to search for an ink swatch";
    }

    @Override
    public String getHelp() {
        return "If you'd like to search for an ink swatch, for eg Diamine Pink Champagne, you can type {Pink Champagne}. The bot will ignore the precise order of the words which make up the ink name, but spelling does matter! If the bot finds no matches, it will not respond.\n\nYou may specify multiple inks in a single message. For example, I like {Diammine Oxblood} for my red ink and {Carbon Black} for my day-to-day.";
    }

    @Override
    public boolean isPublic() {
        return true;
    }
}