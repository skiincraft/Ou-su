package me.skiincraft.discord.ousu.commands;

import java.awt.Color;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import com.oopsjpeg.osu4j.ApprovalState;
import com.oopsjpeg.osu4j.GameMode;
import com.oopsjpeg.osu4j.OsuBeatmap;
import com.oopsjpeg.osu4j.exception.OsuAPIException;

import me.skiincraft.discord.ousu.customemoji.OsuEmoji;
import me.skiincraft.discord.ousu.events.ReactionUtils;
import me.skiincraft.discord.ousu.manager.CommandCategory;
import me.skiincraft.discord.ousu.manager.Commands;
import me.skiincraft.discord.ousu.osu.ScoreType;
import me.skiincraft.discord.ousu.osu.UserOsu;
import me.skiincraft.discord.ousu.osu.UserScores;
import me.skiincraft.discord.ousu.utils.DefaultEmbed;
import me.skiincraft.discord.ousu.utils.ReactionMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class RecentUserCommand extends Commands {

	public RecentUserCommand() {
		super("ou!", "top", "ou!top <nickname> <gamemode>", Arrays.asList("topmaps"));
	}
	
	@Override
	public String[] helpMessage() {
		return new String[] {};
	}
	
	@Override
	public CommandCategory categoria() {
		return CommandCategory.Osu;
	}
	
	public static String linkcover;

	@Override
	public void action(String[] args, User user, TextChannel channel) {
		if (args.length == 1) {
			sendUsage().queue();
			return;
		}
		
		if (args.length >= 2) {
			
			UserOsu osuUser;
			try {
				String bc = "";
				for (int i = 1; i < args.length; i++) {
					bc = bc + args[i] + " ";
				}
				bc = bc.substring(0, bc.length()-1);
				osuUser = new UserOsu(bc, GameMode.STANDARD);
			} catch (MalformedURLException | OsuAPIException e) {
				e.printStackTrace();
				return;
			} catch (IndexOutOfBoundsException e) {
				sendEmbedMessage(new DefaultEmbed("Usuario inexistente", "Este usuario que você solicitou não existe."))
				.queue();
				return;
			}
			
			if (osuUser.getTopscore().size() == 0) {
				sendEmbedMessage(new DefaultEmbed("Não há historico", "Este usuario que você solicitou não tem historico."))
				.queue();
				return;
			}
			
		  /*File file = OsuBeatmapSimple.drawImage(userscore.getBeatmap());
			EmbedBuilder e = embed(userscore);
			 sendFileEmbeded(e, file).queue(new Consumer<Message>() {

				@Override
				public void accept(Message message) {
					 message.addReaction("U+25C0").submit();
					 message.addReaction("U+25FC").submit();
					 message.addReaction("U+25B6").submit();
					 ReactionMessage.osuHistory.add(new ReactionUtils(user, message.getId(), osuUser.getUsername(),0));
				}
			});
			*/
			
			sendEmbedMessage(embed(new UserScores(osuUser, ScoreType.TopsScore, 0)))
			.queue(message -> {
				 message.addReaction("U+25C0").submit();
				 message.addReaction("U+25FC").submit();
				 message.addReaction("U+25B6").submit();
				 ReactionMessage.osuHistory.add(new ReactionUtils(user, message.getId(), osuUser.getUsername(),0));
			});
			return;
		}
	}

	public static EmbedBuilder embed(UserScores osuUser) {
		EmbedBuilder embed = new EmbedBuilder();
		
		String inicial = getRankEmote(osuUser);
		
		embed.setColor(Color.gray);
		embed.setTitle(inicial + " " + osuUser.getUsername() + " | Histórico do Jogador");
		embed.setDescription("Você esta visualizando os beatmaps jogados com melhor pontuação de " + 
		"[" + osuUser.getUsername() +"]("+ osuUser.getUserOsu().getUserUrl() +")");
		
		
		OsuBeatmap beat = osuUser.getBeatmap();
		
		String title = "";
		try {
			title = "[" + beat.getTitle() + "](" + beat.getURL().toString() +") por `" +  beat.getArtist() + "`";
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		embed.addField("Beatmap:", title, true);
		embed.addField("Status:", "`" + getApproval(beat.getApproved()) + "`\n"
				+ beat.getVersion(), true);
		
		String h300 = OsuEmoji.Hit300.getEmojiString() + ": " + osuUser.getHit300();
		String h100 = OsuEmoji.Hit100.getEmojiString() + ": "+osuUser.getHit100();
		String h50 = OsuEmoji.Hit50.getEmojiString() + ": "+osuUser.getHit50();
		String miss = OsuEmoji.Miss.getEmojiString() + ": "+osuUser.getMiss();
		String pp = OsuEmoji.PP.getEmojiString() + ": ";
		String l = "\n";
		String field = h300 + l + h100 + l + h50 + l + miss + l;
		
		embed.addField("Pontuação",field, true);
		embed.addField("Pontuação total:", osuUser.getScore() +"", true);
		embed.addField("Combo Maximo:", osuUser.getMaxCombo()+"/" + osuUser.getBeatmap().getMaxCombo(), true);
		
		
		embed.addField("PP",pp + osuUser.getMapPP()+"", true);
		
		int id = osuUser.getBeatmap().getBeatmapSetID();
		String url = "https://assets.ppy.sh/beatmaps/" + id +"/covers/cover.jpg?";
		
		linkcover = url;
		embed.setThumbnail(osuUser.getAvatarURL());
		embed.setImage(url);
		
		UserOsu beatcreator;
		try {
			beatcreator = new UserOsu(osuUser.getBeatMapCreator(), GameMode.STANDARD);
			String author = beatcreator.getUsername();
			String avatar = "https://a.ppy.sh/" + beatcreator.getUserid() +"?.jpeg";
			
			embed.setFooter(beat.getTitle()+ " por " + beat.getArtist() + " | Mapa criado por " + author, avatar);
		} catch (MalformedURLException | OsuAPIException e) {
			e.printStackTrace();
		}
		
		return embed;
	}
	
	public GameMode getGamemode(String gamemode) {
		String gm = gamemode.toLowerCase();
		Map<String, GameMode> map = new HashMap<>();
		
		map.put("standard", GameMode.STANDARD);
		map.put("catch", GameMode.CATCH_THE_BEAT);
		map.put("mania", GameMode.MANIA);
		map.put("taiko", GameMode.TAIKO);

		if (map.containsKey(gm)) {
			return map.get(gamemode);
		}
		
		return null;
	}
	
	public static String getApproval(ApprovalState approval) {
		Map<ApprovalState, String> map = new HashMap<>();
		
		map.put(ApprovalState.RANKED, "Ranqueado");
		map.put(ApprovalState.QUALIFIED, "Qualificado");
		map.put(ApprovalState.PENDING, "Pendente");
		map.put(ApprovalState.APPROVED, "Aprovado");
		map.put(ApprovalState.LOVED, "Loved");
		map.put(ApprovalState.GRAVEYARD, "Cemiterio");
		map.put(ApprovalState.WIP, "WorkInProgress");

		if (map.containsKey(approval)) {
			return map.get(approval);
		}
		
		return "Não classificado";
	}
	
	
	
	public static String getRankEmote(UserScores osuUser) {
		String rank = osuUser.getBeatMapRank();
		if (rank.equalsIgnoreCase("SSH")) {
			return OsuEmoji.SSPlus.getEmojiString();
		}
		if (rank.equalsIgnoreCase("SS")) {
			return OsuEmoji.SS.getEmojiString();
		}
		if (rank.equalsIgnoreCase("SH")) {
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