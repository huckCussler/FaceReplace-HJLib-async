package frHJLib;

import edu.rice.hj.api.HjFuture;

import java.io.*;
import java.util.*;
import java.awt.Rectangle;
import java.util.concurrent.*;
import static edu.rice.hj.Module1.*;

/**
 * Written by Trudy Firestone, Tony Tuttle -- Summer 2013
 *
 * Ported to HJLib by Tony Tuttle -- Feb 19, 2014
 */
public class Main {
    private FaceReplace faceReplace; /*represents the gui */
    private boolean started;

    public Main()
    {
        faceReplace = new FaceReplace(this);  /* create the GUI */
        while(faceReplace.isVisible())        /* keep this program from quitting before the GUI is ready */
        {
            while(!started)
            {
                try{Thread.sleep(100);}
                catch(Exception e){
                    System.err.println("Exception caught in Main/Main");
                }
            }
            this.startMethod();

            started = false;
        }
    }

    public static void main(String[] args)
    {
        new Main();
    }

    public void start()
    {
        started = true;
    }

    public void startMethod()
    {
        File[] files = faceReplace.getFiles();

        initializeHabanero();  /* start the HJ Runtime */

        /* Start reading and compiling information about the images */
        HjFuture<Master> src = future(()->new Master(files[0], files[2], this, true));
        HjFuture<Master> tgt = future(()->new Master(files[1], files[3], this, false));

        /* block until the previous two steps, which contain internal asyncs, complete */
        Master source = src.get();
        Master target = tgt.get();

        CopyOnWriteArrayList<SourceImageRegion> newSources = new CopyOnWriteArrayList<>();

        for(int i = 0; i < source.getComplete().size(); i++)
        {
            newSources.add(new SourceImageRegion(source.getComplete().get(i)));
        }

        /* Match the sub-images */
        MatchMaker maker = new MatchMaker(newSources, target.getComplete(), this);

        ConcurrentHashMap<SourceImageRegion, ImageRegion> matches = maker.makeMatches();
        HashMap<Rectangle, Rectangle> forMapping = new HashMap<>(matches.size());
        for(Enumeration<SourceImageRegion> e = matches.keys(); e.hasMoreElements();){
            SourceImageRegion currSrc = e.nextElement();
            ImageRegion currTgt = matches.get(currSrc);
            forMapping.put(currSrc.getRectangle(), currTgt.getRectangle());
        }
        faceReplace.setMap(forMapping);
    }

    /* Changes the speed at which the program runs by adjusting the delay */
    public void setSpeed(int moddedPercent){
        Master.delayInMillis = 1000 - 10*moddedPercent;
    }

    /* Tell the GUI to increment progress bar(s) */
    public void setProgress(int stage){
        faceReplace.setProgress(stage);
    }

    /* Tell the GUI to highlight a specific sub-image */
    public void setRect(Rectangle rectangle, int thread, boolean sourceOrNot)
    {
        faceReplace.setCurrentRect(rectangle, thread, sourceOrNot);
    }
}
