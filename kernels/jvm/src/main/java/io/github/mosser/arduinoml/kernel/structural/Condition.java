package io.github.mosser.arduinoml.kernel.structural;

public class Condition {
    private Sensor sensor;
    private SIGNAL value;
    private final boolean orWithPrevious;

    public Condition(Sensor sensor, SIGNAL value , boolean orWithPrevious) {
        this.orWithPrevious = orWithPrevious;
        this.sensor = sensor;
        this.value = value;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public boolean isOrWithPrevious() {
        return orWithPrevious;
    }

    public SIGNAL getValue() {
        return value;
    }
}
