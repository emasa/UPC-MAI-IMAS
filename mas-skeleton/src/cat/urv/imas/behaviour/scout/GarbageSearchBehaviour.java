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
import java.util.List;

/**
 *
 * @author Emanuel
 */
public class GarbageSearchBehaviour extends OneShotBehaviour {
    
    @Override
    public void action() {
        ScoutAgent scout = (ScoutAgent) this.getAgent();
        Cell[] buildingsWithNewGarbage = detectBuildingsWithNewGarbage(scout);
        
    }

    private Cell[] detectBuildingsWithNewGarbage(ScoutAgent scout) {
        //      find all surrounding cells to (row,col) that are
        //      buildings and have recent (previously undetected) garbage on it.
        //      Use: BuildingCell.detectGarbage() to do so.        
        ArrayList<Cell> buildingsWithGarbage = new ArrayList<>();

        for (Cell cell : scout.getSurroundingCells()) {
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
        return buildingsWithGarbage.toArray(new Cell[buildingsWithGarbage.size()]);
    }

    private void nextMove(ScoutAgent scout) {
        StreetCell currentPos = scout.getCurrentPosition();

        int previousDir = scout.getCurrentDirection();        
        StreetCell[] possibleDirs = this.validDirections(scout);        

        for (int dir = 0 ; dir < 4 ; ++dir) {
            int candidateDir = (previousDir + dir) % 4;
            StreetCell candidatePos = possibleDirs[candidateDir];
            if (candidatePos != null && !candidatePos.isThereAnAgent()) {
                InfoAgent scoutInfo = currentPos.getAgent();
                try {
                    currentPos.removeAgent(scoutInfo);
                    candidatePos.addAgent(scoutInfo);                                    
                } catch (Exception e) {
                    // TODO: review
                }
            }            
        }
        
        StreetCell[] directions = validDirections(scout);
        
        
    }

    private StreetCell[] validDirections(ScoutAgent scout) {
        // direction is null if invalid movement
        StreetCell[] directions = new StreetCell[] {null, null, null, null};
        StreetCell currentPos = scout.getCurrentPosition();        
        for (Cell cell : scout.getSurroundingCells()) {
            // filter surrounding cells containing a street
            if (cell.getCellType() == CellType.STREET) {
                StreetCell nextPos = (StreetCell) cell;                
                directions[getDirection(currentPos, nextPos)] = nextPos;
            }
        }
        
        return directions;
    }
    
    private int getDirection(Cell origin, Cell target) {

        int dy = origin.getRow() - target.getRow();
        int dx = origin.getCol() - target.getCol();

        // map cell to directions
        if (dx == 0 && dy == 1)  return ScoutAgent.NORTH;;
        if (dx == -1 && dy == 0) return ScoutAgent.EAST;;
        if (dx == 0 && dy == -1) return ScoutAgent.SOUTH;;                
        if (dx == 1 && dy == 0)  return ScoutAgent.WEST;
        if (dx == 0 && dy == 0)  return ScoutAgent.CENTER;
        
        return ScoutAgent.INVALID;
    }
}
