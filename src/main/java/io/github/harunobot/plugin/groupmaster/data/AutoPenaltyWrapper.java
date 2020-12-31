/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.harunobot.plugin.groupmaster.data;

import java.util.List;

/**
 *
 * @author iTeam_VEP
 */
public class AutoPenaltyWrapper {
    private final String id;
    private final List<AutoPenaltyMatcherWrapper> penalties;
    
    public AutoPenaltyWrapper(String id, List<AutoPenaltyMatcherWrapper> penalties){
        this.id = id;
        this.penalties = penalties;
    }

    public String id() {
        return id;
    }
    
    public List<AutoPenaltyMatcherWrapper> penalties() {
        return penalties;
    }

}
