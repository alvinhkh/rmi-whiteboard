/* 
 * Java RMI Whiteboard
 * CM3202 Mini-project
 *
 * 1265987
 */

package alvinhkh.RMIWhiteboard;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

import java.util.ArrayList;
import java.util.Vector;
import java.util.Iterator;

import javax.swing.*;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Cursor;
import java.awt.Color;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Point;
import java.awt.event.*;


public class ShapeListClient {
    
    private String serverIp = "localhost";
    private Integer serverPort = 2020;
    
    Boolean initiated = false;
    ShapeList aShapeList = null;
    Integer aCallbackId = 0;
    boolean add_flag = false;
    boolean delete_flag = false;
    boolean update_flag = false;
    boolean refresh_flag = false;
    boolean exit_flag = false;
    int tool_type = 1; //0=pen, 1=selector
    boolean updateEverySingleMove = false;
    
    JRadioButton rectangleButton = null;
    JRadioButton squareButton = null;
    JRadioButton ovalButton = null;
    JRadioButton circleButton = null;
    JRadioButton triangleButton = null;
    JCheckBox shapeFill = null;
    JPanel borderColourDisplay = null;
    JPanel fillColourDisplay = null;
    
    String shapeType = "Rectangle"; // Rectangle/Circle/Triangle
    Color borderColour = Color.BLACK;
    Color fillColour = Color.BLACK;

    JButton addButton = null;
    JButton deleteButton = null;
    JButton updateButton = null;
    JButton refreshButton = null;
    JButton zoombutton = null;

    JLabel mousePosition;
    JPanel messagePanel;
    JLabel statusMessage;
    JLabel serverHost;
    
    int x, y, w, h;
    boolean newObject = true;
    boolean keyMove = false;
    GraphicalObject gSelectedObject = null;
    GraphicalObject gNewObject = null;

    Vector<Shape> sList = null;
    ArrayList<GraphicalObject> gList = null;
    
    WhiteboardPanel wPanel = null;
    
    
    public static void main(String args[]) {
        new ShapeListClient();
    }
    

    public ShapeListClient() {

        initiate();
        
        if (initiated == false) return;

        try {
            WhiteBoardFrame wFrame = new WhiteBoardFrame(aShapeList);
            
            
            // listens for window closing events
            wFrame.addWindowListener( new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent event) {
                    deregister();
                }
                @Override
                public void windowClosed(WindowEvent event) {
                    deregister();
                }
            });
            
            refresh_flag = true;

            String m = null;
            while (!exit_flag) {

                //if pressed delete button 
                if (delete_flag == true) {
                    delete_flag = false;
                    
                    if (gSelectedObject != null) {
                        aShapeList.deleteShape(gSelectedObject);
                        gSelectedObject = null;
                        gNewObject = null;
                        deleteButton.setEnabled(false);
                        updateButton.setEnabled(false);
                    }
                    
                    //refresh_flag = true;
                }
                
                if (refresh_flag == true) {
                    refresh_flag = false;

                    //if (keyMove != true) gSelectedObject = null;
                    gNewObject = null;
                    getList();
                    if (wPanel != null) wPanel.repaint();
                }
                
                if (add_flag == true) {
                    add_flag = false;
                    
                    if (x > 0 && y > 0 && w > 0 && h > 0) {
                        Boolean fill = shapeFill.isSelected();
                        GraphicalObject g = new GraphicalObject(
                            shapeType, 
                            new Rectangle(x,y,w,h),
                            borderColour,
                            fillColour,
                            fill);
                        aShapeList.newShape(g);
                    }
                    newObject = false;
                    gSelectedObject = null;
                    gNewObject = null;
                    addButton.setEnabled(false);
                    
                    //refresh_flag = true;
                }
                
                if (update_flag == true) {
                    update_flag = false;
                    
                    if (gSelectedObject != null) {
                        Boolean fill = shapeFill.isSelected();
                        GraphicalObject g = new GraphicalObject(
                            gSelectedObject.uuid,
                            shapeType, 
                            gSelectedObject.enclosing,
                            borderColour,
                            fillColour,
                            fill);
                        aShapeList.updateShape(gSelectedObject, g);
                        gSelectedObject = g;
                    }
                    
                    //refresh_flag = true;
                }

            }
            deregister();
            System.exit(0);

        } catch (RemoteException e) {
            errorMessage(e.getMessage());
        }
        deregister();

    }
    
    private void initiate() {
        JTextField ipField = new JTextField(serverIp);
        JTextField portField = new JTextField(serverPort.toString());

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 0.15;
        gc.gridx = 0;
        gc.gridy = 0;
        inputPanel.add(new JLabel("IP Address:"), gc);
        gc.weightx = 0.85;
        gc.gridx = 1;
        gc.gridy = 0;
        inputPanel.add(ipField, gc);
        gc.weightx = 0.15;
        gc.gridx = 0;
        gc.gridy = 1;
        inputPanel.add(new JLabel("Port:"), gc);
        gc.weightx = 0.85;
        gc.gridx = 1;
        gc.gridy = 1;
        inputPanel.add(portField, gc);

        int result = JOptionPane.showConfirmDialog(null, inputPanel, 
           "Please enter server details", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            serverIp = ipField.getText();
            serverPort = Integer.parseInt( portField.getText() );
            initiated = register();
        } else {
            System.exit(0);
        }
    }
    
    private Boolean register() {
        try {
            Registry registry = LocateRegistry.getRegistry(serverIp, serverPort);
            aShapeList = (ShapeList) registry.lookup("Whiteboard");
            System.out.println("Found server");
            WhiteboardCallback callback = new Callback();
            aCallbackId = aShapeList.register(callback);
            System.out.println("callback registered: " + aCallbackId);
            return true;
        } catch (RemoteException e) {
            errorMessage(e.getMessage());
            System.out.println("Connection Fail: " + e.getMessage());
        } catch (NotBoundException e) {
            errorMessage(e.getMessage());
            System.out.println("No associated binding: " + e.getMessage());
        }
        return false;
    }
    
    private void deregister() {
        // Deregister callback when closing window
        try {
            if (aCallbackId > 0 && aShapeList != null) {
                aShapeList.deregister(aCallbackId);
                System.out.println("callback deregistered: " + aCallbackId);
            }
        } catch (RemoteException e) {
            System.out.println("Connection Fail: " + e.getMessage());
        }
    }
    
    private void getList() {
        try {
            sList = aShapeList.allShapes();
            System.out.println("Got All Shapes");
            gList = new ArrayList<GraphicalObject>();
            for (int i = 0; i < sList.size(); i++){
                GraphicalObject gObj = sList.elementAt(i).getAllState();
                gList.add(gObj);
            }
        } catch (RemoteException e) {
            sList = null;
            errorMessage(e.getMessage());
            System.out.println("Connection Fail: " + e.getMessage());
        }
    }
    
    private void errorMessage(String message) {
        JFrame frame = new JFrame();
        JOptionPane.showMessageDialog(frame,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE);
        System.exit(0);
    }
    
    private class ControlPanel extends JPanel implements ActionListener {
        
        private final static long serialVersionUID = 1L;
    
        public ControlPanel() {
            // Control Panel
            setLayout(new GridLayout(0, 1));
            
            rectangleButton = new JRadioButton("Rectangle");
            rectangleButton.setSelected(true);
            rectangleButton.setActionCommand("Rectangle");
            squareButton = new JRadioButton("Square");
            squareButton.setActionCommand("Square");
            ovalButton = new JRadioButton("Oval");
            ovalButton.setActionCommand("Oval");
            circleButton = new JRadioButton("Circle");
            circleButton.setActionCommand("Circle");
            triangleButton = new JRadioButton("Triangle");
            triangleButton.setActionCommand("Triangle");
            ButtonGroup shapeGroup = new ButtonGroup();
            shapeGroup.add(rectangleButton);
            shapeGroup.add(squareButton);
            shapeGroup.add(ovalButton);
            shapeGroup.add(circleButton);
            shapeGroup.add(triangleButton);

            GridBagConstraints gc = new GridBagConstraints();
            borderColourDisplay = new JPanel(new GridBagLayout());
            borderColourDisplay.setBackground(borderColour);
            JButton borderColourButton = new JButton("Border Colour");
            borderColourButton.setActionCommand("BorderColour");
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.weightx = 0.75;
            gc.gridx = 0;
            gc.gridy = 0;
            gc.gridwidth = 1;
            borderColourDisplay.add(borderColourButton, gc);
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.weightx = 0.25;
            gc.gridx = 2;
            gc.gridy = 0;
            gc.gridwidth = 2;
            borderColourDisplay.add(new JLabel(" "), gc);
            fillColourDisplay = new JPanel(new GridBagLayout());
            fillColourDisplay.setBackground(fillColour);
            JButton fillColourButton = new JButton("Fill Colour");
            fillColourButton.setActionCommand("FillColour");
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.weightx = 0.75;
            gc.gridx = 0;
            gc.gridy = 0;
            gc.gridwidth = 1;
            fillColourDisplay.add(fillColourButton, gc);
            shapeFill = new JCheckBox(); 
            shapeFill.setSelected(true);
            shapeFill.setOpaque(false);
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.weightx = 0.25;
            gc.gridx = 1;
            gc.gridy = 0;
            gc.gridwidth = 2;
            fillColourDisplay.add(shapeFill, gc);
            addButton = new JButton("Add");
            addButton.setActionCommand("Add");
            JButton drawButton = new JButton("Draw");
            drawButton.setActionCommand("Draw");
            JButton pickButton = new JButton("Pick");
            pickButton.setActionCommand("Pick");
            deleteButton = new JButton("Delete");
            deleteButton.setActionCommand("Delete");
            updateButton = new JButton("Update");
            updateButton.setActionCommand("Update");
            JButton refreshButton = new JButton("Refresh");
            refreshButton.setActionCommand("Refresh");
            JButton exitbutton = new JButton("Exit");
            exitbutton.setActionCommand("Exit");    
            
            rectangleButton.addActionListener(this);
            squareButton.addActionListener(this);
            ovalButton.addActionListener(this);
            circleButton.addActionListener(this);
            triangleButton.addActionListener(this);
            borderColourButton.addActionListener(this);
            fillColourButton.addActionListener(this);
            addButton.addActionListener(this);
            drawButton.addActionListener(this);
            pickButton.addActionListener(this);
            deleteButton.addActionListener(this);
            updateButton.addActionListener(this);
            refreshButton.addActionListener(this);
            exitbutton.addActionListener(this);
            
            add(rectangleButton);
            add(squareButton);
            add(ovalButton);
            add(circleButton);
            add(triangleButton);
            add(borderColourDisplay);
            add(fillColourDisplay);
            add(drawButton);
            add(pickButton);
            add(addButton);
            add(deleteButton);
            add(updateButton);
            add(refreshButton);    
            add(exitbutton);

            addButton.setEnabled(false);
            deleteButton.setEnabled(false);
            updateButton.setEnabled(false);
            
            setVisible(true);
        }
        
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("Rectangle")) {
                shapeType = "Rectangle";
            } else if (e.getActionCommand().equals("Square")) {
                shapeType = "Square";
            } else if (e.getActionCommand().equals("Oval")) {
                shapeType = "Oval";
            } else if (e.getActionCommand().equals("Circle")) {
                shapeType = "Circle";
            } else if (e.getActionCommand().equals("Triangle")) {
                shapeType = "Triangle";
            } else if (e.getActionCommand().equals("BorderColour")) {
                borderColour = JColorChooser.showDialog(this, "Choose line colour", borderColour);
                borderColourDisplay.setBackground(borderColour);
            } else if (e.getActionCommand().equals("FillColour")) {
                fillColour = JColorChooser.showDialog(this, "Choose fill colour", fillColour); 
                fillColourDisplay.setBackground(fillColour);
            } else if (e.getActionCommand().equals("Add")) {
                add_flag = true;
            } else if (e.getActionCommand().equals("Draw")) {
                tool_type = 0;
                deleteButton.setEnabled(false);
                updateButton.setEnabled(false);
                statusMessage.setText("Draw");
            } else if (e.getActionCommand().equals("Pick")) {            
                tool_type = 1;
                addButton.setEnabled(false);
                statusMessage.setText("Pick");
            } else if (e.getActionCommand().equals("Delete")) {    
                delete_flag = true;
            } else if (e.getActionCommand().equals("Update")) {    
                update_flag = true;
            } else if (e.getActionCommand().equals("Refresh")) {
                refresh_flag = true;
            } else if (e.getActionCommand().equals("Exit")) {
                exit_flag = true;
            }
        }
    }
    
    private class WhiteboardPanel extends JPanel implements KeyListener, MouseListener, MouseMotionListener {

        private final static long serialVersionUID = 1L;
        int x1, x2, y1, y2;
        int ox, oy;
        boolean isNewRect = true;
        boolean isDragging = false;
    
        ShapeList aShapeList = null;
        
        public WhiteboardPanel(ShapeList aShapeList) {
            addKeyListener(this); // listens for key events
            addMouseListener( this ); // listens for own mouse and
            addMouseMotionListener( this ); // mouse-motion events

            setOpaque(true);
            setBackground(Color.WHITE);
            setFocusable(true);
            setVisible(true);
            
            this.aShapeList = aShapeList;
        }
        
        // handle event when key press and release
        public void keyTyped(KeyEvent e) {
            //keyEvents(e, "keyTyped");
        }
        public void keyReleased(KeyEvent e) {
            keyEvents(e, "keyReleased");
        }
        public void keyPressed(KeyEvent e) {
            keyEvents(e, "keyPressed");
        }
        private void keyEvents(KeyEvent e, String eventType) {
            int key = e.getKeyCode();

            boolean move = false;
            int increment = 4;
            if (key == KeyEvent.VK_KP_LEFT || key == KeyEvent.VK_LEFT) {
                move = true;
                if (eventType.equals("keyPressed")) this.x2 = this.x2 - increment;
            } else if (key == KeyEvent.VK_KP_RIGHT || key == KeyEvent.VK_RIGHT) {
                move = true;
                if (eventType.equals("keyPressed")) this.x2 = this.x2 + increment;
            } else if (key == KeyEvent.VK_KP_UP || key == KeyEvent.VK_UP) {
                move = true;
                if (eventType.equals("keyPressed")) this.y2 = this.y2 - increment;
            } else if (key == KeyEvent.VK_KP_DOWN || key == KeyEvent.VK_DOWN) {
                move = true;
                if (eventType.equals("keyPressed")) this.y2 = this.y2 + increment;
            }
            
            if (tool_type == 1 && move == true) {
                keyMove = true;
                statusMessage.setText("Moving");
                repaint();
                if (updateEverySingleMove == true || eventType.equals("keyReleased")) {
                    updateList();
                }
            }
        }

        // handle event when mouse released immediately after press 
        public void mouseClicked( final MouseEvent event ) {
            mousePosition.setText( "Clicked at [" + event.getX() + ", " + event.getY() + "]" );
            keyMove = false;
            if (tool_type == 1) {
                getSelectedGraphicalObject(event);
            }
        }

        private void setControls(GraphicalObject g) {
            shapeType = g.type;
            if (shapeType.equals("Circle")) {
                circleButton.setSelected(true);
            } else if (shapeType.equals("Oval")) {
                ovalButton.setSelected(true);
            } else if (shapeType.equals("Square")) {
                squareButton.setSelected(true);
            } else if (shapeType.equals("Triangle")) {
                triangleButton.setSelected(true);
            } else {
                rectangleButton.setSelected(true);
            }
            borderColourDisplay.setBackground(g.line);
            borderColour = g.line;
            fillColourDisplay.setBackground(g.fill);
            fillColour = g.fill;
            shapeFill.setSelected(g.isFilled);
        }

        // handle event when mouse pressed 
        public void mousePressed( final MouseEvent event ) {
            mousePosition.setText( "Pressed at [" + event.getX() + ", " + event.getY() + "]" );
            
            keyMove = false;
            if (tool_type == 1) {
                getSelectedGraphicalObject(event);
            }

            this.x1 = event.getX();
            this.y1 = event.getY();
            this.x2 = event.getX();
            this.y2 = event.getY();
            
            if (tool_type == 0) {
                newObject = true;
                this.isNewRect = true;
                this.isDragging = true;
                addButton.setEnabled(true);
                repaint();
            }
            
            if (tool_type == 1 && gSelectedObject != null) {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                this.isDragging = true;
                this.ox = gSelectedObject.enclosing.x;
                this.oy = gSelectedObject.enclosing.y;
                //repaint();
            }
        }

        // handle event when mouse released after dragging 
        public void mouseReleased( final MouseEvent event ) {
            mousePosition.setText( "Released at [" + event.getX() + ", " + event.getY() + "]" );
            this.x2 = event.getX();
            this.y2 = event.getY();
            
            statusMessage.setText("");
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            
            if (tool_type == 0 && isDragging == true) {
                addButton.setEnabled(true);
                repaint();
            }

            if (tool_type == 1 && isDragging == true && gSelectedObject != null) {
                repaint();
                updateList();
                this.isDragging = false;
                deleteButton.setEnabled(false);
                updateButton.setEnabled(false);
            }
        }

        // handle event when mouse enters area 
        public void mouseEntered( final MouseEvent event ) {
            mousePosition.setText( "Mouse entered at [" + event.getX() + ", " + event.getY() + "]" );
            if (tool_type == 0 || isDragging == true) {
                repaint();
            }
        }

        // handle event when mouse exits area 
        public void mouseExited( final MouseEvent event ) {
            mousePosition.setText( "Mouse outside window" );
            if (tool_type == 0 || isDragging == true) {
                repaint();
            }
        }

        // handle event when user drags mouse with button pressed 
        public void mouseDragged( final MouseEvent event ) {
            mousePosition.setText( "Dragging at [" + event.getX() + ", " + event.getY() + "]" );

            this.x2 = event.getX();
            this.y2 = event.getY();
            
            if (tool_type == 0) {
                this.isNewRect = false;
                this.isDragging = false;
                repaint();
            }

            if (tool_type == 1) {
                this.isDragging = true;
                statusMessage.setText("Dragging");
                repaint();
                if (updateEverySingleMove == true) {
                    updateList();
                }
            }
        }

        // handle event when user moves mouse 
        public void mouseMoved( final MouseEvent event ) {
            mousePosition.setText( "Moved at [" + event.getX() + ", " + event.getY() + "]" );
            
            if (tool_type == 0 && isDragging == true) {
                repaint();
            }
            
            if (tool_type == 1 && isDragging == true && gSelectedObject != null) {
                isDragging = false;
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (isOpaque()) {
                
                if (sList == null) {
                    getList();
                }
                if (gList != null) {
                    removeAll();
                    Iterator<GraphicalObject> it = gList.iterator();
                    while (it.hasNext()) {
                        GraphicalObject gObj = it.next(); 
                        if (gObj == null) continue;
                        GraphicsDraw d = new GraphicsDraw(gObj);
                        if (d != null) d.paintComponent(g);
                        if (!gList.contains(gObj)) {
                            it.remove();
                        }
                    }
                }
                
                if (tool_type == 0 && newObject == true) {

                    int width = this.x1 - this.x2;
                    int height = this.y1 - this.y2;

                    w = Math.abs( width );
                    if (shapeType.matches("Circle|Square")) {
                        h = w;
                    } else {
                        h = Math.abs( height );
                    }
                    x = width < 0 ? this.x1
                        : this.x2;
                    y = height < 0 ? this.y1
                        : this.y2;

                    if ( !this.isNewRect ) {
                    
                        Boolean fill = shapeFill.isSelected();
                        GraphicalObject gObj = new GraphicalObject(
                            shapeType, 
                            new Rectangle(x,y,w,h),
                            borderColour,
                            fillColour,
                            fill);
                        GraphicsDraw d = new GraphicsDraw(gObj);
                        d.paintComponent(g);
                    
                    }
                
                    g.drawString( "Start [" + this.x1 + ", " + this.y1 + "]", this.x1, this.y1-2 );
                    g.drawString( "End [" + this.x2 + ", " + this.y2 + "]", this.x2, this.y2+2 );
                    statusMessage.setText("Drawing new object. Click Add button to make new changes.");
                
                }
            
                if (tool_type == 1) {
                    gNewObject = updatedGraphicalObject();
                    if (gNewObject != null) {
                        GraphicsDraw d = new GraphicsDraw(gNewObject);
                        d.paintComponent(g);
                        gSelectedObject = gNewObject;
                    }
                }
            }
        }
        
        private void updateList() {
            if (gSelectedObject == null || gNewObject == null) return;
            try {
                aShapeList.updateShape(gSelectedObject, gNewObject);
            } catch (RemoteException e) {
                errorMessage(e.getMessage());
                System.out.println("Connection Fail: " + e.getMessage());
            }
        }
        
        private GraphicalObject updatedGraphicalObject() {
            if (gSelectedObject == null) return null;
            int width = gSelectedObject.enclosing.width;
            int height = gSelectedObject.enclosing.height;
            
            int sx = this.x1-ox;
            int sy = this.y1-oy;
            
            w = Math.abs( width );
            h = Math.abs( height );
            x = (this.x1-ox) > 0 ? (this.x2-sx) : (this.x2+sx);
            y = (this.y1-oy) > 0 ? (this.y2-sy) : (this.y2+sy);
            
            // Within border
            if (x < 1) {
                x = 0;
            } else if (x > (getWidth() - w)) {
                x = getWidth() - w;
            }
            if (y < 1) {
                y = 0;
            } else if (y > (getHeight() - h)) {
                y = getHeight() - h;
            }
            
            GraphicalObject gObj = new GraphicalObject(
                gSelectedObject.uuid,
                gSelectedObject.type, 
                new Rectangle(x,y,w,h),
                gSelectedObject.line,
                gSelectedObject.fill,
                gSelectedObject.isFilled);
            
            //gObj.print();
            return gObj;
        }
        
        private void getSelectedGraphicalObject( final MouseEvent event ) {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            gSelectedObject = null;
            gNewObject = null;
            this.isDragging = false;
            deleteButton.setEnabled(false);
            updateButton.setEnabled(false);
            if (gList == null) return;
            Iterator<GraphicalObject> it = gList.iterator();
            while (it.hasNext()) {
                GraphicalObject g = it.next(); 
                if (g == null) continue;
                GraphicsDraw d = new GraphicsDraw(g);
                if (d != null) {
                    java.awt.Shape s = d.getShape();
                    if (s.contains(event.getX(), event.getY())) {
                        setCursor(new Cursor(Cursor.HAND_CURSOR));
                        deleteButton.setEnabled(true);
                        updateButton.setEnabled(true);
                        statusMessage.setText("Click Delete button to remove selected object or Drag object to new position.");
                        g.print();
                        setControls(g);
                        gSelectedObject = g;
                        this.ox = gSelectedObject.enclosing.x;
                        this.oy = gSelectedObject.enclosing.y;
                        this.x1 = event.getX();
                        this.y1 = event.getY();
                        this.x2 = event.getX();
                        this.y2 = event.getY();
                    }
                }
                if (!gList.contains(g)) {
                    it.remove();
                }
            }
        }
    
    }
    
    private class WhiteBoardFrame extends JFrame {
        
        public WhiteBoardFrame(ShapeList aShapeList) {
            super("WhiteBoard");
            
            setLayout(new GridBagLayout());

            GridBagConstraints gc = new GridBagConstraints();
            
            serverHost = new JLabel();
            serverHost.setText(serverIp + ":" + serverPort);
            gc.fill = GridBagConstraints.VERTICAL;
            gc.weightx = 0.05;
            gc.weighty = 0.05;
            gc.gridx = 0;
            gc.gridy = 0;
            add(serverHost, gc);
            
            // Control Panel
            ControlPanel controlPanel = new ControlPanel();
            gc.anchor = GridBagConstraints.CENTER;
            gc.fill = GridBagConstraints.NONE;
            gc.weightx = 0.05;
            gc.weighty = 0.85;
            gc.gridx = 0;
            gc.gridy = 1;
            add(controlPanel, gc);
            
            // Whiteboard Panel
            wPanel = new WhiteboardPanel(aShapeList);
            gc.fill = GridBagConstraints.BOTH;
            gc.weightx = 0.95;
            gc.weighty = 0.90;
            gc.gridx = 2;
            gc.gridy = 0;
            gc.gridheight = 2;
            add(wPanel, gc);
            
            messagePanel = new JPanel(new BorderLayout());
            messagePanel.setOpaque(true);
            statusMessage = new JLabel();
            statusMessage.setText("Pick");
            mousePosition = new JLabel();
            messagePanel.add( statusMessage, BorderLayout.EAST );
            messagePanel.add( mousePosition, BorderLayout.WEST );
            gc.fill = GridBagConstraints.BOTH;
            gc.weightx = 1;
            gc.weighty = 0.10;
            gc.gridx = 0;
            gc.gridy = 2;
            gc.gridwidth = 3;
            add(messagePanel, gc);

            setSize( 880, 620 );
            setResizable( false );
            setVisible( true );
            setLocation( 200, 0 );
            setLocationRelativeTo( null );
            setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        }
        
    }
    
    private class Callback extends UnicastRemoteObject implements WhiteboardCallback {
        
        private final static long serialVersionUID = 1L;
        
        public Callback () throws RemoteException {
        }
        
        public void callback(int version) throws RemoteException {
            System.out.println("updated " + version);
            refresh_flag = true;
        }
        
    }

}
