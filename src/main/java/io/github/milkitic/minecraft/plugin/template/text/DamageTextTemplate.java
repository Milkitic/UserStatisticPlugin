package io.github.milkitic.minecraft.plugin.template.text;

import io.github.milkitic.minecraft.plugin.Utils;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.text.MessageFormat;

public class DamageTextTemplate implements ITextTemplate {
    private final double _damage;
    private String _behaviour = null;
    private Player _target;
    private Entity _source = null;

    public DamageTextTemplate(Entity source, Player target, double damage) {
        _source = source;
        _target = target;
        _damage = damage;
    }

    public DamageTextTemplate(String behaviour,Player target, double damage) {
        _behaviour = behaviour;
        _target = target;
        _damage = damage;
    }

    @Override
    public Text build() {
        if (_damage < 0.01)
            return null;
        if (_behaviour == null) {
            if (_source instanceof Player) {
                Player playerSource = (Player) _source;
                return Text.builder()
                        .append(Text.of(
                                TextColors.RED,
                                playerSource.getName() + " -> " + _target.getName() + " "
                                )
                        )
                        .append(Text.of(
                                TextColors.RED,
                                TextStyles.BOLD,
                                Utils.getFriendlyHp(_damage)
                                )
                        )
                        .append(Text.of(
                                TextColors.RED,
                                " ♥"
                                )
                        )
                        .build();
            } else {
                String mobName = _source.getType().getName();
                return Text.builder()
                        .append(Text.of(TextColors.GRAY,
                                MessageFormat.format("{0}正在被{1}揍。 (", _target.getName(), mobName)))
                        .append(Text.of(TextColors.GRAY, TextStyles.BOLD,
                                MessageFormat.format("-{0}", Utils.getFriendlyHp(_damage))))
                        .append(Text.of(TextColors.GRAY, " ♥)"))
                        .build();
            }
        }

        String friendlyBehaviour;
        switch (_behaviour) {
            case "fall":
                friendlyBehaviour = "在跳楼";
                break;
            case "drown":
                friendlyBehaviour = "假装自己是鱼";
                break;
            default:
                friendlyBehaviour = ": " + _behaviour;
                break;
        }

        return Text.builder()
                .append(Text.of(TextColors.GRAY,
                        MessageFormat.format("{0}{1}。 (", _target.getName(), friendlyBehaviour)))
                .append(Text.of(TextColors.GRAY, TextStyles.BOLD,
                        MessageFormat.format("-{0}", Utils.getFriendlyHp(_damage))))
                .append(Text.of(TextColors.GRAY, " ♥)"))
                .build();
    }
}
