/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.harunobot.plugin.groupmaster.matcher;

import io.github.harunobot.proto.event.BotMessage;

/**
 *
 * @author iTeam_VEP
 */
public interface MessageMatcher {
    boolean allow(BotMessage[] messages);
}
