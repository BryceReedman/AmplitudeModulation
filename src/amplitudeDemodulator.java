import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**Class represents a demodulator with its own input signal.
 * Only supports 1000hz @ 32000 sampling rate
 */
public class amplitudeDemodulator {

    //load WAV file

    private byte[] inputSignal;


    /**
     * @param filename - name of file you want to use as input signal. carrier must be 1000hz & of a sampling rate 32000
     */
    public void loadAudioFile(String filename) throws UnsupportedAudioFileException, IOException {
        //load WAV file
        File file = new File(filename); // Replace "example.wav" with the path to your WAV file
        //enforcing signed numbers
        AudioFormat af = new AudioFormat(32000, 8, 1, true, true);
        AudioInputStream stream = AudioSystem.getAudioInputStream(file);
        //outputs the samples into a list of bytes
        this.inputSignal = AudioSystem.getAudioInputStream(af, stream).readAllBytes();
    }

    /**
     * @return the demodulated data in the form of a string.
     */
    public String demodulateToString() {
        //used to determine if a 1 or 0 is being signalled
        double sumOfSampleValues = 0;
        //used to reconstruct the bits from the audio file
        ArrayList<Boolean> bits = new ArrayList<>();

        //for each sample
        for (int i = 0; i < inputSignal.length; i++) {
                //used to track the phase the wave
                int phase = (i) % ((32000 / 1000));

                //only sums the samples that belong to the positive portion of the wave
                if (phase <= 32000 / 1000 / 2) {
                    sumOfSampleValues += inputSignal[i];
                }
                //if cycle is complete then average the samples
                //Beginning of the audio file is a special case because it starts at phase 0. This introduces an extra
                //bit. 1!= 0 prevents the special case from occurring.
                if (phase == 0 & i != 0) {
                    double averageOfSampleValues = sumOfSampleValues / (32000 / 1000 / 2d);
                    sumOfSampleValues = 0;
                    //if the average is positive then this cycle must represent a 1!
                    if (averageOfSampleValues > 63.0) {
                        bits.add(true);
                    } else {// otherwise, it must represent a 0!
                        bits.add(false);
                    }
            }
        }

        //Once for loop is completed then the signal has been demodulated. We are now left with bits.
        //By running the helper function we can get our bits back into bytes
        byte[] byteArray = booleanListToByteArray(bits);
        //now return the bytes as a string!
        return new String(byteArray);
    }

    /**Used to get from bits to bytes
     * @param boolList a list of bits in multiples of 8s
     * @return a Arraylist of bytes composed of the bits provided
     */
    public static byte[] booleanListToByteArray(ArrayList<Boolean> boolList) {
        if (boolList == null || boolList.size() % 8 != 0) {
            throw new IllegalArgumentException("Input list must be non-null and have size that is a multiple of 8");
        }
        //work out how many bytes we need to construct
        int byteCount = boolList.size() / 8;
        byte[] result = new byte[byteCount];

        //For each byte
        for (int i = 0; i < byteCount; i++) {
            //initialise a byte of 0 - 00000000
            byte b = 0;
            //for each corresponding bit, perform bitwise operations to set the byte's bits
            //bytes are reconstructed where the left most bit is the least most significant
            //this is the same as what was modulated
            for (int j = 0; j < 8; j++) {
                if (boolList.get(i * 8 + j)) {
                    b |= (1 << j);
                }
            }
            //add the constructed bit to the list
            result[i] = b;
        }

        //return the list of bytes we constructed
        return result;
    }

}
