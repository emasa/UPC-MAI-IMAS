/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import cat.urv.imas.behaviour.harvester.ResponseMovementBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.GarbageType;
import cat.urv.imas.onthology.MessageContent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 *
 * @author Dario ......
 */
public class HarvesterAgent extends ImasAgent{

    private StreetCell position;
    private GarbageType[] garbageTypes;
    private GameSettings game;
    
    /**
     * Builds the coordinator agent.
     */
    public HarvesterAgent() {
        super(AgentType.HARVESTER);
    }
    
    @Override
    protected void setup() {
        log("Creatad new Harvester");
        Object[] args = getArguments();
        if (args != null && args.length > 1) {
            //position = (Cell) args[0];
            garbageTypes = (GarbageType[]) args[1]; 
            
            position = (StreetCell) args[0];
            
            position.getAgent().setAID(getAID());
            
            String typesStr = "";
            for (GarbageType garbageType : garbageTypes) {
                typesStr += " " + garbageType.getShortString();
            }
            log("At (" + position.getRow() + " , " + position.getCol() + ") accepting types:" + typesStr);
            
            //INICIO DARIO
            // Register the service
            String serviceName = this.getLocalName();
            String serviceType = AgentType.HARVESTER.toString();

            try {
                
                DFAgentDescription dfd = new DFAgentDescription();
  		dfd.setName(getAID());
  		ServiceDescription sd = new ServiceDescription();
  		sd.setName(serviceName);
  		sd.setType(serviceType);
                
                Property p = new Property();
                p.setName("GarbageTypes");
                p.setValue(typesStr);
                
                sd.addProperties(p);
                
                sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);
                dfd.addServices(sd);
  		
  		DFService.register(this, dfd);
                System.out.println("Agent "+getLocalName()+" registering service \""+serviceName+"\" of type "+serviceType);
  	
            }
            catch (FIPAException e) {
                e.printStackTrace();
            }
            //FIN DARIO
            
            // add behaviours
        // we wait for the initialization of the game
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        this.addBehaviour(new ResponseMovementBehaviour(this, mt));
        
        }
        else {
            // Make the agent terminate immediately
            doDelete();
        }

    }

    public GameSettings getGame() {
        return game;
    }

    public StreetCell getPosition() {
        return position;
    }

    public void setPosition(StreetCell position) {
        this.position = position;
    }

}
