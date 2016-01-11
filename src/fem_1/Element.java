package fem_1;

/**
 *
 * @author robert
 */
public class Element {
    private static double totalLength = .0;
    private static int totalNumElements = 0; 
    private static double sumS = .0;
    private final Node node1, node2;
    private final int ID;
    private final double S, K, L;
    private double[][] hl, pl = null;
    
    public Element(Node n1, Node n2, int anID, double s, double k){
        node1=n1; node2=n2; 
        ID=anID;
        S=s; K=k; 
        L = node2.getX() - node1.getX();
        totalLength += L;
        sumS += S;
        init_HL_PL();
        totalNumElements++;
    }
    
    private void init_HL_PL() {
        double c = S*K/L;
        hl = new double[2][2];
        hl[0][0]=hl[1][1] = c;
        hl[1][0]=hl[0][1] = (-c);
        
        if (node1.getBC() == 1) {
            pl = new double[1][2];
            pl[0][0] = S*FEM_1.get_q(); // = S * q
        } else if (node2.getBC() == 2) {
            hl[1][1] += S * FEM_1.getAlpha(); // += s * alpha
        }
    }
    
    public double getHLvalueAtIndex(int i, int j) {
        return hl[i][j];
    }
    
    public static double getTotalLength() {
        return totalLength;
    }
    
    public static double getAvgS() {
        return sumS/(totalNumElements);
    }
    public double getS() {
        return S;
    }
        
    @Override
    public String toString() {
        return "Element #"+ID+" nodes: "+node1.getX()+", "+node2.getX();
    }
        
}
