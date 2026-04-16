package com.example.application.base.ui;
import com.example.application.gliders.Glider;
import com.example.application.gliders.GliderService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.GridSingleSelectionModel;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
import java.time.LocalDate;
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
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
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

        GridSingleSelectionModel<Glider> selectionModel = (GridSingleSelectionModel<Glider>)grid.getSelectionModel();
        Button deleteButton = new Button("Delete Glider", e -> {
                Long deletingId;
                if (selectionModel.getSelectedItem().isPresent()) {
                    deletingId = selectionModel.getSelectedItem().get().getId();
                    if (deletingId >= 0) {
                        gliderService.deleteGlider(deletingId);
                    }
                }
                UI.getCurrent().getPage().reload();
        });
        grid.addSelectionListener(e -> {
            deleteButton.setEnabled(true);
            if(e.getFirstSelectedItem().isEmpty()){
                deleteButton.setEnabled(false);
            }
        });
        Button editButton = new Button("Edit Glider", e -> {
            if (selectionModel.getSelectedItem().isPresent()) {
                try {
                    showEditForm(gliderService, selectionModel.getSelectedItem().get());
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        grid.addSelectionListener(e -> {
            deleteButton.setEnabled(true);
            editButton.setEnabled(true);
            if(e.getFirstSelectedItem().isEmpty()){
                deleteButton.setEnabled(false);
                editButton.setEnabled(false);
            }
        });

        deleteButton.setEnabled(false);
        editButton.setEnabled(false);
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
        buttonsLayout.add(searchField, addButton, deleteButton, editButton);
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

        Button addButton = new Button("Add", e -> {
            String regNum = regNumField.getValue();
            PGInterval totalFlightTime = null;
            try {
                String flightTime = totalFlightTimeHrsField.getValue()+" hours "+totalFlightTimeMinsField.getValue()+" minutes";
                totalFlightTime = new PGInterval(flightTime);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            Integer flightCount = flightCountField.getValue();
            String type = typeField.getValue();
            PGInterval nextCheckupHrs;
            if(nextCheckupHrsMinsField.getValue() != null && nextCheckupHrsHrsField.getValue()!=null) {
                try {
                    nextCheckupHrs = new PGInterval(nextCheckupHrsHrsField.getValue() + " hours" + nextCheckupHrsMinsField.getValue() + " minutes");
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
            UI.getCurrent().getPage().reload();
        });
        Button cancelButton = new Button("Cancel", e -> additionForm.close());
        additionForm.getFooter().add(cancelButton, addButton);
        additionForm.add(formLayout);
        additionForm.open();

    }
    private void showEditForm(GliderService gliderService, Glider glider) throws SQLException {
        Dialog editForm = new Dialog();
        FormLayout formLayout = new FormLayout();
        formLayout.setAutoResponsive(true);
        formLayout.setExpandFields(true);
        TextField regNumField = new TextField();
        regNumField.setLabel("Registration Number");
        regNumField.setRequired(true);
        regNumField.setValue(glider.getRegistrationNumber());
        formLayout.setColspan(regNumField, 2);
        IntegerField totalFlightTimeHrsField = new IntegerField();
        totalFlightTimeHrsField.setLabel("Hours");
        totalFlightTimeHrsField.setMin(0);
        totalFlightTimeHrsField.setRequired(true);
        totalFlightTimeHrsField.setValue(glider.getTotalFlightTime().getHours());
        IntegerField totalFlightTimeMinsField = new IntegerField();
        totalFlightTimeMinsField.setLabel("Minutes");
        totalFlightTimeMinsField.setMin(0);
        totalFlightTimeMinsField.setMax(59);
        totalFlightTimeMinsField.setRequired(true);
        totalFlightTimeMinsField.setValue(glider.getTotalFlightTime().getMinutes());
        IntegerField flightCountField = new IntegerField();
        flightCountField.setLabel("Flight Count");
        flightCountField.setRequired(true);
        flightCountField.setMin(0);
        flightCountField.setValue(glider.getFlightCount());
        formLayout.setColspan(flightCountField, 2);
        TextField typeField = new TextField();
        typeField.setLabel("Type");
        formLayout.setColspan(typeField, 2);
        typeField.setValue(glider.getType());
        IntegerField nextCheckupHrsHrsField = new IntegerField();
        nextCheckupHrsHrsField.setLabel("Hours");
        nextCheckupHrsHrsField.setMin(0);

        IntegerField nextCheckupHrsMinsField = new IntegerField();
        nextCheckupHrsMinsField.setLabel("Minutes");
        nextCheckupHrsMinsField.setMin(0);
        nextCheckupHrsMinsField.setMax(59);
        if(glider.getNextCheckupHrs() != null) {
            nextCheckupHrsHrsField.setValue(glider.getNextCheckupHrs().getHours());
            nextCheckupHrsMinsField.setValue(glider.getNextCheckupHrs().getMinutes());
        }
        IntegerField nextCheckupFlightsField = new IntegerField();
        nextCheckupFlightsField.setLabel("Next Checkup in Flights");
        nextCheckupFlightsField.setMin(0);
        nextCheckupFlightsField.setValue(glider.getNextCheckupFlights());
        formLayout.setColspan(nextCheckupFlightsField, 2);
        DatePicker nextCheckupDateField = new DatePicker();
        nextCheckupDateField.setLabel("Next Checkup Deadline");
        if (glider.getNextCheckupDate() != null) {
            nextCheckupDateField.setValue(glider.getNextCheckupDate().toLocalDate());
        }
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

        Button editButton = new Button("Edit", e -> {
            String regNum = regNumField.getValue();
            PGInterval totalFlightTime = null;
            try {
                String flightTime = totalFlightTimeHrsField.getValue()+" hours "+totalFlightTimeMinsField.getValue()+" minutes";
                totalFlightTime = new PGInterval(flightTime);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            Integer flightCount = flightCountField.getValue();
            String type = typeField.getValue();
            PGInterval nextCheckupHrs;
            if(nextCheckupHrsMinsField.getValue() != null && nextCheckupHrsHrsField.getValue()!=null) {
                try {
                    nextCheckupHrs = new PGInterval(nextCheckupHrsHrsField.getValue() + " hours " + nextCheckupHrsMinsField.getValue() + " minutes");
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
            gliderService.editGlider(glider, regNum, totalFlightTime, flightCount, type, nextCheckupHrs, nextCheckupFlights, nextCheckupDate);
            editForm.close();
            UI.getCurrent().getPage().reload();
        });
        Button cancelButton = new Button("Cancel", e -> editForm.close());
        editForm.getFooter().add(cancelButton, editButton);
        editForm.add(formLayout);
        editForm.open();

    }
    public static void showView() {
        UI.getCurrent().navigate(GlidersView.class);
    }
}