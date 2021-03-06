package grupa;

import battlecode.common.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays.*;



//import static battlecode.common.GameConstants.*;

public class MessageTranslator{
    /*rezygnujemy z tego pakowania wielu wiadomosci do jendej*/
    
	//message.ints[0] = type
    /*message.ints[1] = target (id robota, jesli do konkretnego)
     *                  0, wiadomość do wszsytkich
                        -n, do robotów o typie n
     *
     * message.ints[2] = numer tury, w której była wysłana wiadomość 
     * message.ints[3] = numer kierunku, jesli bedzie potrzebny
     */
     
    
    static public final int INT_PER_MSG = 4;
    static public final int LOC_PER_MSG = 2;
    /*

     Numery typów wiadomości

     */
	static public final int MOVE_IN_DIRECTION = 0;
	
	static public final int MOVE_TO_FLUX_DEPOSIT = 1;

	static public final int BLOCKS = 5;
    static public final int ENEMY_NEAR = 6;
    static public final int ATTACK_FLUX = 7;


    static public final int ATTACK_AIR = 100;
    static public final int ATTACK_GROUND = 101;
    static public final int HOLD_FIRE = 102;
    
}
