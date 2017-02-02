/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.harvestercoordinator;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.HarvesterCoordinatorAgent;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.SettableBuildingCell;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.GarbageType;
import cat.urv.imas.onthology.InfoAgent;
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
            
            int CoalitionNumber = agent.getCoalitionNumber();
            
            
            //ADD MANUALLY GARBAGE TO TEST COALITION
            /*
            SettableBuildingCell g1 = new SettableBuildingCell(3,3);
            g1.setGarbage(GarbageType.PAPER, 10);
            SettableBuildingCell g2 = new SettableBuildingCell(16,3);
            g2.setGarbage(GarbageType.GLASS, 20);
            SettableBuildingCell g3 = new SettableBuildingCell(3,7);
            g3.setGarbage(GarbageType.PLASTIC, 20);
            agent.addSettableBuildingCellList(g1);
            agent.addSettableBuildingCellList(g2);
            agent.addSettableBuildingCellList(g3);
            */
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
                        int gX = garbage.getRow();
                        int gY = garbage.getCol();
                        System.out.println(gX+" "+gY+"*********************************************");
                        //Get new coordinate for garbage (adjacent to building)
                        
                        ArrayList<Cell> adjacentCells = agent.getGame().getAdjacentCells(new StreetCell(gX,gY));
                        for (Cell c : adjacentCells){
                            if(c.getCellType().equals(CellType.STREET)){
                                gX = c.getRow();
                                gY = c.getCol();
                                break;
                            }
                        }
                        
                        System.out.println("\n"+garbage+"\n");
                        System.out.println("\n"+adjacentCells+"\n");
                        
                        System.out.println(gX+" "+gY+"*********************************************");
                        
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
                        
                        //Agent positions
                        int aX = 0;
                        int aY = 0;
                        int i = 0;
                        
                        int WrcX = 0;
                        int WrcY = 0;
                        int WgX = 0;
                        int WgY = 0;
                        String winnerPermutation = null;
                        for (String p : permutations) {
                            String[] ais = p.split("\\s+");

                            //For each recycling center
                            double CoalitionCost = 0;
                            i = 0;
                            System.out.println("Size of recycling Center: "+agent.getRecyclingCenter().size());
                            for (Cell recyclingCenter : agent.getRecyclingCenter()) {

                                int rcX = recyclingCenter.getRow();
                                int rcY = recyclingCenter.getCol();
                                int price = agent.getGame().getRecyclingCenterPrices()[i][j];
                        
                                //Get new coordinate por garbage (adjacent to building)
                                adjacentCells = agent.getGame().getAdjacentCells(new StreetCell(rcX, rcY));
                                for (Cell c : adjacentCells) {
                                    if (c.getCellType().equals(CellType.STREET)) {
                                        rcX = c.getRow();
                                        rcY = c.getCol();
                                        break;
                                    }
                                }
                                
                                //For each agent
                                for (String ai : ais) {
                                    //Get agent current position
                                    String agentName = agent.getServiceDescriptionList().get(Integer.parseInt(ai)).getName();
                                    //System.out.println("agentName ::: "+agentName + " for coalition calculation.");
                                
                                    //CORREGIR ANGEL
//                                    aX = 1;
//                                    aY = 18;
                                    
                                    List<Cell> harvestersCells = (ArrayList<Cell>) agent.getGame().getAgentList().get(AgentType.HARVESTER);
                                    System.out.println(harvestersCells);
                                    for (int ii = 0; ii < harvestersCells.size(); ii++) {
                                        StreetCell harvesterCell = (StreetCell) harvestersCells.get(ii);
                                        InfoAgent harvester = harvesterCell.getAgent();
                                        AID harvesterID = harvesterCell.getAgent().getAID();

                                        System.out.println(harvesterCell.getAgent().getAID());
                                        System.out.println(agentName+"*********************");
                                        System.out.println(harvesterID.getLocalName()+"*********************");

                                        if(harvesterID.getLocalName().equals(agentName)){
                                            aX = harvesterCell.getRow();
                                            aY = harvesterCell.getCol();
                                        }
                                    }                                  
                                    
                                    
                                    //CoalitonCost update 
                                    CoalitionCost -= (double) Math.round(agent.getPfb().getDistanceBtwPoints(aX, aY, gX, gY)/ais.length);
                                    //System.out.println("Distance between points: --->> "+agent.getPfb().getDistanceBtwPoints(aX, aY, gX, gY));
                                }
                        
                                
                                int harvestersQty = ais.length;
                                if(garbageQty>harvestersQty*HarvesterCapacity){
                                    CoalitionCost -= (double) 2* Math.ceil((garbageQty-harvestersQty*HarvesterCapacity) / HarvesterCapacity) * agent.getPfb().getDistanceBtwPoints(gX, gY, rcX, rcY);
                                    CoalitionCost -= (double) agent.getPfb().getDistanceBtwPoints(gX, gY, rcX, rcY);
                                
                                }else{
                                    CoalitionCost -= (double) harvestersQty * agent.getPfb().getDistanceBtwPoints(gX, gY, rcX, rcY);
                                
                                }
                                CoalitionCost += (double) garbageQty * (double) price;
                                System.out.println("Coalition Cost: --->> "+CoalitionCost);
                                
                                if (CoalitionCost > BestCoalitionCost) {
                                    winnerPermutation = p;
                                    BestRecyclingCenter = i;
                                    WrcX = rcX;
                                    WrcY = rcY;
                                    WgX = gX;
                                    WgY = gY;
                                }

                                
                                i++;

                            }
                        }

                    
                        System.out.println("Winner Coalition: ---> " + winnerPermutation);
                        System.out.println("Winner Recycling Center: ---> " + String.valueOf(BestRecyclingCenter));

                        //Set AgentStatus with Coalition Number
                        String[] wi = winnerPermutation.split("\\s+");
                        for (String s : wi) {
                            
                            int agentI = Integer.parseInt(s);
                            System.out.println("Agent "+agent.getServiceDescriptionList().get(agentI).getName() + " of type "+agent.getServiceDescriptionList().get(agentI).getType());
                            agent.getAgentStatus().set(agentI, CoalitionNumber);
                            
                            int[] origin = {aX,aY};
                            int[] destination = {WgX,WgY};
                            
                            int originNode = origin[0]*agent.getPfb().getCols() + origin[1];
                            int destinationNode = destination[0]*agent.getPfb().getCols() + destination[1];
                            List<Integer> h2g = agent.getPfb().getOptimalPath(originNode, destinationNode);
                            System.out.print("Optimal path from agent to garbage: "+h2g);
                            
                            origin[0] = WgX;
                            origin[1] = WgY;
                            destination[0] = WrcX;
                            destination[1] = WrcY;
                            originNode = origin[0]*agent.getPfb().getCols() + origin[1];
                            destinationNode = destination[0]*agent.getPfb().getCols() + destination[1];
                            List<Integer> g2r = agent.getPfb().getOptimalPath(originNode, destinationNode);
                            System.out.println("\n----------------------------------------\n");
                            System.out.print("Optimal path from garbage to Recycling Center: "+g2r);
                            agent.getHarvesterRecyclePaths().put(agent.getAIDList().get(agentI), agent.getPfb().OptimalPathToCartesianCoords(agent.getPfb().getOptimalPath(originNode, destinationNode)));
                            
                            
                            if(h2g != null && g2r != null){
                                //ArrayList<StreetCell> path = (ArrayList<StreetCell>) agent.getPfb().OptimalPathToCartesianCoords(h2g);
                                //ArrayList<StreetCell> path2 = (ArrayList<StreetCell>) agent.getPfb().OptimalPathToCartesianCoords(agent.getPfb().getOptimalPath(originNode, destinationNode));
//                                agent.getHarvesterGarbagePaths().put(agent.getAIDList().get(agentI), path.subList(1, path.size()));
//                                agent.getHarvesterRecyclePaths().put(agent.getAIDList().get(agentI), path2.subList(1, path2.size()));
                                agent.getHarvesterGarbagePaths().put(agent.getAIDList().get(agentI), agent.getPfb().OptimalPathToCartesianCoords(h2g));
                                agent.getHarvesterRecyclePaths().put(agent.getAIDList().get(agentI), agent.getPfb().OptimalPathToCartesianCoords(agent.getPfb().getOptimalPath(originNode, destinationNode)));
                                agent.getHarvesterGarbageBuilding().put(agent.getAIDList().get(agentI), garbage);
                                agent.getHarvesterRecyclinCenter().put(agent.getAIDList().get(agentI), agent.getRecyclingCenter().get(BestRecyclingCenter));
                            }
                            System.out.println(agent.getAIDList().get(agentI));
                            System.out.println("Optimal Path H2G : "+agent.getHarvesterGarbagePaths().get(agent.getAIDList().get(agentI)));
                            System.out.println("Optimal Path G2R : "+agent.getHarvesterRecyclePaths().get(agent.getAIDList().get(agentI)));
                            System.out.println("Garbage in Building : "+agent.getHarvesterGarbageBuilding().get(agent.getAIDList().get(agentI)));
                            System.out.println("Recycling center : "+agent.getHarvesterRecyclinCenter().get(agent.getAIDList().get(agentI)));
                        }
                        
                        for (int e : agent.getAgentStatus()){
                            System.out.println("Agent Status: "+String.valueOf(e));
                        }
                                
                        //Increment CoalitionNumber
                        CoalitionNumber++;
                        agent.setCoalitionNumber(CoalitionNumber);
                        agent.getSettableBuildingCellList().remove(0);
                    }
                    
                }
            }
        }
    }
    
    