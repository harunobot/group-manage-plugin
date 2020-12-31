/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.harunobot.plugin.groupmaster.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.harunobot.database.configuration.DatabaseConfiguration;
import java.util.List;

/**
 *
 * @author iTeam_VEP
 */
public class Configuration {
    @JsonProperty(value="enable-database")
    private boolean enableDatabase;
    private DatabaseConfiguration database;
    @JsonProperty(value="manual-penalty")
    private ManualPenaltyConfig manualPenalty;
    @JsonProperty(value="penalty-files")
    private List<AutoPenaltyFileConfig> penaltyFiles;
    @JsonProperty(value="global-penalty-configs")
    private List<GroupConfig> globalPenaltyConfigs;
    @JsonProperty(value="group-penalty-configs")
    private List<GroupConfig> groupPenaltyConfigs;
    @JsonProperty(value="super-admins")
    private List<Long> superAdmins;

    /**
     * @return the database
     */
    public DatabaseConfiguration getDatabase() {
        return database;
    }

    /**
     * @param database the database to set
     */
    public void setDatabase(DatabaseConfiguration database) {
        this.database = database;
    }

    /**
     * @return the manualPenalty
     */
    public ManualPenaltyConfig getManualPenalty() {
        return manualPenalty;
    }

    /**
     * @param manualPenalty the manualPenalty to set
     */
    public void setManualPenalty(ManualPenaltyConfig manualPenalty) {
        this.manualPenalty = manualPenalty;
    }

    /**
     * @return the enableDatabase
     */
    public boolean isEnableDatabase() {
        return enableDatabase;
    }

    /**
     * @param enableDatabase the enableDatabase to set
     */
    public void setEnableDatabase(boolean enableDatabase) {
        this.enableDatabase = enableDatabase;
    }

    /**
     * @return the penaltyFiles
     */
    public List<AutoPenaltyFileConfig> getPenaltyFiles() {
        return penaltyFiles;
    }

    /**
     * @param penaltyFiles the penaltyFiles to set
     */
    public void setPenaltyFiles(List<AutoPenaltyFileConfig> penaltyFiles) {
        this.penaltyFiles = penaltyFiles;
    }

    /**
     * @return the globalPenaltyConfigs
     */
    public List<GroupConfig> getGlobalPenaltyConfigs() {
        return globalPenaltyConfigs;
    }

    /**
     * @param globalPenaltyConfigs the globalPenaltyConfigs to set
     */
    public void setGlobalPenaltyConfigs(List<GroupConfig> globalPenaltyConfigs) {
        this.globalPenaltyConfigs = globalPenaltyConfigs;
    }

    /**
     * @return the groupPenaltyConfigs
     */
    public List<GroupConfig> getGroupPenaltyConfigs() {
        return groupPenaltyConfigs;
    }

    /**
     * @param groupPenaltyConfigs the groupPenaltyConfigs to set
     */
    public void setGroupPenaltyConfigs(List<GroupConfig> groupPenaltyConfigs) {
        this.groupPenaltyConfigs = groupPenaltyConfigs;
    }

    /**
     * @return the superAdmins
     */
    public List<Long> getSuperAdmins() {
        return superAdmins;
    }

    /**
     * @param superAdmins the superAdmins to set
     */
    public void setSuperAdmins(List<Long> superAdmins) {
        this.superAdmins = superAdmins;
    }
}
