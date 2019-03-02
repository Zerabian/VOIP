/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SocketChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import Main.Client;
import TestPlatform.server;
import TestPlatform.server_voice;
import Threads.capture_thread;
import Threads.play_note_thread;
import Threads.player_thread;
import Threads.receive_note_thread;
import Threads.recorder_thread;
import Threads.send_note_thread;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.sound.sampled.LineUnavailableException;

/**
 *
 * @author 18632807
 * @author 18293468
 */
public class ClientGUI extends javax.swing.JFrame {

    // VOIP variables
    private int port_server;
    private String add_server = "localhost";
    private TargetDataLine audio_in;
    public int dgPort;
    public int tempPort;
    public volatile boolean gotPort = false;
    public volatile boolean SEND = false;
    public volatile boolean AR = false;
    public volatile boolean isActive = true;
    public volatile boolean stop = true;
    // Messaging variables    
    private final Client cli;
    private int chatIndex = 0;
    private LinkedList<String> names = new LinkedList<>();
    private LinkedList<String> users = new LinkedList<>();
    private LinkedList<String> vName = new LinkedList<>();
    private LinkedList<byte[]> buffers = new LinkedList<>();
    private LinkedList<JTextArea> chats = new LinkedList<>();

    private HashMap<String, LinkedList<String>> groups = new HashMap<>();

    private DateFormat format = new SimpleDateFormat("HH:mm");

    private static JFrame frame;
    private static JFrame login;
    private static Thread t1;

    // Message flags    
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

    // Voice capture variables
    ByteArrayOutputStream baos;
    AudioFormat audioFormat;
    TargetDataLine tdl;
    AudioInputStream ais;
    SourceDataLine sourceDataLine;
    public SourceDataLine audio_out;
    DatagramSocket VN;

    /**
     * Creates new form NewJFrame
     */
    public ClientGUI(Client client, JFrame login) {
        // Initialise Swing components        
        initComponents();

        // Setup start state        
        this.frame = this;
        this.login = login;
        names.add("GLOBAL");
        tabbedPane1.setName("GLOBAL");
        chats.add(ChatArea);
        this.cli = client;

        // Start receiving messages        
        receive(cli);

        while (!gotPort);
        listen(dgPort);
        tempPort = dgPort;

        try {
            VN = new DatagramSocket(dgPort - 1000);
        } catch (SocketException ex) {
            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Print welcome messages        
        ChatArea.append("You are connected..." + "\n");
        WelcomeLabel.setText(WelcomeLabel.getText() + cli.getUsername());

        // Response if user in tab is clicked
        tabbedPane1.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JTabbedPane list = (JTabbedPane) evt.getSource();
                if (evt.getClickCount() == 1) {
                    // Double-click detected
                    chatIndex = list.getSelectedIndex();
                }
            }
        });

        // Response if user in list is double-clicked
        UserList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() == 2 && users.size() > 0) {
                    // Double-click detected
                    int index = list.locationToIndex(evt.getPoint());
                    JTextArea a = new JTextArea();
                    a.setEditable(false);
                    String name = (String) list.getModel().getElementAt(index);
                    if (!names.contains(name)) {
                        tabbedPane1.addTab(name, a);
                        names.add(name);
                        a.append("Whisper chat with " + list.getModel().getElementAt(index) + "\n");
                        chats.add(a);
                    }
                }
            }
        });

        // Response if window is closed        
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // Set login frame to visible
                login.setVisible(true);

                // Disconnect client
                Message M = new Message("disconnect", cli.getUsername(), cli.getUsername(), 5);
                Message M2 = new Message("stop", cli.getUsername(), cli.getUsername(), STOP);
                M.send(cli.getSockChannel(), false);
                M2.send(cli.getSockChannel(), false);
                isActive = false;

                // Dispose of current frame
                frame.setVisible(false);
                frame.dispose();
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        textField1 = new javax.swing.JTextField();
        SendButton = new javax.swing.JButton();
        DisconnectButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        UserList = new javax.swing.JList<>();
        jLabel1 = new javax.swing.JLabel();
        WelcomeLabel = new javax.swing.JLabel();
        groupBtn = new javax.swing.JButton();
        voiceNoteButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        vList = new javax.swing.JList<>();
        jLabel2 = new javax.swing.JLabel();
        playVoiceBtn = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        tabbedPane1 = new javax.swing.JTabbedPane();
        Global = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        ChatArea = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        textField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textField1ActionPerformed(evt);
            }
        });

        SendButton.setText("Send");
        SendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SendButtonActionPerformed(evt);
            }
        });

        DisconnectButton.setText("Disconnect");
        DisconnectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DisconnectButtonActionPerformed(evt);
            }
        });

        jScrollPane1.setViewportView(UserList);

        jLabel1.setText("User List:");

        WelcomeLabel.setText("Welcome, ");

        groupBtn.setText("Create Group");
        groupBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                groupBtnActionPerformed(evt);
            }
        });

        voiceNoteButton.setText("Voice Note");
        voiceNoteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                voiceNoteButtonActionPerformed(evt);
            }
        });

        jScrollPane2.setViewportView(vList);

        jLabel2.setText("Voice Messages");

        playVoiceBtn.setText("Play Message");
        playVoiceBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playVoiceBtnActionPerformed(evt);
            }
        });

        ChatArea.setEditable(false);
        ChatArea.setColumns(20);
        ChatArea.setLineWrap(true);
        ChatArea.setRows(5);
        jScrollPane3.setViewportView(ChatArea);

        javax.swing.GroupLayout GlobalLayout = new javax.swing.GroupLayout(Global);
        Global.setLayout(GlobalLayout);
        GlobalLayout.setHorizontalGroup(
            GlobalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3)
        );
        GlobalLayout.setVerticalGroup(
            GlobalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(GlobalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE))
        );

        tabbedPane1.addTab("Global", Global);

        jScrollPane4.setViewportView(tabbedPane1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(WelcomeLabel)
                    .addComponent(textField1, javax.swing.GroupLayout.PREFERRED_SIZE, 439, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(groupBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                            .addComponent(SendButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(29, 29, 29)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(DisconnectButton, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(voiceNoteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(29, 29, 29)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(playVoiceBtn)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 22, Short.MAX_VALUE))))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(WelcomeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 281, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(playVoiceBtn)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(DisconnectButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(groupBtn)))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(voiceNoteButton)
                    .addComponent(SendButton)
                    .addComponent(textField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void DisconnectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DisconnectButtonActionPerformed
        if (cli.getCalling()) {
            chats.get(chatIndex).append("Please end the call before disconnecting.\n");
            return;
        }

        if (cli.getStopCapture()) {
            chats.get(chatIndex).append("Please end the voice note before disconnecting.\n");
            return;
        }
        // Set login frame to visible
        login.setVisible(true);

        // Disconnect client
        Message M = new Message("disconnect", cli.getUsername(), cli.getUsername(), 5);
        Message M2 = new Message("stop", cli.getUsername(), cli.getUsername(), STOP);
        M.send(cli.getSockChannel(), false);
        M2.send(cli.getSockChannel(), false);

        isActive = false;
        // Dispose of current thread
        frame.setVisible(false);
        frame.dispose();

    }//GEN-LAST:event_DisconnectButtonActionPerformed


    private void SendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SendButtonActionPerformed
        send();
    }//GEN-LAST:event_SendButtonActionPerformed

    private void textField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textField1ActionPerformed
        send();
    }//GEN-LAST:event_textField1ActionPerformed


    private void groupBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_groupBtnActionPerformed
        ChatGGUI gChat = new ChatGGUI(cli, users);
        gChat.setVisible(true);
    }//GEN-LAST:event_groupBtnActionPerformed

    private void voiceNoteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voiceNoteButtonActionPerformed

        if (names.get(tabbedPane1.getSelectedIndex()).equals("GLOBAL")) {
            return;
        }

        if (voiceNoteButton.getText() == "Voice Note") {
            voiceNoteButton.setText("Stop");
            cli.setStopCapture(false);
            Message m = new Message("note", cli.getUsername(), names.get(tabbedPane1.getSelectedIndex()), NOTE);
            m.send(cli.getSockChannel(), false);

        } else {
            cli.setStopCapture(true);
            voiceNoteButton.setText("Voice Note");

            Message m = new Message("stop", cli.getUsername(), names.get(tabbedPane1.getSelectedIndex()), NOTE_END);
            m.send(cli.getSockChannel(), false);
        }
    }//GEN-LAST:event_voiceNoteButtonActionPerformed

    private void playVoiceBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playVoiceBtnActionPerformed
        if (!vList.isSelectionEmpty()) {
            int index = vList.getSelectedIndex();
            playAudio(buffers.get(index));
        }
    }//GEN-LAST:event_playVoiceBtnActionPerformed

    /**
     * **************** CODE *********************
     */
    /**
     * Function responsible for sending messages based on input from the text
     * field
     */
    private void send() {
        new Thread(() -> {
            Message m = null;

            String message = textField1.getText();
            String strip[] = message.split(" ");
            if (strip[0].equals("\\call")) {
                if (strip.length > 1) {
                    if (users.contains(strip[1])) {
                        String content = "";
                        try {
                            content = cli.getSockChannel().getLocalAddress().toString().split(":")[0];
                        } catch (IOException ex) {
                            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        content = content.substring(1, content.length()) + ":" + dgPort;
                        m = new Message(content, cli.getUsername(), users.get(users.indexOf(strip[1])), CALL);
                        chats.get(tabbedPane1.getSelectedIndex()).append("Calling " + m.getDest() + "\n");
                        m.send(cli.getSockChannel(), false);
                    } else {
                        chats.get(tabbedPane1.getSelectedIndex()).append("Call unsuccessful: User is currently not connected.");
                    }

                } else if (groups.get(names.get(tabbedPane1.getSelectedIndex())) != null) {
                    // Code for group calls                    
                    for (String user : groups.get(names.get(tabbedPane1.getSelectedIndex()))) {
                        m = new Message("group_call", cli.getUsername(), user, CALL_G);
                        chats.get(tabbedPane1.getSelectedIndex()).append("Calling " + names.get(tabbedPane1.getSelectedIndex()) + "\n");
                        m.send(cli.getSockChannel(), false);
                    }
                } else {
                    // Code for calls
                    if (names.get(tabbedPane1.getSelectedIndex()).equals("GLOBAL")) {
                        return;
                    }
                    String content = "";
                    try {
                        content = cli.getSockChannel().getLocalAddress().toString().split(":")[0];
                    } catch (IOException ex) {
                        Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    content = content.substring(1, content.length()) + ":" + dgPort;

                    m = new Message(content, cli.getUsername(), names.get(tabbedPane1.getSelectedIndex()), CALL);
                    chats.get(tabbedPane1.getSelectedIndex()).append("Calling " + names.get(tabbedPane1.getSelectedIndex()) + "\n");

                    m.send(cli.getSockChannel(), false);
                }
            } else if (strip[0].equals("\\stop")) {

                // Code for stopping calls
                if (strip[0].equals("\\stop")) {
                    if (strip.length > 1) {
                        if (users.contains(strip[1])) {
                            m = new Message("call", cli.getUsername(), users.get(users.indexOf(strip[1])), STOP);
                            m.send(cli.getSockChannel(), false);
                            chats.get(tabbedPane1.getSelectedIndex()).append("Ended call with " + m.getDest() + "\n");
                        } else {
                            chats.get(tabbedPane1.getSelectedIndex()).append("Stopping call unsuccessful: User is currently not connected.");
                        }
                    } else if (groups.get(names.get(tabbedPane1.getSelectedIndex())) == null) {
                        if (names.get(tabbedPane1.getSelectedIndex()).equals("GLOBAL")) {
                            return;
                        }

                        m = new Message("stop", cli.getUsername(), names.get(tabbedPane1.getSelectedIndex()), STOP);
                        m.send(cli.getSockChannel(), false);
                        chats.get(tabbedPane1.getSelectedIndex()).append("Ended call with " + names.get(tabbedPane1.getSelectedIndex()) + "\n");
                    }
                }
                if (stop) {
                    return;
                }
                stop = true;

                cli.setListening(false);
                cli.setCalling(false);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }

                listen(dgPort);
            } else if (!message.isEmpty() && message.length() < 195) {
                try {
                    if (names.get(tabbedPane1.getSelectedIndex()).equals("GLOBAL")) {
                        // Global Text Message
                        m = new Message(message, cli.getUsername(), cli.getUsername(), GLOBAL);
                    } else if (users.contains(names.get(tabbedPane1.getSelectedIndex()))) {
                        // Private Text Message
                        m = new Message(message, cli.getUsername(), names.get(tabbedPane1.getSelectedIndex()), MESSAGE);
                    } else {
                        // Group Text Message
                        m = new Message(message, cli.getUsername(), names.get(tabbedPane1.getSelectedIndex()), GROUP);
                    }
                    m.send(cli.getSockChannel(), false);
                    chats.get(chatIndex).append(m.getSrc() + " (" + format.format(m.getDate()) + "): " + m.getContent() + "\n");
                } catch (Exception E) {
                    E.printStackTrace();
                }

            } else {
                chats.get(chatIndex).append("Client: Message exceeds the max length or is empty.\n");
            }

            textField1.setText("");
        }).start();
    }

    /**
     * Function that receives messages from server and runs them accordingly
     *
     * @param cli - the client used for the GUI
     *
     */
    private void receive(Client cli) {
        SocketChannel channel = cli.getSockChannel();

        t1 = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    Message m = null;
                    while (m == null && isActive) {
                        m = Message.receive(channel);
                    }
                    processMessage(m);

                }
            }
        });
        t1.start();
    }

    /**
     * Function that processes messages received from the server.
     *
     * @param m - The message being processed
     */
    private void processMessage(Message m) {
        if (m == null) {
            return;
        }

        switch (m.getType()) {
            case GLOBAL:
                chats.get(0).append(m.getDest() + " (" + format.format(m.getDate()) + "): " + m.getContent() + "\n");
                break;

            case MESSAGE:
                if (names.contains(m.getSrc())) {
                    chats.get(names.indexOf(m.getSrc())).append(m.getSrc() + " (" + format.format(m.getDate()) + "): " + m.getContent() + "\n");
                } else {
                    JTextArea a = new JTextArea();
                    a.setEditable(false);
                    String name = m.getSrc();
                    tabbedPane1.addTab(name, a);
                    names.add(name);
                    a.append("Whisper chat with " + m.getSrc() + "\n");
                    a.append(m.getSrc() + " (" + format.format(m.getDate()) + "): " + m.getContent() + "\n");
                    chats.add(a);
                }
                break;

            case USERADD:
                users.add(m.getSrc());
                ArrayList<String> list = new ArrayList<>(users);
                Collections.sort(list);
                users = new LinkedList<>(list);
                UserList.setListData(users.toArray(new String[users.size()]));
                chats.get(names.indexOf("GLOBAL")).append(m.getSrc() + " connected\n");
                break;

            case USERLIST:
                String str = m.getContent();

                str = str.substring(1, str.length() - 1);
                String tokens[] = str.split(",");
                if (!tokens[0].equals("")) {
                    for (int i = 0; i < tokens.length; i++) {
                        if (!tokens[i].trim().equals(cli.getUsername())) {
                            users.add(tokens[i].trim());
                        }
                    }
                    UserList.setListData(users.toArray(new String[users.size()]));
                }
                break;

            case DISCONNECT:
                users.remove(users.indexOf(m.getSrc()));
                UserList.setListData(users.toArray(new String[users.size()]));

                // Tell global chat
                chats.get(names.indexOf("GLOBAL")).append(m.getSrc() + " disconnected\n");

                // Close private chats
                if (names.contains(m.getSrc())) {
                    tabbedPane1.remove(tabbedPane1.indexOfTab(m.getSrc()));
                    chats.remove(chats.get(names.indexOf(m.getSrc())));
                }
                break;

            case GROUP:
                if (!m.getSrc().equals(cli.getUsername())) {
                    chats.get(names.indexOf(m.getDest())).append(m.getSrc() + " (" + format.format(m.getDate()) + "): " + m.getContent() + "\n");
                }
                break;

            case GROUP_ADD:
                JTextArea a = new JTextArea();
                a.setEditable(false);
                String name = m.getDest();
                tabbedPane1.addTab(name, a);
                names.add(name);
                a.append("Group chat with " + m.getDest() + "\n");
                chats.add(a);

                LinkedList<String> tempNames = new LinkedList<>();
                String list1[] = m.getContent().split(",");
                for (String l : list1) {
                    if (!l.equals(cli.getUsername())) {
                        tempNames.add(l);
                    }
                }

                //Adding to the hashmap of groups.
                groups.put(name, tempNames);
                break;
            case CALL:
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        // Create call request window                        
                        call_request req = new call_request(m.getSrc());
                        req.setVisible(true);
                        AR = req.getResponse();

                        // Close call request window                        
                        req.dispose();

                        if (AR) {
                            if (!m.getSrc().equals(cli.getUsername())) {
                                call(m.getContent());
                            }

                            String content = "";
                            try {
                                content = cli.getSockChannel().getLocalAddress().toString().split(":")[0];
                            } catch (IOException ex) {
                                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            content = content.substring(1, content.length()) + ":" + dgPort;
                            Message m2 = new Message(content, m.getDest(), m.getSrc(), RESP);
                            m2.send(cli.getSockChannel(), false);
                            chats.get(tabbedPane1.getSelectedIndex()).append("Accepted call from " + m.getSrc() + "\n");
                        } else {
                            Message m2 = new Message("false", m.getDest(), m.getSrc(), RESP);
                            m2.send(cli.getSockChannel(), false);
                            chats.get(tabbedPane1.getSelectedIndex()).append("Rejected call from " + m.getSrc() + "\n");
                        }
                    }
                });
                t.start();
                break;
            case CALL_G:
                tempPort++;
                listen(tempPort);
                String content = "";
                try {
                    content = cli.getSockChannel().getLocalAddress().toString().split(":")[0];
                } catch (IOException ex) {
                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                content = content.substring(1, content.length()) + ":" + tempPort;
                Message m2 = new Message(content, m.getDest(), m.getSrc(), RESP);
                m2.send(cli.getSockChannel(), false);
                break;

            case RESP:
                if (!m.getContent().equals("false")) {
                    call(m.getContent());
                } else {
                    chats.get(chatIndex).append("Call has been refused.\n");
                }
                break;

            case PORT:
                System.out.println("Port assigned.");
                dgPort = Integer.parseInt(m.getContent());
                gotPort = true;
                break;

            case STOP:
                if (stop) {
                    return;
                }
                stop = true;
                chats.get(chatIndex).append("Call has ended with " + m.getSrc() + "\n");
                System.out.println("Received stop flag.");
                cli.setCalling(false);
                cli.setListening(false);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }

                listen(dgPort);

                break;
            case EXIT:
                isActive = false;
                // Set login frame to visible
                login.setVisible(true);

                this.setVisible(false);
                this.dispose();
                break;
            case NOTE:
                String header = "";
                try {
                    header = cli.getSockChannel().getLocalAddress().toString().split(":")[0];
                } catch (IOException ex) {
                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                header = header.substring(1, header.length()) + ":" + (dgPort - 1000);
                System.out.println(header);

                Message m3 = new Message(header, m.getDest(), m.getSrc(), NOTE_R);
                m3.send(cli.getSockChannel(), false);

                //Start receiving
                receive_note_thread rec = new receive_note_thread();
                rec.cli = cli;
                rec.din = VN;
                cli.setReceive(true);
                baos = new ByteArrayOutputStream();
                rec.baos = baos;
                rec.start();
                break;

            case NOTE_R:
                send_note_thread s = new send_note_thread();
                String nHeader = m.getContent();
                int port = Integer.parseInt(nHeader.split(":")[1]);
                String ip = nHeader.split(":")[0];
                InetAddress inet = null;

                try {
                    inet = InetAddress.getByName(ip);
                } catch (UnknownHostException ex) {
                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }

                s.dout = VN;
                s.cli = cli;
                audioFormat = getAudioFormat();
                DataLine.Info dli1 = new DataLine.Info(TargetDataLine.class, audioFormat);
                try {
                    tdl = (TargetDataLine) AudioSystem.getLine(dli1);
                    tdl.open(audioFormat);
                    tdl.start();
                } catch (LineUnavailableException ex) {
                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                s.audio_in = tdl;
                s.server_ip = inet;
                s.server_port = port;
                s.start();
                break;
            case NOTE_END:
                cli.setReceive(false);
                try {
                    buffers.add(this.baos.toByteArray());
                    vName.add(m.getSrc());
                    vList.setListData(vName.toArray(new String[vName.size()]));
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }

                cli.setReceive(true);
                break;

            case ONLINE:
                System.out.println("ONLINE");
                String src = m.getDest();
                String dest = m.getSrc();
                Message m5 = new Message("call", src, dest, CALL);
                m5.send(cli.getSockChannel(), false);
                break;
        }

    }

    private void playAudio(byte[] buffer) {
        try {
            byte audioData[] = buffer;
            InputStream bais = new ByteArrayInputStream(audioData);
            AudioFormat audioFormat = getAudioFormat();
            ais = new AudioInputStream(bais, audioFormat, audioData.length / audioFormat.getFrameSize());
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open();
            sourceDataLine.start();

            //Thread to play the voice note
            play_note_thread p = new play_note_thread();
            p.ais = ais;
            p.sourceDataLine = sourceDataLine;
            p.start();
        } catch (Exception E) {
            System.out.println(E);
            System.exit(0);
        }
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 8000.0F;
        int sampleSizeIntBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;

        //create and send audio format
        return new AudioFormat(sampleRate, sampleSizeIntBits, channels, signed, bigEndian);
    }

    // VOIP functions
    public void call(String content) {
        try {
            stop = false;
            String ip = content.split(":")[0];
            int port = Integer.parseInt(content.split(":")[1]);
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("not supported");
                System.exit(0);
            }

            audio_in = (TargetDataLine) AudioSystem.getLine(info);
            audio_in.open(format);
            audio_in.start();

            recorder_thread r = new recorder_thread();
            InetAddress inet = InetAddress.getByName(ip);
            r.audio_in = audio_in;
            r.dout = new DatagramSocket();
            r.server_ip = inet;
            r.server_port = port;
            r.cli = cli;
            cli.setCalling(true);
            r.start();

        } catch (LineUnavailableException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SocketException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void listen(int port) {
        try {
            AudioFormat format = getAudioFormat();
            DataLine.Info info_out = new DataLine.Info(SourceDataLine.class, format);
            if (!AudioSystem.isLineSupported(info_out)) {
                System.out.println("not supported.");
                System.exit(0);
            }

            audio_out = (SourceDataLine) AudioSystem.getLine(info_out);
            audio_out.open(format);
            audio_out.start();

            player_thread p = new player_thread();
            p.din = new DatagramSocket(port);

            p.audio_out = audio_out;
            p.cli = cli;

            cli.setListening(true);
            p.start();
        } catch (LineUnavailableException ex) {
            Logger.getLogger(server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SocketException ex) {
            Logger.getLogger(server.class.getName()).log(Level.SEVERE, null, ex);
        }

    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea ChatArea;
    private javax.swing.JButton DisconnectButton;
    private javax.swing.JPanel Global;
    private javax.swing.JButton SendButton;
    private javax.swing.JList<String> UserList;
    private javax.swing.JLabel WelcomeLabel;
    private javax.swing.JButton groupBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JButton playVoiceBtn;
    private javax.swing.JTabbedPane tabbedPane1;
    private javax.swing.JTextField textField1;
    private javax.swing.JList<String> vList;
    private javax.swing.JButton voiceNoteButton;
    // End of variables declaration//GEN-END:variables
}
