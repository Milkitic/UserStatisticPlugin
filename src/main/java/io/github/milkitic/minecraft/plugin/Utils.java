package io.github.milkitic.minecraft.plugin;

import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Utils {
    public static String getTimeStringBySeconds(long totalSecond) {
        long second = totalSecond % 60;
        long minute = totalSecond / 60;
        long hour = minute / 60;
        long day = hour / 24;
        StringBuilder sb = new StringBuilder();
        if (day > 0) {
            sb.append(day).append("天");
        }

        if (hour > 0) {
            sb.append(hour).append("小时");
        }

        if (minute > 0 && day == 0) {
            sb.append(minute).append("分钟");
        }

        if (second > 0 && day == 0 && hour == 0) {
            sb.append(second).append("秒");
        }

        return sb.toString();
    }

    public static long getPlayerTotalTime(String playerName,
                                          HashMap<String, Long> totalSecondMap,
                                          HashMap<String, Calendar> currentDateMap) {
        long addTime = getPlayerCurrentTime(playerName, currentDateMap);
        if (!totalSecondMap.containsKey(playerName)) {
            return addTime;
        }

        long newTotalTime = totalSecondMap.get(playerName) + addTime;
        return newTotalTime;
    }

    public static long getPlayerCurrentTime(String playerName,
                                            HashMap<String, Calendar> currentDateMap) {
        if (!currentDateMap.containsKey(playerName)) {
            return 0;
        }

        long addTime = (Calendar.getInstance().getTimeInMillis() -
                currentDateMap.get(playerName).getTimeInMillis()) / 1000;
        return addTime;
    }

    public static String getFriendlyHp(double hp) {
        return String.format("%.2f", Math.abs(hp) / 2);
    }

    public static HashMap<String, Long> readConfig(Path path) {
        HashMap<String, Long> hshMap = new HashMap<>();
        try (FileReader reader = new FileReader(path.toString());
             BufferedReader br = new BufferedReader(reader)
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                int index = line.indexOf('=');
                if (index == -1) continue;

                String name = line.substring(0, index);
                String strSecs = line.substring(index + 1);
                long lngSecs = Long.parseLong(strSecs);
                hshMap.put(name, lngSecs);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return hshMap;
    }

    public static void writeConfig(Path path, HashMap<String, Long> totalSecondMap, Logger logger) throws IOException {
        File file = path.toFile();
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();

        try (FileWriter writer = new FileWriter(file)) {

            for (Map.Entry<String, Long> tuple : totalSecondMap.entrySet()) {
                String str = MessageFormat.format("{0}={1}\n", tuple.getKey(), tuple.getValue());
                writer.write(str);
            }

            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
