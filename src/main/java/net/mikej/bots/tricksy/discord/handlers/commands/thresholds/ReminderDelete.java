package net.mikej.bots.tricksy.discord.handlers.commands.thresholds;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.mikej.bots.tricksy.discord.handlers.commands.RemindMe;
import net.mikej.bots.tricksy.discord.handlers.commands.RemindMe.Reminder;
import net.mikej.bots.tricksy.discord.handlers.commands.RemindMe.ReminderSaved;
import net.mikej.bots.tricksy.discord.handlers.commands.RemindMe.ReminderThreshold;

import static com.mongodb.client.model.Filters.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReminderDelete extends ReminderThreshold {

    @Override
    public boolean matches(String message) {
        return message.matches("!remindme delete (\\d+)");
    }

    @Override
    public String getHelpText() {
        return "!remindme delete # (hint: !remindme list to get list)";
    }

    @Override
    public Reminder getReminder(String message) {
        return null;
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public boolean isHelperCommand() {
        return true;
    }
    
    @Override
    public void handleComamnd(MessageReceivedEvent event) {
        Pattern pattern = Pattern.compile("!remindme delete (\\d+)");
        Matcher matcher = pattern.matcher(event.getMessage().getContentRaw());
        if (!matcher.find()) return;
        int indexToDelete = Integer.parseInt(matcher.group(1)) - 1;
        List<ReminderSaved> reminders = new ArrayList<>();
        RemindMe.getCollection().find(eq("userId", event.getAuthor().getId())).into(reminders);
        if (indexToDelete < 0 || indexToDelete + 1 > reminders.size()) {
            event.getAuthor().openPrivateChannel().complete().sendMessage("Invalid reminder specified. Try using `!remindme list` first.").queue();
            return;
        }

        ReminderSaved sv = reminders.get(indexToDelete);
        RemindMe.getCollection().deleteOne(eq("_id", sv.get_id()));
        RemindMe.removeScheduledReminder(reminders.get(indexToDelete));

        event.getAuthor().openPrivateChannel().complete().sendMessage(String.format("The following reminder was removed: `%s`", sv.getMessage())).queue();
    }
}