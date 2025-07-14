package org.devoxx4kids.spigot.plugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class MyPlugin extends JavaPlugin implements Listener, CommandExecutor {

    private Set<String> qualifiedPlayers = new HashSet<>();
    private Map<UUID, Long> joinTimes = new HashMap<>();
    private DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();

    private static final long TEN_MINUTES_MILLIS = 10 * 60 * 1000;


    @Override
    public void onEnable() {
        getLogger().info("TAXES plugin is ON");
        getLogger().info(ChatColor.GREEN + "как же я люблю волосатых мужиков, которые обмазываются маслов и еб....");
        File file = new File(getDataFolder(), "players.yml");

        if (file.exists()){ // если файл ещё не существует — пропускаем

            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            List<String> list = config.getStringList("players");

            qualifiedPlayers.addAll(list); // загружаем в наше множество
        }

        getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("taxes_players").setExecutor(this);

    }

    @Override
    public void onDisable() {
        File file = new File(getDataFolder(), "players.yml");

        YamlConfiguration config = new YamlConfiguration();
        config.set("players", new ArrayList<>(qualifiedPlayers)); // сохраняем как список

        try {
            config.save(file);
            getLogger().info("Список игроков сохранён.");
        } catch (IOException e) {
            getLogger().warning("Не удалось сохранить players.yml: " + e.getMessage());
        }       
        
        getLogger().info("TAXES plugin is OFF");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        joinTimes.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        if (LocalDate.now().getDayOfWeek() != dayOfWeek && LocalDate.now().getDayOfWeek() == DayOfWeek.MONDAY) {
            getLogger().info("List of players is cleared. Players:");
            for (String name : qualifiedPlayers) {
                getLogger().info("- " + name);
            }
            qualifiedPlayers.clear();
        }
        dayOfWeek = LocalDate.now().getDayOfWeek();
    }

    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        Long joinTime = joinTimes.remove(playerId);
        if (joinTime != null) {
            long timeSpent = System.currentTimeMillis() - joinTime;
            if (timeSpent >= TEN_MINUTES_MILLIS) {
                qualifiedPlayers.add(event.getPlayer().getName());
                getLogger().info(event.getPlayer().getName() + " провел на сервере больше 10 минут и добавлен в список.");
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("taxes_players")) {
                Player player = (Player) sender;
                if (qualifiedPlayers.isEmpty()) {
                    player.sendMessage("Пока нет игроков, которые должны платить налоги.");
                } else {
                    player.sendMessage("Игроки, которые должны заплатить налог за эту неделю:");
                    for (String name : qualifiedPlayers) {
                        player.sendMessage("- " + name);
                    }
                }
                return true;
        }
        return false;
    }
}
