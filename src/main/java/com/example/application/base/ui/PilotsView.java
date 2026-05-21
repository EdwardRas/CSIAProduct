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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.List;

@Route("pilots")
public class PilotsView extends VerticalLayout implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent e) {
        if(!"user".equals(VaadinSession.getCurrent().getAttribute("username"))) {
            e.rerouteTo(LoginView.class);
        }
    }

    //DB service objects
    private final PilotService pilotService;
    private final FlightService flightService;

    @Autowired//automatically handles method inputs
    public PilotsView(PilotService pilotService, FlightService flightService) {
        this.flightService = flightService;
        this.pilotService = pilotService;

        //get all pilots in DB
        List<Pilot> records = this.pilotService.getAllPilots();

        //create grid to display pilots
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
                .setHeader("License Number").setResizable(true).setSortable(true);
        Grid.Column<Pilot> statusColumn = grid
                .addColumn(Pilot::isFlying)
                .setHeader("Is flying?").setResizable(true).setSortable(true);

        //set items of grid to be all pilots
        GridListDataView<Pilot> dataView = grid.setItems(records);

        //create search text field
        TextField searchField = new TextField();
        searchField.setWidth("250px");
        searchField.setLabel("Search");

        //create button to open addition form
        Button addButton = new Button("Add Pilot", e -> {
            try {
                showAdditionForm(pilotService);
            } catch (SQLException ex) {
                Notification notification = Notification
                        .show("Error: " + ex.getErrorCode());
                throw new RuntimeException(ex);
            }
        });

        //get selection model for pilot selection to get selected item in grid
        GridSingleSelectionModel<Pilot> selectionModel = (GridSingleSelectionModel<Pilot>)grid.getSelectionModel();

        //create delete button
        Button deleteButton = new Button("Delete Pilot", e -> {
            Long deletingId;
            if (selectionModel.getSelectedItem().isPresent()) {
                deletingId = selectionModel.getSelectedItem().get().getId();
                if (deletingId >= 0) {
                    //create dialog to confirm deletion
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
                            Notification notification = Notification
                                    .show("Error: " + ex.getErrorCode());
                            throw new RuntimeException(ex);
                        }
                    });
                    deleteDialog.open();
                }
            }
        });

        //create button to show pilot edit form
        Button editButton = new Button("Edit Pilot", e -> {
            if (selectionModel.getSelectedItem().isPresent()) {
                try {
                    showEditForm(pilotService, selectionModel.getSelectedItem().get());
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
            }
        });

        //set buttons which are not always to be disabled by default
        deleteButton.setEnabled(false);
        editButton.setEnabled(false);

        //search functionality
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

        //add all UI elements
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSizeFull();
        buttonsLayout.add(new Button("Flights", e -> FlightsView.showView()));
        buttonsLayout.add(new Button("Gliders", e -> GlidersView.showView()));
        buttonsLayout.add(searchField, addButton, deleteButton, editButton);
        add(buttonsLayout, grid);
    }

    //show user form to input data for pilot addition
    private void showAdditionForm(PilotService pilotService) throws SQLException {
        //create UI elements for data input
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

        //create buttons to confirm addition
        Button addButton = new Button("Add", e -> {
            //if any field has an invalid value, end lambda function early
            if(nameField.isEmpty()){
                Notification.show("At least one field value is invalid");
                return;
            }
            String name = nameField.getValue();
            String licenseNumber = licenseNumberField.getValue();
            pilotService.addPilot(name, licenseNumber, false);
            additionForm.close();
            UI.getCurrent().getPage().reload();
        });
        Button cancelButton = new Button("Cancel", e -> additionForm.close());

        //add all UI elements, show form
        additionForm.getFooter().add(cancelButton, addButton);
        additionForm.add(formLayout);
        additionForm.open();
    }

    //show user form to input data for pilot editing
    private void showEditForm(PilotService pilotService, Pilot pilot) throws SQLException {
        //create UI elements for data input
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

        //create buttons to confirm addition
        Button addButton = new Button("Add", e -> {
            if(nameField.isEmpty()){
                Notification.show("At least one field value is invalid");
                return;
            }
            pilot.setName(nameField.getValue());
            pilot.setLicenseNumber(licenseNumberField.getValue());
            pilotService.editPilot(pilot);
            editForm.close();
            UI.getCurrent().getPage().reload();
        });
        Button cancelButton = new Button("Cancel", e -> editForm.close());

        //add all UI elements, show form
        editForm.getFooter().add(cancelButton, addButton);
        editForm.add(formLayout);
        editForm.open();
        
    }
    //navigate the UI to this view
    public static void showView() {
        UI.getCurrent().navigate(PilotsView.class);
    }
}

