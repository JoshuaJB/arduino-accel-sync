package me.jbakita.arduinodatalogging;

import android.util.Log;

import com.punchthrough.bean.sdk.BeanListener;
import com.punchthrough.bean.sdk.message.BeanError;
import com.punchthrough.bean.sdk.message.ScratchBank;

import java.util.ArrayList;

/**
 * Listen and cache all the accelerometer readings that we recieve from
 * the LightBlue Bean.
 */
public class SerialBytestreamListener implements BeanListener {


    // Holds our buffer of all recived accelerometer readings
    private ArrayList<AccelerometerReading> buffer = new ArrayList();

    @Override
    public void onConnected() {
        Log.i("SerialBSListener", "Connected.");
    }

    @Override
    public void onConnectionFailed() {
        Log.w("SerialBSListener", "Connection Failed.");
    }

    @Override
    public void onDisconnected() {
        Log.i("SerialBSListener", "Disconnected");
    }

    @Override
    public void onScratchValueChanged(ScratchBank scratchBank, byte[] bytes) {
        // Ignore.
    }

    @Override
    public void onError(BeanError beanError) {
        Log.e("SerialBSListener", beanError.toString());
    }

    /**
     * We've received a discrete serial message from the LightBlue Bean.
     * We only care about messages containing accelerometer data. Our
     * message format is below:
     *  Data:
     *      10 2's complement bits for each reading
     *      10 bits * 3 readings (x,y,x) = 30 bits total
     *      2 bits of 1s padding are added to the left as a primitive
     *      means for validation.
     *      =!= THIS WILL BREAK AFTER 37 CONTINUOUS DAYS OF OPERATION =!=
     *      TODO: FIX
     *      Bitwise Format: |tt!xx|xxxx|xxxx!yyyy|yyyy|yy!zz|zzzz|zzzz|
     *  Timesync:
     *      32 unsigned bits directly from the Arduino millis() function
     *
     * @param bytes A byte representation of the message that we just
     *              recieved from the LightBlue Bean.
     */
    @Override
    public void onSerialMessageReceived(byte[] bytes) {
        /**
         * All accelerometer data/timesync messages are 32 / 8 or 4 bytes
         * long.
         * NOTE: The following binary contants are confusing because java
         * handles binary ops on bytes SENSELESSlY. It automatically sign-
         * -extends the byte to an integer with sign extension first....
         */
        if (bytes.length != 4) {
            Log.i("SerialBSListener", "Unhandled serial message: "
                    + bytes.toString());
            return;
        }
        // Assume that if the first two bits are up, it's a data message
        if ((bytes[0] & 0x000000FF) >= 0x000000C0) {
            /**
             * Again, java's byte type is broken and bitwise operations
             * become ABSOLUTE MONSTERS. The following comments use left
             * to right bit numbering starting at 0. (bits 0-1 are signal)
             */
            // The X reading is bits 2-11. The 6 rightmost bits from byte
            // 0 and the 4 leftmost bits from byte 1.
            byte[] xbytes = {
                    (byte)(bytes[0] & 0x3F >> 4),
                    (byte)((bytes[1] & 0xF0 >> 4) | (bytes[0] & 0xF << 4))
            };
            // The Y reading is bits 12-21. The 4 rightmost bits from byte
            // 1 and the 6 leftmost bits from byte 2.
            byte[] ybytes = {
                    (byte)(bytes[1] & 0x0F >> 2),
                    (byte)(bytes[2] & 0xFC >> 2)
            };
            // The Z reading is bits 22-31. The 2 rightmost bits from byte
            // 2 and all the bits from from byte 3.
            byte[] zbytes = {
                    (byte)(bytes[2] & 0x03),
                    bytes[3]
            };
            // Decode the bytes and push them onto the buffer
            AccelerometerReading accelReading = new AccelerometerReading(decodeBytes(xbytes),
                    decodeBytes(ybytes), decodeBytes(zbytes));
            buffer.add(accelReading);
            Log.d("SerialBSListener", "Accelerometer reading" + accelReading.toString());
            return;
        }
        // It's a timesync message
        Log.d("SerialBSListener", "Received unknown message");
        //TODO
        return;
    }

    /**
     * Decode an array of bytes to an integer
     * @param bytes An array of bytes, big endian
     */
    private int decodeBytes(byte[] bytes) {
        /* Note on Java and Bitwise Operators
         *  Java bitwise operators only work on ints and longs,
         *  Bytes will undergo promotion with sign extension first.
         *  So, we have to undo the sign extension on the lower order
         *  bits here.
         */
        int ans = bytes[0];
        for (int i = 1; i < bytes.length; i++) {
            ans <<= 8;
            ans |= bytes[i] & 0xFF;
        }
        return ans;
    }

    /**
     * Get our current buffer of all recieved accelerometer readings
     * @return AccelerometerReading array
     */
    public AccelerometerReading[] getBuffer() {
        return buffer.toArray(new AccelerometerReading[]{});
    }
}
