package lego.util;

/**
 * Created by jIRKA on 3.10.2014.
 *
 * @deprecated Too garbage inducing
 */
@Deprecated
public class TupleIntInt {

    private int x = 0;
    private int y = 0;

    public TupleIntInt(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }

}
