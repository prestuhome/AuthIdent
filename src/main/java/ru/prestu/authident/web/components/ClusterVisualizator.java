package ru.prestu.authident.web.components;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.addon.charts.model.style.Color;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.*;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.springframework.beans.factory.annotation.Autowired;
import ru.prestu.authident.domain.model.entities.Author;
import ru.prestu.authident.domain.model.entities.Book;
import ru.prestu.authident.domain.repositories.BookRepository;
import ru.prestu.authident.serverside.analyzer.elements.TextCharacteristic;
import ru.prestu.authident.web.Notifications;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SpringComponent
@UIScope
public class ClusterVisualizator extends VerticalLayout {

    private Chart chart;
    private ComboBox<TextCharacteristic> firstSelector;
    private ComboBox<TextCharacteristic> secondSelector;
    private HorizontalLayout resultLayout;
    private Label resultLabel;

    private List<? extends Cluster<Book>> clusters;
    private Book bookWithNullAuthor;
    private Author prospectiveAuthor;

    private BookRepository bookRepository;

    @Autowired
    public ClusterVisualizator(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
        setSpacing(true);
        setMargin(false);

        chart = new Chart(ChartType.SCATTER);
        chart.setSizeFull();
        chart.setHeight(600, Unit.PIXELS);

        HorizontalLayout selectors = new HorizontalLayout();
        selectors.setCaption("Выберете характеристики");
        selectors.setMargin(false);
        selectors.setSpacing(true);
        firstSelector = new ComboBox<>();
        firstSelector.setWidth(600, Unit.PIXELS);
        firstSelector.setItems(TextCharacteristic.values());
        firstSelector.setItemCaptionGenerator(TextCharacteristic::getDescription);
        firstSelector.setEmptySelectionAllowed(false);
        firstSelector.setValue(TextCharacteristic.AVG_PARAGRAPH_LENGTH);
        firstSelector.addValueChangeListener(valueChangeEvent -> {
           if (firstSelector.getValue().equals(secondSelector.getValue())) {
               Notifications.show("Ошибка", "Нельзя выбирать две одинаковых характеристики", Notifications.SMALL_WINDOW);
               firstSelector.setValue(valueChangeEvent.getOldValue());
           } else {
               visualize();
           }
        });
        secondSelector = new ComboBox<>();
        secondSelector.setWidth(600, Unit.PIXELS);
        secondSelector.setItems(TextCharacteristic.values());
        secondSelector.setItemCaptionGenerator(TextCharacteristic::getDescription);
        secondSelector.setEmptySelectionAllowed(false);
        secondSelector.setValue(TextCharacteristic.MAX_PARAGRAPH_LENGTH);
        secondSelector.addValueChangeListener(valueChangeEvent -> {
            if (firstSelector.getValue().equals(secondSelector.getValue())) {
                Notifications.show("Ошибка", "Нельзя выбирать две одинаковых характеристики", Notifications.SMALL_WINDOW);
                secondSelector.setValue(valueChangeEvent.getOldValue());
            } else {
                visualize();
            }
        });
        selectors.addComponents(firstSelector, secondSelector);

        resultLayout = new HorizontalLayout();
        resultLayout.setMargin(false);
        resultLayout.setSpacing(true);
        resultLayout.setVisible(false);
        resultLabel = new Label();
        Button confirmButton = new Button("Подтвердить");
        confirmButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
        confirmButton.addClickListener(clickEvent -> {
            if (bookWithNullAuthor != null && prospectiveAuthor != null) {
                bookWithNullAuthor.setAuthor(prospectiveAuthor);
                this.bookRepository.save(bookWithNullAuthor);
                visualize();
            }
        });
        Button rejectButton = new Button("Отменить");
        rejectButton.addClickListener(clickEvent -> {
            if (bookWithNullAuthor != null) this.bookRepository.delete(bookWithNullAuthor);
            visualize();
        });
        resultLayout.addComponents(resultLabel, confirmButton, rejectButton);

        addComponents(chart, selectors, resultLayout);
        setVisible(false);
    }

    private void update() {
        bookWithNullAuthor = null;
        prospectiveAuthor = null;
        resultLabel.setValue("");
        resultLayout.setVisible(false);
    }

    public void visualize(List<? extends Cluster<Book>> clusters) {
        this.clusters = clusters;
        visualize();
    }

    private void visualize() {
        setVisible(true);
        update();
        TextCharacteristic firstCharacteristic = firstSelector.getValue();
        TextCharacteristic secondCharacteristic = secondSelector.getValue();

        XAxis xAxis = new XAxis();
        xAxis.setTitle(firstCharacteristic.getDescription());
        YAxis yAxis = new YAxis();
        yAxis.setTitle(secondCharacteristic.getDescription());
        Tooltip tooltip = new Tooltip();
        tooltip.setFormatter("'<b>' + this.series.name + '</b><br/>' + this.point.name + '<br/>X:' + this.point.x + '<br/>Y:' + this.point.y");

        Configuration config = new Configuration();
        ChartModel model = new ChartModel();
        model.setZoomType(ZoomType.XY);
        model.setType(ChartType.SCATTER);
        config.setChart(model);
        config.setTitle((String) null);
        config.addxAxis(xAxis);
        config.addyAxis(yAxis);
        config.setTooltip(tooltip);

        int firstOrdinal = firstCharacteristic.ordinal();
        int secondOrdinal = secondCharacteristic.ordinal();
        for (Cluster<Book> cluster : clusters) {
            List<Book> books = cluster.getPoints();
            Author clusterOwner = books.stream()
                    .collect(Collectors.groupingBy(Book::getAuthor, Collectors.counting()))
                    .entrySet().stream().max((entry1, entry2) -> {
                        if (Objects.equals(entry1.getValue(), entry2.getValue())) return 0;
                        else if (entry1.getValue() > entry2.getValue()) return 1;
                        else return -1;
                    }).get().getKey();
            DataSeries clusterDataSeries = new DataSeries();
            clusterDataSeries.setName(clusterOwner.toString());
            for (Book book : books) {
                DataSeriesItem item = new DataSeriesItem();
                item.setX(book.getBookInfo().get(firstOrdinal));
                item.setY(book.getBookInfo().get(secondOrdinal));
                item.setName(book.toString());
                clusterDataSeries.add(item);
                if (book.getAuthor() == null) {
                    bookWithNullAuthor = book;
                    prospectiveAuthor = clusterOwner;
                    resultLabel.setValue("Предполагаемый автор книги " + bookWithNullAuthor + " " + prospectiveAuthor);
                    resultLayout.setVisible(true);
                }
            }
            if (cluster instanceof CentroidCluster) {
                double[] centerPoint = ((CentroidCluster<?>) cluster).getCenter().getPoint();
                DataSeriesItem center = new DataSeriesItem();
                Marker marker = new Marker(true);
                marker.setRadius(5);
                marker.setLineWidth(2);
                marker.setLineColor(SolidColor.BLACK);
                center.setMarker(marker);
                center.setX(centerPoint[firstOrdinal]);
                center.setY(centerPoint[secondOrdinal]);
                center.setName("Центроид кластера");
                clusterDataSeries.add(center);
            }
            config.addSeries(clusterDataSeries);
        }
        chart.setConfiguration(config);
        chart.drawChart();
    }
}
