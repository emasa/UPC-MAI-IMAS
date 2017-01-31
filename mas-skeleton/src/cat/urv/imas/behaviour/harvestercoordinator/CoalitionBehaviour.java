/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.harvestercoordinator;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.HarvesterCoordinatorAgent;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.SettableBuildingCell;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.GarbageType;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.Property;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Dario
 */
public class CoalitionBehaviour extends CyclicBehaviour {

        public CoalitionBehaviour(HarvesterCoordinatorAgent agent) {
            super(agent);
        }
        
        public double distance (int x1,int y1,int x2,int y2){
            return Math.abs(x1-x2) + Math.abs(y1-y2);
        }
        
        
        @Override
        public void action() {
            HarvesterCoordinatorAgent agent = (HarvesterCoordinatorAgent)this.getAgent();
            
            int CoalitionNumber = 1;
            
            
            //ADD MANUALLY GARBAGE TO TEST COALITION
            SettableBuildingCell g1 = new SettableBuildingCell(3,3);
            g1.setGarbage(GarbageType.PAPER, 10);
            SettableBuildingCell g2 = new SettableBuildingCell(16,3);
            g2.setGarbage(GarbageType.GLASS, 20);
            SettableBuildingCell g3 = new SettableBuildingCell(3,7);
            g3.setGarbage(GarbageType.PLASTIC, 20);
            agent.addSettableBuildingCellList(g1);
            agent.addSettableBuildingCellList(g2);
            agent.addSettableBuildingCellList(g3);
            
            //agent.game = InitialGameSettings.load("game.settings");
            
            if(!(agent.getGame() == null)){
                
                //int HarvesterCapacity = agent.game.getHarvestersCapacity();
                int HarvesterCapacity = agent.getGame().getHarvestersCapacity();
            
                
                //Create Permutations list
                ArrayList<String> permutations = new ArrayList<>();
                //while (agent.SettableBuildingCellList.size()>0) {
                while (agent.getSettableBuildingCellList().size()>0) {

                    
                    //Get garbage type and quantity
                    SettableBuildingCell garbage = agent.getSettableBuildingCellList().get(0);
                    Map<GarbageType, Integer> g = garbage.detectGarbage();
                    Iterator it = g.entrySet().iterator();
                    GarbageType garbageType = null;
                    int garbageQty = 0;
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        garbageType = (GarbageType) pair.getKey();
                        garbageQty = (int) pair.getValue();
                    }
                    //System.out.println("Garbage Type: " + garbageType.getShortString() + ", qty " + String.valueOf(garbageQty));

                    //Create a List of index of available agents (not in coalition)
                    ArrayList<Integer> AvailableAgent = new ArrayList<>();
                    
                    for (int s = 0; s < agent.getAgentStatus().size(); s++) {
                        String agentGarbageTypes = "";
                        Iterator itr = agent.getServiceDescriptionList().get(s).getAllProperties();
                        
                        while(itr.hasNext()) {
                            Property element = (Property) itr.next();
                            agentGarbageTypes = (String) element.getValue();
                            
                        }
                            
                        if (agent.getAgentStatus().get(s) == 0 && agentGarbageTypes.contains(garbageType.getShortString())) {
                            AvailableAgent.add(s);
                            System.out.println(String.valueOf(s));
                        }
                        
                    }

                    
                    
                    
                    if(!AvailableAgent.isEmpty()){
                        
                        //Coalition of 1
                        for (int s = 0; s < AvailableAgent.size(); s++) {
                            permutations.add(String.valueOf(AvailableAgent.get(s)));
                        }
                        //Coalition of 2
                        if (garbageQty > HarvesterCapacity & AvailableAgent.size() > 1) {
                            for (int s = 0; s < AvailableAgent.size() - 1; s++) {
                                for (int h = s + 1; h < AvailableAgent.size(); h++) {
                                    permutations.add(String.valueOf(AvailableAgent.get(s)) + " " + String.valueOf(AvailableAgent.get(h)));
                                }
                            }
                        }
                        //Coalition of 3
                        if (garbageQty > 2 * HarvesterCapacity & AvailableAgent.size() > 2) {
                            for (int s = 0; s < AvailableAgent.size() - 2; s++) {
                                for (int h = s + 1; h < AvailableAgent.size() - 1; h++) {
                                    for (int r = h + 1; r < AvailableAgent.size(); r++) {
                                        permutations.add(String.valueOf(AvailableAgent.get(s)) + " " + String.valueOf(AvailableAgent.get(h)) + " " + String.valueOf(AvailableAgent.get(r)));
                                    }
                                }
                            }
                        }
                        
                        
                        //Get garbage current position
                        int gX = garbage.getCol();
                        int gY = garbage.getRow();

                        //Define index to access recycling center prices
                        int j = 0;
                        if (garbageType.getShortString().equals(GarbageType.PLASTIC.getShortString())) {
                            j = 0;
                        }
                        if (garbageType.getShortString().equals(GarbageType.GLASS.getShortString())) {
                            j = 1;
                        }
                        if (garbageType.getShortString().equals(GarbageType.PAPER.getShortString())) {
                            j = 2;
                        }

                        //Now for each element in permutations calculate Coalition Cost
                        double BestCoalitionCost = Double.NEGATIVE_INFINITY;
                        int BestRecyclingCenter = 10;
                        
                        String winnerPermutation = null;
                        for (String p : permutations) {
                            String[] ais = p.split("\\s+");

                            //For each recycling center
                            double CoalitionCost = 0;
                            int i = 0;
                            for (Cell recyclingCenter : agent.getRecyclingCenter()) {

                                int rcX = recyclingCenter.getRow();
                                int rcY = recyclingCenter.getCol();

                                int price = agent.getGame().getRecyclingCenterPrices()[i][j];
                                //For each agent
                                for (String ai : ais) {
                                    //Get agent current position
                                    String agentName = agent.getServiceDescriptionList().get(Integer.parseInt(ai)).getName();
                                    System.out.println("agentName ::: "+agentName + " for coalition calculation.");
                                
                                    int aX = 0;
                                    int aY = 0;
                                    
                                    List<Cell> harvestersCells = (ArrayList<Cell>) agent.getGame().getAgentList().get(AgentType.HARVESTER);
                                    for (int ii = 0; ii < harvestersCells.size(); ii++) {
                                        StreetCell harvesterCell = (StreetCell) harvestersCells.get(ii);
                                        AID harvester = harvesterCell.getAgent().getAID();
                                        System.out.println(harvester);
                                        if(harvester.getLocalName().equals(agentName)){
                                            aX = harvesterCell.getRow();
                                            aY = harvesterCell.getCol();
                                        }
                                        
                                    }                                  
                                    
                                    
                                    //CoalitonCost update 
                                    CoalitionCost -= distance(aX, aY, gX, gY);
                                }
                        
                                String[] aI = p.split("\\s+");
                                int harvestersQty = aI.length;
                                if(garbageQty>harvestersQty*HarvesterCapacity){
                                    CoalitionCost -= (double) 2* Math.ceil((garbageQty-harvestersQty*HarvesterCapacity) / HarvesterCapacity) * distance(gX, gY, rcX, rcY);
                                    CoalitionCost -= (double) harvestersQty * distance(gX, gY, rcX, rcY);
                                
                                }else{
                                    CoalitionCost -= (double) harvestersQty * distance(gX, gY, rcX, rcY);
                                
                                }
                                CoalitionCost += (double) garbageQty * (double) price;
                                
                                
                                i++;
                            }

                            if (CoalitionCost > BestCoalitionCost) {
                                winnerPermutation = p;
                                BestRecyclingCenter = i;
                            }
                        }

                    
                        System.out.println("Winner Coalition: ---> " + winnerPermutation);
                        System.out.println("Winner Recycling Center: ---> " + String.valueOf(BestRecyclingCenter));

                        //Set AgentStatus with Coalition Number
                        String[] wi = winnerPermutation.split("\\s+");
                        for (String s : wi) {
                            System.out.println("Agent "+agent.getServiceDescriptionList().get(Integer.parseInt(s)).getName() + " of type "+agent.getServiceDescriptionList().get(Integer.parseInt(s)).getType());
                            agent.getAgentStatus().set(Integer.parseInt(s), CoalitionNumber);
                        }
                        
                        for (int e : agent.getAgentStatus()){
                            System.out.println("Agent Status: "+String.valueOf(e));
                        }
                                
                        //Increment CoalitionNumber
                        CoalitionNumber++;

                        agent.getSettableBuildingCellList().remove(0);
                    }
                    
                }
            }
        }
    }
    
    