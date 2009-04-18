/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package grupa;

import static battlecode.common.GameConstants.*;
import battlecode.common.*;

/**
 *
 * @author liorin
 */
public class Cannon extends AbstractRobot implements RobotApi {
    private CannonStatus status;
    private MapLocation attackTarget = null;
    private Boolean attackAir = false;
    public Cannon(RobotController myRC) {
        super(myRC);
        status = CannonStatus.YIELD;
    }
    private void readMessages() {
        for (Message m : myRC.getAllMessages() ) {
            if (msgIsToMe(m)) {
                switch (m.ints[0]) {
                    case MessageTranslator.ATTACK_AIR:
                        status = CannonStatus.FIRE;
                        attackTarget = m.locations[0];
                        attackAir = true;
                        break;
                    case MessageTranslator.ATTACK_GROUND:
                        status = CannonStatus.FIRE;
                        attackTarget = m.locations[0];
                        attackAir = false;
                        break;
                    case MessageTranslator.HOLD_FIRE:
                        status = CannonStatus.YIELD;
                        break;
                }
            }
        }
    }


    public void nextTurn() {
        readMessages();

        switch (status) {
            case FIRE:
                if (!(myRC.isAttackActive() || myRC.isMovementActive()) ) {
                    try {
                        if (attackAir) {
                            myRC.attackAir(attackTarget);
                        } else {
                            myRC.attackGround(attackTarget);

                        }
                    } catch (Exception e) {}
                }
                break;

        }
        myRC.yield();
    }

}
