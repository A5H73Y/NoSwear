package me.A5H73Y.NoSwear;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.A5H73Y.NoSwear.NoSwearMethods.WarningType;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class NoSwearListener implements Listener {

    private NoSwear noSwear;

    private HashMap<String, String> lastMessage = new HashMap<>();
    private HashMap<String, Long> lastMessageSent = new HashMap<>();

    private String[] website = { ".com", ".co.uk", ".net", ".tk", ".cc", ".org", "www.", "(dot)", "http:", "https:", ".ly", ".enjin", "mc."};

    NoSwearListener(NoSwear noSwear) {
        this.noSwear = noSwear;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent chat) {
        if (chat.getPlayer().hasPermission(noSwear.PERM_BYPASS) && !noSwear.SET_INCLUDE_OPS)
            return;

        Player player = chat.getPlayer();
        String message = chat.getMessage().toLowerCase();

        // Muted
        if (noSwear.getMuted().contains(player.getName())) {
            player.sendMessage(noSwear.getNoSwearMethods().getMessage(player, "Message.Muted"));
            chat.setCancelled(true);
            return;
        }

        // Block website
        if (noSwear.SET_WEBBLOCKER) {
            for (String site : website) {
                if (message.contains(site)) {
                    if (noSwear.getNoSwearMethods().playerPunish(player, WarningType.ADVERTISING))
                        chat.setCancelled(true);
                    return;
                }
            }

            if (noSwear.getConfig().getBoolean("Block.Websites.IncludeIPs")) {
                Pattern ipAddress = Pattern.compile("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$");
                Matcher match = ipAddress.matcher(message.replaceAll("[^0-9.]", ""));

                if (match.matches()) {
                    noSwear.getNoSwearMethods().playerPunish(player, WarningType.ADVERTISING);
                    chat.setCancelled(true);
                    return;
                }
            }
        }

        if (noSwear.SET_STRICT)
            message = message.replaceAll(noSwear.SET_STRICT_REGEX, "");

        if (noSwear.getConfig().getBoolean("OnSwear.StrictDetection.RepeatedLetters"))
            message = message.replaceAll("([a-z])\\1+", "$1$1");

        boolean swore = false;

        if (!noSwear.SET_OLD_DETECT) {
            String[] words = message.split(" ");

            for (String word : words) {
                for (String blockedWord : noSwear.getBlockedWords()) {
                    if (word.contains(blockedWord)) {
                        boolean isPardoned = false;

                        if (!noSwear.getWhiteWords().isEmpty()) {
                            for (String pardoned : noSwear.getWhiteWords()) {
                                if (word.equals(pardoned)) {
                                    isPardoned = true;
                                }
                            }
                        }

                        if (isPardoned)
                            continue;

                        if (noSwear.SET_REPLACE)
                            message = message.replace(blockedWord, noSwear.getNoSwearMethods().replaceSwearWord(blockedWord));

                        swore = true;
                    }
                }
            }

        } else {
            message = message.replace(" ", "");

            for (String blockedWord : noSwear.getBlockedWords()) {
                if (message.contains(blockedWord)) {
                    swore = true;
                }
            }
        }

        if (swore) {
            if (noSwear.getNoSwearMethods().playerPunish(player, WarningType.SWEARING))
                chat.setCancelled(true);

            if (noSwear.SET_MESSAGE_OP)
                noSwear.getNoSwearMethods().sendMessageToOps(player.getName(), chat.getMessage());

            if (noSwear.SET_REPLACE)
                chat.setMessage(message);

            return;
        }

        //Block caps
        if (noSwear.SET_CAPSBLOCKER && (message.length() >= 6)) {
            int upper = 0, lower = 0;
            for (int i = 0; i < chat.getMessage().length(); i++) {
                char character = chat.getMessage().charAt(i);
                if (Character.isLetter(character)) {
                    if (Character.isUpperCase(character)) {
                        upper++;
                    } else {
                        lower++;
                    }
                }
            }
            if (upper + lower != 0) {
                double percent = 1.0D * upper / (upper + lower) * 100.0D;
                if (percent >= noSwear.SET_CAPSBLOCKER_PERCENT) {
                    chat.setMessage(chat.getMessage().toLowerCase());
                    player.sendMessage(noSwear.getNoSwearMethods().getMessage(player, "Message.Caps"));
                }
            }
        }

        if (!noSwear.SET_SPAMBLOCKER)
            return;

        if (message.equals(lastMessage.get(player.getName()))) {
            noSwear.getNoSwearMethods().playerPunish(player, WarningType.SPAMMING);
            chat.setCancelled(true);
            return;
        }

        if (lastMessageSent.containsKey(player.getName())) {
            Long timeRemaining = lastMessageSent.get(player.getName()) + (noSwear.SET_SPAMBLOCKER_DELAY * 1000) - System.currentTimeMillis();

            if (timeRemaining > 0) {
                player.sendMessage(noSwear.getNoSwearMethods().getMessage(player, "Message.SpamWait").replace("%SECONDS%", String.valueOf((timeRemaining.intValue() / 1000) + 1)));
                chat.setCancelled(true);
                return;
            }
        }

        lastMessage.put(player.getName(), message);
        lastMessageSent.put(player.getName(), System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (noSwear.getConfig().getBoolean("OnJoin.WelcomeMessage"))
            event.getPlayer().sendMessage(noSwear.getNoSwearMethods().getMessage(event.getPlayer(), "Message.Join"));

        if (noSwear.getConfig().getBoolean("OnJoin.WarningsLeft"))
            noSwear.getNoSwearMethods().displayRemainingWarnings(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!noSwear.getConfig().getBoolean("Block.IncludeCommands"))
            return;

        if (event.getPlayer().hasPermission(noSwear.PERM_BYPASS) && !noSwear.SET_INCLUDE_OPS)
            return;

        String[] args = event.getMessage().split(" ");

        for (String arg : args) {
            arg = arg.toLowerCase();

            if (noSwear.SET_STRICT)
                arg = arg.replaceAll(noSwear.SET_STRICT_REGEX, "");

            if (noSwear.getBlockedWords().contains(arg)) {
                noSwear.getNoSwearMethods().playerPunish(event.getPlayer(), WarningType.SWEARING);
                event.setCancelled(true);
                return;
            }
        }

        if (!noSwear.SET_SPAMBLOCKER)
            return;

        if (lastMessageSent.containsKey(event.getPlayer().getName())) {
            Long timeRemaining = lastMessageSent.get(event.getPlayer().getName()) + (noSwear.SET_SPAMBLOCKER_DELAY * 1000) - System.currentTimeMillis();

            if (timeRemaining > 0) {
                event.getPlayer().sendMessage(noSwear.getNoSwearMethods().getMessage(event.getPlayer(), "Message.SpamWait").replace("%SECONDS%", String.valueOf((timeRemaining.intValue() / 1000) + 1)));
                event.setCancelled(true);
                return;
            }
        }

        lastMessageSent.put(event.getPlayer().getName(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSignCreate(SignChangeEvent event) {
        if (!noSwear.getConfig().getBoolean("Block.IncludeSigns"))
            return;

        if (event.getPlayer().hasPermission(noSwear.PERM_BYPASS) && !noSwear.SET_INCLUDE_OPS)
            return;

        for (String line : event.getLines()) {
            line = line.toLowerCase().replace(" ", "");

            if (noSwear.SET_STRICT)
                line = line.replaceAll(noSwear.SET_STRICT_REGEX, "");

            for (String blockedWord : noSwear.getBlockedWords()) {
                if (line.contains(blockedWord)) {
                    noSwear.getNoSwearMethods().playerPunish(event.getPlayer(), WarningType.SWEARING);
                    event.setCancelled(true);
                    event.getBlock().breakNaturally();
                    return;
                }
            }
        }
    }
}
