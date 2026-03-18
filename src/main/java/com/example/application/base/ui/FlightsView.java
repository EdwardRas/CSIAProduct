package com.example.application.base.ui;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("building-apps/navigate/flights")
public class FlightsView extends VerticalLayout {

    FlightsView() {
        add();
    }
    public static void showView() {
        UI.getCurrent().navigate(FlightsView.class);
    }
}