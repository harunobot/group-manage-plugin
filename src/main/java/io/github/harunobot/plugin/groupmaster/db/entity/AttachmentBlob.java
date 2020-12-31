/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.harunobot.plugin.groupmaster.db.entity;

import java.sql.Blob;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author iTeam_VEP
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public class AttachmentBlob {
    @Lob
    private Blob attachment;
}
