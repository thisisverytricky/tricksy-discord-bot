package net.mikej.bots.tricksy.discord.handlers.commands;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Instant;
import org.reflections.Reflections;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.mikej.bots.tricksy.discord.handlers.CommandHandler;

public class RemindMe extends CommandHandler {

    private List<ReminderThreshold> reminderThresholds = new ArrayList<>();

    public RemindMe() throws InstantiationException, IllegalAccessException {
        Reflections reflections = new Reflections("net.mikej.bots.tricksy.discord.handlers.commands.thresholds");
        for (Class<? extends ReminderThreshold> adpt : reflections.getSubTypesOf(ReminderThreshold.class)) {
            ReminderThreshold threshold = adpt.newInstance();
            reminderThresholds.add(threshold);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.getMessage().getContentRaw().startsWith("!remindme ")) return;

        for (ReminderThreshold rt : reminderThresholds) {
            if (rt.matches(event.getMessage().getContentRaw())) {
                Reminder reminder = rt.getReminder(event.getMessage().getContentRaw());
                rt.schedule(reminder, event.getAuthor(), event.getMessage().getJumpUrl());
                event.getChannel().sendMessage(String.format("Alright! I will remind you `%s` @ `%s`", reminder.getMessage(), reminder.getReminderInstant())).queue();
                return;
            }
        }

        event.getChannel().sendMessage("Invalid remind me conmmand provided! Type `!help` for help.").queue();
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
        public abstract void schedule(Reminder reminder, User event, String messageUrl);
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
}