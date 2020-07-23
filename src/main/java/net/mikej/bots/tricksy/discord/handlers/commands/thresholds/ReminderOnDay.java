package net.mikej.bots.tricksy.discord.handlers.commands.thresholds;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import net.mikej.bots.tricksy.discord.handlers.commands.RemindMe.Reminder;
import net.mikej.bots.tricksy.discord.handlers.commands.RemindMe.ReminderThreshold;

public class ReminderOnDay extends ReminderThreshold {
    private Pattern pattern;

    public ReminderOnDay() {
        pattern = Pattern.compile("!remindme on (\\d{4})-(\\d{2})-(\\d{2}) (.+)");
    }

    @Override
    public boolean matches(String message) {
        return pattern.matcher(message).matches();
    }

    @Override
    public String getHelpText() {
        return "!remindme on 2020-01-01 <MESSAGE>";
    }

    @Override
    public Reminder getReminder(String message) {
        Matcher m = pattern.matcher(message);
        if (!m.find())
            return null;
        DateTime date= new DateTime(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), 0, 0, DateTimeZone.UTC);
        String reminderMessage = m.group(4);
        return new Reminder(reminderMessage, date.toInstant());
    }
}