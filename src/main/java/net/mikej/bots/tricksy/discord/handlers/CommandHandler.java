package net.mikej.bots.tricksy.discord.handlers;

import net.dv8tion.jda.api.hooks.ListenerAdapter;

public abstract class CommandHandler extends ListenerAdapter {
    public abstract String getCommandName();
    public abstract String getShortHelp();
    public abstract String getHelp();
    public abstract boolean isPublic();
    public abstract int priority();
}