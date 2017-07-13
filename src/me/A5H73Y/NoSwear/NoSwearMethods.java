package A5H73Y.NoSwear;

import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NoSwearMethods {

	public enum WarningType {
		SWEARING,
		ADVERTISING,
		SPAMMING
	}

	private NoSwear noSwear;

	NoSwearMethods(NoSwear noSwear) {
		this.noSwear = noSwear;
	}

	private void sendWarningMessage(Player player, WarningType type) {
		String warning = "Message.Warn";

		if (type.equals(WarningType.ADVERTISING))
			warning = "Message.Advertise";

		else if (type.equals(WarningType.SPAMMING))
			warning = "Message.Spam";

		player.sendMessage(getMessage(player, warning));
	}

	public String getMessage(Player player, String message) {
		return noSwear.NOSWEAR + colour(
				noSwear.getConfig().getString(message).replace("%PLAYER%", player.getName()));
	}

	/**
	 * Decide what to do to the player after they swear.
	 * @param player
	 * @return whether or not to cancel the event
	 */
	public boolean playerPunish(Player player, WarningType type) {
		int remainingWarnings = getRemainingWarnings(player.getName());
		boolean cancel = noSwear.SET_CANCEL;
		
		if (noSwear.SET_NOTIFY_OP)
			notifyOps(player, type);

		//Remaining warnings
		if (remainingWarnings >= 1){
			remainingWarnings--;

			setRemainingWarnings(player.getName(), remainingWarnings);
			sendWarningMessage(player, type);
			displayRemainingWarnings(player);

			return cancel;
		}	

		//Swearing depleted
		if (noSwear.getConfig().getBoolean("OnWarningsDeplete.RestoreWarnings"))
			setRemainingWarnings(player.getName(), getDefaultWarnings());

		if (noSwear.getConfig().getBoolean("OnWarningsDeplete.Command.Enabled")){
			noSwear.getServer().dispatchCommand(noSwear.getServer().getConsoleSender(), 
					noSwear.getConfig().getString("OnWarningsDeplete.Command.Execute").replace("%PLAYER%", player.getName()));
		}

		chargePlayer(player);

		if (noSwear.getConfig().getBoolean("OnWarningsDeplete.Mute") && !noSwear.getMutedPlayers().contains(player.getName())){
			mutePlayer(player.getName());
			player.sendMessage(getMessage(player, "Message.Muted"));
			return cancel;
		}

		if (noSwear.getConfig().getBoolean("OnWarningsDeplete.Ban")) {
			banPlayer(player);
			return cancel;
		}

		if (noSwear.getConfig().getBoolean("OnWarningsDeplete.Kick")){
			kickPlayer(player);
			return cancel;
		}

		if (noSwear.getConfig().getBoolean("OnWarningsDeplete.Kill")){
			killPlayer(player, type);
			return cancel;
		}

		sendWarningMessage(player, type);
		return cancel;
	}

	private void notifyOps(Player player, WarningType type) {
		for (Player players : noSwear.getServer().getOnlinePlayers())
			if (players.isOp())
				players.sendMessage(noSwear.NOSWEAR + player.getName() + " tried " + type.toString());
	}

    protected void sendMessageToOps(String playerName, String message) {
        for (Player players : noSwear.getServer().getOnlinePlayers())
            if (players.isOp())
                players.sendMessage(noSwear.NOSWEAR + playerName + " said: " + message);
    }

	/**
	 * Add a swear word to the list of blocked words.
	 * Can be sent from player or console.
	 * @param sender
	 * @param word
	 */
	public void addSwearWord(CommandSender sender, String word){
		word = word.toLowerCase();

		if (noSwear.getBlockedWords().contains(word)){
			sender.sendMessage(noSwear.NOSWEAR + "Word already added!");
			return;
		}

		noSwear.getBlockedWords().add(word);
		noSwear.saveWords();
		sender.sendMessage(noSwear.NOSWEAR + ChatColor.AQUA + word + ChatColor.WHITE + " has been added.");
	}

	/**
	 * Remove a swear word to the list of blocked words.
	 * Can be sent from player or console.
	 * @param sender
	 * @param word
	 */
	public void deleteSwearWord(CommandSender sender, String word){
		word = word.toLowerCase();

		if (!noSwear.getBlockedWords().contains(word)){
			sender.sendMessage(noSwear.NOSWEAR + "Word is not in the list!");
			return;
		}

		noSwear.getBlockedWords().remove(word);
		noSwear.saveWords();
		sender.sendMessage(noSwear.NOSWEAR + ChatColor.AQUA + word + ChatColor.WHITE + " has been removed.");
	}
	
	/**
	 * Add a word into the Whitelist.
	 * Can be sent from player or console.
	 * @param sender
	 * @param word
	 */
	public void addWhiteWord(CommandSender sender, String word) {
		word = word.toLowerCase();

		if (noSwear.getWhiteWords().contains(word)){
			sender.sendMessage(noSwear.NOSWEAR + "Word already whitelisted!");
			return;
		}

		noSwear.getWhiteWords().add(word);
		noSwear.saveWords();
		sender.sendMessage(noSwear.NOSWEAR + ChatColor.AQUA + word + ChatColor.WHITE + " has been whitelisted.");
	}

	/**
	 * Add a word into the Whitelist.
	 * Can be sent from player or console.
	 * @param sender
	 * @param word
	 */
	public void deleteWhiteWord(CommandSender sender, String word) {
		word = word.toLowerCase();

		if (!noSwear.getWhiteWords().contains(word)){
			sender.sendMessage(noSwear.NOSWEAR + "Word is not in the whitelist!");
			return;
		}

		noSwear.getWhiteWords().remove(word);
		noSwear.saveWords();
		sender.sendMessage(noSwear.NOSWEAR + ChatColor.AQUA + word + ChatColor.WHITE + " has been removed from the whitelist.");
	}
	
	/**
	 * Add a player to the list of muted player.
	 * Can be sent from player or console.
	 * @param player
	 * @param targetPlayer
	 */
	public void mutePlayer(CommandSender player, Player targetPlayer){
		if (targetPlayer == null){
			player.sendMessage(noSwear.NOSWEAR + "Could not find player!");
			return;
		}

		if (noSwear.getMuted().contains(targetPlayer.getName())){
			player.sendMessage(noSwear.NOSWEAR + targetPlayer.getName() + " is already muted!");
			return;
		}

		mutePlayer(targetPlayer.getName());
		player.sendMessage(noSwear.NOSWEAR + ChatColor.AQUA + targetPlayer.getName() + ChatColor.WHITE + " has been muted!");
	}

	private void mutePlayer(String playerName){
		if (noSwear.getMuted().contains(playerName)){
			return;
		}

		noSwear.getMuted().add(playerName);
		noSwear.getConfig().set("Muted", noSwear.getMuted());
		noSwear.saveConfig();
	}

	/**
	 * Remove a player to the list of muted player.
	 * Can be sent from player or console.
	 * @param player
	 * @param targetPlayer
	 */
	public void unmutePlayer(CommandSender player, Player targetPlayer) {
		if (targetPlayer == null){
			player.sendMessage(noSwear.NOSWEAR + "Could not find player!");
			return;
		}

		if (!noSwear.getMuted().contains(targetPlayer.getName())){
			player.sendMessage(noSwear.NOSWEAR + targetPlayer.getName() + " is not muted!");
			return;
		}

		noSwear.getMuted().remove(targetPlayer.getName());
		noSwear.getConfig().set("Muted", noSwear.getMuted());
		noSwear.saveConfig();
		player.sendMessage(noSwear.NOSWEAR + ChatColor.AQUA + targetPlayer.getName() + ChatColor.WHITE + " has been unmuted!");
	}

	/**
	 * Kick a player.
     * @param player
     */
    private void kickPlayer(final Player player){
		noSwear.getServer().getScheduler().scheduleSyncDelayedTask(noSwear, new Runnable(){
			public void run(){
				player.kickPlayer(getMessage(player, "Message.Kick"));
			}
		}, 1L);
	}

	/**
	 * Ban a player, will also kick the player.
	 * @param player
	 */
    private void banPlayer(final Player player){
		noSwear.getServer().getScheduler().scheduleSyncDelayedTask(noSwear, new Runnable(){
			public void run(){
				Bukkit.getBanList(Type.NAME).addBan(player.getName(), getMessage(player, "Message.Ban"), null, null);
				player.kickPlayer(getMessage(player, "Message.Ban"));
			}
		}, 1L);
	}

	private void killPlayer(final Player player, final WarningType type){
		noSwear.getServer().getScheduler().scheduleSyncDelayedTask(noSwear, new Runnable(){
			public void run(){
				player.setHealth(0.0D);
				sendWarningMessage(player, type);
			}
		}, 1L);
	}

	private void chargePlayer(Player player){
        if (!noSwear.SET_ECONOMY)
            return;

		double amount = noSwear.getConfig().getDouble("OnWarningsDeplete.Economy.ChargeAmount");
		if (amount > 0){
			noSwear.getEconomy().withdrawPlayer(player, amount);
			player.sendMessage(getMessage(player, "Message.EconomyCharge")
					.replace("%AMOUNT%", String.valueOf(amount)));
		}
	}

	public String replaceSwearWord(String word){
		StringBuilder sb = new StringBuilder(word.length());
		for (int i = 0; i < word.length(); i++) {
			sb.append('*');
		}
		return sb.toString();
	}

    /* Utils */
    public final void displayRemainingWarnings(Player player){
        player.sendMessage(noSwear.getPrefix() + colour(
                noSwear.getConfig().getString("Message.Remaining")
                        .replace("%REMAINING%", String.valueOf(getRemainingWarnings(player.getName())))
                        .replace("%DEFAULT%", String.valueOf(getDefaultWarnings()))));
    }

    public final String colour(String S) {
        return ChatColor.translateAlternateColorCodes('&', S);
    }

    public int getDefaultWarnings() {
        return noSwear.getConfig().getInt("Warnings", 3);
    }

    public int getRemainingWarnings(String PlayerName) {
        return noSwear.getConfig().getInt("Warned." + PlayerName, getDefaultWarnings());
    }

    public void setRemainingWarnings(String PlayerName, int warnRemaining) {
        noSwear.getConfig().set("Warned." + PlayerName, warnRemaining);
        noSwear.saveConfig();
    }
}
