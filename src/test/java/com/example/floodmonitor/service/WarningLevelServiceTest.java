package com.example.floodmonitor.service;

import com.example.floodmonitor.model.Location;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.WarningLevel;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;

class WarningLevelServiceTest {
    private final Station station = new Station("ST-TEST", "Test", "Testfluss",
            new Location(48.0, 16.0), 150.0, 175.0, 200.0, true);

    private final WarningLevelService service =
            new WarningLevelService(Clock.systemUTC(), null, null, 60_000);

    @Test
    void waterLevelBelowWarningIsNormal() {
        WarningLevel result = service.getWarningLevel(station, 160.0);
        assertThat(result).isEqualTo(WarningLevel.NORMAL);
    }

    @Test
    void waterLevelBetweenWarningAndCriticalIsWarning() {
        WarningLevel result = service.getWarningLevel(station, 190.0);
        assertThat(result).isEqualTo(WarningLevel.WARNING);
    }

    @Test
    void waterLevelAboveCriticalIsCritical() {
        WarningLevel result = service.getWarningLevel(station, 210.0);
        assertThat(result).isEqualTo(WarningLevel.CRITICAL);
    }
}
