/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.harunobot.plugin.groupmaster.data;

import io.github.harunobot.plugin.groupmaster.configuration.Turnover;
import io.github.harunobot.proto.event.BotMessage;
import io.github.harunobot.proto.event.type.MessageContentType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author iTeam_VEP
 */
public class TurnoverWrapper {
    private boolean enable;
    private long groupId;
    private boolean enableWelcome;
    private List<BotMessage> welcome;
    private boolean enableFarewell;
    private List<BotMessage> farewell;
    private int silenceTimeout;
    private String silenceTimeoutWarning;
    private boolean recordSilenceTimeout;
    private boolean banLeave;
    private boolean rejectJoin;
    private int silenceUser = 0;
    
    public TurnoverWrapper(long groupId, Turnover turnover){
        this.groupId = groupId;
        this.enable = turnover.isEnable();
        if(turnover.getWelcome() != null && !turnover.getWelcome().isEmpty()) {
            this.enableWelcome = true;
            this.welcome = BotMessage.convertRecords(turnover.getWelcome());
        }
        if(turnover.getFarewell() != null && !turnover.getFarewell().isEmpty()) {
            this.enableFarewell = true;
            this.farewell = BotMessage.convertRecords(turnover.getFarewell());
        }
        this.silenceTimeout = turnover.getSilenceTimeout();
        this.recordSilenceTimeout = turnover.isRecordSilenceTimeout();
        this.silenceTimeoutWarning = turnover.getSilenceTimeoutWarning();
        this.banLeave = turnover.isBanLeave();
        this.rejectJoin = turnover.isRejectJoin();
    }
    
    public BotMessage[] generateWelcome(long userId){
        BotMessage[] welcomeMessages = new BotMessage[welcome.size()];
        for(int i=0; i<welcome.size(); i++){
            if(welcome.get(i).messageType() == MessageContentType.MENTION){
                welcomeMessages[i] = new BotMessage.Builder()
                        .messageType(MessageContentType.MENTION)
                        .data(String.valueOf(userId))
                        .build();
            } else {
                welcomeMessages[i] = welcome.get(i);
            }
        }
        return welcomeMessages;
    }
    
    public BotMessage[] generateFarewell(long userId){
        BotMessage[] farewellMessages = new BotMessage[farewell.size()];
        for(int i=0; i<farewell.size(); i++){
            if(farewell.get(i).messageType() == MessageContentType.MENTION){
                farewellMessages[i] = new BotMessage.Builder()
                        .messageType(MessageContentType.TEXT)
                        .data(new StringBuilder().append(" ").append(userId).append(" ").toString())
                        .build();
            } else {
                farewellMessages[i] = farewell.get(i);
            }
        }
        return farewellMessages;
    }
    
    public void addSilenceUser(){
        silenceUser++;
    }
    
    public void removeSilenceUser(){
        silenceUser--;
    }
    
    public boolean existSilenceUser(){
        if(silenceUser <0){
            throw new RuntimeException("WTF silenceUser is " + silenceUser);
        }
        return silenceUser > 0;
    }
    

    /**
     * @return the enable
     */
    public boolean isEnable() {
        return enable;
    }

    /**
     * @return the enableWelcome
     */
    public boolean isEnableWelcome() {
        return enableWelcome;
    }

    /**
     * @return the enableFarewell
     */
    public boolean isEnableFarewell() {
        return enableFarewell;
    }

    /**
     * @return the silenceTimeout
     */
    public int getSilenceTimeout() {
        return silenceTimeout;
    }

    /**
     * @return the silenceTimeoutWarning
     */
    public String getSilenceTimeoutWarning() {
        return silenceTimeoutWarning;
    }

    /**
     * @return the banLeave
     */
    public boolean isBanLeave() {
        return banLeave;
    }

    /**
     * @return the recordSilenceTimeout
     */
    public boolean isRecordSilenceTimeout() {
        return recordSilenceTimeout;
    }

    /**
     * @param recordSilenceTimeout the recordSilenceTimeout to set
     */
    public void setRecordSilenceTimeout(boolean recordSilenceTimeout) {
        this.recordSilenceTimeout = recordSilenceTimeout;
    }

    /**
     * @return the rejectJoin
     */
    public boolean isRejectJoin() {
        return rejectJoin;
    }
    
}
