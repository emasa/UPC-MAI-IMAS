/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.system;

import cat.urv.imas.agent.SystemAgent;
import cat.urv.imas.map.BuildingCell;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.SettableBuildingCell;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.GarbageType;
import jade.core.behaviours.OneShotBehaviour;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Emanuel
 */
public class NewGarbageBehaviour extends OneShotBehaviour {

    final GarbageType[] GARBAGE_TYPES = new GarbageType[] 
    {GarbageType.PLASTIC, GarbageType.PAPER, GarbageType.GLASS};

    private final float overrideNewGarbageProbability;
    
    public NewGarbageBehaviour(SystemAgent agent, float newGarbageProbability) {
        super(agent);
        // override new garbage probability (useful for the initial step)
        this.overrideNewGarbageProbability = newGarbageProbability;
    }
    
    public NewGarbageBehaviour(SystemAgent agent){
        super(agent);
        this.overrideNewGarbageProbability = -1;        
    }
    
    @Override
    public void action() {        
        SystemAgent agent = (SystemAgent)this.getAgent();
        GameSettings game = agent.getGame();
        
        double newGarbageProbability = this.overrideNewGarbageProbability;
        if (this.overrideNewGarbageProbability < 0.0) {
            // in game settings the probability is in range [0, 100]
            game.setNewGarbageProbability(50);
            newGarbageProbability = game.getNewGarbageProbability()/100.0;
            agent.log("Using default newGarbageProbability " + newGarbageProbability);
        }
        
        // check if new garbage will be created (with a given probability)
        if ( agent.getRandom().nextFloat() > newGarbageProbability) {
            agent.log("No garbage will be created in this step");
            return;
        }
        // shuffle the list of empty buildings and select those on the top
        List<BuildingCell> emptyBuildings = getEmptyBuildings(game);
        Collections.shuffle(emptyBuildings, agent.getRandom());
        // stop when maximum number of buildings with new garbage is reached
        // or there are not more empty buildings 
        for (int building_idx = 0 , numberOfBuildingsWithNewGargabe = 0 ; 
             // loop condition
             building_idx < emptyBuildings.size() && 
             numberOfBuildingsWithNewGargabe <= game.getMaxNumberBuildingWithNewGargabe() ;
             // move to the next building
             ++building_idx , ++numberOfBuildingsWithNewGargabe) {
 
            // cast to SettableBuildingCell (only allowed to the SystemAgent)
            SettableBuildingCell building = (SettableBuildingCell) emptyBuildings.get(building_idx);            
            // decide new amount and new type of garbage randomly
            int newAmount = agent.getRandom().nextInt(game.getMaxAmountOfNewGargabe() + 1);
            GarbageType newType = GARBAGE_TYPES[agent.getRandom().nextInt(3)];
            building.setGarbage(newType, newAmount);
            
            agent.log("New garbage created at: " + building);
        }     
    }

    private List<BuildingCell> getEmptyBuildings(GameSettings game) {
        
        Cell[][] map = game.getMap();
        int rows = map.length, cols = map[0].length;

        // list empty buildings
        List<BuildingCell> emptyBuildings = new ArrayList<>();
        for (int row = 0 ; row < rows ; ++row) {
            for (int col = 0 ; col < cols ; ++col) {
                if ( map[row][col].getCellType() == CellType.BUILDING ) {
                    BuildingCell building = (BuildingCell) map[row][col];
                    if ( building.getGarbage().isEmpty() ) {
                        emptyBuildings.add(building);
                    }
                }
            }
        }
        
        return emptyBuildings;
    }
}
