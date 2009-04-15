package grupa;

import battlecode.common.*;

//import static battlecode.common.GameConstants.*;

public abstract class AbstractRobot implements RobotApi {
	
	protected final RobotController myRC;
	
	public AbstractRobot(RobotController rc) {
		myRC = rc;
        
	}
	
	boolean isOnMap (MapLocation mapLocation){
		
		/*if (!myRC.canSenseSquare(mapLocation))
			return false;*/
		TerrainTile tile = myRC.senseTerrainTile(mapLocation);
        if (tile == null) {
            return false;
        }
		return (tile.getType()
				!= TerrainTile.TerrainType.OFF_MAP);
        
	}
	
	boolean isFreeLocation(MapLocation mapLocation) throws Exception{
		
		if (!myRC.canSenseSquare(mapLocation))
			return false;
        try {
            Robot r = myRC.senseGroundRobotAtLocation(mapLocation);
            return (r == null);
        }
            catch (Exception e) {}
            return false;
        
	}
		
	public void setDirectionToUnownedFluxDeposit() {
		
		while (myRC.isMovementActive()){
			myRC.yield();
		}
		
		Direction direction = myRC.senseDirectionToUnownedFluxDeposit();
		try {
            if (direction != Direction.OMNI && direction != Direction.NONE && myRC.getDirection() != direction){
                myRC.setDirection(direction);
                myRC.yield();
            }
        } catch (Exception e) {}
		
	}


	
	public Robot[] senseEnemyRobots() {
		
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
				try {
                    if (myRC.canSenseObject(robots[i])){

                        RobotInfo robotInfo = myRC.senseRobotInfo(robots[i]);
					
                        if (robotInfo.team != myRC.getTeam()){
                            count++;
                        }
					
                    }
                } catch (Exception e) {}

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
				try {
                    if (myRC.canSenseObject(robots[i])){
                        RobotInfo robotInfo = myRC.senseRobotInfo(robots[i]);
					
                        if (robotInfo.team != myRC.getTeam()){
                            enemyRobots[count] = robots[j];
                            j++;
                        }
                    
					
                    }
				} catch (Exception e) {}
			}
		}

		return enemyRobots;
		
	}
	
	
	public Robot[] senseMyRobots(RobotType robotType) {
		/*Poprzednia wersja wywalaala exception, gdy w czasie wykonania metody
         jeden z robotow wyjechal poza zasieg*/
		Robot[] allRobots;
		
		if (robotType.isAirborne()){
			allRobots = myRC.senseNearbyAirRobots();
		} else {
			allRobots = myRC.senseNearbyGroundRobots();
		}
		
		int count = 0;
		
		for (int i = 1; i < allRobots.length; i++){
			try{
                if (myRC.canSenseObject(allRobots[i])){
                
                    RobotInfo robotInfo = myRC.senseRobotInfo(allRobots[i]);
				
                    if (robotInfo.type == robotType && robotInfo.team == myRC.getTeam()){
                        count++;
                    }
                
                }
			} catch (Exception e ) { }	
		}
		
		Robot[] myRobots = new Robot[count];
		
		int j = 0;
		
		for (int i = 1; i < allRobots.length; i++){
			try {
                if (myRC.canSenseObject(allRobots[i])){
                
                    RobotInfo robotInfo = myRC.senseRobotInfo(allRobots[i]);
				
                    if (robotInfo.type == robotType && robotInfo.team == myRC.getTeam()){
                        myRobots[j] = allRobots[i];
                        j++;
                    }
                } 
				
			} catch (Exception e){}
				
		}
        /*Niwe wiadomo, ile naprawdę pól jest wypełnionych 
         niektóre roboty mogły uciec z pola widzenia naszego robota
         Dlatego tablicę wynikową trzeba uzupełnić*/
        for (int i = j; i < allRobots.length; i++) {
            myRobots[j] = null;
        }
		
		return myRobots;
		
		
	}
	
	public MapLocation[] senseMyRobotsLocations(RobotType robotType) {
		
		Robot[] robots = senseMyRobots(robotType);
		MapLocation[] robotsLocations = new MapLocation[robots.length];
		
		for (int i = 0; i < robots.length; i++) {
            try {
                RobotInfo robotInfo = myRC.senseRobotInfo(robots[i]);
                robotsLocations[i] = robotInfo.location;
            } catch (Exception e) {
                robotsLocations[i] = null;
            }
		}
		
		return robotsLocations;
		
	}
	
	public MapLocation findTheNearestArchon(Robot robot) {
		RobotInfo robotInfo = null;
        try {
            robotInfo = myRC.senseRobotInfo(robot);
            /*Jeśli tu będzie wyjątek, to znaczy, że badany robot jest poza zasięgiem
             wogóle, nie wiem, czy nie jest sensowniej zrobić tą metode od MapLocation, a nie od Robota*/
            
        } catch (Exception e) {return null; }
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
	
    protected boolean move(Direction dir) {
        
        try {
            while (myRC.hasActionSet() || myRC.isMovementActive()) {
                myRC.yield();
            }
            if (myRC.getDirection() != dir) {
                myRC.setDirection(dir);
                myRC.yield();
            }
            myRC.moveForward();
            myRC.yield();
        } catch (Exception e) { return false;}

        return true;
    }

	public void tryToMoveInDirection(Direction direction) {
		
	
        if (!move(direction) ) {
            /*Nie jestem pewien, jaki byl pierwotny zamysl, ale na razie niech bedzie tak*/
            try {
                if (myRC.hasActionSet()) {
                    myRC.yield();
                }
                myRC.setDirection(myRC.getDirection().rotateRight());
            } catch (Exception e) {}
        }
		/* To nie wygląda na poprawny kod
         * przecież, jeśli zmienimy kierunek na direction, to już się nie ruszamy
		if (myRC.canMove(direction) && direction != myRC.getDirection()) {
			myRC.setDirection(direction);
		} else {
			if(myRC.canMove(myRC.getDirection())){
				myRC.moveForward();
			} else {
				myRC.setDirection(myRC.getDirection().rotateRight());
			}
					
		}
         */
	
		myRC.yield();
		
	}
	
	public int chooseRobotToTransferEnergon(Robot[] robots) {
		
		// id of the robot with minimal energon level
        /*lepiej -1 dać na początek, łatwiej błąd poznać*/
		int idRobot = robots.length;
			
		double minEnergon = 0;
			
		for (int i = 0; i < robots.length; i++) {
			try {
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
            } catch (Exception e) {}
			
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


    /*Tutaj proponuje umieścić metody odpowiedzialne za ocenianie możliwości robota
     np. liczenie ile energonu jest potrzebne żeby dojść z jednej lokacji do drugiej, itp.*/
    protected double travelCost(MapLocation start, MapLocation end) {
        /*@return Zwraca minimalną ilość energonu, który jest potrzebny
         aby przejść najkrótszą drogą z start do end
         Uwaga: ta metoda nie uwzględnia różnic w wysokości terenu!*/
        RobotType type = myRC.getRobotType();
        /*najpierw obliczamy jaki jest całkowity koszt ruchu o jedno pole po skosie i prosto
          to  +1 jest konieczne, bo trzeba wliczyć sam ruch */
        double diagonalMoveCost = type.energonUpkeep() * (type.moveDelayDiagonal() +1);
        double orthogonalMoveCost = type.energonUpkeep() * (type.moveDelayOrthogonal() + 1);

        int distX = Math.abs(start.getX() - end.getX());
        int distY = Math.abs(start.getY() - end.getY());
        /*tyle będzie ruchów po skosie  - tyle, ile potrzeba by zniwelować różnicę na współrzędnej x lub y,
         zależnie od tego gdzie ta różnica jest mniejsza*/
        int diagonalMovesNeeded = Math.min(distX, distY);
        /*potem trzeba będzie wykonać resztę ruchów w kierunkach głównych*/
        int orthogonalMovesNeeded = Math.max(distX, distY) - diagonalMovesNeeded;
        /**/
        return (diagonalMovesNeeded*diagonalMoveCost + orthogonalMoveCost*orthogonalMovesNeeded);
    }

	
		
}
