/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.harunobot.plugin.groupmaster.data;

import io.github.harunobot.plugin.groupmaster.configuration.ManualPenaltyConfig;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author iTeam_VEP
 */
public class ManualPenaltyWrapper {
    private final boolean global;
    private final Set<Long> globalOperators;
    private final Map<Long, Set<Long>> groups;
    private final String mute;
    private final String block;
    private final String ban;
    private final String globalBan;
    private String splitChar;
    private final int muteDuration;
    
    public ManualPenaltyWrapper(ManualPenaltyConfig manualPenaltyConfig){
        this.global = manualPenaltyConfig.isGlobal();
        this.globalOperators = new HashSet(manualPenaltyConfig.getGlobalOperators());
        if(manualPenaltyConfig.getGroups() != null && !manualPenaltyConfig.getGroups().isEmpty()){
            this.groups = new HashMap();
            manualPenaltyConfig.getGroups().forEach((groupId, operators) -> this.groups.put(groupId, new HashSet(operators)));
        } else {
            this.groups = null;
        }
        this.mute = manualPenaltyConfig.getMute();
        this.block = manualPenaltyConfig.getBlock();
        this.ban = manualPenaltyConfig.getBan();
        this.globalBan = manualPenaltyConfig.getGlobalBan();
        this.splitChar = manualPenaltyConfig.getCommandSplitChar();
        this.muteDuration = manualPenaltyConfig.getMuteDuration();
    }
    
    public boolean allow(long group, long operator){
        if(global && globalOperators.contains(operator)){
            return true;
        }
        return groups.containsKey(group) && groups.get(group).contains(operator);
    }
    
    public boolean allowGlobal(long operator){
        return global && globalOperators.contains(operator);
    }

    /**
     * @return the global
     */
    public boolean global() {
        
        return global;
    }


    /**
     * @return the mute
     */
    public String mute() {
        return mute;
    }

    /**
     * @return the warn
     */
    public String block() {
        return block;
    }

    /**
     * @return the ban
     */
    public String ban() {
        return ban;
    }

    /**
     * @return the muteDuration
     */
    public int muteDuration() {
        return muteDuration;
    }

    /**
     * @return the globalBan
     */
    public String globalBan() {
        return globalBan;
    }

    /**
     * @return the splitChar
     */
    public String splitChar() {
        return splitChar;
    }
    
}
