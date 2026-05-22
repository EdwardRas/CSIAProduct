package com.example.application.base.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

//very basic login page
@Route("")//first view which opens
public class LoginView extends VerticalLayout {
    //password: xnB2Muk3CjO
    //Hash method: MD5
    private static final Map<String, String> ALLOWED_USERS = Map.of("user", "e450e8afb4a460cbffdeab3b0691e5c7");

    public LoginView() {
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSizeFull();
        LoginForm loginForm = new LoginForm();
        loginForm.addLoginListener(e -> {
            String hash = hashMD5(e.getPassword());
            if(ALLOWED_USERS.containsKey(e.getUsername()) && ALLOWED_USERS.get(e.getUsername()).equals(hash)) {
                VaadinSession.getCurrent().setAttribute("username", e.getUsername());
                GlidersView.showView();
            }
            else {
                loginForm.setError(true);
            }
        });
        loginForm.addForgotPasswordListener(e -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Forgot Password?");
            dialog.setText("The password can be found in the documentation (Criterion D). If you do not have access to the documentation, you shouldn't have access to this application.");
            dialog.setConfirmText("OK");
            dialog.open();
        });
        add(loginForm);
    }
    private String hashMD5(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
