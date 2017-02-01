
package cat.urv.imas.behaviour.harvestercoordinator;

import cat.urv.imas.agent.HarvesterCoordinatorAgent;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.Coordinate;
import cat.urv.imas.onthology.InitialGameSettings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 *
 * @author pabloreynoso
 * 
 */


public class PathFinderBehaviour {
    
    
    
    //Harvester coordinates (x,y)
    private final int [] haversterCoord;
    
    //Garbage coordinates (x,y)
    private final int [] garbageCoord;
    
    //Cell Map
    private final Cell[][] cell_map;
    
    //City Graph Matrixes
    private final double[][] graph;
    private final int[][] next;
    private double[][] distances;

    public double[][] getDistances() {
        return distances;
    }

    public void setDistances(double[][] distances) {
        this.distances = distances;
    }
    
    //Columns and Rows of graph, next and distance
    private int rows = 0;
    private int cols = 0;

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }
    
    private final int invalidFlag = -1;
    
    //Optimal Path (List of positions)
    protected List<StreetCell> harvesterOptimalPathPositions = new ArrayList<>();

    public List<StreetCell> getHarvesterOptimalPathPositions1() {
        return harvesterOptimalPathPositions;
    }

    public void setHarvesterOptimalPathPositions1(List<StreetCell> harvesterOptimalPathPositions1) {
        this.harvesterOptimalPathPositions = harvesterOptimalPathPositions1;
    }
    
    public void addharvesterOptimalPathPositions(StreetCell celda){
        this.harvesterOptimalPathPositions.add(celda);
    }
    //NegativeCycle
    private boolean negativeCycle;
    
       
    public PathFinderBehaviour(){
        //Initializing the map
        GameSettings game = InitialGameSettings.load("game.settings");
        this.cell_map = game.getMap();
        
     
        //Rows & Cols city_map
        this.cols = this.cell_map[0].length;
        this.rows = this.cell_map.length;
     
        //Init Harvester & Garbage Positions
        this.haversterCoord = null;
        this.garbageCoord = null;
        
        //Initialize graph algorithm matrixes
        this.graph = new double [rows*cols][rows*cols];
        this.next = new int [rows*cols][rows*cols];
        this.distances = new double [rows*cols][rows*cols];
        
        this.harvesterOptimalPathPositions = null;
    
    
    }
    
    //Constructor
    public PathFinderBehaviour(int [] harvestersCoord, int [] garbageCoord){
        
        
        
        //Initializing the map
        HarvesterCoordinatorAgent hca = new HarvesterCoordinatorAgent();
        GameSettings game = InitialGameSettings.load("game.settings");
        this.cell_map = game.getMap();
        
        //Init Harvester & Garbage Positions
        this.haversterCoord = harvestersCoord;
        this.garbageCoord = garbageCoord;
        
        //Rows & Cols city_map
        this.cols = this.cell_map[0].length;
        this.rows = this.cell_map.length;
     
        //Initialize graph algorithm matrixes
        this.graph = new double [rows*cols][rows*cols];
        this.next = new int [rows*cols][rows*cols];
        this.distances = new double [rows*cols][rows*cols];
        
        this.harvesterOptimalPathPositions = null;
    
    }
    
    //Display harvesters positions list
    public void printHarvesterOptimalPath(){
        
        for(int h=0; h<this.harvesterOptimalPathPositions.size(); h++){
            
            System.out.print("Position "+(h+1)+": (");
            
            System.out.print(this.harvesterOptimalPathPositions.get(h).getRow()+",");
            System.out.print(this.harvesterOptimalPathPositions.get(h).getCol()+")");
            System.out.print("\n");
        
        }
        
    }
    
    //Return the harvester optimal path as list of coordinates
    public List<StreetCell> getHarvesterOptimalPath(){
        return this.harvesterOptimalPathPositions;
    }
    //Initializing Graph
    public void initGraph() {
        
        for(int i=0; i<this.graph.length; i++){
            for(int j=0; j<this.graph.length; j++){
                
                this.graph[i][j] = Double.POSITIVE_INFINITY;
                if (i==j) this.graph[i][j] = 0;
                
                this.next[i][j] = this.invalidFlag;
                if (i==j) this.next[i][j] = 0;
            }
        }
        
    }
    
    //Build Graph based on Cell Map
    public void buildGraphByMap(){
        
        for(int i=0; i<this.rows; i++) {
            for(int j=0; j<this.cols; j++) {
                //System.out.println(this.cell_map[i][j].getCellType());
                if(this.cell_map[i][j].getCellType() == CellType.STREET){
                    
                    if(i-1>-1 && this.cell_map[i-1][j].getCellType() == CellType.STREET) addEdge(i*this.cols+j,(i-1)*this.cols+j,1.0);
                    if(i+1<this.rows && this.cell_map[i+1][j].getCellType() == CellType.STREET) addEdge(i*this.cols+j,(i+1)*this.cols+j,1.0);
                    if(j-1>-1 && this.cell_map[i][j-1].getCellType() == CellType.STREET) addEdge(i*this.cols+j,i*this.cols+(j-1),1.0);
                    if(j+1<this.cols && this.cell_map[i][j+1].getCellType() == CellType.STREET) addEdge(i*this.cols+j,i*this.cols+(j+1),1.0);    
               
                                   
                }
            }
        }
        
        
    }

    //Add arcs to adjacencies list
    private void addEdge(int from, int to, double cost) {
        
        this.graph[from][to] = cost;
        this.next[from][to] = to; //?
        
    }
    
    //Has negative cycle
    private boolean hasNegativeCycle() {
        return this.negativeCycle;
    }

    //FloydWarshall - Shortest path between two update distances matrix
    public double[][] floydWarshall() {
        
        int n = this.graph.length;
        this.distances = Arrays.copyOf(this.graph, n);

        for(int k=0; k<n; k++){
            for(int i=0; i<n; i++){
                for(int j=0; j<n; j++){
                    if(this.distances[i][j] > this.distances[i][k] + this.distances[k][j]){
                        
                         this.distances[i][j] = this.distances[i][k] + this.distances[k][j];
                         this.next[i][j] = this.next[i][k];
                         
                    }
                }
            }

            if (this.distances[k][k] < 0.0) {
                this.negativeCycle = true;
            }
        }
        return this.distances;
        
    }

    //Get optimal path using adjacencies reference matrix
    public List<Integer> getOptimalPath(int uNode, int vNode){
        
        //1)If there is no conecction between node u & v.
        List<Integer> path = new ArrayList<Integer>();
       
        if(this.next[uNode][vNode] == this.invalidFlag){
            path = null;
            return path;
            
        }
        
        //2)Otherwise
        path.add(uNode);
        while (uNode != vNode){
            uNode = this.next[uNode][vNode];
            path.add(uNode);
        }
        
        return path;
    }
    
    //Get distance between two points considering pre-execution of floydWarshall
    public double getDistanceBtwPoints(int x1, int y1, int x2, int y2){
        
        int obj1Pos = x1*this.cols+y1;
        int obj2Pos = x2*this.cols+y2;
        return this.distances[obj1Pos][obj2Pos];
        
    }
    
    public List<StreetCell> OptimalPathToCartesianCoords(List<Integer> optimalPath) {
        
        List<StreetCell> lista = new ArrayList<>();
        System.out.println("OptimalPath Size: "+optimalPath.size());
        for(int i=0; i<optimalPath.size(); i++){
            
            int x = optimalPath.get(i) / this.cols;
            double y = Math.round(((1.0 * optimalPath.get(i) / this.cols) - x)* this.cols);

            lista.add(new StreetCell(x,(int)y));
            
            
            
        }
        return lista;
    }
    
//    Display garbages positions list
//    public static void main(String[]args){
//        
//        Considering map matrix dimensions max (indexes) as (19,24) 
//        NOTE: (18,23) farest poisiton available based on map config
//        int [] hCoord = {18,24}; 
//        int [] gCoord = {7,2};
//        
//        PathFinderBehaviour PFB = new PathFinderBehaviour(hCoord,gCoord);
//        
//        int harvesterPos = PFB.haversterCoord[0]*PFB.cols + PFB.haversterCoord[1];
//        int garbagePos = PFB.garbageCoord[0]*PFB.cols + PFB.garbageCoord[1];
//        
//        System.out.println();
//        System.out.println("cell_map ["+PFB.rows+"x"+PFB.cols+"]");
//        System.out.println("harvesterCoord = ("+PFB.haversterCoord[0]+","+PFB.haversterCoord[1]+")");
//        System.out.println("garbageCoord = ("+PFB.garbageCoord[0]+","+PFB.garbageCoord[1]+")");
//        System.out.println("Considering Adjacencies matrix dimensions : "+PFB.graph.length+"x"+PFB.graph[0].length);
//        System.out.println("harvesterPos = "+harvesterPos);
//        System.out.println("garbagePos = "+garbagePos);
//        System.out.println();
//        
//        /*
//        for(int i=0; i<PFB.rows; i++){
//            for(int j=0; j<PFB.cols; j++){
//                System.out.print(PFB.cell_map[i][j].getCellType()+" ");
//            }
//            System.out.println();
//        }
//        */
//        
//        List<Integer> OptimalPath = null;
//        
//        PFB.initGraph();
//        PFB.buildGraphByMap();
//        PFB.floydWarshall();
//        
//        boolean validHarvesterPos = PFB.cell_map[PFB.haversterCoord[0]][PFB.haversterCoord[1]].getCellType() == CellType.STREET;
//        boolean validGarbagePos = PFB.cell_map[PFB.garbageCoord[0]][PFB.garbageCoord[1]].getCellType() == CellType.STREET;
//
//        if(!validHarvesterPos || !validGarbagePos){
//            
//            if(!validHarvesterPos) System.out.println("Harvester position must be in a street cell");
//            if(!validGarbagePos) System.out.println("Garbage position must be in a street cell");
//            System.exit(1);
//        }
//            
//        OptimalPath = PFB.getOptimalPath(harvesterPos,garbagePos);
//
//        If an optimal path exists
//        if(OptimalPath != null){
//
//            PFB.OptimalPathToCartesianCoords(OptimalPath);
//            PFB.getHarvesterOptimalPath();
//            PFB.printHarvesterOptimalPath();
//
//        }else{
//
//            System.out.println("There is not a path between harvester("+PFB.haversterCoord[0]+","+PFB.haversterCoord[1]+")");
//            System.out.print(" and garbage("+PFB.garbageCoord[0]+","+PFB.garbageCoord[1]+")");
//
//        }
//           
//        
//        
//        
//    } 
//    
    
}
