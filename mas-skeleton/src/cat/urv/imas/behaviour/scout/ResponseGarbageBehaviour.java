/**
 *  IMAS base code for the practical work.
 *  Copyright (C) 2014 DEIM - URV
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.behaviour.scout;

import cat.urv.imas.agent.ScoutAgent;
import cat.urv.imas.map.BuildingCell;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.InfoAgent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;;
import cat.urv.imas.onthology.MessageContent;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**

 */
public class ResponseGarbageBehaviour extends AchieveREResponder {

    /**
     * Sets up the System agent and the template of messages to catch.
     *
     * @param agent The agent owning this behavior
     * @param mt Template to receive future responses in this conversation
     */
    public ResponseGarbageBehaviour(ScoutAgent agent, MessageTemplate mt) {
        super(agent, mt);
        agent.log("Waiting REQUESTs from authorized agents");
    }

    /**
     * When System Agent receives a REQUEST message, it agrees. Only if
     * message type is AGREE, method prepareResultNotification() will be invoked.
     * 
     * @param msg message received.
     * @return AGREE message when all was OK, or FAILURE otherwise.
     */
    @Override
    protected ACLMessage prepareResponse(ACLMessage msg) {
        ScoutAgent scout = (ScoutAgent) this.getAgent();
        ACLMessage reply = msg.createReply();
        try {
            HashMap<String,List<Cell>> content = (HashMap<String,List<Cell>>)msg.getContentObject();
            if (content.keySet().iterator().next().equals(MessageContent.GET_GARBAGE)) {
                ArrayList<Cell> adjacentCells = (ArrayList<Cell>) content.get(MessageContent.GET_GARBAGE);
                scout.setAdjacentCells(adjacentCells);
                scout.log( " - " + myAgent.getLocalName() + " <- " + adjacentCells.size() );

                // perform search of garbage and movement
                this.execute(scout);

                reply.setPerformative(ACLMessage.AGREE);
                reply.setContentObject(scout.getGarbageCells());
            }
        } catch (UnreadableException | IOException e) {
            reply.setPerformative(ACLMessage.FAILURE);
            scout.errorLog(e.getMessage());
        }
        scout.log("Response being prepared");
        return reply;
    }

    /**
     * After sending an AGREE message on prepareResponse(), this behaviour
     * sends an INFORM message with the whole game settings.
     * 
     * NOTE: This method is called after the response has been sent and only when one
     * of the following two cases arise: the response was an agree message OR no
     * response message was sent. 
     *
     * @param msg ACLMessage the received message
     * @param response ACLMessage the previously sent response message
     * @return ACLMessage to be sent as a result notification, of type INFORM
     * when all was ok, or FAILURE otherwise.
     */
    @Override
    protected ACLMessage prepareResultNotification(ACLMessage msg, ACLMessage response) {

        // it is important to make the createReply in order to keep the same context of
        // the conversation
        ScoutAgent agent = (ScoutAgent)this.getAgent();
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        try {            
            reply.setContentObject(agent.getGarbageCells());
            agent.log("Cells with Garbage sent from: " + agent.getLocalName());
        } catch (Exception e) {
            reply.setPerformative(ACLMessage.FAILURE);
            agent.errorLog(e.toString());
            e.printStackTrace();
        }

        return reply;

    }

    /**
     * No need for any specific action to reset this behaviour
     */
    @Override
    public void reset() {
    }

    public void execute(ScoutAgent scout) {
        
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
        int currentDir = scout.getCurrentDirection();     
        
        scout.log("Current position: " + currentPos + "Current direction: " + currentDir);
        StreetCell[] priorityDirs = this.getPriorityDirections(scout);
        for (int dir = 0 ; dir < 4 ; ++dir) {
            // round-robin
            int candidateDir;
            
            if (currentDir != ScoutAgent.CENTER) {
                candidateDir = (currentDir + dir) % 4;
            } else {
                candidateDir = dir;
            }

            scout.log("Current direction: " + currentDir + " dir: " + dir);            
            scout.log("Evaluating candidate direction: " + candidateDir);
            
            StreetCell candidatePos = priorityDirs[candidateDir];
            scout.log("Evaluating candidate position: " + candidatePos);            
            
            if (candidatePos != null && !candidatePos.isThereAnAgent()) {
                InfoAgent scoutInfo = currentPos.getAgent();
                try {
                    //currentPos.removeAgent(scoutInfo);
                    //candidatePos.addAgent(scoutInfo);

                    StreetCell currentPos2 = (StreetCell) scout.getGame().get(currentPos.getRow(), currentPos.getCol());
                    StreetCell candidatePos2 = (StreetCell) scout.getGame().get(candidatePos.getRow(), candidatePos.getCol());
                    currentPos2.removeAgent(scoutInfo);
                    candidatePos2.addAgent(scoutInfo);    
                    scout.setCurrentDirection(candidateDir);
                    scout.setCurrentPosition(candidatePos2);
                    
                    break;
                } catch (Exception e) {
                    scout.log("Movement failed" + e);
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
                scout.log("Testing " + nextPos.toString());
                int nextPosDir = getDirection(scout.getCurrentPosition(), nextPos);
                if (nextPosDir != ScoutAgent.INVALID) {                            
                    dirs[nextPosDir] = nextPos;                
                }
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
