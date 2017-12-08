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
import java.util.logging.Level;
import java.util.logging.Logger;

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
        int coord;
        try {
            BufferedReader br = new BufferedReader(new FileReader(args[0]));
            String line;
            
            while ((line = br.readLine())!=null){
                coord = 1;
                String [] items;
                items = line.split(",");
                int len = items.length;
//                if (items[0].equals("-n"))
//                    System.out.println(items[0]);
                if (coord == 1)                    
                    x0 = toDouble(items, coord);
                if (coord == 2)                    
                    y0 = toDouble(items, coord);
                if (coord == 3)                    
                    z0 = toDouble(items, coord);
                coord++;
            }
            
        } catch (FileNotFoundException ex) {
            System.err.format("File %s not found",args[0]);
            System.exit(1);
        } catch (IOException ex) {
            System.err.print("Error while reading a line");
            System.exit(1);
        }
        
        PrintWriter writer;
        try {
            writer = new PrintWriter(args[1]);
            for (int i = 0; i < x0.length; i++)
            {
                writer.print(x0[i]);
            }
            
            writer.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DU2IDW.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static double [] toDouble(String []items, int i){
        if (i == 1){            
            double []x;
            x = new double [items.length];
            for (int a = 0; a < items.length; a++)
            {
                x[a] = Double.parseDouble(items[a]);
            }
            return x;
        }
        if (i == 2){            
            double []y;
            y = new double [items.length];
            for (int a = 0; a < items.length; a++)
            {
                y[a] = Double.parseDouble(items[a]);
            }
            return y;
        }
        else{            
            double []z;
            z = new double [items.length];
            for (int a = 0; a < items.length; a++)
            {
                z[a] = Double.parseDouble(items[a]);
            }
            return z;
        }
        
//        
//        }
    }
    
}
