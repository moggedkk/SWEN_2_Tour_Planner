package com.backend.backend.service;

import com.backend.backend.model.entity.TourLog;
import com.backend.backend.service.implementation.TourAttributeCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Pure unit tests — no Spring, no mocks. The calculator only has math in it.
class TourAttributeCalculatorTest {

    private TourAttributeCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new TourAttributeCalculator();
    }

    // ---- popularity ----

    @Test
    void popularity_zeroLogs_isLow() {
        assertThat(calculator.computePopularity(0)).isEqualTo("Low");
    }

    @Test
    void popularity_oneLog_isMedium() {
        assertThat(calculator.computePopularity(1)).isEqualTo("Medium");
    }

    @Test
    void popularity_twoLogs_isMedium() {
        assertThat(calculator.computePopularity(2)).isEqualTo("Medium");
    }

    @Test
    void popularity_threeOrMoreLogs_isHigh() {
        assertThat(calculator.computePopularity(3)).isEqualTo("High");
        assertThat(calculator.computePopularity(100)).isEqualTo("High");
    }

    // ---- child friendliness ----

    @Test
    void childFriendliness_noLogs_isLow() {
        // can't infer anything from zero data
        assertThat(calculator.computeChildFriendliness(List.of())).isEqualTo("Low");
    }

    @Test
    void childFriendliness_easyShortTour_isHigh() {
        // 1km, 30 minutes, "easy" — easiest case
        TourLog log = buildLog("easy", 30, 1_000);
        assertThat(calculator.computeChildFriendliness(List.of(log))).isEqualTo("High");
    }

    @Test
    void childFriendliness_challengingLongTour_isLow() {
        // 50km, 5 hours, "challenging" — hardest case
        TourLog log = buildLog("challenging", 300, 50_000);
        assertThat(calculator.computeChildFriendliness(List.of(log))).isEqualTo("Low");
    }

    @Test
    void childFriendliness_moderateMediumTour_isMedium() {
        // 10km, 90 minutes, "moderate" — middle of the road
        TourLog log = buildLog("moderate", 90, 10_000);
        assertThat(calculator.computeChildFriendliness(List.of(log))).isEqualTo("Medium");
    }

    @Test
    void childFriendliness_averagesAcrossMultipleLogs() {
        // mix of easy/short and challenging/long — average should be Medium
        TourLog easyLog = buildLog("easy", 30, 1_000);          // score 9
        TourLog hardLog = buildLog("challenging", 300, 50_000); // score 1
        // avg = (9 + 1) / 2 = 5 -> Medium
        assertThat(calculator.computeChildFriendliness(List.of(easyLog, hardLog))).isEqualTo("Medium");
    }

    @Test
    void childFriendliness_unknownDifficulty_treatedAsModerate() {
        // garbage difficulty value should not crash and should score in the middle (2 pts)
        TourLog log = buildLog("???", 30, 1_000);
        // difficulty=2, time=3, distance=3 -> 8 -> High
        assertThat(calculator.computeChildFriendliness(List.of(log))).isEqualTo("High");
    }

    // helper to build a TourLog with just the fields the calculator cares about
    private TourLog buildLog(String difficulty, double totalTimeMin, double totalDistanceMeters) {
        TourLog log = new TourLog();
        log.setDifficulty(difficulty);
        log.setTotalTime(totalTimeMin);
        log.setTotalDistance(totalDistanceMeters);
        return log;
    }
}
