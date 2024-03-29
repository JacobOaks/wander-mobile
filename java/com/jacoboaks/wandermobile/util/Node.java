package com.jacoboaks.wandermobile.util;

import android.os.Bundle;

import com.jacoboaks.wandermobile.MainActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Serves as a medium of data transfer using a tree-like data structure.
 */
public class Node {

    //Static Data
    private static char DIVIDER_CHAR = ':';
    private static char INDENT_CHAR = '\t';

    //Data
    private List<Node> children;
    private String name;
    private String value;

    /**
     * Constructs this Node by giving it all of its properties upfront.
     * @param name the name of the node
     * @param value the value of the node
     * @param children the list of the node's children
     */
    public Node(String name, String value, List<Node> children) {
        this.name = name;
        this.value = value;
        this.children = children;
    }

    /**
     * Constructs this Node by giving it a name, its data, and a single child.
     * @param name the name of the node
     * @param value the value of the node
     * @param child the single child of the node
     */
    public Node(String name, String value, Node child) {
        this(name, value, new ArrayList<Node>());
        this.children.add(child);
    }

    /**
     * Constructs this Node without giving it any children.
     * @param name the name of the node
     * @param value the value of the node
     */
    public Node(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Constructs this Node by solely giving it a name.
     * @param name the name of the node
     */
    public Node(String name) {
        this.name = name;
    }

    /**
     * Constructs this Node without setting any of its properties initially.
     */
    public Node() {}

    /**
     * @return the list of this Node's children
     */
    public List<Node> getChildren() { return this.children; }

    /**
     * @return the amount of children this Node has
     */
    public int getChildCount() { return this.children.size(); }

    /**
     * Adds a child to this Node.
     * @param child the Node to add as a child to this Node
     */
    public void addChild(Node child) {
        if (this.children == null) this.children = new ArrayList<>();
        this.children.add(child);
    }

    /**
     * Adds a child Node to this Node with the given information.
     * @param name the name of the child
     * @param value the value of the child
     */
    public void addChild(String name, String value) {
        this.addChild(new Node(name, value));
    }


    /**
     * Adds multiple children to this Node.
     * @param children the List of Nodes to add as children to this Node
     */
    public void addChildren(List<Node> children) {
        if (this.children == null) this.children = new ArrayList<>();
        if (children == null) return;
        for (Node child : children) this.children.add(child);
    }

    /**
     * Retrieves a child of this Node at the given index.
     * @param index the index of the child to retrieve
     * @return the child at the given index
     */
    public Node getChild(int index) {
        if (index > this.children.size()) {
            throw Util.fatalError("Node.java", "getChild(int",
                    "Unable to access index " + index + " in child array of size " + this.children.size());
        }
        return this.children.get(index);
    }

    /**
     * Retrieves a child of this Node with the given name.
     * @param name the name of the child to retrieve
     * @return the first child of this Node whose name matches the given name
     */
    public Node getChild(String name) {
        for (Node child : this.children) if (child.getName().equals(name)) return child;
        return null;
    }

    /**
     * @return whether or not this Node has any children
     */
    public boolean hasChildren() {
        if (this.children == null) return false;
        if (this.children.size() < 1) return false;
        return true;
    }

    //Accessors
    public String getName() { return this.name; }
    public String getValue() { return this.value; }
    public boolean hasName() { return this.name != null; }
    public boolean hasValue() { return this.value != null; }

    //Mutators
    public void setValue(String value) { this.value = value; }
    public void setName(String name) { this.name = name; }

    /**
     * Adds the given Node's data to the given Bundle.
     * @param bundle the Bundle to store the Node data in
     * @param node the Node whose data is to be added to the given Bundle.
     */
    public static void nodeToBundle(Bundle bundle, Node node) {
        nodeToBundleR(bundle, "", node);
    }

    /**
     * Recursively traverses a Node and put its contents into the given Bundle
     * @param bundle the Bundle to put the node data in
     * @param preface the preface for the Bundle data naming (maintains the structure of the Node)
     * @param node the Node whose data to deposit into the Bundle
     */
    private static void nodeToBundleR(Bundle bundle, String preface, Node node) {
        bundle.putString(preface + node.getName(), node.getValue());
        if (node.hasChildren())
            for (Node child : node.getChildren())
                nodeToBundleR(bundle, preface + node.getName() + "_", child);
    }

    /**
     * Reads a Node from a given resource.
     * @param resourceID the resourceID to read the node from
     * @return the read Node
     */
    public static Node readNode(int resourceID) {

        //create node
        Node node = new Node();

        //read data from resource
        List<String> data = Util.readResourceFile(resourceID);

        //parse node recursively
        readNodeR(node, data, 0, 0);

        //return node
        return node;
    }

    /**
     * Reads a node at the given file path relative to the app's file path.
     * @param path the path to read the Node from
     * @return the read Node
     */
    public static Node readNode(String path) {

        //create node
        Node node = new Node();

        //open file
        try {
            BufferedReader in = new BufferedReader(new FileReader(MainActivity.appDir + "/" + path));
            List<String> fileContents = new ArrayList<>();

            //put file into an ArrayList and then recursively parse it
            while (in.ready()) fileContents.add(in.readLine());
            readNodeR(node, fileContents, 0, 0);

            //catch any errors
        } catch (Exception e) {
            throw Util.fatalError("Node.java", "readNode(String)", e.getMessage());
        }

        //return node
        return node;
    }

    /**
     * Recursively reads a Node from a given list of strings.
     * @param node the current Node in focus
     * @param fileContents the recursively static file contents
     * @param i the current line of fileContents in focus
     * @param indent the current indent in terms of number of characters
     * @return the node in focus and its recursively read children
     */
    private static int readNodeR(Node node, List<String> fileContents, int i, int indent) {

        //format next line and find dividing point
        String nextLine = fileContents.get(i); //get line
        nextLine = nextLine.substring(indent, nextLine.length()); //remove indent
        int dividerLocation = -1; //location of the divider in line
        for (int j = 0; j < nextLine.length() && dividerLocation == -1; j++)
            if (nextLine.charAt(j) == Node.DIVIDER_CHAR) dividerLocation = j; //find divider

        //throw error if no divider found
        if (dividerLocation == -1)
            throw Util.fatalError("Node.java", "readNodeR(Node, List<String>, int, int",
                    "could not find divider in line: '" + nextLine + "'");

        //create node and set name
        Node curr = new Node();
        String possibleName = nextLine.substring(0, dividerLocation);
        if (!possibleName.equals("")) curr.setName(nextLine.substring(0, dividerLocation)); //create node with name

        //set node value if there is one
        String possibleValue = nextLine.substring(dividerLocation + 1, nextLine.length()); //grab possible value
        if (!possibleValue.equals(" ") && !possibleValue.equals("")) { //if possible value has substance
            curr.setValue(possibleValue.substring(1, possibleValue.length())); //set value (remove first space space)
        }

        //check for more file
        if (i + 1 <= fileContents.size()) { //if not eof

            //check for child nodes
            if (fileContents.get(i + 1).contains("{")) { //if the node has children
                i += 2; //iterate twice
                indent++; //iterate indent
                while (!fileContents.get(i).contains("}")) { //while there are more children

                    //add child
                    Node child = new Node(); //create child node
                    i = readNodeR(child, fileContents, i, indent); //recursively read child, keep track of file position
                    curr.addChild(child); //add child

                    //throw error if file suddenly stops
                    if ((i + 1) > fileContents.size())
                        throw Util.fatalError("Node.java", "readNodeR(Node, List<String>, int, int",
                                "unexpected stop in file at line " + i);

                    //iterate i
                    i += 1;
                }
            }
        }

        //set node, return current position in file
        node.setName(curr.getName());
        node.setValue(curr.getValue());
        node.addChildren(curr.getChildren());
        return i;
    }

    /**
     * Writes a node to a file
     * @param node the Node to write
     * @param path the path to write the Node to. Will throw an error if doesn't exit or cannot open
     */
    public static void writeNode(Node node, String path) {

        //try to open file to print
        try {
            Node.makeAppropriateDirs(path);
            PrintWriter out = new PrintWriter(new File(MainActivity.appDir, path));

            //recursively save node then close file
            Node.writeNodeR(out, node, new StringBuilder());
            out.close();

            //catch errors
        } catch (Exception e) {
            throw Util.fatalError("Node.java", "writeNode(Node, String)", e.getMessage());
        }
    }

    /**
     * Makes the appropriate directories for a given path
     * @param path the path whose directories to make
     */
    public static void makeAppropriateDirs(String path) {
        int lastSlash = -1;
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '/') lastSlash = i;
        }
        if (lastSlash > -1) {
            String dirs = path.substring(0, lastSlash);
            File dir = new File(MainActivity.appDir, dirs);
            boolean result = dir.mkdirs();
        }
    }

    /**
     * Recursively write a node to a file
     * @param out the PrintWriter to use for writing
     * @param node the current node in focus
     * @param indent the current indent to use
     */
    private static void writeNodeR(PrintWriter out, Node node, StringBuilder indent) {

        //print name and date
        String indentString = indent.toString();
        out.print(indentString + (node.hasName() ? node.getName() : "") + Node.DIVIDER_CHAR + " ");
        out.println(node.hasValue() ? node.getValue() : "");

        //print children
        if (node.hasChildren()) {
            out.println(indentString + "{");
            indent.append(Node.INDENT_CHAR);
            for (Node child : node.getChildren()) writeNodeR(out, child, indent);
            indent.deleteCharAt(indent.length() - 1);
            out.println(indentString + "}");
        }
    }
}
