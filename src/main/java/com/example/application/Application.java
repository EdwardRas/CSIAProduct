package com.example.application;

import com.example.application.flights.Flight;
import com.example.application.gliders.Glider;
import com.example.application.pilots.Pilot;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;

@SpringBootApplication
@StyleSheet(Lumo.STYLESHEET) // Use Aura.STYLESHEET to use Aura instead
@StyleSheet(Lumo.UTILITY_STYLESHEET)
@StyleSheet("styles.css") // Your custom styles
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
         Glider testGlider = new Glider("SP1234");
        Pilot testPilot = new Pilot(1, "Adam");
        Flight testFlight = new Flight(1, testGlider, testPilot);
        SpringApplication.run(Application.class, args);
    }

}
