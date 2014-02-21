package frHJLib;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Written by Trudy Firestone, Tony Tuttle -- Summer 2013
 *
 * Ported to HJLib by Tony Tuttle -- Feb 20, 2014
 */
public class ImageRegion {
    private BufferedImage image;    /* The image that represents this ImageRegion */
    private ImageRegion match;      /* The best match from the other set of ImageRegions */
    private Color avgColor;         /* The average color of this image region */
    private Rectangle rectangle;    /* The Rectangle corresponding to where this ImageRegion is located */

    /**
     * Creates an image region that contains the image given.
     * @param image : the image to be stores as an image region
     */
    public ImageRegion(BufferedImage image, Rectangle rect)
    {
        this.image = image;
        this.rectangle = rect;
        this.match = null;
    }

    /**
     * Copy constructor for use with subclass(es) (e.g. SourceImageRegion)
     * @param imgRgn -- the ImageRegion we are copying
     */
    public ImageRegion(ImageRegion imgRgn){
        if(imgRgn == null)
            return;
        this.image = imgRgn.getImg();
        this.rectangle = imgRgn.getRectangle();
        this.match = imgRgn.getMatch();
        this.avgColor = imgRgn.getAvgColor();
    }

    public Rectangle getRectangle(){return this.rectangle;}

    public BufferedImage getImg(){return this.image;}

    public ImageRegion getMatch(){return this.match;}

    public void setMatch(ImageRegion _match){this.match = _match;}

    public Color getAvgColor(){return this.avgColor;}

    public void setAvgColor(Color avg){this.avgColor = avg;}

    /**
     * Computes the match score as a double between this imageRegion and another.
     * @param tgt : the ImageRegion being compared with this one
     * @return the double representing the match between the two. A larger score represents a worse match
     */
    public double computeMatchScore(ImageRegion tgt)
    {
        double redScore = Math.abs(tgt.getAvgColor().getRed() - this.getAvgColor().getRed());
        double grnScore = Math.abs(tgt.getAvgColor().getGreen() - this.getAvgColor().getGreen());
        double bluScore = Math.abs(tgt.getAvgColor().getBlue() - this.getAvgColor().getBlue());
        return redScore + grnScore + bluScore;
    }

    /**
     * Stores the average color of the image in the ImageRegion and returns it.
     * @return the average color of this image region
     */
    public Color computeHeuristic()
    {

        BufferedImage img = this.getImg();
        int[] vals = new int[3];

        for(int x=0; x<img.getWidth(); x++){
            for(int y=0; y<img.getHeight(); y++){
                int color = img.getRGB(x,y);
                int red = (color >> 16) & 0xFF;
                int grn = (color >> 8) & 0xFF;
                int blu = color & 0xFF;

                // sum the red, green, and blue values
                vals[0] += red;
                vals[1] += grn;
                vals[2] += blu;
            }
        }

        int pixCnt = img.getWidth() * img.getHeight();
        return new Color(vals[0]/pixCnt, vals[1]/pixCnt, vals[2]/pixCnt);
    }
}
