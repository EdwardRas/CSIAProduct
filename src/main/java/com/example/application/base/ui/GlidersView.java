package com.example.application.base.ui;
import com.example.application.flights.FlightService;
import com.example.application.gliders.Glider;
import com.example.application.gliders.GliderService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.GridSingleSelectionModel;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.grid.Grid;
import org.postgresql.util.PGInterval;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.*;
import java.util.List;

@Route(value = "gliders", layout =  MainLayout.class)
@Menu(order = 1, icon = "vaadin:airplane")public class GlidersView extends VerticalLayout{
    //DB service objects
    private final GliderService gliderService;
    private final FlightService flightService;

    @Autowired //automatically handles input variables
    public GlidersView(GliderService gliderService,  FlightService flightService) {
        this.gliderService = gliderService;
        this.flightService = flightService;

        //get all gliders in DB
        List<Glider> records = this.gliderService.getAllGliders();

        //create grid display element
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
        Grid.Column<Glider> statusColumn = grid
                .addColumn(Glider::isFlying)
                .setHeader("Is flying?").setResizable(true).setSortable(true);

        //set rows of grid to be all gliders in DB
        GridListDataView<Glider> dataView = grid.setItems(records);

        //create search text field
        TextField searchField = new TextField();
        searchField.setWidth("250px");
        searchField.setPlaceholder("Search");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> dataView.refreshAll());

        //create button to display addition form
        Button addButton = new Button("Add Glider", e -> {
            try {
                showAdditionForm(gliderService);
            } catch (SQLException ex) {
                Notification notification = Notification
                            .show("Error: " + ex.getErrorCode());
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                throw new RuntimeException(ex);
            }
        });

        //get selection model for glider selection to get selected item in grid
        GridSingleSelectionModel<Glider> selectionModel = (GridSingleSelectionModel<Glider>)grid.getSelectionModel();

        //create button to delete glider
        Button deleteButton = new Button("Delete Glider");
        deleteButton.addClickListener(e -> {
            Long deletingId;
            if (selectionModel.getSelectedItem().isPresent()) {
                deletingId = selectionModel.getSelectedItem().get().getId();
                if (deletingId >= 0) {
                    //create dialog to confirm deletion
                    ConfirmDialog deleteDialog = new ConfirmDialog();
                    deleteDialog.setHeader("Delete Glider?");
                    deleteDialog.setText("Are you sure you want to permanently delete this item? This will also delete all flights associated with this item");

                    deleteDialog.setCancelable(true);
                    deleteDialog.addCancelListener(event -> deleteDialog.close());

                    deleteDialog.setConfirmText("Delete");
                    deleteDialog.setConfirmButtonTheme("error primary");
                    deleteDialog.addConfirmListener(event -> {
                        try {
                            flightService.deleteFlightByGliderId(deletingId);
                            gliderService.deleteGlider(deletingId);
                            UI.getCurrent().getPage().reload();
                        } catch (SQLException ex) {
                            Notification notification = Notification
                                    .show("Error: " + ex.getErrorCode());
                            throw new RuntimeException(ex);
                        }
                    });
                    deleteDialog.open();
                }
            }
        });

        //create edit button
        Button editButton = new Button("Edit Glider", e -> {
            if (selectionModel.getSelectedItem().isPresent()) {
                try {
                    showEditForm(gliderService, selectionModel.getSelectedItem().get());
                } catch (SQLException ex) {
                    Notification notification = Notification
                            .show("Error: " + ex.getErrorCode());
                    throw new RuntimeException(ex);
                }
            }
        });

        //add selection listener to grid
        grid.addSelectionListener(e -> {
            deleteButton.setEnabled(true);
            editButton.setEnabled(true);
            if(e.getFirstSelectedItem().isEmpty()){
                deleteButton.setEnabled(false);
                editButton.setEnabled(false);
                FlightsView.gliderFilter = null;
            }
            else{
                FlightsView.gliderFilter = selectionModel.getSelectedItem().get();
            }
        });

        //set buttons which are not always enabled to be disabled by default\
        deleteButton.setEnabled(false);
        editButton.setEnabled(false);

        //search functionality
        dataView.addFilter(item -> {
            String searchTerm = searchField.getValue().trim();

            if (searchTerm.isEmpty())
                return true;

            boolean matchesID = String.valueOf(item.getId()).equals(searchTerm);
            boolean matchesRegNum = item.getRegistrationNumber().contains(searchTerm);
            boolean matchesFlightCount = String.valueOf(item.getFlightCount()).contains(searchTerm);
            boolean matchesType = item.getType().contains(searchTerm);
            boolean matchesTotalFlightTime = String.valueOf(item.getTotalFlightTime()).contains(searchTerm);
            boolean matchesNextCheckupHrs = String.valueOf(item.getNextCheckupHrs()).contains(searchTerm);
            boolean matchesNextCheckupFlights = String.valueOf(item.getNextCheckupFlights()).contains(searchTerm);
            boolean matchesNextCheckupDate = String.valueOf(item.getNextCheckupDate()).contains(searchTerm);

            return matchesID || matchesRegNum || matchesNextCheckupHrs || matchesTotalFlightTime || matchesFlightCount || matchesType || matchesNextCheckupFlights || matchesNextCheckupDate;
        });

        addButton.setPrefixComponent(new Icon(VaadinIcon.FILE_ADD));
        deleteButton.setPrefixComponent(new Icon(VaadinIcon.TRASH));
        editButton.setPrefixComponent(new Icon(VaadinIcon.EDIT));

        //add all UI elements
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSizeFull();
        buttonsLayout.add(searchField, addButton, deleteButton, editButton);
        add(buttonsLayout, grid);
    }

    //shows user form to input addition data
    private void showAdditionForm(GliderService gliderService) throws SQLException {
        //create all UI elements for data input
        Dialog additionForm = new Dialog();
        FormLayout formLayout = new FormLayout();
        formLayout.setSizeFull();
        formLayout.setMaxWidth("400px");
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        TextField regNumField = new TextField();
        regNumField.setLabel("Registration Number");
        regNumField.setRequired(true);
        regNumField.setSizeFull();
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
        flightCountField.setSizeFull();
        TextField typeField = new TextField();
        typeField.setLabel("Type");
        typeField.setSizeFull();
        typeField.setRequired(true);
        IntegerField nextCheckupHrsHrsField = new IntegerField();
        nextCheckupHrsHrsField.setLabel("Hours");
        nextCheckupHrsHrsField.setMin(0);
        totalFlightTimeHrsField.addValueChangeListener(e -> nextCheckupHrsHrsField.setMin(e.getValue()));
        IntegerField nextCheckupHrsMinsField = new IntegerField();
        nextCheckupHrsMinsField.setLabel("Minutes");
        nextCheckupHrsMinsField.setMin(0);
        nextCheckupHrsMinsField.setMax(59);
        totalFlightTimeMinsField.addValueChangeListener(e -> {
            if(totalFlightTimeHrsField == nextCheckupHrsHrsField) {
                nextCheckupHrsMinsField.setMin(e.getValue());
            }
        });
        IntegerField nextCheckupFlightsField = new IntegerField();
        nextCheckupFlightsField.setLabel("Next Checkup in Flights");
        nextCheckupFlightsField.setMin(0);
        flightCountField.addValueChangeListener(e -> {nextCheckupFlightsField.setMin(e.getValue());});
        nextCheckupFlightsField.setSizeFull();
        DatePicker nextCheckupDateField = new DatePicker();
        nextCheckupDateField.setLabel("Next Checkup Deadline");
        nextCheckupDateField.setSizeFull();
        formLayout.addFormRow(regNumField);
        regNumField.setSizeFull();
        formLayout.addFormRow(regNumField);
        regNumField.setSizeFull();
        formLayout.addFormRow(typeField);
        Span totalFlightTimeTitle = new Span("Total Flight Time");
        totalFlightTimeTitle.setSizeFull();
        formLayout.addFormRow(totalFlightTimeTitle);
        HorizontalLayout totalFlightTimeLayout = new HorizontalLayout();
        totalFlightTimeLayout.add(totalFlightTimeHrsField, totalFlightTimeMinsField);
        totalFlightTimeLayout.setSpacing(true);
        totalFlightTimeLayout.setSizeFull();
        formLayout.addFormRow(totalFlightTimeLayout);
        formLayout.addFormRow(flightCountField);
        Span checkupHrsTitle = new Span("Next Checkup in Hours");
        checkupHrsTitle.setSizeFull();
        formLayout.addFormRow(checkupHrsTitle);
        HorizontalLayout nextChecukpHrsLayout = new HorizontalLayout();
        nextChecukpHrsLayout.add(nextCheckupHrsHrsField, nextCheckupHrsMinsField);
        nextChecukpHrsLayout.setSpacing(true);
        nextChecukpHrsLayout.setSizeFull();
        formLayout.addFormRow(nextChecukpHrsLayout);
        formLayout.addFormRow(nextCheckupFlightsField);
        formLayout.addFormRow(nextCheckupDateField);

        //create buttons to confirm and cancel addition
        Button addButton = new Button("Add", e -> {
            //if any field has an invalid value, end lambda function early
            if(regNumField.isEmpty() || flightCountField.isEmpty() || totalFlightTimeHrsField.isInvalid() || totalFlightTimeMinsField.isInvalid() || typeField.isEmpty() || nextCheckupHrsHrsField.isInvalid() || nextCheckupFlightsField.isInvalid() || nextCheckupHrsMinsField.isInvalid() || nextCheckupDateField.isInvalid()) {
                Notification.show("At least one field value is invalid");
                return;
            }
            String regNum = regNumField.getValue();
            PGInterval totalFlightTime = null;
            try {
                String flightTime = totalFlightTimeHrsField.getValue()+" hours "+totalFlightTimeMinsField.getValue()+" minutes";
                totalFlightTime = new PGInterval(flightTime);
            } catch (SQLException ex) {
                Notification notification = Notification
                            .show("Error: " + ex.getErrorCode());
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                throw new RuntimeException(ex);
            }
            Integer flightCount = flightCountField.getValue();
            String type = typeField.getValue();
            PGInterval nextCheckupHrs;
            if(nextCheckupHrsMinsField.getValue() != null && nextCheckupHrsHrsField.getValue()!=null) {
                try {
                    nextCheckupHrs = new PGInterval(nextCheckupHrsHrsField.getValue() + " hours" + nextCheckupHrsMinsField.getValue() + " minutes");
                } catch (SQLException ex) {
                    Notification notification = Notification
                            .show("Error: " + ex.getErrorCode());
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

        //add all UI elements and show form
        additionForm.getFooter().add(cancelButton, addButton);
        additionForm.add(formLayout);
        additionForm.open();
    }

    //shows user form for record editing
    private void showEditForm(GliderService gliderService, Glider glider) throws SQLException {
        //create all UI elements for data input
        Dialog editForm = new Dialog();
        FormLayout formLayout = new FormLayout();
        formLayout.setSizeFull();
        formLayout.setMaxWidth("400px");
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        TextField regNumField = new TextField();
        regNumField.setLabel("Registration Number");
        regNumField.setRequired(true);
        regNumField.setValue(glider.getRegistrationNumber());
        regNumField.setSizeFull();
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
        flightCountField.setSizeFull();
        TextField typeField = new TextField();
        typeField.setLabel("Type");
        typeField.setSizeFull();
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
        nextCheckupFlightsField.setSizeFull();
        DatePicker nextCheckupDateField = new DatePicker();
        nextCheckupDateField.setLabel("Next Checkup Deadline");
        if (glider.getNextCheckupDate() != null) {
            nextCheckupDateField.setValue(glider.getNextCheckupDate().toLocalDate());
        }
        nextCheckupDateField.setSizeFull();
        formLayout.addFormRow(regNumField);
        regNumField.setSizeFull();
        formLayout.addFormRow(typeField);
        Span totalFlightTimeTitle = new Span("Total Flight Time");
        totalFlightTimeTitle.setSizeFull();
        formLayout.addFormRow(totalFlightTimeTitle);
        HorizontalLayout totalFlightTimeLayout = new HorizontalLayout();
        totalFlightTimeLayout.add(totalFlightTimeHrsField, totalFlightTimeMinsField);
        totalFlightTimeLayout.setSpacing(true);
        totalFlightTimeLayout.setSizeFull();
        formLayout.addFormRow(totalFlightTimeLayout);
        formLayout.addFormRow(flightCountField);
        Span checkupHrsTitle = new Span("Next Checkup in Hours");
        checkupHrsTitle.setSizeFull();
        formLayout.addFormRow(checkupHrsTitle);
        HorizontalLayout nextChecukpHrsLayout = new HorizontalLayout();
        nextChecukpHrsLayout.add(nextCheckupHrsHrsField, nextCheckupHrsMinsField);
        nextChecukpHrsLayout.setSpacing(true);
        nextChecukpHrsLayout.setSizeFull();
        formLayout.addFormRow(nextChecukpHrsLayout);
        formLayout.addFormRow(nextCheckupFlightsField);
        formLayout.addFormRow(nextCheckupDateField);

        //create buttons to confirm and cancel record editing
        Button editButton = new Button("Edit", e -> {
            //if any field has an invalid value, end lambda function early
            if(regNumField.isEmpty() || flightCountField.isEmpty() || totalFlightTimeHrsField.isInvalid() || totalFlightTimeMinsField.isInvalid() || typeField.isEmpty() || nextCheckupHrsHrsField.isInvalid() || nextCheckupFlightsField.isInvalid() || nextCheckupHrsMinsField.isInvalid() || nextCheckupDateField.isInvalid()) {
                Notification.show("At least one field value is invalid");
                return;
            }
            String regNum = regNumField.getValue();
            PGInterval totalFlightTime;
            try {
                String flightTime = totalFlightTimeHrsField.getValue()+" hours "+totalFlightTimeMinsField.getValue()+" minutes";
                totalFlightTime = new PGInterval(flightTime);
            } catch (SQLException ex) {
                Notification notification = Notification
                            .show("Error: " + ex.getErrorCode());
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                throw new RuntimeException(ex);
            }
            Integer flightCount = flightCountField.getValue();
            String type = typeField.getValue();
            PGInterval nextCheckupHrs;
            if(nextCheckupHrsMinsField.getValue() != null && nextCheckupHrsHrsField.getValue()!=null) {
                try {
                    nextCheckupHrs = new PGInterval(nextCheckupHrsHrsField.getValue() + " hours " + nextCheckupHrsMinsField.getValue() + " minutes");
                } catch (SQLException ex) {
                    Notification notification = Notification
                            .show("Error: " + ex.getErrorCode());
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
            glider.setRegistrationNumber(regNum);
            glider.setTotalFlightTime(totalFlightTime);
            glider.setType(type);
            glider.setFlightCount(flightCount);
            glider.setNextCheckupHrs(nextCheckupHrs);
            glider.setNextCheckupFlights(nextCheckupFlights);
            glider.setNextCheckupDate(nextCheckupDate);

            gliderService.editGlider(glider);
            editForm.close();
            UI.getCurrent().getPage().reload();
        });
        Button cancelButton = new Button("Cancel", e -> editForm.close());

        //add all UI elements, show form
        editForm.getFooter().add(cancelButton, editButton);
        editForm.add(formLayout);
        editForm.open();
    }

    //navigates the display to this view
    public static void showView() {
        UI.getCurrent().navigate(GlidersView.class);
    }
}