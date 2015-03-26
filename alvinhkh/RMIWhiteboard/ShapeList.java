/* 
 * Java RMI Whiteboard
 * CM3202 Mini-project
 *
 * 1265987
 */

package alvinhkh.RMIWhiteboard;

import java.rmi.*;
import java.util.Vector;

public interface ShapeList extends Remote {

    Shape newShape(GraphicalObject g) throws RemoteException;

    Vector<Shape> allShapes() throws RemoteException;

    void updateShape(GraphicalObject oldG, GraphicalObject newG) throws RemoteException;

    void deleteShape(GraphicalObject oldG) throws RemoteException;

    int getVersion() throws RemoteException;

    int register(WhiteboardCallback callback) throws RemoteException;

    void deregister(int callbackId) throws RemoteException;

}
