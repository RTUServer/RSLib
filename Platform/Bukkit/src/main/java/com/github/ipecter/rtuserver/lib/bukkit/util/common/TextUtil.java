package com.github.ipecter.rtuserver.lib.bukkit.util.common;

import com.github.ipecter.rtuserver.lib.bukkit.RSLib;
import com.github.ipecter.rtuserver.lib.plugin.config.Configurations;
import com.github.ipecter.rtuserver.lib.plugin.config.MessageConfiguration;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class TextUtil {

    private static final Configurations configurations = RSLib.getInstance().getConfigurations();
    private static final MessageConfiguration message = configurations.getMessage();

    public static DataNameType checkType(List<String> list, String name) {
        if (name.matches("[^ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z0-9_]*$")) {
            return DataNameType.ALL;
        } else if (!name.matches("^[ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z0-9_]*$")) {
            return DataNameType.WRONG;
        }
        if (name.length() > 15) {
            return DataNameType.LONG;
        }
        if (Collections.frequency(list, name) >= 1) {
            return DataNameType.EXIST;
        }
        return DataNameType.PASS;
    }

    public static DataNameType checkType(Player player, List<String> list, String name) {
        Audience audience = RSLib.getInstance().getAdventure().player(player);
        DataNameType dataNameType = checkType(list, name);
        switch (dataNameType) {
            case ALL -> audience.sendMessage(ComponentUtil.formatted(message.get("dataNameType.all")));
            case WRONG -> audience.sendMessage(ComponentUtil.formatted(message.get("dataNameType.wrong")));
            case LONG -> audience.sendMessage(ComponentUtil.formatted(message.get("dataNameType.long")));
            case EXIST -> audience.sendMessage(ComponentUtil.formatted(message.get("dataNameType.exist")));
            default -> audience.sendMessage(ComponentUtil.formatted(message.get("")));
        }
        return dataNameType;
    }


    public enum DataNameType {
        /**
         * 이름 전체가 모두 잘못된 문자
         */
        ALL,
        /**
         * 이름에 잘못된 문자를 포함
         */
        WRONG,
        /**
         * 이름의 길이가 10자 초과
         */
        LONG,
        /**
         * 이미 존재하는 이름
         */
        EXIST,
        /**
         * 문제 없음
         */
        PASS
    }
}
