package com.example.application.base.ui;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("building-apps/navigate/gliders")
public class GlidersView extends VerticalLayout {

    GlidersView() {
        add(new Button("Gliders", e -> FlightsView.showView()));
    }

    public static void showView() {
        UI.getCurrent().navigate(GlidersView.class);
    }
}