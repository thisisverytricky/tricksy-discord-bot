package net.mikej.bots.tricksy.discord.handlers.commands.thresholds;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Seconds;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.mikej.bots.tricksy.discord.handlers.commands.RemindMe.Reminder;
import net.mikej.bots.tricksy.discord.handlers.commands.RemindMe.ReminderThreshold;

public class ReminderInSeconds extends ReminderThreshold {
    private Pattern pattern;

    public ReminderInSeconds() {
        pattern = Pattern.compile("!remindme in (\\d+) seconds? (.+)");
    }

    @Override
    public boolean matches(String message) {
        return pattern.matcher(message).matches();
    }

    @Override
    public String getHelpText() {
        return "!remindme in 10 seconds <MESSAGE>";
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
    public void schedule(final Reminder reminder, final User user, final String messageUrl) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        int seconds = Seconds.secondsBetween(Instant.now(), reminder.getReminderInstant()).getSeconds();
        scheduler.schedule(new Runnable() {

            @Override
            public void run() {
                user.openPrivateChannel().queue(chan -> {
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle("Here is your scheduled reminder!");
                    eb.setDescription(reminder.getMessage() + "\n\n" + "[Message that scheduled me](" + messageUrl + ")");
                    chan.sendMessage(eb.build()).queue();
                });
            }

        }, seconds, TimeUnit.SECONDS);
    }
}