package com.example.application.base.ui;
import com.example.application.gliders.Glider;
import com.example.application.gliders.GliderService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.grid.Grid;
import org.postgresql.util.PGInterval;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.*;
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
                .setResizable(true).setSortable(true);
        Grid.Column<Glider> regNumColumn = grid
                .addColumn(Glider::getRegistrationNumber)
                .setHeader("Registration Number").setResizable(true).setSortable(true);
        Grid.Column<Glider> typeColumn = grid
                .addColumn(Glider::getType)
                .setHeader("Type").setResizable(true).setSortable(true);
        Grid.Column<Glider> totalFlightTimeColumn = grid
                .addColumn(Glider::getTotalFlightTime)
                .setHeader("Total Flight Time").setResizable(true).setSortable(true);
        Grid.Column<Glider> flightCountColumn = grid
                .addColumn(Glider::getFlightCount)
                .setHeader("Flight Count").setResizable(true).setSortable(true);
        Grid.Column<Glider> nextCheckupHrsColumn = grid
                .addColumn(Glider::getNextCheckupHrs)
                .setHeader("Next Checkup in Hours").setResizable(true).setSortable(true);
        Grid.Column<Glider> nextCheckupFlightsColumn = grid
                .addColumn(Glider::getNextCheckupFlights)
                .setHeader("Next Checkup in Flights").setResizable(true).setSortable(true);
        Grid.Column<Glider> nextCheckupDateColumn = grid
                .addColumn(Glider::getNextCheckupDate)
                .setHeader("Next Checkup Deadline").setResizable(true)
                .setSortable(true);
        GridListDataView<Glider> dataView = grid.setItems(records);
        TextField searchField = new TextField();
        searchField.setWidth("250px");
        searchField.setLabel("Search:");
        Button addButton = new Button("Add Glider", e -> {
            try {
                showAdditionForm(gliderService);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
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
    private void showAdditionForm(GliderService gliderService) throws SQLException {
        Dialog additionForm = new Dialog();
        FormLayout formLayout = new FormLayout();
        formLayout.setAutoResponsive(true);
        formLayout.setExpandFields(true);
        TextField regNumField = new TextField();
        regNumField.setLabel("Registration Number");
        regNumField.setRequired(true);
        formLayout.setColspan(regNumField, 2);
        IntegerField totalFlightTimeHrsField = new IntegerField();
        totalFlightTimeHrsField.setLabel("Hours");
        totalFlightTimeHrsField.setMin(0);
        totalFlightTimeHrsField.setRequired(true);
        IntegerField totalFlightTimeMinsField = new IntegerField();
        totalFlightTimeMinsField.setLabel("Minutes");
        totalFlightTimeMinsField.setMin(0);
        totalFlightTimeMinsField.setMax(59);
        totalFlightTimeMinsField.setRequired(true);
        /*totalFlightTimeField.addValueChangeListener(e -> {
            try{
                if(!e.getValue().isEmpty()){
                    new PGInterval(e.getValue());
                }
                totalFlightTimeField.setInvalid(false);
            }
            catch(Exception ex){
                totalFlightTimeField.setInvalid(true);
                totalFlightTimeField.setErrorMessage("Invalid Format");
            }
        });*/
        IntegerField flightCountField = new IntegerField();
        flightCountField.setLabel("Flight Count");
        flightCountField.setRequired(true);
        flightCountField.setMin(0);
        formLayout.setColspan(flightCountField, 2);
        TextField typeField = new TextField();
        typeField.setLabel("Type");
        formLayout.setColspan(typeField, 2);
        IntegerField nextCheckupHrsHrsField = new IntegerField();
        nextCheckupHrsHrsField.setLabel("Hours");
        nextCheckupHrsHrsField.setMin(0);
        IntegerField nextCheckupHrsMinsField = new IntegerField();
        nextCheckupHrsMinsField.setLabel("Minutes");
        nextCheckupHrsMinsField.setMin(0);
        nextCheckupHrsMinsField.setMax(59);
        IntegerField nextCheckupFlightsField = new IntegerField();
        nextCheckupFlightsField.setLabel("Next Checkup in Flights");
        nextCheckupFlightsField.setMin(0);
        formLayout.setColspan(nextCheckupFlightsField, 2);
        DatePicker nextCheckupDateField = new DatePicker();
        nextCheckupDateField.setLabel("Next Checkup Deadline");
        formLayout.setColspan(nextCheckupDateField, 2);
        formLayout.addFormRow(regNumField);
        formLayout.setColspan(regNumField, 2);
        formLayout.addFormRow(typeField);
        formLayout.addFormRow(new Span("Total Flight Time"));
        formLayout.addFormRow(totalFlightTimeHrsField, totalFlightTimeMinsField);
        formLayout.addFormRow(flightCountField);
        formLayout.addFormRow(new Span("Next Checkup in Hours"));
        formLayout.addFormRow(nextCheckupHrsHrsField, nextCheckupHrsMinsField);
        formLayout.addFormRow(nextCheckupFlightsField);
        formLayout.addFormRow(nextCheckupDateField);


//        formLayout.add(regNumField, totalFlightTimeField, flightCountField, typeField, nextCheckupHrsField, nextCheckupFlightsField, nextCheckupDateField);
        Button addButton = new Button("Add", e -> {
            /*if(flightCountField.isInvalid()){
                Notification.show("Please enter a valid flight count");
                return;
            }*/
            String regNum = regNumField.getValue();
            PGInterval totalFlightTime = null;
            try {
                totalFlightTime = new PGInterval(totalFlightTimeHrsField.getValue()+":"+totalFlightTimeMinsField.getValue());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            Integer flightCount = flightCountField.getValue();
            String type = typeField.getValue();
            PGInterval nextCheckupHrs;
            if(nextCheckupHrsMinsField.getValue() != null && nextCheckupHrsHrsField.getValue()!=null) {
                try {
                    nextCheckupHrs = new PGInterval(nextCheckupHrsHrsField.getValue() + ":" + nextCheckupHrsMinsField.getValue());
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            else {
                nextCheckupHrs = null;
            }
            Integer nextCheckupFlights = nextCheckupFlightsField.getValue();
            Date nextCheckupDate = null;
            if(nextCheckupDateField.getValue() != null) {
                nextCheckupDate = Date.valueOf(nextCheckupDateField.getValue());
            }
            gliderService.addGlider(regNum, totalFlightTime, flightCount, type, nextCheckupHrs, nextCheckupFlights, nextCheckupDate);
            additionForm.close();
        });
        Button cancelButton = new Button("Cancel", e -> additionForm.close());
        additionForm.getFooter().add(cancelButton, addButton);
        additionForm.add(formLayout);
        additionForm.open();

    }
    public static void showView() {
        UI.getCurrent().navigate(GlidersView.class);
    }
}