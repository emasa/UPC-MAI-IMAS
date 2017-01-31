/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinatorHarvester;

import cat.urv.imas.onthology.Coordinate;
import cat.urv.imas.onthology.InitialGameSettings;
import java.util.ArrayList;
import java.util.Random;
/**
 *
 * @author pabloreynoso
 */
public class HarvestersPathFinder {
    
    
    //Harvesters positions list of coordinates
    private final ArrayList<Coordinate> harvestersPositions;
    
    //Garbages positions list of coordinates
    private final ArrayList<Coordinate> garbagesPositions;
    
    //Harvesters optimal path positions list of coordinates
    private ArrayList<Coordinate> harvesterOptimalPathPositions;
    
    //City Map
    private final int[][] city_map;
    
    public HarvestersPathFinder(ArrayList<Coordinate> harvestersPos, ArrayList<Coordinate> garbagesPos){
        Random randomGen = new Random();
        
        //Initializing the map
        InitialGameSettings igs = new InitialGameSettings();
        this.city_map = igs.getInitialMap();
        
        //Initializing harvesters positions
        this.harvestersPositions = new ArrayList<Coordinate>();
        this.harvestersPositions.addAll(getHarvestersPositions());
        
        //Initializing garbage positions
        this.garbagesPositions = new ArrayList<Coordinate>();
        int n_garbages = 20;
        for(int i=0;i<n_garbages;i++){
            Coordinate coordG = new Coordinate();
            coordG.setXY(randomGen.nextInt(20), randomGen.nextInt(20));
            if(this.harvestersPositions.contains(coordG)){
                i = i - 1;
                continue;
            }
            this.garbagesPositions.add(i, coordG);
        }
        
        //Initializing harvester optimal path
        this.harvesterOptimalPathPositions = null;
    }
    
    private ArrayList<Coordinate> getHarvestersPositions(){
        
        ArrayList<Coordinate> harvestersPos = new ArrayList<Coordinate>();
        
        for(int i=0; i<this.city_map.length; i++){
            for(int j=0; j<this.city_map[0].length; j++){
                
                if(this.city_map[i][j] == -1){
                    
                    Coordinate coordH = new Coordinate();
                    coordH.setXY(i,j);
                    harvestersPos.add(i, coordH);
                    
                }
                
            }
        }
        return harvestersPos;
    }
    
    private void getHervestersOptimalPaths(){
       
        
        for(int i=0; i<this.harvestersPositions.size(); i++){
            for(int j=0; j<this.garbagesPositions.size(); j++){
                
                //Obtaining ith - harvester coodrinates
                int [] harvesterPos = {this.harvestersPositions.get(i).getX(),this.harvestersPositions.get(i).getY()};
                
                 //Obtaining jth - garbage coodrinates
                int [] garbagePos = {this.garbagesPositions.get(j).getX(),this.garbagesPositions.get(j).getY()};
                
                PathFinderBehaviour pfb = new PathFinderBehaviour(harvesterPos,garbagePos);
                //this.harvesterOptimalPathPositions = pfb.CalulateHarvesterOptimalpath();
                
            }
        }
        
    }
            
            
    public static void main(String[]args){
        
        //List of harvesters map positions
        ArrayList<Coordinate> harvestersPositions = null;
       
        //List of garbages map positions
        ArrayList<Coordinate> garbagesPositions = null;
        
        HarvestersPathFinder hspf = new HarvestersPathFinder(harvestersPositions,garbagesPositions);
        
        
    
    }        
}
