package ru.prestu.authident.web;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public class ExampleUtils {

    @SuppressWarnings("serial")
    public static class ExampleFrame extends JFrame {

        /**
         * Returns the main panel which should be printed by the screenshot action.
         * <p>
         * By default, it returns the content pane of this frame, but can be overriden
         * in case the frame has a global scroll pane which would cut off any offscreen content. 
         *
         * @return the main panel to print
         */
        public Component getMainPanel() {
            return getContentPane();
        }
    }

    public static void showExampleFrame(final ExampleFrame frame) {
        Runnable r = new Runnable() {
            public void run() {
                JMenuItem screenshot = new JMenuItem("Screenshot (png)");
                screenshot.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK));
                screenshot.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
                        if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                            File file = fileChooser.getSelectedFile();
                            BufferedImage img = getScreenShot(frame.getMainPanel());
                            try {
                                // write the image as a PNG
                                ImageIO.write(img, "png", file);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                JMenuItem exit = new JMenuItem("Exit");
                exit.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        System.exit(0);
                    }
                });

                JMenu menu = new JMenu("File");
                menu.add(screenshot);
                menu.add(exit);
                JMenuBar mb = new JMenuBar();
                mb.add(menu);
                frame.setJMenuBar(mb);

                frame.setLocationRelativeTo(null);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        };
        SwingUtilities.invokeLater(r);
    }

    private static BufferedImage getScreenShot(Component component) {
        BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_RGB);
        // call the Component's paint method, using the Graphics object of the image.
        component.paint(image.getGraphics());
        return image;
    }

}
