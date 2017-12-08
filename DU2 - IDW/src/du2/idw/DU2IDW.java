/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package du2.idw;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
//                if (items[0].equals("-n"))
//                    System.out.println(items[0]);
/**
 *
 * @author Blizius
 */
public class DU2IDW {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        double []x0 = null;
        double []y0 = null;
        double []z0 = null;
        int lineCount = 0;
        int coord = 0;
        try {            
            BufferedReader br = new BufferedReader(new FileReader(args[0]));
            String line;            
            while ((line = br.readLine())!=null){                               
                String [] items;
                items = line.split(",");                
                if (coord == 0){
                    lineCount = parseInt(items[0]);
                    coord++;                    
                }                    
                else if (coord == 1){                    
                    x0 = toDouble(items, lineCount);                    
                    coord++;
                }
                else if (coord == 2){                    
                    y0 = toDouble(items, lineCount);
                    coord++;
                }
                else                   
                    z0 = toDouble(items, lineCount);                
            }
        
            
        } catch (FileNotFoundException ex) {
            System.err.format("File %s not found.",args[0]);
            System.exit(1);
        } catch (IOException ex) {
            System.err.print("Error while reading a line.");
            System.exit(1);
        }
        catch (NumberFormatException ex){
            System.err.print("Wrong data format, line count has to be an integer.");
            System.exit(1);
        }        
        
        double []z = IDW(x0,y0,z0,lineCount);
        PrintWriter writer;
        try {
            writer = new PrintWriter(args[1]);
            for (int i = 0; i < z.length; i++)
            {
                if ((i+1)%100 == 0){
                    writer.format("%.2f\n", z[i]);
                }
                else{
                    writer.format("%.2f;", z[i]);
                }
            }            
            writer.close();
        } catch (FileNotFoundException ex) {
            System.err.format("File %s not found.",args[1]);
            System.exit(1);
        }
    }
    
    public static double [] toDouble(String []items, int lines){                    
        double []line;
        line = new double [lines];
        try {
            for (int i = 0; i < lines; i++)
            {
                line[i] = parseDouble(items[i]);
            }            
        }
        catch (NumberFormatException ex){
        System.err.print("Wrong data format, coordinates have to be numbers.");
        System.exit(1);
        }
        return line;
    }
    
    public static double []IDW(double []x0, double []y0, double []z0, int lines){
        double []x = new double [100];
        double []y = new double [100];
        double []z = new double [100*100];
        x[0] = min(x0);
        y[0] = min(y0);
        x[99] = max(x0);
        y[99] = max(y0);
        double xStep = (x[99] - x[0])/99;
        double yStep = (y[99] - y[0])/99;        
        for (int i = 1; i < 99; i++){
            x[i] = x[i-1] + xStep;
            y[i] = y[i-1] + yStep;
        }
        
        double Dist;
        double w;
        double wSum = 0;
        double zSum = 0;
        for (int a = 0; a < y.length; a++){            
            for (int i = 0; i < x.length; i++){
                for (int k = 0; k < lines; k++){
                    Dist = (Math.sqrt((x0[k] - x[i])*(x0[k] - x[i]) + (y0[k] - y[a])*(y0[k] - y[a])));
                    if (Dist == 0){
                        z[(a*x.length)+i] = z0[k];
                    }
                    else{    
                    w = 1/(Dist*Dist);
                    wSum += w;
                    zSum += z0[k]*w;
                    }
                }
                z[(a*x.length)+i] = zSum/wSum;
            }
        }
        return z;
    }
    
    public static double min(double []pole){
        Arrays.sort(pole);
        return pole[0];
    }
    public static double max(double []pole){
        Arrays.sort(pole);
        return pole [pole.length - 1];
    }
}
