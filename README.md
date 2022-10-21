# Intro to CS (Java) Project 2

This was my biggest project so far, and it was a group project with one other person. Implements threads, GUIs, and networking.

### How to run:
* Need to have javafx sdk downloaded and library loaded
* Run either version of the server
  * WAMServerGUI
    * java WAMServerGUI.java
    * Input the Port, Rows, Columns, Players, and Game Time into the UI
    * Hit start
  * WAMServer
    * java WAMServer.java \<Port> \<Rows> \<Columns> \<Players> \<Time>
* Run the client code
  * If there are two players then each player needs to run the client code
  * WAMGUI
    * java WAMGUI.java \<Host Address> \<Port>