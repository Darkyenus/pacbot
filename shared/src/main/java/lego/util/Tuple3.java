package lego.util;

/**
 * Private property.
 * User: jIRKA
 * Date: 9.10.2014
 * Time: 18:58
 */
public class Tuple3<A,B,C>{

    private A a = null;
    private B b = null;
    private C c = null;

    public Tuple3(A a, B b, C c){
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public A getA(){
        return a;
    }
    public B getB(){
        return b;
    }
    public C getC(){
        return c;
    }

}
