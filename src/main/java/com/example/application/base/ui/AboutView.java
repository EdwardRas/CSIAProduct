package com.example.application.base.ui;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("building-apps/navigate/about")
public class AboutView extends VerticalLayout {

    AboutView() {
        add("You'll navigate to this view.");
    }

    public static void showView() {
        UI.getCurrent().navigate(AboutView.class);
    }
}