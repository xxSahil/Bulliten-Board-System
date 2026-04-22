import java.awt.*;
import java.io.*;
import java.net.Socket;
import javax.swing.*;

public class ClientGUI {
    private JFrame frame;
    private JTextField hostBox;
    private JTextField portBox;
    private JTextArea outputArea;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientGUI() {
        
        // Main window
        frame = new JFrame("Bulletin Board Client");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Board layout will be split into top/middle/bottom
        frame.setLayout(new BorderLayout(8, 8));

        //  Top: connection panel 
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hostBox = new JTextField("localhost");
        portBox = new JTextField("4554");
        JButton connectBtn = new JButton("Connect");

        topPanel.add(new JLabel("Host:"));
        topPanel.add(hostBox);
        topPanel.add(new JLabel("Port:"));
        topPanel.add(portBox);
        topPanel.add(connectBtn);
        frame.add(topPanel, BorderLayout.NORTH);

        // Middle: buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 4, 15, 15));
        JButton postBtn = new JButton("POST");
        JButton getBtn = new JButton("GET (filters)");
        JButton getPinsBtn = new JButton("GET PINS");
        JButton pinBtn = new JButton("PIN");
        JButton unpinBtn = new JButton("UNPIN");
        JButton shakeBtn = new JButton("SHAKE");
        JButton clearBtn = new JButton("CLEAR");
        JButton disconnectBtn = new JButton("DISCONNECT");

        buttonPanel.add(postBtn);
        buttonPanel.add(getBtn);
        buttonPanel.add(getPinsBtn);
        buttonPanel.add(pinBtn);
        buttonPanel.add(unpinBtn);
        buttonPanel.add(shakeBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(disconnectBtn);
        frame.add(buttonPanel, BorderLayout.CENTER);

        // Bottom: output
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(outputArea);
        scroll.setPreferredSize(new Dimension(700, 400));
        frame.add(scroll, BorderLayout.SOUTH);

        connectBtn.addActionListener(e -> connect());
        postBtn.addActionListener(e -> sendPost());
        getBtn.addActionListener(e -> sendGetFilters());
        getPinsBtn.addActionListener(e -> sendCommand("GET PINS"));
        pinBtn.addActionListener(e -> sendPin());
        unpinBtn.addActionListener(e -> sendUnpin());
        shakeBtn.addActionListener(e -> sendCommand("SHAKE"));
        clearBtn.addActionListener(e -> sendCommand("CLEAR"));
        disconnectBtn.addActionListener(e -> sendCommand("DISCONNECT"));

        frame.setVisible(true);
    }

    // Connection
    private void connect() {
        try {
            String host = hostBox.getText().trim();
            int port = Integer.parseInt(portBox.getText().trim());

            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            outputArea.append("Connected to server\n");

            // Read handshake (3 lines)
            for (int i = 0; i < 3; i++) {
                String line = in.readLine();
                outputArea.append(line + "\n");
            }

            outputArea.append("\n");
        } catch (Exception ex) {
            outputArea.append("Connection failed: " + ex.getMessage() + "\n\n");
        }
    }

    /**
     * Commands are done by sending one line to the server
     * which is reads and prints the servers response
     * 
     * We are using this for commands that dont need an input from the
     * client such as; GET PINS, SHAKE, CLEAR, DISCONNECT
     * 
     */
    private void sendCommand(String cmd) {
        try {
            if (!isConnected()) return;

            out.println(cmd);
            readResponse();

            // If disconnected, close socket locally too
            if (cmd.equalsIgnoreCase("DISCONNECT")) {
                closeConnection();
            }
        } catch (Exception e) {
            outputArea.append("Error sending command: " + e.getMessage() + "\n\n");
        }
    }

    /**
     * The following send commands require some sort of input. So there
     * will need to be input of values and they must account for any
     * error handling, which in this case will just be that the command
     * is not sent.
     * 
     * These are for the commands; POST, PIN, UNPIN, GET filters
     */

    private void sendPost() {
        try {
            if (!isConnected()) return;

            String x = JOptionPane.showInputDialog(frame, "POST: x");
            if (x == null) return;

            String y = JOptionPane.showInputDialog(frame, "POST: y");
            if (y == null) return;

            String color = JOptionPane.showInputDialog(frame, "POST: color");
            if (color == null) return;

            String msg = JOptionPane.showInputDialog(frame, "POST: message");
            if (msg == null) return;

            if (x.trim().isEmpty() || y.trim().isEmpty() || color.trim().isEmpty() || msg.trim().isEmpty()) {
                outputArea.append("POST cancelled: missing fields\n\n");
                return;
            }

            out.println("POST " + x.trim() + " " + y.trim() + " " + color.trim() + " " + msg);
            readResponse();

        } catch (Exception e) {
            outputArea.append("POST error: " + e.getMessage() + "\n\n");
        }
    }

    private void sendPin() {
        try {
            if (!isConnected()) return;

            String x = JOptionPane.showInputDialog(frame, "PIN: x");
            if (x == null) return;

            String y = JOptionPane.showInputDialog(frame, "PIN: y");
            if (y == null) return;

            if (x.trim().isEmpty() || y.trim().isEmpty()) {
                outputArea.append("PIN cancelled: missing fields\n\n");
                return;
            }

            out.println("PIN " + x.trim() + " " + y.trim());
            readResponse();

        } catch (Exception e) {
            outputArea.append("PIN error: " + e.getMessage() + "\n\n");
        }
    }

    private void sendUnpin() {
        try {
            if (!isConnected()) return;

            String x = JOptionPane.showInputDialog(frame, "UNPIN: x");
            if (x == null) return;

            String y = JOptionPane.showInputDialog(frame, "UNPIN: y");
            if (y == null) return;

            if (x.trim().isEmpty() || y.trim().isEmpty()) {
                outputArea.append("UNPIN cancelled: missing fields\n\n");
                return;
            }

            out.println("UNPIN " + x.trim() + " " + y.trim());
            readResponse();

        } catch (Exception e) {
            outputArea.append("UNPIN error: " + e.getMessage() + "\n\n");
        }
    }

    private void sendGetFilters() {
        try {
            if (!isConnected()) return;

            String color = JOptionPane.showInputDialog(frame, "GET filter: color (leave blank for none)");
            if (color == null) return;
            color = color.trim();

            String cx = JOptionPane.showInputDialog(frame, "GET filter: contains x (leave blank for none)");
            if (cx == null) return;
            cx = cx.trim();

            String cy = JOptionPane.showInputDialog(frame, "GET filter: contains y (leave blank for none)");
            if (cy == null) return;
            cy = cy.trim();

            String refers = JOptionPane.showInputDialog(frame, "GET filter: refersTo (leave blank for none)");
            if (refers == null) return;
            refers = refers.trim();

            String cmd = "GET";

            if (!color.isEmpty()) cmd += " color=" + color;

            // Only add contains if both x and y are provided
            if (!cx.isEmpty() || !cy.isEmpty()) {
                if (cx.isEmpty() || cy.isEmpty()) {
                    outputArea.append("GET cancelled: contains requires both x and y\n\n");
                    return;
                }
                cmd += " contains=" + cx + " " + cy;
            }

            if (!refers.isEmpty()) cmd += " refersTo=" + refers;

            out.println(cmd);
            readResponse();

        } catch (Exception e) {
            outputArea.append("GET error: " + e.getMessage() + "\n\n");
        }
    }


    /**
     * Read each server response and print it to the bottom
     * output area. Possibilities are ERROR or OK followed by 
     * number of lines of info. 
     * 
     */
    private void readResponse() throws IOException {
        String firstLine = in.readLine();
        if (firstLine == null) {
            outputArea.append("Server closed connection.\n\n");
            closeConnection();
            return;
        }

        outputArea.append(firstLine + "\n");

        // If server response is OK with a numeric count, read that many extra lines
        if (firstLine.startsWith("OK ")) {
            String[] parts = firstLine.split("\\s+");
            if (parts.length == 2) {
                try {
                    int count = Integer.parseInt(parts[1]);
                    for (int i = 0; i < count; i++) {
                        String extra = in.readLine();
                        if (extra == null) break;
                        outputArea.append(extra + "\n");
                    }
                } catch (NumberFormatException ignored) {
                    // This is used for responses with no integer
                    // such as "OK PIN_ADDED"
                }
            }
        }

        outputArea.append("\n");
    }

    // Helpers
    private boolean isConnected() {
        if (out == null || in == null || socket == null || socket.isClosed()) {
            outputArea.append("Not connected. Click Connect first.\n\n");
            return false;
        }
        return true;
    }

    private void closeConnection() {
        try { if (in != null) in.close(); } catch (Exception ignored) {}
        try { if (out != null) out.close(); } catch (Exception ignored) {}
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}

        in = null;
        out = null;
        socket = null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }
}


/**
 * REFERENCES
 * -----------------
 * https://www.geeksforgeeks.org/java/introduction-to-java-swing/
 * 
 * 
 */