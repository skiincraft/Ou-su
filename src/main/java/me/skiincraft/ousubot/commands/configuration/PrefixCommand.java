package me.skiincraft.ousubot.commands.configuration;

import me.skiincraft.beans.stereotypes.CommandMap;
import me.skiincraft.ousubot.core.commands.AbstractCommand;
import me.skiincraft.ousubot.view.Messages;
import me.skiincraft.ousubot.view.emotes.GenericsEmotes;
import me.skiincraft.ousucore.OusuCore;
import me.skiincraft.ousucore.command.utils.CommandTools;
import me.skiincraft.ousucore.language.Language;
import me.skiincraft.ousucore.repository.GuildRepository;
import me.skiincraft.ousucore.repository.OusuGuild;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Arrays;

@CommandMap
public class PrefixCommand extends AbstractCommand {

    public PrefixCommand() {
        super("prefix", Arrays.asList("prefixo", "startwith"), "prefix <prefix>");
    }

    public CommandType getCategory() {
        return CommandType.Configuration;
    }

    public void execute(String label, String[] args, CommandTools channel) {
        Language guildLang = Language.getGuildLanguage(channel.getChannel().getGuild());
        if (!channel.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            channel.reply(channel.getMember().getAsMention() + " " + guildLang.getString("command.messages.permission")
                    .replace("{permission}", Permission.MANAGE_SERVER.getName()));
            return;
        }

        if (args.length == 0) {
            replyUsage(channel.getChannel());
            return;
        }

        if (args[0].matches("[a-zA-Z0-9]*")) {
            channel.reply(Messages.getWarning("command.messages.prefix.incorrect_use", channel.getChannel().getGuild()));
            return;
        }
        if (args[0].length() > 3) {
            channel.reply(Messages.getWarning("command.messages.prefix.incorrect_use2", channel.getChannel().getGuild()));
            return;
        }

        changePrefix(args[0], channel.getChannel().getGuild());
        channel.reply(formatSucessful(args[0], guildLang));
    }

    public void changePrefix(String prefix, Guild guild) {
        GuildRepository repository = OusuCore.getGuildRepository();
        OusuGuild ousuGuild = repository.getById(guild.getIdLong()).orElse(new OusuGuild(guild));
        repository.save(ousuGuild.setPrefix(prefix));
    }

    public MessageEmbed formatSucessful(String newPrefix, Language lang) {
        EmbedBuilder embed = new EmbedBuilder();
        String[] str = lang.getStrings("command.messages.prefix.changed");
        GenericsEmotes emotes = OusuCore.getInjector().getInstanceOf(GenericsEmotes.class);
        embed.setTitle(":gear:" + str[0]);
        embed.setAuthor(String.format("'%s' %s", newPrefix, lang.getString("command.messages.prefix.newprefix")));
        embed.setDescription(String.join("\n", Arrays.copyOfRange(str, 1, str.length)));
        embed.setThumbnail(emotes.getEmoteEquals(getCategory().name()).getEmoteUrl());
        embed.addField("Prefix", newPrefix, true);

        return embed.build();
    }

}
