package com.jacoboaks.wandermobile.game.gameitem;

import android.opengl.GLES20;

import com.jacoboaks.wandermobile.graphics.Model;
import com.jacoboaks.wandermobile.graphics.ShaderProgram;
import com.jacoboaks.wandermobile.util.Bounds;
import com.jacoboaks.wandermobile.util.Coord;

/**
 * Represents a single game item with a model, position, velocity, and visibility.
 */
public class GameItem {

    //Data
    protected Model model; //model
    protected float x, y, vx, vy; //position and velocity in aspected or world space
    protected boolean visible; //visibility

    /**
     * Constructs this GameItem with the given model, world x, and world y position.
     * @param model the model to represent the GameItem
     * @param x the world x position
     * @param y the world y position
     */
    public GameItem(Model model, float x, float y) {
        this.model = model;
        this.x = x;
        this.y = y;
        this.vx = this.vy = 0f;
        this.visible = true;
    }

    /**
     * Constructs this GameItem by copying another.
     * @param other the GameItem to copy from
     */
    public GameItem(GameItem other) {
        this.model = other.model;
        this.x = other.x;
        this.y = other.y;
        this.vx = other.vx;
        this.vy = other.vy;
        this.visible = other.visible;
    }

    //Update Method
    public void update(float dt) {

        //update position with velocity
        this.x += this.vx;
        this.y += this.vy;
    }

    //Draw Method
    public void render(ShaderProgram shaderProgram) {

        //return if invisible
        if (!this.visible) return;

        //set x and y uniforms
        GLES20.glUniform1fv(shaderProgram.getUniformIndex("x"), 1,
                new float[] { x }, 0);
        GLES20.glUniform1fv(shaderProgram.getUniformIndex("y"), 1,
                new float[] { y }, 0);

        //draw model
        this.model.render(shaderProgram);
    }

    //Accessors
    public boolean isVisible() { return this.visible; }
    public float getX() { return this.x; }
    public float getY() { return this.y; }
    public Model getModel() { return this.model; }
    public float getWidth() { return this.model.getWidth(); };
    public float getHeight() { return this.model.getHeight(); };
    public Bounds getBounds() {
        return new Bounds(new Coord(this.x, this.y),
            this.getWidth(), this.getHeight());
    }

    //Mutators
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setVisibility(boolean visibility) { this.visible = visibility; }
    public void moveX(float dx) { this.x += dx; }
    public void moveY(float dy) { this.y += dy; }
    public void setVx(float vx) { this.vx = vx; }
    public void setVy(float vy) { this.vy = vy; }
    public void stopMoving() {
        this.vx = this.vy = 0;
    }
    public void scale(float factor) { this.model.scale(factor); }
}
