package com.example.application.base.ui;

import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

//very basic login page
@Route("")//first view which opens
public class LoginView extends VerticalLayout {
    private String hashedPassword;
    public LoginView() {
        LoginForm loginForm = new LoginForm();
        add(loginForm);
    }
}
