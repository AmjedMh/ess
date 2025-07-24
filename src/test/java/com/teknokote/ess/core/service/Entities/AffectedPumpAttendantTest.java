package com.teknokote.ess.core.service.Entities;

import com.teknokote.ess.core.model.configuration.Pump;
import com.teknokote.ess.core.model.organization.PumpAttendant;
import com.teknokote.ess.core.model.organization.PumpAttendantTeam;
import com.teknokote.ess.core.model.shifts.AffectedPumpAttendant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AffectedPumpAttendantTest {
    private AffectedPumpAttendant affectedPumpAttendant;

    @BeforeEach
    void setUp() {
        affectedPumpAttendant = new AffectedPumpAttendant();
    }

    @Test
    void testSetAndGetPumpAttendantTeam() {
        PumpAttendantTeam team = new PumpAttendantTeam();
        team.setId(1L); // Assuming a setter exists in PumpAttendantTeam

        affectedPumpAttendant.setPumpAttendantTeam(team);
        assertEquals(team, affectedPumpAttendant.getPumpAttendantTeam());
    }

    @Test
    void testSetAndGetPumpAttendantTeamId() {
        affectedPumpAttendant.setPumpAttendantTeamId(2L);
        assertEquals(Long.valueOf(2), affectedPumpAttendant.getPumpAttendantTeamId());
    }

    @Test
    void testSetAndGetPump() {
        Pump pump = new Pump();
        pump.setId(3L); // Assuming a setter exists in Pump

        affectedPumpAttendant.setPump(pump);
        assertEquals(pump, affectedPumpAttendant.getPump());
    }

    @Test
    void testSetAndGetPumpId() {
        affectedPumpAttendant.setPumpId(4L);
        assertEquals(Long.valueOf(4), affectedPumpAttendant.getPumpId());
    }

    @Test
    void testSetAndGetPumpAttendant() {
        PumpAttendant attendant = new PumpAttendant();
        attendant.setId(5L); // Assuming a setter exists in PumpAttendant

        affectedPumpAttendant.setPumpAttendant(attendant);
        assertEquals(attendant, affectedPumpAttendant.getPumpAttendant());
    }

    @Test
    void testSetAndGetPumpAttendantId() {
        affectedPumpAttendant.setPumpAttendantId(6L);
        assertEquals(Long.valueOf(6), affectedPumpAttendant.getPumpAttendantId());
    }
}