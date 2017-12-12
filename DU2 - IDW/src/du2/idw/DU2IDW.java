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
        if (args.length == 0){
            System.err.print("Nebyly zadány žádné argumenty. Minimálně vstupní a"
                    + " výstupní soubor je potřeba k běhu programu.");
            System.exit(1);
        }
        
        int lineCount = 0;        
        double p = power(args);     //proměná pro mocninu váhy
        
        // Načtení počtu dat ze souboru a následná inicializace pole souřadnic na tuto délku.
        try {
            BufferedReader br = new BufferedReader(new FileReader(args[args.length - 2]));    
            String line;
            line = br.readLine();
            lineCount = parseInt(line);
        }
        catch (FileNotFoundException ex) {
                System.err.format("Soubor %s nenalezen.", args[args.length - 2]);
                System.exit(1);
        } catch (IOException ex) {
            System.err.print("Chyba při čtení ze souboru.");
            System.exit(1);
        } catch (NumberFormatException ex) {
            System.err.print("Špatný formát dat, první řádek musí být jedno celé číslo");                        
            System.exit(1);
        } catch (NullPointerException ex){
            System.err.print("V souboru se nenachází žádná dat.");
            System.exit(1);
        }  
        
        double [][] xyz = new double [3][lineCount];        
        
        // Podmínka na základě přepínače -d pro výběr formátu dat. Pokud je zadán -d
        // bude použito čtení ze souboru se souřadnicemi x ve druhém, y ve třetím  a z ve čtvrtém řádku.
        // Jinak normální sloupcový vstup.
        if (data(args) == false) {
            xyz = read (args,xyz);            
        }        
        else{
            xyz = dRead (args,xyz);                            
        }
        // Vytvoření pole všech interpolovaných hodnot a jejich zápis do souboru.
        double[] z = IDW(xyz, p);
        PrintWriter writer;
        try {
            writer = new PrintWriter(args[args.length - 1]);
            for (int i = 0; i < z.length; i++) {
                if ((i + 1) % 100 == 0) {
                    writer.format("%.2f\n", z[i]);
                } else {
                    writer.format("%.2f;", z[i]);
                }
            }
            writer.close();
        } catch (FileNotFoundException ex) {
            System.err.format("Soubor %s nenalezen.", args[args.length - 1]);
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
            System.err.print("Špatný formát dat, souřadnice musí být čísla (s desetinou tečkou)"
                    + " oddělená čárkou.");
            System.exit(1);
        }
        return line;
    }
    // Funkce pro výpočet interpolovaných hodnot v mřížce bodů 100*100. Vstupují souřadnice
    // zadaných bodů ze souboru a parametr mocniny vah. Vystupuje pole
    // z-ových hodnot všech interpolovaných bodů mřížky.
    public static double[] IDW(double[][]xyz, double p) {
        double[] x = new double[100];
        double[] y = new double[100];
        double[] z = new double[100 * 100];
        x[0] = min(xyz[0]);
        y[0] = min(xyz[1]);
        x[99] = max(xyz[0]);
        y[99] = max(xyz[1]);
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
                for (int k = 0; k < xyz[0].length; k++) {
                    Dist = (Math.sqrt((xyz[0][k] - x[i]) * (xyz[0][k] - x[i]) + (xyz[1][k] - y[a]) * (xyz[1][k] - y[a])));
                    if (Dist == 0) {        // Pokud je vzdálenost 0, hodnota bodu se rovná hodnotě bodu zadaného.
                        z[(a * x.length) + i] = xyz[2][k];
                        wSum = 0;
                        zSum = 0;
                        break;
                    } else {
                        w = 1 / (pow(Dist,p));
                        wSum += w;
                        zSum += xyz[2][k] * w;
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
                    System.err.print("Mocnina nemůže být záporná nebo 0.");
                    System.exit(1);
                }
                return p;                    
            }
        }
        return 2;
    }
    // Funkce pro čtení dat ze souboru ve formátu ze zadání. Sloupec x, druhý sloupec y, třetí sloupec z.
    public static double [][] read(String [] args, double [][]xyz){        
        try {
                BufferedReader br = new BufferedReader(new FileReader(args[args.length - 2]));    
                String line;
                int coord = -1;                
                //Cyklus pro čtení ze souboru
                while ((line = br.readLine()) != null) {
                    if (coord == -1){       // Přeskočení již známého prvního řádku počtu dat.
                        coord++;
                        continue;
                    }
                    String[] items;
                    items = line.split(",");                          
                    if (items.length != 3){              // Chybová podmínka pro případ vybrání špatného formátu dat.
                        System.err.print("Špatný formát dat, musí zde být tři"
                                + " sloupce. První pro x, druhý pro y a třetí"
                                + " pro z.");
                        System.exit(1);
                    }
                    xyz[0][coord] = parseDouble(items[0]);  //Přepisování stringů ze souboru na čísla do polí.
                    xyz[1][coord] = parseDouble(items[1]);
                    xyz[2][coord] = parseDouble(items[2]);
                    coord++;
                }
            }
            
           // Chybová hlášení a ukončení programů v případě vyjímek.
            catch (FileNotFoundException ex) {
                System.err.format("Soubor %s nenalezen.", args[args.length - 2]);
                System.exit(1);
            } catch (IOException ex) {
                System.err.print("Chyba při čtení ze souboru.");
                System.exit(1);
            } catch (NumberFormatException ex) {
                System.err.print("Špatný formát dat, první řádek musí být jedno celé číslo"
                        + " (počet bodů) a zbytek řádků souřadnice (čísla s desetinou tečkou),"
                        + " oddělenné čárkou.");
                System.exit(1);
            }            
            catch (ArrayIndexOutOfBoundsException ex){
                System.err.print("V souboru je rozdílný počet x-ových, y-ových a z-ových souřadnic.");
                System.exit(1);
            }
        return xyz;
    }
    // Druhý způsob čtení ze souboru pro x ve druhém, y ve třetím  a z ve čtvrtém řádku.
    // Opět s chybovými hlášeními.
    public static double [][] dRead (String [] args, double [][]xyz){        
        try {
            BufferedReader br = new BufferedReader(new FileReader(args[args.length - 2]));
            String line;
            int coord = 0;
            while ((line = br.readLine()) != null) {
                String[] items;
                items = line.split(",");
                if (coord == 0){        // Přeskočení již známého prvního řádku počtu dat.
                    coord++;                    
                }
                else if (coord == 1) {
                    xyz[0] = toDouble(items);
                    coord++;
                } else if (coord == 2) {
                    xyz[1] = toDouble(items);
                    coord++;
                } else if (coord == 3) {
                    xyz[2] = toDouble(items);
                    coord++;
                } else {
                    System.err.print("Špatný formát dat. Druhý řádek musí být x-ové,"
                            + " třetí y-ové a čtvrtý z-ové souřadnice.");
                    System.exit(1);
                }
            }

        }
        catch (FileNotFoundException ex) {
            System.err.format("Soubor %s nenalezen.", args[args.length - 2]);
            System.exit(1);
        } catch (IOException ex) {
            System.err.print("Chyba při čtení ze souboru.");
            System.exit(1);
        }   
        return xyz;        
    }
}


