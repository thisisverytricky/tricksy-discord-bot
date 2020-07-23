package net.mikej.bots.tricksy.discord.handlers.internal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.mongodb.client.MongoCollection;

import org.bson.Document;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.mikej.bots.tricksy.data.MongoContainer;

import static com.mongodb.client.model.Filters.*;

public class MentionHandler extends ListenerAdapter {

    private final LinkedHashMap<String, List<PendingMention>> pendingMentions = new LinkedHashMap<>();
    private static final List<String> silencedUsers = new ArrayList<>();

    public MentionHandler() {
        for (Document doc : MongoContainer.getClient().getDatabase("discord-bot").getCollection("pending-mentions-silenced").find()) {
            silencedUsers.add(doc.getString("_id"));
        }
    }

    @Override
    public void onReady(ReadyEvent event) {
        for (PendingMention pm : getCollection().find()) {
            if (!pendingMentions.containsKey(pm.getMentionedMemberId()))
                pendingMentions.put(pm.getMentionedMemberId(), new ArrayList<PendingMention>());
            pendingMentions.get(pm.getMentionedMemberId()).add(pm);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.isFromGuild()) return;

        event.getMessage().getMentionedMembers().forEach(m -> {
            if (silencedUsers.contains(m.getUser().getId())) return;
            if (m.getOnlineStatus() != OnlineStatus.OFFLINE && m.getOnlineStatus() != OnlineStatus.IDLE) return;

            PendingMention pm = new PendingMention(event.getMessage().getJumpUrl(), m.getId());
            if (!pendingMentions.containsKey(pm.getMentionedMemberId()))
                pendingMentions.put(pm.getMentionedMemberId(), new ArrayList<PendingMention>());
            pendingMentions.get(pm.getMentionedMemberId()).add(pm);
            getCollection().insertOne(pm);
        });
    }

    @Override
    public void onUserUpdateOnlineStatus(UserUpdateOnlineStatusEvent event) {
        if (!pendingMentions.containsKey(event.getMember().getId())) return;
        if (event.getNewOnlineStatus() != OnlineStatus.ONLINE) return;
        if (silencedUsers.contains(event.getUser().getId())) return;

        PrivateChannel pc = event.getMember().getUser().openPrivateChannel().complete();
        String message = "You were mentioned while offline:\n";
        pc.sendMessage("Your were mentioned while offline:");
        for (PendingMention pm : pendingMentions.get(event.getMember().getId())) {
            message += String.format("* %s\n", pm.getMessageUrl());
            getCollection().deleteOne(eq("_id", pm.getId()));
        }
        message += "If you would like to stop receving mention notifications, just reply with !toggleMentions";
        pc.sendMessage(message).complete();
        pendingMentions.remove(event.getMember().getId());
    }

    public MongoCollection<PendingMention> getCollection() {
        return MongoContainer.getClient().getDatabase("discord-bot").getCollection("pending-mentions", PendingMention.class);
    }

    public static void addSilencedUser(String userId) { silencedUsers.add(userId); }
    public static void removeSilencedUser(String userId) { silencedUsers.remove(userId); }

    @BsonDiscriminator
    public static class PendingMention {
        @BsonId
        private ObjectId id;
        private String messageUrl;
        private String mentionedMemberId;
        
        public ObjectId getId() { return id; }
        public String getMessageUrl() { return messageUrl; }
        public String getMentionedMemberId() { return mentionedMemberId; }

        public void setId(ObjectId id) { this.id = id; }
        public void setMessageUrl(String messageUrl) { this.messageUrl = messageUrl; }
        public void setMentionedMemberId(String mentionedMemberId) { this.mentionedMemberId = mentionedMemberId; }

        public PendingMention() { }
        public PendingMention(String messageUrl, String mentionedMemberId) {
            this.messageUrl = messageUrl;
            this.mentionedMemberId = mentionedMemberId;
        }
    }
}