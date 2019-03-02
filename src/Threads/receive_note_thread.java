/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Threads;

import Main.Client;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author 18293468
 */
public class receive_note_thread extends Thread {

    byte tempBuffer[] = new byte[10000];
    public ByteArrayOutputStream baos;
    public Client cli;

    public DatagramSocket din;
    byte[] buffer = new byte[512];
    int i = 0;
    int count = 0;

    public void run() {
        DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
        while (cli.getReceive()) {
            try {
                din.receive(incoming);
                buffer = incoming.getData();
                if (buffer.length > 0) {
                    baos.write(buffer, 0, buffer.length);
//                    System.out.println("voice note received #" + i++);
                } else {
                    cli.setReceive(false);
                }
            } catch (IOException ex) {
                Logger.getLogger(player_thread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        try {
            baos.close();
        } catch (IOException ex) {
            Logger.getLogger(receive_note_thread.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(cli.getReceive());
        System.out.println("STOP");
    }
}
