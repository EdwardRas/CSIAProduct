package com.example.application.base.ui;
import com.example.application.gliders.Glider;
import com.example.application.gliders.GliderService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
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
        Grid<Glider> grid = new Grid<>();
        Grid.Column<Glider> IDColumn = grid
                .addColumn(Glider::getId).setHeader("ID")
                .setResizable(true);
        Grid.Column<Glider> regNumColumn = grid.addColumn(Glider::getRegistrationNumber)
                .setHeader("Registration Number").setResizable(true);
        Grid.Column<Glider> totalFlightTimeColumn = grid
                .addColumn(Glider::getTotalFlightTime)
                .setHeader("Total Flight Time").setResizable(true);
        Grid.Column<Glider> flightCountColumn = grid
                .addColumn(Glider::getFlightCount)
                .setHeader("Flight Count").setResizable(true);
        Grid.Column<Glider> typeColumn = grid
                .addColumn(Glider::getType)
                .setHeader("Type").setResizable(true);
        Grid.Column<Glider> nextCheckupHrsColumn = grid
                .addColumn(Glider::getNextCheckupHrs)
                .setHeader("Next Checkup in Hours").setResizable(true);
        Grid.Column<Glider> nextCheckupFlightsColumn = grid
                .addColumn(Glider::getNextCheckupFlights)
                .setHeader("Next Checkup in Flights").setResizable(true);
        Grid.Column<Glider> nextCheckupDateColumn = grid
                .addColumn(Glider::getNextCheckupDate)
                .setHeader("Next Checkup Deadline").setResizable(true);
        GridListDataView<Glider> dataView = grid.setItems(records);
        TextField searchField = new TextField();
        searchField.setWidth("250px");
        searchField.setPlaceholder("Search:");
        Button addButton = new Button("Add Glider", e -> add(showAdditionForm(gliderService)));
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> dataView.refreshAll());
        dataView.addFilter(item -> {
            String searchTerm = searchField.getValue().trim();

            if (searchTerm.isEmpty())
                return true;

            boolean matchesID = String.valueOf(getId()).equals(searchTerm);
            boolean matchesRegNum = item.getRegistrationNumber().contains(searchTerm);
            boolean matchesNextCheckupHrs = item.getNextCheckupHrs().equals(searchTerm);

            return matchesID || matchesRegNum || matchesNextCheckupHrs;
        });
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSizeFull();
        buttonsLayout.add(new Button("Flights", e -> FlightsView.showView()));
        buttonsLayout.add(searchField, addButton);
        add(buttonsLayout, grid);
    }
    private static Dialog showAdditionForm(GliderService gliderService) {
        Dialog additionForm = new Dialog();
        FormLayout formLayout = new FormLayout();
        TextField regNumField = new TextField();
        regNumField.setWidth("250px");
        regNumField.setPlaceholder("Registration Number");
        TextField totalFlightTimeField = new TextField();
        totalFlightTimeField.setWidth("250px");
        totalFlightTimeField.setPlaceholder("Total Flight Time");
        TextField flightCountField = new TextField();
        flightCountField.setWidth("250px");
        flightCountField.setPlaceholder("Flight Count");
        TextField typeField = new TextField();
        typeField.setWidth("250px");
        typeField.setPlaceholder("Type");
        TextField nextCheckupHrsField = new TextField();
        nextCheckupHrsField.setWidth("250px");
        nextCheckupHrsField.setPlaceholder("Next Checkup Hours");
        TextField nextCheckupFlightsField = new TextField();
        nextCheckupFlightsField.setWidth("250px");
        nextCheckupFlightsField.setPlaceholder("Next Checkup Flights");
        TextField nextCheckupDateField = new TextField();
        nextCheckupDateField.setWidth("250px");
        nextCheckupDateField.setPlaceholder("Next Checkup Date");
        formLayout.add(regNumField, totalFlightTimeField, flightCountField, typeField, nextCheckupHrsField, nextCheckupFlightsField, nextCheckupDateField);
        String regNum = regNumField.getValue();
        String totalFlightTimeStr = totalFlightTimeField.getValue();
        String flightCountStr = flightCountField.getValue();
        String type = typeField.getValue();
        String nextCheckupHrsStr = nextCheckupHrsField.getValue();
        String nextCheckupFlightsStr = nextCheckupFlightsField.getValue();
        String nextCheckupDateStr = nextCheckupDateField.getValue();
        Button addButton = new Button("Add"/*, e -> gliderService.addGlider(regNum, Integer.parseInt(flightCountStr))*/);
        Button cancelButton = new Button("Cancel", e -> additionForm.close());
        additionForm.getFooter().add(addButton, cancelButton);
        return additionForm;

    }
    public static void showView() {
        UI.getCurrent().navigate(GlidersView.class);
    }
}