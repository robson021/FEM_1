package fem_1;

import Jama.Matrix;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author Robert Nowak
 */
public class FEM_1 {
    private static final String RESULT_FILE = "src/result.txt", SEPARATOR = ",";
    private int num_elements=0, num_nodes=0;
    private double[][] GH;
    private double[][] PL;
    private static double alpha, q, temperatureOfEnvironment;
    private final List<Node> nodes = new ArrayList<>();
    private final List<Element> elements = new ArrayList<>();   
    
    
    /**
     * Loads data form .csv file. 
     * The file looks like: <br>
     * 
     * alpha <br>
     * q <br>
     * temp_of_enviroment <br>
     * bc, x  <br>
     * s, k   <br>
     * bc, x   <br>
     * s, k    <br>
     * bc, x  <br>
     * 
     * etc... <br>
     * 
     * Example:
     *  <br>
     * 10 <br>
     * -150 <br>
     * 40 <br>
     * 1, 0 <br>
     * 1, 70 <br>
     * 0, 1 <br>
     * 1, 70 <br>
     * 2, 2 <br>
     * 
     *  
     * @param FILE_NAME name of file to be loaded 
     * 
     * @return true or false - loaded or not
     *
     */
    private boolean loadFromFile(final String FILE_NAME) throws IOException {
        boolean tof = false;
        BufferedReader fileReader = null;
        String line = "";
        try {
            fileReader = new BufferedReader(new FileReader(new File("src/"+FILE_NAME)));
            line = fileReader.readLine();
            
            alpha = Double.parseDouble(line);
            line = fileReader.readLine();
            q = Double.parseDouble(line);
            line = fileReader.readLine();
            temperatureOfEnvironment = Double.parseDouble(line);
            
            line = fileReader.readLine();
            String[] values = line.split(SEPARATOR);
            double[] elementsVal = new double[2];
            double[] nodesVal = new double[2]; // for node: bc, x
            nodesVal[0] = Double.parseDouble(values[0]);
            nodesVal[1] = Double.parseDouble(values[1]);
            
            Node node2, node1 = new Node(num_nodes++, (int) nodesVal[0], nodesVal[1]);
            nodes.add(node1);
            
            while ((line = fileReader.readLine()) != null) 
            {
                //System.out.println(line);
                values = line.split(SEPARATOR);
                if (values.length == 0)
                    break;
                
                // element: s, k
                elementsVal[0] = Double.parseDouble(values[0]);
                elementsVal[1] = Double.parseDouble(values[1]);
                
                line = fileReader.readLine();
                //System.out.println(line);
                values = line.split(SEPARATOR);
                
                // node: bc, x
                nodesVal[0] = Double.parseDouble(values[0]);
                nodesVal[1] = Double.parseDouble(values[1]);
                node2 = new Node(num_nodes++, (int) nodesVal[0], nodesVal[1]);            
                Element element = new Element(node1, node2, num_elements++, elementsVal[0], elementsVal[1]);
                
                elements.add(element);
                nodes.add(node2);
                
                node1=node2;
            }
            tof = true;
        } finally {
            if (fileReader != null)
                fileReader.close();
            return tof;
        }
               
    }
    
    private void saveResultToFile(double[][] result) throws IOException {
        FileWriter fw = null;
        try {          
            fw = new FileWriter(new File(RESULT_FILE));    
            fw.append(Calendar.getInstance().getTime().toString()+"\n\n");
            for (int i=0;i<num_nodes;i++) {
                fw.append(i+":  "+String.valueOf(result[i][0]));
                fw.append("\n");
            }           
            System.out.println("\tSaved to: "+RESULT_FILE);
        } finally {
            if (fw != null)
                fw.close();
        }
    }
    
    private void initSampleCSVfile() {
        //TODO
    }
    private void initGlobalMatrix(){
        final int size = num_nodes;
        GH = new double[size][size];
        for (int i=0;i<size;i++)
            for(int j=0;j<size;j++)
                GH[i][j] = .0;
        
        int i=0, j=0;
        for(Element el : elements) 
            sumMatrix(i++, j++, el);        
    }
    private void sumMatrix(int i, int j, Element el){
        GH[i][j] += el.getHLvalueAtIndex(0, 0);
        GH[i][j+1] += el.getHLvalueAtIndex(0, 1);
        GH[i+1][j] += el.getHLvalueAtIndex(1, 0);
        GH[i+1][j+1] += el.getHLvalueAtIndex(1, 1);
    }
    
    private double[][] compute() {
        Matrix H = new Matrix(GH);        
        Matrix T = H.solve(new Matrix(PL).transpose().uminus()); //transponowanie i zmiana znaku
        
        return T.getArray();
    }
    
    public void runProgram() throws IOException {
        String fileName = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter file name, example: test.csv or test.txt");
        fileName = br.readLine();
        //fileName = "test.csv";        
        if (!loadFromFile(fileName)){
            System.out.println("Error. Could not find or read the file.");
            System.exit(1);
        }
        
        printBasicInfo();
        initGlobalMatrix();               
        printGlobalMatrixGH();
        initPLmatrix();
        printPLmatrix();
        double[][] temperatureVector = compute();
        
        System.out.println("\n\tTemperature vector:");
        for (int i=0;i<num_nodes;i++)
            System.out.println(temperatureVector[i][0]+" K;\t"+(temperatureVector[i][0]-273)+" C");
        
        saveResultToFile(temperatureVector);
    }
    
    private void printBasicInfo() {
        System.out.println("\nalpha: "+alpha);
        System.out.println("q: "+q);
        System.out.println("temperature of environment: "+temperatureOfEnvironment+" K;\t"+(temperatureOfEnvironment-273+" C"));        
        elements.stream().forEach((e) -> {
            System.out.println(e.toString());
        }); System.out.println("Total element length: "+Element.getTotalLength());
    }
    
    private void printGlobalMatrixGH() {
        System.out.println("\n\tGlobal matrix(GH):");
        for (int i=0;i<GH.length;i++){
            for (int j=0;j<GH.length;j++)
                System.out.print(GH[i][j] + " ");
            System.out.println("");
        }
    }
    
    public static double getAlpha() {
        return alpha;
    }
    public static double get_q() {
        return q;
    }
    public static double getEnvTemperature() {
        return temperatureOfEnvironment;
    }
    
    private void initPLmatrix() {
        PL = new double[1][num_nodes];
        for (int i=0;i<num_nodes;i++) {
            PL[0][i] = .0;
            if (nodes.get(i).getBC() == 1)
                PL[0][i] = q * elements.get(0).getS();
            else if (nodes.get(i).getBC() == 2)
                PL[0][i] = -alpha * temperatureOfEnvironment * elements.get(elements.size()-1).getS();
        }
                
        // wektor obciazen
        //PL[0][0] = q * elements.get(0).getS();
        //PL[0][num_nodes-1] = -alpha * temperatureOfEnvironment * elements.get(elements.size()-1).getS();
    }
    
    private void printPLmatrix() {
        System.out.println("\n\t PL matrix:");
        for (int i=0;i<num_nodes;i++)
            System.out.println(PL[0][i]);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new FEM_1().runProgram();
        } catch (IOException ex) {
            System.exit(1);
        }
        System.exit(0);
    }
}
