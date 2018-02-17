package ru.geekbrains.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatWindow extends JFrame{
    private JTextField message;
    private JTextArea chatHistory;
    private JTextArea usersList;
    private JTextField login;
    private JPasswordField password;
    private JPanel top;
    JPanel bottom;

    private ClientConnection clientConnection;


    public ChatWindow(){
        clientConnection = new ClientConnection();

        setTitle("Chat");
        setSize(400, 400);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        chatHistory = new JTextArea();
        chatHistory.setLineWrap(true);
        chatHistory.setEditable(false);
        JScrollPane jScrollPane = new JScrollPane(chatHistory);

        usersList = new JTextArea();
        usersList.setEditable(false);
        JScrollPane usersScroll = new JScrollPane(usersList);
        usersScroll.setPreferredSize(new Dimension(100, 350));

        bottom = new JPanel();
        bottom.setLayout(new BorderLayout());
        bottom.setPreferredSize(new Dimension(300, 50));

        JButton send = new JButton("Send");
        message = new JTextField();
        message.setPreferredSize(new Dimension(200, 50));

        login = new JTextField();
        password = new JPasswordField();
        JButton auth = new JButton("Login");
        top = new JPanel(new GridLayout(1,3));
        top.add(login);
        top.add(password);
        top.add(auth);

        send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                sendMessage();
            }
        });
        message.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                sendMessage();
            }
        });
        auth.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                auth();
            }
        });

        bottom.add(send, BorderLayout.EAST);
        bottom.add(message, BorderLayout.CENTER);

        add(jScrollPane, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);
        add(usersScroll, BorderLayout.EAST);

        switchWindows();
        setVisible(true);
    }
    public void sendMessage(){
        String message = this.message.getText();
        this.message.setText("");
        clientConnection.sendMessage(message);
    }
    public void auth(){
        clientConnection.init(this);
        clientConnection.auth(login.getText(), new String(password.getPassword()));
        login.setText("");
        password.setText("");
    }
    public void showMessage(String message){
        chatHistory.append(message + "\n");
        chatHistory.setCaretPosition(chatHistory.getDocument().getLength());
    }
    public void switchWindows(){
        top.setVisible(!clientConnection.isAuthrozied());
        bottom.setVisible(clientConnection.isAuthrozied());
    }
    public void showUsersList(String[] users){
        usersList.setText("");
        for(int i = 1; i < users.length; i++){
            usersList.append(users[i] + "\n");
        }
    }
}
