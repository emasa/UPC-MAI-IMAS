/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.onthology;

import jade.core.AID;

/**
 *
 * @author Dario
 */
public class AgentFeature {
    
    private String agentName;
    private String agentType;
    private int agentCapacity;
    
    public void setProperty(String agentName,String agentType,int agentCapacity){
        this.agentName = agentName;
        this.agentType = agentType;
        this.agentCapacity = agentCapacity;
    }
    
    public String getName(){
        return this.agentName;
    }
    public String getType(){
        return this.agentType;
    }
    public int getCapacity(){
        return this.agentCapacity;
    }
    
}
