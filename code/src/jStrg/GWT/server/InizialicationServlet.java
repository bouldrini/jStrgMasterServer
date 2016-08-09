package jStrg.GWT.server;

import jStrg.environment.Environment;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.IOException;

/**
 * Created by Jura on 18.05.2016.
 */
public class InizialicationServlet extends HttpServlet {
    @Override
    public void init() throws ServletException {
        super.init();
        Environment.startup();
        try {
            Environment.seed(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
