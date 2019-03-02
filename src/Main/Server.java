/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;

/**
 *
 * @author 18632807
 * @author 18293468
 */
public class Server extends javax.swing.JFrame {

    // Server properties
    private int port = 8000;
    private Selector selector = null;
    private ServerSocketChannel ssc;
    private volatile boolean open;

    // Message type flags
    private final int MESSAGE = 0;
    private final int USER = 1;
    private final int GLOBAL = 2;
    private final int USERADD = 3;
    private final int USERLIST = 4;
    private final int DISCONNECT = 5;
    private final int SOUND = 6;
    private final int GROUP = 7;
    private final int GROUP_ADD = 8;
    private final int CALL = 9;
    private final int PORT = 10;
    private final int STOP = 11;
    private final int CALL_G = 12;
    private final int RESP = 14;
    private final int EXIT = 15;
    private final int NOTE = 16;
    private final int NOTE_R = 17;
    private final int NOTE_END = 18;
    private final int ONLINE = 19;

    // User data
    private LinkedList<String> usernames = new LinkedList<>();
    private HashMap<String, SocketChannel> names = new HashMap<>();
    private HashMap<String, String> gNames = new HashMap<>();
    private Queue<Message> messages = new LinkedList<>();
    private HashMap<String, String> portLink = new HashMap<>();

    // Generated variables
    private int dgPort = 7000;

    // Constructor method
    public Server() {
        initComponents();
        users.removeAll();
        try {
            open = true;
            selector = Selector.open();

            // Initialising the server socket channel.
            ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);

            // Bind Port
            InetSocketAddress host_address = new InetSocketAddress(port);
            ssc.bind(host_address);

            // Register channel
            ssc.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("server initialised.");
        activity.append("Server initialised.\n");

        run();

        // Response if window is closed        
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                Message m = new Message("exit", "exit", "exit", EXIT);
                sendG(m);
                open = false;
            }
        });
    }

    private void run() {
        new Thread(new Runnable() {
            public void run() {
                System.out.println("server running.");
                activity.append("Server running.\n");
                while (open) {
                    try {
                        int ready = 0;
                        ready = selector.select();
                        if (ready == 0) {
                            continue;
                        }

                        Set<SelectionKey> ready_keys = selector.selectedKeys();
                        Iterator<SelectionKey> iterator = ready_keys.iterator();
                        while (iterator.hasNext()) {
                            SelectionKey key = iterator.next();
                            iterator.remove();
                            options(key);
                        }

                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                Set<SelectionKey> ready_keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator1 = ready_keys.iterator();
                while (iterator1.hasNext()) {
                    SelectionKey key = iterator1.next();
                    iterator1.remove();
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                System.exit(0);
            }
        }).start();
    }

    private void options(SelectionKey key) {
        if (key.isAcceptable()) {
            accept(key);
        } else if (key.isReadable()) {
            read(key);
        } else if (key.isWritable()) {
            write(key);
        }
    }

    /**
     * Function that accepts clients
     *
     * @param key - The Selection Key
     */
    private void accept(SelectionKey key) {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();

        // Get client socket channel
        SocketChannel client = null;

        try {
            client = server.accept();
            client.configureBlocking(false);

            // Register for read and write operations.
            client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        } catch (Exception E) {
            System.out.println("Server could not connect to client - " + E);
            activity.append("Server could not connect to client -" + E + "\n");
        }

        Message m = null;
        while (m == null) {
            m = Message.receive(client);
        }

        System.out.print("user: " + m.getSrc() + " --> requesting connection to Server --> ");
        activity.append("user: " + m.getSrc() + " --> requesting connection to Server --> \n");
        if (isUnique(m.getSrc())) {
            System.out.println("successful");
            activity.append("Successful\n");
            usernames.add(m.getSrc());
            users.setListData(usernames.toArray(new String[usernames.size()]));
            m.setContent("Successful");
            m.send(client, true);
            m.setType(USERADD);
            sendG(m);

            m.setContent(names.keySet().toString());
            m.setType(USERLIST);
            m.send(client, true);
            names.put(m.getSrc(), client);

            dgPort++;
            portLink.put(m.getSrc(), dgPort + "");
            m.setContent(dgPort + "");
            m.setType(PORT);
            m.send(client, true);
        } else {
            System.out.println("unsuccessful");
            activity.append("Unsuccessful\n");
            m.setContent("Unsuccessful");
            m.send(client, true);
        }
    }

    private void read(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();
        try {
            if (client.isConnected()) {
                Message msg = Message.receive(client);

                if (msg == null) {
                    return;
                }

                switch (msg.getType()) {
                    case USERLIST:
                        Message m = new Message(names.keySet().toString());
                        m.setType(USERLIST);
                        m.send(client, true);
                        break;
                    case DISCONNECT:
                        // Notify other clients about the disconnection
                        System.out.println(msg.getSrc() + " disconnected");
                        activity.append(msg.getSrc() + " disconnected\n");
                        sendG(msg);
                        key.cancel();
                        key.channel().close();

                        // Update group members if client was in a group
                        for (Map.Entry<String, String> entry : gNames.entrySet()) {
                            if (entry.getValue().contains(msg.getSrc() + ",")) {
                                String updated = entry.getValue().replace(msg.getSrc() + ",", "");
                                gNames.put(entry.getKey(), updated);
                            }
                        }

                        names.remove(msg.getSrc());
                        usernames.remove(msg.getSrc());
                        users.setListData(usernames.toArray(new String[usernames.size()]));
                        break;
                    case MESSAGE:
                    case GLOBAL:
                    case GROUP:
                    case GROUP_ADD:
                    case CALL_G:
                    case RESP:
                    case NOTE:
                    case NOTE_END:
                    case ONLINE:
                        messages.add(msg);
                        break;
                    case CALL:
                        activity.append(msg.getSrc() + " calling " + msg.getDest() + ".\n");
                        messages.add(msg);
                        break;
                    case STOP:
                        activity.append("Call between  " + msg.getSrc() + " and " + msg.getDest() + " has ended.\n");
                        messages.add(msg);
                        break;
                    case NOTE_R:
                        activity.append("Voice note sent from  " + msg.getDest() + " too " + msg.getSrc() + ".\n");
                        messages.add(msg);
                        break;
                }
            }

        } catch (Exception E) {
            System.out.println("error reading.");
            activity.append("error reading.\n");
        }
    }

    private void write(SelectionKey key) {
        if (!messages.isEmpty()) {
            Message msg = messages.remove();
            if (msg.getType() == GLOBAL) {
                sendG(msg);
            } else if (msg.getType() == GROUP || msg.getType() == GROUP_ADD) {
                sendP(msg);
            } else if (msg.getType() == CALL || msg.getType() == STOP) {
                if (!msg.getDest().equals("GLOBAL")) {
                    msg.send(names.get(msg.getDest()), true);
                }
            } else if (msg.getType() == ONLINE) {
                msg.send(names.get(msg.getSrc()), true);
            } else if (msg.getType() == NOTE || msg.getType() == NOTE_END) {
                if (!msg.getDest().equals("GLOBAL") && gNames.get(msg.getDest()) == null) {
                    msg.send(names.get(msg.getDest()), true);
                }
            } else {
                msg.send(names.get(msg.getDest()), true);
            }
        }
    }

    /**
     * Function to find out if a username is already in use or unique
     *
     * @param src - The username in string form
     * @return true if the username is unique
     */
    private boolean isUnique(String src) {
        return names.get(src) == null;
    }

    private void sendG(Message m) {
        Iterator iter = names.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry client = (Map.Entry) iter.next();
            if (!client.getKey().equals(m.getSrc())) {
                SocketChannel cli = (SocketChannel) client.getValue();
                m.send(cli, true);
            }
        }
    }

    private void sendP(Message m) {
        System.out.println("Sending group message.");
        activity.append("Sending group message.\n");
        String groupName = m.getDest();
        String members = "";

        if (m.getType() == GROUP_ADD) {
            gNames.put(groupName, m.getContent());
            members = m.getContent();
        } else {
            members = gNames.get(groupName);
        }
        if (members == null) {
            return;
        }

        String tokens[] = members.split(",");
        for (int i = 0; i < tokens.length; i++) {
            if (names.get(tokens[i]) != null) {
                SocketChannel cli = (SocketChannel) names.get(tokens[i]);
                m.send(cli, true);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        serverLabel = new javax.swing.JLabel();
        userLabel = new javax.swing.JLabel();
        exitBtn = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        activity = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        users = new javax.swing.JList<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        serverLabel.setText("Server");

        userLabel.setText("users");

        exitBtn.setText("Disconnect Server");
        exitBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitBtnActionPerformed(evt);
            }
        });

        activity.setColumns(20);
        activity.setLineWrap(true);
        activity.setRows(5);
        activity.setMinimumSize(new java.awt.Dimension(400, 500));
        jScrollPane3.setViewportView(activity);

        jScrollPane4.setViewportView(users);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(serverLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(113, 113, 113))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(exitBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 276, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(userLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(serverLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(userLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3)
                    .addComponent(jScrollPane4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exitBtn)
                .addGap(17, 17, 17))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitBtnActionPerformed
        Message m = new Message("stop", "stop", "stop", STOP);
        sendG(m);
        m = new Message("exit", "exit", "exit", EXIT);
        sendG(m);
        open = false;

        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.dispose();


    }//GEN-LAST:event_exitBtnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Server().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea activity;
    private javax.swing.JButton exitBtn;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel serverLabel;
    private javax.swing.JLabel userLabel;
    private javax.swing.JList<String> users;
    // End of variables declaration//GEN-END:variables
}
