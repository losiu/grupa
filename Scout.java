/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package grupa;

import battlecode.common.*;

/**
 *
 * @author liorin
 */
public class Scout extends AbstractRobot implements RobotApi{
    private enum Rotation {LEFT, RIGHT};
    /*Rotation odpowiada za kierunek, w którym Scout skręca podczas patrolu*/


    private static final Double MIN_ENERGON_RESERVE = 1.0;

    ScoutStatus state = ScoutStatus.START;

    /*Zmienne potrzebne do patrolowaia*/
    Rotation patrolRotation = Rotation.RIGHT;
    Direction patrolDirection;
    MapLocation fluxLocation = null;
    Boolean patrolEnemiesNearby = null;

    /*Na chwilę obecną mam takie pomysły zastosowania Scouta :
     1) patrolowanie okolicy fluxa
     2) polowanie na archony przeciwnika*/
    public Scout(RobotController myRC) {
        super(myRC);
    }


    private void changeRotation() {
        switch (patrolRotation) {
            case RIGHT:
                patrolRotation = Rotation.LEFT;
                break;
            case LEFT:
                patrolRotation = Rotation.RIGHT;
                break;
        }
    }

    private Direction newPatrolDirection(Direction dir) {
        switch (patrolRotation) {
            case RIGHT:
                return dir.rotateRight().rotateRight();
            case LEFT:
            default:
                return dir.rotateLeft().rotateLeft();
        }
    }


    private void readMessages() {

        /* TODO : treść metody
         Czytamy wszystkie wiadomości, które przyszły
         Powinniśmy czytać wszystkie, bo czytanie po jednej może spowodować duże opóźnienia i
         trudności w synchronizacji działań robotów. */

        for (Message m : myRC.getAllMessages()) {

        }
    }

    public void nextTurn() {
        readMessages();
        switch (state){
            case START:
                /*po prostu czekamy na rozkazy*/
                myRC.yield();
                break;
            case PATROL:
                /*krótki opis:
                 1. sprawdz, czy sa wrogowie w poblizu
                 2. jeśli tak, wróć do fluxa
                 3. jeśli nie, sprawdź czy masz dość energonu
                 4. jeśli nie, wróć do fluxa
                 5. jeśli tak, kontynuuj patrol*/

                if (senseEnemyRobots().length > 0) {
                    patrolEnemiesNearby = true;
                    state = ScoutStatus.RETURN_TO_FLUX;
                    break;
                }

                if (myRC.getEnergonLevel() - travelCost(myRC.getLocation(), fluxLocation)
                        < 4*singleDiagonalMoveCost() ) {
                    /*jezeli mamy malo energii, wracamy*/
                    state = ScoutStatus.RETURN_TO_FLUX;
                    break;
                }


                patrolDirection = newPatrolDirection(myRC.getLocation().directionTo(fluxLocation));
                
                if (!myRC.canMove(patrolDirection)) {
                    changeRotation();
                } else {
                    move(patrolDirection);
                }

                break;
            case FIND_ARCHONS:
                break;
            case KILL_ARCHON:
                break;
        }
    }

}
