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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import org.jspecify.annotations.NonNull;
import org.postgresql.util.PGInterval;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Route("building-apps/navigate/flights")
public class FlightsView extends VerticalLayout {

    private final FlightService flightService;
    private final PilotService pilotService;
    private final GliderService gliderService;

    public boolean isActiveFilter = false;
    public boolean isArchivalFilter = false;
    private Pilot pilot1Filter;
    private Pilot pilot2Filter;
    private Pilot anyPilotFilter;
    private Date dateFilter;
    private String pointOfDepartureFilter;
    private String pointOfArrivalFilter;
    private Time timeOfDepartureFilter;
    private Time timeOfArrivalFilter;
    private PGInterval flightTimeFilter;
    private String taskFilter;
    private String preFlightCheckupFilter;
    private Glider gliderFilter;

    @Autowired
    public FlightsView(FlightService flightService, GliderService gliderService, PilotService pilotService) {
        this.flightService = flightService;
        this.pilotService = pilotService;
        this.gliderService = gliderService;

        List<Flight> records = this.flightService.getAllFlights();
        Grid<Flight> grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        Grid.Column<Flight> IDColumn = grid
                .addColumn(Flight::getId).setHeader("ID")
                .setResizable(true).setSortable(true);
        Grid.Column<Flight> gliderColumn = grid
                .addColumn((Flight flight) -> flight.getGlider().getRegistrationNumber())
                .setHeader("Glider").setResizable(true).setSortable(true);
        Grid.Column<Flight> pilot1Column = grid
                .addColumn((Flight flight) -> flight.getPilot1().getName())
                .setHeader("Pilot 1").setResizable(true).setSortable(true);
        Grid.Column<Flight> pilot2Column = grid
                .addColumn(flight -> {
                    if(flight.getPilot2() != null){
                        return flight.getPilot2().getName();
                    }
                    else {
                        return null;
                    }
                })
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
                .addColumn(Flight::getFlightTime)
                .setHeader("Flight Time").setResizable(true)
                .setSortable(true);
        Grid.Column<Flight> taskColumn = grid
                .addColumn(Flight::getTask)
                .setHeader("Task").setResizable(true)
                .setSortable(true);
        Grid.Column<Flight> preFlightCheckupColumn = grid
                .addColumn(Flight::getPreFlightCheckup)
                .setHeader("Pre-Flight Checkup").setResizable(true)
                .setSortable(true);
        GridListDataView<Flight> dataView = grid.setItems(records);

        Button addButton = new Button("Add Flight", e -> {
            try {
                showAdditionForm(gliderService, pilotService);
            } catch (SQLException ex) {
                Notification notification = Notification
                        .show("Error: " + ex.getErrorCode());
                throw new RuntimeException(ex);
            }
        });

        GridSingleSelectionModel<Flight> selectionModel = (GridSingleSelectionModel<Flight>)grid.getSelectionModel();

        Button deleteButton = new Button("Delete Flight", e -> {
            Long deletingId;
            if (selectionModel.getSelectedItem().isPresent()) {
                deletingId = selectionModel.getSelectedItem().get().getId();
                if (deletingId >= 0) {
                    ConfirmDialog deleteDialog = getDeleteDialog(flightService, selectionModel.getSelectedItem().get().getId(), gliderService);
                    deleteDialog.open();
                }
            }

        });
        grid.addSelectionListener(e -> {
            deleteButton.setEnabled(true);
            if(e.getFirstSelectedItem().isEmpty()){
                deleteButton.setEnabled(false);
            }
        });
        Button editButton = new Button("Edit Flight", e -> {
            if (selectionModel.getSelectedItem().isPresent()) {
                try {
                    showEditForm(gliderService, pilotService, selectionModel.getSelectedItem().get());
                } catch (SQLException ex) {
                    Notification notification = Notification
                            .show("Error: " + ex.getErrorCode()); notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    throw new RuntimeException(ex);
                }
            }
        });
        Button launchButton = new Button("Launch", e -> {
            if(selectionModel.getSelectedItem().isPresent() && !(selectionModel.getSelectedItem().get().isActive || selectionModel.getSelectedItem().get().isArchival)){
                Flight flight = selectionModel.getSelectedItem().get();
                Glider glider =  selectionModel.getSelectedItem().get().getGlider();
                Pilot pilot1 =  selectionModel.getSelectedItem().get().getPilot1();
                Pilot pilot2 =  selectionModel.getSelectedItem().get().getPilot2();
                Date date =  selectionModel.getSelectedItem().get().getDate();
                String pointOfDeparture = selectionModel.getSelectedItem().get().getPointOfDeparture();
                String pointOfArrival = selectionModel.getSelectedItem().get().getPointOfArrival();
                LocalTime timeOfDeparture = LocalTime.now(ZoneId.of("UTC"));
                LocalTime timeOfArrival = null;
                String task = selectionModel.getSelectedItem().get().getTask();
                String preFlightCheckup = selectionModel.getSelectedItem().get().getPreFlightCheckup();
                editFlightLogic(flight, glider, pilot1, pilot2, date, pointOfDeparture, pointOfArrival, timeOfDeparture,  timeOfArrival, task, preFlightCheckup);
            }
        });
        Button landButton = new Button("Land", e -> {
            if(selectionModel.getSelectedItem().isPresent() && selectionModel.getSelectedItem().get().isActive){
                Flight flight = selectionModel.getSelectedItem().get();
                Glider glider =  selectionModel.getSelectedItem().get().getGlider();
                Pilot pilot1 =  selectionModel.getSelectedItem().get().getPilot1();
                Pilot pilot2 =  selectionModel.getSelectedItem().get().getPilot2();
                Date date =  selectionModel.getSelectedItem().get().getDate();
                String pointOfDeparture = selectionModel.getSelectedItem().get().getPointOfDeparture();
                String pointOfArrival = selectionModel.getSelectedItem().get().getPointOfArrival();
                LocalTime timeOfDeparture = flight.getTimeOfDeparture().toLocalTime();
                LocalTime timeOfArrival = LocalTime.now(ZoneId.of("UTC"));
                String task = selectionModel.getSelectedItem().get().getTask();
                String preFlightCheckup = selectionModel.getSelectedItem().get().getPreFlightCheckup();
                editFlightLogic(flight, glider, pilot1, pilot2, date, pointOfDeparture, pointOfArrival, timeOfDeparture,  timeOfArrival, task, preFlightCheckup);
                UI.getCurrent().getPage().reload();
            }
        });
        Button filterButton = new Button("Filter", e -> showFilterForm(dataView));
        grid.addSelectionListener(e -> {
            deleteButton.setEnabled(true);
            editButton.setEnabled(true);
            if(e.getFirstSelectedItem().isPresent()) {
                if (!(selectionModel.getSelectedItem().get().isActive || selectionModel.getSelectedItem().get().isArchival)) {
                    launchButton.setEnabled(true);
                }
                if (selectionModel.getSelectedItem().get().isActive) {
                    landButton.setEnabled(true);
                }
            }
            if(e.getFirstSelectedItem().isEmpty()){
                deleteButton.setEnabled(false);
                editButton.setEnabled(false);
                launchButton.setEnabled(false);
                landButton.setEnabled(false);
            }
        });

        deleteButton.setEnabled(false);
        editButton.setEnabled(false);
        launchButton.setEnabled(false);
        landButton.setEnabled(false);

        Tab premade = new Tab("Pre-Made");
        Tab active = new Tab("Active");
        Tab archival = new Tab("Archival");

        Tabs tabs = new Tabs(premade, active, archival);
        tabs.addSelectedChangeListener(e -> {
            if (tabs.getSelectedTab() == premade) {
                //set global filters
                isActiveFilter = false;
                isArchivalFilter = false;
            }
            else if (tabs.getSelectedTab() == active) {
                //set global filters
                isActiveFilter = true;
                isArchivalFilter = false;
            }
            if (tabs.getSelectedTab() == archival) {
                //set global filters
                isActiveFilter = false;
                isArchivalFilter = true;
            }
            dataView.refreshAll();
        });
        TextField searchField = new TextField();
        searchField.setWidth("250px");
        searchField.setLabel("Search:");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> dataView.refreshAll());
        //Data filtering
        dataView.addFilter(item -> {
            String searchTerm = searchField.getValue().trim();
            //Search Field handling
            boolean matchesID = true;
            boolean matchesGlider = true;
            boolean matchesPilot1 = true;
            boolean matchesPilot2 = true;
            boolean matchesDate = true;
            boolean matchesPointOfDeparture = true;
            boolean matchesPointOfArrival = true;
            boolean matchesTimeOfArrival = true;
            boolean matchesTimeOfDeparture = true;
            boolean matchesFlightDuration = true;
            boolean matchesTask = true;
            boolean matchesPreFlightCheckup = true;
            if (!searchTerm.isEmpty()) {
                matchesID = String.valueOf(item.getId()).equals(searchTerm);
                matchesDate = String.valueOf(item.getDate()).contains(searchTerm);
                matchesGlider = item.getGlider().getRegistrationNumber().contains(searchTerm);
                matchesPilot1 = item.getPilot1().getName().contains(searchTerm);
                matchesPilot2 = false;
                if (item.getPilot2() != null) {
                    matchesPilot2 = item.getPilot2().getName().contains(searchTerm);
                }
                matchesPointOfDeparture = item.getPointOfDeparture().contains(searchTerm);
                matchesPointOfArrival = item.getPointOfArrival().contains(searchTerm);
                if(item.getTimeOfDeparture() != null) {
                    matchesTimeOfDeparture = String.valueOf(item.getTimeOfDeparture()).contains(searchTerm);
                    if(item.getTimeOfArrival() != null) {
                        matchesTimeOfArrival = String.valueOf(item.getTimeOfArrival()).contains(searchTerm);
                    }
                }
                matchesFlightDuration = String.valueOf(item.getFlightTime()).contains(searchTerm);
                matchesTask = item.getTask().contains(searchTerm);
                matchesPreFlightCheckup = item.getPreFlightCheckup().contains(searchTerm);
            }
            //Filter functionality
            boolean matchesFilter = (item.isActive == isActiveFilter) && (item.isArchival ==  isArchivalFilter);
            //pilot1Filter
            if(pilot1Filter != null){
                matchesFilter = matchesFilter && pilot1Filter.equals(item.getPilot1());
            }
            //pilot2Filter;
            if(pilot2Filter != null){
                matchesFilter = matchesFilter && pilot2Filter.equals(item.getPilot2());
            }
            //anyPilotFilter
            if(anyPilotFilter != null){
                matchesFilter = matchesFilter && (anyPilotFilter.equals(item.getPilot1()) || anyPilotFilter.equals(item.getPilot2()));
            }
            //dateFilter
            if (dateFilter != null) {
                matchesFilter = matchesFilter && dateFilter.equals(item.getDate());
            }
            //pointOfDepartureFilter
            if (pointOfDepartureFilter != null) {
                matchesFilter = matchesFilter && pointOfDepartureFilter.equals(item.getPointOfDeparture());
            }
            //pointOfArrivalFilter
            if (pointOfArrivalFilter != null) {
                matchesFilter =  matchesFilter && pointOfArrivalFilter.equals(item.getPointOfArrival());
            }
            //timeOfDepartureFilter
            if (timeOfDepartureFilter != null) {
                matchesFilter =  matchesFilter && timeOfDepartureFilter.equals(item.getTimeOfDeparture());
            }
            //timeOfArrivalFilter
            if (timeOfArrivalFilter != null) {
                matchesFilter =  matchesFilter && timeOfArrivalFilter.equals(item.getTimeOfArrival());
            }
            //flightTimeFilter
            if (flightTimeFilter != null) {
                matchesFilter = matchesFilter && flightTimeFilter.equals(item.getFlightTime());
            }
            //taskFilter
            if (taskFilter != null) {
                matchesFilter = matchesFilter && taskFilter.equals(item.getTask());
            }
            //preFlightCheckupFilter
            if (preFlightCheckupFilter != null) {
                matchesFilter = matchesFilter && preFlightCheckupFilter.equals(item.getPreFlightCheckup());
            }
            //gliderFilter
            if (gliderFilter != null) {
                matchesFilter = matchesFilter && gliderFilter.equals(item.getGlider());
            }
            return (matchesPreFlightCheckup|| matchesTask || matchesFlightDuration || matchesTimeOfArrival ||
                    matchesID || matchesDate || matchesPointOfDeparture || matchesPilot2 || matchesGlider ||
                    matchesPilot1 || matchesPointOfArrival || matchesTimeOfDeparture) && matchesFilter;
        });
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSizeFull();
        buttonsLayout.add(new Button("Gliders", e -> GlidersView.showView()));
        buttonsLayout.add(new Button("Pilots", e -> PilotsView.showView()));
        buttonsLayout.add(addButton, deleteButton, editButton, searchField, filterButton, launchButton, landButton);
        add(buttonsLayout, tabs, grid);

    }

    private static @NonNull ConfirmDialog getDeleteDialog(FlightService flightService, Long deletingId, GliderService gliderService) {
        ConfirmDialog deleteDialog = new ConfirmDialog();
        deleteDialog.setHeader("Delete Flight?");
        deleteDialog.setText("Are you sure you want to permanently delete this item?");

        deleteDialog.setCancelable(true);
        deleteDialog.addCancelListener(event -> deleteDialog.close());
        deleteDialog.setConfirmText("Delete");
        deleteDialog.setConfirmButtonTheme("error primary");
        deleteDialog.addConfirmListener(event -> {
            try {
                if(flightService.getFlightById(deletingId).isArchival){
                    Flight flight = flightService.getFlightById(deletingId);
                    Glider glider = flight.getGlider();
                    Glider editedGlider = glider;
                    if(editedGlider.getTotalFlightTime().getMinutes() - flight.getFlightTime().getMinutes() < 0) {
                        editedGlider.setTotalFlightTime(new PGInterval((editedGlider.getTotalFlightTime().getHours() - flight.getFlightTime().getHours() - 1) + " hours " + (flight.getFlightTime().getMinutes() - editedGlider.getTotalFlightTime().getMinutes()) + " minutes"));
                    }
                    else{
                        editedGlider.setTotalFlightTime(new PGInterval((editedGlider.getTotalFlightTime().getHours() - flight.getFlightTime().getHours()) + " hours " + (editedGlider.getTotalFlightTime().getMinutes() - flight.getFlightTime().getMinutes()) + " minutes"));
                    }
                    editedGlider.setFlightCount(editedGlider.getFlightCount() - 1);
                    gliderService.editGlider(glider, editedGlider);
                }
            } catch (SQLException e) {
                Notification notification = Notification
                        .show("Error: " + e.getErrorCode());
                throw new RuntimeException(e);
            }
            flightService.deleteFlight(deletingId);
            UI.getCurrent().getPage().reload();
        });
        return deleteDialog;
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
        timeOfDeparturePicker.addValueChangeListener(e -> timeOfArrivalPicker.setMin(e.getValue()));
        timeOfArrivalPicker.addValueChangeListener(e -> timeOfDeparturePicker.setMax(e.getValue()));
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
        dateField.setLabel("Date");
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
            boolean isActive;
            boolean isArchival;
            if (timeOfDeparturePicker.getValue() == null) {
                isActive = false;
                isArchival = false;
            } else if (timeOfArrivalPicker.getValue() == null) {
                isActive = true;
                isArchival = false;
            }
            else{
                isActive = false;
                isArchival = true;
            }
            PGInterval flightDuration = null;
            String hours;
            String minutes;
            LocalTime timeOfArrival = timeOfArrivalPicker.getValue();
            LocalTime timeOfDeparture =  timeOfDeparturePicker.getValue();
            if(isArchival) {
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
                    Notification notification = Notification
                            .show("Error: " + ex.getErrorCode()); notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    throw new RuntimeException(ex);
                }
            }
            Date date = Date.valueOf(dateField.getValue());
            String pointOfArrival = pointOfArrivalField.getValue();
            String pointOfDeparture = pointOfDepartureField.getValue();
            String task = taskField.getValue();
            String preFlightCheckup = preFlightCheckupField.getValue();
            Time sqlTimeOfDeparture = null;
            if(timeOfDeparture != null){
                sqlTimeOfDeparture = Time.valueOf(timeOfDeparture);
            }
            Time sqlTimeOfArrival = null;
            if(timeOfArrival != null){
                sqlTimeOfArrival = Time.valueOf(timeOfArrival);
            }
            Flight flight = new Flight(glider, pilot1, pilot2, date, pointOfDeparture, pointOfArrival, sqlTimeOfDeparture, sqlTimeOfArrival, task, preFlightCheckup);
            flight.isActive = isActive;
            flight.isArchival = isArchival;
            if(isArchival) {
                flight.setFlightTime(flightDuration);
            }
            try{
                List<Flight> filteredByGlider = flightService.getArchivalFlightsByGliderAndDate(glider, date);
                List<Flight> filteredByPilot1 = flightService.getArchivalFlightsByPilotAndDate(pilot1, date);
                List<Flight> filteredByPilot2 = null;
                if (pilot2 != null){
                    filteredByPilot2 = flightService.getArchivalFlightsByPilotAndDate(pilot2, date);
                }
                if(flight.validateAddition(filteredByGlider, filteredByPilot1, filteredByPilot2)) {
                    String checkupChecker = flight.checkNextCheckup();
                    if (checkupChecker == null) {
                        flightService.addFlight(flight);
                        if(isArchival) {
                            try {
                                if (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() < 60) {
                                    Glider editGlider = new Glider(glider.getRegistrationNumber(), new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours()) + " hours" + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes()) + "minutes"), glider.getFlightCount(), glider.getType(), glider.getNextCheckupHrs(), glider.getNextCheckupFlights(), glider.getNextCheckupDate(), false);
                                    editGlider.setFlightCount(glider.getFlightCount() + 1);
                                    gliderService.editGlider(glider, editGlider);
                                } else {
                                    Glider editGlider = new Glider(glider.getRegistrationNumber(), new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours() + 1) + " hours" + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - 60) + "minutes"), glider.getFlightCount(), glider.getType(), glider.getNextCheckupHrs(), glider.getNextCheckupFlights(), glider.getNextCheckupDate(), false);
                                    editGlider.setFlightCount(glider.getFlightCount() + 1);
                                    gliderService.editGlider(glider, editGlider);
                                }
                            } catch (SQLException ex) {
                                Notification notification = Notification
                                        .show("Error: " + ex.getErrorCode()); notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                                throw new RuntimeException(ex);
                            }
                        }
                        additionForm.close();
                        UI.getCurrent().getPage().reload();
                    }
                    else {
                        if (!checkupChecker.contains("overdue")) {
                            flightService.addFlight(flight);
                            if(isArchival) {
                                try {
                                    if (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() < 60) {
                                        Glider editGlider = new Glider(glider.getRegistrationNumber(), new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours()) + " hours" + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes()) + "minutes"), glider.getFlightCount(), glider.getType(), glider.getNextCheckupHrs(), glider.getNextCheckupFlights(), glider.getNextCheckupDate(), false);
                                        editGlider.setFlightCount(glider.getFlightCount() + 1);
                                        gliderService.editGlider(glider, editGlider);
                                    } else {
                                        Glider editGlider = new Glider(glider.getRegistrationNumber(), new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours() + 1) + " hours" + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - 60) + "minutes"), glider.getFlightCount(), glider.getType(), glider.getNextCheckupHrs(), glider.getNextCheckupFlights(), glider.getNextCheckupDate(), false);
                                        editGlider.setFlightCount(glider.getFlightCount() + 1);
                                        gliderService.editGlider(glider, editGlider);
                                    }
                                } catch (SQLException ex) {
                                    Notification notification = Notification
                                            .show("Error: " + ex.getErrorCode());
                                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                                    throw new RuntimeException(ex);
                                }
                            }
                            additionForm.close();
                            UI.getCurrent().getPage().reload();
                            Notification notification = Notification.show(checkupChecker);
                            notification.addThemeVariants(NotificationVariant.LUMO_WARNING);

                        }
                        else {
                            additionForm.close();
                            UI.getCurrent().getPage().reload();
                            Notification notification = Notification.show(checkupChecker);
                            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                        }
                    }
                }
                else{
                    Notification notification = Notification
                            .show("The submitted record conflicts with at least one preexisting record");
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            }
            catch (SQLException ex) {
                Notification notification = Notification
                        .show("Error: " + ex.getErrorCode());
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
        if(flight.getTimeOfDeparture()!=null) {
            timeOfDeparturePicker.setValue(LocalTime.of(flight.getTimeOfDeparture().getHours(), flight.getTimeOfDeparture().getMinutes()));
        }
        TimePicker timeOfArrivalPicker = new TimePicker();
        timeOfArrivalPicker.setLabel("Time of arrival (UTC)");
        if(flight.getTimeOfArrival()!=null) {
            timeOfArrivalPicker.setValue(LocalTime.of(flight.getTimeOfArrival().getHours(), flight.getTimeOfArrival().getMinutes()));
        }
        formLayout.setColspan(timeOfDeparturePicker, 2);
        formLayout.setColspan(timeOfArrivalPicker, 2);
        timeOfArrivalPicker.setEnabled(timeOfDeparturePicker.getValue() != null);
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
        timeOfDeparturePicker.addValueChangeListener(e -> timeOfArrivalPicker.setMin(e.getValue()));
        timeOfArrivalPicker.addValueChangeListener(e -> timeOfDeparturePicker.setMax(e.getValue()));
        DatePicker dateField = new DatePicker();
        dateField.setLabel("Date");
        formLayout.setColspan(dateField, 2);
        dateField.setRequired(true);
        dateField.setValue(flight.getDate().toLocalDate());
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
            LocalTime timeOfArrival = timeOfArrivalPicker.getValue();
            LocalTime timeOfDeparture =  timeOfDeparturePicker.getValue();
            Date date = Date.valueOf(dateField.getValue());
            String pointOfArrival = pointOfArrivalField.getValue();
            String pointOfDeparture = pointOfDepartureField.getValue();
            String task = taskField.getValue();
            String preFlightCheckup = preFlightCheckupField.getValue();
            editFlightLogic(flight, glider, pilot1, pilot2, date, pointOfDeparture, pointOfArrival, timeOfDeparture, timeOfArrival, task, preFlightCheckup);
            editForm.close();
            UI.getCurrent().getPage().reload();
        });
        Button cancelButton = new Button("Cancel", e -> editForm.close());
        editForm.getFooter().add(cancelButton, editButton);
        editForm.add(formLayout);
        editForm.open();
    }

    private void showFilterForm(GridListDataView<Flight> dataView) {
        Dialog filterForm = new Dialog();
        FormLayout formLayout = new FormLayout();
        formLayout.setAutoResponsive(true);
        formLayout.setExpandFields(true);
        ComboBox<Glider> gliderField = new ComboBox<>();
        gliderField.setLabel("Glider");
        gliderField.setItems(gliderService.getAllGliders());
        gliderField.setItemLabelGenerator(Glider::getRegistrationNumber);
        if(gliderFilter != null) {
            gliderField.setValue(gliderFilter);
        }
        formLayout.setColspan(gliderField, 2);
        ComboBox<Pilot> pilot1Field = new ComboBox<>();
        pilot1Field.setLabel("Pilot 1");
        pilot1Field.setItems(pilotService.getAllPilots());
        if(pilot1Filter != null) {
            pilot1Field.setValue(pilot1Filter);
        }
        pilot1Field.setItemLabelGenerator(Pilot::getName);
        ComboBox<Pilot> pilot2Field = new ComboBox<>();
        pilot2Field.setLabel("Pilot 2");
        pilot2Field.setItems(pilotService.getAllPilots());
        if(pilot2Filter != null) {
            pilot2Field.setValue(pilot2Filter);
        }
        pilot2Field.setItemLabelGenerator(Pilot::getName);
        ComboBox<Pilot> eitherPilotField = new ComboBox<>();
        eitherPilotField.setLabel("Either Pilot");
        eitherPilotField.setItems(pilotService.getAllPilots());
        if(anyPilotFilter != null) {
            eitherPilotField.setValue(anyPilotFilter);
        }
        eitherPilotField.setItemLabelGenerator(Pilot::getName);
        TimePicker timeOfDeparturePicker = new TimePicker();
        timeOfDeparturePicker.setLabel("Time of departure (UTC)");
        if(timeOfDepartureFilter != null) {
            timeOfDeparturePicker.setValue(timeOfDepartureFilter.toLocalTime());
        }
        TimePicker timeOfArrivalPicker = new TimePicker();
        timeOfArrivalPicker.setLabel("Time of arrival (UTC)");
        if(timeOfArrivalFilter != null) {
            timeOfArrivalPicker.setValue(timeOfArrivalFilter.toLocalTime());
        }
        formLayout.setColspan(timeOfDeparturePicker, 2);
        formLayout.setColspan(timeOfArrivalPicker, 2);
        TextField pointOfDepartureField = new TextField();
        pointOfDepartureField.setLabel("Point of departure");
        if(pointOfDepartureFilter != null) {
            pointOfDepartureField.setValue(pointOfDepartureFilter);
        }
        formLayout.setColspan(pointOfDepartureField, 2);
        TextField pointOfArrivalField = new TextField();
        pointOfArrivalField.setLabel("Point of arrival");
        formLayout.setColspan(pointOfArrivalField, 2);
        if (pointOfArrivalFilter != null) {
            pointOfArrivalField.setValue(pointOfArrivalFilter);
        }
        timeOfDeparturePicker.addValueChangeListener(e -> timeOfArrivalPicker.setMin(e.getValue()));
        timeOfArrivalPicker.addValueChangeListener(e -> timeOfDeparturePicker.setMax(e.getValue()));
        DatePicker dateField = new DatePicker();
        dateField.setLabel("Date");
        formLayout.setColspan(dateField, 2);
        if (dateFilter != null) {
            dateField.setValue(dateFilter.toLocalDate());
        }
        TextField taskField = new TextField();
        taskField.setLabel("Task");
        if(taskFilter != null) {
            taskField.setValue(taskFilter);
        }
        formLayout.setColspan(taskField, 2);
        TextField preFlightCheckupField = new TextField();
        preFlightCheckupField.setLabel("Pre Flight Checkup");
        if(preFlightCheckupFilter != null) {
            preFlightCheckupField.setValue(preFlightCheckupFilter);
        }
        IntegerField flightDurationHrsField = new IntegerField();
        flightDurationHrsField.setLabel("Hours");
        flightDurationHrsField.setMin(0);
        flightDurationHrsField.setRequired(true);
        IntegerField flightDurationMinsField = new IntegerField();
        flightDurationMinsField.setLabel("Minutes");
        flightDurationMinsField.setMin(0);
        flightDurationMinsField.setMax(59);
        flightDurationMinsField.setRequired(true);
        formLayout.setColspan(preFlightCheckupField, 2);
        formLayout.addFormRow(gliderField);
        formLayout.addFormRow(pilot1Field);
        formLayout.addFormRow(pilot2Field);
        formLayout.addFormRow(eitherPilotField);
        formLayout.addFormRow(dateField);
        formLayout.addFormRow(taskField);
        formLayout.addFormRow(pointOfDepartureField);
        formLayout.addFormRow(pointOfArrivalField);
        formLayout.addFormRow(timeOfDeparturePicker);
        formLayout.addFormRow(timeOfArrivalPicker);
        formLayout.addFormRow(new Span("Flight Duration"));
        formLayout.addFormRow(flightDurationHrsField, flightDurationMinsField);
        formLayout.addFormRow(preFlightCheckupField);
        Button confirmButton = new Button("Confirm", e -> {
            gliderFilter = gliderField.getValue();
            pilot1Filter = pilot1Field.getValue();
            pilot2Filter = pilot2Field.getValue();
            anyPilotFilter = eitherPilotField.getValue();
            if (dateField.getValue() != null) {
                dateFilter = Date.valueOf(dateField.getValue());
            }
            if (timeOfDeparturePicker.getValue() != null) {
                timeOfDepartureFilter = Time.valueOf(timeOfDeparturePicker.getValue());
            }
            if (timeOfArrivalPicker.getValue() != null ) {
            timeOfArrivalFilter = Time.valueOf(timeOfArrivalPicker.getValue());
            }
            PGInterval flightDuration;
            if(flightDurationMinsField.getValue() != null && flightDurationHrsField.getValue()!=null) {
                try {
                    flightDuration = new PGInterval(flightDurationHrsField.getValue() + " hours" + flightDurationMinsField.getValue() + " minutes");
                } catch (SQLException ex) {
                    Notification notification = Notification
                            .show("Error: " + ex.getErrorCode()); notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    throw new RuntimeException(ex);
                }
            }
            else {
                flightDuration = null;
            }
            flightTimeFilter = flightDuration;
            pointOfDepartureFilter = pointOfDepartureField.getValue();
            pointOfArrivalFilter = pointOfArrivalField.getValue();
            taskFilter = taskField.getValue();
            preFlightCheckupFilter = preFlightCheckupField.getValue();
            filterForm.close();
            dataView.refreshAll();
        });
        Button cancelButton = new Button("Cancel", e -> filterForm.close());
        filterForm.add(formLayout);
        filterForm.getFooter().add(cancelButton, confirmButton);
        filterForm.open();
    }

    private void editFlightLogic(Flight flight, Glider glider, Pilot pilot1, Pilot pilot2, Date date, String pointOfDeparture, String pointOfArrival, LocalTime timeOfDeparture, LocalTime timeOfArrival, String task, String preFlightCheckup) {
        Time sqlTimeOfDeparture = null;
        if (timeOfDeparture != null) {
            sqlTimeOfDeparture = Time.valueOf(timeOfDeparture);
        }
        Time sqlTimeOfArrival = null;
        if (timeOfArrival != null) {
            sqlTimeOfArrival = Time.valueOf(timeOfArrival);
        }
        Flight editedFlight = new Flight(glider, pilot1, pilot2, date, pointOfDeparture, pointOfArrival, sqlTimeOfDeparture, sqlTimeOfArrival, task, preFlightCheckup);
        boolean isActive;
        boolean isArchival;
        try {
            if (timeOfDeparture == null) {
                isActive = false;
                isArchival = false;
            } else if (timeOfArrival == null) {
                isActive = true;
                isArchival = false;
            } else {
                isActive = false;
                isArchival = true;
            }
            String hours;
            String minutes;
            PGInterval flightDuration = null;
            if (isArchival) {
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
                    Notification notification = Notification
                            .show("Error: " + ex.getErrorCode());
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    throw new RuntimeException(ex);
                }
            }
            PGInterval prevFlightTime = flight.getFlightTime();
            List<Flight> filteredByGlider = flightService.getArchivalFlightsByGliderAndDate(glider, date);
            List<Flight> filteredByPilot1 = flightService.getArchivalFlightsByPilotAndDate(pilot1, date);
            List<Flight> filteredByPilot2 = null;
            if (pilot2 != null) {
                filteredByPilot2 = flightService.getArchivalFlightsByPilotAndDate(pilot2, date);
            }
            if (flight.validateEdit(flight, filteredByGlider, filteredByPilot1, filteredByPilot2)) {
                flightService.editFlight(flight, editedFlight);String checkupChecker = flight.checkNextCheckup();
                if (checkupChecker == null) {
                    flightService.addFlight(flight);
                    if (isArchival) {
                        if (prevFlightTime != null) {
                            try {
                                if (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes() < 60 && flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes() > 0) {
                                    Glider editedGlider = new Glider(glider.getRegistrationNumber(), new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours() - prevFlightTime.getHours()) + " hours" + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes()) + "minutes"), glider.getFlightCount(), glider.getType(), glider.getNextCheckupHrs(), glider.getNextCheckupFlights(), glider.getNextCheckupDate(), false);
                                    gliderService.editGlider(glider, editedGlider);
                                } else if (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes() > 60) {
                                    Glider editedGlider = new Glider(glider.getRegistrationNumber(), new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours() - prevFlightTime.getHours() + 1) + " hours" + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes() - 60) + "minutes"), glider.getFlightCount(), glider.getType(), glider.getNextCheckupHrs(), glider.getNextCheckupFlights(), glider.getNextCheckupDate(), false);
                                    gliderService.editGlider(glider, editedGlider);
                                } else if (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes() < 0) {
                                    Glider editedGlider = new Glider(glider.getRegistrationNumber(), new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours() - prevFlightTime.getHours() + 1) + " hours" + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes() + 60) + "minutes"), glider.getFlightCount(), glider.getType(), glider.getNextCheckupHrs(), glider.getNextCheckupFlights(), glider.getNextCheckupDate(), false);
                                    gliderService.editGlider(glider, editedGlider);
                                }
                            } catch (SQLException ex) {
                                Notification notification = Notification
                                        .show("Error: " + ex.getErrorCode());
                                throw new RuntimeException(ex);
                            }
                        }
                        else{
                            try {
                                if (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() < 60) {
                                    Glider editGlider = new Glider(glider.getRegistrationNumber(), new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours()) + " hours" + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes()) + "minutes"), glider.getFlightCount(), glider.getType(), glider.getNextCheckupHrs(), glider.getNextCheckupFlights(), glider.getNextCheckupDate(), false);
                                    gliderService.editGlider(glider, editGlider);
                                } else {
                                    Glider editGlider = new Glider(glider.getRegistrationNumber(), new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours() + 1) + " hours" + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - 60) + "minutes"), glider.getFlightCount(), glider.getType(), glider.getNextCheckupHrs(), glider.getNextCheckupFlights(), glider.getNextCheckupDate(), false);
                                    gliderService.editGlider(glider, editGlider);
                                }
                            } catch (SQLException ex) {
                                Notification notification = Notification
                                        .show("Error: " + ex.getErrorCode());
                                throw new RuntimeException(ex);
                            }
                        }
                    }
                    pilotService.editPilot(flight.getPilot1(), flight.getPilot1().getName(), flight.getPilot1().getLicenseNumber(), false);
                    if (flight.getPilot2() != null) {
                        pilotService.editPilot(flight.getPilot2(), flight.getPilot2().getName(), flight.getPilot2().getLicenseNumber(), false);
                    }
                    if (isActive) {
                        pilotService.editPilot(editedFlight.getPilot1(), editedFlight.getPilot1().getName(), editedFlight.getPilot1().getLicenseNumber(), true);
                        if (editedFlight.getPilot2() != null) {
                            pilotService.editPilot(flight.getPilot2(), flight.getPilot2().getName(), flight.getPilot2().getLicenseNumber(), false);
                        }
                    }
                }
                else {
                    if (!checkupChecker.contains("overdue")) {
                        flightService.addFlight(flight);
                        if (isArchival) {
                            if (prevFlightTime != null) {
                                try {
                                    if (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes() < 60 && flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes() > 0) {
                                        Glider editedGlider = new Glider(glider.getRegistrationNumber(), new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours() - prevFlightTime.getHours()) + " hours" + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes()) + "minutes"), glider.getFlightCount(), glider.getType(), glider.getNextCheckupHrs(), glider.getNextCheckupFlights(), glider.getNextCheckupDate(), false);
                                        gliderService.editGlider(glider, editedGlider);
                                    } else if (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes() > 60) {
                                        Glider editedGlider = new Glider(glider.getRegistrationNumber(), new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours() - prevFlightTime.getHours() + 1) + " hours" + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes() - 60) + "minutes"), glider.getFlightCount(), glider.getType(), glider.getNextCheckupHrs(), glider.getNextCheckupFlights(), glider.getNextCheckupDate(), false);
                                        gliderService.editGlider(glider, editedGlider);
                                    } else if (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes() < 0) {
                                        Glider editedGlider = new Glider(glider.getRegistrationNumber(), new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours() - prevFlightTime.getHours() + 1) + " hours" + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes() + 60) + "minutes"), glider.getFlightCount(), glider.getType(), glider.getNextCheckupHrs(), glider.getNextCheckupFlights(), glider.getNextCheckupDate(), false);
                                        gliderService.editGlider(glider, editedGlider);
                                    }
                                } catch (SQLException ex) {
                                    Notification notification = Notification
                                            .show("Error: " + ex.getErrorCode());
                                    throw new RuntimeException(ex);
                                }
                            }
                            else{
                                try {
                                    if (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() < 60) {
                                        Glider editGlider = new Glider(glider.getRegistrationNumber(), new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours()) + " hours" + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes()) + "minutes"), glider.getFlightCount(), glider.getType(), glider.getNextCheckupHrs(), glider.getNextCheckupFlights(), glider.getNextCheckupDate(), false);
                                        gliderService.editGlider(glider, editGlider);
                                    } else {
                                        Glider editGlider = new Glider(glider.getRegistrationNumber(), new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours() + 1) + " hours" + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - 60) + "minutes"), glider.getFlightCount(), glider.getType(), glider.getNextCheckupHrs(), glider.getNextCheckupFlights(), glider.getNextCheckupDate(), false);
                                        gliderService.editGlider(glider, editGlider);
                                    }
                                } catch (SQLException ex) {
                                    Notification notification = Notification
                                            .show("Error: " + ex.getErrorCode());
                                    throw new RuntimeException(ex);
                                }
                            }
                        }
                        pilotService.editPilot(flight.getPilot1(), flight.getPilot1().getName(), flight.getPilot1().getLicenseNumber(), false);
                        if (flight.getPilot2() != null) {
                            pilotService.editPilot(flight.getPilot2(), flight.getPilot2().getName(), flight.getPilot2().getLicenseNumber(), false);
                        }
                        if (isActive) {
                            pilotService.editPilot(editedFlight.getPilot1(), editedFlight.getPilot1().getName(), editedFlight.getPilot1().getLicenseNumber(), true);
                            if (editedFlight.getPilot2() != null) {
                                pilotService.editPilot(flight.getPilot2(), flight.getPilot2().getName(), flight.getPilot2().getLicenseNumber(), false);
                            }
                        }
                        Notification notification = Notification.show(checkupChecker);
                        notification.addThemeVariants(NotificationVariant.LUMO_WARNING);

                    }
                    else {
                        Notification notification = Notification.show(checkupChecker);
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);}
                }
            }
            else{
                Notification notification = Notification
                        .show("The submitted record conflicts with at least one preexisting record");
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception e) {
            Notification notification = Notification
                    .show("Error: " + e.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            throw new RuntimeException(e);
        }
    }


    public static void showView() {
        UI.getCurrent().navigate(FlightsView.class);
    }
}