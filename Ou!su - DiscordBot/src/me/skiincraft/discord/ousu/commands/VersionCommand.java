package me.skiincraft.discord.ousu.commands;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Date;

import me.skiincraft.discord.ousu.OusuBot;
import me.skiincraft.discord.ousu.manager.CommandCategory;
import me.skiincraft.discord.ousu.manager.Commands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class VersionCommand extends Commands {

	public VersionCommand() {
		super("ou!", "ver", "ou!ver", Arrays.asList("version"));
	}

	@Override
	public String[] helpMessage() {
		return null;
	}

	@Override
	public CommandCategory categoria() {
		return CommandCategory.Sobre;
	}

	@Override
	public void action(String[] args, User user, TextChannel channel) {
		channel.sendMessage(embed(channel.getGuild()).build()).queue();
		
	}
	
	public EmbedBuilder embed(Guild guild) {
		EmbedBuilder embed = new EmbedBuilder();
		User user = OusuBot.getOusu().getJda().getUserById("247096601242238991");
		SelfUser self = OusuBot.getOusu().getJda().getSelfUser();
		embed.setAuthor(self.getName() + "#" + self.getDiscriminator(), "https://github.com/skiincraft", self.getAvatarUrl());
		embed.setColor(Color.MAGENTA);
		OffsetDateTime data = self.getTimeCreated();
		embed.setDescription("Ou!su é um Discord Bot programado na linguagem de programação Java\n"
				+ "Esta aplicação Java utiliza o OsuAPI disponibilizado oficialmente por ppy.\n\n"
				+ "Criado em " + new SimpleDateFormat("dd/MM/yyyy").format(Date.from(data.toInstant())));
		
		embed.addField("Dependencias", "OsuAPI (oopsjpeg)", true);
		embed.addField("Versão", "1.0.1", true);
		if (guild.isMember(user)) {
			embed.addField("Author", user.getAsMention() + " - [Sknz](https://github.com/skiincraft)", true);
		} else {
			embed.addField("Author", "[Sknz](https://github.com/skiincraft)", true);
		}
		
		embed.setFooter("Ou!su Bot | Todos direitos autorizados", user.getAvatarUrl());
		return embed;	
	}
}
