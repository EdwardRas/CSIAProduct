package com.example.application.base.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

//very basic login page
@Route("")//first view which opens
public class LoginView extends VerticalLayout {
    //TODO nie wiem czy trzymać jako string
    private String hashedPassword;
    private String tempPassword = "adminskip";
    public LoginView() {
        LoginForm loginForm = new LoginForm();
        add(loginForm);
        // TODO temporary skip until login implement
        TextField skip = new TextField("skip password");
        add(skip);
        add(new Button("skip (temporary)", e -> {
            if(skip.getValue().equals(tempPassword)) {
                GlidersView.showView();
            }
        }));
    }
}
