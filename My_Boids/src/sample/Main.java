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
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.*;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Group root = new Group();
        primaryStage.setTitle("Boids");



        Canvas canvas = new Canvas(800, 600);


        root.getChildren().add(canvas);
        Scene myScene = new Scene(root);
        Flock f = new Flock();
        myScene.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                System.out.println("mouse click detected! " + mouseEvent.getSource());
                System.out.println(mouseEvent.getX()+" "+mouseEvent.getY());
                f.addBoid(new Boid(mouseEvent.getX(), mouseEvent.getY()));
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
class Obstacle{
    public Obstacle(){

    }
}
class Boid {
    static final Random r = new Random();
    static final Vec migrate = new Vec(0.1, 0.1);
    static final int size = 3;

    final double maxForce, maxSpeed;

    Vec location, velocity, acceleration;
    private boolean included = true;

    Boid(double x, double y) {
        acceleration = new Vec();
        velocity = new Vec(r.nextInt(5) - 2.5, r.nextInt(5) - 2.5);
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

    void flock(java.util.List<Boid> boids) {
        view(boids);

        Vec rule1 = separation(boids);
        Vec rule2 = alignment(boids);
        Vec rule3 = cohesion(boids);
        bounce(boids);

        rule1.mult(2.5);
        rule2.mult(1.5);
        rule3.mult(1.3);
//        bounce.mult(1.5);

        applyForce(rule1);
        applyForce(rule2);
        applyForce(rule3);
//        applyForce(bounce);
//        applyForce(migrate);
    }

    void view(List<Boid> boids) {
        double sightDistance = 120;
        double peripheryAngle = PI * 0.85;

        for (Boid b : boids) {
            b.included = false;

            if (b == this)
                continue;

            double d = Vec.dist(location, b.location);
            if (d <= 0 || d > sightDistance)
                continue;

            Vec lineOfSight = Vec.sub(b.location, location);

            double angle = Vec.angleBetween(lineOfSight, velocity);
            if (angle < peripheryAngle)
                b.included = true;
        }
    }
    Vec separation(java.util.List<Boid> boids) {
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
                diff.div(d);        // weight by distance
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

    Vec alignment(java.util.List<Boid> boids) {
        double preferredDist = 70;

        Vec steer = new Vec(0, 0);
        int count = 0;

        for (Boid b : boids) {
            if (!b.included)
                continue;

            double d = Vec.dist(location, b.location);
            if ((d >= 0) && (d < preferredDist)) {
                steer.add(b.velocity);
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

    Vec cohesion(java.util.List<Boid> boids) {
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
            return seek(target);
        }
        return target;
    }
    void bounce(java.util.List<Boid> boids){
        double ForceX=0, ForceY=0;
        if(location.x<=30 && velocity.x<0){
//            ForceX = 10;
            ForceX = Math.abs(velocity.x);
        }
        else if(location.x>=755 && velocity.x>0){
//            ForceX=-10;
            ForceX = -Math.abs(velocity.x);
        }
        if(location.y<=30 && velocity.y<0){
//            ForceY = 10;
            ForceY = Math.abs(velocity.y);
        }
        else if(location.y>=555 && velocity.y>0){
//            ForceY = -10;
            ForceY = -Math.abs(velocity.y);
        }

        Vec v = new Vec(ForceX, ForceY);
        v.mult(1.2);
        this.applyForce(v);
        for (Boid b : boids) {
            if (!b.included)
                continue;

            b.applyForce(v);
        }
    }
    void draw(GraphicsContext gc, int w, int h) {

        gc.fillOval(location.x, location.y, 15, 15);
    }

    void run(GraphicsContext gc, List<Boid> boids, int w, int h) {

        flock(boids);
        update();
        draw(gc, w, h);
    }
}

class Flock {
    List<Boid> boids;

    void run(GraphicsContext gc, int w, int h) {
        gc.clearRect(0,0, w, h);
        for (Boid b : boids) {
            b.run(gc, boids, w, h);
        }
    }

    Flock() {
        boids = new ArrayList<>();
    }

    void addBoid(Boid b) {
        boids.add(b);
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