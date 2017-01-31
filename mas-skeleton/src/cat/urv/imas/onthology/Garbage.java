/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.onthology;

/**
 *
 * @author Dario
 */
public class Garbage {
   
    private GarbageType type;
    private int qty;
    private int x;
    private int y;
    
    
    public void setGarbage(GarbageType type,int qty,int x, int y){
        this.type = type;
        this.qty = qty;
        this.x = x;
        this.y = y;
    }
    
    public GarbageType getType(){
        return this.type;
    }
    
    public int getQty(){
        return this.qty;
    }
    
    public int getX(){
        return this.x;
    }
    
    public int getY(){
        return this.y;
    }
    
}
