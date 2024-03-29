package com.jacoboaks.wandermobile.game;

import android.util.Log;

import com.jacoboaks.wandermobile.game.gameitem.Entity;
import com.jacoboaks.wandermobile.game.gameitem.StaticTile;
import com.jacoboaks.wandermobile.game.gameitem.Tile;
import com.jacoboaks.wandermobile.graphics.Font;
import com.jacoboaks.wandermobile.graphics.ShaderProgram;
import com.jacoboaks.wandermobile.util.Coord;
import com.jacoboaks.wandermobile.util.Node;
import com.jacoboaks.wandermobile.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represent a single area composed of StaticTiles and Entities.
 */
public class Area {

    //Data
    private List<Entity> entities;
    private List<StaticTile> staticTiles;
    private String name;
    private String filename;
    private Coord spawn;

    /**
     * Constructs this Area with only a name.
     * @param name the name of the zone
     * @param filename the filename of the zone
     */
    public Area(String name, String filename) {
        this(name, filename, new ArrayList<StaticTile>(), new ArrayList<Entity>(), new Coord(0, 0));
    }

    /**
     * Constructs this Area with a name and a list of tiles and entities.
     * @param name the name of the zone
     * @param filename the filename of the zone
     * @param staticTiles the list of static tiles of the zone
     * @param entities the list of entities of the zone
     */
    public Area(String name, String filename, List<StaticTile> staticTiles, List<Entity> entities, Coord spawn) {
        this.name = name;
        this.filename = filename;
        this.staticTiles = staticTiles;
        this.entities = entities;
        this.spawn = spawn;
    }

    //Node Constructor
    public Area(Node node, Font font) {
        this.name = node.getChild("name").getValue();
        this.spawn = new Coord(node.getChild("spawn"));
        this.staticTiles = new ArrayList<>();
        for (Node st : node.getChild("statictiles").getChildren()) {
            this.staticTiles.add(StaticTile.nodeToStaticTile(st, font));
        }
        this.entities = new ArrayList<>();
        for (Node e : node.getChild("entities").getChildren()) {
            this.entities.add(Entity.nodeToEntity(e, font));
        }
    }

    //Update Method
    public void update(float dt) {
        for (StaticTile staticTile : this.staticTiles) staticTile.update(dt);
        for (Entity entity : this.entities) entity.update(dt);
    }

    //Render Method
    public void render(ShaderProgram shaderProgram) {
        for (StaticTile staticTile : this.staticTiles) staticTile.render(shaderProgram);
        for (Entity entity : this.entities) entity.render(shaderProgram);
    }

    /**
     * @return the StaticTile (or Entity if there is one) at the given grid position. Will return
     * null if there is none there
     */
    public Tile getTile(int gx, int gy) {

        //create tile and position coordinate
        Tile t = null;
        Coord pos;

        //search static tiles
        for (StaticTile st : this.staticTiles) {
            pos = st.getGridPosition();
            if ((int)pos.x == gx && (int)pos.y == gy) t = st;
        }

        //search entities
        for (Entity e : this.entities) {
            pos = e.getGridPosition();
            if ((int)pos.x == gx && (int)pos.y == gy) t = e;
        }

        //return tile
        return t;
    }

    //Accessor
    public String getName() { return this.name; }
    public String getFilename() { return this.filename; }
    public Coord getSpawn() { return this.spawn; }

    /**
     * Loads a brand new area from a given resource id
     * @param resourceID the resource id of the area to load
     * @param font the font to use for symbol tiles
     * @return the loaded area
     */
    public static Area loadArea(int resourceID, Font font) {

        //load data and parse load type
        Node areaData = Node.readNode(resourceID);
        String loadTypes = areaData.getChild("loadType").getValue();

        //create lists
        List<StaticTile> st = new ArrayList<>();
        List<Entity> e = new ArrayList<>();

        //populate area based on load type
        if (loadTypes.equals("row listing")) {

            //load key
            Node keyNode = areaData.getChild("key");
            Map<Character, Tile> key = new HashMap<>();
            for (Node child : keyNode.getChildren()) {

                //check for type of tile and add accordingly
                if (child.getName().equals("StaticTile")) {
                    key.put(child.getValue().charAt(0), StaticTile.nodeToStaticTile(child, font));
                } else if (child.getName().equals("Entity")) {
                    key.put(child.getValue().charAt(0), Entity.nodeToEntity(child, font));
                } else if (child.getName().equals("Tile")) {
                    key.put(child.getValue().charAt(0), Tile.nodeToTile(child, font));
                }
            }

            //load layout
            boolean moreRows = false;
            Node layout = areaData.getChild("layout");
            Node nextRow = layout.getChild("row 1");
            moreRows = nextRow != null;
            for (int y = 1; moreRows; y++) {

                //loop through row
                String row = nextRow.getValue();
                for (int x = 0; x < row.length(); x++) {

                    //ignore spaces
                    if (row.charAt(x) != ' ') {
                        Tile matchingTile = key.get(row.charAt(x));
                        if (matchingTile == null) { //check if null
                            if (Util.DEBUG) Log.i(Util.getLogTag("Area.java", "loadArea(int, Font)"),
                                    "invalid character in map (not defined in key): " + row.charAt(x));

                        //copy and add tile if not
                        } else {
                            matchingTile.setGridPosition(x, -(y - 1));
                            if (matchingTile instanceof StaticTile) {
                                st.add(new StaticTile((StaticTile)matchingTile));
                            } else if (matchingTile instanceof Entity) {
                                e.add(new Entity((Entity)matchingTile));
                            } else { //all normal tiles are treated as StaticTiles with a maneuverability of 0
                                st.add(new StaticTile(matchingTile, 0));
                            }
                        }
                    }
                }

                //check if there is another row
                nextRow = layout.getChild("row " + (y + 1));
                moreRows = nextRow != null;
            }

        } else if (loadTypes.equals("tile listing")) {

            //TODO: implement tile listing area loading

        //invalid load type given
        } else {
            throw Util.fatalError("Area.java", "loadArea(int)", "Unable to" +
                    "load area with loadType: " + loadTypes + ". Options are: 'row listing' or 'tile listing'");
        }

        //get spawn
        Coord spawn = new Coord();
        Node spawnNodeX = areaData.getChild("spawnx");
        Node spawnNodeY = areaData.getChild("spawny");
        if (spawnNodeX != null && spawnNodeY != null) {
            spawn = new Coord(Integer.parseInt(spawnNodeX.getValue()), Integer.parseInt(spawnNodeY.getValue()));
        }

        //create and return area
        return new Area(areaData.getChild("name").getValue(), areaData.getChild("filename").getValue(), st, e, spawn);
    }

    //Node Converter
    public Node toNode() {
        Node node = new Node("area");
        node.addChild("name", this.name);
        node.addChild("filename", this.filename);
        Node layout = new Node("statictiles");
        for (StaticTile t : this.staticTiles) {
            layout.addChild(t.toNode());
        }
        node.addChild(layout);
        Node entities = new Node("entities");
        for (Entity e : this.entities) {
            entities.addChild(e.toNode());
        }
        Node spawn = this.spawn.toNode();
        spawn.setName("spawn");
        node.addChild(spawn);
        return node;
    }
}
