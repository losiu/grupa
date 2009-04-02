package grupa;

import battlecode.common.*;
import static battlecode.common.GameConstants.*;

import static team009.MessageTranslator.*;

public class Archon extends AbstractRobot implements RobotApi{

	private ArchonStatus status = ArchonStatus.LOOKING_FOR_DEPOSIT;
	
	private boolean lookingForNearestDeposit = true;
	
	private int roundNumNotNearest;
		
	private Direction myDirection  = Direction.NORTH;
		
	public Archon(RobotController rc) {
		super(rc);
	}
	
	public void setFirstDirection() throws Exception{
		
		MapLocation[] archonsLocations = myRC.senseAlliedArchons();
		
		// gets statistics of not fixed archons's locations	
		boolean first = true;
		int maxX = 0; int maxY = 0;
		int minX = 0; int minY = 0;
			
		for (int i = 0; i < archonsLocations.length; i++) {
			
			if (first || maxX < archonsLocations[i].getX())
				maxX = archonsLocations[i].getX();
					
			if (first || minX > archonsLocations[i].getX())
				minX = archonsLocations[i].getX();
				
			if (first || maxY < archonsLocations[i].getY())
				maxY = archonsLocations[i].getY();
				
			if (first || minY > archonsLocations[i].getY())
				minY = archonsLocations[i].getY();
		
			first = false;
		}
			
		int myX = myRC.getLocation().getX();
		int myY = myRC.getLocation().getY();
		
		Direction myDirection = myRC.getDirection();

		if (myY == maxY && myX != maxX)
			myDirection = Direction.SOUTH;
				
		if (myY == minY && myX != maxX)
			myDirection = Direction.NORTH;
	 			
		if (myX == maxX && myY != maxY)
			myDirection = Direction.EAST;
				
		if (myX == minX && myY != maxY)
			myDirection = Direction.WEST;
	 	
		if (myX == maxX && myY == maxY)
			myDirection = Direction.SOUTH_EAST;
				
		if (myX == maxX && myY == minY)
			myDirection = Direction.NORTH_EAST;

		if (myX == minX && myY == maxY)
			myDirection = Direction.SOUTH_WEST;
				
		if (myX == minX && myY == minY)
			myDirection = Direction.NORTH_WEST;
	 	
		myRC.setDirection(myDirection);
		myRC.yield();
		
	}
	
	public boolean hasEnergonToSpawn(RobotType robotType){
		return (myRC.getEnergonLevel() > robotType.spawnCost());
	}
	
	public boolean canSpawn(Direction direction, RobotType robotType) throws Exception{
		
		MapLocation spawnLocation = myRC.getLocation().add(direction);
	
		return (isOnMap(spawnLocation)
				&& isFreeLocation(spawnLocation)
				&& hasEnergonToSpawn(robotType));
		
	}
	
	public boolean spawnSoldier(Direction direction) throws Exception{

		boolean hasBeenSpawned = false;
		
		if (direction != myRC.getDirection()){
			while (myRC.isMovementActive()){
				myRC.yield();
			}
			myRC.setDirection(direction);
			myRC.yield();
		}
		
		if (canSpawn(myRC.getDirection(), RobotType.SOLDIER)){
			
				myRC.spawn(RobotType.SOLDIER);
				hasBeenSpawned = true;
				myRC.yield();
				
				fillRobotsWithEnergon();
				
		}
		
		return hasBeenSpawned;
		
	}
	
	public void fillRobotsWithEnergon () throws Exception{
		
		//System.out.println("fillRobotsWithEnergon(level)");
		
		//System.out.println("level = " + RobotType.SOLDIER.maxEnergon() * 4 / 5);
		
		double minEnergon = 1;
		
		while (minEnergon != 0 && minEnergon < RobotType.SOLDIER.maxEnergon() * 4 / 5) {
			//System.out.println("konieczny transfer");
			minEnergon = transferEnergon();
			myRC.yield();
		}
		
	}
	
	public void firstSpawnSoldiers() throws Exception{
	
		int spawnedSoldiersNum = 0;
		int count = 0;
		
		Direction direction = myRC.getDirection();
		
		while (spawnedSoldiersNum < 1 && count < 8){
			
			while (!hasEnergonToSpawn(RobotType.SOLDIER)){
				myRC.yield();
			}
			
			if (spawnSoldier(direction))
				spawnedSoldiersNum++;
			
			direction = direction.rotateRight();
			count++;
		}
	}
	
	public void spawnSoldiers() throws Exception{
		
		Robot[] soldiers = senseMyRobots(RobotType.SOLDIER);
		Robot[] archons = senseMyRobots(RobotType.ARCHON);
		
		if (soldiers.length < 3 * archons.length){
		
			if (hasEnergonToSpawn(RobotType.SOLDIER)){
				spawnSoldier(myRC.getDirection());
			}
			
		}
		
	}
	
	/*
	public double directionRang(Direction newDirection, Direction direction, MapLocation[] archons){
	
		double rang = 0;
		
		Direction leftDirection = newDirection;
		Direction rightDirection = newDirection;

		while (direction != leftDirection && direction != rightDirection){
		
			leftDirection = leftDirection.rotateLeft();
			rightDirection = rightDirection.rotateRight();
			rang++;
			
		}
		
		double actDistance = 0;
		
		for (int i = 0; i < archons.length; i++){
			actDistance+=archons[i].distanceSquaredTo(myRC.getLocation());
		}
		
		MapLocation newLocation = myRC.getLocation().add(newDirection);
		
		double newDistance = 0;
		
		for (int i = 0; i < archons.length; i++){
			newDistance+=archons[i].distanceSquaredTo(newLocation);
		}
		
		rang = rang + (actDistance - newDistance) / actDistance * 100;
		
		return rang;
		
	}
	
	public Direction getNextDirection(){
		
		MapLocation[] archons = myRC.senseAlliedArchons();
		Direction direction = myRC.getDirection();
		
		double minRang = 0;
		boolean first = true;
		Direction directionWithMinRang = myRC.getDirection(); 
		
		for (int i = 0; i < archons.length; i++) {
					
			Double directionRang = directionRang(direction, myRC.getDirection(), archons);
			
			if (first || minRang > directionRang) {
				minRang = directionRang;
				directionWithMinRang = direction;
			}
				
		
			first = false;
			
			direction = direction.rotateRight();
		}
		
		return directionWithMinRang;
	
	}
	*/
	
	
	public double transferEnergon() throws Exception{
	
		double minEnergon = 0;
		
		// energon will be transfered to one of ground robots
		Robot[] groundRobots = myRC.senseNearbyGroundRobots();

		int idRobot = chooseRobotToTransferEnergon(groundRobots);
			
		if (idRobot < groundRobots.length) {
			RobotInfo robotInfo = myRC.senseRobotInfo(groundRobots[idRobot]);
			
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

		return minEnergon;
		
	}
	
	public MapLocation[] senseNeutralFluxDepositLocations() throws Exception{
		
		FluxDeposit[] fluxDeposits = myRC.senseNearbyFluxDeposits();
		
		int count = 0;
		
		for (int i = 0; i < fluxDeposits.length; i++) {
			FluxDepositInfo depositInfo = myRC.senseFluxDepositInfo(fluxDeposits[i]);
			if (depositInfo.team == Team.NEUTRAL){
				count++;
			}
		}
		
		MapLocation[] depositsLocations = new MapLocation[count];
		int m = 0;
		
		for (int i = 0; i < fluxDeposits.length; i++) {
			FluxDepositInfo depositInfo = myRC.senseFluxDepositInfo(fluxDeposits[i]);
			if (depositInfo.team == Team.NEUTRAL){
				depositsLocations[m] = depositInfo.location;
				m++;
			}
		}
		
		return depositsLocations;
		
	}
	

	public void findFlux() throws Exception{
				
		while (myRC.isMovementActive()){
			myRC.yield();
		}
		
		if (lookingForNearestDeposit)
			roundNumNotNearest = Clock.getRoundNum();
		
		MapLocation[] archonLocations = senseMyRobotsLocations(RobotType.ARCHON);
		MapLocation[] depositsLocations = senseNeutralFluxDepositLocations();
		
		boolean[] archonIsFixed = new boolean[archonLocations.length];
		int notFixedarchonsNum = archonLocations.length;
		
		if (depositsLocations.length >= archonLocations.length)
			lookingForNearestDeposit = true;
		
		// there are more archons then deposits
		// every deposit should has its fixed archon and not fixed 
		// archons should move in some different directions  
			if (depositsLocations.length < archonLocations.length) {
				
				for (int i = 0; i < archonLocations.length; i++) 
	 	     		archonIsFixed[i] = false;
	 			
	 			for (int i = 0; i < depositsLocations.length; i++) {
	 					
	 				// idOfFixedArchon == archonLocations.length
	 				// means then any archon is fixed
	 				int idOfFixedArchon = archonLocations.length; 
	 				int minDistance = 0;
	 	     		
	 				// looking for the nearest archon to this fluxDeposit 
	 				for (int j = 0; j < archonLocations.length; j++){
	 					int distance = depositsLocations[i].distanceSquaredTo(archonLocations[j]);
	 					if (!archonIsFixed[j] && (idOfFixedArchon == archonLocations.length 
	 						|| distance < minDistance)){
	 	     					minDistance = distance;
	 	     					idOfFixedArchon = j;
	 					}
	 	     				
	 				}
	 	     			
	 				if (idOfFixedArchon < archonLocations.length) {
	 					archonIsFixed[idOfFixedArchon] = true;
	 					notFixedarchonsNum --;
	 				}
	 	     				 	 	     			 					
	 			}
	 			
	 			// gets statistics of not fixed archons's locations	
	 			boolean first = true;
	 			int maxX = 0; int maxY = 0;
	 			int minX = 0; int minY = 0;
				
	 			for (int i = 0; i < archonLocations.length; i++) {
	 				if (!archonIsFixed[i]){
	 					if (first || maxX < archonLocations[i].getX())
	 						maxX = archonLocations[i].getX();
	 					
	 					if (first || minX > archonLocations[i].getX())
	 						minX = archonLocations[i].getX();
	 				
	 					if (first || maxY < archonLocations[i].getY())
	 						maxY = archonLocations[i].getY();
	 				
	 					if (first || minY > archonLocations[i].getY())
	 						minY = archonLocations[i].getY();
	 				}
	 				first = false;
	 			}
	 			
	 			
	 			int myX = myRC.getLocation().getX();
	 			int myY = myRC.getLocation().getY();
	 			
	 			
	 			// there are not deposits, but some archon should be fixed
	 			// to move in senseDirectionToUnownedFluxDeposit
	 			if (depositsLocations.length == 0 && archonLocations.length > 0){
	 				for (int i = 0; i < archonLocations.length; i++) {
	 					if (archonLocations[i].getX() == maxX
	 							&& archonLocations[i].getY() == maxY){
	 								archonIsFixed[i] = true;
	 								notFixedarchonsNum --;
	 					}	
	 				}
	 			}
	 			
	 			
	 			// if there are more then one not fixed archons,
	 			// they should move in some different directions
	 			if (notFixedarchonsNum > 1){
	 				
	 				if (myY == maxY && myX != maxX)
	 					myDirection = Direction.SOUTH;
	  				
	 				if (myY == minY && myX != maxX)
	 					myDirection = Direction.NORTH;
	 	 			
	 				if (myX == maxX && myY != maxY)
	 					myDirection = Direction.EAST;
	  				
	 				if (myX == minX && myY != maxY)
	 					myDirection = Direction.WEST;
	 	 		
	 				if (myX == maxX && myY == maxY)
	 					myDirection = Direction.SOUTH_EAST;
	  				
	 				if (myX == maxX && myY == minY)
	 					myDirection = Direction.NORTH_EAST;

	 				if (myX == minX && myY == maxY)
	 					myDirection = Direction.SOUTH_WEST;
	  				
	 				if (myX == minX && myY == minY)
	 					myDirection = Direction.NORTH_WEST;
	 	 			
	 			}
	 			
	 			  			
	 			lookingForNearestDeposit = false;
	 			
	 			for (int i = 0; i < archonLocations.length; i++) {
	 				if (archonIsFixed[i]
	 				    && archonLocations[i].getX() == myRC.getLocation().getX()
	 				    && archonLocations[i].getY() == myRC.getLocation().getY()){
	 						lookingForNearestDeposit = true;
	 				}
	 				
	 			}
					
			}
			
 		Direction direction;
 		
 		//System.out.println("lookingForNearestDeposit = " + lookingForNearestDeposit);
			
 		if (lookingForNearestDeposit) {
 			direction = myRC.senseDirectionToUnownedFluxDeposit();
 		} else {
 			//direction = myRC.getDirection();
 			direction = myDirection;
 			if (Clock.getRoundNum() > roundNumNotNearest + ROUNDS_TO_CAPTURE / 2){
 				lookingForNearestDeposit = true;
 			}
					
 		}
		
		if (direction == Direction.OMNI) {
				
			status = ArchonStatus.DEPOSIT_FOUND;
			
		} else {
					
			tryToMoveInDirection(direction);
			
		}	
		
	}
	
	static public Message moveInDirectionMsg(Direction direction){
		
		int[] ints = new int[2];
		
		ints[0] = MOVE_IN_DIRECTION;
		ints[1] = toInt(direction);
		
		Message message = new Message(); 
		message.ints = ints;
		
		return message;
		
	}
	
	public int senseMaxArchonId() throws Exception{
		
		int maxId = myRC.getRobot().getID();
		
		Robot[] archons = senseMyRobots(RobotType.ARCHON);
		
		for (int i = 0; i < archons.length; i++){
			maxId = Math.max(maxId, archons[i].getID());
		}
		
		return maxId;
		
	}
			
	public void sendRequestToSoldiers() throws Exception{
		
		Direction direction = myRC.getDirection();
		
		if (senseMaxArchonId() == myRC.getRobot().getID()){
			
			Message message = moveInDirectionMsg(direction);
			myRC.broadcast(message);
			myRC.yield();
			
		}
			
	}
	
	
	public void nextTurn() throws Exception{
	
		switch(status){
			
		/*
			case CREATING_ME:
							
				setFirstDirection();
				
				if(myRC.canMove(myRC.getDirection()))
					myRC.moveForward();
				else
					myRC.setDirection(myRC.getDirection().rotateRight());
				
				myRC.yield();
				
				firstSpawnSoldiers();
				
				status = ArchonStatus.LOOKING_FOR_DEPOSIT;
		
				break;
		*/
					
			case LOOKING_FOR_DEPOSIT:
				
				//spawnSoldiers();
				
				//fillRobotsWithEnergon();
				
				findFlux();
				
				//sendRequestToSoldiers();
			
				break;
			
			case DEPOSIT_FOUND:
				
				fillRobotsWithEnergon();
				
				//blocksLocations = myRC.senseNearbyBlocks();
				
				status = ArchonStatus.CAPTURING_DEPOSIT;
				
				break;
				
			case CAPTURING_DEPOSIT:
				break;
		}
		
	}

}
