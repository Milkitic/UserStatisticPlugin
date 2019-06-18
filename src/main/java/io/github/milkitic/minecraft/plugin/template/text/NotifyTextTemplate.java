package io.github.milkitic.minecraft.plugin.template.text;

import io.github.milkitic.minecraft.plugin.Utils;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Map.Entry.comparingByValue;

public class NotifyTextTemplate implements ITextTemplate {

    private Map<String, Long> _totalSecondMap;

    public NotifyTextTemplate(Map<String, Long> totalSecondMap) {
        _totalSecondMap = totalSecondMap;
    }

    @Override
    public Text build() {

        final Text.Builder builder = Text.builder()
                .append(Text.of(TextColors.AQUA, "游戏时间统计：\n"));

        AtomicInteger maxLen = new AtomicInteger();
        AtomicInteger iterationIndex = new AtomicInteger(0);

        _totalSecondMap.forEach((k, v)->{
            if (k.length() > maxLen.get()) {
                maxLen.set(k.length());
            }

            long totalTime = _totalSecondMap.get(k);
            _totalSecondMap.replace(k, totalTime);
        });

        _totalSecondMap.entrySet()
            .stream()
            .sorted(Collections.reverseOrder(comparingByValue()))
            .forEach(e -> {
                TextColor color;
                switch (iterationIndex.get()) {
                    case 0:
                        color = TextColors.GOLD;
                        break;
                    case 1:
                        color = TextColors.YELLOW;
                        break;
                    case 2:
                        color = TextColors.WHITE;
                        break;
                    default:
                        color = TextColors.GRAY;
                        break;
                }
                char[] tabs = new char[maxLen.get() - e.getKey().length()];
                Arrays.fill(tabs, ' ');
                builder.append(Text.of(color, MessageFormat.format("{0}:{1}{2} {3}\n",
                iterationIndex.get() + 1,
                e.getKey(),
                new String(tabs),
                Utils.getTimeStringBySeconds(e.getValue()))));
                iterationIndex.getAndIncrement();
            });
        return builder.build();
    }
}
