package dev.ludwici.beyondthehorizons.neoforge.environment;

public record EnvironmentSnapshot(
        double pressureBar,
        short temperatureCelsius,
        double wind,
        double radiation,
        boolean hazardousPressure
) {
}
