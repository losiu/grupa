package grupa;

import battlecode.common.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays.*;



//import static battlecode.common.GameConstants.*;

public class MessageTranslator{
    
	//message.ints[0] = type
    /*message.ints[1] = target (id robota, jesli do konkretnego)
     *                  0, wiadomość do wszsytkich
                        -n, do robotów o typie n
     *
     * message.ints[2] = numer tury, w której była wysłana wiadomość 
     * message.ints[3] = numer kierunku, jesli bedzie potrzebny
     */
     
    
    
    
	static public final int MOVE_IN_DIRECTION = 0;
	
	static public final int MOVE_TO_FLUX_DEPOSIT = 1;
	// message.locations[0] = archon's location
    // message.locations[1] = lokacja wroga || cokolwiek innego, jesli bedze potrzebne
    
    /*Wydaje mi się, że stringów nie potrzebujemy*/
    
    /*Zakładam, że każdy robot będzie sobie tworzył własnego translatora, 
     który bedzie mu tłumaczył wiadomości */
    
   
    
   
    
    
}
