package ru.prestu.authident.web;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.springframework.beans.factory.annotation.Autowired;
import ru.prestu.authident.domain.model.Book;
import ru.prestu.authident.serverside.analyzer.elements.TextCharacteristic;

import java.util.List;

@SpringComponent
@UIScope
public class ClusterVisualizator extends VerticalLayout {

    private Chart chart;
    private ComboBox<TextCharacteristic> firstSelector;
    private ComboBox<TextCharacteristic> secondSelector;

    private List<? extends Cluster<Book>> clusters;

    @Autowired
    public ClusterVisualizator() {
        setSpacing(true);
        setMargin(false);

        chart = new Chart(ChartType.BUBBLE);
        chart.setSizeFull();

        HorizontalLayout selectors = new HorizontalLayout();
        selectors.setCaption("Выберете характеристики");
        selectors.setMargin(false);
        selectors.setSpacing(true);

        firstSelector = new ComboBox<>();
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

        addComponents(chart, selectors);
        setVisible(false);
    }

    public void visualize(List<? extends Cluster<Book>> clusters) {
        this.clusters = clusters;
        visualize();
    }

    public void visualize() {
        setVisible(true);
        TextCharacteristic firstCharacteristic = firstSelector.getValue();
        TextCharacteristic secondCharacteristic = secondSelector.getValue();

        XAxis xAxis = new XAxis();
        xAxis.setTitle(firstCharacteristic.getDescription());
        YAxis yAxis = new YAxis();
        yAxis.setTitle(secondCharacteristic.getDescription());

        Configuration config = new Configuration();
        PlotOptionsBubble plotOptionsBubble = new PlotOptionsBubble();
        plotOptionsBubble.setMinSize("3");
        plotOptionsBubble.setDisplayNegative(false);
        config.setPlotOptions(plotOptionsBubble);
        config.addxAxis(xAxis);
        config.addyAxis(yAxis);

        int firstOrdinal = firstCharacteristic.ordinal();
        int secondOrdinal = secondCharacteristic.ordinal();
        for (Cluster<Book> cluster : clusters) {
            DataSeries clusterDataSeries = new DataSeries();
            for (Book book : cluster.getPoints()) {
                DataSeriesItem3d item = new DataSeriesItem3d();
                item.setX(book.getBookInfo().get(firstOrdinal));
                item.setY(book.getBookInfo().get(secondOrdinal));
                item.setZ(3);
                item.setName(book.toString());
                clusterDataSeries.add(item);
            }
            if (cluster instanceof CentroidCluster) {
                double[] centerPoint = ((CentroidCluster<?>) cluster).getCenter().getPoint();
                DataSeriesItem3d center = new DataSeriesItem3d();
                center.setX(centerPoint[firstOrdinal]);
                center.setY(centerPoint[secondOrdinal]);
                center.setZ(4);
                center.setName("Центроид");
                clusterDataSeries.add(center);
            }
            config.addSeries(clusterDataSeries);
        }
        chart.setConfiguration(config);
    }
}
