/**
 * Copyright (c) 2008, MTV Networks
 */

package hudson.plugins.twitter;

import hudson.model.Hudson;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import org.kohsuke.stapler.StaplerRequest;

import com.mockobjects.servlet.MockServletContext;

public class HudsonUtil {

    public static final Hudson hudson;
    public static final File root;
    public static final MockServletContext servletContext;

    static {
        root = new File("target/test-hudson");
        root.mkdirs();
        servletContext = new MockServletContext();
        Hudson temp = null;
        try {
            temp = new Hudson(root, servletContext);
        } catch (IOException e) {
            System.err.println("Exception creating Hudson instance.");
            e.printStackTrace();
        }
        hudson = temp;
    }

    public static void init() {
        hudson.getRootUrl();
    }

    public static StaplerRequest createStaplerRequest(HttpServletRequest req) throws Exception {
        Class requestClass = Class.forName("org.kohsuke.stapler.RequestImpl");
        Constructor con = requestClass.getConstructors()[0];
        con.setAccessible(true);
        return (StaplerRequest) con.newInstance(null, req, Collections.emptyList(), null);
    }

}
