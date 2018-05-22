package ru.prestu.authident.domain.model.enums;

import net.sf.javaml.distance.*;

public enum Distances {

    ANGULAR_DISTANCE("Угловое расстояние", new AngularDistance()),
    CHEBYCHEV_DISTANCE("Расстояние Чебышева", new ChebychevDistance()),
    COSINE_DISTANCE("Косинусное расстояние", new CosineDistance()),
    MANHATTAN_DISTANCE("Манхэттенское расстояние", new ManhattanDistance()),
    EUCLIDEAN_DISTANCE("Евклидово расстояние", new EuclideanDistance()),
    MINKOWSKI_DISTANCE("Расстояние Минковского", new MinkowskiDistance()),
    RBF_KERNEL_DISTANCE("RBF", new RBFKernelDistance());

    private String description;
    private DistanceMeasure distance;

    Distances(String description, DistanceMeasure distance) {
        this.description = description;
        this.distance = distance;
    }

    public String getDescription() {
        return description;
    }

    public DistanceMeasure getDistance() {
        return distance;
    }
}
