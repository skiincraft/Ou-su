package me.skiincraft.discord.ousu.common;

import java.util.List;

import me.skiincraft.discord.core.command.Command;
import me.skiincraft.discord.ousu.messages.Messages;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public abstract class Comando extends Command {
	
	public Comando(String name, List<String> aliases, String usage) {
		super(name, aliases, usage);
	}
	
	public abstract CommandCategory getCategory();
	
	public void replyUsage() {
		reply(Messages.UsageMessage(this, getLanguageManager().getLanguage()));
	}
	
	public boolean isOwner(User user) {
		return user.getIdLong() == Long.valueOf("247096601242238991");
	}
	
	public Member getMember(User user) {
		return getTextChannel().getGuild().getMember(user);
	}

}