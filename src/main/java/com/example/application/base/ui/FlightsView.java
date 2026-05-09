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
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
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
import java.time.LocalDate;
import java.time.LocalTime;
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
                throw new RuntimeException(ex);
            }
        });

        GridSingleSelectionModel<Flight> selectionModel = (GridSingleSelectionModel<Flight>)grid.getSelectionModel();

        Button deleteButton = new Button("Delete Flight", e -> {
            Long deletingId;
            if (selectionModel.getSelectedItem().isPresent()) {
                deletingId = selectionModel.getSelectedItem().get().getId();
                if (deletingId >= 0) {
                    ConfirmDialog deleteDialog = getDeleteDialog(flightService, selectionModel.getSelectedItem().get().getId());
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

        Tab premade = new Tab("Pre-Made");
        Tab active = new Tab("Active");
        Tab archival = new Tab("Archival");

        Tabs tabs = new Tabs(premade, active, archival);
        tabs.addSelectedChangeListener(e -> {
            if (tabs.getSelectedTab() == premade) {
                //set global filters
                isActiveFilter = false;
                isArchivalFilter = false;
                dataView.refreshAll();
            }
            else if (tabs.getSelectedTab() == active) {
                //set global filters
                isActiveFilter = true;
                isArchivalFilter = false;
                dataView.refreshAll();
            }
            if (tabs.getSelectedTab() == archival) {
                //set global filters
                isActiveFilter = false;
                isArchivalFilter = true;
                dataView.refreshAll();
            }
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
            if (searchTerm.isEmpty()){
                return true;
            }
            boolean matchesID = String.valueOf(item.getId()).equals(searchTerm);
            boolean matchesDate = String.valueOf(item.getDate()).contains(searchTerm);
            boolean matchesGlider = item.getGlider().getRegistrationNumber().contains(searchTerm);
            boolean matchesPilot1 = item.getPilot1().getName().contains(searchTerm);
            boolean matchesPilot2 = false;
            if(item.getPilot2() != null) {
                matchesPilot2 = item.getPilot2().getName().contains(searchTerm);
            }
            boolean matchesPointOfDeparture = item.getPointOfDeparture().contains(searchTerm);
            boolean matchesPointOfArrival = item.getPointOfArrival().contains(searchTerm);
            boolean matchesTimeOfArrival = String.valueOf(item.getTimeOfArrival()).contains(searchTerm);
            boolean matchesTimeOfDeparture = String.valueOf(item.getTimeOfDeparture()).contains(searchTerm);
            boolean matchesFlightDuration = String.valueOf(item.getFlightTime()).contains(searchTerm);
            boolean matchesTask =  item.getTask().contains(searchTerm);
            boolean matchesPreFlightCheckup = item.getPreFlightCheckup().contains(searchTerm);
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
        buttonsLayout.add(searchField, addButton, deleteButton, editButton);
        add(buttonsLayout, tabs, grid);

    }

    private static @NonNull ConfirmDialog getDeleteDialog(FlightService flightService, Long deletingId) {
        ConfirmDialog deleteDialog = new ConfirmDialog();
        deleteDialog.setHeader("Delete Flight?");
        deleteDialog.setText("Are you sure you want to permanently delete this item?");

        deleteDialog.setCancelable(true);
        deleteDialog.addCancelListener(event -> deleteDialog.close());
        deleteDialog.setConfirmText("Delete");
        deleteDialog.setConfirmButtonTheme("error primary");
        deleteDialog.addConfirmListener(event -> {
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
                    throw new RuntimeException(ex);
                }
            }
            Date date = Date.valueOf(dateField.getValue());
            String pointOfArrival = pointOfArrivalField.getValue();
            String pointOfDeparture = pointOfDepartureField.getValue();
            String task = taskField.getValue();
            String preFlightCheckup = preFlightCheckupField.getValue();
            Flight flight = new Flight(glider, pilot1, pilot2, date, pointOfDeparture, pointOfArrival, Time.valueOf(timeOfDeparture), Time.valueOf(timeOfArrival), task, preFlightCheckup);
            flight.isActive = isActive;
            flight.isArchival = isArchival;
            try {
                if(flight.validateAddition(flightService.getArchivalFlightsByGliderAndDate(glider, date), flightService.getArchivalFlightsByPilotAndDate(pilot1, date), flightService.getArchivalFlightsByPilotAndDate(pilot2, date))) {
                    flightService.addFlight(flight);
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            try {
                if(flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() < 60) {
                    Glider editGlider = new Glider(glider.getRegistrationNumber(), new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours()) + " hours" + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes()) + "minutes"), glider.getFlightCount(), glider.getType(), glider.getNextCheckupHrs(), glider.getNextCheckupFlights(), glider.getNextCheckupDate(), false);
                    gliderService.editGlider(glider, editGlider);
                }
                else{
                    Glider editGlider = new Glider(glider.getRegistrationNumber(), new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours() + 1) + " hours" + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - 60) + "minutes"), glider.getFlightCount(), glider.getType(), glider.getNextCheckupHrs(), glider.getNextCheckupFlights(), glider.getNextCheckupDate(), false);
                    gliderService.editGlider(glider, editGlider);
                }
            }
            catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            additionForm.close();
            UI.getCurrent().getPage().reload();
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
                    throw new RuntimeException(ex);
                }
            }
            Date date = Date.valueOf(dateField.getValue());
            String pointOfArrival = pointOfArrivalField.getValue();
            String pointOfDeparture = pointOfDepartureField.getValue();
            String task = taskField.getValue();
            String preFlightCheckup = preFlightCheckupField.getValue();
            PGInterval prevFlightTime = flight.getFlightTime();
            Flight editedFlight = new Flight(glider, pilot1, pilot2, date, pointOfDeparture, pointOfArrival, Time.valueOf(timeOfDeparture), Time.valueOf(timeOfArrival), task, preFlightCheckup);
            try {
                List<Flight> filteredByGlider = flightService.getArchivalFlightsByGliderAndDate(glider, date);
                List<Flight> filteredByPilot1 = flightService.getArchivalFlightsByPilotAndDate(pilot1, date);
                List<Flight> filteredByPilot2 = null;
                if (pilot2 != null){
                    filteredByPilot2 = flightService.getArchivalFlightsByPilotAndDate(pilot2, date);
                }
                if(flight.validateEdit(flight, filteredByGlider, filteredByPilot1, filteredByPilot2)) {
                    flightService.editFlight(flight, editedFlight);
                    pilotService.editPilot(flight.getPilot1(), flight.getPilot1().getName(), flight.getPilot1().getLicenseNumber(), false);
                    if(flight.getPilot2() != null){
                        pilotService.editPilot(flight.getPilot2(), flight.getPilot2().getName(), flight.getPilot2().getLicenseNumber(), false);
                    }
                    if(isActive) {
                        pilotService.editPilot(editedFlight.getPilot1(), editedFlight.getPilot1().getName(), editedFlight.getPilot1().getLicenseNumber(), true);
                        if (editedFlight.getPilot2() != null) {
                            pilotService.editPilot(flight.getPilot2(), flight.getPilot2().getName(), flight.getPilot2().getLicenseNumber(), false);
                        }
                    }
                    if(isArchival) {
                        try {
                            if (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes() < 60 && flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes() > 0) {
                                Glider editedGlider = new Glider(glider.getRegistrationNumber(), new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours() - prevFlightTime.getHours()) + " hours" + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes()) + "minutes"), glider.getFlightCount(), glider.getType(), glider.getNextCheckupHrs(), glider.getNextCheckupFlights(), glider.getNextCheckupDate(), false);
                                gliderService.editGlider(glider, editedGlider);
                            } else if (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes() > 60) {
                                Glider editedGlider = new Glider(glider.getRegistrationNumber(), new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours() - prevFlightTime.getHours() + 1) + " hours" + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes() - 60) + "minutes"), glider.getFlightCount(), glider.getType(), glider.getNextCheckupHrs(), glider.getNextCheckupFlights(), glider.getNextCheckupDate(), false);
                                gliderService.editGlider(glider, editedGlider);
                            }
                            else if (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes() < 0) {
                                Glider editedGlider = new Glider(glider.getRegistrationNumber(), new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours() - prevFlightTime.getHours() + 1) + " hours" + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes() + 60) + "minutes"), glider.getFlightCount(), glider.getType(), glider.getNextCheckupHrs(), glider.getNextCheckupFlights(), glider.getNextCheckupDate(), false);
                                gliderService.editGlider(glider, editedGlider);
                            }
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    }

                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            editForm.close();
            UI.getCurrent().getPage().reload();
        });
        Button cancelButton = new Button("Cancel", e -> editForm.close());
        editForm.getFooter().add(cancelButton, editButton);
        editForm.add(formLayout);
        editForm.open();
    }

    public static void showView() {
        UI.getCurrent().navigate(FlightsView.class);
    }
    //TODO develop this to display the flights by filter passed to this function
    public static void showViewFilter(Glider glider) {
        UI.getCurrent().navigate(FlightsView.class);
    }
}