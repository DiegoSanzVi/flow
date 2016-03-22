package com.vaadin.hummingbird.uitest.ui.push;

import java.util.Timer;
import java.util.TimerTask;

import com.vaadin.hummingbird.dom.Element;
import com.vaadin.hummingbird.dom.ElementFactory;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

public class ClientServerCounterUI extends UI {

    public static final String CLIENT_COUNTER_ID = "clientCounter";

    public static final String STOP_TIMER_ID = "stopTimer";

    public static final String START_TIMER_ID = "startTimer";

    public static final String SERVER_COUNTER_ID = "serverCounter";

    public static final String INCREMENT_BUTTON_ID = "incrementCounter";

    private final Timer timer = new Timer(true);

    private TimerTask task;
    private int clientCounter = 0;
    private int serverCounter = 0;

    private Element serverCounterElement;

    @Override
    protected void init(VaadinRequest request) {
        getReconnectDialogConfiguration().setDialogModal(false);
        spacer();

        // Client counter
        getElement().appendChild(ElementFactory
                .createDiv("Client counter (click 'increment' to update):"));
        Element lbl = ElementFactory.createDiv(clientCounter + "")
                .setAttribute("id", CLIENT_COUNTER_ID);
        getElement().appendChild(lbl);

        Element button = ElementFactory.createButton("Increment")
                .setAttribute("id", INCREMENT_BUTTON_ID);
        button.addEventListener("click", e -> {
            clientCounter++;
            lbl.setTextContent(clientCounter + "");
        });

        getElement().appendChild(button);
        spacer();

        /*
         * Server counter
         */
        getElement().appendChild(ElementFactory.createDiv(
                "Server counter (updates each second by server thread):"));
        serverCounterElement = ElementFactory.createDiv().setAttribute("id",
                SERVER_COUNTER_ID);
        serverCounterElement.setTextContent(serverCounter + "");
        getElement().appendChild(serverCounterElement);

        Element startTimer = ElementFactory.createButton("Start timer")
                .setAttribute("id", START_TIMER_ID);

        startTimer.addEventListener("click", e -> {
            serverCounter = 0;
            if (task != null) {
                task.cancel();
            }
            task = new TimerTask() {
                @Override
                public void run() {
                    access(() -> {
                        serverCounter++;
                        serverCounterElement.setTextContent(serverCounter + "");
                    });
                }
            };
            timer.scheduleAtFixedRate(task, 1000, 1000);
        });
        getElement().appendChild(startTimer);

        Element stopTimer = ElementFactory.createButton("Stop timer")
                .setAttribute("id", STOP_TIMER_ID);
        stopTimer.setTextContent("Stop timer");
        stopTimer.addEventListener("click", e -> {
            if (task != null) {
                task.cancel();
                task = null;
            }
        });
        getElement().appendChild(stopTimer);

    }

    protected void spacer() {
        getElement().appendChild(ElementFactory.createHorizontalRule());
    }

    @Override
    public void detach() {
        super.detach();
        timer.cancel();
    }

}
