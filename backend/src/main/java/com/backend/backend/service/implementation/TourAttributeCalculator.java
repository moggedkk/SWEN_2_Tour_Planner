package com.backend.backend.service.implementation;

import com.backend.backend.model.entity.TourLog;
import org.springframework.stereotype.Component;

import java.util.List;

// Computes the "automatic" tour attributes the spec asks for:
//   - popularity (derived from number of logs)
//   - child-friendliness (derived from difficulty + total time + distance across logs)
//
// Kept in its own class so:
//   1. the math is easy to find and reason about
//   2. it can be unit-tested without spinning up the database
//   3. the protocol can point to it as an example of the "Strategy" / single-responsibility pattern
@Component
public class TourAttributeCalculator {

    // ---- Popularity ----
    // Pure rule: more logs = more popular. We don't try to be clever about recency etc.
    //   0 logs   -> "Low"
    //   1-2 logs -> "Medium"
    //   3+ logs  -> "High"
    public String computePopularity(int logCount) {
        if (logCount <= 0) return "Low";
        if (logCount < 3)  return "Medium";
        return "High";
    }

    // ---- Child-friendliness ----
    // Per the spec, this uses difficulty + total time + distance from all logs.
    // We score each log out of 9 points (easier/shorter = more points), then average.
    //
    //   avg >= 7 -> "High"   (clearly easy + short, kid-friendly)
    //   avg >= 4 -> "Medium" (somewhere in the middle)
    //   else     -> "Low"
    //
    // An "all moderate" log scores 6 — we want that to be Medium, not High,
    // hence the cutoff at 7 instead of 6.
    //
    // No logs at all -> "Low" (we can't infer anything, so we don't claim it's friendly).
    public String computeChildFriendliness(List<TourLog> logs) {
        if (logs == null || logs.isEmpty()) return "Low";

        double totalScore = 0;
        for (TourLog log : logs) {
            totalScore += scoreSingleLog(log);
        }
        double averageScore = totalScore / logs.size();

        if (averageScore >= 7) return "High";
        if (averageScore >= 4) return "Medium";
        return "Low";
    }

    // Helper: score one log out of 9 (max points = easiest/shortest tour).
    // Split into 3 sub-scores so the rule is obvious from the code.
    private int scoreSingleLog(TourLog log) {
        return difficultyPoints(log.getDifficulty())
             + timePoints(log.getTotalTime())
             + distancePoints(log.getTotalDistance());
    }

    private int difficultyPoints(String difficulty) {
        // higher number = friendlier. unknown values get 2 (middle).
        if (difficulty == null) return 2;
        return switch (difficulty.toLowerCase()) {
            case "easy"        -> 3;
            case "moderate"    -> 2;
            case "challenging" -> 1;
            default            -> 2;
        };
    }

    // totalTime is stored in MINUTES (matches the frontend's "duration" input).
    private int timePoints(double totalTimeMinutes) {
        if (totalTimeMinutes < 60)  return 3;   // under 1 hour
        if (totalTimeMinutes < 120) return 2;   // under 2 hours
        if (totalTimeMinutes < 240) return 1;   // under 4 hours
        return 0;
    }

    // totalDistance comes from ORS in METERS, so thresholds are in meters too.
    private int distancePoints(double totalDistanceMeters) {
        if (totalDistanceMeters < 5_000)  return 3;   // under 5 km
        if (totalDistanceMeters < 15_000) return 2;   // under 15 km
        if (totalDistanceMeters < 30_000) return 1;   // under 30 km
        return 0;
    }
}
