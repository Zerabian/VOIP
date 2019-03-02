/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author 18632807
 * @author 18293468
 */
public class Client {

    private String username;
    private InetSocketAddress addr;
    private SocketChannel sockChannel;
    private boolean calling = false;
    private boolean listening = false;
    private boolean stopCapture = false;
    private boolean connectStatus = true;
    private boolean receive = false;
    private boolean drain = false;

    /**
     * ****************************************** Constructors
     * *******************************************
     */
    public Client(String username) {
        this.username = username;

        // Initiating the socket Channel
        addr = new InetSocketAddress("localhost", 8000);
        try {
            sockChannel = SocketChannel.open(addr);
            sockChannel.configureBlocking(false);
        } catch (IOException ex) {
            connectStatus = false;
        }

    }

    public Client(String username, String ip, int port) {
        this.username = username;

        // Initiating the socket Channel
        addr = new InetSocketAddress(ip, port);
        try {
            sockChannel = SocketChannel.open(addr);
            sockChannel.configureBlocking(false);
        } catch (IOException ex) {
            connectStatus = false;
        }
    }

    /**
     * ****************************************** Communication Functions
     * *******************************************
     */
    /**
     * Function that sends username to server for authentication
     *
     * @return true if username is valid and authenticated by server
     *
     */
    public boolean sendUsername() {
        try {
            Message m = new Message(username, username, username, 0);
            m.send(sockChannel, false);
        } catch (Exception E) {
            System.out.println("Error: Sending username  " + E);
        }

        // Receive Data.
        try {
            Message m = null;
            while (m == null) {
                m = Message.receive(sockChannel);
            }
            if (m.getContent().equals("Successful")) {
                return true;
            }
        } catch (Exception E) {
            System.out.println("Error: Receiving username:  " + E);
        }
        return false;
    }

    /**
     * ****************************************** Getters and Setters *******************************************
     */
    public boolean isConnectStatus() {
        return connectStatus;
    }

    public SocketChannel getSockChannel() {
        return sockChannel;
    }

    public String getUsername() {
        return username;
    }

    public boolean getCalling() {
        return calling;
    }

    public void setCalling(boolean calling) {
        this.calling = calling;
    }

    public boolean getListening() {
        return this.listening;
    }

    public void setListening(boolean listening) {
        this.listening = listening;
    }

    public boolean getStopCapture() {
        return stopCapture;
    }

    public void setStopCapture(boolean stopCapture) {
        this.stopCapture = stopCapture;
    }
    
    public boolean getReceive(){
        return this.receive;
    }
    
    public void setReceive(boolean receive){
        this.receive = receive;
    }

}
