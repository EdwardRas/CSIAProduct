package com.example.application.base.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Route("properties")
public class PropertiesView extends VerticalLayout implements BeforeEnterObserver {
    //development debug tool


    @Override
    public void beforeEnter(BeforeEnterEvent e) {
        if(!"user".equals(VaadinSession.getCurrent().getAttribute("username"))) {
            e.rerouteTo(LoginView.class);
        }
    }

    public static class PropertyEntry {
        private String key;
        private String value;

        public PropertyEntry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    @Autowired
    public PropertiesView(Environment env) {
        // Get all system properties and sort them by key
        Map<String, String> sortedProperties = new TreeMap<>();
        if (env instanceof ConfigurableEnvironment) {
            ConfigurableEnvironment configEnv = (ConfigurableEnvironment) env;
            for (PropertySource<?> propertySource : configEnv.getPropertySources()) {
                if (propertySource instanceof EnumerablePropertySource) {
                    EnumerablePropertySource<?> enumerablePropertySource = (EnumerablePropertySource<?>) propertySource;
                    for (String propertyName : enumerablePropertySource.getPropertyNames()) {
                        String value = String.valueOf(enumerablePropertySource.getProperty(propertyName));
                        sortedProperties.put("ENV:" + propertySource.getName() + ":" + propertyName,
                                String.valueOf(value));
                    }
                }
            }
        }
        Map<Object, Object> systemProperties = System.getProperties();
        for (Map.Entry<Object, Object> entry : systemProperties.entrySet()) {
            sortedProperties.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        List<PropertyEntry> propertyList = new ArrayList<>();
        sortedProperties.forEach((key, value) -> propertyList.add(new PropertyEntry(key, value)));

        // Create grid to display properties
        Grid<PropertyEntry> grid = new Grid<>();
        grid.addColumn(PropertyEntry::getKey).setHeader("Property Key").setResizable(true).setSortable(true);
        grid.addColumn(PropertyEntry::getValue).setHeader("Value").setResizable(true).setSortable(true);

        GridListDataView<PropertyEntry> dataView = grid.setItems(propertyList);

        // Create search text field
        TextField searchField = new TextField();
        searchField.setWidth("300px");
        searchField.setLabel("Search Properties");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> dataView.refreshAll());

        dataView.addFilter(item -> {
            String searchTerm = searchField.getValue().trim().toLowerCase();
            if (searchTerm.isEmpty()) {
                return true;
            }
            return item.getKey().toLowerCase().contains(searchTerm) ||
                    item.getValue().toLowerCase().contains(searchTerm);
        });

        // Navigation buttons
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSizeFull();
        buttonsLayout.add(new Button("Flights", e -> FlightsView.showView()));
        buttonsLayout.add(new Button("Gliders", e -> GlidersView.showView()));
        buttonsLayout.add(new Button("Pilots", e -> PilotsView.showView()));
        buttonsLayout.add(searchField);

        add(buttonsLayout, grid);
        setSizeFull();
        grid.setSizeFull();
    }

    public static void showView() {
        UI.getCurrent().navigate(PropertiesView.class);
    }
}
