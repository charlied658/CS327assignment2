public class TCB_Server{
  int nodeIDServer; //node ID of server , similar as IP address
  int portNumServer; //port number of server
  int nodeIDClient; //node ID of client , similar as IP address
  int portNumClient; //port number of client
  int stateServer; //state of server; CLOSED 1, LISTENING 2, CONNECTED 3, CLOSEWAIT 4.

  final static int CLOSED = 1;
  final static int LISTENING = 2;
  final static int CONNECTED = 3;
  final static int CLOSEWAIT = 4;

  public TCB_Server(int whatPortNumServer){

    portNumServer = whatPortNumServer;
    stateServer=1;

  }

  public TCB_Server(int whatNodeIDServer, int whatPortNumServer, int whatNodeIDClient, int whatPortNumClient, int whatStateClient){
    nodeIDServer = whatNodeIDServer;
    portNumServer = whatPortNumServer;
    nodeIDClient = whatNodeIDClient;
    portNumClient = whatPortNumClient;

    if(whatStateClient < 0 || whatStateClient > 4){
      System.out.println("Please enter a value between 0 and 4");
    }//end if
    else{
      stateServer = whatStateClient;
    }//end else

  }//end constructor
}//end class TCBServer
