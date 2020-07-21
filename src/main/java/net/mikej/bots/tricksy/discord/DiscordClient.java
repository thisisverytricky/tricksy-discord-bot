package net.mikej.bots.tricksy.discord;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.security.auth.login.LoginException;

import org.reflections.Reflections;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.mikej.bots.tricksy.discord.handlers.CommandHandler;
import net.mikej.bots.tricksy.discord.handlers.commands.HelpCommand;

public class DiscordClient {
    private static JDA _client;

    public static void init(String token, Activity activity) throws LoginException, InstantiationException,
            IllegalAccessException {
        _client = JDABuilder
            .createDefault(token)
            .enableIntents(GatewayIntent.GUILD_MEMBERS)
            .enableIntents(GatewayIntent.GUILD_PRESENCES)
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .setActivity(activity)
            .build();
        
        registerCommands();
        registerListeners();
    }

    private static void registerCommands() throws InstantiationException, IllegalAccessException {
        List<CommandHandler> handlers = new ArrayList<>();
        Reflections reflections = new Reflections("net.mikej.bots.tricksy.discord.handlers.commands");    
        Set<Class<? extends CommandHandler>> classes = reflections.getSubTypesOf(CommandHandler.class);
        for (Class<? extends CommandHandler> adpt : classes) {
            CommandHandler adapter = adpt.newInstance();
            _client.addEventListener(adapter);
            if (adapter.isPublic()) handlers.add(adapter);
        }
        _client.addEventListener(new HelpCommand(handlers));
    }

    private static void registerListeners() throws InstantiationException, IllegalAccessException {
        Reflections reflections = new Reflections("net.mikej.bots.tricksy.discord.handlers.internal");    
        Set<Class<? extends ListenerAdapter>> classes = reflections.getSubTypesOf(ListenerAdapter.class);
        for (Class<? extends ListenerAdapter> adpt : classes) {
            if (adpt.isAssignableFrom(CommandHandler.class)) continue;
            _client.addEventListener(adpt.newInstance());
        }
    }

    public static JDA getClient() {
        return _client;
    }
}