package com.jacoboaks.wandermobile.game.gameitem;

import com.jacoboaks.wandermobile.graphics.Font;
import com.jacoboaks.wandermobile.graphics.Texture;
import com.jacoboaks.wandermobile.util.Color;

public class Entity extends Tile {

    //Data
    int health;
    int maxHealth;
    int level;
    boolean isDead;

    /**
     * @purpose is to construct this StaticTile using a colored character
     * @param name the name of the static tile
     * @param font the font to draw the character from
     * @param symbol the character to represent this tile
     * @param color the color of the character
     * @param gx the grid x coordinate
     * @param gy the grid y coordinate
     */
    public Entity(String name, Font font, char symbol, Color color, int gx, int gy) {
        super(name, font, symbol, color, gx, gy);
        this.health = this.maxHealth = this.level = 1;
    }

    /**
     * @purpose is to construct this StaticTile using a texture
     * @param name the name of the static tile
     * @param texture the texture to use
     * @param gx the grid x coordinate
     * @param gy the grid y coordinate
     */
    public Entity(String name, Texture texture, int gx, int gy) {
        super(name, texture, gx, gy);
        this.health = this.maxHealth = this.level = 1;
    }

    /**
     * @purpose is to set the basic entity info
     * @param health the health this entity has
     * @param maxHealth the maximum health this entity can have
     * @param level the level of this entity
     */
    public void setEntityInto(int health, int maxHealth, int level) {
        this.health = health;
        this.maxHealth = maxHealth;
        this.level = level;
    }

    /**
     * @purpose is to change the health of this entity
     * @param health the value to change the health to - will not heal over maximum health
     */
    public void setHealth(int health) {
        if (health > this.maxHealth) this.health = this.maxHealth;
        else this.health = health;
    }

    /**
     * @purpose is to deal a certain amount of damage to this entity
     * @param damage
     */
    public void dealDamage(int damage) { this.health -= damage; }

    //Accessors
    public boolean isDead() { return this.health <= 0; }
    public int getHealth() { return this.health; }
    public int getMaxHealth() { return this.maxHealth; }
    public int getLevel() { return this.level; }
}