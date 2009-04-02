package grupa;

import battlecode.common.*;
//import static battlecode.common.GameConstants.*;

public class RobotPlayer implements Runnable {

   //private final RobotController myRC;
   private RobotApi robot;
   
   public RobotPlayer(RobotController rc) {
	   
	  switch (rc.getRobotType()){
	  
	  	case ARCHON:
	  		robot = new Archon(rc);
	  		break;
	  		
	  	case WORKER:
	  		robot = new Worker(rc);
	  		break;
	  		
	  	case SOLDIER:
	  		robot = new Soldier(rc);
	  		break;
	  		
	  }
	    
   }

   public void run() {
      while(true){
         try{  
         
        	/*** beginning of main loop ***/
        	robot.nextTurn();
            /*** end of main loop ***/
 
         } catch(Exception e) {
        	 
            System.out.println("caught exception:");
            e.printStackTrace();
            
         }
      }
   }
}
