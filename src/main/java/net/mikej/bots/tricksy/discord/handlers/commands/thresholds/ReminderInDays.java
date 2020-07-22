package net.mikej.bots.tricksy.discord.handlers.commands.thresholds;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;

import net.mikej.bots.tricksy.discord.handlers.commands.RemindMe.Reminder;
import net.mikej.bots.tricksy.discord.handlers.commands.RemindMe.ReminderThreshold;

public class ReminderInDays extends ReminderThreshold {
    private Pattern pattern;

    public ReminderInDays() {
        pattern = Pattern.compile("!remindme in (\\d+|\\d*\\.\\d+|\\d+\\.\\d*) days? (.+)");
    }

    @Override
    public boolean matches(String message) {
        return pattern.matcher(message).matches();
    }

    @Override
    public String getHelpText() {
        return "!remindme in 10 days <MESSAGE>";
    }

    @Override
    public Reminder getReminder(String message) {
        Matcher m = pattern.matcher(message);
        if (!m.find())
            return null;
        double time = Double.parseDouble(m.group(1));
        String reminderMessage = m.group(2);
        return new Reminder(reminderMessage, DateTime.now().plusSeconds((int)(time * 60 * 60 * 24)).toInstant());
    }
}