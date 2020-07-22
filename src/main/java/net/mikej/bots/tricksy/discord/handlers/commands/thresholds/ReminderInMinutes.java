package net.mikej.bots.tricksy.discord.handlers.commands.thresholds;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.client.MongoCollection;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Minutes;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.mikej.bots.tricksy.data.MongoContainer;
import net.mikej.bots.tricksy.discord.handlers.commands.RemindMe.Reminder;
import net.mikej.bots.tricksy.discord.handlers.commands.RemindMe.ReminderSaved;
import net.mikej.bots.tricksy.discord.handlers.commands.RemindMe.ReminderThreshold;

import static com.mongodb.client.model.Filters.*;


public class ReminderInMinutes extends ReminderThreshold {
    private Pattern pattern;

    public ReminderInMinutes() {
        pattern = Pattern.compile("!remindme in (\\d+) minutes? (.+)");
    }

    @Override
    public boolean matches(String message) {
        return pattern.matcher(message).matches();
    }

    @Override
    public String getHelpText() {
        return "!remindme in 10 minutes <MESSAGE>";
    }

    @Override
    public Reminder getReminder(String message) {
        Matcher m = pattern.matcher(message);
        if (!m.find())
            return null;
        int time = Integer.parseInt(m.group(1));
        String reminderMessage = m.group(2);
        return new Reminder(reminderMessage, DateTime.now().plusSeconds(time).toInstant());
    }

    @Override
    public ReminderSaved schedule(final Reminder reminder, final User user, final String messageUrl) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        int seconds = Minutes.minutesBetween(Instant.now(), reminder.getReminderInstant()).getMinutes();
        scheduler.schedule(new Runnable() {

            @Override
            public void run() {
                user.openPrivateChannel().queue(chan -> {
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle("Here is your scheduled reminder!");
                    eb.setDescription(
                            reminder.getMessage() + "\n\n" + "[Message that scheduled me](" + messageUrl + ")");
                    chan.sendMessage(eb.build()).queue();
                    
                    getCollection().findOneAndDelete(eq("messageUrl", messageUrl));
                });
            }

        }, seconds, TimeUnit.MINUTES);
        return new ReminderSaved(reminder.getReminderInstant(), user.getId(), messageUrl, reminder.getMessage());
    }

    private MongoCollection<ReminderSaved> getCollection() {
        return MongoContainer.getClient().getDatabase("discord-bot").getCollection("reminders", ReminderSaved.class);
    }
}