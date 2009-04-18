package grupa;

//import java.util.HashSet;

import static battlecode.common.GameConstants.WORKER_MAX_HEIGHT_DELTA;

import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

import battlecode.common.*;
//import static battlecode.common.GameConstants.*;

public class Worker extends AbstractRobot implements RobotApi{
	
	WorkerStatus status = WorkerStatus.CREATING_ME;
	
	HashSet<MapLocation> locationsOfUnloadedBlocks = new HashSet<MapLocation>();
	
	MapLocation depositLocation;

/*dodalem*/
    Set<MapLocation> possibleBlockLocations = new HashSet<MapLocation>();
	
	//Direction lookingForDirection;
	
	public Worker(RobotController rc) {
		super(rc);
	}
	
	private boolean isLocationOfBuilding(MapLocation location){
		
		Direction direction = location.directionTo(depositLocation);
		
		if (direction == Direction.OMNI)
			return true;
		
		if (locationsOfUnloadedBlocks.contains(location))
			return true;
		
		direction = Direction.EAST;
		
		for(int i = 0; i < 8; i++){
			
			MapLocation nextLocation = location.subtract(direction.opposite());
			
			if (!myRC.canSenseSquare(nextLocation))
				return false;
			
			try{
				
				int height = myRC.senseHeightOfLocation(location);
				int nextHeight = myRC.senseHeightOfLocation(nextLocation);
				
				int delta = nextHeight - height;
				
				if ((delta > 0)
					&& (delta <= WORKER_MAX_HEIGHT_DELTA)
					&& isLocationOfBuilding(nextLocation)) {
					
						if (!locationsOfUnloadedBlocks.contains(location))
							locationsOfUnloadedBlocks.add(location);
						return true;
				}
				
			} catch(Exception e){
				
			}
			
			direction = direction.rotateRight();
		}
		
		return false;

	}
	
private boolean isLocationOfBuildingOrNeighbourhood(MapLocation location) throws Exception{
		
		Direction direction = location.directionTo(depositLocation);
		
		if (direction == Direction.OMNI)
			return true;
	
		//direction = Direction.EAST;
		
		//for(int i = 0; i < 8; i++){
			
			MapLocation nextLocation = location.subtract(direction.opposite());
			
			if (locationsOfUnloadedBlocks.contains(nextLocation))
				return true;
			
			if (!myRC.canSenseSquare(nextLocation))
				return false;
			
			int height = myRC.senseHeightOfLocation(location);
			int nextHeight = myRC.senseHeightOfLocation(nextLocation);
			
			int delta = nextHeight - height;
			
			if ((delta >= 0)
				&& (delta <= WORKER_MAX_HEIGHT_DELTA)
				&& isLocationOfBuilding(nextLocation))
					return true;
			
			//direction = direction.rotateRight();
		//}
		
		return false;

	}
	
	public MapLocation senseNearestBlock(){
		
		MapLocation[] allBlocksLocations = myRC.senseNearbyBlocks();
		int blocksLocationsNum = 0;
		MapLocation nearestBlock = null;
			
		for (int i = 0; i < allBlocksLocations.length; i++){
			if (!isLocationOfBuilding(allBlocksLocations[i])){
				blocksLocationsNum++;
			}
		}
		
		MapLocation[] blocksLocations = new MapLocation[blocksLocationsNum];
		int j = 0;
		
		for (int i = 1; i < allBlocksLocations.length; i++){
			if (!isLocationOfBuilding(allBlocksLocations[i])){
				blocksLocations[j] = allBlocksLocations[i];
				j++;
			}
		}
	
		//the block which can be loaded or the nearest block		
		if (blocksLocations.length > 0) {
			
			double minDistance = myRC.getLocation().distanceSquaredTo(blocksLocations[0]);
			
			for (int i = 0; i < blocksLocations.length; i++){
				
				double distance = myRC.getLocation().distanceSquaredTo(blocksLocations[i]);
				
				if (minDistance > distance) {
					minDistance = distance;
					nearestBlock = blocksLocations[i];
				}
				
			}
			
		}
		
		return nearestBlock;
		
	}
	
	 public void loadBlockFromLocation(MapLocation location) {
	       
		 try {
		 
			RobotInfo robotInfo = myRC.senseRobotInfo(myRC.getRobot());
		 
		 	if (robotInfo.numBlocksInCargo != 0)
			 	return;
		
	     	while (myRC.hasActionSet() || myRC.isMovementActive()) {
	     		myRC.yield();
	     	}
	     
	     	myRC.loadBlockFromLocation(location);
	     	myRC.yield();
	            
	     } catch (Exception e) { return;}

	 }

    private void readMessages() {
        for (Message m : myRC.getAllMessages() ) {
            if (msgIsToMe(m)) {
                switch (m.ints[0]) {
                    case MessageTranslator.BLOCKS :
                        possibleBlockLocations.addAll(Arrays.asList(m.locations) );
                        break;
                }
            }
        }
    }
	
	public void nextTurn() throws Exception{
		readMessages();
		switch(status){
			case CREATING_ME:
				
				depositLocation = findTheNearestArchon(myRC.getRobot());
				
				//lookingForDirection = myRC.getLocation().directionTo(depositLocation).opposite();
				
				status = WorkerStatus.LOOKING_FOR_BLOCK;
			
			case LOOKING_FOR_BLOCK:
				
				MapLocation blockLocation = senseNearestBlock();
				
				if (blockLocation != null
					&& myRC.canLoadBlockFromLocation(blockLocation)){
				
					loadBlockFromLocation(blockLocation);
					
					status = WorkerStatus.MODIFYING_TERRAIN;
				
				
				} else {
					
					//direction =
					Direction direction = Direction.NORTH;
					tryToMoveInDirection(direction);
					
				}
				
				
				
		}
		
	}
		
}
