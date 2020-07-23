package net.mikej.bots.tricksy.discord.handlers.internal;

import java.util.LinkedHashMap;
import java.util.List;

import com.mongodb.client.MongoCollection;

import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.mikej.bots.tricksy.data.MongoContainer;
import net.mikej.bots.tricksy.discord.DiscordClient;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

public class PaginatableImageHandler extends ListenerAdapter {

    private static final LinkedHashMap<String, PagableImage> trackedMessages = new LinkedHashMap<>();
    private static final String previousArrow = "\u2B05";
    private static final String nextArrow = "\u27A1";

    public PaginatableImageHandler() {
        for (PagableImage pi : getCollection().find()) {
            trackedMessages.put(pi.getMessageId(), pi);
        }
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.getUser().isBot())
            return;
            
        if (!trackedMessages.containsKey(event.getMessageId()))
            return;

        final String emote = event.getReactionEmote().getEmoji();
        if (!emote.equals(previousArrow) && !emote.equals(nextArrow))
            return;

        final PagableImage pimg = trackedMessages.get(event.getMessageId());
        if (emote.equals(previousArrow))
            pimg.prev();
        else
            pimg.next();

        final User reactionUser = event.getUser();

        DiscordClient.getClient().getGuildById(pimg.getGuildId()).getTextChannelById(pimg.getChannelId()).retrieveMessageById(pimg.getMessageId()).queue(msg -> {
            msg.removeReaction(emote, reactionUser).queue();
            EmbedBuilder eb = new EmbedBuilder(msg.getEmbeds().get(0));
            eb.setFooter(String.format("%s of %s images", pimg.getIndex()+1, pimg.getImages().size()));
            eb.setImage(pimg.getCurrentImage());
            msg.editMessage(eb.build()).queue();
            getCollection().updateOne(eq("_id", pimg.getId()), set("index", pimg.getIndex()));
        });
    }

    public static void registerPagableMessage(EmbedBuilder embedBuilder, Message message, List<String> images) {
        PagableImage pi = new PagableImage(embedBuilder, message, images);
        trackedMessages.put(message.getId(), pi);
        message.addReaction(previousArrow).queue();
        message.addReaction(nextArrow).queue();

        getCollection().insertOne(pi);
    }

    private static MongoCollection<PagableImage> getCollection() {
        return MongoContainer.getClient().getDatabase("discord-bot").getCollection("tracked-paginatable-images",
                PagableImage.class);
    }

    @BsonDiscriminator
    public static class PagableImage {

        @BsonId
        private ObjectId id;
        private int index;
        @BsonIgnore
        private EmbedBuilder embedBuilder;
        @BsonIgnore
        private Message message;
        private String messageId;
        private String channelId;
        private String guildId;
        private List<String> images;

        public PagableImage() {
        }

        public PagableImage(EmbedBuilder embedBuilder, Message message, List<String> images) {
            this.index = 0;
            this.embedBuilder = embedBuilder;
            this.message = message;
            this.images = images;
            this.messageId = message.getId();
            this.channelId = message.getChannel().getId();
            this.guildId = message.getGuild().getId();
        }

        public ObjectId getId() {
            return id;
        }

        public int getIndex() {
            return index;
        }

        @BsonIgnore
        public EmbedBuilder getEmbedBuilder() {
            return embedBuilder;
        }

        @BsonIgnore
        public Message getMessage() {
            return message;
        }

        public String getMessageId() {
            return messageId;
        }

        public String getChannelId() {
            return channelId;
        }

        public String getGuildId() {
            return guildId;
        }

        @BsonIgnore
        public String getCurrentImage() {
            return images.get(index);
        }

        public List<String> getImages() {
            return images;
        }

        public void setId(ObjectId id) {
            this.id = id;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public void setEmbedBuilder(EmbedBuilder embedBuilder) {
            this.embedBuilder = embedBuilder;
        }

        public void setMessage(Message message) {
            this.message = message;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }

        public void setChannelId(String channelId) {
            this.channelId = channelId;
        }

        public void setGuildId(String guildId) {
            this.guildId = guildId;
        }

        public void setImages(List<String> images) {
            this.images = images;
        }

        public void next() {
            if (++index >= images.size())
                index = 0;
        }

        public void prev() {
            if (--index < 0)
                index = images.size() - 1;
        }
    }
}
