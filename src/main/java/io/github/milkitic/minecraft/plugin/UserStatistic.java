package io.github.milkitic.minecraft.plugin;

import com.google.inject.Inject;
import io.github.milkitic.minecraft.plugin.command.MyTimeCommand;
import io.github.milkitic.minecraft.plugin.template.text.DamageTextTemplate;
import io.github.milkitic.minecraft.plugin.template.text.NotifyOnJoinTextTemplate;
import io.github.milkitic.minecraft.plugin.template.text.NotifyTextTemplate;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Plugin(
        version = "1.0.0",
        id = "userstatistic",
        name = "UserStatistic",
        description = "User Statistic Plugin",
        url = "https://github.com/Milkitic",
        authors = {
                "Milkitic"
        }
)
public class UserStatistic {

    @Inject
    private Logger _logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    private Map<String, Long> _totalSecondMap;
    private Map<String, Calendar> _currentDateMap; //login date

    public static Calendar lastUpdated;

    @Listener
    public void onPreInitialization(GamePreInitializationEvent event) throws InterruptedException, IOException {
        Path dataPath = getDataPath();
        File file = dataPath.toFile();
        if (file.exists()) {
            _totalSecondMap = Utils.readConfig(dataPath);
        } else {
            _totalSecondMap = new HashMap<>();
        }

        lastUpdated = Calendar.getInstance();
        _currentDateMap = new HashMap<>();
        createCommands();
        runLoopNotifyTask();

        _logger.warn("UserStatistic plugin was successfully initialized.");
    }

    @Listener
    public void onServerStop(GameStoppedServerEvent event) {
        Utils.updateAllPlayersTotalTime(lastUpdated, _currentDateMap, _totalSecondMap);
        Utils.writeConfig(getDataPath(), _totalSecondMap);
    }

    private void runLoopNotifyTask() {
        Task.builder()
                .interval(15, TimeUnit.MINUTES)
                .delay(5, TimeUnit.MINUTES)
                .execute(() -> {
                    Utils.updateAllPlayersTotalTime(lastUpdated, _currentDateMap, _totalSecondMap);
                    MessageChannel publicChannel = Sponge.getServer().getBroadcastChannel();
                    Text text = new NotifyTextTemplate(_totalSecondMap).build();
                    publicChannel.send(text);
                    lastUpdated = Calendar.getInstance();
                })
                .name("UserStatistic - Public Message Loop")
                .submit(this);
    }

    private void createCommands() {
        CommandSpec myTimeCmdSpec = CommandSpec.builder()
                .description(Text.of("Get players' play time."))
                .executor(new MyTimeCommand(_totalSecondMap, _currentDateMap))
                .build();
        Sponge.getCommandManager().register(this, myTimeCmdSpec, "mystats");
    }

    @Listener
    public void onPlayerJoined(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        MessageChannel privateChannel = MessageChannel.fixed(player);

        String playerName = player.getName();
        _currentDateMap.put(playerName, Calendar.getInstance());

        _totalSecondMap.computeIfPresent(playerName, (k, v)->{
            Text text = new NotifyOnJoinTextTemplate(k, v).build();
            privateChannel.send(text);
            return v;
        });
        _totalSecondMap.putIfAbsent(playerName, 0L);
    }

    @Listener
    public void onPlayerLeaved(ClientConnectionEvent.Disconnect event) {
        Player player = event.getTargetEntity();
        String playerName = player.getName();

        long newTotalTime = Utils.getPlayerTotalTime(playerName, _totalSecondMap, _currentDateMap);
        _currentDateMap.remove(playerName);
        _totalSecondMap.replace(playerName, newTotalTime);

        Path dataPath = getDataPath();
        Utils.writeConfig(dataPath, _totalSecondMap);
        _logger.info(MessageFormat.format("{0}\'s Time: {1}",
                playerName,
                Utils.getTimeStringBySeconds(newTotalTime))
        );
    }

    @Listener
    public void onPlayerDamaged(DamageEntityEvent event) {
        Entity targetEntity = event.getTargetEntity();
        if (!(targetEntity instanceof Player)) {
            return;
        }

        MessageChannel broadcastChannel = Sponge.getServer().getBroadcastChannel();
        Player targetPlayer = (Player) targetEntity;
        Object eventSource = event.getSource();
        double damage = event.getFinalDamage();

        if (eventSource instanceof EntityDamageSource) {
            EntityDamageSource damageSource = (EntityDamageSource) eventSource;
            Entity sourceEntity = damageSource.getSource();

            Text text = new DamageTextTemplate(sourceEntity, targetPlayer, damage).build();
            if (text != null) {
                broadcastChannel.send(text);
            }

        } else if (eventSource instanceof DamageSource) {
            DamageSource damageSource = (DamageSource) eventSource;
            DamageType damageType = damageSource.getType();
            String damageTypeName = damageType.getName();

            Text text = new DamageTextTemplate(damageTypeName, targetPlayer, damage).build();
            if (text != null) {
                broadcastChannel.send(text);
            }
        }
    }

    public Path getDataPath() {
        String dataFile = "userdata.json";
        return configDir.resolve(dataFile);
    }
}
