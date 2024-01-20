package org.welcomejoin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Random;

public class BossBarManager {
    private BossBar bossBar;
    private int duration; // BossBar'ın gösterilme süresi (saniye cinsinden)

    public BossBarManager(FileConfiguration config) {
        boolean showTitle = config.getBoolean("bossbar.show-title", true);
        duration = config.getInt("bossbar.duration", 10); // Varsayılan 10 saniye

        bossBar = Bukkit.createBossBar("", getRandomBarColor(), BarStyle.SOLID);
        bossBar.setVisible(showTitle);
    }

    public void showBossBar(Player player) {
        bossBar.addPlayer(player);

        // BossBar'ı belirli bir süre sonra kaldırmak için bir zamanlayıcı kullanabilirsiniz
        Bukkit.getScheduler().runTaskTimerAsynchronously(WelcomeJoin.getPlugin(WelcomeJoin.class), () -> {
            updateBossBar();
        }, 0L, 20L); // 1 saniye = 20 tick olduğu için 20L, her saniye güncellenmesi için

        // BossBar'ı dolu olarak başlatın
        bossBar.setProgress(1.0);

        Bukkit.getScheduler().runTaskLater(WelcomeJoin.getPlugin(WelcomeJoin.class), () -> {
            bossBar.removePlayer(player);
        }, duration * 20L); // 1 saniye = 20 tick olduğu için süreyi tick cinsinden ayarlıyoruz
    }


    private void updateBossBar() {
        bossBar.setTitle(getRandomBossBarTitle());
        bossBar.setColor(getRandomBarColor());

        double progress = (double) bossBar.getProgress();
        double decreaseAmount = 1.0 / duration;

        if (progress >= decreaseAmount) {
            bossBar.setProgress(progress - decreaseAmount);
        } else {
            bossBar.setProgress(0.0);
        }
    }

    private String getRandomBossBarTitle() {
        // BossBar başlığını istediğiniz gibi oluşturun
        return ChatColor.translateAlternateColorCodes('&', "&aWelcome to the server!");
    }

    private BarColor getRandomBarColor() {
        // Rastgele bir BarColor seçin
        BarColor[] colors = BarColor.values();
        return colors[new Random().nextInt(colors.length)];
    }
}
