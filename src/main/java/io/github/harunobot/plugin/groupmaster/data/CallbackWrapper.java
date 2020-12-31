/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.harunobot.plugin.groupmaster.data;

import io.github.harunobot.plugin.groupmaster.configuration.type.PunishmentType;

/**
 *
 * @author iTeam_VEP
 */
public class CallbackWrapper {
    private PunishmentType type;
    private String message;
    private boolean global;
    
    public CallbackWrapper(PunishmentType type, String message, boolean global){
        this.type = type;
        this.message = message;
        this.global = global;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

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
     * @return the type
     */
    public PunishmentType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(PunishmentType type) {
        this.type = type;
    }
    
}
