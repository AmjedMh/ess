package com.teknokote.ess.core.model.shifts;

import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.time.LocalTime;

/**
 * Entité représentant les POSTEs (shifts) = une période dans la journée
 * Un poste a nom, index, un début et une fin
 */
@Entity
@Getter
@Setter
public class Shift extends ESSEntity<Long, User>
{
   @Serial
   private static final long serialVersionUID = 1530309875963821943L;

   private int index;
   private String name;
   private LocalTime startingTime;
   private LocalTime endingTime;
   private Boolean offDay; // Marque les shifts de type repos. Normalement, une rotation n'a qu'un shift de type repos
   private boolean crossesDayBoundary;// Marque les shifts qui qui passe la journéé courante
   @ManyToOne(fetch = FetchType.LAZY)
   private ShiftRotation shiftRotation;
   @Column(name = "shift_rotation_id",insertable = false, updatable = false)
   private Long shiftRotationId;
}
