/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.harunobot.plugin.groupmaster.matcher;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import io.github.harunobot.proto.event.BotMessage;
import io.github.harunobot.proto.event.type.MessageContentType;

/**
 *
 * @author iTeam_VEP
 */
public class RegexMessageMatcher implements MessageMatcher {

    private final RunAutomaton regex;
    
    public RegexMessageMatcher(String regex){
        this.regex = new RunAutomaton(new RegExp(regex).toAutomaton());
    }
    
    @Override
    public boolean allow(BotMessage[] messages) {
        for(BotMessage message:messages){
            if(message.messageType() == MessageContentType.TEXT){
                if(regex.run(message.data())){
                    return false;
                }
            }
        }
        return true;
    }
    
}
