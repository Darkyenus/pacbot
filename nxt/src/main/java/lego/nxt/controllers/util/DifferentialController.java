package lego.nxt.controllers.util;

import lego.api.controllers.EnvironmentController;

/**
 * Private property.
 * User: Darkyen
 * Date: 02/12/14
 * Time: 18:42
 */
public interface DifferentialController {
    public EnvironmentController.Direction getHeadingDirection();
    public byte getX();
    public byte getY();
}
