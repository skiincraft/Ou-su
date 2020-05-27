package me.skiincraft.discord.ousu.commands;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import me.skiincraft.api.ousu.modifiers.Gamemode;
import me.skiincraft.discord.ousu.OusuBot;
import me.skiincraft.discord.ousu.embeds.TypeEmbed;
import me.skiincraft.discord.ousu.events.TopUserReaction;
import me.skiincraft.discord.ousu.language.LanguageManager;
import me.skiincraft.discord.ousu.manager.CommandCategory;
import me.skiincraft.discord.ousu.manager.Commands;
import me.skiincraft.discord.ousu.osuskins.OsuSkin;
import me.skiincraft.discord.ousu.search.OsuSearchGetter;
import me.skiincraft.discord.ousu.utils.Emoji;
import me.skiincraft.discord.ousu.utils.ReactionMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class SkinsCommand extends Commands {

	public SkinsCommand() {
		super("ou!", "skins", "ou!skins", null);
	}

	@Override
	public String[] helpMessage(LanguageManager langm) {
		return langm.translatedArrayHelp("OSU_HELPMESSAGE_SKINS");
	}

	@Override
	public CommandCategory categoria() {
		return CommandCategory.Osu;
	}

	@Override
	public void action(String[] args, String label, User user, TextChannel channel) {
			sendEmbedMessage(TypeEmbed.LoadingEmbed()).queue(new Consumer<Message>() {

				@Override
				public void accept(Message msg) {
					List<OsuSkin> skins;
					try {
						skins = OsuSearchGetter.pageskins();
						List<EmbedBuilder> embeds = new ArrayList<EmbedBuilder>();
						
						for (OsuSkin osu: skins) {
							embeds.add(embed(osu));
						}
						EmbedBuilder[] embed = new EmbedBuilder[embeds.size()];
						embeds.toArray(embed);
						msg.editMessage(embeds.get(0).build()).queue( new Consumer<Message>() {

							@Override
							public void accept(Message t) {
								t.addReaction("U+25C0").queue();
								t.addReaction("U+25B6").queue();
								ReactionMessage.skinsReaction.add(new TopUserReaction(user, t.getId(), embed, 0));
								
							}
						});
						
					} catch (IOException e) {
						//e.printStackTrace();
						msg.delete().queue();
					}					
				}
			});
	}
	
	public EmbedBuilder embed(OsuSkin osu) {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle(osu.getSkinname());
		embed.setImage(osu.getSkinimage());
		embed.addField("Skin", osu.getSkinname(), true);
		embed.addField(getLang().translatedEmbeds("CREATOR"), osu.getCreator(), true);
		embed.addField("Download",OusuBot.getEmoteAsMention("download") + "[__Here__](" + osu.getDownloadurl()+ ")", true);
		StringBuffer gamemodes = new StringBuffer();
		for (Gamemode mode : osu.getGamemodes()) {
			gamemodes.append(OusuBot.getEmote(mode.name().toLowerCase()).getAsMention() + " " + mode.getDisplayName() + "\n");
		}
		embed.addField("Gamemodes", gamemodes.toString(), true);
		embed.setDescription(Emoji.EYE.getAsMention()+ " " + osu.getStatistics().getViewes() + " " +
				OusuBot.getEmoteAsMention("download")+ " " + osu.getStatistics().getDownloads() + " " +
				Emoji.CLOUD.getAsMention()+ " " + osu.getStatistics().getComments());
		
		embed.setColor(Color.ORANGE);
		embed.setThumbnail("https://i.imgur.com/bz1MKtv.jpg");
		embed.setFooter("Skins by osuskins.net", "https://osuskins.net/favicon-32x32.png");
		
		return embed;
	}
	
}