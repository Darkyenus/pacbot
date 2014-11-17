package lego.simulator;

/**
 * Private property.
 * User: Darkyen
 * Date: 24/10/14
 * Time: 15:27
 */
public enum MapTile {
    FREE(' '),OBSTACLE('O'),START('S');

    private final char display;

    MapTile(char display) {
        this.display = display;
    }


    @Override
    public String toString() {
        return Character.toString(display);
    }
}
