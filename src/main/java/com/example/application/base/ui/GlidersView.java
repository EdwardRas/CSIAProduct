package com.example.application.base.ui;
import com.example.application.gliders.Glider;
import com.example.application.gliders.GliderService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.grid.Grid;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

//@Route("building-apps/navigate/gliders")
@Route("")
public class GlidersView extends VerticalLayout {
    private final GliderService gliderService;
    @Autowired
    public GlidersView(GliderService gliderService) {
        this.gliderService = gliderService;
        List<Glider> records = this.gliderService.getAllGliders();
        Grid<Glider> grid = new Grid<>(Glider.class);
        GridListDataView<Glider> dataView = grid.setItems(records);
        TextField searchField = new TextField();
        searchField.setWidth("250px");
        searchField.setPlaceholder("Search:");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> dataView.refreshAll());
        dataView.addFilter(item -> {
            String searchTerm = searchField.getValue().trim();

            if (searchTerm.isEmpty())
                return true;

            boolean matchesID = String.valueOf(getId()).equals(searchTerm);
            boolean matchesRegNum = item.getRegistrationNumber().contains(searchTerm);

            return matchesID || matchesRegNum;
        });
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSizeFull();
        buttonsLayout.add(new Button("Flights", e -> FlightsView.showView()));
        buttonsLayout.add(searchField);
        add(buttonsLayout, grid);
    }
    public static void showView() {
        UI.getCurrent().navigate(GlidersView.class);
    }
}