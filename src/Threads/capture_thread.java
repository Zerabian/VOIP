/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Threads;

import Main.Client;
import java.io.ByteArrayOutputStream;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author 18293468
 */
public class capture_thread extends Thread{
        byte tempBuffer[] = new byte[10000];
        public ByteArrayOutputStream baos;
        public boolean stopCapture;
        public TargetDataLine tdl;
        public Client cli;
        
        public void run() {
            baos = new ByteArrayOutputStream();
            stopCapture = false;
            int count;
            try {
                while (cli.getStopCapture()) {
                    count = tdl.read(tempBuffer, 0, tempBuffer.length);
                    if (count > 0) {
                        baos.write(tempBuffer, 0, count);
                    }
                }

                baos.close();
            } catch (Exception E) {

            }

        }
}
