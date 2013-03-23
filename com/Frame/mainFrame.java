package com.Frame;

import com.Data.Face;
import com.Data.Model;
import com.Data.Vector3d;
import com.Main;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.UIManager;

/**
 *
 * @author j0ker
 */
public class mainFrame extends JFrame {

    public static JButton run, jb;
    public static JLabel progress;
    public static JProgressBar bar;
    public static JSlider scale;

    public mainFrame() {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e) {
        }
        setTitle("LAS viewer");
        setResizable(false);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((dim.width - Main.width) / 2, (dim.height - Main.height) / 2);
        try {
            setIconImage(ImageIO.read(Main.class.getResource("res/icon.png")));
        } catch (IOException ex) {
        }
        setSize(500, 500);
        setLayout(null);
//        final JLabel scale1 = new JLabel();
//        scale1.setBounds(100, 410, 100, 20);
//        add(scale1);
//        scale = new JSlider();
//        scale.addChangeListener(new ChangeListener() {
//            @Override
//            public void stateChanged(ChangeEvent e) {
//                scale1.setText(((double) scale.getValue() / 250.0) + "");
//            }
//        });
//        scale.setMinimum(1);
//        scale.setMaximum(1000);
//        scale.setBounds(10, 450, 480, 20);
//        scale.setValue(250);
//        JLabel label = new JLabel("Scale of Model: ");
//        label.setBounds(10, 410, 100, 20);
//        JLabel warning = new JLabel("WARNING: It is recomended that you don't change the scale from 1.0");
//        warning.setBounds(10, 430, 400, 20);
//        add(label);
//        add(warning);
//        add(scale);

        progress = new JLabel();
        progress.setBounds(175, 130, 200, 20);
        add(progress);

        bar = new JProgressBar();
        bar.setMinimum(0);
        bar.setMaximum(7);
        bar.setBounds(10, 160, 480, 30);
        add(bar);

        jb = new JButton("Browse for LAS file");
        jb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new openFile();
            }
        });
        jb.setBounds(175, 50, 150, 20);

        run = new JButton("No file selected");
        run.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ee) {
                if (run.getText().compareTo("No file selected") != 0) {
                    jb.setEnabled(false);
//                    scale.setEnabled(false);
                    new Thread() {
                        @Override
                        public void run() {
                            progress.setText("Unloading resources...");
                            bar.setValue(1);
                            try {
                                InputStream src = (InputStream) Main.class.getResource("res/las2txt.exe").openStream();
                                Main.las2txt = File.createTempFile("las2txt", ".exe");
                                FileOutputStream out = new FileOutputStream(Main.las2txt);
                                byte[] temp = new byte[32768];
                                int rc;
                                while ((rc = src.read(temp)) > 0) {
                                    out.write(temp, 0, rc);
                                }
                                src.close();
                                out.close();
                                Main.las2txt.deleteOnExit();
                            } catch (Exception e) {
                            }
                            try {
                                InputStream src = (InputStream) Main.class.getResource("res/txt2las.exe").openStream();
                                Main.txt2las = File.createTempFile("txt2las", ".exe");
                                FileOutputStream out = new FileOutputStream(Main.txt2las);
                                byte[] temp = new byte[32768];
                                int rc;
                                while ((rc = src.read(temp)) > 0) {
                                    out.write(temp, 0, rc);
                                }
                                src.close();
                                out.close();
                                Main.txt2las.deleteOnExit();
                            } catch (Exception e) {
                            }
                            try {
                                InputStream src = (InputStream) Main.class.getResource("res/las2tin.exe").openStream();
                                Main.las2tin = File.createTempFile("las2tin", ".exe");
                                FileOutputStream out = new FileOutputStream(Main.las2tin);
                                byte[] temp = new byte[32768];
                                int rc;
                                while ((rc = src.read(temp)) > 0) {
                                    out.write(temp, 0, rc);
                                }
                                src.close();
                                out.close();
                                Main.las2tin.deleteOnExit();
                            } catch (Exception e) {
                            }
                            try {
                                InputStream src = (InputStream) Main.class.getResource("res/shp2text.exe").openStream();
                                Main.shp2text = File.createTempFile("shp2text", ".exe");
                                FileOutputStream out = new FileOutputStream(Main.shp2text);
                                byte[] temp = new byte[32768];
                                int rc;
                                while ((rc = src.read(temp)) > 0) {
                                    out.write(temp, 0, rc);
                                }
                                src.close();
                                out.close();
                                Main.shp2text.deleteOnExit();
                            } catch (Exception e) {
                            }
                            try {
//                                Main.scale = (double) scale.getValue() / 250.0;
                                progress.setText("Triangulating faces");
                                bar.setValue(2);
                                File f = Main.las2tin(Main.curr);
                                progress.setText("Converting file to readable ASCII format");
                                bar.setValue(3);
                                ArrayList<Face> r = Main.createFacesFromSHP(f);
                                progress.setText("Getting cloud model from file");
                                bar.setValue(4);
                                File f1 = Main.las2txt(Main.curr);
                                progress.setText("Putting values into Array");
                                bar.setValue(5);
                                Model m = Main.load(f1);
                                progress.setText("Calculating best postition for camera");
                                bar.setValue(6);
                                Vector3d ap = r.get(0).vertex;
                                for (Face ff : r) {
                                    Vector3d temp = new Vector3d((ff.vertex.x + ap.x) / 2.0,
                                            (ff.vertex.y + ap.y) / 2.0,
                                            (ff.vertex.z + ap.z) / 2.0);
                                    ap = temp;
                                }
                                progress.setText("Done!");
                                bar.setValue(7);
                                dispose();
                                new Screen().startGL(r, m, ap);
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(null, "Could not read indicated file", "Error", JOptionPane.ERROR_MESSAGE);
                                jb.setEnabled(true);
//                                scale.setEnabled(true);
                                progress.setText("");
                                bar.setValue(0);
                                run.setText("No file selected");
                                jb.setText("Browse for LAS file");
                            }
                        }
                    }.start();
                }
            }
        });
        run.setBounds(175, 100, 150, 20);
        add(run);
        add(jb);
        setVisible(true);
    }
}