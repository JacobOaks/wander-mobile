package com.jacoboaks.wandermobile.game.gameitem;

import com.jacoboaks.wandermobile.graphics.Font;
import com.jacoboaks.wandermobile.graphics.Material;
import com.jacoboaks.wandermobile.graphics.Model;
import com.jacoboaks.wandermobile.graphics.Texture;
import com.jacoboaks.wandermobile.graphics.Transformation;
import com.jacoboaks.wandermobile.util.Color;
import com.jacoboaks.wandermobile.util.Coord;
import com.jacoboaks.wandermobile.util.Node;

/**
 * Represents a specific type of GameItem which is a tile existing in the grid world.
 * Coordinates should be manipulated using grid position rather than direct position as it
 * represents the place in the grid separate of the model sizes.
 */
public class Tile extends GameItem {

    //Static Data
    private static final float STD_MOVE_ANIMATION_SPEED = 0.025f;
    private static final float IMPENDING_MOVEMENT_TIME = 180f;

    //Data
    private String name; //name of the tile
    private float impendingMovementTime; //time until an impending move is undergone
    private float tgtX = 0, tgtY = 0; //target x and y (of movement animation) - in world coordinates
    private int igx, igy; //delta impending grid position
    private char symbol; //symbol of the tile
    private boolean isMoving = false; //whether or not the tile is currently undergoing a moving animation
    private boolean symbolTile; //whether or not this tile is a symbol tile (true) or texture (false)

    /**
     * Constructs this Tile using a colored character.
     * @param name the name of this tile
     * @param font the font to draw the character from
     * @param symbol the character to represent this tile
     * @param color the color of the character
     * @param gx the grid x coordinate
     * @param gy the grid y coordinate
     */
    public Tile(String name, Font font, char symbol, Color color, int gx, int gy) {
        super(new Model(Model.STD_SQUARE_MODEL_COORDS(), font.getCharacterTextureCoordinates(symbol, false),
                Model.STD_SQUARE_DRAW_ORDER(), new Material(font.getFontSheet(), color, true)),
                (float)gx * Model.STD_SQUARE_SIZE, (float)gy * Model.STD_SQUARE_SIZE);
        this.symbolTile = true;
        this.symbol = symbol;
        this.name = name;
    }

    /**
     * Construct this Tile using a texture.
     * @param name the name of this tile
     * @param texture the texture to use
     * @param gx the grid x coordinate
     * @param gy the grid y coordinate
     */
    public Tile(String name, Texture texture, int gx, int gy) {
        super(new Model(Model.STD_SQUARE_MODEL_COORDS(), Model.STD_SQUARE_TEX_COORDS(), Model.STD_SQUARE_DRAW_ORDER(),
                new Material(texture)), (float)gx * Model.STD_SQUARE_SIZE, (float)gy * Model.STD_SQUARE_SIZE);
        this.symbolTile = false;
        this.symbol = 0;
        this.name = name;
    }

    /**
     * Contructs this Tile by copying another Tile.
     * @param other the Tile to copy from
     */
    public Tile(Tile other) {
        super(other);
        this.name = other.name;
        this.igx = other.igx;
        this.igy = other.igy;
        this.impendingMovementTime = other.impendingMovementTime;
        this.tgtX = other.tgtX;
        this.tgtY = other.tgtY;
        this.symbolTile = other.symbolTile;
        this.isMoving = other.isMoving;
        this.symbol = other.symbol;
    }

    /**
     * Constructs this Tile using a given Node. This constructor assumes that this Tile is a symbol
     * tile.
     * @param data the node to use when constructing this Tile
     * @param font the font to draw the character from
     */
    protected Tile(Node data, Font font) {
        this(data.getChild("name").getValue(), font, data.getChild("symbol").getValue().charAt(0),
                new Color(data.getChild("color")), Integer.parseInt(data.getChild("gridx").getValue()),
                Integer.parseInt(data.getChild("gridy").getValue()));
    }

    /**
     * Constructs this Tile using a given Node. This constructor assume that this Tile is not a
     * symbol tile.
     * @param data the node to use when constructing this Tile
     */
    protected Tile(Node data) {
        this(data.getChild("name").getValue(), new Texture(Integer.parseInt(data.getChild("texture").getValue())),
                Integer.parseInt(data.getChild("gridx").getValue()), Integer.parseInt(data.getChild("gridy").getValue()));
    }

    /**
     * Creates a Tile with the given data. Will make the differentiation between symbol tiles and
     * non-symbol tiles so that one doesn't have to choose which constructor to use.
     * @param data the node to use when constructing this Tile
     * @param font the font to draw the character from
     * @return the constructed Tile
     */
    public static Tile nodeToTile(Node data, Font font) {
        if (Boolean.parseBoolean(data.getChild("symbolTile").getValue())) {
            return new Tile(data, font);
        } else {
            return new Tile(data);
        }
    }

    //Update Method
    @Override
    public void update(float dt) {
        super.update(dt);

        //impending movement
        if (this.impendingMovementTime > 0) {
            this.impendingMovementTime -= dt;
            if (this.impendingMovementTime <= 0) {
                this.moveGridPos(this.igx, this.igy);
                this.resetImpendingMovement();
            }
        }

        //if this tile is undergoing the movement animation
        if (this.isMoving) {

            //check if destination reached
            boolean destinationReached = false;
            if (this.vx > 0 && this.x >= this.tgtX) { //moving right
                destinationReached = true;
            } else if (this.vx < 0 && this.x <= this.tgtX) { //moving left
                destinationReached = true;
            } else if (this.vy > 0 && this.y >= this.tgtY) { //moving down
                destinationReached = true;
            } else if (this.vy < 0 && this.y <= this.tgtY) { //moving up
                destinationReached = true;
            }

            //stop movement if so
            if (destinationReached) {
                this.vx = this.vy = 0;
                this.x = this.tgtX;
                this.y = this.tgtY;
                this.isMoving = false;
            }
        }
    }

    /**
     * @return the width of the Tile
     */
    @Override
    public float getWidth() {
        return Model.STD_SQUARE_SIZE;
    }

    /**
     * @return the height of the Tile
     */
    @Override
    public float getHeight() {
        return Model.STD_SQUARE_SIZE;
    }

    /**
     * Begins a movement animation to the given world position.
     * @param dgx the amount of grid x to move by (delta x)
     * @param dgy the amount of grid y to move by
     */
    public void moveGridPos(int dgx, int dgy) {

        //set target x and target y
        Coord dpos = new Coord(dgx, dgy);
        Transformation.gridToWorld(dpos);
        this.tgtX = this.x + dpos.x;
        this.tgtY = this.y + dpos.y;

        //set velocity
        this.vx = Tile.STD_MOVE_ANIMATION_SPEED * (float)dgx;
        this.vy = Tile.STD_MOVE_ANIMATION_SPEED * (float)dgy;

        //set moving flag to true
        this.isMoving = true;
    }

    /**
     * Registers an impending movement.
     * @param dgx the delta grid x for the impending movement
     * @param dgy the delta grid y for the impending movement
     */
    public void impendingMove(int dgx, int dgy) {
        this.igx = dgx;
        this.igy = dgy;
        this.impendingMovementTime = Tile.IMPENDING_MOVEMENT_TIME;
    }

    /**
     * Resets any impending movement for this game item.
     */
    public void resetImpendingMovement() {
        this.igx = this.igy = 0;
        this.impendingMovementTime = 0f;
    }

    /**
     * Stops any movement or impending movement.
     */
    @Override
    public void stopMoving() {
        if (this.isMoving) return; //ignore if already undergoing a movement
        super.stopMoving();
        this.resetImpendingMovement();
    }

    /**
     * Sets the grid position of this Tile with the given coordinates
     * @param gx the grid x
     * @param gy the grid y
     */
    public void setGridPosition(int gx, int gy) {
        Coord position = new Coord(gx, gy);
        Transformation.gridToWorld(position);
        this.x = position.x;
        this.y = position.y;
    }

    //Accessors
    public boolean isMoving() { return this.isMoving; }
    public boolean hasImpendingMovement() { return this.impendingMovementTime > 0.01f; }
    public char getSymbol() { return this.symbol; }
    public String getName() { return this.name; }
    public Coord getGridPosition() {
        Coord position = new Coord(this.x, this.y);
        Transformation.worldToGrid(position);
        return position;
    }

    //Node Converter
    public Node toNode() {
        Node node = new Node(Character.toString(this.symbol), "Tile");
        node.addChild(new Node("name", this.name));
        node.addChild(new Node("symbolTile", Boolean.toString(this.symbolTile)));
        if (this.symbolTile) {
            node.addChild(new Node("symbol", Character.toString(this.symbol)));
            node.addChild(this.model.getMaterial().getColor().toNode());
        }
        else node.addChild(new Node("texture", Integer.toString(this.model.getMaterial().getTexture().getResourceID())));
        Coord gridPosition = this.getGridPosition();
        node.addChild(new Node("gridx", Integer.toString((int)gridPosition.x)));
        node.addChild(new Node("gridy", Integer.toString((int)gridPosition.y)));
        return node;
    }
}