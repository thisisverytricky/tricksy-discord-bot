package net.mikej.bots.tricksy.discord.handlers.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.mongodb.client.MongoCollection;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;
import org.joda.time.Instant;
import org.joda.time.Seconds;
import org.reflections.Reflections;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.mikej.bots.tricksy.data.MongoContainer;
import net.mikej.bots.tricksy.discord.DiscordClient;
import net.mikej.bots.tricksy.discord.handlers.CommandHandler;

import static com.mongodb.client.model.Filters.*;

public class RemindMe extends CommandHandler {

    private List<ReminderThreshold> reminderThresholds = new ArrayList<>();

    public RemindMe() throws InstantiationException, IllegalAccessException {
        Reflections reflections = new Reflections("net.mikej.bots.tricksy.discord.handlers.commands.thresholds");
        for (Class<? extends ReminderThreshold> adpt : reflections.getSubTypesOf(ReminderThreshold.class)) {
            ReminderThreshold threshold = adpt.newInstance();
            reminderThresholds.add(threshold);
        }

        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        for (ReminderSaved reminder : getCollection().find()) {
            final String message = reminder.getMessage();
            final String messageUrl = reminder.getMessageUrl();
            final User user = DiscordClient.getClient().getUserById(reminder.getUserId());
            
            service.schedule(new Runnable() {

                @Override
                public void run() {
                    user.openPrivateChannel().queue(chan -> {
                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setTitle("Here is your scheduled reminder!");
                        eb.setDescription(
                                message + "\n\n" + "[Message that scheduled me](" + messageUrl + ")");
                        chan.sendMessage(eb.build()).queue();
                        
                        getCollection().findOneAndDelete(eq("messageUrl", messageUrl));
                    });
                }
                
            }, Seconds.secondsBetween(Instant.now(), reminder.instant()).getSeconds(), TimeUnit.SECONDS);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.getMessage().getContentRaw().startsWith("!remindme ")) return;

        for (ReminderThreshold rt : reminderThresholds) {
            if (rt.matches(event.getMessage().getContentRaw())) {
                Reminder reminder = rt.getReminder(event.getMessage().getContentRaw());
                final ReminderSaved rs = rt.schedule(reminder, event.getAuthor(), event.getMessage().getJumpUrl());
                event.getChannel().sendMessage(String.format("Alright! I will remind you `%s` @ `%s`", reminder.getMessage(), reminder.getReminderInstant())).queue();
                getCollection().insertOne(rs);
                return;
            }
        }

        event.getChannel().sendMessage("Invalid remind me conmmand provided! Type `!help` for help.").queue();
    }

    private MongoCollection<ReminderSaved> getCollection() {
        return MongoContainer.getClient().getDatabase("discord-bot").getCollection("reminders", ReminderSaved.class);
    }

    @Override
    public String getCommandName() {
        return "Remind Me";
    }

    @Override
    public String getShortHelp() {
        return String.format("Want a reminder? Type %s", reminderThresholds.get(0).getHelpText());
    }

    @Override
    public String getHelp() {
        String msg = "Currently, the bot supposed the following commands:\n";
        for (ReminderThreshold threshold : reminderThresholds)
            msg += String.format("\n`%s`", threshold.getHelpText());
        return msg;
    }

    @Override
    public boolean isPublic() {
        return true;
    }

    @Override
    public int priority() {
        return 3;
    }
    
    public abstract static class ReminderThreshold 
    {
        public abstract boolean matches(String message);
        public abstract String getHelpText();
        public abstract Reminder getReminder(String message);
        public abstract ReminderSaved schedule(Reminder reminder, User event, String messageUrl);
    }

    public static class Reminder
    {
        private final String message;
        private final Instant reminderInstant;

        public Reminder(final String message, final Instant reminderInstant) {
            this.message = message;
            this.reminderInstant = reminderInstant;
        }

        public String getMessage() { return message; }
        public Instant getReminderInstant() { return reminderInstant; }
    }

    public static class ReminderSaved
    {
        @BsonId
        public ObjectId _id;
        public Instant instant;
        public String userId;
        private String messageUrl;
        private String message;

        public ReminderSaved() {}
        public ReminderSaved(Instant instant, String userId, String messageUrl, String message) {
            this.instant = instant;
            this.userId = userId;
            this.messageUrl = messageUrl;
            this.message = message;
        }

        public ObjectId get_id() { return _id; }
        public long getInstant() { return instant.getMillis(); }
        public String getUserId() { return userId; }
        public String getMessageUrl() { return messageUrl; }
        public String getMessage() { return message; }
        @BsonIgnore
        public Instant instant() { return instant; }

        public void set_id(ObjectId _id) {
            this._id = _id;
        }
        public void setInstant(Long instant) {
            this.instant = Instant.ofEpochMilli(instant);
        }
        public void setUserId(String userId) { 
            this.userId = userId;
        }
        public void setMessageUrl(String messageUrl) {
            this.messageUrl = messageUrl;
        }
        public void setMessage(String message) {
            this.message = message;
        }
    }
}