/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package grupa;

/**
 *
 * @author liorin
 */
public enum ScoutStatus {
    START, KILL_ARCHON, FIND_ARCHONS, PATROL, RETURN_TO_FLUX
    /*Wyjasnienie przeznaczenia tych statusów:
     START : stan początkowy
     KILL_ARCHON : atak na wrogiego archona
     FIND_ARCHONS : szukanie wrogich archonow
     PATROL : patrolowanie oklicy (w domyśle okolicy fluxa)
     RETURN_TO_FLUX : powrót do fluxa
     */
}
