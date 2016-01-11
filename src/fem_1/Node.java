package fem_1;

/**
 *
 * @author robert
 */
public class Node {
    private final int id;
    private double t;
    private final int bc;
    private final double x;
    
    public Node(int anID, int bc, double X){
        id=anID; this.bc=bc; x=X;  
    }
    
    @Override
    public String toString(){
        return "Node #"+id+"; x = "+x+"; t = "+t;
    }
    public double getX() {
        return x;
    }
    public int getBC(){
        return bc;
    }
}
