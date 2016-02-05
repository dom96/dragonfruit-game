package com.dragonfruitstudios.brokenbonez;

import android.graphics.Color;
import android.graphics.PointF;
import android.util.Log;

import com.dragonfruitstudios.brokenbonez.BoundingShapes.Circle;
import com.dragonfruitstudios.brokenbonez.BoundingShapes.Rect;

public class Bike implements Drawable {

    class Wheel {
        VectorF pos;
        VectorF velocity; // Measured in px/s
        VectorF acceleration; // Measured in px/s²

        float rotation; // Measured in radians
        float angularVelocity; // Radians per second
        float angularAcceleration; // Radians per second per second

        Circle boundingCircle;

        boolean wasInAir;
        float torque;

        Wheel() {
            pos = new VectorF(0, 0);
            velocity = new VectorF(0, 0);
            acceleration = new VectorF(0, 0);

            //angularAcceleration = 3;

            rotation = 0;

            boundingCircle = new Circle(pos, 30);

            wasInAir = true;
        }

        public void update(float lastUpdate, Level currentLevel) {
            // Acceleration due to gravity.
            acceleration.setY(10*9.81f);
            // Deceleration due to air resistance. It acts in the opposite direction to the
            // velocity.
            VectorF airResistance = new VectorF(-(0.1f * velocity.getX()), -(0.1f * velocity.getY()));
            // TODO: Friction
            // Calculate the resultant acceleration.
            VectorF resultantAccel = new VectorF(acceleration);
            resultantAccel.add(airResistance);

            // Update the wheels' velocity based on acceleration.
            float updateFactor = lastUpdate / 1000;
            velocity.multAdd(resultantAccel, updateFactor);

            // Change position based on velocity.
            pos.multAdd(velocity, updateFactor);

            // Change angular velocity based on angular acceleration.
            angularVelocity += angularAcceleration * updateFactor;
            // Change rotation based on angular velocity.
            rotation += angularVelocity * updateFactor;

            // Check if the wheel intersects with the current level's ground.
            if (currentLevel.intersectsGround(boundingCircle)) {
                // We need to move the wheel, so that it just touches what it collided with.
                Rect nearest = currentLevel.getNearestSolid(boundingCircle.getCenter());
                pos.setY(nearest.getTop() - boundingCircle.getRadius());
                velocity.setY(0);

                if (wasInAir) {
                    wasInAir = false;
                    // Velocity = ω * radius
                    VectorF newVelocity = new VectorF(angularVelocity * boundingCircle.getRadius(), 0);
                    velocity.add(newVelocity);
                }
                else {
                    // TODO: Need to consider the angle of the tangent of the point that the
                    // TODO: wheel is touching.
                    float newAngularVelocity = velocity.magnitude() / boundingCircle.getRadius();
                    if (velocity.getX() < 0) {
                        // TODO: This probably won't work on slopes?
                        newAngularVelocity = -newAngularVelocity;
                    }
                    angularVelocity = newAngularVelocity;
                }

                // Set acceleration based on torque.
                // TODO: This may need to be adjusted, especially when slopes come into play.
                acceleration.setX(torque);

            }
            else {
                wasInAir = true;
            }
        }

        /**
         * Sets the position of this wheel to the specified x and y coordinates.
         */
        public void setPos(float x, float y) {
            pos.set(x, y);

            boundingCircle.setCenter(x, y);
        }

        /**
         * Sets the acceleration of this wheel to the specified vector.
         */
        public void setAcceleration(float amount) {
            torque = amount;
        }

        public void draw(GameView view) {
            view.drawCircle(pos.x, pos.y, boundingCircle.getRadius(), Color.parseColor("#6d6d6d"));
            // A rotation of `0` will show the line horizontally to the right.
            VectorF rotatedFinish = new VectorF(boundingCircle.getRadius(), 0);
            rotatedFinish.rotate(rotation);
            view.drawLine(pos, pos.added(rotatedFinish), Color.parseColor("#ffe961"));

            boundingCircle.draw(view);
        }

    }

    Level currentLevel;

    Wheel leftWheel;
    Wheel rightWheel;

    VectorF startPos;

    public Bike(Level currentLevel) {
        this.currentLevel = currentLevel;

        this.leftWheel = new Wheel();
        this.rightWheel = new Wheel();
        // TODO: Just a small test to see if giving the wheel an initial x-axis velocity works.
        this.rightWheel.velocity.set(20, 0);
        //this.rightWheel.velocity.set(40, 0);
        this.leftWheel.angularVelocity = 2;
    }

    public void draw(GameView gameView) {
        leftWheel.draw(gameView);
        rightWheel.draw(gameView);

        // Draw text on screen with some debug info
        String debugInfo = String.format("Bike[LWV: (%.1f, %.1f), LWA: (%.1f, %.1f), LWP: (%.1f, %.1f)]",
                leftWheel.velocity.getX(), leftWheel.velocity.getY(), leftWheel.acceleration.getX(),
                leftWheel.acceleration.getY(), leftWheel.pos.getX(), leftWheel.pos.getY());
        gameView.drawText(debugInfo, 20, 60, Color.WHITE);
    }

    public void updateSize(int width, int height) {
        updateStartPos(currentLevel.getStartPoint());
    }

    private void updateStartPos(VectorF startPos) {
        // This is necessary because when the Bike is initialised the GameView may not have
        // initialised properly yet, and so its height has not been calculated yet. This causes
        // the start position to be incorrect.
        this.startPos = startPos;
        Log.d("Bike", "Updated start pos: " + startPos.toString());
        // Calculate positions of the left and right wheels.
        leftWheel.setPos(startPos.x + 25, startPos.y);
        rightWheel.setPos(startPos.x + 200, startPos.y);
    }

    public void update(float lastUpdate) {
        leftWheel.update(lastUpdate, currentLevel);
        rightWheel.update(lastUpdate, currentLevel);
    }

    /**
     * Sets the acceleration of the bike. The strength should be a value between 0 and 1. 0 means
     * no acceleration and 1 means maximum acceleration.
     * @param strength
     */
    public void setAcceleration(float strength) {
        // Left wheel is controlled by the engine, so it gets the acceleration.
        // TODO: Maximum speed of bike is currently hardcoded. Make this customisable, perhaps
        // TODO: allow different bikes with differing acceleration characteristics?
        leftWheel.setAcceleration(500*strength);

        Log.d("Bike/Acc", "Torque is now " + 500*strength);
    }
}
