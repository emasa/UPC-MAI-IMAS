/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import cat.urv.imas.map.Cell;

/**
 *
 * @author Dario
 */
public class ScoutAgent extends ImasAgent{
     /**
     * Builds the coordinator agent.
     */
    public ScoutAgent() {
        super(AgentType.SCOUT);
    }
    private Cell position;
  
    
    
    @Override
    protected void setup() {
        log("Creatad new Scout");
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            position = (Cell) args[0];
            log("At (" + position.getRow() + " , " + position.getCol() + ")");
        }
        else {
            // Make the agent terminate immediately
            doDelete();
        }

        
    }

}
