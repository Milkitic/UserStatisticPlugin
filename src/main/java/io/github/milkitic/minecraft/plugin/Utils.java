package io.github.milkitic.minecraft.plugin;

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
                                          Map<String, Long> totalSecondMap,
                                          Map<String, Calendar> currentDateMap) {
        long addTime = getPlayerCurrentTime(playerName, currentDateMap);
        long newTotalTime = totalSecondMap.get(playerName) + addTime;
        return newTotalTime;
    }

    public static long getPlayerCurrentTime(String playerName,
                                            Map<String, Calendar> currentDateMap) {
        if (!currentDateMap.containsKey(playerName)) {
            return 0;
        }

        long addTime = (Calendar.getInstance().getTimeInMillis() -
                currentDateMap.get(playerName).getTimeInMillis()) / 1000;
        return addTime;
    }

    public static void updateAllPlayersTotalTime(Calendar lastUpdated,
                                                Map<String, Calendar> currentDateMap,
                                                Map<String, Long> totalSecondMap) {
        totalSecondMap.forEach((k, v)->{
            if(currentDateMap.containsKey(k)){
                Calendar loginDate = currentDateMap.get(k);
                long newTotalTime;
                if(lastUpdated.after(loginDate)){
                    newTotalTime = totalSecondMap.get(k) + lastUpdated.getTimeInMillis();
                }else{
                    newTotalTime = totalSecondMap.get(k) + loginDate.getTimeInMillis();
                }
                totalSecondMap.replace(k, newTotalTime);
            }
        });
    }

    public static String getFriendlyHp(double hp) {
        return String.format("%.2f", Math.abs(hp) / 2);
    }

    public static Map<String, Long> readConfig(Path path) {
        Map<String, Long> hshMap = new HashMap<>();
        try (FileReader reader = new FileReader(path.toString());
            BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                if(line.contains("=")){
                    String[] lineSplits = line.split("=");
                    hshMap.put(lineSplits[0], Long.parseLong(lineSplits[1]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return hshMap;
    }

    public static void writeConfig(Path path, Map<String, Long> totalSecondMap){
        File file = path.toFile();
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
