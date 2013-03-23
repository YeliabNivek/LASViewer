package com.Data;

import java.io.InputStream;
import java.util.Scanner;

/**
 *
 * @author j0ker
 */
public class OBJLoader {
    
    public static Model loadModel(InputStream in) {
        try {
            Scanner s = new Scanner(in);
            Model m = new Model();
            String line;
            while (s.hasNextLine()) {
                line = s.nextLine();
                if (line.startsWith("v ")) {
                    m.vertices.add(new Vector3d(Double.valueOf(line.split(" ")[1]),
                            Double.valueOf(line.split(" ")[2]), 
                            Double.valueOf(line.split(" ")[3])));
                } else if (line.startsWith("f ")) {
                    m.faces.add(new Face(new Vector3d(Double.valueOf(line.split(" ")[1]),
                            Double.valueOf(line.split(" ")[2]),
                            Double.valueOf(line.split(" ")[3]))));
                }
            }
            s.close();
            return m;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}