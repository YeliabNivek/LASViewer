package com;

import com.Data.Face;
import com.Data.Model;
import com.Data.Vector3d;
import com.Frame.mainFrame;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author 13Baileykt
 */
public class Main {

    public static String version = "Alpha 1";
    public static int width = 800, height = 600, max_fps = 60;
    public static float walkSpeed = .05f;
    public static double scale = 1d;
    public static File las2txt, txt2las, las2tin, shp2text, curr;

    public static void main(String... args) {
        new mainFrame();
    }

    public static File las2txt(File las) {
        try {
            File txt = new File(las.getAbsolutePath().substring(0, las.getAbsolutePath().lastIndexOf(".")) + ".txt");
            if (txt.exists()) {
                txt.delete();
            }
            Process p = Runtime.getRuntime().exec(las2txt.getAbsolutePath() + " -i " + las.getAbsolutePath() + " -o " + txt.getAbsolutePath() + " -parse xyz");
            p.waitFor();
            if (txt.exists()) {
                return txt;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public static File las2tin(File las) {
        try {
            File txt = new File(las.getAbsolutePath().substring(0, las.getAbsolutePath().lastIndexOf(".")) + ".shp");
            if (txt.exists()) {
                txt.delete();
            }
            Process p = Runtime.getRuntime().exec(las2tin.getAbsolutePath() + " -i " + las.getAbsolutePath() + " -o " + txt.getAbsolutePath() + " -last_only");
            p.waitFor();
            if (txt.exists()) {
                return txt;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public static ArrayList<Face> createFacesFromSHP(File shp) {
        ArrayList<Face> r = new ArrayList<Face>();
        try {
            Process p = Runtime.getRuntime().exec(shp2text.getAbsolutePath() + " " + shp.getAbsolutePath());
            BufferedInputStream buffer = new BufferedInputStream(p.getInputStream());
            BufferedReader commandResult = new BufferedReader(new InputStreamReader(buffer));
            String line = "";
            while ((line = commandResult.readLine()) != null) {
                if (line.startsWith("     (")) {
                    String meat1 = line.substring(line.indexOf("(") + 1, line.indexOf(")")).replaceAll(" ", "");
                    String nextLine = commandResult.readLine();
                    String meat2 = nextLine.substring(nextLine.indexOf("(") + 1, nextLine.indexOf(")")).replaceAll(" ", "");
                    String nextLine1 = commandResult.readLine();
                    String meat3 = nextLine1.substring(nextLine1.indexOf("(") + 1, nextLine1.indexOf(")")).replaceAll(" ", "");
                    r.add(new Face((new Vector3d(Double.valueOf(meat1.split(",")[0]) / scale, 
                            Double.valueOf(meat1.split(",")[1]) / scale, Double.valueOf(meat1.split(",")[2]) / scale)), new Vector3d(Double.valueOf(meat2.split(",")[0]) / scale, 
                            Double.valueOf(meat2.split(",")[1]) / scale, Double.valueOf(meat2.split(",")[2]) / scale), new Vector3d(Double.valueOf(meat3.split(",")[0]) / scale, 
                            Double.valueOf(meat3.split(",")[1]) / scale, Double.valueOf(meat3.split(",")[2]) / scale)));
                } else if (line.startsWith("   + (")) {
                    String meat1 = line.substring(line.indexOf("(") + 1, line.indexOf(")")).replaceAll(" ", "");
                    String nextLine = commandResult.readLine();
                    String meat2 = nextLine.substring(nextLine.indexOf("(") + 1, nextLine.indexOf(")")).replaceAll(" ", "");
                    String nextLine1 = commandResult.readLine();
                    String meat3 = nextLine1.substring(nextLine1.indexOf("(") + 1, nextLine1.indexOf(")")).replaceAll(" ", "");
                    r.add(new Face((new Vector3d(Double.valueOf(meat1.split(",")[0]) / scale, 
                            Double.valueOf(meat1.split(",")[1]) / scale, Double.valueOf(meat1.split(",")[2]) / scale)), new Vector3d(Double.valueOf(meat2.split(",")[0]) / scale, 
                            Double.valueOf(meat2.split(",")[1]) / scale, Double.valueOf(meat2.split(",")[2]) / scale), new Vector3d(Double.valueOf(meat3.split(",")[0]) / scale, 
                            Double.valueOf(meat3.split(",")[1]) / scale, Double.valueOf(meat3.split(",")[2]) / scale)));
                }
            }
        } catch (Exception e) {
        }
        return r;
    }

    public void save(ArrayList<Vector3f> arr) {
        BufferedWriter bufferedWriter = null;
        try {
            File oldsave = new File("Data/profile.sav");
            oldsave.delete();
            File newsave = new File("Data/profile.sav");
            newsave.createNewFile();
            bufferedWriter = new BufferedWriter(new FileWriter("Data/profile.sav"));
            for (Vector3f p : arr) {
                bufferedWriter.write(p.x + " " + p.y + " " + p.z);
                bufferedWriter.newLine();
            }
        } catch (Exception ex) {
        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.flush();
                    bufferedWriter.close();
                }
            } catch (Exception ex) {
            }
        }
    }

    public static Model load(File file) {
        try {
            Scanner s = new Scanner(file);
            Model m = new Model();
            String line;
            while (s.hasNextLine()) {
                line = s.nextLine();
                m.vertices.add(new Vector3d(Double.valueOf(line.split(" ")[0]) / scale,
                        Double.valueOf(line.split(" ")[1]) / scale,
                        Double.valueOf(line.split(" ")[2]) / scale));
            }
            s.close();
            return m;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static double dist3D(Vector3d f, Vector3d e) {
        return Math.sqrt(Math.pow((f.x - e.x), 2) + Math.pow((f.y - e.y), 2) + Math.pow((f.z - e.z), 2));
    }
}