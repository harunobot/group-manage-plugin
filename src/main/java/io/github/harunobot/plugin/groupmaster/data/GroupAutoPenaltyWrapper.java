/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.harunobot.plugin.groupmaster.data;

import io.github.harunobot.plugin.groupmaster.configuration.GroupAutoPenaltyConfig;
import io.github.harunobot.plugin.groupmaster.configuration.type.PunishmentType;
import io.github.harunobot.proto.event.BotMessage;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author iTeam_VEP
 */
public class GroupAutoPenaltyWrapper {
    private final String id;
    private final String warnMessage;
    private final PunishmentType punishment;
    private final Set<Long> admins;
    private final int muteDuration;
    private final int maxMuteDuration;
    private final boolean delete;
    private final boolean counted;
    private final int muteMaxCount;
    private final int banMaxCount;
    private final List<AutoPenaltyMatcherWrapper> penalties;
    
    public GroupAutoPenaltyWrapper(String id, GroupAutoPenaltyConfig config, Set<Long> admins, List<AutoPenaltyMatcherWrapper> penalties){
        this.id = id;
        this.admins = admins;
        this.punishment = config.getPunishment();
        this.warnMessage = config.getWarnMessage();
        this.muteDuration = config.getMuteDuration();
        this.maxMuteDuration = config.getMaxMuteDuration();
        this.delete = config.isDelete();
        this.counted = config.isCounted();
        this.muteMaxCount = config.getMuteMaxCount();
        this.banMaxCount = config.getBanMaxCount();
        this.penalties = penalties;
    }
    
    public boolean allow(long group, long user, BotMessage[] messages){
        if(admins.contains(user)){
            return true;
        }
        return penalties.stream().noneMatch(matcher -> (!matcher.allow(group, user, messages)));
    }

    /**
     * @return the id
     */
    public String id() {
        return id;
    }

    /**
     * @return the warnMessage
     */
    public String warnMessage() {
        return warnMessage;
    }

    /**
     * @return the punishment
     */
    public PunishmentType punishment() {
        return punishment;
    }

    /**
     * @return the muteDuration
     */
    public int muteDuration() {
        return muteDuration;
    }

    /**
     * @return the maxMuteDuration
     */
    public int maxMuteDuration() {
        return maxMuteDuration;
    }

    /**
     * @return the delete
     */
    public boolean delete() {
        return delete;
    }

    /**
     * @return the counted
     */
    public boolean counted() {
        return counted;
    }

    /**
     * @return the muteMaxCount
     */
    public int muteMaxCount() {
        return muteMaxCount;
    }

    /**
     * @return the banMaxCount
     */
    public int banMaxCount() {
        return banMaxCount;
    }

}
