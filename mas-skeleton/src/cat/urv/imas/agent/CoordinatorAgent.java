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
package cat.urv.imas.agent;

import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.behaviour.coordinator.RequesterBehaviour;
import cat.urv.imas.onthology.InitialGameSettings;
import cat.urv.imas.onthology.MessageContent;
import cat.urv.imas.onthology.MessageWrapper;
import jade.core.*;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The main Coordinator agent. 
 * TODO: This coordinator agent should get the game settings from the System
 * agent every round and share the necessary information to other coordinators.
 */
public class CoordinatorAgent extends ImasAgent {

    /**
     * Game settings in use.
     */
    private GameSettings game;

    public GameSettings getGame() {
        return game;
    }

    public void setGame(GameSettings game) {
        this.game = game;
    }
    /**
     * System agent id.
     */
    private AID systemAgent;

    /**
     * Builds the coordinator agent.
     */
    public CoordinatorAgent() {
        super(AgentType.COORDINATOR);
    }

    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {

        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.COORDINATOR.toString());
        sd1.setName(getLocalName());
        sd1.setOwnership(OWNER);
        
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd1);
        dfd.setName(getAID());
        try {
            DFService.register(this, dfd);
            log("Registered to the DF");
        } catch (FIPAException e) {
            System.err.println(getLocalName() + " registration with DF unsucceeded. Reason: " + e.getMessage());
            doDelete();
        }

        // search SystemAgent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.SYSTEM.toString());
        this.systemAgent = UtilsAgents.searchAgent(this, searchCriterion);
        // searchAgent is a blocking method, so we will obtain always a correct AID

        /* ********************************************************************/
        ACLMessage initialRequest = new ACLMessage(ACLMessage.REQUEST);
        initialRequest.clearAllReceiver();
        initialRequest.addReceiver(this.systemAgent);
        initialRequest.setProtocol(InteractionProtocol.FIPA_REQUEST);
        log("Request message to agent");
        try {
            initialRequest.setContent(MessageContent.GET_MAP);
            log("Request message content:" + initialRequest.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //we add a behaviour that sends the message and waits for an answer
        this.addBehaviour(new RequesterBehaviour(this, initialRequest));

        
        // setup finished. When we receive the last inform, the agent itself will add
        // a behaviour to send/receive actions
    

        //INICIO DARIO
        //Add behaviour to inform basic info about game
        addBehaviour(new InitialInformToHarvesterCoordinatorAgentBehaviour(this));
    
        
        this.setGame(InitialGameSettings.load("game.settings"));
        
        log("Initial configuration settings loaded");
        //FIN DARIO
        
    }

    
    
    
    //INICIO DARIO
    public class InitialInformToHarvesterCoordinatorAgentBehaviour extends OneShotBehaviour {
        
        public InitialInformToHarvesterCoordinatorAgentBehaviour(CoordinatorAgent agent) {
            super(agent);
        }
        
        @Override
        public void action() {
                       
            CoordinatorAgent agent = (CoordinatorAgent)this.getAgent();
            //Delay the search in DF
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException ex) {
                Logger.getLogger(HarvesterCoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            // Search for services of name "HARVESTERCOORDINATOR"
            String serviceName = AgentType.HARVESTER_COORDINATOR.toString();
            
            System.out.println("Agent "+getLocalName()+" searching for services of type "+serviceName);
            try {
                // Build the description used as template for the search
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription templateSd = new ServiceDescription();
                templateSd.setName(serviceName);
                template.addServices(templateSd);
  		
                SearchConstraints sc = new SearchConstraints();
                // We want to receive 10 results at most
                sc.setMaxResults(new Long(10));
  	
                DFAgentDescription[] results = DFService.search(CoordinatorAgent.this, template, sc);
                
                if (results.length > 0) {
                    System.out.println("Agent "+getAID()+" found the following services:");
                    for (int i = 0; i < results.length; ++i) {
  			DFAgentDescription dfd = results[i];
  			AID provider = dfd.getName();
  	
                        MessageWrapper message = new MessageWrapper();
                        message.setType(MessageContent.SEND_GAME);
                        message.setObject(agent.getGame());
                        ACLMessage msg = new ACLMessage( ACLMessage.INFORM );
                        msg.setContentObject(message);
        
                        msg.clearAllReceiver();
                        msg.addReceiver(provider);

                        send(msg);
                    }
                }	
                else {
                    System.out.println("Agent "+getLocalName()+" did not find any "+serviceName+ " service");
                }
            
            }
            catch (FIPAException fe) {
  		fe.printStackTrace();
            } catch (IOException ex) {
                Logger.getLogger(CoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    //FIN DARIO
    
}
