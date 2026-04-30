package com.example.application.base.ui;
import com.example.application.flights.Flight;
import com.example.application.flights.FlightService;
import com.example.application.gliders.Glider;
import com.example.application.gliders.GliderService;
import com.example.application.pilots.Pilot;
import com.example.application.pilots.PilotService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSingleSelectionModel;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import org.postgresql.util.PGInterval;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Route("building-apps/navigate/flights")
public class FlightsView extends VerticalLayout {

    private final FlightService flightService;
    @Autowired
    public FlightsView(FlightService flightService, GliderService gliderService, PilotService pilotService) {
        this.flightService = flightService;
        List<Flight> records = this.flightService.getAllFlights();
        Grid<Flight> grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        Grid.Column<Flight> IDColumn = grid
                .addColumn(Flight::getId).setHeader("ID")
                .setResizable(true).setSortable(true);
        //TODO make the glider, pilot1, pilot2 columns display registration number and names
        Grid.Column<Flight> gliderColumn = grid
                .addColumn(Flight::getGlider)
                .setHeader("Glider").setResizable(true).setSortable(true);
        Grid.Column<Flight> pilot1Column = grid
                .addColumn(Flight::getPilot1)
                .setHeader("Pilot 1").setResizable(true).setSortable(true);
        Grid.Column<Flight> pilot2Column = grid
                .addColumn(Flight::getPilot2)
                .setHeader("Pilot 2").setResizable(true).setSortable(true);
        Grid.Column<Flight> dateColumn = grid
                .addColumn(Flight::getDate)
                .setHeader("Date").setResizable(true).setSortable(true);
        Grid.Column<Flight> pointOfDepartureColumn = grid
                .addColumn(Flight::getPointOfDeparture)
                .setHeader("Point of Departure").setResizable(true).setSortable(true);
        Grid.Column<Flight> pointOfArrivalColumn = grid
                .addColumn(Flight::getPointOfArrival)
                .setHeader("Point of Arrival").setResizable(true).setSortable(true);
        Grid.Column<Flight> timeOfDepartureColumn = grid
                .addColumn(Flight::getTimeOfDeparture)
                .setHeader("Time of Departure").setResizable(true).setSortable(true);
        Grid.Column<Flight> timeOfArrivalColumn = grid
                .addColumn(Flight::getTimeOfArrival)
                .setHeader("Time of Arrival").setResizable(true)
                .setSortable(true);
        Grid.Column<Flight> flightTimeColumn = grid
                .addColumn(Flight::getTimeOfArrival)
                .setHeader("Flight Time").setResizable(true)
                .setSortable(true);
        Grid.Column<Flight> taskColumn = grid
                .addColumn(Flight::getTimeOfArrival)
                .setHeader("Task").setResizable(true)
                .setSortable(true);
        Grid.Column<Flight> preFlightCheckupColumn = grid
                .addColumn(Flight::getTimeOfArrival)
                .setHeader("Pre-Flight Checkup").setResizable(true)
                .setSortable(true);
        GridListDataView<Flight> dataView = grid.setItems(records);
        TextField searchField = new TextField();
        searchField.setWidth("250px");
        searchField.setLabel("Search:");
        Button addButton = new Button("Add Flight", e -> {
            try {
                showAdditionForm(gliderService, pilotService);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        Span status = new Span();
        status.setVisible(false);

        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete \"Report Q4\"?");
        dialog.setText(
                "Are you sure you want to permanently delete this item?");

        dialog.setCancelable(true);
        dialog.addCancelListener(event -> System.out.println("Cancel"));

        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> System.out.println("Delete"));

        Button testDialogButton = new Button("Open confirm dialog");
        testDialogButton.addClickListener(event -> {
            dialog.open();
            status.setVisible(false);
        });
        GridSingleSelectionModel<Flight> selectionModel = (GridSingleSelectionModel<Flight>)grid.getSelectionModel();
        Button deleteButton = new Button("Delete Glider", e -> {
            Long deletingId;
            if (selectionModel.getSelectedItem().isPresent()) {
                deletingId = selectionModel.getSelectedItem().get().getId();
                if (deletingId >= 0) {
                    //Dialog not working, deletion works
                    ConfirmDialog deleteDialog = new ConfirmDialog();
                    deleteDialog.setHeader("Delete Flight?");
                    deleteDialog.setText(
                            "Are you sure you want to permanently delete this item?");

                    deleteDialog.setCancelable(true);
                    //deleteDialog.addCancelListener(event -> deleteDialog.close());

                    deleteDialog.setConfirmText("Delete");
                    deleteDialog.setConfirmButtonTheme("error primary");
                    deleteDialog.addConfirmListener(event -> flightService.deleteFlight(deletingId));
                    deleteDialog.open();
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
                    showEditForm(gliderService, pilotService, selectionModel.getSelectedItem().get());
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

            boolean matchesID = String.valueOf(item.getId()).equals(searchTerm);
            boolean matchesDate = String.valueOf(item.getDate()).contains(searchTerm);
            boolean matchesGlider = item.getGlider().getRegistrationNumber().contains(searchTerm);
            boolean matchesPilot1 = item.getPilot1().getName().contains(searchTerm);
            boolean matchesPilot2 = item.getPilot2().getName().contains(searchTerm);
            boolean matchesPointOfDeparture = item.getPointOfDeparture().contains(searchTerm);
            boolean matchesPointOfArrival = item.getPointOfArrival().contains(searchTerm);
            boolean matchesTimeOfArrival = String.valueOf(item.getTimeOfArrival()).contains(searchTerm);
            boolean matchesTimeOfDeparture = String.valueOf(item.getTimeOfDeparture()).contains(searchTerm);
            boolean matchesFlightDuration = String.valueOf(item.getFlightTime()).contains(searchTerm);
            boolean matchesTask =  item.getTask().contains(searchTerm);
            boolean matchesPreFlightCheckup = item.getPreFlightCheckup().contains(searchTerm);

            return matchesPreFlightCheckup|| matchesTask || matchesFlightDuration || matchesTimeOfArrival || matchesID || matchesDate || matchesPointOfDeparture || matchesPilot2 || matchesGlider || matchesPilot1 || matchesPointOfArrival || matchesTimeOfDeparture;
        });
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSizeFull();
        buttonsLayout.add(new Button("Gliders", e -> GlidersView.showView()));
        buttonsLayout.add(searchField, addButton, deleteButton, editButton, testDialogButton);
        add(buttonsLayout, grid);
    }
    private void showAdditionForm(GliderService gliderService, PilotService pilotService) throws SQLException {
        Dialog additionForm = new Dialog();
        FormLayout formLayout = new FormLayout();
        formLayout.setAutoResponsive(true);
        formLayout.setExpandFields(true);
        ComboBox<Glider> gliderField = new ComboBox<>();
        gliderField.setLabel("Glider");
        gliderField.setRequired(true);
        gliderField.setItems(gliderService.getAllGliders());
        gliderField.setItemLabelGenerator(Glider::getRegistrationNumber);
        formLayout.setColspan(gliderField, 2);
        ComboBox<Pilot> pilot1Field = new ComboBox<>();
        pilot1Field.setLabel("Pilot 1");
        pilot1Field.setItems(pilotService.getAllPilots());
        pilot1Field.setRequired(true);
        pilot1Field.setItemLabelGenerator(Pilot::getName);
        ComboBox<Pilot> pilot2Field = new ComboBox<>();
        pilot2Field.setLabel("Pilot 2");
        pilot2Field.setItems(pilotService.getAllPilots());
        pilot2Field.setItemLabelGenerator(Pilot::getName);
        TimePicker timeOfDeparturePicker = new TimePicker();
        timeOfDeparturePicker.setLabel("Time of departure (UTC)");
        TimePicker timeOfArrivalPicker = new TimePicker();
        timeOfArrivalPicker.setLabel("Time of arrival (UTC)");
        formLayout.setColspan(timeOfDeparturePicker, 2);
        formLayout.setColspan(timeOfArrivalPicker, 2);
        timeOfArrivalPicker.setEnabled(false);
        TextField pointOfDepartureField = new TextField();
        pointOfDepartureField.setLabel("Point of departure");
        formLayout.setColspan(pointOfDepartureField, 2);
        pointOfDepartureField.setRequired(true);
        TextField pointOfArrivalField = new TextField();
        pointOfArrivalField.setLabel("Point of arrival");
        formLayout.setColspan(pointOfArrivalField, 2);
        pointOfArrivalField.setRequired(true);
        DatePicker dateField = new DatePicker();
        dateField.setLabel("Next Checkup Deadline");
        formLayout.setColspan(dateField, 2);
        dateField.setRequired(true);
        TextField taskField = new TextField();
        taskField.setLabel("Task");
        formLayout.setColspan(taskField, 2);
        taskField.setRequired(true);
        TextField preFlightCheckupField = new TextField();
        preFlightCheckupField.setLabel("Pre Flight Checkup");
        formLayout.setColspan(preFlightCheckupField, 2);
        preFlightCheckupField.setRequired(true);
        timeOfDeparturePicker.addValueChangeListener(e -> {
            timeOfArrivalPicker.setEnabled(timeOfDeparturePicker.getValue() != null);
        });
        formLayout.addFormRow(gliderField);
        formLayout.setColspan(gliderField, 2);
        formLayout.addFormRow(pilot1Field);
        formLayout.addFormRow(pilot2Field);
        formLayout.addFormRow(dateField);
        formLayout.addFormRow(taskField);
        formLayout.addFormRow(pointOfDepartureField);
        formLayout.addFormRow(pointOfArrivalField);
        formLayout.addFormRow(timeOfDeparturePicker);
        formLayout.addFormRow(timeOfArrivalPicker);
        formLayout.addFormRow(preFlightCheckupField);

        Button addButton = new Button("Add", e -> {
            Glider glider = gliderField.getValue();
            Pilot pilot1 = pilot1Field.getValue();
            Pilot pilot2 = pilot2Field.getValue();
            String status;
            if (timeOfDeparturePicker.getValue() == null) {
                status = "premade";
            } else if (timeOfArrivalPicker.getValue() == null) {
                status = "active";
            }
            else{
                status = "archival";
            }
            PGInterval flightDuration = null;
            String hours;
            String minutes;
            LocalTime timeOfArrival = timeOfArrivalPicker.getValue();
            LocalTime timeOfDeparture =  timeOfDeparturePicker.getValue();
            if(status.equals("archival")) {
                if (timeOfArrival.getHour() - timeOfDeparture.getHour() > 0 && timeOfArrival.getMinute() < timeOfDeparture.getMinute()) {
                    hours = String.valueOf(timeOfArrival.getHour() - timeOfDeparture.getHour() - 1);
                    minutes = String.valueOf(60 - timeOfArrival.getMinute() + timeOfDeparture.getMinute());
                } else {
                    hours = String.valueOf(timeOfArrival.getHour() - timeOfDeparture.getHour());
                    minutes = String.valueOf(timeOfArrival.getMinute() - timeOfDeparture.getMinute());
                }
                try {
                    flightDuration = new PGInterval(hours + " hours " + minutes + " minutes");
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            Date date = Date.valueOf(dateField.getValue());
            String pointOfArrival = pointOfArrivalField.getValue();
            String pointOfDeparture = pointOfDepartureField.getValue();
            String task = taskField.getValue();
            String preFlightCheckup = preFlightCheckupField.getValue();
            flightService.addFlight(glider, pilot1, pilot2, status, date, pointOfDeparture, pointOfArrival, Time.valueOf(timeOfDeparture), Time.valueOf(timeOfArrival), task, preFlightCheckup);
            try {
                gliderService.editGlider(glider, glider.getRegistrationNumber(), new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours()) + " hours" + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() + "minutes")), glider.getFlightCount(), glider.getType(), glider.getNextCheckupHrs(), glider.getNextCheckupFlights(), glider.getNextCheckupDate());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        Button cancelButton = new Button("Cancel", e -> additionForm.close());
        additionForm.getFooter().add(cancelButton, addButton);
        additionForm.add(formLayout);
        additionForm.open();

    }
    private void showEditForm(GliderService gliderService, PilotService pilotService, Flight flight) throws SQLException {
        Dialog editForm = new Dialog();
        FormLayout formLayout = new FormLayout();
        formLayout.setAutoResponsive(true);
        formLayout.setExpandFields(true);
        ComboBox<Glider> gliderField = new ComboBox<>();
        gliderField.setLabel("Glider");
        gliderField.setRequired(true);
        gliderField.setItems(gliderService.getAllGliders());
        gliderField.setItemLabelGenerator(Glider::getRegistrationNumber);
        gliderField.setValue(flight.getGlider());
        formLayout.setColspan(gliderField, 2);
        ComboBox<Pilot> pilot1Field = new ComboBox<>();
        pilot1Field.setLabel("Pilot 1");
        pilot1Field.setItems(pilotService.getAllPilots());
        pilot1Field.setRequired(true);
        pilot1Field.setValue(flight.getPilot1());
        pilot1Field.setItemLabelGenerator(Pilot::getName);
        ComboBox<Pilot> pilot2Field = new ComboBox<>();
        pilot2Field.setLabel("Pilot 2");
        pilot2Field.setItems(pilotService.getAllPilots());
        pilot2Field.setValue(flight.getPilot2());
        pilot2Field.setItemLabelGenerator(Pilot::getName);
        TimePicker timeOfDeparturePicker = new TimePicker();
        timeOfDeparturePicker.setLabel("Time of departure (UTC)");
        timeOfDeparturePicker.setValue(LocalTime.of(flight.getTimeOfDeparture().getHours(), flight.getTimeOfDeparture().getMinutes()));
        TimePicker timeOfArrivalPicker = new TimePicker();
        timeOfArrivalPicker.setLabel("Time of arrival (UTC)");
        timeOfArrivalPicker.setValue(LocalTime.of(flight.getTimeOfArrival().getHours(), flight.getTimeOfArrival().getMinutes()));
        formLayout.setColspan(timeOfDeparturePicker, 2);
        formLayout.setColspan(timeOfArrivalPicker, 2);
        timeOfArrivalPicker.setEnabled(false);
        TextField pointOfDepartureField = new TextField();
        pointOfDepartureField.setLabel("Point of departure");
        pointOfDepartureField.setValue(flight.getPointOfDeparture());
        formLayout.setColspan(pointOfDepartureField, 2);
        pointOfDepartureField.setRequired(true);
        TextField pointOfArrivalField = new TextField();
        pointOfArrivalField.setLabel("Point of arrival");
        formLayout.setColspan(pointOfArrivalField, 2);
        pointOfArrivalField.setRequired(true);
        pointOfArrivalField.setValue(flight.getPointOfArrival());
        DatePicker dateField = new DatePicker();
        dateField.setLabel("Next Checkup Deadline");
        formLayout.setColspan(dateField, 2);
        dateField.setRequired(true);
        dateField.setValue(LocalDate.of(flight.getDate().getYear(), flight.getDate().getMonth(), flight.getDate().getDay()));
        TextField taskField = new TextField();
        taskField.setLabel("Task");
        taskField.setValue(flight.getTask());
        formLayout.setColspan(taskField, 2);
        taskField.setRequired(true);
        TextField preFlightCheckupField = new TextField();
        preFlightCheckupField.setLabel("Pre Flight Checkup");
        preFlightCheckupField.setValue(flight.getPreFlightCheckup());
        formLayout.setColspan(preFlightCheckupField, 2);
        preFlightCheckupField.setRequired(true);
        timeOfDeparturePicker.addValueChangeListener(e -> {
            timeOfArrivalPicker.setEnabled(timeOfDeparturePicker.getValue() != null);
        });
        formLayout.addFormRow(gliderField);
        formLayout.setColspan(gliderField, 2);
        formLayout.addFormRow(pilot1Field);
        formLayout.addFormRow(pilot2Field);
        formLayout.addFormRow(dateField);
        formLayout.addFormRow(taskField);
        formLayout.addFormRow(pointOfDepartureField);
        formLayout.addFormRow(pointOfArrivalField);
        formLayout.addFormRow(timeOfDeparturePicker);
        formLayout.addFormRow(timeOfArrivalPicker);
        formLayout.addFormRow(preFlightCheckupField);

        Button editButton = new Button("Edit", e -> {
            Glider glider = gliderField.getValue();
            Pilot pilot1 = pilot1Field.getValue();
            Pilot pilot2 = pilot2Field.getValue();
            String status;
            if (timeOfDeparturePicker.getValue() == null) {
                status = "premade";
            } else if (timeOfArrivalPicker.getValue() == null) {
                status = "active";
            }
            else{
                status = "archival";
            }
            PGInterval flightDuration = null;
            String hours;
            String minutes;
            LocalTime timeOfArrival = timeOfArrivalPicker.getValue();
            LocalTime timeOfDeparture =  timeOfDeparturePicker.getValue();
            if(status.equals("archival")) {
                if (timeOfArrival.getHour() - timeOfDeparture.getHour() > 0 && timeOfArrival.getMinute() < timeOfDeparture.getMinute()) {
                    hours = String.valueOf(timeOfArrival.getHour() - timeOfDeparture.getHour() - 1);
                    minutes = String.valueOf(60 - timeOfArrival.getMinute() + timeOfDeparture.getMinute());
                } else {
                    hours = String.valueOf(timeOfArrival.getHour() - timeOfDeparture.getHour());
                    minutes = String.valueOf(timeOfArrival.getMinute() - timeOfDeparture.getMinute());
                }
                try {
                    flightDuration = new PGInterval(hours + " hours " + minutes + " minutes");
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            Date date = Date.valueOf(dateField.getValue());
            String pointOfArrival = pointOfArrivalField.getValue();
            String pointOfDeparture = pointOfDepartureField.getValue();
            String task = taskField.getValue();
            String preFlightCheckup = preFlightCheckupField.getValue();
            flightService.editFlight(flight, glider, pilot1, pilot2, date, pointOfDeparture, pointOfArrival, Time.valueOf(timeOfDeparture), Time.valueOf(timeOfArrival), task, preFlightCheckup);
        });
        Button cancelButton = new Button("Cancel", e -> editForm.close());
        editForm.getFooter().add(cancelButton, editButton);
        editForm.add(formLayout);
        editForm.open();
    }
    public static void showView() {
        UI.getCurrent().navigate(FlightsView.class);
    }
    public static void showViewFilter() {
        UI.getCurrent().navigate(FlightsView.class);
    }
}