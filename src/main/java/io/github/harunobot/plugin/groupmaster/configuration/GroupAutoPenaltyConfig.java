/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.harunobot.plugin.groupmaster.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.harunobot.plugin.groupmaster.configuration.type.PunishmentType;
import java.util.List;

/**
 *
 * @author iTeam_VEP
 */
public class GroupAutoPenaltyConfig {
    private List<String> ids;
    private String warnMessage;
    private PunishmentType punishment;
    @JsonProperty(value="mute-duration")
    private int muteDuration;
    private boolean delete;
    private boolean counted;
    @JsonProperty(value="mute-max-count")
    private int muteMaxCount;
    @JsonProperty(value="ban-max-count")
    private int banMaxCount;
    @JsonProperty(value="max-mute-duration")
    private int maxMuteDuration;

    /**
     * @return the warnMessage
     */
    public String getWarnMessage() {
        return warnMessage;
    }

    /**
     * @param warnMessage the warnMessage to set
     */
    public void setWarnMessage(String warnMessage) {
        this.warnMessage = warnMessage;
    }

    /**
     * @return the punishment
     */
    public PunishmentType getPunishment() {
        return punishment;
    }

    /**
     * @param punishment the punishment to set
     */
    public void setPunishment(PunishmentType punishment) {
        this.punishment = punishment;
    }

    /**
     * @return the muteDuration
     */
    public int getMuteDuration() {
        return muteDuration;
    }

    /**
     * @param muteDuration the muteDuration to set
     */
    public void setMuteDuration(int muteDuration) {
        this.muteDuration = muteDuration;
    }

    /**
     * @return the maxMuteDuration
     */
    public int getMaxMuteDuration() {
        return maxMuteDuration;
    }

    /**
     * @param maxMuteDuration the maxMuteDuration to set
     */
    public void setMaxMuteDuration(int maxMuteDuration) {
        this.maxMuteDuration = maxMuteDuration;
    }

    /**
     * @return the delete
     */
    public boolean isDelete() {
        return delete;
    }

    /**
     * @param delete the delete to set
     */
    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    /**
     * @return the counted
     */
    public boolean isCounted() {
        return counted;
    }

    /**
     * @param counted the counted to set
     */
    public void setCounted(boolean counted) {
        this.counted = counted;
    }

    /**
     * @return the muteMaxCount
     */
    public int getMuteMaxCount() {
        return muteMaxCount;
    }

    /**
     * @param muteMaxCount the muteMaxCount to set
     */
    public void setMuteMaxCount(int muteMaxCount) {
        this.muteMaxCount = muteMaxCount;
    }

    /**
     * @return the banMaxCount
     */
    public int getBanMaxCount() {
        return banMaxCount;
    }

    /**
     * @param banMaxCount the banMaxCount to set
     */
    public void setBanMaxCount(int banMaxCount) {
        this.banMaxCount = banMaxCount;
    }

    /**
     * @return the ids
     */
    public List<String> getIds() {
        return ids;
    }

    /**
     * @param ids the ids to set
     */
    public void setIds(List<String> ids) {
        this.ids = ids;
    }
    
}
