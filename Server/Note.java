/**
 * Note represents a note on the board
 * and has the following:
 *  - Its top left corner as (x, y) coordinates
 *  - Its color 
 *  - Its text message
 * 
 * The dimensions of the note are pre-defined by the server as 
 * well as its possible colors
 * 
 */


public class Note {

    // Top left coordinate of the Note
    public int x;
    public int y;

    // Color and string attached to Note
    public String color;
    public String message;

    // Create a note at the given (x, y) coordinate with a color and message
    public Note(int x, int y, String color, String message) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.message = message;
    }

    /**
     * Checks if a point is inside the dimensions of the Note
     * 
     * This is needed so it can be used by:
     *  - PIN: To place a pin inside of a note
     *  - GET contains=: To find a note at a coordinate
     *  - PINNED=true/false: To check if any pins are inside a note
     */
    public boolean containsPoint(int px, int py, int noteW, int noteH) {
        return px >= x && px < x + noteW &&
               py >= y && py < y + noteH;
    }

    // Check for complete overlap as per the RFC
    public boolean completeOverlap(Note other) {
        return this.x == other.x && this.y == other.y;
    }
}
