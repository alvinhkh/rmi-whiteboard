# Java RMI Whiteboard
***2015 March, CM3202 Emerging Technologies Coursework***

### Main Features 
- Drawing simple graphical objects, such as circles, triangles and squares
- Add new graphical objects to the shared white-board
- Obtain from the server a list of the current set of graphical objects on the white-board
- Display the current white-board using the list
- Specifying type of graphical object (circle, triangle or square), colour, size, and position
- Server callback, to notice client to update whiteboard (WhiteboardCallback)

###### Client Graphical Interface
 * Enter IP Address and Port number
 * Draw object using mouse dragging
 * Select object using mouse click
 * Update or delete selected object
 * Update position of object using mouse dragging or keyboard arrows
 * Whiteboard size is limited
 * Choose border colour and fill colour using JColorChooser dialog

###### Server
 * Specify port number in first command-line argument (below example start with port 2020)
 * ```
$ java alvinhkh.RMIWhiteboard.ShapeListServer 2020```


*Outline of the code for this Java RMI program is based on Section 5.5 of the book “Distributed Systems: Concepts and Design” by George Coulouris et al.*