package frHJLib;

import java.util.concurrent.*;
import static edu.rice.hj.Module1.*;

/**
 * Written by Trudy Firestone, Tony Tuttle -- Summer 2013
 *
 * Ported to HJLib by Tony Tuttle -- Feb 20, 2014
 */
public class MatchMaker {
    ConcurrentLinkedQueue<SourceImageRegion> source; // a queue representing the source images that haven't been matched yet.
    ConcurrentHashMap<SourceImageRegion, ImageRegion> matchedPairs; //a map of the sources to the targets with the best pairings according to stable marriages
    Main theMain;

    /**
     * Creates a matchmaker object that will match the source images to the target images
     */
    public MatchMaker(CopyOnWriteArrayList<SourceImageRegion> src, CopyOnWriteArrayList<ImageRegion> tgt, Main theMain)
    {
        this.theMain = theMain;
        this.source = new ConcurrentLinkedQueue<>(src);
        finish(()->{
            //loops through each source image region and adds a sorted version of the target list to it
            for (SourceImageRegion region : source) {
                async(() -> region.createBestMatches(tgt));
            }
        });
    }

    /**
     * Returns a hash map representing the best pairs according to the
     * stable marriage problem
     */
    public ConcurrentHashMap<SourceImageRegion, ImageRegion> makeMatches()
    {
        //faceReplace.setProgress(4);
        //if already created just return it
        if(matchedPairs != null)
            return matchedPairs;
        matchedPairs = new ConcurrentHashMap<>();
        //keep going while there are still unmatched SourceImageRegions
        while(!source.isEmpty()){
            SourceImageRegion curr = source.peek();
            ImageRegion matchWanted = curr.getNextBestMatch();
            // if the SrcImgRgn's most desired match is not currently matched, then match them
            theMain.setRect(curr.getRectangle(), 0, true);
            theMain.setRect(matchWanted.getRectangle(), 0, false);
            if(matchWanted.getMatch() == null){
                setBestMatch(curr, matchWanted);
                matchedPairs.put(source.poll(), matchWanted);
                this.theMain.setProgress(4);
            }
            // otherwise, if the desired match prefers THIS SrcImgRgn, then make that change
            else if(curr.computeMatchScore(matchWanted) < matchWanted.getMatch().computeMatchScore(matchWanted))
            {
                //will be a SourceImageRegion
                source.offer((SourceImageRegion)matchWanted.getMatch());
                matchedPairs.remove(matchWanted.getMatch());
                unsetBestMatch(matchWanted,matchWanted.getMatch());
                setBestMatch(curr, matchWanted);
                matchedPairs.put(source.poll(), matchWanted);
                this.theMain.setProgress(4);
            }
            // ARTIFICIALLY FABRICATED DELAY
            try{Thread.sleep(Master.delayInMillis);}
            catch(Exception e){
                System.err.println("Caught Exception in MatchMaker/makeMatches");
            }
        }
        theMain.setRect(null, 0, true);
        theMain.setRect(null, 0, false);
        return matchedPairs;
    }

    /**
     * Allows the ImageRegion that matches this one best to be set.
     * @param src : the SrcImgRgn we are matching TO
     * @param tgt : the ImageRegion we are setting with src
     */
    public static void setBestMatch(ImageRegion src, ImageRegion tgt)
    {
        src.setMatch(tgt);
        tgt.setMatch(src);
    }

    /**
     * Unsets a current match if a better match is found.
     */
    public static void unsetBestMatch(ImageRegion src, ImageRegion tgt)
    {
        tgt.setMatch(null);
        src.setMatch(null);
    }
}
