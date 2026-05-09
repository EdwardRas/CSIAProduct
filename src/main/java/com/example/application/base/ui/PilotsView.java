package com.example.application.base.ui;

import com.example.application.flights.Flight;
import com.example.application.flights.FlightService;
import com.example.application.gliders.Glider;
import com.example.application.pilots.Pilot;
import com.example.application.pilots.PilotService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
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
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.List;

@Route("building-apps/navigate/pilots")
public class PilotsView extends VerticalLayout {
    private final PilotService pilotService;
    private final FlightService flightService;
    @Autowired
    public PilotsView(PilotService pilotService, FlightService flightService) {
        this.flightService = flightService;
        this.pilotService = pilotService;
        List<Pilot> records = this.pilotService.getAllPilots();
        Grid<Pilot> grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        Grid.Column<Pilot> IDColumn = grid
                .addColumn(Pilot::getId).setHeader("ID")
                .setResizable(true).setSortable(true);
        Grid.Column<Pilot> nameColumn = grid
                .addColumn(Pilot::getName)
                .setHeader("Name").setResizable(true).setSortable(true);
        Grid.Column<Pilot> licenseNumberColumn = grid
                .addColumn(Pilot::getLicenseNumber)
                .setHeader("Type").setResizable(true).setSortable(true);
        Grid.Column<Pilot> statusColumn = grid
                .addColumn(Pilot::isFlying)
                .setHeader("Is flying").setResizable(true).setSortable(true);

        GridListDataView<Pilot> dataView = grid.setItems(records);
        TextField searchField = new TextField();
        searchField.setWidth("250px");
        searchField.setLabel("Search:");
        Button addButton = new Button("Add Pilot", e -> {
            try {
                showAdditionForm(pilotService);
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
        GridSingleSelectionModel<Pilot> selectionModel = (GridSingleSelectionModel<Pilot>)grid.getSelectionModel();
        Button deleteButton = new Button("Delete Pilot", e -> {
            Long deletingId;
            if (selectionModel.getSelectedItem().isPresent()) {
                deletingId = selectionModel.getSelectedItem().get().getId();
                if (deletingId >= 0) {
                    ConfirmDialog deleteDialog = new ConfirmDialog();
                    deleteDialog.setHeader("Delete Pilot?");
                    deleteDialog.setText(
                            "Are you sure you want to permanently delete this item? This will also delete all flights where it is as Pilot 1");

                    deleteDialog.setCancelable(true);
                    deleteDialog.addCancelListener(event -> deleteDialog.close());

                    deleteDialog.setConfirmText("Delete");
                    deleteDialog.setConfirmButtonTheme("error primary");
                    deleteDialog.addConfirmListener(event -> {
                        try {
                            pilotService.deletePilot(deletingId);
                            flightService.modifyFlightByPilotId(deletingId);
                            UI.getCurrent().getPage().reload();;
                        }
                        catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    });;
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
        Button editButton = new Button("Edit Pilot", e -> {
            if (selectionModel.getSelectedItem().isPresent()) {
                try {
                    showEditForm(pilotService, selectionModel.getSelectedItem().get());
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
            boolean matchesName = item.getName().contains(searchTerm);
            boolean matchesLicenseNumber = item.getLicenseNumber().contains(searchTerm);

            return matchesID || matchesName || matchesLicenseNumber ;
        });
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSizeFull();
        buttonsLayout.add(new Button("Flights", e -> FlightsView.showView()));
        buttonsLayout.add(new Button("Gliders", e -> GlidersView.showView()));
        buttonsLayout.add(searchField, addButton, deleteButton, editButton, testDialogButton);
        add(buttonsLayout, grid);
    }

    private void showAdditionForm(PilotService pilotService) throws SQLException {
        Dialog additionForm = new Dialog();
        FormLayout formLayout = new FormLayout();
        formLayout.setAutoResponsive(true);
        formLayout.setExpandFields(true);
        TextField nameField = new TextField();
        nameField.setLabel("Name");
        nameField.setRequired(true);
        formLayout.setColspan(nameField, 2);
        TextField licenseNumberField = new TextField();
        licenseNumberField.setLabel("License Number");
        formLayout.setColspan(licenseNumberField, 2);
        formLayout.addFormRow(nameField);
        formLayout.addFormRow(licenseNumberField);
        Button addButton = new Button("Add", e -> {
            String name = nameField.getValue();
            String licenseNumber = licenseNumberField.getValue();
            pilotService.addPilot(name, licenseNumber, false);
            additionForm.close();
            UI.getCurrent().getPage().reload();
        });
        Button cancelButton = new Button("Cancel", e -> additionForm.close());
        additionForm.getFooter().add(cancelButton, addButton);
        additionForm.add(formLayout);
        additionForm.open();
    }
    private void showEditForm(PilotService pilotService, Pilot pilot) throws SQLException {
        Dialog editForm = new Dialog();
        FormLayout formLayout = new FormLayout();
        formLayout.setAutoResponsive(true);
        formLayout.setExpandFields(true);
        TextField nameField = new TextField();
        nameField.setLabel("Name");
        nameField.setRequired(true);
        nameField.setValue(pilot.getName());
        formLayout.setColspan(nameField, 2);
        TextField licenseNumberField = new TextField();
        licenseNumberField.setLabel("License Number");
        formLayout.setColspan(licenseNumberField, 2);
        licenseNumberField.setValue(pilot.getLicenseNumber());
        formLayout.addFormRow(nameField);
        formLayout.addFormRow(licenseNumberField);
        Button addButton = new Button("Add", e -> {
            String name = nameField.getValue();
            String licenseNumber = licenseNumberField.getValue();
            pilotService.editPilot(pilot, name, licenseNumber, pilot.isFlying);
            try {
                List<Flight> oldFlights = flightService.getFlightsByPilot(pilot);
                for(int i = 0; i < oldFlights.size(); i++) {
                    Flight editedFlight;
                    if (oldFlights.get(i).getPilot1().equals(pilot)) {
                        editedFlight = new Flight(oldFlights.get(i).getGlider(), pilot, oldFlights.get(i).getPilot2(), oldFlights.get(i).getDate(), oldFlights.get(i).getPointOfDeparture(), oldFlights.get(i).getPointOfArrival(), oldFlights.get(i).getTimeOfDeparture(), oldFlights.get(i).getTimeOfArrival(), oldFlights.get(i).getTask(), oldFlights.get(i).getPreFlightCheckup());
                    } else {
                        editedFlight = new Flight(oldFlights.get(i).getGlider(), oldFlights.get(i).getPilot1(), pilot, oldFlights.get(i).getDate(), oldFlights.get(i).getPointOfDeparture(), oldFlights.get(i).getPointOfArrival(), oldFlights.get(i).getTimeOfDeparture(), oldFlights.get(i).getTimeOfArrival(), oldFlights.get(i).getTask(), oldFlights.get(i).getPreFlightCheckup());
                    }
                    flightService.editFlight(oldFlights.get(i), editedFlight);
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            editForm.close();
            UI.getCurrent().getPage().reload();
            

                
        });
        Button cancelButton = new Button("Cancel", e -> editForm.close());
        editForm.getFooter().add(cancelButton, addButton);
        editForm.add(formLayout);
        editForm.open();
        
    }
    public static void showView() {
        UI.getCurrent().navigate(PilotsView.class);
    }
}

