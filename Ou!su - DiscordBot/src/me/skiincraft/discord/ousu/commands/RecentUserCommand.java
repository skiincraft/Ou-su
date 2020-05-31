package me.skiincraft.discord.ousu.commands;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import me.skiincraft.api.ousu.beatmaps.Beatmap;
import me.skiincraft.api.ousu.exceptions.InvalidUserException;
import me.skiincraft.api.ousu.exceptions.NoHistoryException;
import me.skiincraft.api.ousu.modifiers.Approvated;
import me.skiincraft.api.ousu.modifiers.Gamemode;
import me.skiincraft.api.ousu.modifiers.Mods;
import me.skiincraft.api.ousu.scores.Score;
import me.skiincraft.api.ousu.users.User;
import me.skiincraft.discord.ousu.OusuBot;
import me.skiincraft.discord.ousu.customemoji.EmojiCustom;
import me.skiincraft.discord.ousu.customemoji.OsuEmoji;
import me.skiincraft.discord.ousu.embeds.TypeEmbed;
import me.skiincraft.discord.ousu.events.TopUserReaction;
import me.skiincraft.discord.ousu.language.LanguageManager;
import me.skiincraft.discord.ousu.language.LanguageManager.Language;
import me.skiincraft.discord.ousu.manager.CommandCategory;
import me.skiincraft.discord.ousu.manager.Commands;
import me.skiincraft.discord.ousu.mysql.SQLAccess;
import me.skiincraft.discord.ousu.mysql.SQLPlayer;
import me.skiincraft.discord.ousu.utils.Emoji;
import me.skiincraft.discord.ousu.utils.ImageUtils;
import me.skiincraft.discord.ousu.utils.ReactionMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class RecentUserCommand extends Commands {

	public RecentUserCommand() {
		super("ou!", "recent", "ou!recent <nickname> <gamemode>", Arrays.asList("recents"));
	}

	@Override
	public String[] helpMessage(LanguageManager lang) {
		return lang.translatedArrayHelp("OSU_HELPMESSAGE_RECENTUSER");
	}

	@Override
	public CommandCategory categoria() {
		return CommandCategory.Osu;
	}

	public static String linkcover;

	@Override
	public void action(String[] args, String label, TextChannel channel) {
		if (args.length == 0) {
			sendUsage().queue();
			return;
		}

		if (args.length >= 1) {
			List<Score> osuUser;
			try {
				StringBuffer stringArgs = new StringBuffer();
				for (int i = 0; i < args.length; i++) {
					stringArgs.append(args[i] + " ");
				}

				int length = stringArgs.toString().length() - 1;

				String usermsg = stringArgs.toString().substring(0, length);
				String lastmsg = args[args.length - 1];
				String name = usermsg.replace(" " + lastmsg, "");

				if (getEvent().getMessage().getMentionedUsers().size() != 0) {
					String userid = getEvent().getMessage().getMentionedUsers().get(0).getAsMention().replaceAll("\\D+",
							"");

					SQLPlayer sql = new SQLPlayer(OusuBot.getJda().getUserById(userid));
					if (sql.existe()) {
						String nic = sql.get("osu_account");
						name = nic;
						usermsg = nic;
					}
				}

				if (Gamemode.getGamemode(lastmsg) != null) {
					osuUser = OusuBot.getOsu().getRecentUser(name, Gamemode.getGamemode(lastmsg), 5);
				} else {
					osuUser = OusuBot.getOsu().getRecentUser(usermsg, 5);
				}

				@SuppressWarnings("unused")
				Score score = osuUser.get(0);

			} catch (InvalidUserException e) {
				String[] str = getLang().translatedArrayOsuMessages("INEXISTENT_USER");
				StringBuffer buffer = new StringBuffer();
				for (String append : str) {
					if (append != str[0]) {
						buffer.append(EmojiCustom.S_RDiamond.getEmoji() + " " + append);
					}
				}

				sendEmbedMessage(TypeEmbed.WarningEmbed(str[0], buffer.toString())).queue();
				return;
			} catch (NoHistoryException | NullPointerException e) {
				String[] str = getLang().translatedArrayOsuMessages("NO_HAS_HISTORY");
				StringBuffer buffer = new StringBuffer();
				for (String append : str) {
					if (append != str[0]) {
						buffer.append(append);
					}
				}
				sendEmbedMessage(TypeEmbed.SoftWarningEmbed(str[0], buffer.toString())).queue();
				return;
			}
			sendEmbedMessage(TypeEmbed.LoadingEmbed()).queue(message -> {
				me.skiincraft.api.ousu.users.User us = osuUser.get(0).getUser();
				List<EmbedBuilder> emb = new ArrayList<EmbedBuilder>();

				int v = 1;
				for (Score s : osuUser) {
					emb.add(embed(s, new Integer[] { v, osuUser.size() }, us, channel.getGuild()));
					v++;
				}

				EmbedBuilder[] sc = new EmbedBuilder[emb.size()];
				emb.toArray(sc);
				message.editMessage(sc[0].build()).queue(message2 -> {
					message.addReaction("U+25C0").queue();
					// message.addReaction("U+25FC").queue();
					message.addReaction("U+25B6").queue();

					ReactionMessage.recentHistory.add(new TopUserReaction(getUserId(), message.getId(), sc, 0));
				});
			});

			return;
		}
	}

	public static EmbedBuilder embed(Score scorelist, Integer[] order,User user, Guild guild) {
		// "Imports"
		EmbedBuilder embed = new EmbedBuilder();
		Score score = scorelist;
		SQLAccess sql = new SQLAccess(guild);
		LanguageManager lang = new LanguageManager(Language.valueOf(sql.get("language")));
		Beatmap beatmap = score.getBeatmap();

		// Strings
		String inicial = getRankEmote(score);
		String ordem = "[" + order[0].intValue() + "/" + order[1].intValue() + "]";
		String u = "[" + user.getUserName() + "](" + user.getURL() + ")";
		String title = "[" + beatmap.getTitle() + "](" + beatmap.getURL() + ") por `" + beatmap.getArtist() + "`";

		// String notes
		String h300 = OsuEmoji.Hit300.getEmojiString() + ": " + score.get300();
		String h100 = OsuEmoji.Hit100.getEmojiString() + ": " + score.get100();
		String h50 = OsuEmoji.Hit50.getEmojiString() + ": " + score.get50();
		String miss = OsuEmoji.Miss.getEmojiString() + ": " + score.getMiss();
		String l = "\n";
		String field = h300 + l + h100 + l + h50 + l + miss + l;
		int id = beatmap.getBeatmapSetID();
		String url = "https://assets.ppy.sh/beatmaps/" + id + "/covers/cover.jpg?";

		String mods = "";
		for (Mods mod : score.getEnabledMods()) {
			for (Emote emoji : OusuBot.getEmotes()) {
				if (mod.name().toLowerCase().replace("_", "").contains(emoji.getName().toLowerCase())) {
					mods += emoji.getAsMention() + " ";
				}
			}
		}

		// Embed
		embed.setAuthor(user.getUserName());
		embed.setTitle(inicial + " " + lang.translatedEmbeds("TITLE_USER_COMMAND_HISTORY") + " | " + ordem);
		embed.setDescription(OusuBot.getEmote("small_green_diamond").getAsMention() + " "
				+ lang.translatedEmbeds("MESSAGE_RECENTUSER").replace("{USERNAME}", u));

		embed.addField("Beatmap:", Emoji.HEADPHONES.getAsMention() + title, true);
		embed.addField(lang.translatedEmbeds("MAP_STATS"),
				"`" + getApproval(beatmap.getApprovated()) + "`\n" + beatmap.getVersion() + "\n" + mods, true);

		embed.addField(lang.translatedEmbeds("SCORE"), field, true);
		embed.addField(lang.translatedEmbeds("TOTAL_SCORE"), score.getScore() + "", true);
		embed.addField(lang.translatedEmbeds("MAX_COMBO"), score.getMaxCombo() + "/" + score.getBeatmap().getMaxCombo(),
				true);

		linkcover = url;
		embed.setThumbnail(user.getUserAvatar());
		embed.setImage(url);

		String author = beatmap.getCreator();
		embed.setFooter("[" + beatmap.getBeatmapID() + "] " + beatmap.getTitle() + " por " + beatmap.getArtist() + " | "
				+ lang.translatedEmbeds("MAP_CREATED_BY") + author);
		try {
			embed.setColor(ImageUtils.getPredominatColor(ImageIO.read(new URL(beatmap.getBeatmapThumbnailUrl()))));
		} catch (NullPointerException | IOException e) {
			embed.setColor(Color.BLUE);
		}
		return embed;
	}

	public static String getApproval(Approvated approval) {
		Map<Approvated, String> map = new HashMap<>();

		map.put(Approvated.Ranked, "Ranked");
		map.put(Approvated.Qualified, "Qualify");
		map.put(Approvated.Pending, "Pending");
		map.put(Approvated.Approved, "Approvated");
		map.put(Approvated.Loved, "Loved");
		map.put(Approvated.Graveyard, "Graveyard");
		map.put(Approvated.WIP, "WiP");

		if (map.containsKey(approval)) {
			return map.get(approval);
		}

		return "Não classificado";
	}

	public static String getRankEmote(Score score) {
		String rank = score.getRank();
		if (rank.equalsIgnoreCase("SS+")) {
			return OsuEmoji.SSPlus.getEmojiString();
		}
		if (rank.equalsIgnoreCase("SS")) {
			return OsuEmoji.SS.getEmojiString();
		}
		if (rank.equalsIgnoreCase("X")) {
			return OsuEmoji.SS.getEmojiString();
		}
		if (rank.equalsIgnoreCase("S+")) {
			return OsuEmoji.SPlus.getEmojiString();
		}
		if (rank.equalsIgnoreCase("S")) {
			return OsuEmoji.S.getEmojiString();
		}
		if (rank.equalsIgnoreCase("A")) {
			return OsuEmoji.A.getEmojiString();
		}
		if (rank.equalsIgnoreCase("B")) {
			return OsuEmoji.B.getEmojiString();
		}
		if (rank.equalsIgnoreCase("C")) {
			return OsuEmoji.C.getEmojiString();
		}
		if (rank.equalsIgnoreCase("F")) {
			return OsuEmoji.F.getEmojiString();
		}
		return OsuEmoji.OsuLogo.getEmojiString();
	}

}
