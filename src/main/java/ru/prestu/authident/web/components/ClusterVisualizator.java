package ru.prestu.authident.web.components;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.*;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;
import org.springframework.beans.factory.annotation.Autowired;
import ru.prestu.authident.domain.model.entities.Author;
import ru.prestu.authident.domain.model.entities.Book;
import ru.prestu.authident.domain.model.enums.Distances;
import ru.prestu.authident.domain.repositories.BookRepository;
import ru.prestu.authident.domain.model.enums.TextCharacteristic;
import ru.prestu.authident.serverside.clustering.FuzzyCMeansClustering;
import ru.prestu.authident.serverside.clustering.elements.Cluster;
import ru.prestu.authident.serverside.clustering.elements.Element;
import ru.prestu.authident.web.Notifications;
import java.text.DecimalFormat;
import java.util.*;

@SpringComponent
@UIScope
public class ClusterVisualizator extends VerticalLayout {

    private ComboBox<Distances> distanceSelector;
    private CheckBoxGroup<TextCharacteristic> characteristicsSelector;
    private Chart chart;
    private ComboBox<TextCharacteristic> xSelector;
    private ComboBox<TextCharacteristic> ySelector;
    private ComboBox<TextCharacteristic> zSelector;
    private CheckBox showCenters;
    private CheckBox useNormBox;
    private Label accuracyLabel;
    private HorizontalLayout authorVerificationLayout;
    private Label authorVerificationLabel;

    private Book bookWithNullAuthor;
    private Author prospectiveAuthor;

    private BookRepository bookRepository;

    private DecimalFormat format = new DecimalFormat("#.##");
    private List<Cluster> clusters;

    @Autowired
    public ClusterVisualizator(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
        setSpacing(true);
        setMargin(false);

        distanceSelector = new ComboBox<>("Выберите расстояние");
        distanceSelector.setWidth(600, Unit.PIXELS);
        distanceSelector.setItems(Distances.values());
        distanceSelector.setItemCaptionGenerator(Distances::getDescription);
        distanceSelector.setEmptySelectionAllowed(false);
        distanceSelector.setValue(Distances.EUCLIDEAN_DISTANCE);
        distanceSelector.addValueChangeListener(valueChangeEvent -> cluster());

        characteristicsSelector = new CheckBoxGroup<>("Выберите характеристики", Arrays.asList(TextCharacteristic.values()));
        characteristicsSelector.setItemCaptionGenerator(TextCharacteristic::getDescription);
        characteristicsSelector.select(TextCharacteristic.values());
        characteristicsSelector.addValueChangeListener(event -> {
            Set<TextCharacteristic> textCharacteristics = characteristicsSelector.getValue();
            if (textCharacteristics.size() < 3) {
                characteristicsSelector.setValue(event.getOldValue());
                return;
            }
            xSelector.setItems(textCharacteristics);
            ySelector.setItems(textCharacteristics);
            zSelector.setItems(textCharacteristics);
            int i = 0;
            for (Iterator<TextCharacteristic> it = textCharacteristics.iterator(); it.hasNext();) {
                if (i == 0) {
                    xSelector.setValue(it.next());
                    i++;
                } else if (i == 1) {
                    ySelector.setValue(it.next());
                    i++;
                } else {
                    zSelector.setValue(null);
                    break;
                }
            }
            cluster();
        });

        chart = new Chart(ChartType.SCATTER);
        chart.setSizeFull();
        chart.setHeight(600, Unit.PIXELS);

        HorizontalLayout xySelectors = new HorizontalLayout();
        xySelectors.setCaption("Выберите характеристики");
        xySelectors.setMargin(false);
        xySelectors.setSpacing(true);
        xSelector = new ComboBox<>();
        xSelector.setWidth(600, Unit.PIXELS);
        xSelector.setItems(TextCharacteristic.values());
        xSelector.setItemCaptionGenerator(TextCharacteristic::getDescription);
        xSelector.setEmptySelectionAllowed(false);
        xSelector.setValue(TextCharacteristic.AVG_PARAGRAPH_LENGTH);
        xSelector.addValueChangeListener(valueChangeEvent -> {
           if (xSelector.getValue().equals(ySelector.getValue()) || xSelector.getValue().equals(zSelector.getValue())) {
               Notifications.show("Ошибка", "Нельзя выбирать одинаковые характеристики", Notifications.SMALL_WINDOW);
               xSelector.setValue(valueChangeEvent.getOldValue());
           } else if (ySelector.getValue() != null) {
               visualize();
           }
        });
        ySelector = new ComboBox<>();
        ySelector.setWidth(600, Unit.PIXELS);
        ySelector.setItems(TextCharacteristic.values());
        ySelector.setItemCaptionGenerator(TextCharacteristic::getDescription);
        ySelector.setEmptySelectionAllowed(false);
        ySelector.setValue(TextCharacteristic.MAX_PARAGRAPH_LENGTH);
        ySelector.addValueChangeListener(valueChangeEvent -> {
            if (ySelector.getValue().equals(xSelector.getValue()) || ySelector.getValue().equals(zSelector.getValue())) {
                Notifications.show("Ошибка", "Нельзя выбирать одинаковые характеристики", Notifications.SMALL_WINDOW);
                ySelector.setValue(valueChangeEvent.getOldValue());
            } else if (xSelector.getValue() != null) {
                visualize();
            }
        });
        zSelector = new ComboBox<>();
        zSelector.setWidth(600, Unit.PIXELS);
        zSelector.setItems(TextCharacteristic.values());
        zSelector.setItemCaptionGenerator(TextCharacteristic::getDescription);
        zSelector.setEmptySelectionAllowed(true);
        zSelector.addValueChangeListener(valueChangeEvent -> {
            if (zSelector.getValue() != null && (zSelector.getValue().equals(xSelector.getValue()) || zSelector.getValue().equals(ySelector.getValue()))) {
                Notifications.show("Ошибка", "Нельзя выбирать одинаковые характеристики", Notifications.SMALL_WINDOW);
                zSelector.setValue(valueChangeEvent.getOldValue());
            } else {
                visualize();
            }
        });
        xySelectors.addComponents(xSelector, ySelector);

        HorizontalLayout checkBoxes = new HorizontalLayout();
        checkBoxes.setMargin(false);
        checkBoxes.setSpacing(true);
        showCenters = new CheckBox("Показывать центройды");
        showCenters.setValue(true);
        showCenters.addValueChangeListener(event -> visualize());
        useNormBox = new CheckBox("Использовать нормированные данные");
        useNormBox.setValue(true);
        useNormBox.addValueChangeListener(event -> cluster());
        checkBoxes.addComponents(showCenters, useNormBox);

        accuracyLabel = new Label();

        authorVerificationLayout = new HorizontalLayout();
        authorVerificationLayout.setMargin(false);
        authorVerificationLayout.setSpacing(true);
        authorVerificationLayout.setVisible(false);
        authorVerificationLabel = new Label();
        Button confirmButton = new Button("Подтвердить");
        confirmButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
        confirmButton.addClickListener(clickEvent -> {
            if (bookWithNullAuthor != null && prospectiveAuthor != null) {
                bookWithNullAuthor.setAuthor(prospectiveAuthor);
                this.bookRepository.save(bookWithNullAuthor);
                update();
            }
        });
        Button rejectButton = new Button("Отменить");
        rejectButton.addClickListener(clickEvent -> {
            if (bookWithNullAuthor != null) this.bookRepository.delete(bookWithNullAuthor);
            update();
        });
        authorVerificationLayout.addComponents(authorVerificationLabel, confirmButton, rejectButton);

        addComponents(distanceSelector, characteristicsSelector, chart, xySelectors, zSelector, checkBoxes, accuracyLabel, authorVerificationLayout);
        setVisible(false);
    }

    public void cluster() {
        List<Book> books;
        books = bookRepository.findAll();
        this.clusters = clusterBooks(books);
        if (clusters != null && clusters.size() != 0) prepareAndVisualize();
    }

    private List<Cluster> clusterBooks(List<Book> books) {
        int[] characteristicOrdinals = new int[characteristicsSelector.getValue().size()];
        int i = 0;
        for (TextCharacteristic characteristic : characteristicsSelector.getValue()) {
            characteristicOrdinals[i++] = characteristic.ordinal();
        }
        Arrays.sort(characteristicOrdinals);
        FuzzyCMeansClustering clustering = new FuzzyCMeansClustering(distanceSelector.getValue().getDistance(), useNormBox.getValue(), characteristicOrdinals);
        return clustering.cluster(books);
    }

    private void update() {
        bookWithNullAuthor = null;
        prospectiveAuthor = null;
        authorVerificationLabel.setValue("");
        authorVerificationLayout.setVisible(false);
    }

    private void prepareAndVisualize() {
        update();
        int booksCounter = 0;
        int booksInRightClusterCounter = 0;
        for (Cluster cluster : clusters) {
            Author clusterOwner = cluster.getAuthor();
            booksCounter += cluster.getElements().size();
            for (Element element : cluster.getElements()) {
                if (element.getBook().getAuthor() == null) {
                    booksCounter--;
                    bookWithNullAuthor = element.getBook();
                    prospectiveAuthor = clusterOwner;
                    authorVerificationLabel.setValue("Предполагаемый автор книги " + bookWithNullAuthor + " " + prospectiveAuthor);
                    authorVerificationLayout.setVisible(true);
                } else if (element.getBook().getAuthor().equals(clusterOwner)) booksInRightClusterCounter++;
            }
        }
        double accuracy = (double) booksInRightClusterCounter * 100 / booksCounter;
        if (accuracy != 100) {
            accuracyLabel.setValue("Точность кластеризации: " + format.format(accuracy) + "%");
        }
        visualize();
    }

    private void visualize() {
        setVisible(true);
        TextCharacteristic xCharacteristic = xSelector.getValue();
        TextCharacteristic yCharacteristic = ySelector.getValue();
        TextCharacteristic zCharacteristic = zSelector.getValue();

        Configuration config;
        if (zCharacteristic == null) {
            config = createConfigFor2dChart(xCharacteristic, yCharacteristic);
        } else {
            config = createConfigFor3dChart(xCharacteristic, yCharacteristic, zCharacteristic);
        }
        chart.setConfiguration(config);
        chart.drawChart();
    }

    private Configuration createConfigFor2dChart(TextCharacteristic xCharacteristic, TextCharacteristic yCharacteristic) {
        XAxis xAxis = new XAxis();
        xAxis.setTitle(xCharacteristic.getDescription());
        YAxis yAxis = new YAxis();
        yAxis.setTitle(yCharacteristic.getDescription());
        Tooltip tooltip = new Tooltip();
        tooltip.setFormatter("'<b>' + this.series.name + '</b><br/>' + this.point.name + '<br/>X: ' + this.point.x + '<br/>Y: ' + this.point.y");

        Configuration config = new Configuration();
        ChartModel model = new ChartModel();
        model.setZoomType(ZoomType.XY);
        if (showCenters.getValue()) {
            model.setType(ChartType.LINE);
        } else {
            model.setType(ChartType.SCATTER);
        }
        config.setChart(model);
        config.setTitle((String) null);
        config.addxAxis(xAxis);
        config.addyAxis(yAxis);
        config.setTooltip(tooltip);

        int xOrdinal = xCharacteristic.ordinal();
        int yOrdinal = yCharacteristic.ordinal();

        for (Cluster cluster : clusters) {
            Author clusterOwner = cluster.getAuthor();
            DataSeries clusterDataSeries = new DataSeries();
            clusterDataSeries.setName(clusterOwner.toString());
            if (!showCenters.getValue()) {
                for (Element element : cluster.getElements()) {
                    double[] point = useNormBox.getValue() ? element.getNormPoint() : element.getPoint();
                    DataSeriesItem item = new DataSeriesItem();
                    item.setX(point[xOrdinal]);
                    item.setY(point[yOrdinal]);
                    item.setName(element.getBook().toString());
                    clusterDataSeries.add(item);
                    if (element.getBook().getAuthor() == null) {
                        bookWithNullAuthor = element.getBook();
                        prospectiveAuthor = clusterOwner;
                        authorVerificationLabel.setValue("Предполагаемый автор книги " + bookWithNullAuthor + " " + prospectiveAuthor);
                        authorVerificationLayout.setVisible(true);
                    }
                }
            } else {
                double[] centerPoint = useNormBox.getValue() ? cluster.getNormCenter() : cluster.getCenter();
                for (Element element : cluster.getElements()) {
                    double[] point = useNormBox.getValue() ? element.getNormPoint() : element.getPoint();
                    DataSeriesItem item = new DataSeriesItem();
                    item.setX(point[xOrdinal]);
                    item.setY(point[yOrdinal]);
                    item.setName(element.getBook().toString());
                    clusterDataSeries.add(item);
                    if (element.getBook().getAuthor() == null) {
                        bookWithNullAuthor = element.getBook();
                        prospectiveAuthor = clusterOwner;
                        authorVerificationLabel.setValue("Предполагаемый автор книги " + bookWithNullAuthor + " " + prospectiveAuthor);
                        authorVerificationLayout.setVisible(true);
                    }
                    DataSeriesItem center = new DataSeriesItem();
                    Marker marker = new Marker(true);
                    marker.setRadius(5);
                    marker.setLineWidth(2);
                    marker.setLineColor(SolidColor.BLACK);
                    center.setMarker(marker);
                    center.setX(centerPoint[xOrdinal]);
                    center.setY(centerPoint[yOrdinal]);
                    center.setName("Центроид кластера");
                    clusterDataSeries.add(center);
                }
            }
            config.addSeries(clusterDataSeries);
        }
        return config;
    }

    private Configuration createConfigFor3dChart(TextCharacteristic xCharacteristic, TextCharacteristic yCharacteristic, TextCharacteristic zCharacteristic) {
        XAxis xAxis = new XAxis();
        xAxis.setTitle(xCharacteristic.getDescription());
        YAxis yAxis = new YAxis();
        yAxis.setTitle(yCharacteristic.getDescription());
        ZAxis zAxis = new ZAxis();
        zAxis.setTitle(zCharacteristic.getDescription());
        Tooltip tooltip = new Tooltip();
        tooltip.setFormatter("'<b>' + this.series.name + '</b><br/>' + this.point.name + '<br/>X: ' + this.point.x + '<br/>Y: ' + this.point.y + '<br/>Z: ' + this.point.z");

        Options3d options3d = new Options3d();
        options3d.setEnabled(true);
        options3d.setAlpha(10);
        options3d.setBeta(30);
        options3d.setFitToPlot(true);

        Frame frame = new Frame();
        Bottom bottom = new Bottom();
        bottom.setSize(1);
        frame.setBottom(bottom);
        options3d.setFrame(frame);

        Configuration config = new Configuration();
        ChartModel model = new ChartModel();
        model.setZoomType(ZoomType.XY);
        model.setType(ChartType.SCATTER);
        model.setOptions3d(options3d);
        config.setChart(model);
        config.setTitle((String) null);
        config.addxAxis(xAxis);
        config.addyAxis(yAxis);
        config.addzAxis(zAxis);
        config.setTooltip(tooltip);

        int xOrdinal = xCharacteristic.ordinal();
        int yOrdinal = yCharacteristic.ordinal();
        int zOrdinal = zCharacteristic.ordinal();
        for (Cluster cluster : clusters) {
            Author clusterOwner = cluster.getAuthor();
            DataSeries clusterDataSeries = new DataSeries();
            clusterDataSeries.setName(clusterOwner.toString());
            for (Element element : cluster.getElements()) {
                double[] point = useNormBox.getValue() ? element.getNormPoint() : element.getPoint();
                DataSeriesItem3d item = new DataSeriesItem3d();
                item.setX(point[xOrdinal]);
                item.setY(point[yOrdinal]);
                item.setZ(point[zOrdinal]);
                item.setName(element.getBook().toString());
                clusterDataSeries.add(item);
                if (element.getBook().getAuthor() == null) {
                    bookWithNullAuthor = element.getBook();
                    prospectiveAuthor = clusterOwner;
                    authorVerificationLabel.setValue("Предполагаемый автор книги " + bookWithNullAuthor + " " + prospectiveAuthor);
                    authorVerificationLayout.setVisible(true);
                }
            }
            if (showCenters.getValue()) {
                double[] centerPoint = useNormBox.getValue() ? cluster.getNormCenter() : cluster.getCenter();
                DataSeriesItem3d center = new DataSeriesItem3d();
                Marker marker = new Marker(true);
                marker.setRadius(5);
                marker.setLineWidth(2);
                marker.setLineColor(SolidColor.BLACK);
                center.setMarker(marker);
                center.setX(centerPoint[xOrdinal]);
                center.setY(centerPoint[yOrdinal]);
                center.setZ(centerPoint[zOrdinal]);
                center.setName("Центроид кластера");
                clusterDataSeries.add(center);
            }
            config.addSeries(clusterDataSeries);
        }
        return config;
    }
}
