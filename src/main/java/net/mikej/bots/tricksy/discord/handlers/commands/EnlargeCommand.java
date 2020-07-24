package net.mikej.bots.tricksy.discord.handlers.commands;

import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.mikej.bots.tricksy.discord.handlers.CommandHandler;

public class EnlargeCommand extends CommandHandler {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
       if (!event.getMessage().getContentRaw().toLowerCase().startsWith("!enlarge ")) return;

       List<Member> mentionedMembers = event.getMessage().getMentionedMembers();
       for (Member member : mentionedMembers) {
           String avatar = member.getUser().getAvatarUrl();
           EmbedBuilder eb = new EmbedBuilder();
           eb.setImage(avatar);
           event.getChannel().sendMessage(eb.build()).queue();
       }

       List<Emote> emotes = event.getMessage().getEmotes();
       for (Emote emote : emotes) {
           String emoteUrl = emote.getImageUrl();
           EmbedBuilder eb = new EmbedBuilder();
           eb.setImage(emoteUrl);
           event.getChannel().sendMessage(eb.build()).queue();
       }

       event.getMessage().delete().queue();
    }

    @Override
    public String getCommandName() {
        return "Enlarge";
    }

    @Override
    public String getShortHelp() {
        return "Want to enlarge a pfp or emoji? Type !enlarge <thing to enlarge>";
    }

    @Override
    public String getHelp() {
        return "This command will enlarge any members you mention following the command, or any emojis following the command.";
    }

    @Override
    public boolean isPublic() {
        return true;
    }

    @Override
    public int priority() {
        return 4;
    }
    
}