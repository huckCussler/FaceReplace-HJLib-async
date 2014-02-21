package frHJLib;

import java.util.*;
import java.awt.*;
import java.io.*;
import java.util.concurrent.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import static edu.rice.hj.Module1.*;

/**
 * Written by Trudy Firestone, Tony Tuttle -- Summer 2013
 *
 * Ported to HJLib by Tony Tuttle -- Feb 19, 2014
 */
public class Master {
    private boolean complete;
    // a queue that contains rectangles--it represents the producer-consumer shared queue for the step from
    // reading in the rectangle file to loading image regions
    private LinkedBlockingQueue<Rectangle> rects;
    // a queue that contains ImageRegions--it represents the producer-consumer shared queue for the step from
    // loading ImageRegions to calculating their heuristics.
    private LinkedBlockingQueue<ImageRegion> imgRgns;
    private CopyOnWriteArrayList<ImageRegion> completes;
    public static long delayInMillis = 500;
    public Main theMain;
    public boolean source;

    /**
     * Creates a Master object and starts 3 asynchronous calls to implement the first 2 stages
     */
    public Master(File imageFile, File rectangleFile, Main theMain, boolean source)
    {
        this.source = source;
        rects = new LinkedBlockingQueue<>();
        imgRgns = new LinkedBlockingQueue<>();
        completes = new CopyOnWriteArrayList<>();
        this.theMain = theMain;
        /* finish scope for all three async calls in Main.java/startMethod */
        async(() -> getRects(rectangleFile));
        async(()->createImageRegions(imageFile));
        async(this::calculateHeuristics);
    }

    /**
     * Returns the ConcurrentLinkedQueue representing the completed ImageRegions from
     * stages 1 and 2.
     * This is a blocking operation. It will wait until the queue is completed before
     * returning.
     */
    public CopyOnWriteArrayList<ImageRegion> getComplete()
    {
        while(!complete){
            try{
                Thread.sleep(100);
            }
            catch(InterruptedException e){
                System.err.println("InterruptedException caught in Master.java/getComplete");
            }
        }
        return completes;
    }

    /**
     * Completes part a of Stage 1 by reading in the file specified by file path and
     * storing the rectangles found in the file in the shared producer-consumer rectangle
     * queue.
     */
    private void getRects(File file)
    {
        try
        {
            Scanner read = new Scanner(file);
            while(read.hasNextLine())
            {
                String rectParams = read.nextLine();
                String[] args = rectParams.split("[ ,]+");
                Rectangle rectangle = new Rectangle(Integer.parseInt(args[0]), Integer.parseInt(args[1]),
                        Integer.parseInt(args[2]), Integer.parseInt(args[3]));

                theMain.setRect(rectangle, (source) ? 0 : 1, source);
                try{
                    rects.put(rectangle);
                }
                catch(InterruptedException e){
                    System.err.println("Caught InterruptedException in Master/getRects");
                }
                theMain.setProgress(1);


                try{Thread.sleep(delayInMillis);}
                catch(Exception e){
                    System.err.println("Caught InterruptedException in Master/getRects");
                }

            }
            //add a poison element to alert the consumer that getRects is done producing
            try{
                rects.put(new Rectangle(0,0,0,0));
            }
            catch(InterruptedException e){
                System.err.println("Caught InterruptedException in Master/getRects.");
            }
            theMain.setRect(null, (source) ? 0 : 1, source);
        }
        catch(FileNotFoundException e)
        {
            System.err.println("Rectangle file not found.");
        }
    }

    /**
     * Completes part b of Stage 1 by reading in the file specified by filepath and the
     * rectangle specified by the shared queue to create ImageRegions which are stored
     * in the producer-consumer ImageRegion queue.
     */
    private void createImageRegions(File filepath)
    {
        Rectangle params = null;
        try{
            BufferedImage bigImg = ImageIO.read(filepath);

            try{
                params = rects.take();
            }
            catch(InterruptedException e){
                System.err.println("Caught InterruptedException in Master/createImageRegions");
            }
            assert params != null;
            while(!params.isEmpty())
            {
                BufferedImage img = bigImg.getSubimage((int)params.getX(), (int)params.getY(),
                        (int)params.getWidth(), (int)params.getHeight());
                imgRgns.put(new ImageRegion(img, params));
                theMain.setRect(params, (source) ? 2 : 3, source);
                theMain.setProgress(2);
                params = rects.take();

                // ARTIFICIALLY FABRICATED DELAY
                Thread.sleep(delayInMillis);
            }
            imgRgns.put(new ImageRegion(null));

            theMain.setRect(null, (source) ? 2 : 3, source);
        }
        catch(IOException e){
            System.out.println("Image file not found.");
        }
        catch(InterruptedException e){
            System.err.println("Caught InterruptedException in Master/createImageRegions");
        }
    }

    /**
     * Completes Stage 2 of the FaceReplace algorithm, by reading from the shared queue
     * of ImageRegions and calculating the heuristic for the algorithm. It then adds the
     * completed ImageRegion to the collection and sets complete to true when it is
     * complete.
     */
    private void calculateHeuristics()
    {
        try
        {
            ImageRegion imgRgn = imgRgns.take();
            while(imgRgn.getImg() != null){
                imgRgn.setAvgColor(imgRgn.computeHeuristic());
                completes.add(imgRgn);
                theMain.setRect(imgRgn.getRectangle(), (source) ? 4 : 5, source);
                theMain.setProgress(3);
                imgRgn = imgRgns.take();

                // ARTIFICIALLY FABRICATED DELAY
                Thread.sleep(delayInMillis);
            }
            theMain.setRect(null, (source) ? 4 : 5, source);
            complete = true;
        }
        catch(InterruptedException e)
        {
            System.out.println("calculateHeuristics() method in Master.hj");
        }
    }
}
