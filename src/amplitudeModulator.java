import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * This represents an amplitude modulator
 * Only supports a carrier wave frequency of 1000hz @ 32000 sampling rate
 */
public class amplitudeModulator {
    private final AudioFormat af = new AudioFormat(32000, 8, 1, true, true);



    /**
     * @return a cycle of the carrier wave
     */
    private static Byte[] generateCarrier() {
        int numSamplesPerCycle = 32000 / 1000;

        Byte[] payload = new Byte[numSamplesPerCycle];

        for (int sample = 0; sample < payload.length; sample++) {
            int phase = sample % (numSamplesPerCycle);
            double angle = 2.0 * Math.PI * phase / numSamplesPerCycle;
            payload[sample] = (byte) (Math.sin(angle) * 127f);
        }

        return payload;
    }

    /**
     * @param audioInputStream - the audio you want to save
     * @param filename - the name of the wave you want to save to
     */
    public static void saveAudioStream(String filename, AudioInputStream audioInputStream) throws IOException {
        AudioFileFormat.Type targetType = AudioFileFormat.Type.WAVE;

        // Write the audio data to the output file
        AudioSystem.write(audioInputStream, targetType, new File(filename));
    }

    /**
     * @param b is the byte we want to inspect
     * @param index the bit we want to check
     * @return true if bit is 1, false if bit is 0
     */
    public static boolean getBit(byte b, int index) {
        if (index < 0 || index > 7) {
            throw new IllegalArgumentException("Bit index must be between 0 and 7");
        }
        return (((b >> index) & 1) == 1);
    }

    /**
     * @param message - the string you want to modulate
     * @return the AudioInputStream containing the modulated message
     */
    public AudioInputStream sendString(String message) {
        //Transmission Data
        byte[] msgBytes = message.getBytes();
        byte[] payload = generatePayload(msgBytes);

        return new AudioInputStream(new ByteArrayInputStream(payload), af, payload.length);

    }

    /**
     * This generates the output signal
     * @param data the bytes to be modulated into the carrier wave
     * @return the output signal in the form of bytes where each byte is an audio sample
     */
    private byte[] generatePayload(byte[] data) {
        //create an arraylist of bytes
        ArrayList<Byte> payload = new ArrayList<>();

        //for each byte
        for (byte datum : data) {
            //for each bit in the byte
            for (int b = 0; b < 8; b++) {
                //if the bit is a 1 then play the carrier
                if (getBit(datum, b)) {
                    payload.addAll(List.of(generateCarrier()));
                } else {// if the bit is a 0 then play silence for 32 samples where 32 samples completes a cycle
                    for (int f = 0; f < 32; f++) {
                        payload.add((byte) 0);
                    }
                }
            }
        }

        //if data is finished being modulated then add the final 0 byte
        payload.add((byte) 0);
        //convert the arraylist into a byte array
        byte[] bytePayload = new byte[payload.size()];

        for (int i = 0; i < bytePayload.length; i++) {
            bytePayload[i] = payload.get(i);
        }
        //return the byte array
        return bytePayload;
    }

}
