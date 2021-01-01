/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.harunobot.plugin.groupmaster.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.harunobot.proto.event.BotMessageRecord;
import java.util.List;

/**
 *
 * @author iTeam_VEP
 */
public class Turnover {
    private boolean enable;
    private List<BotMessageRecord> welcome;
    private List<BotMessageRecord> farewell;
    @JsonProperty(value="silence-timeout")
    private int silenceTimeout;
    @JsonProperty(value="silence-timeout-warning")
    private String silenceTimeoutWarning;
    @JsonProperty(value="ban-if-silence-timeout")
    private boolean banIfSilenceTimeout;
    @JsonProperty(value="ban-if-leave")
    private boolean banLeave;
    @JsonProperty(value="reject-join")
    private boolean rejectJoin;

    /**
     * @return the enable
     */
    public boolean isEnable() {
        return enable;
    }

    /**
     * @param enable the enable to set
     */
    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    /**
     * @return the welcome
     */
    public List<BotMessageRecord> getWelcome() {
        return welcome;
    }

    /**
     * @param welcome the welcome to set
     */
    public void setWelcome(List<BotMessageRecord> welcome) {
        this.welcome = welcome;
    }

    /**
     * @return the farewell
     */
    public List<BotMessageRecord> getFarewell() {
        return farewell;
    }

    /**
     * @param farewell the farewell to set
     */
    public void setFarewell(List<BotMessageRecord> farewell) {
        this.farewell = farewell;
    }

    /**
     * @return the silenceTimeout
     */
    public int getSilenceTimeout() {
        return silenceTimeout;
    }

    /**
     * @param silenceTimeout the silenceTimeout to set
     */
    public void setSilenceTimeout(int silenceTimeout) {
        this.silenceTimeout = silenceTimeout;
    }

    /**
     * @return the banLeave
     */
    public boolean isBanLeave() {
        return banLeave;
    }

    /**
     * @param banLeave the banLeave to set
     */
    public void setBanLeave(boolean banLeave) {
        this.banLeave = banLeave;
    }

    /**
     * @return the silenceTimeoutWarning
     */
    public String getSilenceTimeoutWarning() {
        return silenceTimeoutWarning;
    }

    /**
     * @param silenceTimeoutWarning the silenceTimeoutWarning to set
     */
    public void setSilenceTimeoutWarning(String silenceTimeoutWarning) {
        this.silenceTimeoutWarning = silenceTimeoutWarning;
    }
    
    /**
     * @return the rejectJoin
     */
    public boolean isRejectJoin() {
        return rejectJoin;
    }

    /**
     * @param rejectJoin the rejectJoin to set
     */
    public void setRejectJoin(boolean rejectJoin) {
        this.rejectJoin = rejectJoin;
    }

    /**
     * @return the banIfSilenceTimeout
     */
    public boolean isBanIfSilenceTimeout() {
        return banIfSilenceTimeout;
    }

    /**
     * @param banIfSilenceTimeout the banIfSilenceTimeout to set
     */
    public void setBanIfSilenceTimeout(boolean banIfSilenceTimeout) {
        this.banIfSilenceTimeout = banIfSilenceTimeout;
    }
}
