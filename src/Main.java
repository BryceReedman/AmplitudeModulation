import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
        amplitudeModulator modulator = new amplitudeModulator();
        AudioInputStream payload = modulator.sendString("certified bruh moment");
        amplitudeModulator.saveAudioStream("transmission.wav", payload);



        amplitudeDemodulator demodulator = new amplitudeDemodulator();
        demodulator.loadAudioFile("transmission.wav");
        System.out.println(demodulator.demodulateToString());

    }
}
