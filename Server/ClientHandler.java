import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * ClientHandler will manage communciation with a client
 * 
 * It will send handshake information when a client connect,
 * read command line by line from a client, parse the command to
 * call the Board method and send the Board's response back to the client
 */

public class ClientHandler implements Runnable {
    private Socket socket;
    private Board board;

    private int boardW, boardH, noteW, noteH;
    private String[] colors;

    public ClientHandler(Socket socket, Board board, int boardW, int boardH, int noteW, int noteH, String[] colors) {
        this.socket = socket;
        this.board = board;
        this.boardW = boardW;
        this.boardH = boardH;
        this.noteW = noteW;
        this.noteH = noteH;
        this.colors = colors;
    }

    public void run() {
        try {

            // Input and output stream for a client
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Handshake
            out.println("BOARD " + boardW + " " + boardH);
            out.println("NOTE_SIZE " + noteW + " " + noteH);
            out.println("COLORS " + join(colors));

            // Continuously read lines
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();

                if (line.length() == 0) {
                    out.println("ERROR INVALID_FORMAT Empty command");
                    continue;
                }

                String[] parts = line.split("\\s+");
                String cmd = parts[0].toUpperCase();


                // Disconnect step
                if (cmd.equals("DISCONNECT")) {
                    out.println("OK BYE");
                    break;
                }

                // POST command
                else if (cmd.equals("POST")) {
                    String[] t = line.split("\\s+", 5);

                    // Check error handling for valid POST inputs
                    if (t.length < 5) {
                        out.println("ERROR INVALID_FORMAT POST requires x y color message");
                        continue;
                    }
                    if (!isInt(t[1]) || !isInt(t[2])) {
                        out.println("ERROR INVALID_FORMAT x and y must be integers");
                        continue;
                    }

                    int x = Integer.parseInt(t[1]);
                    int y = Integer.parseInt(t[2]);
                    String color = t[3];
                    String msg = t[4];

                    out.print(board.post(x, y, color, msg));
                    out.flush();
                }
                
                // PIN command
                else if (cmd.equals("PIN")) {

                    // Error handling for valid PIN values
                    if (parts.length != 3) {
                        out.println("ERROR INVALID_FORMAT PIN requires x y");
                        continue;
                    }
                    if (!isInt(parts[1]) || !isInt(parts[2])) {
                        out.println("ERROR INVALID_FORMAT x and y must be integers");
                        continue;
                    }


                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);

                    out.print(board.pin(x, y));
                    out.flush();
                }

                // UNPIN command
                else if (cmd.equals("UNPIN")) {

                    // Error handling for UNPIN values
                    if (parts.length != 3) {
                        out.println("ERROR INVALID_FORMAT UNPIN requires x y");
                        continue;
                    }
                    if (!isInt(parts[1]) || !isInt(parts[2])) {
                        out.println("ERROR INVALID_FORMAT x and y must be integers");
                        continue;
                    }


                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);

                    out.print(board.unpin(x, y));
                    out.flush();
                }

                // SHAKE command
                else if (cmd.equals("SHAKE")) {
                    // Make sure there are no arguments with the SHAKE
                    if (parts.length != 1) {
                        out.println("ERROR INVALID_FORMAT SHAKE takes no arguments");
                        continue;
                    }
                    out.print(board.shake());
                    out.flush();
                }

                // CLEAR command
                else if (cmd.equals("CLEAR")) {
                    // Make sure there are no arguments with the CLEAR
                    if (parts.length != 1) {
                        out.println("ERROR INVALID_FORMAT CLEAR takes no arguments");
                        continue;
                    }
                    out.print(board.clear());
                    out.flush();
                }

                // GET command
                else if (cmd.equals("GET")) {
                    // GET PINS
                    if (parts.length == 2 && parts[1].equalsIgnoreCase("PINS")) {
                        out.print(board.getPinsProtocol());
                        out.flush();
                        continue;
                    }

                    // Filter based GET
                    String color = null;
                    Integer cx = null;
                    Integer cy = null;
                    String refersTo = null;

                    // Parse each filter token after GET
                    
                    for (int i = 1; i < parts.length; i++) {
                        
                        // Color filter
                        if (parts[i].startsWith("color=")) {
                            color = parts[i].substring("color=".length());
                        
                        // Coordinate filter
                        } else if (parts[i].startsWith("contains=")) {

                            // Get just the string of the x in contains
                            String xs = parts[i].substring("contains=".length());
                            
                            // Check for error handling in values
                            if (!isInt(xs) || i + 1 >= parts.length || !isInt(parts[i + 1])) {
                                out.println("ERROR INVALID_FORMAT contains requires x y");
                                color = null; cx = null; cy = null; refersTo = null;
                                break;
                            }
                            cx = Integer.parseInt(xs);
                            cy = Integer.parseInt(parts[i + 1]);
                            i++;
                        
                        //refersTo filter
                        } else if (parts[i].startsWith("refersTo=")) {
                            refersTo = parts[i].substring("refersTo=".length());

                        // Error handling for unknown filter
                        } else {
                            out.println("ERROR INVALID_FORMAT Unrecognized GET filter");
                            
                            // Reset all filters
                            color = null;
                            cx = null;
                            cy = null; 
                            refersTo = null;
                            break;
                        }
                    }

                    out.print(board.getNotesProtocol(color, cx, cy, refersTo));
                    out.flush();
                }

                // Check for random command error handling
                else {
                    out.println("ERROR UNKNOWN_COMMAND " + cmd + " is not recognized");
                }
            }

            socket.close();
        } catch (Exception e) {
            // do nothing (server should not crash)
        }
    }

    // Helper Methods
    private boolean isInt(String s) {
        try { Integer.parseInt(s); return true; }
        catch (Exception e) { return false; }
    }

    private String join(String[] arr) {
        String s = "";
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) s += " ";
            s += arr[i];
        }
        return s;
    }
}


