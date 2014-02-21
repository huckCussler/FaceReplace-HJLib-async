package frHJLib;

import java.util.concurrent.*;
import java.util.Comparator;

/**
 * Written by Trudy Firestone, Tony Tuttle -- Summer 2013
 *
 * Ported to HJLib by Tony Tuttle -- Feb 20, 2014
 */
public class SourceImageRegion extends ImageRegion {
    private PriorityBlockingQueue<ImageRegion> bestMatches;//stores the target regions in order of best match (better matches are at the top of the queue)--after a match has been proposed, it is removed from the queue.

    /**
     * Just calls the superclass constructor with the same argument
     */
    public SourceImageRegion(ImageRegion source)
    {
        super(source);
    }

    /**
     * Creates the queue containing the ordered best matches--bestMatches is null before
     * this point
     */
    public void createBestMatches(CopyOnWriteArrayList<ImageRegion> targets)
    {
        bestMatches = new PriorityBlockingQueue<>(50, new BestMatchComparator());
        bestMatches.addAll(targets);
    }

    /**
     * Returns the next best match or null if there isn't one ready and removes it from the
     * queue so that it won't be used again
     */
    public ImageRegion getNextBestMatch()
    {
        if(bestMatches == null)
        {
            return null;
        }
        try
        {
            return bestMatches.take();
        }
        catch(InterruptedException e)
        {
            System.out.println(e.getMessage());
            return null;
        }
    }



    /**
     * Creates a comparator for the ImageRegion class that depends on the match score
     * of each object
     */
    class BestMatchComparator implements Comparator<ImageRegion>
    {
        /**
         * According to the Java API this method should compare its two arguments for
         * and return a negative integer if source is less than target, 0 if target is
         * equal to source, and a positive integer if the source is greater than target.
         */

        public int compare(ImageRegion target1, ImageRegion target2)
        {
            double answer = SourceImageRegion.this.computeMatchScore(target2) - SourceImageRegion.this.computeMatchScore(target1);
            if(answer > 0)
                return -1;
            else if(answer < 0)
                return 1;
            else
                return 0;
        }
    }
}
