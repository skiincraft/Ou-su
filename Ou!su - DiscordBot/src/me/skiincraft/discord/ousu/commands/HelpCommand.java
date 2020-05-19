package me.skiincraft.discord.ousu.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.skiincraft.discord.ousu.OusuBot;
import me.skiincraft.discord.ousu.customemoji.OsuEmoji;
import me.skiincraft.discord.ousu.embedtypes.DefaultEmbed;
import me.skiincraft.discord.ousu.language.LanguageManager;
import me.skiincraft.discord.ousu.manager.CommandCategory;
import me.skiincraft.discord.ousu.manager.Commands;
import me.skiincraft.discord.ousu.mysql.SQLAccess;
import me.skiincraft.discord.ousu.utils.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class HelpCommand extends Commands {

	public HelpCommand() {
		super("ou!", "help");
	}

	public static List<Commands> commands = new ArrayList<Commands>();

	@Override
	public String[] helpMessage(LanguageManager lang) {
		return lang.translatedArrayHelp("HELPMESSAGE_HELP");
	}

	@Override
	public CommandCategory categoria() {
		return CommandCategory.Ajuda;
	}

	@Override
	public void action(String[] args, String label, User user, TextChannel channel) {
		if (args.length == 0) {
			sendEmbedMessage(embed(channel.getGuild())).queue();
			return;
		}

		if (args.length == 1) {
			System.out.println(args[0]);
			sendEmbedMessage(emb(args[0], channel.getGuild())).queue();
			return;
		}

		return;
	}

	public EmbedBuilder emb(String comando, Guild guild) {
		EmbedBuilder embed = new EmbedBuilder();

		for (Commands com : commands) {
			if (comando.equalsIgnoreCase(com.getCommand())) {
				embed.setTitle("Help <" + com.getCommand() + ">");
				if (com.helpMessage(getLang()) != null) {
					embed.setDescription(String.join("\n", com.helpMessage(getLang())));
				} else {
					embed.setDescription(getLang().translatedHelp("NO_COMMAND_DESCRIPTION"));
				}

				if (com.hasAliases()) {
					String[] alias = com.getAliases().stream().toArray(String[]::new);
					embed.addField("Aliases", String.join("\n", alias), true);
				}

				embed.addField("Usage", com.getUsage().replace("ou!", new SQLAccess(guild).get("prefix")), true);
				return embed;
			}
		}

		String[] msg = getLang().translatedArrayMessages("INEXISTENT_COMMAND_HELP");

		return new DefaultEmbed(OsuEmoji.ThinkAnime.getEmojiString() + msg[0], StringUtils.commandMessage(msg))
				.construirEmbed();
	}

	public EmbedBuilder embed(Guild guild) {
		EmbedBuilder embed = new EmbedBuilder();

		StringBuffer a = new StringBuffer();
		StringBuffer b = new StringBuffer();
		StringBuffer c = new StringBuffer();
		StringBuffer d = new StringBuffer();
		StringBuffer e = new StringBuffer();

		for (int i = 0; i < commands.size(); i++) {
			Commands comando = commands.get(i);
			String prefix = new SQLAccess(guild).get("prefix");

			if (comando.getCategoria() == CommandCategory.Administração) {
				a.append("- " + prefix + comando.getCommand());
				a.append(",");
			}
			if (comando.getCategoria() == CommandCategory.Ajuda) {
				b.append("- " + prefix + comando.getCommand());
				b.append(",");
			}
			if (comando.getCategoria() == CommandCategory.Osu) {
				c.append("- " + prefix + comando.getCommand());
				c.append(",");
			}
			if (comando.getCategoria() == CommandCategory.Sobre) {
				d.append("- " + prefix + comando.getCommand());
				d.append(",");
			}
			if (comando.getCategoria() == CommandCategory.Utilidade) {
				e.append("- " + prefix + comando.getCommand());
				e.append(",");
			}
		}

		String[] Adm = a.toString().split(",");
		Arrays.sort(Adm);
		String[] Ajuda = b.toString().split(",");
		Arrays.sort(Ajuda);
		String[] Osu = c.toString().split(",");
		Arrays.sort(Osu);
		String[] Sobre = d.toString().split(",");
		Arrays.sort(Sobre);
		String[] Util = e.toString().split(",");
		Arrays.sort(Util);

		String[] str = getLang().translatedArrayMessages("HELP_COMMAND_MESSAGE");

		// embed.setTitle(str[0]);
		String gif = "https://cdn.discordapp.com/attachments/710231271623753738/710231428037869568/3dgifmaker25.gif";
		embed.setAuthor("", null, gif);
		embed.setTitle(str[0]);
		embed.setThumbnail(OusuBot.getSelfUser().getAvatarUrl());
		StringBuffer buffer = new StringBuffer();
		for (String append : str) {
			if (append != str[0]) {
				buffer.append(append + "\n");
			}
		}

		embed.setDescription(buffer.toString());

		embed.addField(":computer: **" + CommandCategory.Administração.getCategoria(getLanguage()) + "**",
				"`" + String.join("\n", Adm) + "`", true);
		embed.addField(":question: **" + CommandCategory.Ajuda.getCategoria(getLanguage()) + "**",
				"`" + String.join("\n", Ajuda) + "`", true);
		embed.addField(
				OsuEmoji.OsuLogo.getEmojiString() + " **" + CommandCategory.Osu.getCategoria(getLanguage()) + "**",
				"`" + String.join("\n", Osu) + "`", true);
		// embed.addField("**" + CommandCategory.Utilidade.getCategoria(getLanguage()) +
		// "**", "`" + String.join("\n", Util) + "`", true);
		embed.addField(":bulb: **" + CommandCategory.Sobre.getCategoria(getLanguage()) + "**",
				"`" + String.join("\n", Sobre) + "`", true);

		User user = OusuBot.getJda().getUserById("247096601242238991");
		embed.setFooter(user.getName() + "#" + user.getDiscriminator() + " | Ou!su bot ™", user.getAvatarUrl());
		return embed;
	}
}
