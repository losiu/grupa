package grupa;

import java.util.HashMap;

import battlecode.common.*;
import static battlecode.common.GameConstants.*;

import static grupa.MessageTranslator.*;

/**
* @author losiu
*/
public class Archon extends AbstractRobot implements RobotApi{

	private ArchonStatus status = ArchonStatus.LOOKING_FOR_DEPOSIT;
	
	
	// LOOKING_FOR_DEPOSIT:
	
	private boolean lookingForNearestDeposit = true;
	private int roundNumNotNearest;
	private Direction myDirection  = Direction.NORTH;
	
	
	
	// CAPTURING_DEPOSIT:
	
	//tyle maks rund WORKER moze przezyc bez transferu energon
	static public final double MAX_ROUNDS_WORKER_HAVE_NOT_BE_SEEN
		= RobotType.WORKER.maxEnergon() / RobotType.WORKER.energonUpkeep();
	
	static public final int MAX_WORKERS = 5;
	
	// Archon wie gdzie warto szukac blokow
	MapLocation[] blocksLocations;

	// informacja o tym kiedy ostatnio widziano workera o podanym id
	// aby okreslic ile posiadamy workerow - chcemy miec ich okreslona ilosc
	private HashMap<Integer, Integer> workers = new HashMap<Integer, Integer>();
	
	
	
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
	
	public boolean spawnRobot(Direction direction, RobotType robotType) throws Exception{

		boolean hasBeenSpawned = false;
		
		if (direction != myRC.getDirection()){
			while (myRC.isMovementActive()){
				myRC.yield();
			}
			myRC.setDirection(direction);
			myRC.yield();
		}
		
		if (canSpawn(myRC.getDirection(), robotType)){
			
				myRC.spawn(robotType);
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
			
			if (spawnRobot(direction, RobotType.SOLDIER))
				spawnedSoldiersNum++;
			
			direction = direction.rotateRight();
			count++;
		}
	}
	
	public void spawnRobots(RobotType robotType) throws Exception{
		
		Robot[] soldiers = senseMyRobots(RobotType.SOLDIER);
		//Robot[] archons = senseMyRobots(RobotType.ARCHON);
		
		boolean shouldBeSpawned = true;
		
		switch(robotType){
			case WORKER:
				
				Robot[] robots = senseMyRobots(RobotType.WORKER);
				for (int i = 0; i < robots.length; i++){
					workers.remove(robots[i].getID());
					workers.put(robots[i].getID(), Clock.getRoundNum());	
				}
				
				int workersNum = 0;
				
				for (int key: workers.keySet()){
					if (workers.get(key) + MAX_ROUNDS_WORKER_HAVE_NOT_BE_SEEN < Clock.getRoundNum())
						workersNum++;
					else
						workers.remove(key);
				}
				
				if (workersNum >= MAX_WORKERS)
					shouldBeSpawned = false;
				
		}
		
		
		if (shouldBeSpawned){
		
			if (hasEnergonToSpawn(robotType)){
				spawnRobot(myRC.getDirection(), robotType);
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
		
 		//[losiu] chwilowo zrobilem tak, ze wszyscie archony chca
 		//dojsc do najblizszego flux deposit
 		direction = myRC.senseDirectionToUnownedFluxDeposit();
 		
 		/*
 		if (lookingForNearestDeposit) {
 			direction = myRC.senseDirectionToUnownedFluxDeposit();
 		} else {
 			//direction = myRC.getDirection();
 			direction = myDirection;
 			if (Clock.getRoundNum() > roundNumNotNearest + 5){
 				lookingForNearestDeposit = true;
 			}
					
 		}
 		*/
 		
		
		if (direction == Direction.OMNI) {
				
			status = ArchonStatus.DEPOSIT_FOUND;
			
		} else {
					
			tryToMoveInDirection(direction);
			
		}	
		
	}
	
	/*
	public Message moveInDirectionMsg(Direction direction){
		
		int[] ints = new int[2];
		
		ints[0] = MOVE_TO_FLUX_DEPOSIT;
		ints[1] = myRC.getLocation();
		//ints[1] = toInt(direction);
		
		Message message = new Message(); 
		message.ints = ints;
		
		return message;
		
	}
	*/
	
	public Message msgMoveToFluxDeposit(){
		
		int[] ints = new int[1];
		ints[0] = MOVE_TO_FLUX_DEPOSIT;
		
		MapLocation[] locations = new MapLocation[1];
		locations[0] = myRC.getLocation();
		
		Message message = new Message(); 
		message.ints = ints;
		message.locations = locations;
		
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
			
			Message message = msgMoveToFluxDeposit();
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
				
				//spawnRobots(RobotType.SOLDIER);
				
				//fillRobotsWithEnergon();
				
				findFlux();
				
				//sendRequestToSoldiers();
			
				break;
			
			case DEPOSIT_FOUND:
				
				fillRobotsWithEnergon();
				
				blocksLocations = myRC.senseNearbyBlocks();
				
				status = ArchonStatus.CAPTURING_DEPOSIT;
				
				break;
				
			case CAPTURING_DEPOSIT:
				
				spawnRobots(RobotType.WORKER);
				
				break;
		}
		
	}

}
