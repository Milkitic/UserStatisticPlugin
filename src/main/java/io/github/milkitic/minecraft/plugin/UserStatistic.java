package io.github.milkitic.minecraft.plugin;

import com.google.inject.Inject;
import io.github.milkitic.minecraft.plugin.Generic.Tuple;
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
import java.util.List;
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

    private HashMap<String, Long> _totalSecondMap;
    private HashMap<String, Calendar> _currentDateMap;

    @Listener
    public void onPreInitialization(GamePreInitializationEvent event) throws InterruptedException, IOException {
        Path dataPath = getDataPath();
        File file = dataPath.toFile();
        if (file.exists()) {
            _totalSecondMap = Utils.readConfig(dataPath);
        } else {
            _totalSecondMap = new HashMap<>();
        }

        _currentDateMap = new HashMap<>();
        createCommands();
        runLoopNotifyTask();

        _logger.warn("UserStatistic plugin was successfully initialized.");
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
    }

    private void runLoopNotifyTask() {
        Task.builder()
                .interval(15, TimeUnit.MINUTES)
                .delay(5, TimeUnit.MINUTES)
                .execute(() -> {
                    MessageChannel publicChannel = Sponge.getServer().getBroadcastChannel();
                    NotifyTextTemplate templ = new NotifyTextTemplate(_totalSecondMap, _currentDateMap);
                    Text text = templ.build();
                    List<Tuple<String, Long>> tuples = templ.getTuples();

                    if (tuples != null) {
                        Path dataPath = getDataPath();
                        try {
                            Utils.writeConfig(dataPath, tuples, _logger);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    publicChannel.send(text);
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
        if (_currentDateMap.containsKey(playerName)) {
            _currentDateMap.replace(playerName, Calendar.getInstance());
        } else {
            _currentDateMap.put(playerName, Calendar.getInstance());
        }

        if (_totalSecondMap.containsKey(playerName)) {
            long seconds = _totalSecondMap.get(playerName);
            Text text = new NotifyOnJoinTextTemplate(playerName, seconds).build();
            privateChannel.send(text);
        } else {
            _totalSecondMap.put(playerName, 0L);
        }
    }

    @Listener
    public void onPlayerLeaved(ClientConnectionEvent.Disconnect event) throws IOException {
        Player player = event.getTargetEntity();
        String playerName = player.getName();

        long newTotalTime = Utils.getPlayerTotalTime(playerName, _totalSecondMap, _currentDateMap);
        _totalSecondMap.replace(playerName, newTotalTime);

        Path dataPath = getDataPath();
        Utils.writeConfig(dataPath, _totalSecondMap, _logger);
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
