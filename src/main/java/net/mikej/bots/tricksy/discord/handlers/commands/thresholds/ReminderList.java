package net.mikej.bots.tricksy.discord.handlers.commands.thresholds;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.mikej.bots.tricksy.discord.handlers.commands.RemindMe;
import net.mikej.bots.tricksy.discord.handlers.commands.RemindMe.Reminder;
import net.mikej.bots.tricksy.discord.handlers.commands.RemindMe.ReminderSaved;
import net.mikej.bots.tricksy.discord.handlers.commands.RemindMe.ReminderThreshold;

import static com.mongodb.client.model.Filters.*;

import org.joda.time.DateTime;

public class ReminderList extends ReminderThreshold {

    @Override
    public boolean matches(String message) {
        return message.toLowerCase().equals("!remindme list");
    }

    @Override
    public String getHelpText() {
        return "!remindme list";
    }

    @Override
    public Reminder getReminder(String message) {
        return null;
    }

    @Override
    public int priority() {
        return 99;
    }

    @Override
    public boolean isHelperCommand() {
        return true;
    }
    
    @Override
    public void handleComamnd(MessageReceivedEvent event) {
        String reminderList = "";
        int i = 1;
        for (ReminderSaved rm : RemindMe.getCollection().find(eq("userId", event.getAuthor().getId()))) {
            reminderList += String.format("%s: `%s` - scheduled for `%s`\n\n", i++, rm.getMessage(), new DateTime(rm.getInstant()));
        }
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Your scheduled reminders:");
        eb.setDescription(reminderList);
        event.getAuthor().openPrivateChannel().queue(chan -> {
            chan.sendMessage(eb.build()).queue();
        });
    }
}