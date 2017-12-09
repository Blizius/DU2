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
import static java.lang.Math.pow;
import java.util.Arrays;
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
    // Inicializace polí pro souřadnice ze souboru a vytvoření pomocných proměnných.
    public static void main(String[] args) {
        double[] x0 = null;
        double[] y0 = null;
        double[] z0 = null;
        int lineCount = 0;
        int coord = 0;
        double p = power(args);     //proměná pro mocninu váhy
        
        // Podmínka na základě přepínače -d pro výběr formátu dat. Pokud je zadán -d
        // bude použito čtení ze souboru se souřadnicemi x ve druhém, y ve třetím  a z ve čtvrtém řádku.
        //Jinak normální sloupcový vstup.
        if (data(args) == false) {
            coord = -1;            
            try {
                BufferedReader br = new BufferedReader(new FileReader(args[3]));    //Cyklus pro čtení ze souboru
                String line;
                while ((line = br.readLine()) != null) {
                    String[] items;
                    items = line.split(",");
                    if (coord == -1) {                      //Načtení počtu dat a přiřazení délky polím souřadnic.
                        lineCount = parseInt(line);
                        x0 = new double [lineCount];
                        y0 = new double [lineCount];
                        z0 = new double [lineCount];
                        coord++;
                    } else {
                        if (items.length > 3){              // Chybová podmínka pro případ vybrání špatného formátu dat.
                            System.err.print("Wrong data format. There have to be"
                                    + " three columns. First for x, second for y"
                                    + ", third for z.");
                            System.exit(1);
                        }
                        x0[coord] = parseDouble(items[0]);  //Přepisování stringů ze souboru na čísla do polí.
                        y0[coord] = parseDouble(items[1]);
                        z0[coord] = parseDouble(items[2]);
                        coord++;
                    }
                }
            }
           // Chybová hlášení a ukončení programů v případě vyjímek.
            catch (FileNotFoundException ex) {
                System.err.format("File %s not found.", args[3]);
                System.exit(1);
            } catch (IOException ex) {
                System.err.print("Error while reading a line.");
                System.exit(1);
            } catch (NumberFormatException ex) {
                System.err.print("Wrong data format, first line has to be one integer (data count)"
                        + " and coordinates have to be numbers (floats).");
                System.exit(1);
            }
            catch (NullPointerException ex){
                System.err.print("There are no coordinates, only Null.");
                System.exit(1);
            }
            catch (ArrayIndexOutOfBoundsException ex){
                System.err.print("There are more coordinates than what is stated by data count.");
                System.exit(1);
            }
        }
        // Druhý způsob čtení ze souboru pro x ve druhém, y ve třetím  a z ve čtvrtém řádku.
        // Opět s chybovými hlášeními.
        else{
                try {
                    BufferedReader br = new BufferedReader(new FileReader(args[3]));
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] items;
                        items = line.split(",");
                        if (coord == 0) {
                            lineCount = parseInt(line);
                            coord++;
                        } else if (coord == 1) {
                            x0 = toDouble(items);
                            coord++;
                        } else if (coord == 2) {
                            y0 = toDouble(items);
                            coord++;
                        } else if (coord == 3) {
                            z0 = toDouble(items);
                            coord++;
                        } else {
                            System.err.print("Wrong data format. First line has to be"
                                    + " data count (one number), second line x, third line y,"
                                    + " fourth line z.");
                            System.exit(1);
                        }
                    }

                }
                catch (FileNotFoundException ex) {
                    System.err.format("File %s not found.", args[3]);
                    System.exit(1);
                } catch (IOException ex) {
                    System.err.print("Error while reading a line.");
                    System.exit(1);
                } catch (NumberFormatException ex) {
                    System.err.print("Wrong data format, line count has to be one integer.");
                    System.exit(1);
                }
                if (x0.length != y0.length || x0.length != z0.length) {
                    System.err.print("Different count of coordinates. There has to be same"
                            + " amount of x as y and z coordinates. Second line for x, "
                            + "third line for y and fourth line for z.");
                    System.exit(1);
                }
        }
                // Vytvoření pole všech interpolovaných hodnot a jejich zápis do souboru.
                double[] z = IDW(x0, y0, z0, lineCount, p);
                PrintWriter writer;
                try {
                    writer = new PrintWriter(args[4]);
                    for (int i = 0; i < z.length; i++) {
                        if ((i + 1) % 100 == 0) {
                            writer.format("%.2f\n", z[i]);
                        } else {
                            writer.format("%.2f;", z[i]);
                        }
                    }
                    writer.close();
                } catch (FileNotFoundException ex) {
                    System.err.format("File %s not found.", args[4]);
                    System.exit(1);
                }
            }    
    
    // Funkce pro přepis stringů ze souboru na doubly do polí. Použito při druhém způsobu čtení ze soboru.
    public static double[] toDouble(String[] items) {
        double[] line;
        line = new double[items.length];
        try {
            for (int i = 0; i < items.length; i++) {
                line[i] = parseDouble(items[i]);
            }
        } catch (NumberFormatException ex) {
            System.err.print("Wrong data format, coordinates have to be numbers.");
            System.exit(1);
        }
        return line;
    }
    // Funkce pro výpočet interpolovaných hodnot v mřížce bodů 100*100. Vstupují souřadnice
    // zadaných bodů ze souboru, počet dat a parametr mocniny vah. Vystupuje pole
    // z-ových hodnot všech interpolovaných bodů mřížky.
    public static double[] IDW(double[] x0, double[] y0, double[] z0, int lines, double p) {
        double[] x = new double[100];
        double[] y = new double[100];
        double[] z = new double[100 * 100];
        x[0] = min(x0);
        y[0] = min(y0);
        x[99] = max(x0);
        y[99] = max(y0);
        double xStep = (x[99] - x[0]) / 99;
        double yStep = (y[99] - y[0]) / 99;
        for (int i = 1; i < 99; i++) {
            x[i] = x[i - 1] + xStep;
            y[i] = y[i - 1] + yStep;
        }   
//      /\ Určení rozsahu a vytvoření souřadnic mřížky bodů 100*100. /\
        
        // Trojitý cyklus pro výpočet hodnot na základě vah převrácených vzdáleností.
        double Dist = 0;
        double w;
        double wSum = 0;
        double zSum = 0;
        for (int a = 0; a < y.length; a++) {
            for (int i = 0; i < x.length; i++) {
                for (int k = 0; k < lines; k++) {
                    Dist = (Math.sqrt((x0[k] - x[i]) * (x0[k] - x[i]) + (y0[k] - y[a]) * (y0[k] - y[a])));
                    if (Dist == 0) {        // Pokud je vzdálenost 0, hodnota bodu se rovná hodnotě bodu zadaného.
                        z[(a * x.length) + i] = z0[k];
                        wSum = 0;
                        zSum = 0;
                        break;
                    } else {
                        w = 1 / (pow(Dist,p));
                        wSum += w;
                        zSum += z0[k] * w;
                    }
                }
                if (Dist != 0) {
                    z[(a * x.length) + i] = zSum / wSum;
                    wSum = 0;
                    zSum = 0;
                }
            }
        }
        return z;
    }
    // Funkce pro minimum ze zadaných x-ových nebo y-ových souřadnic.
    public static double min(double[] pole) {
        double min = pole[0];
        for (int i = 0; i < pole.length; i++) {
            if (pole[i] < min) {
                min = pole[i];
            }
        }
        return min;
    }
    // Funkce pro maximum ze zadaných x-ových nebo y-ových souřadnic.
    public static double max(double[] pole) {
        double max = pole[0];
        for (int i = 0; i < pole.length; i++) {
            if (pole[i] > max) {
                max = pole[i];
            }
        }
        return max;
    }
    // Funkce pro určení, zda byl zadán přepínač -d pro výběr formátu vstupního souboru.
    public static boolean data(String []args){
        boolean d = false;
        for (int i = 0; i < args.length; i++){
            if (args[i].equals("-d")){
                d = true;
                break;
            }
        }
        return d;
    }
    // Funkce pro určení zda a jaký parametr -p byl zadán pro mocninu vah IDW.
    // Pokud nezadán, přiřazena hodnota 2.
    public static double power(String []args){        
        double p;
        for (int i = 0; i < args.length; i++){
            if(args[i].equals("-p")){
                p = parseDouble(args[i+1]);
                if (p <= 0){
                    System.err.print("Power cannot be negative or zero.");
                    System.exit(1);
                }
                return p;                    
            }
        }
        return 2;
    }
}


