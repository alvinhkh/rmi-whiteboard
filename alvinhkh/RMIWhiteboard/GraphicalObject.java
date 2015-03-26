/* 
 * Java RMI Whiteboard
 * CM3202 Mini-project
 *
 * 1265987
 */

package alvinhkh.RMIWhiteboard;

import java.util.UUID;

import java.awt.Rectangle;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.io.Serializable;

@SuppressWarnings("serial")
public class GraphicalObject implements Serializable {
    
    public UUID uuid;

    public String type;

    public Rectangle enclosing;

    public Color line;

    public Color fill;

    public boolean isFilled;

    // Constructors
    public GraphicalObject() { }
    
    public GraphicalObject(UUID anUuid,
                            String aType,
                            Rectangle anEnclosing,
                            Color aLine,
                            Color aFill,
                            boolean anIsFilled) {
        uuid = anUuid;
        type = aType;
        enclosing = anEnclosing;
        line = aLine;
        fill = aFill;
        isFilled = anIsFilled;
    }

    public GraphicalObject(String aType,
                            Rectangle anEnclosing,
                            Color aLine,
                            Color aFill,
                            boolean anIsFilled) {
        uuid = UUID.randomUUID();
        type = aType;
        enclosing = anEnclosing;
        line = aLine;
        fill = aFill;
        isFilled = anIsFilled;
    }

    public void print() {
        System.out.print(type);
        System.out.print(" ");
        System.out.print(enclosing.x + " , " + enclosing.y + " , " + enclosing.width + " , "  + enclosing.height);
        System.out.print(" ");
        if (isFilled) {
            System.out.println("- filled");
        } else {
            System.out.println("not filled");
        }
    }

}
