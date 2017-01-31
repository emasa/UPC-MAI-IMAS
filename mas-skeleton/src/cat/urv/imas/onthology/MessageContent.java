/**
 *  IMAS base code for the practical work.
 *  Copyright (C) 2014 DEIM - URV
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.onthology;

/**
 * Content messages for inter-agent communication.
 */
public class MessageContent {
    
    /**
     * Message sent from Coordinator agent to System agent to get the whole
     * city information.
     */
    public static final String GET_MAP = "Get map";
    public static final String GET_MAP_REPLY = "Get map reply";    
    /**
     * Message sent from Scout Coordinator agent to Scout agent to get the
     * BuildingCells with garbage.
     */
    public static final String GET_GARBAGE = "Get garbage";
    
    /**
     * Message sent from Scout Coordinator agent to Scout agent to get the
     * BuildingCells with garbage.
     */
    public static final String GET_SCOUT_STEPS = "Get scout steps";
    public static final String GET_SCOUT_STEPS_REPLY = "Get scout steps";
    
    public static final String GARBAGE_FOUND = "Garbage found";
    
    /**
     * Message sent from Scout Coordinator agent to Scout agent to get the
     * BuildingCells with garbage.
     */
    public static final String NEW_CELL = "New cell";
    
    public static final String NEW_STEP = "New Step";    
    public static final String STEP_FINISHED = "Step Finished";
    
}
