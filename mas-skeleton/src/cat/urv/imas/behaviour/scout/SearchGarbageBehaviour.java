/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.scout;

import cat.urv.imas.agent.ScoutAgent;
import cat.urv.imas.map.BuildingCell;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.InfoAgent;
import jade.core.behaviours.OneShotBehaviour;
import java.util.ArrayList;

/**
 *
 * @author Emanuel
 */
public class SearchGarbageBehaviour extends OneShotBehaviour {
    
    @Override
    public void action() {
        ScoutAgent scout = (ScoutAgent) this.getAgent();        
        // detect buildings with garbage in the current position
        ArrayList<BuildingCell> buildingsWithNewGarbage = detectBuildingsWithNewGarbage(scout);
        scout.setGarbageCells(buildingsWithNewGarbage);
        // move if possible
        nextMove(scout);
        
        if ( !buildingsWithNewGarbage.isEmpty() ) {
            scout.log("New garbage detected at: " + buildingsWithNewGarbage);
        } else {
            scout.log("No garbaged detected");
        }
    }

    private ArrayList<BuildingCell> detectBuildingsWithNewGarbage(ScoutAgent scout) {
        //      find all surrounding cells to (row,col) that are
        //      buildings and have recent (previously undetected) garbage on it.
        //      Use: BuildingCell.detectGarbage() to do so.        
        ArrayList<BuildingCell> buildingsWithGarbage = new ArrayList<>();

        for (Cell cell : scout.getAdjacentCells()) {
            // filter surrounding cells containing a building
            if (cell.getCellType() == CellType.BUILDING) {
                BuildingCell building = (BuildingCell) cell;
                // list garbage that was previously undetected (building 
                // declared as empty) but after detecting now it is found
                // TODO: no estoy seguro si esta deberia ser la condicion
                if (building.getGarbage().isEmpty()
                        && !building.detectGarbage().isEmpty()) {
                    buildingsWithGarbage.add(building);
                }
            }
        }
        // return a array with the buildings 
        return buildingsWithGarbage;
    }

    private void nextMove(ScoutAgent scout) {
        StreetCell currentPos = scout.getCurrentPosition();
        int previousDir = scout.getCurrentDirection();     
        
        StreetCell[] priorityDirs = this.getPriorityDirections(scout);
        for (int dir = 0 ; dir < 4 ; ++dir) {
            // round-robin
            int candidateDir = (previousDir + dir) % 4;
            StreetCell candidatePos = priorityDirs[candidateDir];
            if (candidatePos != null && !candidatePos.isThereAnAgent()) {
                InfoAgent scoutInfo = currentPos.getAgent();
                try {
                    currentPos.removeAgent(scoutInfo);
                    candidatePos.addAgent(scoutInfo);                                
                    scout.setCurrentDirection(candidateDir);
                } catch (Exception e) {
                    // TODO: review
                }
            }
        }
        
        // check if scout haven't moved
        if (currentPos == scout.getCurrentPosition()) {
            scout.setCurrentDirection(ScoutAgent.CENTER);
        }
    }

    private StreetCell[] getPriorityDirections(ScoutAgent scout) {
        // direction is null if invalid movement
        StreetCell[] dirs = new StreetCell[] {null, null, null, null};        
        // define cardinal directions
        for (Cell cell : scout.getAdjacentCells()) {
            // filter surrounding cells containing a street
            if (cell.getCellType() == CellType.STREET) {
                StreetCell nextPos = (StreetCell) cell;                
                dirs[getDirection(scout.getCurrentPosition(), nextPos)] = nextPos;
            }
        }
        
        StreetCell[] priorityDirs = new StreetCell[] {null, null, null, null};        
        // priority: follow same direction, go right, go left, reverse direction
        switch(scout.getCurrentDirection()) {
            case ScoutAgent.NORTH:
            case ScoutAgent.CENTER:
                priorityDirs[0] = dirs[ScoutAgent.NORTH]; priorityDirs[3] = dirs[ScoutAgent.SOUTH];
                priorityDirs[1] = dirs[ScoutAgent.EAST]; priorityDirs[2] = dirs[ScoutAgent.WEST];                
                break;
            case ScoutAgent.SOUTH:
                priorityDirs[0] = dirs[ScoutAgent.SOUTH]; priorityDirs[3] = dirs[ScoutAgent.NORTH];
                priorityDirs[1] = dirs[ScoutAgent.WEST]; priorityDirs[2] = dirs[ScoutAgent.EAST];
                break;
            case ScoutAgent.EAST:
                priorityDirs[0] = dirs[ScoutAgent.EAST]; priorityDirs[3] = dirs[ScoutAgent.WEST];
                priorityDirs[1] = dirs[ScoutAgent.SOUTH]; priorityDirs[2] = dirs[ScoutAgent.NORTH];
                break;
            case ScoutAgent.WEST:
                priorityDirs[0] = dirs[ScoutAgent.WEST]; priorityDirs[3] = dirs[ScoutAgent.EAST];
                priorityDirs[1] = dirs[ScoutAgent.NORTH]; priorityDirs[2] = dirs[ScoutAgent.SOUTH];                
        }
        
        return priorityDirs;
    }
    
    private int getDirection(Cell origin, Cell target) {

        int dy = origin.getRow() - target.getRow();
        int dx = origin.getCol() - target.getCol();

        // map cell to directions
        if (dx == 0 && dy == 1)  return ScoutAgent.NORTH;
        if (dx == -1 && dy == 0) return ScoutAgent.EAST;
        if (dx == 0 && dy == -1) return ScoutAgent.SOUTH;                
        if (dx == 1 && dy == 0)  return ScoutAgent.WEST;
        if (dx == 0 && dy == 0)  return ScoutAgent.CENTER;
        
        return ScoutAgent.INVALID;
    }
}
