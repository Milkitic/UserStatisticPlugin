package io.github.milkitic.minecraft.plugin.template.text;

import io.github.milkitic.minecraft.plugin.Generic.Tuple;
import io.github.milkitic.minecraft.plugin.TupleListComparator;
import io.github.milkitic.minecraft.plugin.Utils;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.text.MessageFormat;
import java.util.*;

public class NotifyTextTemplate implements ITextTemplate {

    private HashMap<String, Long> _totalSecondMap;
    private HashMap<String, Calendar> _currentDateMap;
    private List<Tuple<String, Long>> _tuples;

    public NotifyTextTemplate(HashMap<String, Long> totalSecondMap, HashMap<String, Calendar> currentDateMap) {

        _totalSecondMap = totalSecondMap;
        _currentDateMap = currentDateMap;
    }

    @Override
    public Text build() {
        int maxLen = 0;
        List<Tuple<String, Long>> tuples = new ArrayList<>();

        for (Map.Entry<String, Long> entry : _totalSecondMap.entrySet()) {
            String name = entry.getKey();
            long recordTime = entry.getValue();

            if (name.length() > maxLen) {
                maxLen = name.length();
            }

            long totalTime = recordTime + Utils.getPlayerCurrentTime(name, _currentDateMap);
            tuples.add(new Tuple<>(name, totalTime));
        }

        tuples.sort(new TupleListComparator());
        this._tuples = tuples;
        Text.Builder builder = Text.builder()
                .append(Text.of(TextColors.AQUA, "游戏时间统计：\n"));
        for (int i = 0; i < tuples.size(); i++) {
            if (i == 8) {
                break;
            }
            Tuple<String, Long> tuple = tuples.get(i);
            TextColor color;
            switch (i) {
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

            builder = builder.append(Text.of(color, MessageFormat.format("{0}： {1}{2} {3}\n",
                    i + 1,
                    tuple.first,
                    //" ",
                    String.join("", Collections.nCopies(maxLen - tuple.first.length(), " ")),
                    Utils.getTimeStringBySeconds(tuple.second))));
        }
        return builder.build();
    }

    public List<Tuple<String, Long>> getTuples(){
        return _tuples;
    }
}
