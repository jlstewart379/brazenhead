package com.leandog.brazenhead;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

import android.view.View;
import android.widget.*;

import com.google.brazenhead.gson.*;
import com.leandog.brazenhead.commands.*;
import com.leandog.brazenhead.json.*;

public class BrazenheadRequestHandler extends AbstractHandler {

    private final BrazenheadServer brazenheadServer;
    private final CommandRunner commandRunner;

    public BrazenheadRequestHandler(final BrazenheadServer brazenheadServer, final CommandRunner commandRunner) {
        this.brazenheadServer = brazenheadServer;
        this.commandRunner = commandRunner;
    }

    @Override
    public void handle(String target, HttpServletRequest theRequest, HttpServletResponse theResponse, int dispatch) throws IOException, ServletException {
        setHandled(theRequest);

        if (isKillCommand(theRequest)) {
            stopServer(theResponse);
            return;
        }

        try {
            commandRunner.execute(getCommands(theRequest));
            writeResultTo(theResponse);
            theResponse.setStatus(HttpServletResponse.SC_OK);
        } catch (Throwable e) {
            writeTo(theResponse, e);
            theResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isKillCommand(HttpServletRequest request) {
        return request.getPathInfo().equals("/kill");
    }

    private void stopServer(HttpServletResponse response) {
        try {
            response.flushBuffer();
            this.brazenheadServer.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setHandled(HttpServletRequest request) {
        ((Request) request).setHandled(true);
    }

    private void writeResultTo(HttpServletResponse response) throws IOException {
        writeTo(response, commandRunner.theLastResult());
    }

    private void writeTo(HttpServletResponse response, Object theValue) throws IOException {
        response.getWriter().print(gson().toJson(theValue));
    }

    private Gson gson() {
        return new GsonBuilder()
            .registerTypeAdapter(Command.class, new CommandDeserializer())
            .registerTypeHierarchyAdapter(Exception.class, new ExceptionSerializer())
            .registerTypeHierarchyAdapter(View.class, new ViewSerializer())
            .registerTypeHierarchyAdapter(ImageView.class, new ImageViewSerializer())
            .registerTypeHierarchyAdapter(TextView.class, new TextViewSerializer())
            .create();
    }

    private Command[] getCommands(HttpServletRequest request) {
        return gson().fromJson(commandsParameter(request), Command[].class);
    }

    private String commandsParameter(HttpServletRequest request) {
        final String commands = request.getParameter("commands");
        if (null == commands) {
            throw new IllegalArgumentException("brazenhead requires a \"commands\" parameter");
        }
        return commands;
    }

}