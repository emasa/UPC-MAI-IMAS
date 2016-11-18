/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import cat.urv.imas.map.Cell;
import cat.urv.imas.onthology.GarbageType;

/**
 *
 * @author Dario
 */
public class HarvesterAgent extends ImasAgent{

    private Cell position;
    private GarbageType[] garbageTypes;
    
    /**
     * Builds the coordinator agent.
     */
    public HarvesterAgent() {
        super(AgentType.HARVESTER);
    }
    
    @Override
    protected void setup() {
        log("Creatad new Harvester");
        Object[] args = getArguments();
        if (args != null && args.length > 1) {
            position = (Cell) args[0];
            garbageTypes = (GarbageType[]) args[1]; 
            
            String typesStr = "";
            for (GarbageType garbageType : garbageTypes) {
                typesStr += " " + garbageType.getShortString();
            }
            log("At (" + position.getRow() + " , " + position.getCol() + ") accepting types:" + typesStr);
        }
        else {
            // Make the agent terminate immediately
            doDelete();
        }

    }

}
