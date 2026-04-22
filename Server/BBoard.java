import java.net.ServerSocket;
import java.net.Socket;

/**
 * BBOARD is the main configuration for the server program.
 * It will initialize the server from the command line arguments such as:
 *  - port
 *  - board dimensions
 *  - note dimensions
 *  - colors
 * 
 * It will create a board from the given arguments and open a 
 * ServerSocket to continuously accept client connections. It 
 * will also make sure to start a new ClientHandler thread
 * for each connected client
 * 
 */



public class BBoard {
    public static void main(String[] args) throws Exception {
        if (args.length < 6) {
            // Make sure it gives the minimum 6 agruments of: port, boardW, boardH, noteW, noteH, color
            System.out.println("Usage: java BBoard <port> <boardW> <boardH> <noteW> <noteH> <color1> ... <colorN>");
            return;
        }
        
        // Parse each value 
        int port = Integer.parseInt(args[0]);
        int boardW = Integer.parseInt(args[1]);
        int boardH = Integer.parseInt(args[2]);
        int noteW = Integer.parseInt(args[3]);
        int noteH = Integer.parseInt(args[4]);

        // Create a string array for all the possible colors
        String[] colors = new String[args.length - 5];
        for (int i = 5; i < args.length; i++) {
            colors[i - 5] = args[i];
        }
        
        // Create the board with all the values
        Board board = new Board(boardW, boardH, noteW, noteH, colors);

        // Main ServerSocket connection that will run continuously
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server listening on port " + port);

        while (true) {
            Socket client = serverSocket.accept();
            ClientHandler handler = new ClientHandler(client, board, boardW, boardH, noteW, noteH, colors);
            new Thread(handler).start();
        }
    }
}


/**
 * REFERENCES
 * ------------------
 * 
 * https://github.com/MustafaDaraghmeh/socket-programming-java-web-server
 * 
 * https://www.geeksforgeeks.org/java/java-net-serversocket-class-in-java/
 * 
 * 
 */