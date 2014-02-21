package frHJLib;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Written by Trudy Firestone -- Summer 2013
 *
 * Ported to HJLib by Tony Tuttle -- 2/18/14
 *
 * FaceReplace is the GUI component of the program.
 */
public class FaceReplace extends JFrame implements ActionListener, ChangeListener, MouseMotionListener, MouseListener{
    private static final long serialVersionUID = 1L;
    private ImagePanel targetImagePanel;
    private boolean inProgress;
    private JButton targetRectButton;
    private JButton sourceRectButton;
    private ImagePanel sourceImagePanel;
    private JButton sourceImageButton;
    private JButton targetImageButton;
    private JButton startButton;
    private Main mainRunning;
    private HashMap<Rectangle, Rectangle> mapping;
    private JProgressBar[] stagesProgress;
    private Color[] colors;

    /**
     * @param mainRunning is the main class of the FaceReplace program.
     */
    public FaceReplace(Main mainRunning)
    {
        this.mainRunning = mainRunning;
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        colors = new Color[]{Color.RED, Color.BLUE, Color.GREEN, Color.CYAN, Color.MAGENTA, Color.GRAY, Color.WHITE, Color.YELLOW};
        JPanel mainPanel = new JPanel();
        this.setTitle("Habanero Java Face Replace");
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        GridBagLayout mainPanelLayout = new GridBagLayout();
        mainPanelLayout.rowWeights = new double[] {0.98, 0.01, 0.01, 0.1};
        mainPanelLayout.columnWeights = new double[] {1.0, 1.0};
        mainPanel.setLayout(mainPanelLayout);

        sourceImagePanel = new ImagePanel();
        sourceImagePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        sourceImagePanel.addMouseMotionListener(this);
        sourceImagePanel.addMouseListener(this);
        JPanel imagePanel = new JPanel(new GridLayout(1,2));
        mainPanel.add(imagePanel, new GridBagConstraints(0, 0, 2,1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        targetImagePanel = new ImagePanel();
        targetImagePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        imagePanel.add(sourceImagePanel);
        imagePanel.add(targetImagePanel);
        sourceImageButton =  new JButton();
        mainPanel.add(sourceImageButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        sourceImageButton.setText("Get Source Image");
        sourceImageButton.addActionListener(this);

        targetImageButton = new JButton();
        mainPanel.add(targetImageButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        targetImageButton.setText("Get Target Image");
        targetImageButton.addActionListener(this);

        sourceRectButton = new JButton();
        sourceRectButton.setEnabled(false);
        mainPanel.add(sourceRectButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        sourceRectButton.setText("Get Source Rectangles");
        sourceRectButton.addActionListener(this);

        targetRectButton = new JButton();
        targetRectButton.setEnabled(false);
        mainPanel.add(targetRectButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        targetRectButton.setText("Get Target Rectangles");
        targetRectButton.addActionListener(this);

        JPanel stages = new JPanel();
        GridBagLayout temp = new GridBagLayout();
        temp.rowWeights = new double[]{0.9, 0.01};
        temp.columnWeights = new double[]{1, 1};
        stages.setLayout(temp);
        JTabbedPane stagesInfo = new JTabbedPane();
        stages.add(stagesInfo, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        startButton = new JButton("Start");
        stages.add(startButton, new GridBagConstraints(0,1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        mainPanel.add(stages, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        startButton.addActionListener(this);
        startButton.setEnabled(false);
        JPanel stage1 = new JPanel(new BorderLayout());
        stagesInfo.addTab("Stage 1", stage1);
        stage1.add(new JLabel("Load rectangles."));
        JPanel stage2 = new JPanel(new BorderLayout());
        stage2.add(new JLabel("Create Image Regions from Rectangles"));
        stagesInfo.addTab("Stage 2", stage2);
        JPanel stage3 = new JPanel(new BorderLayout());
        stage3.add(new JLabel("Compute average color of each Image Region"));
        stagesInfo.addTab("Stage 3", stage3);
        JPanel stage4 = new JPanel(new BorderLayout());
        stage4.add(new JLabel("Find Matches"));
        stagesInfo.addTab("Stage 4", stage4);

        JPanel stageProgress = new JPanel();
        GridBagLayout stageProgressLayout = new GridBagLayout();
        stageProgressLayout.columnWeights = new double[]{0.1, 0.9};
        stageProgressLayout.rowWeights = new double[]{1,1,1,1,1,1};
        stageProgress.setLayout(stageProgressLayout);
        mainPanel.add(stageProgress, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        stageProgress.add(new JLabel("Total Progress: "), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        stagesProgress = new JProgressBar[5];
        stagesProgress[0] = new JProgressBar();
        stageProgress.add(stagesProgress[0], new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));


        stageProgress.add(new JLabel("Stage 1: "), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        stagesProgress[1]= new JProgressBar();
        stageProgress.add(stagesProgress[1], new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        stageProgress.add(new JLabel("Stage 2: "), new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        stagesProgress[2] = new JProgressBar();
        stageProgress.add(stagesProgress[2], new GridBagConstraints(1, 2, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        stageProgress.add(new JLabel("Stage 3: "), new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        stagesProgress[3] = new JProgressBar();
        stageProgress.add(stagesProgress[3], new GridBagConstraints(1, 3, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        stageProgress.add(new JLabel("Stage 4: "), new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        stagesProgress[4] = new JProgressBar();
        stageProgress.add(stagesProgress[4], new GridBagConstraints(1, 4, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        for(int i = 0; i< 5; i++)
        {
            stagesProgress[i].setMinimum(0);
        }
        stageProgress.add(new JLabel("Speed Slide: "), new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, 0, 100,50);
        speedSlider.setMinorTickSpacing(10);
        speedSlider.setMajorTickSpacing(25);
        speedSlider.setPaintLabels(true);
        speedSlider.setPaintTicks(true);
        stageProgress.add(speedSlider, new GridBagConstraints(1, 5, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        speedSlider.addChangeListener(this);

        this.pack();
        this.setSize(1000,800);
        this.setVisible(true);
    }

    /**
     * @return  an array containing the following
     * [sourceImageFile, targetImageFile, sourceRectangleFile, targetRectangleFile]
     */
    public File[] getFiles()
    {
        return new File[]{sourceImagePanel.getImageFile(), targetImagePanel.getImageFile(), sourceImagePanel.getRectangleFile(), targetImagePanel.getRectangleFile()};
    }

    private void reset()
    {
        mapping = null;
        for(int i = 0; i < 5; i++)
        {
            stagesProgress[i].setValue(0);
        }
    }

    @Override
    public void actionPerformed(ActionEvent action)
    {
        if(action.getSource() == sourceImageButton)
        {
            File f = getFile(false);
            if(f != null)
            {
                reset();
                sourceImagePanel.setImageFile(f);
                sourceRectButton.setEnabled(true);
            }

        }
        else if(action.getSource() == targetImageButton)
        {
            File f = getFile(false);
            if(f != null)
            {
                reset();
                targetImagePanel.setImageFile(f);
                targetRectButton.setEnabled(true);

            }
        }
        else if(action.getSource() == sourceRectButton)
        {
            File f = getFile(true);
            if(f != null)
            {
                reset();
                sourceImagePanel.setRectangleFile(f);
                mapping = null;
            }
        }
        else if(action.getSource() == targetRectButton)
        {
            File f = getFile(true);
            if(f != null)
            {
                reset();
                targetImagePanel.setRectangleFile(f);
                mapping = null;
            }
        }
        else if(action.getSource() == startButton)
        {
            startButton.setEnabled(false);
            sourceImageButton.setEnabled(false);
            sourceRectButton.setEnabled(false);
            targetImageButton.setEnabled(false);
            targetRectButton.setEnabled(false);
            reset();
            mainRunning.start();
            inProgress = true;
        }
        if(sourceImagePanel.getRectangleFile() != null && targetImagePanel.getRectangleFile() != null && !inProgress)
        {
            startButton.setEnabled(true);
            int n = sourceImagePanel.getNumRectangles();
            stagesProgress[0].setMaximum(n*7);
            stagesProgress[1].setMaximum(n*2);
            stagesProgress[2].setMaximum(n*2);
            stagesProgress[3].setMaximum(n*2);
            stagesProgress[4].setMaximum(n);
        }
        repaint();
    }

    /**
     * Returns the string representing the path to the requested file
     *
     * @return the file or null if the operation was cancelled.
     */
    private File getFile(boolean text)
    {
        JFileChooser chooser = new JFileChooser("imgs");
        if(text)
            chooser.setFileFilter(new FileNameExtensionFilter("text files", "txt"));
        else
            chooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpeg", "jpg", "png", "gif"));
        do
        {
            int result = chooser.showOpenDialog(this);
            if(result == JFileChooser.CANCEL_OPTION)
            {
                return null;
            }
            else
            {
                File f = chooser.getSelectedFile();
                if(f.canRead())
                {
                    return f;
                }

            }
        }while(true);
    }


    public void setProgress(int stageNumber)
    {
        synchronized (new Object())
        {
            stagesProgress[stageNumber].setValue(stagesProgress[stageNumber].getValue()+1);
            stagesProgress[0].setValue(stagesProgress[0].getValue()+1);
            repaint();
        }
    }

    public void setCurrentRect(Rectangle rectangle, int thread, boolean sourceImage)
    {
        if(sourceImage)
        {
            synchronized(new Object())
            {
                sourceImagePanel.setRectangle(rectangle, colors[thread%colors.length]);
            }
        }
        else
        {
            synchronized(new Object())
            {
                targetImagePanel.setRectangle(rectangle, colors[thread%colors.length]);
            }
        }

    }

    public void setMap(HashMap<Rectangle, Rectangle> regions)
    {
        this.mapping = regions;
        inProgress = false;
        startButton.setEnabled(true);
        startButton.setEnabled(true);
        sourceImageButton.setEnabled(true);
        sourceRectButton.setEnabled(true);
        targetImageButton.setEnabled(true);
        targetRectButton.setEnabled(true);

        repaint();
        //System.out.println("Done Yay!");
    }

    @Override
    public void stateChanged(ChangeEvent arg0)
    {
        JSlider slider =(JSlider) arg0.getSource();
        if(!slider.getValueIsAdjusting())
        {
            mainRunning.setSpeed(slider.getValue());
        }
    }

    @Override
    public void mouseDragged(MouseEvent arg0)
    {
        mouseMoved(arg0);

    }

    @Override
    public void mouseMoved(MouseEvent mouse)
    {
        if(mapping != null)
        {
            Rectangle rectangle = mapping.get(sourceImagePanel.getRectangle(mouse.getX(), mouse.getY()));
            targetImagePanel.setRectangle(rectangle, Color.GREEN);
        }
        repaint();


    }

    @Override
    public void mouseClicked(MouseEvent arg0) {}

    @Override
    public void mouseEntered(MouseEvent arg0) {}

    @Override
    public void mouseExited(MouseEvent arg0)
    {
        if(mapping != null)
        {
            sourceImagePanel.setRectangle(null, Color.GREEN);
            targetImagePanel.setRectangle(null, Color.GREEN);
            repaint();
        }

    }

    @Override
    public void mousePressed(MouseEvent arg0) {}

    @Override
    public void mouseReleased(MouseEvent arg0) {}
}
