package grupa;

import battlecode.common.*;
//import static battlecode.common.GameConstants.*;

public abstract class AbstractRobot implements RobotApi{
	
	protected final RobotController myRC;
	
	public AbstractRobot(RobotController rc) {
		myRC = rc;
	}
	
	boolean isOnMap (MapLocation mapLocation){
		
		if (!myRC.canSenseSquare(mapLocation))
			return false;
		
		return (myRC.senseTerrainTile(mapLocation).getType()
				!= TerrainTile.TerrainType.OFF_MAP);
	}
	
	boolean isFreeLocation(MapLocation mapLocation) throws Exception{
		
		if (!myRC.canSenseSquare(mapLocation))
			return false;
		
		return (myRC.senseGroundRobotAtLocation(mapLocation) == null);
	}
		
	public void setDirectionToUnownedFluxDeposit() throws Exception{
		
		while (myRC.isMovementActive()){
			myRC.yield();
		}
		
		Direction direction = myRC.senseDirectionToUnownedFluxDeposit();
		
		if (direction != Direction.OMNI && myRC.getDirection() != direction){
			myRC.setDirection(direction);
			myRC.yield();
		}
		
	}
	
	public Robot[] senseEnemyRobots()  throws GameActionException{
		
		Robot[] allAirRobots = myRC.senseNearbyAirRobots();
		
		Robot[] allGroundRobots = myRC.senseNearbyGroundRobots();
		
		Robot[] robots;
		
		int count = 0;
		
		for (int k = 0; k < 2; k++){
		
			if (k == 0)
				robots = allAirRobots;
			else
				robots = allGroundRobots;
			
			for (int i = 1; i < robots.length; i++){
				
				if (myRC.canSenseObject(robots[i])){
				
					RobotInfo robotInfo = myRC.senseRobotInfo(robots[i]);
					
					if (robotInfo.team != myRC.getTeam()){
						count++;
					}
					
				}
					
			}
			
		}
		
		Robot[] enemyRobots = new Robot[count];
		
		for (int k = 0; k < 2; k++){
			
			int j = 0;
			
			if (k == 0)
				robots = allAirRobots;
			else
				robots = allGroundRobots;
			
			for (int i = 1; i < robots.length; i++){
				
				if (myRC.canSenseObject(robots[i])){
				
					RobotInfo robotInfo = myRC.senseRobotInfo(robots[i]);
					
					if (robotInfo.team != myRC.getTeam()){
						enemyRobots[count] = robots[j];
						j++;
					}
					
				}
					
			}
		}

		return enemyRobots;
		
	}
	
	
	public Robot[] senseMyRobots(RobotType robotType) throws GameActionException{
		
		Robot[] allRobots;
		
		if (robotType.isAirborne()){
			allRobots = myRC.senseNearbyAirRobots();
		} else {
			allRobots = myRC.senseNearbyGroundRobots();
		}
		
		int count = 0;
		
		for (int i = 1; i < allRobots.length; i++){
			
			if (myRC.canSenseObject(allRobots[i])){
			
				RobotInfo robotInfo = myRC.senseRobotInfo(allRobots[i]);
				
				if (robotInfo.type == robotType && robotInfo.team == myRC.getTeam()){
					count++;
				}
				
			}
				
		}
		
		Robot[] myRobots = new Robot[count];
		
		int j = 0;
		
		for (int i = 1; i < allRobots.length; i++){
			
			if (myRC.canSenseObject(allRobots[i])){
			
				RobotInfo robotInfo = myRC.senseRobotInfo(allRobots[i]);
				
				if (robotInfo.type == robotType && robotInfo.team == myRC.getTeam()){
					myRobots[j] = allRobots[i];
					j++;
				}
				
			}
				
		}
		
		return myRobots;
		
		
	}
	
	public MapLocation[] senseMyRobotsLocations(RobotType robotType) throws Exception{
		
		Robot[] robots = senseMyRobots(robotType);
		MapLocation[] robotsLocations = new MapLocation[robots.length];
		
		for (int i = 0; i < robots.length; i++) {
			RobotInfo robotInfo = myRC.senseRobotInfo(robots[i]);
			robotsLocations[i] = robotInfo.location;
		}
		
		return robotsLocations;
		
	}
	
	public MapLocation findTheNearestArchon(Robot robot) throws Exception{
		
		RobotInfo robotInfo = myRC.senseRobotInfo(robot);
		
		MapLocation[] archons = myRC.senseAlliedArchons();
		
		boolean first = true;
		
		double min = 0;
		MapLocation minLocation = myRC.getLocation();
			
		for (int i = 0; i < archons.length; i++) {
			
			double distance = robotInfo.location.distanceSquaredTo(archons[i]);
					
			if (first || min > distance) {
				min = distance;
				minLocation = archons[i]; 
			}
				
	
		}
		
		return minLocation;
	
	}
	
	public void tryToMoveInDirection(Direction direction) throws Exception{
		
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
	
	public int chooseRobotToTransferEnergon(Robot[] robots) throws Exception{
		
		// id of the robot with minimal energon level
		int idRobot = robots.length;
			
		double minEnergon = 0;
			
		for (int i = 0; i < robots.length; i++) {
				
			if (myRC.canSenseObject(robots[i])){
					
				RobotInfo robotInfo = myRC.senseRobotInfo(robots[i]);

				if (myRC.getLocation().isAdjacentTo(robotInfo.location)){
					
					double energon = robotInfo.eventualEnergon;
						
					if (energon < minEnergon || idRobot == robots.length){
						minEnergon = energon;
						idRobot = i;
					}
			
				}
					
			}
			
		}
		
		return idRobot;
		
	}
	
	boolean msgIsToMe(Message message){
		//return (message.ints[1] == myRC.getRobot().getID());
		
		//TODO: it depends on message type
		return true;
	}
	
	//converts direction to int
	static public int toInt(Direction direction){
		
		int count = 0;
		
		while (direction != Direction.EAST){
			direction = direction.rotateRight();
			count = (count + 1) % 8;
		}
		
		return count;
		
	}
	
	//converts int to direction
	static public Direction toDirection(int count){
		
		Direction direction = Direction.EAST;
		int myCount = 0;
		
		while (myCount != count){
			direction = direction.rotateRight();
			myCount = (myCount + 1) % 8;
		}
		
		return direction;
		
	}

	
		
}
