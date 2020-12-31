/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.harunobot.plugin.groupmaster.matcher;

import io.github.harunobot.proto.event.BotMessage;
import io.github.harunobot.proto.event.BotMessageRecord;
import io.github.harunobot.proto.event.type.MessageContentType;
import java.util.Optional;

/**
 *
 * @author iTeam_VEP
 */
public class BotMessageMatcher implements MessageMatcher {
    
    private final MessageContentType messageType;
    private final String data;
    private final String file;
    private final String url;
    
    public BotMessageMatcher(BotMessageRecord record){
        this.messageType = record.getRecord();
        this.data = record.getData();
        this.file = record.getFile();
        this.url = record.getUrl();
    }

    @Override
    public boolean allow(BotMessage[] messages) {
        for(BotMessage message:messages){
            if(this.messageType == message.messageType()){
                if(this.messageType == MessageContentType.MEME){
                    if(this.data.equals(message.data())){
                        return false;
                    }
                } else if(this.messageType == MessageContentType.IMAGE){
                    if(this.file.equals(message.file())){
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
}
