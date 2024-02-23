package sample;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.*;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Group root = new Group();
        primaryStage.setTitle("Boids");

        Canvas canvas = new Canvas(800, 600);

        root.getChildren().add(canvas);
        Scene myScene = new Scene(root);
        Flock f = new Flock();
        f.addBoid(new Boid(40, 80));
        f.addBoid(new Boid(40, 40));
        f.addBoid(new Boid(80, 40));
        f.addBoid(new Boid(80, 80));

        myScene.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                System.out.println("mouse click detected! " + mouseEvent.getSource());
                System.out.println(mouseEvent.getX() + " " + mouseEvent.getY());
                f.setGoal(mouseEvent.getX(), mouseEvent.getY());
            }
        });

        final Timeline timeline = new Timeline();
        timeline.setCycleCount(2);
        timeline.setAutoReverse(true);
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(1)));
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                GraphicsContext gc = canvas.getGraphicsContext2D();
                f.run(gc, 800, 600);
            }
        };

        primaryStage.setScene(myScene);
        primaryStage.show();
        timer.start();
        timeline.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class Boid {
    static final Random r = new Random();
    static final int size = 3;

    static double maxSpeed, maxForce;

    Vec location, velocity, acceleration;

    Vec goal = new Vec(100, 100);

    private boolean included = true;
    private boolean isLeader = false; // New variable to identify the leader

    static final Color leaderColor = Color.RED; // You can set the color you want for the leader

    Boid(double x, double y) {
        acceleration = new Vec();
        velocity = new Vec(0, 0);
        location = new Vec(x, y);
        maxSpeed = 2.3;
        maxForce = 0.05;
    }

    

    void update() {
        velocity.add(acceleration);
        velocity.limit(maxSpeed);
        location.add(velocity);
        acceleration.mult(0);
    }

    void applyForce(Vec force) {
        acceleration.add(force);
    }

    Vec seek(Vec target) {
        Vec steer = Vec.sub(target, location);
        steer.normalize();
        steer.mult(maxSpeed);
        steer.sub(velocity);
        steer.limit(maxForce);
        return steer;
    }

    void flock(List<Boid> boids) {
        view(boids);
    
        Vec rule1 = separation(boids);
        Vec rule2 = alignment(boids, isLeader);
        Vec rule3 = cohesion(boids);
    
        rule1.mult(2.5);
        rule2.mult(0.5);
        rule3.mult(1.8);
    
        if (isLeader && velocity.mag() <= 0.1) {
            // If the leader is hovering, make other boids stop as well
            velocity = new Vec(0, 0);
        } else {
            applyForce(rule1);
            applyForce(rule2);
            applyForce(rule3);
        }
    }
    
    

    void view(List<Boid> boids) {
        double sightDistance = 120;

        for (Boid b : boids) {
            b.included = false;

            if (b == this)
                continue;

            double d = Vec.dist(location, b.location);
            if (d <= 0 || d > sightDistance)
                continue;

            b.included = true;
        }
    }

    Vec separation(List<Boid> boids) {
        double desiredSeparation = 45;

        Vec steer = new Vec(0, 0);
        int count = 0;
        for (Boid b : boids) {
            if (!b.included)
                continue;

            double d = Vec.dist(location, b.location);
            if ((d > 0) && (d < desiredSeparation)) {
                Vec diff = Vec.sub(location, b.location);
                diff.normalize();
                diff.div(d);
                steer.add(diff);
                count++;
            }
        }
        if (count > 0) {
            steer.div(count);
        }

        if (steer.mag() > 0) {
            steer.normalize();
            steer.mult(maxSpeed);
            steer.sub(velocity);
            steer.limit(maxForce);
            return steer;
        }
        return new Vec(0, 0);
    }

    Vec alignment(List<Boid> boids, boolean isLeader) {
        double preferredDist = 70;
    
        Vec steer = new Vec(0, 0);
        int count = 0;
    
        for (Boid b : boids) {
            if (!b.included || (isLeader && !b.isLeader))
                continue;
    
            double d = Vec.dist(location, b.location);
            if ((d >= 0) && (d < preferredDist)) {
                steer.add(b.velocity);  // Use b.velocity for alignment
                count++;
            }
        }
    
        if (count > 0) {
            steer.div(count);
            steer.normalize();
            steer.mult(maxSpeed);
            steer.sub(velocity);
            steer.limit(maxForce);
        }
        return steer;
    }
    
    
    

    Vec cohesion(List<Boid> boids) {
    double preferredDist = 70;

    Vec target = new Vec(0, 0);
    int count = 0;

    for (Boid b : boids) {
        if (!b.included)
            continue;

        double d = Vec.dist(location, b.location);
        if (d > preferredDist) {
            target.add(b.location);
            count++;
        }
    }
    if (count > 0) {
        target.div(count);
        // Instead of seeking the target directly, calculate the offset
        Vec offset = new Vec(target.x - location.x, target.y - location.y);
        offset.normalize();
        offset.mult(maxSpeed);
        Vec desired = new Vec(0, 0);  // Target position is the current position (hovering)
        desired.add(offset);
        Vec steer = Vec.sub(desired, velocity);
        steer.limit(maxForce);
        return steer;
    }
    return target;
}

    void draw(GraphicsContext gc, int w, int h) {
        if (isLeader) {
            gc.setFill(leaderColor);
        } else {
            gc.setFill(Color.BLUE);
        }

        gc.fillOval(location.x, location.y, 15, 15);
    }

    void run(GraphicsContext gc, List<Boid> boids, int w, int h, boolean head) {
        if (head) {
            if (Vec.dist(location, goal) <= Boid.maxSpeed / 2) {
                velocity = new Vec(0, 0);
            } else {
                Vec steer = Vec.sub(goal, location);
                steer.normalize();
                steer.mult(Boid.maxSpeed / 2);
                velocity = steer;
                location.add(velocity);
            }
            isLeader = true;
        } else {
            flock(boids);
            update();
            isLeader = false;
        }
        draw(gc, w, h);
    }

    void setGoal(double x, double y) {
        goal.x = x;
        goal.y = y;
    }
}

class Flock {
    List<Boid> boids;
    Vec goal = new Vec(100, 100);

    void run(GraphicsContext gc, int w, int h) {
        gc.clearRect(0, 0, w, h);
        double MIN = 10000000;
        int index = -1;
        for (int i = 0; i < boids.size(); i++) {
            double temp = Vec.dist(boids.get(i).location, goal);
            if (temp < MIN) {
                MIN = temp;
                index = i;
            }
        }
        for (int i = 0; i < boids.size(); i++) {
            if (i != index) {
                boids.get(i).run(gc, boids, w, h, false);
            } else {
                boids.get(i).setGoal(goal.x, goal.y);
                boids.get(i).run(gc, boids, w, h, true);
            }
        }
    }

    Flock() {
        boids = new ArrayList<>();
    }

    void addBoid(Boid b) {
        boids.add(b);
    }

    void setGoal(double x, double y) {
        goal.x = x;
        goal.y = y;
    }
}

class Vec {
    double x, y;

    Vec() {
    }

    Vec(double x, double y) {
        this.x = x;
        this.y = y;
    }

    void add(Vec v) {
        x += v.x;
        y += v.y;
    }

    void sub(Vec v) {
        x -= v.x;
        y -= v.y;
    }

    void div(double val) {
        x /= val;
        y /= val;
    }

    void mult(double val) {
        x *= val;
        y *= val;
    }

    double mag() {
        return sqrt(pow(x, 2) + pow(y, 2));
    }

    double dot(Vec v) {
        return x * v.x + y * v.y;
    }

    void normalize() {
        double mag = mag();
        if (mag != 0) {
            x /= mag;
            y /= mag;
        }
    }

    void limit(double lim) {
        double mag = mag();
        if (mag != 0 && mag > lim) {
            x *= lim / mag;
            y *= lim / mag;
        }
    }

    double heading() {
        return atan2(y, x);
    }

    static Vec sub(Vec v, Vec v2) {
        return new Vec(v.x - v2.x, v.y - v2.y);
    }

    static double dist(Vec v, Vec v2) {
        return sqrt(pow(v.x - v2.x, 2) + pow(v.y - v2.y, 2));
    }

    static double angleBetween(Vec v, Vec v2) {
        return acos(v.dot(v2) / (v.mag() * v2.mag()));
    }
}
