package com.Frame;

import com.Data.Vector3d;
import com.Data.Face;
import com.Data.Model;
import com.Main;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluPerspective;

/**
 *
 * @author 13Baileykt
 */
public class Screen {

    private Frame f;
    private Canvas display_parent;
    private Thread gameThread;
    private float x, y, z, yaw, pitch, mouseSensitivity, yawPlayer;
    private boolean closeRequested;
    private ArrayList<Face> m;
    private Model model;
    private Vector3d loc;
    private float xOff, yOff, zOff;
    private boolean wire = false, cloud = false;
    private float sunLocX, sunLocY, sunLocZ;

    public void startGL(ArrayList<Face> m, Model model, Vector3d loc) {
        xOff = 0f;
        yOff = 0f;
        zOff = 0f;
        yaw = 0f;
        pitch = 0f;
        this.loc = loc;
        this.model = model;
        x = -(float) (loc.x + 10f);
        y = -(float) (loc.y);
        z = -(float) (loc.z);
        sunLocX = (float) (loc.x) + 3;
        sunLocY = (float) (loc.y);
        sunLocZ = (float) (loc.z) + 4;
        yawPlayer = (float) Math.toDegrees(Math.atan((-x - loc.x) / (-z - loc.z)));
        this.m = m;
        mouseSensitivity = 0.7f;
        closeRequested = false;
        display_parent = new Canvas();
        f = new Frame("LAS Viewer version --- " + Main.version);
        f.setResizable(false);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        f.setLocation((dim.width - Main.width) / 2, (dim.height - Main.height) / 2);
        try {
            f.setIconImage(ImageIO.read(Main.class.getResource("res/icon.png")));
        } catch (IOException ex) {
        }
        f.setLayout(new BorderLayout());
        f.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                display_parent.requestFocusInWindow();
            }
        });
        MenuBar mb = new MenuBar();
        mb.setFont(new Font("Monospaced", Font.PLAIN, 13));
        Menu item1 = new Menu("File");
        MenuItem one = new MenuItem("Exit                         ESC");
        one.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeRequested = true;
            }
        });
        item1.add(one);
        mb.add(item1);
        Menu item2 = new Menu("Options");
        MenuItem wireOn = new MenuItem("Turn on wire frame            T");
        wireOn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                wire = true;
            }
        });
        item2.add(wireOn);
        MenuItem wireOff = new MenuItem("Turn off wire frame           F");
        wireOff.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                wire = false;
            }
        });
        item2.add(wireOff);
        MenuItem reset = new MenuItem("Reset Object                  R");
        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                xOff = 0f;
                yOff = 0f;
                zOff = 0f;
                yaw = 0f;
                pitch = 0f;
            }
        });
        item2.add(reset);
        MenuItem cloudMod = new MenuItem("Convert to Cloud model        C");
        cloudMod.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cloud = true;
            }
        });
        item2.add(cloudMod);
        MenuItem tri = new MenuItem("Triangulate Faces             V");
        tri.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cloud = false;
            }
        });
        item2.add(tri);
        mb.add(item2);

        f.setMenuBar(mb);
        f.add(display_parent, BorderLayout.CENTER);
        gameThread = new Thread() {
            @Override
            public void run() {
                try {
                    Display.setParent(display_parent);
                    Display.setVSyncEnabled(true);
                    Display.create();
                    initGL();
                    display_parent.setVisible(true);
                    display_parent.setFocusable(true);
                    display_parent.requestFocus();
                    display_parent.setIgnoreRepaint(true);
                } catch (Exception e) {
                }
                f.setVisible(true);
//                Mouse.setGrabbed(true);
                while (!Display.isCloseRequested() && !closeRequested) {
                    long time = System.currentTimeMillis();
                    gameLoop();
                    long curr = System.currentTimeMillis();
                    long diff = (curr - time);
                    if (diff < 1000 / Main.max_fps) {
                        try {
                            Thread.sleep((1000 / Main.max_fps) - diff);
                        } catch (Exception ex) {
                        }
                    }
                }
                Display.destroy();
                System.exit(0);
            }
        };
        f.setPreferredSize(new Dimension(Main.width, Main.height));
        f.setMinimumSize(new Dimension(Main.width, Main.height));
        f.pack();
        gameThread.start();
    }

    public void initGL() {
        glMatrixMode(GL_PROJECTION);
        gluPerspective(45.0f, (float) Main.width / (float) Main.height, 0.1f, 900000.0f);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glShadeModel(GL_SMOOTH);
    }

    public void gameLoop() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (wire && !cloud) {
            glPolygonMode(GL_FRONT, GL_LINE);
            glPolygonMode(GL_BACK, GL_LINE);
        } else {
            glPolygonMode(GL_FRONT, GL_FILL);
            glPolygonMode(GL_BACK, GL_FILL);
        }

//        glEnable(GL_CULL_FACE);
//        glCullFace(GL_BACK);

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glTranslated(loc.x + xOff, loc.y + yOff, loc.z + zOff);
        glRotatef(yaw, 0f, 1f, 0f);
        glRotatef(pitch, 0f, 0f, 1f);
        glTranslated(-loc.x - xOff, -loc.y - yOff, -loc.z - zOff);

        if (!cloud) {
            glEnable(GL_LIGHTING);
            glEnable(GL_LIGHT0);
            glLightModel(GL_LIGHT_MODEL_AMBIENT, asFloatBuffer(new float[]{0.05f, 0.05f, 0.05f, 1f}));
            glLight(GL_LIGHT0, GL_DIFFUSE, asFloatBuffer(new float[]{.2f, .2f, .2f, 1}));
            glEnable(GL_COLOR_MATERIAL);
            glColorMaterial(GL_FRONT, GL_DIFFUSE);
        } else {
            glDisable(GL_LIGHTING);
            glDisable(GL_LIGHT0);
            glDisable(GL_COLOR_MATERIAL);
        }

        glColor3f(1f, 1f, 1f);
        if (cloud) {
            glBegin(GL_POINTS);
            for (Vector3d t : model.vertices) {
                glVertex3d(t.x + xOff, t.y + yOff, t.z + zOff);
            }
        } else {
            glBegin(GL_TRIANGLES);
            try {
                for (Face face : m) {
                    glVertex3d(face.vertex.x + xOff, face.vertex.y + yOff, face.vertex.z + zOff);
                    glVertex3d(face.vertex1.x + xOff, face.vertex1.y + yOff, face.vertex1.z + zOff);
                    glVertex3d(face.vertex2.x + xOff, face.vertex2.y + yOff, face.vertex2.z + zOff);
                }
            } catch (Exception e) {
            }
        }
        glEnd();

        if (!cloud) {
            glLight(GL_LIGHT0, GL_POSITION, asFloatBuffer(new float[]{sunLocX, sunLocY, sunLocZ, 1f}));
        }

        glPopMatrix();
        glRotatef(0f, 1f, 0f, 0f);

//        yawPlayer += Mouse.getDX() * mouseSensitivity;
//        pitch += -Mouse.getDY() * mouseSensitivity;
//        if (pitch > 80 || pitch < -80) {
//            pitch -= dy * mouseSensitivity;
//        }

//        System.out.println(yaw + " " + pitch);

//        if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
//            x -= Main.walkSpeed * (float) Math.sin(Math.toRadians(yaw));
//            z += Main.walkSpeed * (float) Math.cos(Math.toRadians(yaw));
//        }
//        if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
//            x += Main.walkSpeed * (float) Math.sin(Math.toRadians(yaw));
//            z -= Main.walkSpeed * (float) Math.cos(Math.toRadians(yaw));
//        }
//        if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
//            x -= Main.walkSpeed * (float) Math.sin(Math.toRadians(yaw - 90));
//            z += Main.walkSpeed * (float) Math.cos(Math.toRadians(yaw - 90));
//        }
//        if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
//            x -= Main.walkSpeed * (float) Math.sin(Math.toRadians(yaw + 90));
//            z += Main.walkSpeed * (float) Math.cos(Math.toRadians(yaw + 90));
//        }
//        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
//            y -= Main.walkSpeed;
//        }
//        if (Keyboard.isKeyDown(Keyboard.KEY_B)) {
//            y += Main.walkSpeed;
//        }
        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            closeRequested = true;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
            yOff++;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
            yOff--;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
            zOff++;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
            zOff--;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_Z)) {
            xOff++;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_X)) {
            xOff--;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_T)) {
            wire = true;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_F)) {
            wire = false;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
            xOff = 0f;
            yOff = 0f;
            zOff = 0f;
        }
        if (Mouse.isButtonDown(Mouse.getEventButton())) {
            yaw += Mouse.getDX() * mouseSensitivity;
            pitch += -Mouse.getDY() * mouseSensitivity;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
            cloud = true;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_V)) {
            cloud = false;
        }
//        if (Mouse.hasWheel()) {
////            System.out.println(Mouse.getDWheel() / 100);
//            scale += Mouse.getDWheel() / 100;
//        }
//        System.out.println(off1 + " " + off2 + " " + off3);

        glLoadIdentity();
        glRotatef(yawPlayer, 0.0f, 1.0f, 0.0f);
        glTranslatef(x, y, z);
        Display.update();
        Display.sync(Main.max_fps);
    }

    public FloatBuffer asFloatBuffer(float[] values) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(values.length);
        buffer.put(values);
        buffer.flip();
        return buffer;
    }
}