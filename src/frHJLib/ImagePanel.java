package frHJLib;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * Written by Trudy Firestone -- Summer 2013
 *
 * Ported to HJLib by Tony Tuttle -- Feb 20, 2014
 */
public class ImagePanel extends JPanel{
    private static final long serialVersionUID = 1L;
    private BufferedImage image;
    private File imageFile;
    private File rectangleFile;
    private ArrayList<Rectangle> rectangles;
    private HashMap<Color, Rectangle> rectanglesThreads;
    private int x, y;
    private double scale;

    public ImagePanel()
    {
        super();
        rectanglesThreads = new HashMap<>();
    }

    public int  getNumRectangles()
    {
        return rectangles.size();
    }
    public void setImageFile(File imageFile)
    {
        if(imageFile != null && imageFile != this.imageFile)
        {
            this.imageFile = imageFile;
            try {
                image = ImageIO.read(imageFile);
            } catch (IOException e) {
                image = null;
                e.printStackTrace();
            }
        }
    }

    public void setRectangleFile(File rectangleFile)
    {
        if(rectangleFile == null || rectangleFile == this.rectangleFile)
            return;
        this.rectangleFile =rectangleFile;
        try {
            Scanner reader = new Scanner(rectangleFile);
            rectangles = new ArrayList<>();
            while(reader.hasNextLine())
            {
                String rectParams = reader.nextLine();
                String[] args = rectParams.split("[ ,]+");
                rectangles.add(new Rectangle(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]),  Integer.parseInt(args[3])));
            }

        } catch (FileNotFoundException e) {
            rectangles = null;
            e.printStackTrace();
        }
    }

    public File getRectangleFile()
    {
        return rectangleFile;
    }

    public File getImageFile()
    {
        return imageFile;
    }

    @Override
    public void paint(Graphics g)
    {
        Graphics2D g2= (Graphics2D)g;
        if(image !=null)
        {
            //BufferedImage tempImage = new BufferedImage(image.getColorModel(), image.copyData(null), image.getColorModel().isAlphaPremultiplied(), null);


            double widthPercent = (double)this.getWidth()/image.getWidth();
            double heightPercent = (double)this.getHeight()/image.getHeight();
            scale = (widthPercent > heightPercent) ? heightPercent : widthPercent;
            if(scale > 1)
                scale = 1;
            x =(int)( (this.getWidth()-image.getWidth()*scale)/2);

            y =(int)( (this.getHeight()-image.getHeight()*scale)/2) ;
            g2.drawImage(image, x, y,(int)(image.getWidth()*scale),(int)( image.getHeight()*scale), this);
            if(rectangles != null)
            {
                g2.setColor(Color.CYAN);
                g2.setStroke(new BasicStroke(3.0f));
                for (Rectangle rect : rectangles)
                    g2.drawRect((int) (rect.x * scale + x), (int) (rect.y * scale + y), (int) (rect.width * scale), (int) (rect.height * scale));

                for (Color temp : rectanglesThreads.keySet()) {
                    Rectangle rectangle = rectanglesThreads.get(temp);
                    g2.setColor(new Color(temp.getRed(), temp.getGreen(), temp.getBlue(), 80));
                    g2.fillRect((int) (rectangle.x * scale + x), (int) (rectangle.y * scale + y), (int) (rectangle.width * scale), (int) (rectangle.height * scale));
                    g2.setColor(temp);
                    g2.drawRect((int) (rectangle.x * scale + x), (int) (rectangle.y * scale + y), (int) (rectangle.width * scale), (int) (rectangle.height * scale));
                }
            }


        }
        g.setColor(Color.BLACK);
        paintBorder(g);
    }
    /**
     * Highlights and returns the rectangle that contains the
     * @param x = the x-coordinate
     * @param y = the y-coordinate
     */
    public Rectangle getRectangle(int x, int y)
    {
        x =(int)( x*(1/scale) - this.x);
        y = (int)(y*(1/scale) - this.y);

        for (Rectangle rectangle : rectangles) {
            if (rectangle.contains(x, y)) {
                if (!rectanglesThreads.containsValue(rectangle)) {
                    rectanglesThreads.clear();
                    rectanglesThreads.put(Color.GREEN, rectangle);
                }
                return rectangle;
            }
        }
        rectanglesThreads.clear();
        return null;
    }

    public void setRectangle(Rectangle rectangle, Color color)
    {
        if(rectangle == null)
        {
            rectanglesThreads.remove(color);
            return;
        }
        rectanglesThreads.put(color, rectangle);
    }
}
