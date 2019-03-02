/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Threads;

import Main.Client;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.SourceDataLine;
import TestPlatform.server_voice;

/**
 *
 * @author 18293468
 */
public class player_thread extends Thread{
    public DatagramSocket din;
    public SourceDataLine audio_out;
    byte[] buffer = new byte[512];
    public Client cli;
    int i = 0;
    @Override
    public void run(){
        DatagramPacket  incoming = new DatagramPacket(buffer, buffer.length);
        while(cli.getListening()){
            try {
                din.receive(incoming);
                buffer = incoming.getData();
                audio_out.write(buffer, 0, buffer.length);
//                System.out.println("received #"+i++);
            } catch (IOException ex) {
                Logger.getLogger(player_thread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        audio_out.close();
        audio_out.drain();
        din.close();
        System.out.println("Thead stopped listen.");
    }
}
