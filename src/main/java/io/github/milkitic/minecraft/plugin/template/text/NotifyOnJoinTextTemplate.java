package io.github.milkitic.minecraft.plugin.template.text;

import io.github.milkitic.minecraft.plugin.Utils;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

public class NotifyOnJoinTextTemplate implements ITextTemplate {
    private String _name;
    private long _seconds;

    public NotifyOnJoinTextTemplate(String name, long seconds) {
        _name = name;
        _seconds = seconds;
    }

    @Override
    public Text build() {
        return Text.builder()
                .append(Text.of(
                        TextColors.GOLD,
                        _name + ", 你的累计在线时长为"
                ))
                .append(Text.of(
                        TextColors.YELLOW,
                        TextStyles.BOLD,
                        Utils.getTimeStringBySeconds(_seconds)
                ))
                .append(Text.of(
                        TextColors.GOLD,
                        "。"
                ))
                .build();
    }
}
