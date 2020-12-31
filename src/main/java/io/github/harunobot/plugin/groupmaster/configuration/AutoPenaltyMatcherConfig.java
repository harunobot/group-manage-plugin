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
public class AutoPenaltyMatcherConfig {
    private String text;
    private String regex;
    @JsonProperty(value="bot-message")
    private BotMessageRecord record;
    @JsonProperty(value="exclude-text")
    private List<String> excludeText;

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return the regex
     */
    public String getRegex() {
        return regex;
    }

    /**
     * @param regex the regex to set
     */
    public void setRegex(String regex) {
        this.regex = regex;
    }

    /**
     * @return the record
     */
    public BotMessageRecord getRecord() {
        return record;
    }

    /**
     * @param record the record to set
     */
    public void setRecord(BotMessageRecord record) {
        this.record = record;
    }

    /**
     * @return the excludeText
     */
    public List<String> getExcludeText() {
        return excludeText;
    }

    /**
     * @param excludeText the excludeText to set
     */
    public void setExcludeText(List<String> excludeText) {
        this.excludeText = excludeText;
    }

}
