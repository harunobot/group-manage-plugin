/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.harunobot.plugin.groupmaster.matcher;

import io.github.harunobot.proto.event.BotMessage;
import io.github.harunobot.proto.event.type.MessageContentType;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author iTeam_VEP
 */
public class PlainStringMessageMatcher implements MessageMatcher {
    
    private final String text;
    private final Set<String> excludedTexts;
    
    public PlainStringMessageMatcher(String text, List<String> excludedTexts){
        this.text = text;
        if(excludedTexts != null && !excludedTexts.isEmpty()){
            this.excludedTexts = new HashSet(excludedTexts);
        } else {
            this.excludedTexts = null;
        }
    }

    @Override
    public boolean allow(BotMessage[] messages) {
        for(BotMessage message:messages){
            if(message.messageType() == MessageContentType.TEXT){
                if(message.data().contains(text)){
                    if(excludedTexts == null){
                        return false;
                    }
                    Optional<String> excludedText = excludedTexts
                            .parallelStream()
                            .filter(excludedString -> message.data().contains(excludedString))
                            .findFirst()
                            ;
                    if(excludedText.isEmpty()){
                        return false;
                    }
                    return message.data().indexOf(text) == message.data().indexOf(excludedText.get());
                }
            }
        }
        return true;
    }
    
}
