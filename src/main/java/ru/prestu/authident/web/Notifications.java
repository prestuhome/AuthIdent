package ru.prestu.authident.web;

import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.Notification;

public class Notifications {

    public final static String SMALL_WINDOW = "tray dark small closable login-help";
    public final static String BOTTOM_PANEL = "bar success small";
    public static void show(String caption, String message, String type) {
        Notification notification = new Notification(caption);
        notification.setDescription("<span>" + message + "</span>");
        notification.setHtmlContentAllowed(true);
        notification.setStyleName(type);
        notification.setPosition(Position.BOTTOM_CENTER);
        notification.setDelayMsec(2000);
        notification.show(Page.getCurrent());
    }
}