/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author 18293468
 */
public class Message {
    
    private String content;
    private String src;
    private String dest;
    private Date date;
    private int type;
    private int S_C; //Is this coming from the server or the client.
    
    //type 0 = message
    //type 1 = user
    //type 2 = global
    //type 3 = user add
    //type 4 = add user list
    //type 5 = disconnect
    //type 6 = sound
    //type 7 = group
    //type 8 = group add
    //type 9 = call
    //type 10 = port
    //type 11 = stop
    //type 12 = call_g
    //type 14 = resp
    //type 15 = exit
    //type 16 = note
    //type 17 = note_r
    
    // Constructor methods
    public Message(String content, String src, String dest, int type){
        this.content = content;
        this.src = src;
        this.dest = dest;
        this.date = new Date();
        this.type = type;
    }
    
    public Message(String user){
        this.content = user;
        this.type = 1;
    }
    
    // Getters
    public String getContent(){ return content; }
    public String getSrc(){ return src; }
    public String getDest(){ return dest; }
    public Date getDate(){return date; }
    public int getType(){ return type; }
    
    // Setters
    public void setContent(String content){ this.content = content; }
    public void setSrc(String src){ this.src = src; }
    public void setDest(String dest){ this.dest = dest; }
    public void setType(int type){ this.type = type; }
    
    @Override
    public String toString(){
     String temp = "";
     temp += "\nContent: " + content;
     temp += "\nSource: " + src;
     temp += "\nDestination: " + dest;
     temp += "\nType: " + type +"\n";
     return temp;
    }
    
    /**
     * Sends object
     * @param sc - The socket channel over which the message is being sent
     * @param server - Boolean about whether or not the message is comin from the server or not
     */
    public void send(SocketChannel sc, boolean server) {
        ByteBuffer buffer = ByteBuffer.allocate(290);
        byte[] contentB = new byte[200];
        byte[] srcB = new byte[20];
        byte[] destB = new byte[20];
        byte[] typeB = new byte[50];
        
        contentB = Arrays.copyOf(content.getBytes(), 200);
        srcB = Arrays.copyOf(src.getBytes(), 20);
        destB = Arrays.copyOf(dest.getBytes(), 20);
        typeB = Arrays.copyOf((type+"").getBytes(), 50);
        
        buffer.put(contentB);
        buffer.put(srcB);
        buffer.put(destB);
        buffer.put(typeB);
        buffer.flip();
        try {
            sc.write(buffer);
        } catch (IOException ex) {
            Logger.getLogger(Message.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Receives object
     * @param sc - The socket channel over which the message is being sent
     */
    public static Message receive(SocketChannel sc) {
        int read = 0;
        ByteBuffer buffer = ByteBuffer.allocate(290);
        try {
            read = sc.read(buffer);
        } catch (IOException ex) {
            Logger.getLogger(Message.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(read > 0){
            String content =  new String(Arrays.copyOfRange(buffer.array(), 0, 200)).trim();
            String src =  new String(Arrays.copyOfRange(buffer.array(), 200, 220)).trim();
            String dest =  new String(Arrays.copyOfRange(buffer.array(), 220, 240)).trim();
            int type =  Integer.parseInt(new String(Arrays.copyOfRange(buffer.array(), 240, 290)).trim());
            Message m  = new Message(content, src, dest, type);
            return m;
        }
        
        return null;
    }    
}
