package org.xyp.sample.spring.webvaadin.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.val;
import org.xyp.sample.spring.webvaadin.controller.HelloController;
import org.xyp.sample.spring.webvaadin.domain.Person;
import org.xyp.sample.spring.webvaadin.layout.MainLayout;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@PageTitle("MainView")
@Route(value = "/", layout = MainLayout.class)
public class MainView extends VerticalLayout {

    final TextField textField = new TextField("Your name");
    final Button button = new Button("Say hello");
    final HelloController helloController;

    public MainView(HelloController helloController) {
        this.helloController = helloController;
        button.addClickListener(e -> {
            Notification.show("hello" + textField.getValue());
            textField.clear();

            val resp = helloController.hello();
            if (resp.data() != null) {
                textField.setValue(resp.data().name());
            } else if (resp.problem() != null) {
                textField.setValue(resp.problem().getDetail());
            }
//            try {
//                getPerson();
//            } catch (URISyntaxException ex) {
//                throw new RuntimeException(ex);
//            }
        });
        add(textField, button);
    }

//    private void getPerson() throws URISyntaxException {
//        HttpRequest httpRequest = HttpRequest.newBuilder()
//            .uri(URI.create("http://localhost:8080/api/v1/hello"))
//            .GET()
//            .build();
//
//        HttpClient httpClient = HttpClient.newHttpClient();
//
//        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
//            .thenApply(HttpResponse::body)
//            .thenAccept(text -> textField.setValue("" + text))
//            .thenAccept(System.out::println)
//            .thenAccept(ignored -> httpClient.close())
//        ;
//    }
}
