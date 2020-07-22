package net.mikej.bots.tricksy.discord.handlers.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.mikej.bots.tricksy.data.MongoContainer;
import net.mikej.bots.tricksy.discord.handlers.CommandHandler;
import net.mikej.bots.tricksy.discord.handlers.internal.MentionHandler;

import static com.mongodb.client.model.Filters.*;

import com.mongodb.client.MongoCollection;

import org.bson.BsonString;
import org.bson.Document;

public class SilenceMentions extends CommandHandler {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.getMessage().getContentRaw().equals("!toggleMentions")) return;

        Document doc = getCollection().find(eq("_id", event.getAuthor().getId())).first();
        if (doc == null) {
            getCollection().insertOne(new Document("_id", new BsonString(event.getAuthor().getId())));
            MentionHandler.addSilencedUser(event.getAuthor().getId());
            event.getAuthor().openPrivateChannel().queue(chan -> {
                chan.sendMessage("Alright! I won't send you notifications about mentions anymore. If you'd like to turn this feature back on, just message me !toggleMentions").queue();
            });
        } else {
            getCollection().findOneAndDelete(eq("_id", event.getAuthor().getId()));
            MentionHandler.removeSilencedUser(event.getAuthor().getId());
            event.getAuthor().openPrivateChannel().queue(chan -> {
                chan.sendMessage("Alright alright alright! I will start sending you notifications about mentions again!").queue();
            });
        }
    }

    private MongoCollection<Document> getCollection() {
        return MongoContainer.getClient().getDatabase("discord-bot").getCollection("pending-mentions-silenced");
    }

    @Override
    public String getCommandName() {
        return "Toggle Mention Notification";
    }

    @Override
    public String getShortHelp() {
        return "Message !toggleMentions to toggle mention notification";
    }

    @Override
    public String getHelp() {
        return "You might have noticed the bot messaging you when you're mentioned whilst offline! If you'd like the bot to stop, just toggle your mention notifications.";
    }

    @Override
    public boolean isPublic() {
        return true;
    }

    @Override
    public int priority() {
        return 1;
    }
    
}