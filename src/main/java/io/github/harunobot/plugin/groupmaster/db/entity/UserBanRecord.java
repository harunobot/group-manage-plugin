/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.harunobot.plugin.groupmaster.db.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import io.github.harunobot.plugin.groupmaster.db.type.CauseType;
import io.github.harunobot.plugin.groupmaster.db.type.StatusType;
import io.github.harunobot.proto.event.BotMessageRecord;
import java.sql.Blob;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

/**
 *
 * @author iTeam_VEP
 */
@Entity
@Valid
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_ban_record")
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonStringType.class),
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class),
})
public class UserBanRecord extends AttachmentBlob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "column_id")
    private long columnId;
    @Column(name = "user_id")
    private long userId;
    @Column(name = "group_id")
    private long groupId;
    @Column(name = "operator_id")
    private long operatorId;
    private boolean global;
    @Enumerated(EnumType.STRING)
    @Column(name = "cause_type")
    private CauseType causeType;
    private String cause;
    private boolean activated;
    @Column(columnDefinition = "jsonb")
    @Type(type = "jsonb")
    private BotMessageRecord[] messages;
    @Column(nullable = false, updatable = false)
//    @Temporal(TemporalType.TIMESTAMP)
    private Instant createtime;
    
    
}
