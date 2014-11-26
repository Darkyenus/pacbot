package lego.bots.node;

/**
 * Private property.
 * User: jIRKA
 * Date: 17.11.2014
 * Time: 21:43
 */
public final class Node {

    byte x, y;

    byte horLeftLinkedX, horLeftLinkedY;
    byte horLeftPrice;
    byte horLeftEdgeId = -1;

    byte horRightLinkedX, horRightLinkedY;
    byte horRightPrice;
    byte horRightEdgeId = -1;

    byte verUpLinkedX, verUpLinkedY;
    byte verUpPrice;
    byte verUpEdgeId = -1;

    byte verDownLinkedX, verDownLinkedY;
    byte verDownPrice;
    byte verDownEdgeId = -1;

}
