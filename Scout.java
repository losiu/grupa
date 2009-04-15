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
    ScoutStatus state = ScoutStatus.START;
    Direction patrolDirection;
    Rotation patrolRotation = Rotation.RIGHT;
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
        /*Czytamy wszystkie wiadomości, które przyszły
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
                 1. sprawdz, czy sa wrogowie w poblizu (nie-workery, bo workery są niegroźne)
                 2. jeśli tak, wróć do fluxa
                 3. jeśli nie, sprawdź czy masz dość energonu
                 4. jeśli nie, wróć do fluxa
                 5. jeśli tak, kontynuuj patrol*/

                if (senseEnemyRobots().length != 0) {
                }
                
                if (!move(patrolDirection)) {
                    changeRotation();
                }

                break;
            case FIND_ARCHONS:
                break;
            case KILL_ARCHON:
                break;
        }
    }

}
