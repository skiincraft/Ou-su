package me.skiincraft.discord.ousu.manager;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import me.skiincraft.discord.ousu.mysql.SQLAccess;
import me.skiincraft.discord.ousu.utils.DefaultEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public abstract class Commands extends ListenerAdapter {
	
	private String prefix;
	private String command;
	private List<String> aliases;
	private String usage;


	private String[] args;
	private GuildMessageReceivedEvent event;
	
	private TextChannel channel;
	private User user;

	public Commands(String prefix, String command) {
		this.prefix = prefix;
		this.command = command;
		this.aliases = null;
		this.usage = null;
	}
	
	public Commands(String prefix, String command, String usage, List<String> aliases) {
		this.prefix = prefix;
		this.command = command;
		this.aliases = aliases;
		this.usage = usage;
	}
	
	public abstract String[] helpMessage();
	public abstract CommandCategory categoria();
	
	public abstract void action(String[] args, User user, TextChannel channel);
	
	public boolean check(GuildMessageReceivedEvent e) {
		args = e.getMessage().getContentRaw().split(" ");
		if (e.getAuthor().isBot()) {
			return false;
		}
		if (e.getJDA().getSelfUser().getName() == e.getAuthor().getName()) {
			return false;
		}
		
		if (e.getChannel().isNSFW()) {
			return false;
		}
		prefix = new SQLAccess(e.getGuild()).get("prefix");

		if (!hasAliases()) {
			if (!args[0].equalsIgnoreCase(prefix + command)) {
				return false;
			}
		}
		
		this.channel = e.getChannel();
		this.user = e.getAuthor();
		this.event = e;
		return true;
	}
	
	public boolean hasPermission(User user, Permission permission) {
		if (event.getGuild().getMember(user).hasPermission(permission)) {
			return true;
		}
		return false;
	}
	
	public boolean hasRole(User user, String rolename) {
		List<Role> role = getEvent().getGuild().getRolesByName(rolename, true);
		List<Role> memberRoles = event.getGuild().getMember(user).getRoles();
		
		if (memberRoles.contains(role.get(0))){
			return true;
		}
		return false;
	}
	
	public boolean hasPermissionorRole(User user, Permission permission, String rolename) {
		if (hasPermission(user, permission)) {
			return true;
		}
		
		if (hasRole(user, "mod")) {
			return true;
		}
		
		return false;
	}
	
	public boolean hasAliases() {
		if (getAliases() == null) {
			return false;
		}
		
		int a = getAliases().size();
		for (int i = 0; i < a;i++) {
			if (args[0].equalsIgnoreCase(prefix + getAliases().get(i))) {
				return true;
			}
		}
		return false;
	}
	
	public String getCommand() {
		return command;
	}
	
	public String getCommandFull() {
		return prefix + command;
	}
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (check(event) == false) {
			return;
		}
		
		String date = new SimpleDateFormat("HH:mm:ss").format(new Date()); 
		String comp = "["+ channel.getGuild().getName() + ":" + channel.getName() + " | "+ date +"]: ";
		System.out.println(comp + user.getName() + user.getDiscriminator() + " executou o comando " + getCommandFull());
		
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				action(event.getMessage().getContentRaw().split(" "), user, channel);
				String data = new SimpleDateFormat("HH:mm:ss").format(new Date());
				String comp = "["+ channel.getGuild().getName() + ":" + channel.getName() + " | "+ data +"]: ";
				System.out.println(comp + user.getName() + user.getDiscriminator() + " utilizou o comando " + getCommandFull() + " com sucesso");
			}
		});
		
		t.start();
	}
	
	public boolean isInsuficient() {
		try {
			String a = args[1];
			a.length();
			return false;
		} catch (ArrayIndexOutOfBoundsException e) {
			return true;
		}
	}
	
	public void DeletarMSGReceived() {
		event.getMessage().delete().queue();
	}
	
	public MessageAction sendUsage() {
		MessageAction a = event.getChannel().sendMessage(new DefaultEmbed(
				"'❌' Uso incorreto", "Tente utilizar o comando " + getUsage())
				.construir());
		return a;		
	}
	
	public MessageAction noPermissionMessage(Permission permission) {
		MessageAction a = event.getChannel().sendMessage(new DefaultEmbed(
				"'❌'Permissão insufiente!", "Você não tem permissão suficiente para utilizar este comando\nAs permissões necessarias são: " + permission.getName())
				.construir());
		return a;		
	}

	public MessageAction sendMessage(String message) {
		MessageAction a = event.getChannel().sendMessage(message);
		return a;
	}
	
	public MessageAction sendFile(File file) {
		MessageAction a = event.getChannel().sendFile(file, file.getName());
		return a;
	}
	
	public MessageAction sendPrivateMessage(String message) {
		MessageAction a = event.getAuthor().openPrivateChannel().complete().sendMessage(message);
		return a;
	}
	
	public MessageAction sendEmbedMessage(EmbedBuilder e) {
		MessageAction a = event.getChannel().sendMessage(e.build());
		return a;
	}
	
	public MessageAction sendEmbedMessage(MessageEmbed e) {
		MessageAction a = event.getChannel().sendMessage(e);
		return a;
	}
	
	public MessageAction sendFileEmbeded(EmbedBuilder e, File file) {
		MessageAction b = event.getChannel().sendFile(file, file.getName()).embed(e.setImage("attachment://" + file.getName()).build());
		return b;
	}
	
	public MessageAction sendFileEmbeded(EmbedBuilder e, InputStream input) {
		MessageAction b = event.getChannel().sendFile(input, "profile_osu.png").embed(e.setImage("attachment://profile_osu.png").build());
		return b;
	}
	
	public MessageAction sendFileEmbeded(DefaultEmbed e, File file) {
		MessageAction b = event.getChannel().sendFile(file, file.getName()).embed(e.construirEmbed().setImage("attachment://" + file.getName()).build());
		return b;
	}
	
	public MessageAction sendEmbedMessage(DefaultEmbed e) {
		MessageAction a = event.getChannel().sendMessage(e.construir());
		return a;
	}
	
	public MessageAction sendPrivateEmbedMessage(DefaultEmbed e) {
		MessageAction a = event.getAuthor().openPrivateChannel().complete().sendMessage(e.construir());
		return a;
	}
	
	public List<String> getAliases() {
		return aliases;
	}

	public void setAliases(List<String> aliases) {
		this.aliases = aliases;
	}

	public String getUsage() {
		if (usage == null) {
			setUsage(getCommandFull());
		}
		return usage;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}
	
	public GuildMessageReceivedEvent getEvent() {
		return event;
	}
	
	public CommandCategory getCategoria() {
		return this.categoria();
	}

	public void setEvent(GuildMessageReceivedEvent event) {
		this.event = event;
	}
	
}
