package com.jacoboaks.wandermobile.game;

import android.opengl.GLES20;
import android.os.Bundle;

import com.jacoboaks.wandermobile.R;
import com.jacoboaks.wandermobile.game.gameitem.Entity;
import com.jacoboaks.wandermobile.game.gameitem.GameItem;
import com.jacoboaks.wandermobile.game.gameitem.StaticTile;
import com.jacoboaks.wandermobile.game.gameitem.Tile;
import com.jacoboaks.wandermobile.graphics.FollowingCamera;
import com.jacoboaks.wandermobile.graphics.ShaderProgram;
import com.jacoboaks.wandermobile.util.Color;
import com.jacoboaks.wandermobile.util.Node;
import com.jacoboaks.wandermobile.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * @purpose is to hold many GameItems to be rendered under the HUD
 */
public class World {

    //Surface Data
    float aspectRatio;
    boolean aspectRatioAction; //true (ratio < 1) -> multiply y by aspect ratio; false (ratio >= 1) -> divide x by aspect ratio

    //Data
    private Entity player;
    private Area area;
    private ShaderProgram shaderProgram;
    private FollowingCamera camera;

    /**
     * @purpose is to construct this World
     * @param aspectRatio the aspect ratio of the surface
     * @param aspectRatioAction the aspect ration action given the current aspect ratio (explained in data)
     */
    public World(float aspectRatio, boolean aspectRatioAction, Area area, Entity player) {

        //initialize graphics and shader program
        this.initGraphics(aspectRatio, aspectRatioAction, player);
        this.initShaderProgram();

        //set area and player references
        this.area = area;
        this.player = player;
    }

    /**
     * @purpose is to instate any saved world data
     * @param data
     */
    public void instateLoadedData(Bundle data) {
        this.camera.setX(Float.parseFloat(data.getString("worldlogic_camerax")));;
        this.camera.setY(Float.parseFloat(data.getString("worldlogic_cameray")));;
        this.camera.setZoom(Float.parseFloat(data.getString("worldlogic_camerazoom")));
    }

    /**
     * @purpose is to initialize the shader program
     */
    private void initShaderProgram() {

        //create shader program, load shaders, and link them.
        this.shaderProgram = new ShaderProgram();
        this.shaderProgram.loadShader(R.raw.worldvshader, GLES20.GL_VERTEX_SHADER);
        this.shaderProgram.loadShader(R.raw.worldfshader, GLES20.GL_FRAGMENT_SHADER);
        this.shaderProgram.link();

        //register shader program uniforms
        this.shaderProgram.registerUniform("aspectRatio");
        this.shaderProgram.registerUniform("aspectRatioAction");
        this.shaderProgram.registerUniform("x");
        this.shaderProgram.registerUniform("y");
        this.shaderProgram.registerUniform("camx");
        this.shaderProgram.registerUniform("camy");
        this.shaderProgram.registerUniform("camzoom");
        this.shaderProgram.registerUniform("color");
        this.shaderProgram.registerUniform("textureSampler");
        this.shaderProgram.registerUniform("colorOverride");
        this.shaderProgram.registerUniform("isTextured");
    }

    /**
     * @purpose is to initialize the graphical components of this World
     */
    private void initGraphics(float aspectRatio, boolean aspectRatioAction, GameItem cameraFollowee) {
        this.aspectRatio = aspectRatio;
        this.aspectRatioAction = aspectRatioAction;
        this.camera = new FollowingCamera(0.2f, cameraFollowee, false);
    }

    //Update Method
    public void update(float dt) {
        this.area.update(dt);
        this.player.update(dt);
        this.camera.update(dt);
    }

    //Render Method
    public void render() {

        //bind shader program
        this.shaderProgram.bind();

        //update aspect ratio and aspect ratio action
        GLES20.glUniform1fv(this.shaderProgram.getUniformIndex("aspectRatio"), 1,
                new float[] { aspectRatio }, 0);
        GLES20.glUniform1iv(this.shaderProgram.getUniformIndex("aspectRatioAction"), 1,
                new int[] { this.aspectRatioAction ? 1 : 0 }, 0);

        //update camera properties
        GLES20.glUniform1fv(this.shaderProgram.getUniformIndex("camx"), 1,
                new float[] { this.camera.getX() }, 0);
        GLES20.glUniform1fv(this.shaderProgram.getUniformIndex("camy"), 1,
                new float[] { this.camera.getY() }, 0);
        GLES20.glUniform1fv(this.shaderProgram.getUniformIndex("camzoom"), 1,
                new float[] { this.camera.getZoom() }, 0);

        //render area and player
        this.area.render(this.shaderProgram);
        this.player.render(this.shaderProgram);

        //unbind shader program
        this.shaderProgram.unbind();
    }

    //Accessors
    public Entity getPlayer() { return this.player; }
    public FollowingCamera getCamera() { return this.camera; }

    /**
     * @purpose is to compile all important data into a node to be put into a bundle before
     * terminating the logic - this will be reloaded in the next instance after the interruption has
     * ceased
     * @return the node containing all of the compiled important information
     */
    public void requestData(Node data) {
        data.addChild(new Node("camerax", Float.toString(this.camera.getX())));
        data.addChild(new Node("cameray", Float.toString(this.camera.getY())));
        data.addChild(new Node("camerazoom", Float.toString(this.camera.getZoom())));
    }
}
