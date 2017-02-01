/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.onthology;

import java.util.ArrayList;

/**
 *
 * @author Dario
 */
public class CoordinatePath {
   
    private ArrayList<Coordinate> c;
    private Boolean d;
    private String agentName;
    
    //d == 0 means direction from origin position Harvester to destination position Garbage
    //d == 1 means direction from origin position Garbage to destinatio position Recycling Center
    
    //ArrayList is a list withi pairs of coordinates from origin to destination
    
    public void setPath(String agentName,Boolean d, ArrayList<Coordinate> c){
        this.c = c;
        this.d = d;
        this.agentName = agentName;
    }
    
    public Boolean getDirection(){
        return this.d;
    }

    public ArrayList<Coordinate> getCoordinates(){
        return this.c;
    }
    public String getName(){
        return this.agentName;
    }

}
