import java.util.ArrayList;

/**
 * Board will represent the entire board state. 
 * It will hold the information of all:
 *  - Posted notes
 *  - Pins
 * 
 * Since this is used in a multithreaded server, the public methods 
 * are synchronized to ensure that only one
 * thread at a time can be executed and the other threads must wait
 * until the method finishes from a previous call.
 * 
*/

public class Board {
    // Start with board dimension and colors
    private int boardW, boardH, noteW, noteH;
    private String[] colors;

    // Create an array for all the notes and pins on the board
    private ArrayList<Note> notes = new ArrayList<>();
    private ArrayList<int[]> pins = new ArrayList<>();

    // Initialize the Board
    public Board(int boardW, int boardH, int noteW, int noteH, String[] colors) {
        this.boardW = boardW;
        this.boardH = boardH;
        this.noteW = noteW;
        this.noteH = noteH;
        this.colors = colors;
    }

    /* 
        Helper methods for consistent error handling 
        and internal checks
    */ 

    // Checks if a note is within the board dimension
    private boolean noteFits(int x, int y) {
        return x >= 0 && y >= 0 &&
               x + noteW <= boardW &&
               y + noteH <= boardH;
    }

    // Checks if atlease one pin is inside a note
    private boolean noteIsPinned(Note n) {
        for (int i = 0; i < pins.size(); i++) {
            int[] p = pins.get(i);
            if (n.containsPoint(p[0], p[1], noteW, noteH)) {
                return true;
            }
        }
        return false;
    }

    private int countNotesAtPoint(int x, int y) {
        int count = 0;
        for (int i = 0; i < notes.size(); i++) {
            Note n = notes.get(i);
            if (n.containsPoint(x, y, noteW, noteH)) {
                count++;
            }
        }
        return count;
    }
    private boolean isValidColor(String c) {
        for (String color : colors) {
            if (color.equals(c)){
                return true;
            }
        }
        return false;
    }

    /* 
       Main command Methods
    */ 

    // POST: Place a note onto the board
    public synchronized String post(int x, int y, String color, String message) {
        
        // First check with helper methods for error handling
        if (!noteFits(x, y))
            return "ERROR OUT_OF_BOUNDS Note exceeds board dimensions\n";

        if (!isValidColor(color))
            return "ERROR COLOR_NOT_SUPPORTED " + color + " is not a valid color\n";
        
        // Create a new note
        Note newNote = new Note(x, y, color, message);

        // Check for complete overlap of a new note
        for (int i = 0; i < notes.size(); i++) {
            Note n = notes.get(i);
            if (n.completeOverlap(newNote))
                return "ERROR COMPLETE_OVERLAP Note overlaps an existing note entirely\n";
        }

        // Add the note with message
        notes.add(newNote);
        return "OK NOTE_POSTED\n";
    }

    // PIN: Add a pin if a note exists at that point
    public synchronized String pin(int x, int y) {
        if (countNotesAtPoint(x, y) == 0)
            return "ERROR NO_NOTE_AT_COORDINATE No note contains the given point\n";

        pins.add(new int[]{x, y});
        return "OK PIN_ADDED\n";
    }

    // Unpin: Remove a pin at a certain coordinate
    public synchronized String unpin(int x, int y) {
        for (int i = 0; i < pins.size(); i++) {
            int[] p = pins.get(i);
            if (p[0] == x && p[1] == y) {
                pins.remove(i);
                return "OK PIN_REMOVED\n";
            }
        }
        return "ERROR PIN_NOT_FOUND No pin exists at the given coordinates\n";
    }

    // Clear: Remove all notes and pins on the board
    public synchronized String clear() {
        notes.clear();
        pins.clear();
        return "OK CLEAR_COMPLETE\n";
    }

    // SHAKE: Remove all unpinned notes
    public synchronized String shake() {
        for (int i = notes.size() - 1; i >= 0; i--) {
            if (!noteIsPinned(notes.get(i))) {
                notes.remove(i);
            }
        }
        return "OK SHAKE_COMPLETE\n";
    }

    /**
     * Protocol responses for GET
     * 
     * The format for each should be:
     *  OK <Count of lines>
     *  (Followed by info about the certain GET)
     * 
     */
    public synchronized String getPinsProtocol() {
        StringBuilder sb = new StringBuilder();
        sb.append("OK ").append(pins.size()).append("\n");
        for (int i = 0; i < pins.size(); i++) {
            int[] p = pins.get(i);
            sb.append("PIN ").append(p[0]).append(" ").append(p[1]).append("\n");
        }
        return sb.toString();
    }

    public synchronized String getNotesProtocol(String color, Integer cx, Integer cy, String refersTo) {
        ArrayList<Note> foundNotes = new ArrayList<>();

        for (int i = 0; i < notes.size(); i++) {
            Note n = notes.get(i);
            if (color != null && !n.color.equals(color)) continue;
            if (cx != null && cy != null && !n.containsPoint(cx, cy, noteW, noteH)) continue;
            if (refersTo != null && !n.message.contains(refersTo)) continue;
            foundNotes.add(n);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("OK ").append(foundNotes.size()).append("\n");
        for (int i = 0; i < foundNotes.size(); i++) {
            Note n = foundNotes.get(i);
            sb.append("NOTE ")
              .append(n.x).append(" ")
              .append(n.y).append(" ")
              .append(n.color).append(" ")
              .append(n.message)
              .append(" PINNED=").append(noteIsPinned(n) ? "true" : "false")
              .append("\n");
        }
        return sb.toString();
    }
}


/**
 * REFERENCES
 * -------------------
 * 
 * https://www.geeksforgeeks.org/java/stringbuilder-class-in-java-with-examples/
 * 
 * https://www.w3schools.com/java/java_arraylist.asp
 * 
 * https://docs.oracle.com/javase/tutorial/essential/concurrency/syncmeth.html
 * 
 * 
 */