package com.wup;

import org.slf4j.Logger;

import com.thingworx.things.Thing;
import com.thingworx.thingshape.ThingShape;
import com.thingworx.logging.LogUtilities;
import com.thingworx.webservices.context.ThreadLocalContext;

public class wupBaseThingShape extends ThingShape {

    private static final long serialVersionUID = 1L;

    private static Logger _logger = LogUtilities.getInstance().getApplicationLogger(wupBaseThingShape.class);


    protected Thing getMe() throws Exception {
        final Object meObj = ThreadLocalContext.getMeContext();
        if (meObj instanceof Thing) {
            return (Thing) meObj;
        } else {
            this.logError("getMe() Cannot cast me to Thing.");
            throw new Exception("Cannot cast me to Thing");
        }
    }

    protected String getMeName() throws Exception {
        final Thing me = this.getMe();
        return me.getName();
    }

    protected void logError(String text) {
        try {
        _logger.error("[wupBaseThingShape("+this.getMeName()+")]."+text);
        } catch(Exception e) {

        }
    }

}
