package me.jbakita.pebbledatalogging;

import java.text.DateFormat;
import java.util.Date;

/**
 * Storage for one accelerometer reading. This reading is comprised of x, y, z,
 * and time fields.
 */
public class AccelerometerReading {
    // Directional vectors
    private int x;
    private int y;
    private int z;
    // POSIX time in ms that this reading was taken at
    private long timestamp = 0;

    /**
     * Initialize a reading. We require that a timestamp be added later.
     * @param x The X vector of acceleration
     * @param y The Y vector of acceleration
     * @param z The Z vector of acceleration
     */
    public AccelerometerReading(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Set reading timestamp
     * @param posixMSTime time in ms since UNIX epoch
     */
    public void setTimestamp(long posixMSTime) {
        timestamp = posixMSTime;
    }

    /**
     * Get the X-directed acceleration
     * @return The integer representation of X
     */
    public int GetX() {
        return x;
    }

    /**
     * Get the X-directed acceleration
     * @return The integer representation of X
     */
    public int GetY() {
        return y;
    }

    /**
     * Get the X-directed acceleration
     * @return The integer representation of X
     */
    public int GetZ() {
        return z;
    }

    /**
     * Retrieve the vector magnitude of this reading.
     * (computed using extended pythagorian theorem)
     */
    public double GetMagnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Retrive a human-readable character string representation
     */
    @Override
    public String toString() {
        return String.format("\nX: %+5d, Y: %+5d, Z: %+5d, Time: %s", x, y, z, DateFormat.getDateTimeInstance().format(new Date(timestamp)));
    }
}
