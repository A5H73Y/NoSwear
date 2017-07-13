package A5H73Y.NoSwear;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import A5H73Y.NoSwear.Updater.UpdateResult;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class NoSwear extends JavaPlugin {

	public static String NOSWEAR;
	public final String VERSION = getDescription().getVersion();
	public final String PERM_BYPASS = "NoSwear.Bypass";
	public final String PERM_ADMIN = "NoSwear.Admin";

	private List<String> blockedWords = new ArrayList<>();
	private List<String> whiteWords = new ArrayList<>();
	private List<String> mutedPlayers = new ArrayList<>();

	boolean SET_SPAMBLOCKER;
    boolean SET_CAPSBLOCKER;
    boolean SET_WEBBLOCKER;
    boolean SET_STRICT;
    boolean SET_COMMANDS;
    boolean SET_EXEC_COMMAND;
    boolean SET_NOTIFY_OP;
    boolean SET_ECONOMY;
    boolean SET_CANCEL;
    boolean SET_REPLACE;
    boolean SET_OLD_DETECT;
    boolean SET_INCLUDE_OPS;
    boolean SET_MESSAGE_OP;
	int 	SET_CAPSBLOCKER_PERCENT;
    int     SET_SPAMBLOCKER_DELAY;
	String 	SET_STRICT_REGEX;

	private File wordFile;
	private FileConfiguration wordData;
	private NoSwearMethods noSwearMethods;
	private Economy economy;

	public void onDisable() {
		getLogger().info("Disabled!");
	}

	public void onEnable() {
		noSwearMethods = new NoSwearMethods(this);
		getServer().getPluginManager().registerEvents(new NoSwearListener(this), this);

		setupUpdater();
		setupConfig();
		setupVault();

		getLogger().info(VERSION + " enabled!");
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("noswear")) {
			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("add")) {
					if (!hasPermission(sender, PERM_ADMIN))
						return false;

					if (args.length != 2){
						sender.sendMessage(NOSWEAR + "Invalid syntax: /ns add (word)");
						return false;
					}

					noSwearMethods.addSwearWord(sender, args[1]);

				} else if (args[0].equalsIgnoreCase("del") || args[0].equalsIgnoreCase("delete")) {
					if (!hasPermission(sender, PERM_ADMIN))
						return false;

					if (args.length != 2){
						sender.sendMessage(NOSWEAR + "Invalid syntax: /ns delete (word)");
						return false;
					}

					noSwearMethods.deleteSwearWord(sender, args[1]);

				} else if (args[0].equalsIgnoreCase("mute")) {
					if (!hasPermission(sender, PERM_ADMIN))
						return false;

					if (args.length != 2){
						sender.sendMessage(NOSWEAR + "Invalid syntax: /ns mute (player)");
						return false;
					}

					noSwearMethods.mutePlayer(sender, Bukkit.getPlayer(args[1]));

				} else if (args[0].equalsIgnoreCase("unmute")) {
					if (!hasPermission(sender, PERM_ADMIN))
						return false;

					if (args.length != 2){
						sender.sendMessage(NOSWEAR + "Invalid syntax: /ns unmute (player)");
						return false;
					}

					noSwearMethods.unmutePlayer(sender, Bukkit.getPlayer(args[1]));
					
				} else if (args[0].equalsIgnoreCase("addwhite")) {
					if (!hasPermission(sender, PERM_ADMIN))
						return false;

					if (args.length != 2){
						sender.sendMessage(NOSWEAR + "Invalid syntax: /ns addwhite (word)");
						return false;
					}

					noSwearMethods.addWhiteWord(sender, args[1]);
					
				} else if (args[0].equalsIgnoreCase("delwhite")) {
					if (!hasPermission(sender, PERM_ADMIN))
						return false;

					if (args.length != 2){
						sender.sendMessage(NOSWEAR + "Invalid syntax: /ns delwhite (word)");
						return false;
					}

					noSwearMethods.deleteWhiteWord(sender, args[1]);

				} else if (args[0].equalsIgnoreCase("clear")){
					if (!(sender instanceof Player))
						return false;

					for (int i=0; i < 20; i++)
						sender.sendMessage("");

				} else if (args[0].equalsIgnoreCase("perms") || args[0].equalsIgnoreCase("permissions")){
					sender.sendMessage("-- " + ChatColor.DARK_RED + ChatColor.BOLD + "Permissions" + ChatColor.RESET + " --");
					sender.sendMessage(" Bypass: " + ChatColor.AQUA + sender.hasPermission(PERM_BYPASS));
					sender.sendMessage(" Admin: " + ChatColor.AQUA + sender.hasPermission(PERM_ADMIN));

				} else if (args[0].equalsIgnoreCase("list")) {
					if (!hasPermission(sender, PERM_ADMIN))
						return false;

					sender.sendMessage("-- " + ChatColor.DARK_RED + ChatColor.BOLD + "Blocked Words" + ChatColor.RESET + " --");
					sender.sendMessage(" " + blockedWords.toString().replace("[", "").replace("]", ""));

				} else if (args[0].equalsIgnoreCase("muted")) {
					if (!hasPermission(sender, PERM_ADMIN))
						return false;

					sender.sendMessage("-- " + ChatColor.DARK_RED + ChatColor.BOLD + "Muted senders" + ChatColor.RESET + " --");
					sender.sendMessage(" " + mutedPlayers.toString().replace("[", "").replace("]", ""));

				} else if (args[0].equalsIgnoreCase("warns")) {
					if (!(sender instanceof Player))
						return false;

					noSwearMethods.displayRemainingWarnings((Player) sender);

				} else if (args[0].equalsIgnoreCase("settings")) {
					if (!hasPermission(sender, PERM_ADMIN))
						return false;

					sender.sendMessage("-- " + ChatColor.DARK_RED + ChatColor.BOLD + "Settings" + ChatColor.RESET + " --");
					sender.sendMessage("Warnings: " + ChatColor.AQUA + getConfig().getInt("Warnings"));
					sender.sendMessage("Reset Warns: " + ChatColor.AQUA + getConfig().getBoolean("OnWarningsDeplete.RestoreWarnings"));
					sender.sendMessage("Block Spam: " + ChatColor.AQUA + SET_SPAMBLOCKER);
					sender.sendMessage("Block Websites: " + ChatColor.AQUA + SET_WEBBLOCKER);
					sender.sendMessage("Block Caps: " + ChatColor.AQUA + SET_CAPSBLOCKER);
					sender.sendMessage("Strict Detection: " + ChatColor.AQUA + SET_STRICT);
					sender.sendMessage("Filter Commands: " + ChatColor.AQUA + SET_COMMANDS);
					sender.sendMessage("Filter Signs: " + ChatColor.AQUA + getConfig().getBoolean("Block.IncludeSigns"));
					sender.sendMessage("Notify Ops: " + ChatColor.AQUA + SET_NOTIFY_OP);
					sender.sendMessage("Replace swear: " + ChatColor.AQUA + SET_REPLACE);
					sender.sendMessage("Cancel swear: " + ChatColor.AQUA + SET_CANCEL);
					
				} else if (args[0].equalsIgnoreCase("reload")) {
					if (!hasPermission(sender, PERM_ADMIN))
						return false;

					reloadConfig();
					setupSettings();
					saveWords();
					sender.sendMessage(NOSWEAR + "Config Reloaded!");

				} else if (args[0].equalsIgnoreCase("cmds")) {
					sender.sendMessage("-- " + NOSWEAR + "--");
					if (sender.hasPermission(PERM_ADMIN)){
						sender.sendMessage(ChatColor.DARK_AQUA + "/ns " + ChatColor.AQUA + "add " + ChatColor.YELLOW + "(word)" + ChatColor.BLACK + " : " + ChatColor.WHITE + "Add the word to the blocked list");
						sender.sendMessage(ChatColor.DARK_AQUA + "/ns " + ChatColor.AQUA + "del " + ChatColor.YELLOW + "(word)" + ChatColor.BLACK + " : " + ChatColor.WHITE + "Delete the word from the blocked list");
						sender.sendMessage(ChatColor.DARK_AQUA + "/ns " + ChatColor.AQUA + "mute " + ChatColor.YELLOW + "(player)" + ChatColor.BLACK + " : " + ChatColor.WHITE + "Add player to mute list");
						sender.sendMessage(ChatColor.DARK_AQUA + "/ns " + ChatColor.AQUA + "unmute " + ChatColor.YELLOW + "(player)" + ChatColor.BLACK + " : " + ChatColor.WHITE + "Remove player from mute list");
						sender.sendMessage(ChatColor.DARK_AQUA + "/ns " + ChatColor.AQUA + "addwhite " + ChatColor.YELLOW + "(word)" + ChatColor.BLACK + " : " + ChatColor.WHITE + "Add the word to the whitelist");
						sender.sendMessage(ChatColor.DARK_AQUA + "/ns " + ChatColor.AQUA + "delwhite " + ChatColor.YELLOW + "(word)" + ChatColor.BLACK + " : " + ChatColor.WHITE + "Delete the word from the whitelist");
						sender.sendMessage(ChatColor.DARK_AQUA + "/ns " + ChatColor.AQUA + "list" + ChatColor.BLACK + " : " + ChatColor.WHITE + "Display a list of the blocked words");
						sender.sendMessage(ChatColor.DARK_AQUA + "/ns " + ChatColor.AQUA + "muted " + ChatColor.BLACK + " : " + ChatColor.WHITE + "Display a list of all the muted users");
						sender.sendMessage(ChatColor.DARK_AQUA + "/ns " + ChatColor.AQUA + "settings" + ChatColor.BLACK + " : " + ChatColor.WHITE + "Display the NoSwear settings");
						sender.sendMessage(ChatColor.DARK_AQUA + "/ns " + ChatColor.AQUA + "reload" + ChatColor.BLACK + " : " + ChatColor.WHITE + "Reload the NoSwear config");
					}
					sender.sendMessage(ChatColor.DARK_AQUA + "/ns " + ChatColor.AQUA + "warns" + ChatColor.BLACK + " : " + ChatColor.WHITE + "Display how many warns you have remaining");
					sender.sendMessage(ChatColor.DARK_AQUA + "/ns " + ChatColor.AQUA + "clear " + ChatColor.BLACK + " : " + ChatColor.WHITE + "Clear your chat.");
					sender.sendMessage(ChatColor.DARK_AQUA + "/ns " + ChatColor.AQUA + "perms" + ChatColor.BLACK + " : " + ChatColor.WHITE + "Display the senders permissions");
					sender.sendMessage(ChatColor.DARK_AQUA + "/ns " + ChatColor.AQUA + "cmds" + ChatColor.BLACK + " : " + ChatColor.WHITE + "Display the commands list");
					sender.sendMessage("--------------");

				} else {
					sender.sendMessage(NOSWEAR + "Unknown command!");
					sender.sendMessage(ChatColor.DARK_AQUA + "/ns " + ChatColor.AQUA + "cmds" + ChatColor.GRAY + " - " + ChatColor.WHITE + "To display all NoSwear commands.");
				}
			} else {
				sender.sendMessage(NOSWEAR + "Plugin created by " + ChatColor.AQUA + "A5H73Y");
				sender.sendMessage(ChatColor.DARK_AQUA + "/ns " + ChatColor.AQUA + "cmds" + ChatColor.GRAY + " - " + ChatColor.WHITE + "To display all NoSwear commands.");
			}
		}
		return false;
	}

	private void setupConfig(){
		FileConfiguration config = getConfig();

		config.options().header("==== NoSwear Config ==== #");

		config.addDefault("Warnings", 3);
		config.addDefault("OnWarningsDeplete.RestoreWarnings", true);
		config.addDefault("OnWarningsDeplete.Ban", false);
		config.addDefault("OnWarningsDeplete.Kick", true);
		config.addDefault("OnWarningsDeplete.Mute", false);
		config.addDefault("OnWarningsDeplete.Kill", false);
		config.addDefault("OnWarningsDeplete.Command.Enabled", false);
		config.addDefault("OnWarningsDeplete.Command.Execute", "kill %PLAYER%");
		config.addDefault("OnWarningsDeplete.Economy.Enabled", true);
		config.addDefault("OnWarningsDeplete.Economy.ChargeAmount", 10.0);
		config.addDefault("OnSwear.IncludeOps", false);
		config.addDefault("OnSwear.CancelMessage", true);
		config.addDefault("OnSwear.ReplaceSwearWord", false);
		config.addDefault("OnSwear.StrictDetection.Enabled", true);
		config.addDefault("OnSwear.StrictDetection.RegEx", "[^a-z0-9 ]");
		config.addDefault("OnSwear.StrictDetection.RepeatedLetters", true);
		config.addDefault("OnSwear.NotifyOps", false);
		config.addDefault("OnSwear.SendMessageToOps", false);
		config.addDefault("OnSwear.UseOldDetection", false);
		config.addDefault("OnJoin.WelcomeMessage", true);
		config.addDefault("OnJoin.WarningsLeft", false);
		config.addDefault("Block.Websites.Enabled", true);
		config.addDefault("Block.Websites.IncludeIPs", true);
		config.addDefault("Block.Spam.Enabled", true);
		config.addDefault("Block.Spam.WaitSeconds", 1);
		config.addDefault("Block.Caps.Enabled", true);
		config.addDefault("Block.Caps.MinPercent", 50);
		config.addDefault("Block.IncludeCommands", true);
		config.addDefault("Block.IncludeSigns", true);
		config.addDefault("Message.Prefix", "&0[&4No&fSwear&0] &f");
		config.addDefault("Message.Ban", "Banned for breaking the rules!");
		config.addDefault("Message.Kick", "Kicked for breaking the rules!");
		config.addDefault("Message.Muted", "You have been muted!");
		config.addDefault("Message.Join", "Protects this server!");
		config.addDefault("Message.Caps", "Do not spam caps &b%PLAYER%&f!");
		config.addDefault("Message.Warn", "Do not swear &b%PLAYER%&f!");
		config.addDefault("Message.Spam", "Do not spam &b%PLAYER%&f!");
		config.addDefault("Message.SpamWait", "Please wait %SECONDS% more seconds before talking again.");
		config.addDefault("Message.Advertise", "Do not advertise &b%PLAYER%&f!");
		config.addDefault("Message.EconomyCharge", "You have been charged $%AMOUNT%!");
		config.addDefault("Message.Remaining", "You have &b%REMAINING% &f/ &3%DEFAULT% &fwarnings remaining.");
		config.addDefault("Muted", Collections.singletonList(mutedPlayers));
		config.addDefault("CheckForUpdates", true);
		config.addDefault("SubmitMetrics", true);
		config.addDefault("Version", getDescription().getVersion());
		config.options().copyDefaults(true);
		saveConfig();
		
		setupWords();
		convertToLatest();
		setupSettings();
	}
	
	private void convertToLatest(){
		if (!(getConfig().getDouble("Version") < Double.parseDouble(VERSION)))
			return;
		
		if (getConfig().getString("OnSwear.StrictDetection.RegEx").equals("[^a-z0-9]"))
			getConfig().set("OnSwear.StrictDetection.RegEx", "[^a-z0-9 ]");
		
		getConfig().set("Version", VERSION);
		saveConfig();
	}

	private void setupSettings(){
	    NOSWEAR = getConfig().getString("Message.Prefix");

		SET_SPAMBLOCKER = getConfig().getBoolean("Block.Spam.Enabled");
		SET_SPAMBLOCKER_DELAY = getConfig().getInt("Block.Spam.WaitSeconds");
		SET_CAPSBLOCKER = getConfig().getBoolean("Block.Caps.Enabled");
		SET_CAPSBLOCKER_PERCENT = getConfig().getInt("Block.Caps.MinPercent");
		SET_WEBBLOCKER  = getConfig().getBoolean("Block.Websites.Enabled");
		SET_COMMANDS 	= getConfig().getBoolean("Block.IncludeCommands");
		SET_CANCEL		= getConfig().getBoolean("OnSwear.CancelMessage");
		SET_REPLACE		= getConfig().getBoolean("OnSwear.ReplaceSwearWord");
		SET_OLD_DETECT	= getConfig().getBoolean("OnSwear.UseOldDetection");
		SET_INCLUDE_OPS = getConfig().getBoolean("OnSwear.IncludeOps");

		SET_STRICT 		= getConfig().getBoolean("OnSwear.StrictDetection.Enabled");
		SET_STRICT_REGEX= getConfig().getString("OnSwear.StrictDetection.RegEx");
		SET_EXEC_COMMAND= getConfig().getBoolean("OnWarningsDeplete.Command.Enabled");
		SET_NOTIFY_OP	= getConfig().getBoolean("OnSwear.NotifyOps");
		SET_MESSAGE_OP  = getConfig().getBoolean("OnSwear.SendMessageToOps");

		mutedPlayers 	= getConfig().getStringList("Muted");
	}

	private void setupWords(){
		wordFile = new File(getDataFolder(), "words.yml");
		wordData = new YamlConfiguration();
		if (!wordFile.exists()){
			try {
				wordFile.createNewFile();
				getLogger().info("Created words.yml");
			} catch (Exception ex) {
				getLogger().info("[NoSwear] Failed: " + ex.getMessage());
			}
		}

		try{
			wordData.load(wordFile);
		}catch (Exception ex){
			ex.printStackTrace();
		}

		String[] defaults = { "fuck", "shit", "nigger", "nigga", "asshole", "arsehole", "twat", "whore", "bitch", "cock", "tits", "cunt", "bastard", "dick", "slag", "slut", "wank", "prick", "faggot", "piss", "pussy", "bollocks", "bugger", "tosser" };
		wordData.addDefault("BlockedWords", Arrays.asList(defaults));
		wordData.addDefault("Whitelist", Collections.emptyList());
		wordData.options().copyDefaults(true);
		blockedWords = wordData.getStringList("BlockedWords");
		whiteWords = wordData.getStringList("Whitelist");
		saveWords();
	}

	private void setupUpdater(){
		if (!getConfig().getBoolean("CheckForUpdates"))
			return;
		
		try {
			Updater updater = new Updater(this, 38329, this.getFile(), Updater.UpdateType.DEFAULT, true);
			if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
				getLogger().info("New version available: " + updater.getLatestName());
			}
		} catch (Exception ex) {
			getLogger().info("Failed to check for updates.");
		}
	}

	private void setupVault(){
		if (!getConfig().getBoolean("OnWarningsDeplete.Economy.Enabled"))
			return;

		Plugin vault = getServer().getPluginManager().getPlugin("Vault");

		if (vault != null && vault.isEnabled()) {
			if (setupEconomy()) {
				getLogger().info("[Vault] Linked with Vault v" + vault.getDescription().getVersion());
				SET_ECONOMY = true;
			} else {
				getLogger().info("[Vault] Attempted to link with Vault, but something went wrong. Please ensure you've got an economy plugin installed.");
			}
		} else {
			getLogger().info("[Vault] Vault is missing, disabling Economy Use.");
			getConfig().set("OnWarningsDeplete.Economy.Enabled", false);
			saveConfig();
		}
	}

	private boolean setupEconomy(){
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}


	protected void saveWords(){
		wordData.set("BlockedWords", getBlockedWords());
		wordData.set("Whitelist", getWhiteWords());

		try {
			wordData.save(wordFile);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/* Getters & Setters */

    protected boolean hasPermission(CommandSender sender, String permission){
        if (!sender.hasPermission(permission)){
            sender.sendMessage(getPrefix() + "You do not have permission: " + ChatColor.RED + permission);
            return false;
        }
        return true;
    }

	public List<String> getBlockedWords(){
		return blockedWords;
	}
	
	public List<String> getWhiteWords(){
		return whiteWords;
	}

	public List<String> getMuted(){
		return mutedPlayers;
	}

    public static String getPrefix() {
        return NOSWEAR;
    }

    public String getVersion() {
        return VERSION;
    }

    public List<String> getMutedPlayers() {
        return mutedPlayers;
    }

    public Economy getEconomy() {
        return economy;
    }

    public NoSwearMethods getNoSwearMethods() {
        return noSwearMethods;
    }
}
