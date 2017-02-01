/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.system;

import cat.urv.imas.agent.SystemAgent;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.RecyclingCenterCell;
import cat.urv.imas.map.SettableBuildingCell;
import cat.urv.imas.onthology.GameSettings;
import jade.core.behaviours.OneShotBehaviour;
import java.util.List;

/**
 *
 * @author Emanuel
 */
public class ComputeStatisticsBehaviour extends OneShotBehaviour{

    ComputeStatisticsBehaviour(SystemAgent agent) {
        super(agent);
    }

    @Override
    public void action() {        
        SystemAgent system = (SystemAgent) this.getAgent();
        GameSettings game = system.getGame();
        
        system.showStats("\nAfter " + game.getSimulationSteps() + " steps:" +
                         "\n* Points: " + computePoints(game) + 
                         "\n* Average time for discovering garbage: " + computeAvgTimeForDiscovering(game) +
                         "\n* Average time for starting to collect garbage: " + computeAvgTimeForCollecting(game) + 
                         "\n* Ratio of discovered garbage: " + computeRatioOfDiscoveredGarbage(game) +
                         "\n* Ratio of collected garbage: " + computeRatioOfCOllectedGarbage(game) +
                         "\n");
        
    }

    private Integer computePoints(GameSettings game) {
        Integer points = 0;
        
        Cell[][] map = game.getMap();
        int rows = map.length, cols = map[0].length;

        for (int row = 0 ; row < rows ; ++row) {
            for (int col = 0 ; col < cols ; ++col) {
                if ( map[row][col].getCellType() == CellType.RECYCLING_CENTER ) {
                    RecyclingCenterCell center = (RecyclingCenterCell) map[row][col];
                    for (Integer pointsByTypeOfGarbage : center.getPoints()) {
                        points += pointsByTypeOfGarbage;
                    }
                }
            }
        }
        
        return points;
    }
    
    private Double computeAvgTimeForDiscovering(GameSettings game) {
        Integer time = 0;
        Integer discoveredBuildings = 0;        
        
        Cell[][] map = game.getMap();
        int rows = map.length, cols = map[0].length;

        for (int row = 0 ; row < rows ; ++row) {
            for (int col = 0 ; col < cols ; ++col) {
                if ( map[row][col].getCellType() == CellType.BUILDING ) {
                    SettableBuildingCell building = (SettableBuildingCell) map[row][col];
                    
                    List<Integer> creationStepHistory = building.getCreationStepHistory();
                    List<Integer> discoveryStepHistory = building.getDiscoveryStepHistory();
                    
                    // podria haber basura que no fue descubierta                    
                    for (int idx = 0 ; idx < discoveryStepHistory.size() ; ++idx) {
                        int creationStep = creationStepHistory.get(idx);
                        int discoveryStep = discoveryStepHistory.get(idx);
                        time += (discoveryStep - creationStep);
                        ++discoveredBuildings;
                    }
                }
            }
        }
        
        if (discoveredBuildings > 0)
            return ((new Double(time)) / (new Double(discoveredBuildings)));
        else
            return -1.0;
    }
    
    private Double computeAvgTimeForCollecting(GameSettings game) {
        Integer time = 0;
        Integer collectionStartedBuildings = 0;        
        
        Cell[][] map = game.getMap();
        int rows = map.length, cols = map[0].length;

        for (int row = 0 ; row < rows ; ++row) {
            for (int col = 0 ; col < cols ; ++col) {
                if ( map[row][col].getCellType() == CellType.BUILDING ) {
                    SettableBuildingCell building = (SettableBuildingCell) map[row][col];

                    List<Integer> discoveryStepHistory = building.getDiscoveryStepHistory();                    
                    List<Integer> startCollectionStepHistory = building.getStartCollectionStepHistory();
                    
                    // podria haber basura que no se empezo a recoger 
                    for (int idx = 0 ; idx < startCollectionStepHistory.size() ; ++idx) {
                        int discoveryStep = discoveryStepHistory.get(idx);
                        int startCollectionStep = startCollectionStepHistory.get(idx);
                        time += (startCollectionStep - discoveryStep);
                        ++collectionStartedBuildings;
                    }
                }
            }
        }
        
        if (collectionStartedBuildings > 0)
            return ((new Double(time)) / (new Double(collectionStartedBuildings)));
        else
            return -1.0;
    }

    private Double computeRatioOfDiscoveredGarbage(GameSettings game) {
        Integer totalGarbage = 0;
        Integer discoveredGarbage = 0;        
        
        Cell[][] map = game.getMap();
        int rows = map.length, cols = map[0].length;

        for (int row = 0 ; row < rows ; ++row) {
            for (int col = 0 ; col < cols ; ++col) {
                if ( map[row][col].getCellType() == CellType.BUILDING ) {
                    SettableBuildingCell building = (SettableBuildingCell) map[row][col];
                    
                    List<Integer> collectedHistory = building.getCollectedGarbageHistory();
                    
                    // collected (created, discovered and collected)
                    for (int idx = 0 ; idx < collectedHistory.size() ; ++idx) {
                        totalGarbage += collectedHistory.get(idx);
                        discoveredGarbage += collectedHistory.get(idx);
                    }
                    
                    // created but not completely collected
                    if (collectedHistory.size() < building.getCreationStepHistory().size()) {
                        totalGarbage += building.getNotCollectedYetGarbage() + building.getCollectedGarbage();                        
                    }
                    
                    // discovered but not completely collected
                    if (collectedHistory.size() < building.getDiscoveryStepHistory().size()) {
                        discoveredGarbage += building.getNotCollectedYetGarbage() + building.getCollectedGarbage();
                    }
                }
            }
        }
        
        if (totalGarbage > 0)
            return (new Double(discoveredGarbage)) / (new Double(totalGarbage));
        else
            return -1.0;
    }

    private Double computeRatioOfCOllectedGarbage(GameSettings game) {
        Integer totalGarbage = 0;
        Integer collectedGarbage = 0;        
        
        Cell[][] map = game.getMap();
        int rows = map.length, cols = map[0].length;

        for (int row = 0 ; row < rows ; ++row) {
            for (int col = 0 ; col < cols ; ++col) {
                if ( map[row][col].getCellType() == CellType.BUILDING ) {
                    SettableBuildingCell building = (SettableBuildingCell) map[row][col];
                    
                    List<Integer> collectedHistory = building.getCollectedGarbageHistory();
                    
                    // collected (created, discovered and collected)
                    for (int idx = 0 ; idx < collectedHistory.size() ; ++idx) {
                        totalGarbage += collectedHistory.get(idx);
                        collectedGarbage += collectedHistory.get(idx);
                    }
                    
                    // created but not completely collected
                    if (collectedHistory.size() < building.getCreationStepHistory().size()) {
                        totalGarbage += building.getGarbage().size() + building.getCollectedGarbage();
                    }

                    
                    // discovered but not completely collected
                    if (collectedHistory.size() < building.getDiscoveryStepHistory().size()) {
                        collectedGarbage += building.getCollectedGarbage();
                    }
                }
            }
        }
        
        if (totalGarbage > 0)
            return (new Double(collectedGarbage)) / (new Double(totalGarbage));
        else
            return -1.0;
    }
    
}
