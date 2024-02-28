package chatserver;


import java.awt.BorderLayout;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.IconifyAction;

// IT21252990
// Jayathilaka A.G.K.D.

/**
 * A simple Swing-based client for the chat server.  Graphically
 * it is a frame with a text field for entering messages and a
 * textarea to see the whole dialog.
 *
 * The client follows the Chat Protocol which is as follows.
 * When the server sends "SUBMITNAME" the client replies with the
 * desired screen name.  The server will keep sending "SUBMITNAME"
 * requests as long as the client submits screen names that are
 * already in use.  When the server sends a line beginning
 * with "NAMEACCEPTED" the client is now allowed to start
 * sending the server arbitrary strings to be broadcast to all
 * chatters connected to the server.  When the server sends a
 * line beginning with "MESSAGE " then all characters following
 * this string should be displayed in its message area.
 */
public class ChatClient {

    BufferedReader in;
    PrintWriter out;
    //name is added for each client
    String name ="" ;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(20, 30);
    // TODO: Add a list box
    //list box and list model to maintain all the client names
    DefaultListModel<String> listModel = new DefaultListModel<>();
    ArrayList<String> data = new ArrayList<>() ;
    JList<String> list = new JList<String>(listModel);
    //checkbox for broadcast
    JCheckBox checkBox = new JCheckBox("Broadcast");
    JLabel greetingLabel = new JLabel();
    

    /**
     * Constructs the client by laying out the GUI and registering a
     * listener with the textfield so that pressing Return in the
     * listener sends the textfield contents to the server.  Note
     * however that the textfield is initially NOT editable, and
     * only becomes editable AFTER the client receives the NAMEACCEPTED
     * message from the server.
     */
    public ChatClient() {
    	
    
        // Layout GUI

        
        textField.setEditable(false);
     // Set the placeholder text
        textField.setToolTipText("Enter your message here");
        messageArea.setEditable(false);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(greetingLabel, BorderLayout.NORTH); // Add greeting label to the top
        frame.getContentPane().add(textField, BorderLayout.SOUTH); // Move text field to the bottom
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.getContentPane().add(list, BorderLayout.EAST);
        frame.getContentPane().add(checkBox , BorderLayout.LINE_START) ;
        list.setPreferredSize(new Dimension(100, 100));
        frame.pack();

        // TODO: You may have to edit this event handler to handle point to point messaging,
        // where one client can send a message to a specific client. You can add some header to 
        // the message to identify the recipient. You can get the receipient name from the listbox.
        textField.addActionListener(new ActionListener() {
            /**
             * Responds to pressing the enter key in the textfield by sending
             * the contents of the text field to the server.    Then clear
             * the text area in preparation for the next message.
             */
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
                
                //printing the checkbox status
                out.println(checkBox.isSelected());
                //printing the selected client
                out.println(list.getSelectedValue()) ;
                
            }
        });
        
        
    }

    /**
     * Prompt for and return the address of the server.
     */
    private String getServerAddress() {
        return JOptionPane.showInputDialog(
            frame,
            "Enter IP Address of the Server:",
            "Welcome to the Chatter",
            JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Prompt for and return the desired screen name.
     */
    private String getName() {
    	// get user entered name
    	name = JOptionPane.showInputDialog(
            frame,
            "Choose a screen name:",
            "Screen name selection",
            JOptionPane.PLAIN_MESSAGE);
    	
    	frame.setName(name);
    	// Set the greeting label text
        greetingLabel.setText("Hello " + name);
        frame.pack(); // Update frame layout
    	return name ;
    }

    /**
     * Connects to the server then enters the processing loop.
     */
    private void run() throws IOException {

        // Make connection and initialize streams
        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 9001);
        in = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Process all messages from server, according to the protocol.
        
        // TODO: You may have to extend this protocol to achieve task 9 in the lab sheet
        while (true) {
            String line = in.readLine();
            if (line.startsWith("SUBMITNAME")) {
                out.println(getName());
            } else if (line.startsWith("NAMEACCEPTED")) {
                textField.setEditable(true);
                //getting all the client names from the server
                String nm = in.readLine() ;
                nm = nm.substring(1, nm.length() - 1); // Remove the square brackets
                //adding each client name to the array
                String[] elements = nm.split(",\\s*");
                for (String element : elements) {
                	//if the list model doesnt have the client name add the client name
                    if(!listModel.contains(element)) {
                    	listModel.addElement(element);
                    }
                }
                //updating the jlist
                frame.getContentPane().add(list, BorderLayout.EAST) ;
                frame.pack();
            } else if (line.startsWith("BROADCAST")) {
            	//printing the broadcast message to all the clients
            	messageArea.append(line.substring(10) + "\n");	
            } else if (line.startsWith(name)) {
            	//printing the non broadcast message if the receiver name and the client name are equal
            	messageArea.append(line.substring(name.length()) + "\n");	
            } else if (line.contains(name)) {
            	//printing the message if the sender is and the receiver is same
//            	messageArea.append(line.substring(line.indexOf(name)) + "\n");
            	messageArea.append("Error ! : Please Select the People to Chat !!" + "\n");
			} else if (line.startsWith("NAMELIST")) {
			    // Update the client list based on the received list of names
			    String nm = line.substring(8);
			    nm = nm.substring(1, nm.length() - 1); // Remove the square brackets
			    String[] elements = nm.split(",\\s*");
			    listModel.clear(); // Clear existing names
			    for (String element : elements) {
			        listModel.addElement(element.trim());
			    }
			}
        }
    }

    /**
     * Runs the client as an application with a closeable frame.
     */
    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.frame.setSize(600, 400);
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}