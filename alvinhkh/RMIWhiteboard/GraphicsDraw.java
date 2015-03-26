/* 
 * Java RMI Whiteboard
 * CM3202 Mini-project
 *
 * 1265987
 */

package alvinhkh.RMIWhiteboard;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Polygon;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.*;

public class GraphicsDraw extends JComponent {

    private static final long serialVersionUID = 1L;
    
    GraphicalObject obj;
    Shape shape;
    int x, y, w, h;
    
    public GraphicsDraw(GraphicalObject g) {
        this.obj = g;
        this.x = obj.enclosing.x;
        this.y = obj.enclosing.y;
        this.w = Math.abs( obj.enclosing.width );
        this.h = Math.abs( obj.enclosing.height );
        shape = create();

        //setPreferredSize(new Dimension(w, h));
        setOpaque(true);
        setVisible(true);
        //setBounds(obj.enclosing.x, obj.enclosing.y, obj.enclosing.width, obj.enclosing.height);
    }
    
    public Shape getShape() {
        return shape;
    }
    
    protected Shape create() {
        switch (obj.type) {
            case "Triangle":
                Polygon p = new Polygon();
                p.addPoint(this.x, this.y+this.h);
                p.addPoint(this.x+(this.w/2), this.y);
                p.addPoint(this.x+this.w, this.y+this.h);
                return p;
            case "Oval":
            case "Circle":
                return new Ellipse2D.Float( this.x, this.y, this.w, this.h );
            case "Rectangle":
            case "Square":
            default:
                return new Rectangle2D.Float( this.x, this.y, this.w, this.h );
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (isOpaque()) {
            shape = create();
            
            Graphics2D g2 = (Graphics2D) g;

            // Fill
            if (obj.isFilled == true) {
                g2.setPaint(obj.fill);
                g2.fill(shape);
            }
            // Border
            Stroke stroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0);//,new float[] { 3, 1 }, 0);
            g2.setStroke(stroke);
            g2.setPaint(obj.line);
            g2.draw(shape);
        }
    }

}