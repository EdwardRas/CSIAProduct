package com.example.application.base.ui;
import com.example.application.flights.Flight;
import com.example.application.flights.FlightService;
import com.example.application.gliders.Glider;
import com.example.application.gliders.GliderService;
import com.example.application.pilots.Pilot;
import com.example.application.pilots.PilotService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
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
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import org.postgresql.util.PGInterval;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static com.vaadin.flow.component.notification.Notification.show;

@Route(value = "flights", layout =  MainLayout.class)
@Menu(order = 0, icon = "vaadin:flight-takeoff", title = "Flights")
public class FlightsView extends VerticalLayout {
    //DB service objects
    private final FlightService flightService;
    private final PilotService pilotService;
    private final GliderService gliderService;

    //global filter variables
    private boolean isActiveFilter = false;
    private boolean isArchivalFilter = false;
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
    //gliderFilter is protected static so that it can be easily modified from GlidersView
    protected static Glider gliderFilter;
    Grid<Flight> grid;
    TextField searchField;

    @Autowired //automatically handles input variables
    public FlightsView(FlightService flightService, GliderService gliderService, PilotService pilotService)  {

        this.flightService = flightService;
        this.pilotService = pilotService;
        this.gliderService = gliderService;

        //create list of all flights in DB
        List<Flight> records = this.flightService.getAllFlights();
        //Create gird display element
        grid = new Grid<Flight>();
        //Hides sorting buttons while not hovering over them
        com.vaadin.flow.dom.Element styleElement = new com.vaadin.flow.dom.Element("style");
        styleElement.setText("vaadin-grid-sorter:not([direction]):not(:hover)::part(indicators) {\n" +
                "    color: rgb(255 255 255 / 52%) !important;\n" +
                "}\n" +
                "vaadin-grid-cell-content:hover vaadin-grid-sorter:not([direction])::part(indicators) {\n" +
                "    color: #cccccc !important;\n" +
                "    opacity: 1 !important;\n" +
                "}");
        UI.getCurrent().getElement().appendChild(styleElement);
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
        //set the rows of the grid to all the flights in DB
        GridListDataView<Flight> dataView = grid.setItems(records);
        
        //create button to add flight
        Button addButton = new Button("Add Flight", e -> {
            try {
                //displays addition form to the user
                showAdditionForm(gliderService, pilotService);
            } catch (SQLException ex) {
                Notification notification = show("Error: " + ex.getErrorCode());
                throw new RuntimeException(ex);
            }
        });
        addButton.setPrefixComponent(new Icon(VaadinIcon.FILE_ADD));
        //gets selection model for flight selection to get selected item in grid
        GridSingleSelectionModel<Flight> selectionModel = (GridSingleSelectionModel<Flight>)grid.getSelectionModel();

        //create button to delete record
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

        deleteButton.setPrefixComponent(new Icon(VaadinIcon.TRASH));

        //create button to edit flights
        Button editButton = new Button("Edit Flight", e -> {
            if (selectionModel.getSelectedItem().isPresent()) {
                try {
                    showEditForm(gliderService, pilotService, selectionModel.getSelectedItem().get());
                } catch (SQLException ex) {
                    Notification notification = show("Error: " + ex.getErrorCode()); notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    throw new RuntimeException(ex);
                }
            }
        });

        editButton.setPrefixComponent(new Icon(VaadinIcon.EDIT));

        //create button to launch premade flights
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
                refreshRecords();
            }
        });

        launchButton.setPrefixComponent(new Icon(VaadinIcon.FLIGHT_TAKEOFF));

        //create button to land active flights
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
                refreshRecords();
            }
        });

        landButton.setPrefixComponent(new Icon(VaadinIcon.FLIGHT_LANDING));

        Button filterButton = new Button("Filter");
        filterButton.addClickListener(e -> showFilterForm(dataView));
        filterButton.setPrefixComponent(new Icon(VaadinIcon.FILTER));


        //add listener entity to check when the selection on the grid changes
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

        //set the buttons which aren't always enabled to be disabled by default
        deleteButton.setEnabled(false);
        editButton.setEnabled(false);
        launchButton.setEnabled(false);
        landButton.setEnabled(false);

        //create tabs to filter by status and object which holds them all
        Tab premade = new Tab("Pre-Made");
        Tab active = new Tab("Active");
        Tab archival = new Tab("Archival");

        Tabs tabs = new Tabs(premade, active, archival);

        //add listener object for when tabs are switched
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

        //create search text field
        searchField = new TextField();
        searchField.setWidth("250px");
        searchField.setPlaceholder("Search");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> dataView.refreshAll());

        //Data filtering
        setFilter(dataView);


        //create button to export records
        Button exportButton = new Button("Export", e -> {showExportDialog(gliderService, flightService);});
        exportButton.setPrefixComponent(new Icon(VaadinIcon.DOWNLOAD_ALT));
        //add all UI elements
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSizeFull();
        buttonsLayout.add(addButton, deleteButton, editButton, searchField, filterButton, launchButton, landButton, exportButton);
        add(buttonsLayout, tabs, grid);
    }

    private void setFilter(GridListDataView<Flight> dataView) {
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
            if (pointOfDepartureFilter != null && !pointOfDepartureFilter.isEmpty()) {
                matchesFilter = matchesFilter && pointOfDepartureFilter.equals(item.getPointOfDeparture());
            }
            //pointOfArrivalFilter
            if (pointOfArrivalFilter != null  && !pointOfArrivalFilter.isEmpty()) {
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
            if (taskFilter != null && !taskFilter.isEmpty()) {
                matchesFilter = matchesFilter && taskFilter.equals(item.getTask());
            }
            //preFlightCheckupFilter
            if (preFlightCheckupFilter != null && !preFlightCheckupFilter.isEmpty()) {
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
    }

    //shows user form to export records
    private void showExportDialog(GliderService gliderService, FlightService flightService) {
        //creates form and all its data entry fields
        Dialog exportForm = new Dialog();
        FormLayout formLayout = new FormLayout();
        formLayout.setSizeFull();
        formLayout.setMaxWidth("400px");
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px",2 )
        );
        ComboBox<Glider> gliderField = new ComboBox<>();
        gliderField.setLabel("Glider");
        gliderField.setRequired(true);
        gliderField.setItems(gliderService.getAllGliders());
        gliderField.setItemLabelGenerator(Glider::getRegistrationNumber);
        if(gliderFilter != null){
            gliderField.setValue(gliderFilter);
        }
        gliderField.setSizeFull();
        DatePicker datePicker = new DatePicker();
        datePicker.setLabel("Date");
        datePicker.setRequired(true);
        if(dateFilter != null){
            datePicker.setValue(dateFilter.toLocalDate());
        }
        datePicker.setSizeFull();
        Button downloadButton = new Button("Confirm", e -> exportForm.close());
        Button cancelButton = new Button("Cancel", e -> exportForm.close());

        final List<Flight> flights = new ArrayList<>();
        
        downloadButton.setEnabled(gliderField.getValue() != null && datePicker.getValue() != null);
        gliderField.addValueChangeListener(e -> {
            downloadButton.setEnabled(gliderField.getValue() != null && datePicker.getValue() != null);
            flights.clear();
            if(downloadButton.isEnabled()) {
                Glider glider = gliderField.getValue();
                Date date = Date.valueOf(datePicker.getValue());
                try {
                    flights.addAll(flightService.getArchivalFlightsByGliderAndDate(glider, date));
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                if(flights.isEmpty()) {
                    Notification.show("No flights found");
                    downloadButton.setEnabled(false);
                }
            }
        });
        datePicker.addValueChangeListener(e -> {
            downloadButton.setEnabled(gliderField.getValue() != null && datePicker.getValue() != null);
            flights.clear();
            if(downloadButton.isEnabled()) {
                Glider glider = gliderField.getValue();
                Date date = Date.valueOf(datePicker.getValue());
                try {
                    flights.addAll(flightService.getArchivalFlightsByGliderAndDate(glider, date));
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                if(flights.isEmpty()) {
                    Notification.show("No flights found");
                    downloadButton.setEnabled(false);
                }
            }
        });


        //download logic
        DownloadHandler csvResource = DownloadHandler.fromInputStream(d -> exportRecords(flights));
        Anchor downloadAnchor = new Anchor(csvResource, "");
        downloadAnchor.getElement().setAttribute("download", true);
        downloadAnchor.add(downloadButton);

        //adds all UI elements and opens form
        formLayout.addFormRow(gliderField);
        formLayout.addFormRow(datePicker);
        exportForm.getFooter().add(cancelButton, downloadAnchor);
        exportForm.add(formLayout);
        Scroller scroller = new Scroller(formLayout);
        exportForm.add(scroller);
        exportForm.open();

    }
    //shows user dialog to confirm deletion
    private ConfirmDialog getDeleteDialog(FlightService flightService, Long deletingId, GliderService gliderService) {
        //create dialog
        ConfirmDialog deleteDialog = new ConfirmDialog();
        deleteDialog.setHeader("Delete Flight?");
        deleteDialog.setText("Are you sure you want to permanently delete this item?");
        deleteDialog.setCancelable(true);
        deleteDialog.addCancelListener(event -> deleteDialog.close());
        deleteDialog.setConfirmText("Delete");
        deleteDialog.setConfirmButtonTheme("error primary");
        deleteDialog.addConfirmListener(event -> {
            //deletion logic
            try {
                if(flightService.getFlightById(deletingId).isArchival){
                    Flight flight = flightService.getFlightById(deletingId);
                    Glider glider = flight.getGlider();
                    if(glider.getTotalFlightTime().getMinutes() - flight.getFlightTime().getMinutes() < 0) {
                        glider.setTotalFlightTime(new PGInterval((glider.getTotalFlightTime().getHours() - flight.getFlightTime().getHours() - 1) + " hours " + (glider.getTotalFlightTime().getMinutes() - flight.getFlightTime().getMinutes() + 60) + " minutes"));
                    }
                    else{
                        glider.setTotalFlightTime(new PGInterval((glider.getTotalFlightTime().getHours() - flight.getFlightTime().getHours()) + " hours " + (glider.getTotalFlightTime().getMinutes() - flight.getFlightTime().getMinutes()) + " minutes"));
                    }
                    glider.setFlightCount(glider.getFlightCount() - 1);
                    gliderService.editGlider(glider);
                }
                //TODO for some reason not working
                else if(flightService.getFlightById(deletingId).isActive){
                    flightService.getFlightById(deletingId).getGlider().isFlying = false;
                    gliderService.editGlider(flightService.getFlightById(deletingId).getGlider());
                    flightService.getFlightById(deletingId).getPilot1().isFlying = false;
                    pilotService.editPilot(flightService.getFlightById(deletingId).getPilot1());
                    if(flightService.getFlightById(deletingId).getPilot2() != null){
                        flightService.getFlightById(deletingId).getPilot1().isFlying = false;
                        pilotService.editPilot(flightService.getFlightById(deletingId).getPilot2());
                    }
                }
            } catch (SQLException e) {
                Notification notification = show("Error: " + e.getErrorCode());
                throw new RuntimeException(e);
            }
            flightService.deleteFlight(deletingId);
            refreshRecords();
        });
        return deleteDialog;
    }

    //show form to input addition data
    private void showAdditionForm(GliderService gliderService, PilotService pilotService) throws SQLException {
        //create input fields
        Dialog additionForm = new Dialog();
        FormLayout formLayout = new FormLayout();
        formLayout.setSizeFull();
        formLayout.setMaxWidth("400px");
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        ComboBox<Glider> gliderField = new ComboBox<>();
        gliderField.setLabel("Glider");
        gliderField.setItems(gliderService.getAllGliders());
        gliderField.setRequired(true);
        gliderField.setItemLabelGenerator(Glider::getRegistrationNumber);
        gliderField.setSizeFull();
        ComboBox<Pilot> pilot1Field = new ComboBox<>();
        pilot1Field.setLabel("Pilot 1");
        pilot1Field.setItems(pilotService.getAllPilots());
        pilot1Field.setRequired(true);
        pilot1Field.setItemLabelGenerator(Pilot::getName);
        pilot1Field.setSizeFull();
        ComboBox<Pilot> pilot2Field = new ComboBox<>();
        pilot2Field.setLabel("Pilot 2");
        pilot2Field.setItems(pilotService.getAllPilots());
        pilot2Field.setItemLabelGenerator(Pilot::getName);
        pilot2Field.setSizeFull();
        TimePicker timeOfDeparturePicker = new TimePicker();
        timeOfDeparturePicker.setLabel("Time of departure (UTC)");
        TimePicker timeOfArrivalPicker = new TimePicker();
        timeOfDeparturePicker.addValueChangeListener(e -> timeOfArrivalPicker.setMin(e.getValue()));
        timeOfArrivalPicker.addValueChangeListener(e -> timeOfDeparturePicker.setMax(e.getValue()));
        timeOfArrivalPicker.setLabel("Time of arrival (UTC)");
        timeOfDeparturePicker.setSizeFull();
        timeOfArrivalPicker.setSizeFull();
        timeOfArrivalPicker.setEnabled(false);
        TextField pointOfDepartureField = new TextField();
        pointOfDepartureField.setLabel("Point of departure");
        pointOfDepartureField.setSizeFull();
        pointOfDepartureField.setRequired(true);
        TextField pointOfArrivalField = new TextField();
        pointOfArrivalField.setLabel("Point of arrival");
        pointOfArrivalField.setSizeFull();
        pointOfArrivalField.setRequired(true);
        DatePicker dateField = new DatePicker();
        dateField.setLabel("Date");
        dateField.setSizeFull();
        dateField.setRequired(true);
        TextField taskField = new TextField();
        taskField.setLabel("Task");
        taskField.setSizeFull();
        taskField.setRequired(true);
        TextField preFlightCheckupField = new TextField();
        preFlightCheckupField.setLabel("Pre Flight Checkup");
        preFlightCheckupField.setSizeFull();
        preFlightCheckupField.setRequired(true);
        timeOfDeparturePicker.addValueChangeListener(e -> timeOfArrivalPicker.setEnabled(timeOfDeparturePicker.getValue() != null));

        //add all fields to form layout
        formLayout.addFormRow(gliderField);
        formLayout.addFormRow(pilot1Field);
        formLayout.addFormRow(pilot2Field);
        formLayout.addFormRow(dateField);
        formLayout.addFormRow(taskField);
        formLayout.addFormRow(pointOfDepartureField);
        formLayout.addFormRow(pointOfArrivalField);
        formLayout.addFormRow(timeOfDeparturePicker);
        formLayout.addFormRow(timeOfArrivalPicker);
        formLayout.addFormRow(preFlightCheckupField);

        //create button to execute addition
        Button addButton = new Button("Add", e -> {
            //if any field has an invalid value, end lambda function early
            if(gliderField.isEmpty() || pilot1Field.isEmpty() || (!pilot2Field.isEmpty() && pilot2Field.getValue().equals(pilot1Field.getValue())) || timeOfArrivalPicker.isInvalid() || timeOfDeparturePicker.isInvalid() || pointOfDepartureField.isEmpty() || pointOfArrivalField.isEmpty() || dateField.isEmpty() || dateField.isInvalid() || taskField.isEmpty() || preFlightCheckupField.isEmpty()) {
                Notification.show("At least one field value is invalid");
                return;
            }
            Glider glider = gliderField.getValue();
            Pilot pilot1 = pilot1Field.getValue();
            Pilot pilot2 = pilot2Field.getValue();
            boolean isActive;
            boolean isArchival;
            //determines status
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
                //calculates flight duration
                if (timeOfArrival.getHour() - timeOfDeparture.getHour() > 0 && timeOfArrival.getMinute() < timeOfDeparture.getMinute()) {
                    hours = String.valueOf(timeOfArrival.getHour() - timeOfDeparture.getHour() - 1);
                    minutes = String.valueOf(60 + timeOfArrival.getMinute() - timeOfDeparture.getMinute());
                } else {
                    hours = String.valueOf(timeOfArrival.getHour() - timeOfDeparture.getHour());
                    minutes = String.valueOf(timeOfArrival.getMinute() - timeOfDeparture.getMinute());
                }
                try {
                    flightDuration = new PGInterval(hours + " hours " + minutes + " minutes");
                } catch (SQLException ex) {
                    Notification notification = show("Error: " + ex.getErrorCode()); notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
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
                //gets filtered lists of archival flights which could conflict with this flight
                List<Flight> filteredByGlider = flightService.getArchivalFlightsByGliderAndDate(glider, date);
                List<Flight> filteredByPilot1 = flightService.getArchivalFlightsByPilotAndDate(pilot1, date);
                List<Flight> filteredByPilot2 = null;
                if (pilot2 != null){
                    filteredByPilot2 = flightService.getArchivalFlightsByPilotAndDate(pilot2, date);
                }

                if(flight.validateAddition(filteredByGlider, filteredByPilot1, filteredByPilot2)) {
                    //checks if flight would violate checkup deadlines
                    String checkupChecker = flight.checkNextCheckup();
                    if (checkupChecker == null) {
                        flightService.addFlight(flight);
                        if(isArchival) {
                            try {
                                flightAdditionGliderHandling(gliderService, flightDuration, glider);
                            } catch (SQLException ex) {
                                Notification notification = show("Error: " + ex.getErrorCode()); notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                                throw new RuntimeException(ex);
                            }
                        }
                        else if(isActive){
                            pilot1.isFlying = true;
                            pilotService.editPilot(pilot1);
                            if(pilot2 != null){
                                pilot2.isFlying = true;
                                pilotService.editPilot(pilot2);
                            }
                            glider.setFlightCount(glider.getFlightCount() + 1);
                            glider.isFlying = false;
                            gliderService.editGlider(glider);
                        }
                        additionForm.close();
                        refreshRecords();
                    }
                    else {
                        if (!checkupChecker.contains("overdue")) {
                            flightService.addFlight(flight);
                            if(isArchival) {
                                try {
                                    flightAdditionGliderHandling(gliderService, flightDuration, glider);
                                } catch (SQLException ex) {
                                    Notification notification = show("Error: " + ex.getErrorCode());
                                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                                    throw new RuntimeException(ex);
                                }
                            }
                            if(isActive){
                                pilot1.isFlying = true;
                                pilotService.editPilot(pilot1);
                                if(pilot2 != null){
                                    pilot2.isFlying = true;
                                    pilotService.editPilot(pilot2);
                                }
                                glider.setFlightCount(glider.getFlightCount() + 1);
                                glider.isFlying = false;
                                gliderService.editGlider(glider);
                            }
                            additionForm.close();
                            refreshRecords();
                            Notification notification = show(checkupChecker);
                            notification.addThemeVariants(NotificationVariant.LUMO_WARNING);

                        }
                        else {
//                            additionForm.close();
//                            refreshRecords();
                            Notification notification = show(checkupChecker);
                            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                        }
                    }
                }
                else{
                    Notification notification = show("The submitted record conflicts with at least one preexisting record");
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            }
            catch (SQLException ex) {
                Notification notification = show("Error: " + ex.getErrorCode());
                throw new RuntimeException(ex);
            }

        });
        //create button to cancel action
        Button cancelButton = new Button("Cancel", e -> additionForm.close());

        //add buttons and open form
        additionForm.getFooter().add(cancelButton, addButton);
        additionForm.add(formLayout);
        Scroller scroller = new Scroller(formLayout);
        additionForm.add(scroller);
        additionForm.open();

    }

    //logic to handle changes to glider as a result of flight addition
    private static void flightAdditionGliderHandling(GliderService gliderService, PGInterval flightDuration, Glider glider) throws SQLException {
        if (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() < 60) {
            glider.setTotalFlightTime(new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours()) + " hours " + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes()) + " minutes"));
            glider.isFlying = false;
            glider.setFlightCount(glider.getFlightCount() + 1);
            gliderService.editGlider(glider);
        } else {
            glider.setTotalFlightTime(new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours() + 1) + " hours " + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - 60) + " minutes"));
            glider.setFlightCount(glider.getFlightCount() + 1);
            glider.isFlying = false;
            gliderService.editGlider(glider);
        }
    }

    //shows form to input edit data
    private void showEditForm(GliderService gliderService, PilotService pilotService, Flight flight) throws SQLException {

        //creates input fields and sets their default values to be those of the flight being edited
        Dialog editForm = new Dialog();
        FormLayout formLayout = new FormLayout();
        formLayout.setSizeFull();
        formLayout.setMaxWidth("400px");
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        ComboBox<Glider> gliderField = new ComboBox<>();
        gliderField.setLabel("Glider");
        gliderField.setRequired(true);
        gliderField.setItems(gliderService.getAllGliders());
        gliderField.setItemLabelGenerator(Glider::getRegistrationNumber);
        gliderField.setValue(flight.getGlider());
        gliderField.setSizeFull();
        ComboBox<Pilot> pilot1Field = new ComboBox<>();
        pilot1Field.setLabel("Pilot 1");
        pilot1Field.setItems(pilotService.getAllPilots());
        pilot1Field.setRequired(true);
        pilot1Field.setValue(flight.getPilot1());
        pilot1Field.setItemLabelGenerator(Pilot::getName);
        pilot1Field.setSizeFull();
        ComboBox<Pilot> pilot2Field = new ComboBox<>();
        pilot2Field.setLabel("Pilot 2");
        pilot2Field.setItems(pilotService.getAllPilots());
        pilot2Field.setValue(flight.getPilot2());
        pilot2Field.setItemLabelGenerator(Pilot::getName);
        pilot2Field.setSizeFull();
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
        timeOfDeparturePicker.setSizeFull();
        timeOfArrivalPicker.setSizeFull();
        timeOfArrivalPicker.setEnabled(timeOfDeparturePicker.getValue() != null);
        TextField pointOfDepartureField = new TextField();
        pointOfDepartureField.setLabel("Point of departure");
        pointOfDepartureField.setValue(flight.getPointOfDeparture());
        pointOfDepartureField.setSizeFull();
        pointOfDepartureField.setRequired(true);
        TextField pointOfArrivalField = new TextField();
        pointOfArrivalField.setLabel("Point of arrival");
        pointOfArrivalField.setSizeFull();
        pointOfArrivalField.setRequired(true);
        pointOfArrivalField.setValue(flight.getPointOfArrival());
        timeOfDeparturePicker.addValueChangeListener(e -> timeOfArrivalPicker.setMin(e.getValue()));
        timeOfArrivalPicker.addValueChangeListener(e -> timeOfDeparturePicker.setMax(e.getValue()));
        DatePicker dateField = new DatePicker();
        dateField.setLabel("Date");
        dateField.setSizeFull();
        dateField.setRequired(true);
        dateField.setValue(flight.getDate().toLocalDate());
        TextField taskField = new TextField();
        taskField.setLabel("Task");
        taskField.setValue(flight.getTask());
        taskField.setSizeFull();
        taskField.setRequired(true);
        TextField preFlightCheckupField = new TextField();
        preFlightCheckupField.setLabel("Pre Flight Checkup");
        preFlightCheckupField.setValue(flight.getPreFlightCheckup());
        preFlightCheckupField.setSizeFull();
        preFlightCheckupField.setRequired(true);
        timeOfDeparturePicker.addValueChangeListener(e -> {
            timeOfArrivalPicker.setEnabled(timeOfDeparturePicker.getValue() != null);
        });

        //add fields to form
        formLayout.addFormRow(gliderField);
        gliderField.setSizeFull();
        formLayout.addFormRow(pilot1Field);
        formLayout.addFormRow(pilot2Field);
        formLayout.addFormRow(dateField);
        formLayout.addFormRow(taskField);
        formLayout.addFormRow(pointOfDepartureField);
        formLayout.addFormRow(pointOfArrivalField);
        formLayout.addFormRow(timeOfDeparturePicker);
        formLayout.addFormRow(timeOfArrivalPicker);
        formLayout.addFormRow(preFlightCheckupField);

        //creates button to confirm edit execution
        Button editButton = new Button("Confirm", e -> {
            //if any field has an invalid value, end lambda function early
            if(gliderField.isEmpty() || pilot1Field.isEmpty() || (!pilot2Field.isEmpty() && pilot2Field.getValue().equals(pilot1Field.getValue()))
                    || timeOfArrivalPicker.isInvalid() || timeOfDeparturePicker.isInvalid() || pointOfDepartureField.isEmpty() || pointOfArrivalField.isEmpty()
                    || dateField.isEmpty() || dateField.isInvalid() || taskField.isEmpty() || preFlightCheckupField.isEmpty()) {
                Notification.show("At least one field value is invalid");
                return;
            }
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
            refreshRecords();
        });
        //creates button to cancel edit execution
        Button cancelButton = new Button("Cancel", e -> editForm.close());

        //adds buttons, shows form
        editForm.getFooter().add(cancelButton, editButton);
        editForm.add(formLayout);
        Scroller scroller = new Scroller(formLayout);
        editForm.add(scroller);
        editForm.open();
    }

    private void showFilterForm(GridListDataView<Flight> dataView) {
        //creates UI elements to input filter requirements
        Dialog filterForm = new Dialog();
        FormLayout formLayout = new FormLayout();
        formLayout.setSizeFull();
        formLayout.setMaxWidth("400px");
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        ComboBox<Glider> gliderField = new ComboBox<>();
        gliderField.setLabel("Glider");
        gliderField.setItems(gliderService.getAllGliders());
        gliderField.setItemLabelGenerator(Glider::getRegistrationNumber);
        if(gliderFilter != null) {
            gliderField.setValue(gliderFilter);
        }
        gliderField.setSizeFull();
        ComboBox<Pilot> pilot1Field = new ComboBox<>();
        pilot1Field.setLabel("Pilot 1");
        pilot1Field.setItems(pilotService.getAllPilots());
        if(pilot1Filter != null) {
            pilot1Field.setValue(pilot1Filter);
        }
        pilot1Field.setItemLabelGenerator(Pilot::getName);
        pilot1Field.setSizeFull();
        ComboBox<Pilot> pilot2Field = new ComboBox<>();
        pilot2Field.setLabel("Pilot 2");
        pilot2Field.setItems(pilotService.getAllPilots());
        pilot2Field.setSizeFull();
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
        eitherPilotField.setSizeFull();
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
        timeOfDeparturePicker.setSizeFull();
        timeOfArrivalPicker.setSizeFull();
        TextField pointOfDepartureField = new TextField();
        pointOfDepartureField.setLabel("Point of departure");
        if(pointOfDepartureFilter != null) {
            pointOfDepartureField.setValue(pointOfDepartureFilter);
        }
        pointOfDepartureField.setSizeFull();
        TextField pointOfArrivalField = new TextField();
        pointOfArrivalField.setLabel("Point of arrival");
        pointOfArrivalField.setSizeFull();
        if (pointOfArrivalFilter != null) {
            pointOfArrivalField.setValue(pointOfArrivalFilter);
        }
        timeOfDeparturePicker.addValueChangeListener(e -> timeOfArrivalPicker.setMin(e.getValue()));
        timeOfArrivalPicker.addValueChangeListener(e -> timeOfDeparturePicker.setMax(e.getValue()));
        DatePicker dateField = new DatePicker();
        dateField.setLabel("Date");
        dateField.setSizeFull();
        if (dateFilter != null) {
            dateField.setValue(dateFilter.toLocalDate());
        }
        TextField taskField = new TextField();
        taskField.setLabel("Task");
        if(taskFilter != null) {
            taskField.setValue(taskFilter);
        }
        taskField.setSizeFull();
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
        formLayout.setColspan(flightDurationHrsField, 1);
        formLayout.setColspan(flightDurationMinsField, 1);
        preFlightCheckupField.setSizeFull();
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
        Span flightDurationTitle = new Span("Flight Duration");
        formLayout.addFormRow(flightDurationTitle);
        HorizontalLayout flightDurationLayout = new HorizontalLayout();
        flightDurationLayout.setSpacing(true);
        flightDurationLayout.setSizeFull();
        flightDurationLayout.add(flightDurationHrsField, flightDurationMinsField);
        formLayout.addFormRow(flightDurationLayout);
        formLayout.addFormRow(preFlightCheckupField);

        //creates buttons to confirm and cancel filtering
        Button confirmButton = new Button("Confirm", e -> {
            //sets global filter variables
            gliderFilter = gliderField.getValue();
            pilot1Filter = pilot1Field.getValue();
            pilot2Filter = pilot2Field.getValue();
            anyPilotFilter = eitherPilotField.getValue();
            if (dateField.getValue() != null) {
                dateFilter = Date.valueOf(dateField.getValue());
            }
            else{
                dateFilter = null;
            }
            if (timeOfDeparturePicker.getValue() != null) {
                timeOfDepartureFilter = Time.valueOf(timeOfDeparturePicker.getValue());
            }
            else{
                timeOfDepartureFilter = null;
            }
            if (timeOfArrivalPicker.getValue() != null ) {
                timeOfArrivalFilter = Time.valueOf(timeOfArrivalPicker.getValue());
            }
            PGInterval flightDuration;
            if(flightDurationMinsField.getValue() != null && flightDurationHrsField.getValue()!=null) {
                try {
                    flightDuration = new PGInterval(flightDurationHrsField.getValue() + " hours " + flightDurationMinsField.getValue() + " minutes");
                } catch (SQLException ex) {
                    Notification notification = show("Error: " + ex.getErrorCode()); notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
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

        //adds all UI elements, shows form
        filterForm.add(formLayout);
        Scroller scroller = new Scroller(formLayout);
        filterForm.add(scroller);
        filterForm.getFooter().add(cancelButton, confirmButton);
        filterForm.open();
    }

    //logic of flight editing, separate function to declutter code
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
        editedFlight.setId(flight.getId());
        boolean isActive;
        boolean isArchival;

        //determine status
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
            editedFlight.isActive = isActive;
            editedFlight.isArchival = isArchival;

            //calculate flight duration
            String hours;
            String minutes;
            PGInterval flightDuration = null;
            if (isArchival) {
                if (timeOfArrival.getHour() - timeOfDeparture.getHour() > 0 && timeOfArrival.getMinute() < timeOfDeparture.getMinute()) {
                    hours = String.valueOf(timeOfArrival.getHour() - timeOfDeparture.getHour() - 1);
                    minutes = String.valueOf(60 + timeOfArrival.getMinute() - timeOfDeparture.getMinute());
                } else {
                    hours = String.valueOf(timeOfArrival.getHour() - timeOfDeparture.getHour());
                    minutes = String.valueOf(timeOfArrival.getMinute() - timeOfDeparture.getMinute());
                }
                try {
                    flightDuration = new PGInterval(hours + " hours " + minutes + " minutes");
                } catch (SQLException ex) {
                    Notification notification = show("Error: " + ex.getErrorCode());
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    throw new RuntimeException(ex);
                }
            }
            PGInterval prevFlightTime = flight.getFlightTime();
            //get all flights which might conflict with this one
            List<Flight> filteredByGlider = flightService.getArchivalFlightsByGliderAndDate(glider, date);
            List<Flight> filteredByPilot1 = flightService.getArchivalFlightsByPilotAndDate(pilot1, date);
            List<Flight> filteredByPilot2 = null;
            if (pilot2 != null) {
                filteredByPilot2 = flightService.getArchivalFlightsByPilotAndDate(pilot2, date);
            }
            if (editedFlight.validateEdit(flight, filteredByGlider, filteredByPilot1, filteredByPilot2)) {
                //checks if this flight doesn't violate any deadlines
                String checkupChecker = flight.checkNextCheckup();
                if (checkupChecker == null) {
                    flightService.editFlight(editedFlight);
                    flightEditingSynchronisation(flight, glider, isArchival, prevFlightTime, flightDuration, isActive, editedFlight);
                }
                else {
                    if (!checkupChecker.contains("overdue")) {
                        flightService.editFlight(editedFlight);
                        flightEditingSynchronisation(flight, glider, isArchival, prevFlightTime, flightDuration, isActive, editedFlight);
                        Notification notification = show(checkupChecker);
                        notification.addThemeVariants(NotificationVariant.LUMO_WARNING);

                    }
                    else {
                        Notification notification = show(checkupChecker);
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);}
                }
            }
            else{
                Notification notification = show("The submitted record conflicts with at least one preexisting record");
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception e) {
            Notification notification = show("Error: " + e.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            throw new RuntimeException(e);
        }
    }

    //updates pilots and glider to reflect the changes due to the flight changing
    private void flightEditingSynchronisation(Flight flight, Glider glider, boolean isArchival, PGInterval prevFlightTime, PGInterval flightDuration, boolean isActive, Flight editedFlight) {
        if (isArchival) {
            if (prevFlightTime != null) {
                try {
                    if (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes() < 60 && flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes() > 0) {
                        glider.setTotalFlightTime( new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours() - prevFlightTime.getHours()) + " hours " + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes()) + " minutes"));
                        glider.isFlying = false;
                        gliderService.editGlider(glider);
                    } else if (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes() > 60) {
                        glider.setTotalFlightTime(new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours() - prevFlightTime.getHours() + 1) + " hours " + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes() - 60) + " minutes"));
                        glider.isFlying = false;
                        gliderService.editGlider(glider);
                    } else if (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes() < 0) {
                        glider.setTotalFlightTime(new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours() - prevFlightTime.getHours() + 1) + " hours " + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - prevFlightTime.getMinutes() + 60) + " minutes"));
                        glider.isFlying = false;
                        gliderService.editGlider(glider);
                    }
                } catch (SQLException ex) {
                    Notification notification = show("Error: " + ex.getErrorCode());
                    throw new RuntimeException(ex);
                }
            }
            else{
                try {
                    if (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() < 60) {
                        glider.setTotalFlightTime( new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours()) + " hours " + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes()) + " minutes"));
                        glider.isFlying = false;
                        gliderService.editGlider(glider);
                    } else {
                        glider.setTotalFlightTime(new PGInterval((flightDuration.getHours() + glider.getTotalFlightTime().getHours() + 1) + " hours " + (flightDuration.getMinutes() + glider.getTotalFlightTime().getMinutes() - 60) + " minutes"));
                        glider.isFlying = false;
                        gliderService.editGlider(glider);
                    }
                } catch (SQLException ex) {
                    Notification notification = show("Error: " + ex.getErrorCode());
                    throw new RuntimeException(ex);
                }
            }
        }
        if(!isActive) {
            editedFlight.getGlider().isFlying = false;
            editedFlight.getPilot1().isFlying = false;
            pilotService.editPilot(flight.getPilot1());
            gliderService.editGlider(flight.getGlider());
            if (flight.getPilot2() != null) {
                flight.getPilot2().isFlying = false;
                pilotService.editPilot(flight.getPilot2());
            }
        }
        if (isActive) {
            editedFlight.getPilot1().isFlying = true;
            editedFlight.getGlider().isFlying = true;
            pilotService.editPilot(editedFlight.getPilot1());
            gliderService.editGlider(flight.getGlider());
            if (editedFlight.getPilot2() != null) {
                editedFlight.getPilot2().isFlying = true;
                pilotService.editPilot(flight.getPilot2());
            }
        }
    }
    
    private void refreshRecords(){
        List<Flight> records = this.flightService.getAllFlights();
        GridListDataView dataView = grid.setItems(records);
        setFilter(dataView);
    }

    //creates pdf document containing selected records
    private DownloadResponse exportRecords(List<Flight> flights) {
        try {
            //HTML template for how the document looks
            String templateBegin = """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8"></meta>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0"></meta>
                        <title>Header and Details Tables</title>
                        <style>
                            body {
                                font-family: Arial, sans-serif;
                                margin: 20px;
                                background-color: #f9f9f9;
                                color: #333;
                            }
                            .container {
                                background-color: white;
                                padding: 20px;
                                border-radius: 8px;
                                box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                            }
                            table {
                                border-collapse: collapse;
                                width: 100%;
                                margin-bottom: 30px;
                            }
                            th {
                                        border: 1px solid #ddd;
                                        padding: 5px;
                                        font-size: 14px;
                                        text-align: center;
                                    }
                                    td {
                                        border: 1px solid #ddd;
                                        padding: 5px;
                                        font-size: 11px;
                                        text-align: left;
                                    }
                            th {
                                background-color: #f2f2f2;
                                font-weight: bold;
                            }
                            h2 {
                                color: #444;
                                border-bottom: 2px solid #eee;
                                padding-bottom: 5px;
                                font-size: 15px;
                            }
                            .table-responsive {
                                overflow-x: auto;
                            }
                            
                                @page{
                                    size: A4 landscape;
                                }
                            
                        </style>
                    </head>
                    <body>
                    
                        <div class="container">
                            <table>
                                <tr>
                                    <th>Glider type:</th>
                                    <th>Registration number:</th>
                                    <th>Date:</th>
                                    <th>Total glider flight time:</th>
                                    <th>Glider flight count:</th>
                                </tr>
                                <tr>
                                """;
            String headerRow = """
                                    <td>%s</td>
                                                    <td>%s</td>
                                                    <td>%s</td>
                                                    <td>%s</td>
                                                    <td>%s</td>
                                </tr>
                            </table>
                    
                            <h2>Flights information</h2>
                            <div class="table-responsive">
                                <table>
                                    <thead>
                                        <tr>
                                            <th rowspan = "2">#</th>
                                            <th rowspan = "2">Pilot 1</th>
                                            <th rowspan = "2">Pilot 2</th>
                                            <th colspan = "2">Point of</th>
                                            <th rowspan = "2">Task</th>
                                            <th colspan = "2">Time of</th>
                                            <th rowspan = "2">Flight time</th>
                                            <th rowspan = "2">Pre-flight Check-up</th>
                                            <th rowspan = "2">Signature</th>
                                        </tr>
                                        <tr>
                                        	<th>departure</th>
                                            <th>arrival</th>
                                            <th>departure</th>
                                            <th>arrival</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                    """;
            String templateEnd = """
                                </tbody>
                                </table>
                            </div>
                        </div>
                    
                    </body>
                    </html>
                    """;
            String recordTemplate = """
                    <tr>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td></td>
                    </tr>
                    """;
            Flight first = flights.getFirst();

            //builds HTML of the document
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            String header = String.format(headerRow, first.getGlider().getType(),  first.getGlider().getRegistrationNumber(), sdf.format(first.getDate()),
                    first.getGlider().getTotalFlightTime(), first.getGlider().getFlightCount());
            StringBuilder pdfHTML = new StringBuilder();
            pdfHTML.append(templateBegin);
            pdfHTML.append(header);
            int i = 1;
            for(final Flight flight : flights) {

                String row = String.format(recordTemplate, i, flight.getPilot1().getName(), flight.getPilot2()!= null?flight.getPilot2().getName():"",
                            flight.getPointOfDeparture(), flight.getPointOfArrival(), flight.getTask(), flight.getTimeOfDeparture(),
                            flight.getTimeOfArrival(), flight.getFlightTime(), flight.getPreFlightCheckup());

                pdfHTML.append(row); // <=> pdfHTML = pdfHTML + "/n" +
                i++;
            }
            pdfHTML.append(templateEnd);

            //create pdf file bytes
            byte[] pdfBytes;
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.withUri("exported.pdf");
                builder.toStream(os);
                builder.withHtmlContent(pdfHTML.toString(), "/");
                builder.run();
                pdfBytes = os.toByteArray();
            }
            //create download response downloading pdf file based on pdf bytes
            return new DownloadResponse(
                    new java.io.ByteArrayInputStream(pdfBytes),
                    first.getDate() + first.getGlider().getRegistrationNumber() + ".pdf",
                    "application/pdf",
                    -1
            );
        }
        catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    //navigates the display to this view
    public static void showView() {
        UI.getCurrent().navigate(FlightsView.class);
    }
}