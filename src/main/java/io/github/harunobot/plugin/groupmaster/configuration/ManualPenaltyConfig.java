/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.harunobot.plugin.groupmaster.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author iTeam_VEP
 */
public class ManualPenaltyConfig {
    private boolean global;
    @JsonProperty(value="global-operators")
    private List<Long> globalOperators;
    private Map<Long, List<Long>> groups;
    @JsonProperty(value="mute-cmd")
    private String mute;
    @JsonProperty(value="block-cmd")
    private String block;
    @JsonProperty(value="ban-cmd")
    private String ban;
    @JsonProperty(value="global-ban-cmd")
    private String globalBan;
    @JsonProperty(value="mute-duration")
    private int muteDuration;

    /**
     * @return the global
     */
    public boolean isGlobal() {
        return global;
    }

    /**
     * @param global the global to set
     */
    public void setGlobal(boolean global) {
        this.global = global;
    }

    /**
     * @return the groups
     */
    public Map<Long, List<Long>> getGroups() {
        return groups;
    }

    /**
     * @param groups the groups to set
     */
    public void setGroups(Map<Long, List<Long>> groups) {
        this.groups = groups;
    }

    /**
     * @return the mute
     */
    public String getMute() {
        return mute;
    }

    /**
     * @param mute the mute to set
     */
    public void setMute(String mute) {
        this.mute = mute;
    }

    /**
     * @return the ban
     */
    public String getBan() {
        return ban;
    }

    /**
     * @param ban the ban to set
     */
    public void setBan(String ban) {
        this.ban = ban;
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
     * @return the globalOperators
     */
    public List<Long> getGlobalOperators() {
        return globalOperators;
    }

    /**
     * @param globalOperators the globalOperators to set
     */
    public void setGlobalOperators(List<Long> globalOperators) {
        this.globalOperators = globalOperators;
    }

    /**
     * @return the block
     */
    public String getBlock() {
        return block;
    }

    /**
     * @param block the block to set
     */
    public void setBlock(String block) {
        this.block = block;
    }

    /**
     * @return the globalBan
     */
    public String getGlobalBan() {
        return globalBan;
    }

    /**
     * @param globalBan the globalBan to set
     */
    public void setGlobalBan(String globalBan) {
        this.globalBan = globalBan;
    }

}
