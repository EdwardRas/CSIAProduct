package com.example.application.base.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Layout
public final class MainLayout extends AppLayout implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        boolean isLoggedIn = "user".equals(VaadinSession.getCurrent().getAttribute("username")) || "robert".equals(VaadinSession.getCurrent().getAttribute("username"));
        if (!isLoggedIn) {
            event.rerouteTo(LoginView.class);
        }
        setDrawerOpened(isLoggedIn);
        
        getElement().getChildren().filter(child -> child.getComponent().isPresent()).forEach(child -> {
            Component component = child.getComponent().get();
            // Navbar toggle and title, and drawer components should be visible only if logged in
            if (component instanceof com.vaadin.flow.component.applayout.DrawerToggle ||
                component instanceof com.vaadin.flow.component.html.H1 ||
                (component.getElement().getAttribute("slot") != null && component.getElement().getAttribute("slot").equals("drawer"))) {
                component.setVisible(isLoggedIn);
            }
        });
    }

    MainLayout() {
        setPrimarySection(Section.DRAWER);
        var toggle = new com.vaadin.flow.component.applayout.DrawerToggle();
        var title = new com.vaadin.flow.component.html.H1("e-Onboard Technical Log");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "0");

        addToNavbar(toggle, title);
        addToDrawer(createHeader(), new Scroller(createTopNav()));

        boolean isLoggedIn = VaadinSession.getCurrent().getAttribute("username") != null;
        toggle.setVisible(isLoggedIn);
        title.setVisible(isLoggedIn);
        setDrawerOpened(isLoggedIn);
    }

    private Component createHeader() {
        var appLogo = VaadinIcon.NOTEBOOK.create();
        appLogo.setSize("48px");
        appLogo.setColor("green");

        var appName = new Span("e-OTL");
        appName.getStyle().setFontWeight(Style.FontWeight.BOLD);

        var header = new VerticalLayout(appLogo, appName);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        return header;
    }

    private SideNav createTopNav() {
        var nav = new SideNav();
        nav.addClassNames(LumoUtility.Margin.Horizontal.MEDIUM);
        MenuConfiguration.getMenuEntries().forEach(entry -> nav.addItem(createSideNavItem(entry)));
        return nav;
    }

    private SideNavItem createSideNavItem(MenuEntry menuEntry) {
        if (menuEntry.icon() != null) {
            return new SideNavItem(menuEntry.title(), menuEntry.path(), new Icon(menuEntry.icon()));
        } else {
            return new SideNavItem(menuEntry.title(), menuEntry.path());
        }
    }
}
