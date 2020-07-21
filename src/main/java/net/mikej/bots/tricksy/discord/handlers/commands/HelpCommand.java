package net.mikej.bots.tricksy.discord.handlers.commands;

import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.mikej.bots.tricksy.discord.handlers.CommandHandler;

public class HelpCommand extends ListenerAdapter {
    private List<CommandHandler> _commands;

    public HelpCommand(List<CommandHandler> commands) {
        _commands = commands;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        super.onMessageReceived(event);

        if (event.getAuthor().isBot()) return;
        if (event.getMessage().getContentRaw().toLowerCase().equals("!help")) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Available Commands");

            String message = "Type !help # for more detailed information \n\n";
            for (int i = 0; i < _commands.size(); i++) {
                CommandHandler command = _commands.get(i);
                message += "**" + (i + 1) + "**: _**" + command.getCommandName() + "**_: " + command.getShortHelp() + "\n";
            }
            embed.setDescription(message);
            event.getChannel().sendMessage(embed.build()).complete();
        } else if (event.getMessage().getContentRaw().toLowerCase().startsWith("!help")) {
            String[] parts = event.getMessage().getContentRaw().split(" ");
            if (parts.length > 2) return;
            try {
                int index = Integer.parseUnsignedInt(parts[1]) - 1;
                CommandHandler command = _commands.get(index);
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(command.getCommandName());
                embed.setDescription(command.getHelp());
                event.getChannel().sendMessage(embed.build()).complete();
            } catch(Exception ex) {
                event.getChannel().sendMessage("Invalid command specified").complete();
            }
        }
    }
}