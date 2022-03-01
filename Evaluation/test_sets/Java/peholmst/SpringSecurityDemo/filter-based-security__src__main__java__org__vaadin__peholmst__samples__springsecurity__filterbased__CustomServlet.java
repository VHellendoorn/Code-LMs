package org.vaadin.peholmst.samples.springsecurity.filterbased;

import javax.servlet.ServletException;

import org.springframework.stereotype.Component;

import com.vaadin.server.CustomizedSystemMessages;
import com.vaadin.server.SystemMessagesProvider;
import com.vaadin.spring.server.SpringVaadinServlet;

@Component("vaadinServlet")
public class CustomServlet extends SpringVaadinServlet {

    @Override
    protected void servletInitialized() throws ServletException {
        super.servletInitialized();
        getService().setSystemMessagesProvider((SystemMessagesProvider) systemMessagesInfo -> {
            CustomizedSystemMessages messages = new CustomizedSystemMessages();
            // Don't show any messages, redirect immediately to the session expired URL
            messages.setSessionExpiredNotificationEnabled(false);
            // Force a logout to also end the HTTP session and not only the Vaadin session
            messages.setSessionExpiredURL("logout");
            // Don't show any message, reload the page instead
            messages.setCommunicationErrorNotificationEnabled(false);
            return messages;
        });
    }
}
