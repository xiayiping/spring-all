package org.xyp.sample.spring.webvaadin.layout;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.xyp.sample.spring.webvaadin.view.MainView;

public class MainLayout extends AppLayout {

    private H2 viewTitle;
    public MainLayout() {

//        setPrimarySection(Section.DRAWER);
        addNavbarContent();
        addDrawerContent();
    }

    private void addNavbarContent() {
        var toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");
        toggle.setTooltipText("Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(
            LumoUtility.FontSize.LARGE,
            LumoUtility.Margin.NONE,
            LumoUtility.Flex.GROW
        );

        var header = new Header(toggle, viewTitle);
        header.addClassNames(
            LumoUtility.AlignItems.CENTER,
            LumoUtility.Display.FLEX,
            LumoUtility.Padding.End.MEDIUM,
            LumoUtility.Width.FULL
        );

        addToNavbar(false, header);
    }

    private void addDrawerContent() {
        var appName = new Span("Vaadin Chat");
        appName.addClassNames(
            LumoUtility.AlignItems.CENTER,
            LumoUtility.Display.FLEX,
            LumoUtility.FontSize.LARGE,
            LumoUtility.FontWeight.SEMIBOLD,
            LumoUtility.Height.XLARGE,
            LumoUtility.Padding.Horizontal.MEDIUM
        );

        addToDrawer(appName, new Scroller(createSideNav()));
    }

    private SideNav createSideNav() {
        SideNav nav = new SideNav();

        nav.addItem(
            new SideNavItem("Lobby", MainView.class, VaadinIcon.BUILDING.create())
        );

        return nav;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        if (getContent() == null) {
            return "";
        } else if (getContent() instanceof HasDynamicTitle titleHolder) {
            return titleHolder.getPageTitle();
        } else {
            var title = getContent().getClass().getAnnotation(PageTitle.class);
            return title == null ? "" : title.value();
        }
    }
}
