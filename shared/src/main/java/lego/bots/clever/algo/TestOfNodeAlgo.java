package lego.bots.clever.algo;

import lego.bots.clever.Algo;

/**
 * Private property.
 * User: Darkyen
 * Date: 05/12/14
 * Time: 09:45
 */
public class TestOfNodeAlgo extends Algo {

    @Override
    public void run() {

        bestRoute.pushNext((byte) 4, (byte) 2);
        bestRoute.pushNext((byte) 4, (byte) 3);
        bestRoute.pushNext((byte) 5, (byte) 3);
        bestRoute.pushNext((byte) 6, (byte) 3);
        bestRoute.pushNext((byte) 6, (byte) 2);
        bestRoute.pushNext((byte) 6, (byte) 1);
        bestRoute.pushNext((byte) 6, (byte) 0);
        bestRoute.pushNext((byte) 7, (byte) 0);
        bestRoute.pushNext((byte) 8, (byte) 0);
        bestRoute.pushNext((byte) 8, (byte) 1);
        bestRoute.pushNext((byte) 8, (byte) 2);
        bestRoute.pushNext((byte) 8, (byte) 3);
        bestRoute.pushNext((byte) 8, (byte) 4);
        bestRoute.pushNext((byte) 7, (byte) 4);
        bestRoute.pushNext((byte) 6, (byte) 4);
        bestRoute.pushNext((byte) 5, (byte) 4);
        bestRoute.pushNext((byte) 4, (byte) 4);
        bestRoute.pushNext((byte) 3, (byte) 4);
        bestRoute.pushNext((byte) 2, (byte) 4);
        bestRoute.pushNext((byte) 1, (byte) 4);
        bestRoute.pushNext((byte) 0, (byte) 4);
        bestRoute.pushNext((byte) 0, (byte) 5);
        bestRoute.pushNext((byte) 1, (byte) 5);
        bestRoute.pushNext((byte) 2, (byte) 5);
        bestRoute.pushNext((byte) 1, (byte) 5);
        bestRoute.pushNext((byte) 0, (byte) 5);






        bestRoutePrice = computePrice(bestRoute);
        bestScoredPoints = 40;
    }

}
