/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Threads;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.SourceDataLine;

/**
 *
 * @author 18293468
 */
public class play_note_thread extends Thread {
    public AudioInputStream ais;
    public SourceDataLine sourceDataLine;
    byte tempBuffer[] = new byte[10000];

    public void run() {
        try {
            int count;
            while ((count = ais.read(tempBuffer, 0, tempBuffer.length)) != -1) {
                if (count > 0) {
                    sourceDataLine.write(tempBuffer, 0, tempBuffer.length);
                }
            }

            sourceDataLine.drain();
            sourceDataLine.close();
        } catch (Exception E) {
            System.out.println(E);
            System.exit(0);
        }
    }
}
