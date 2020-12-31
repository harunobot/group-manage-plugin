/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.harunobot.plugin.groupmaster.data;

import io.github.harunobot.plugin.groupmaster.configuration.AutoPenaltyMatcherConfig;
import io.github.harunobot.plugin.groupmaster.matcher.BotMessageMatcher;
import io.github.harunobot.proto.event.BotMessage;
import io.github.harunobot.plugin.groupmaster.matcher.MessageMatcher;
import io.github.harunobot.plugin.groupmaster.matcher.PlainStringMessageMatcher;
import io.github.harunobot.plugin.groupmaster.matcher.RegexMessageMatcher;

/**
 *
 * @author iTeam_VEP
 */
public class AutoPenaltyMatcherWrapper {
    private final MessageMatcher messageMatcher;
    
    public AutoPenaltyMatcherWrapper(AutoPenaltyMatcherConfig config){
        if(config.getText() != null){
            this.messageMatcher = new PlainStringMessageMatcher(config.getText(), config.getExcludeText());
        } else if(config.getRegex() != null){
            this.messageMatcher = new RegexMessageMatcher(config.getRegex());
        } else if(config.getRecord()!= null) {
            this.messageMatcher = new BotMessageMatcher(config.getRecord());
        } else {
            throw new IllegalArgumentException("could not find a suitable matcher");
        }
    }
    
    public boolean allow(long group, long user, BotMessage[] messages){
        return messageMatcher.allow(messages);
    }

    /**
     * @return the messageMatcher
     */
    public MessageMatcher messageMatcher() {
        return messageMatcher;
    }

}
