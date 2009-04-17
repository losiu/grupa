package grupa;

import static battlecode.common.GameConstants.ENERGON_TRANSFER_RATE;
import battlecode.common.*;

import static grupa.MessageTranslator.*;

public class Soldier extends AbstractRobot implements RobotApi{
	
	SoldierStatus status = SoldierStatus.MOVING_WITH_ARCHON;
	
	Direction myDirection;
	
	//Robot myArchon;
	
	public Soldier(RobotController rc) {
		super(rc);
	}
	
	/*
	public void setMyArchon() throws Exception{
		
		MapLocation[] archons = myRC.senseAlliedArchons();
		
		for (int i = 0; i < archons.length; i++)
			System.out.println(archons[i]);
		
		
		double minDistance = 0, distance = 0;
		boolean first = true;
		
		for (int i = 0; i < archons.length; i++) {
			
			if (myRC.canSenseSquare(archons[i])){
				
				
				distance = myRC.getLocation().distanceSquaredTo(archons[i]);
				
				if (first || (minDistance > distance && robot != null)) {
					minDistance = distance;
					myArchon = robot;
				}
					
				first = false;
			}
			
		}
		
		
		
	}
	*/
	
	/*
	public void moveWithArchon(Direction direction) throws Exception{
		
		while (myRC.isMovementActive()){
			myRC.yield();
		}
		
		if (myRC.canMove(direction) && direction != myRC.getDirection()) {
			myRC.setDirection(direction);
		} else {
			if(myRC.canMove(myRC.getDirection())){
				myRC.moveForward();
			} else {
				myRC.setDirection(myRC.getDirection().rotateRight());
			}
					
		}
		
		myRC.yield();
		
	}
	*/
	
	public double transferEnergon() throws Exception{
		
		double minEnergon = 0;
		
		// energon will be transfered to one of ground robots
		Robot[] groundRobots = myRC.senseNearbyGroundRobots();

		int idRobot = chooseRobotToTransferEnergon(groundRobots);
			
		if (idRobot < groundRobots.length){
			
			RobotInfo robotInfo = myRC.senseRobotInfo(groundRobots[idRobot]);
			
			/*
			if (myRC.getEnergonLevel() > myRC.getRobotType().maxEnergon() * 2 / 3
				&& robotInfo.eventualEnergon < robotInfo.maxEnergon * 1 / 3) {
			*/
			
			if (myRC.getEnergonLevel() > myRC.getRobotType().maxEnergon() * 1 / 3) {
			
				minEnergon = robotInfo.eventualEnergon;
			
				double amountOfEnergon = myRC.getEnergonLevel();
				if (amountOfEnergon > ENERGON_TRANSFER_RATE)
					amountOfEnergon = ENERGON_TRANSFER_RATE;
				if (amountOfEnergon > robotInfo.maxEnergon - robotInfo.eventualEnergon)
					amountOfEnergon = robotInfo.maxEnergon - robotInfo.eventualEnergon;
				
			
				if (amountOfEnergon > 0
					&& myRC.senseGroundRobotAtLocation(robotInfo.location) != null ) {
					
					myRC.transferEnergon(amountOfEnergon, robotInfo.location, RobotLevel.ON_GROUND);
					myRC.yield();
				
				}
			}	
			
		}

		return minEnergon;
		
	}
	
	/*
	public boolean messageIsToMe(Message message){
		return (message.ints[0] == myRC.getRobot().getID());
	}
	*/
	/*
	public int typeOfMessage(Message message){
		return message.ints[0];
	}
	*/
	
	public void nextTurn() throws Exception{
		
		MapLocation archonLocation = findTheNearestArchon(myRC.getRobot());
		
		if (myRC.getLocation().isAdjacentTo(archonLocation)){
			
			double eventualEnergon = myRC.senseRobotInfo(myRC.getRobot()).eventualEnergon;
			double maxEnergon = myRC.senseRobotInfo(myRC.getRobot()).maxEnergon;
			
			if (eventualEnergon < maxEnergon / 2)
				myRC.yield();
			
		}
		
		// the next (if exists) message addressed to me in message queue
		Message message = myRC.getNextMessage();
		
		while (message != null && !msgIsToMe(message)){
			message = myRC.getNextMessage();
		}
		
		if (message != null && message.ints[0] == MOVE_IN_DIRECTION){
			myDirection = toDirection(message.ints[1]);
		}
		
		switch(status){
		
			/*
			case CREATING_ME:
				
				
				setMyArchon();
				status = SoldierStatus.MOVING_WITH_ARCHON;
				break;
			*/
				
                
			case MOVING_WITH_ARCHON:
				
				transferEnergon();
				
				if (myRC.getEventualEnergonLevel() < myRC.getMaxEnergonLevel() * 1 / 2){
			
					archonLocation = findTheNearestArchon(myRC.getRobot());
				
					Direction direction = myRC.getLocation().directionTo(archonLocation);
				
					tryToMoveInDirection(direction);
				
				} else {
					
					Direction direction = myDirection;
					
					if (direction == Direction.OMNI)
						direction = Direction.EAST;
					
					tryToMoveInDirection(direction);
					
					
					transferEnergon();
					
					/*
					Message message = myRC.getNextMessage();
					
					myRC.clearBroadcast();
					
					if (message == null){
						
						myRC.yield();
					
					} else {
						
						Direction direction = MessageTranslator.toDirection(message.ints[1]);
					
						tryToMoveInDirection(direction);
							
					}
					*/
					
				}
					
				break;
		}
		
		/*
		Message message = myRC.getNextMessage();
		
		
		while (message == null || !messageIsToMe(message)){
			myRC.yield();
			message = myRC.getNextMessage();
		}
		
		
		switch(typeOfMessage(message)){
		
			case MessageTranslator.MOVE_IN_DIRECTION:
				
				
				Direction direction = myRC.getLocation().directionTo(message.locations[0]);
			
				moveWithArchon(direction);
				
				
//				//moveWithArchon(MessageTranslator.toDirection(message.ints[2]));
				
				break;
		
		}
		*/
		
	}
		
}
