package io.github.milkitic.minecraft.plugin.command;

import io.github.milkitic.minecraft.plugin.Utils;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Calendar;
import java.util.HashMap;

public class MyTimeCommand implements CommandExecutor {

    private final HashMap<String, Long> playerStatisticMap;
    private final HashMap<String, Calendar> nowDateMap;

    public MyTimeCommand(HashMap<String, Long> playerStatisticMap, HashMap<String, Calendar> nowDateMap) {
        this.playerStatisticMap = playerStatisticMap;
        this.nowDateMap = nowDateMap;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            Player player = (Player) src;
            String playerName = player.getName();
            long addTime = (Calendar.getInstance().getTimeInMillis() - nowDateMap.get(playerName).getTimeInMillis()) / 1000;
            long newTotalTime = playerStatisticMap.get(playerName) + addTime;

            player.sendMessage(Text.of("累计游戏时间： " + Utils.getTimeStringBySeconds(newTotalTime)));
            player.sendMessage(Text.of("本次在线时间： " + Utils.getTimeStringBySeconds(addTime)));
        } else {
            src.sendMessage(Text.of("This command can only be used by Player"));
        }

        return CommandResult.success();
    }
}
