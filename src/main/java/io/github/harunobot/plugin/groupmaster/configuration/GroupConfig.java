/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.harunobot.plugin.groupmaster.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 *
 * @author iTeam_VEP
 */
public class GroupConfig {
    @JsonProperty(value="group")
    private long groupId;
    @JsonProperty(value="penalties")
    private List<GroupAutoPenaltyConfig> penalties;
    private Turnover turnover;
    private List<Long> admins;
    

    /**
     * @return the groupId
     */
    public long getGroupId() {
        return groupId;
    }

    /**
     * @param groupId the groupId to set
     */
    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    /**
     * @return the admins
     */
    public List<Long> getAdmins() {
        return admins;
    }

    /**
     * @param admins the admins to set
     */
    public void setAdmins(List<Long> admins) {
        this.admins = admins;
    }

    /**
     * @return the penalties
     */
    public List<GroupAutoPenaltyConfig> getPenalties() {
        return penalties;
    }

    /**
     * @param penalties the penalties to set
     */
    public void setPenalties(List<GroupAutoPenaltyConfig> penalties) {
        this.penalties = penalties;
    }

    /**
     * @return the turnover
     */
    public Turnover getTurnover() {
        return turnover;
    }

    /**
     * @param turnover the turnover to set
     */
    public void setTurnover(Turnover turnover) {
        this.turnover = turnover;
    }
    
}
