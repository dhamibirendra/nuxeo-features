package org.nuxeo.opensocial.servlet;

import java.util.ArrayList;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.osgi.framework.FrameworkEvent;

/**
 * This class is just a wrapper to hold the initialization of Guice (et al)
 * until we have received the "go ahead" from the Runtime and that everything is
 * fully initialized.
 * 
 * @author Ian Smith<ismith@nuxeo.com>
 * 
 */
public class ContextListenerDelayer implements ServletContextListener {

    /*
     * this is the true object but because of differences in initialization
     * sequence we have to delay this until nuxeo is fully up and all theuser's
     * configuration files have been loaded into extension points
     */

    protected GuiceContextListener delayed = new GuiceContextListener();

    /*
     * Saved from the original call so we can feed it to the true object later
     */
    protected ServletContextEvent delayedEvent;

    /*
     * We don't call the constructor directly -- the servlet container does so
     * we have to hold a list of all the objects of this type created. this
     * number should be 1, be we track "all of them" just in case.
     */
    private static ArrayList<ContextListenerDelayer> created = new ArrayList<ContextListenerDelayer>();

    /*
     * We have to keep track of whether or not activate has been called already
     * because in some packagings (jetty) the framework ready method is called
     * BEFORE the war container instantiates the objects. Sigh.
     */
    protected static boolean hasBeenActivated = false;

    /*
     * No need to delay this method.
     * 
     * @seejavax.servlet.ServletContextListener#contextDestroyed(javax.servlet.
     * ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent sce) {
        delayed.contextDestroyed(sce);

    }

    /*
     * Make the list of all objects created of this type.
     */
    public ContextListenerDelayer() {
        created.add(this);
    }

    /*
     * This is where the true object expects to be initialzed but we prevent it.
     */
    public void contextInitialized(ServletContextEvent sce) {
        delayedEvent = sce;
        if (hasBeenActivated) {
            // we are running AFTER the framework is created so no
            // sense in delaying further
            delayed.contextInitialized(sce);
        }
    }

    /*
     * Do the work that should have happened at contextInitialized() now if the
     * Framework is fully up.
     */
    public void frameworkEvent(FrameworkEvent event) {
        if (event.getType() == FrameworkEvent.STARTED) {
            delayed.contextInitialized(delayedEvent);
        }
    }

    /*
     * Walk the list of objects of this type we have created echoing the
     * framework event. Note: This method is static!
     */

    public static void activate(FrameworkEvent event) {
        // this list will be size 0 if no objects were created
        // before the framework is ready, thus we use the
        // has been activated flag
        for (ContextListenerDelayer delayer : created) {
            delayer.frameworkEvent(event);
        }
        hasBeenActivated = true;
    }

}
