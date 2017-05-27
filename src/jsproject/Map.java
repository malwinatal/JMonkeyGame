package jsproject;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Map {

    public Map(int columns, int rows, AssetManager manager, Node rNode, BulletAppState bulletAppState, Node shootables) {
        /*Initialize whole table of Cells with default values/
        Object of Cell class contains: up, down, left, right which
        are possible directions of discovering the maze, and visited
        which says if cell was visited or not.
         */
        M = new Cell[rows][columns];
        for (Cell[] M1 : M) {
            for (int c = 0; c < M1.length; c++) {
                M1[c] = new Cell();
            }
        }
        this.rootNode = rNode;
        this.bulletAppState = bulletAppState;
        this.assetManager = manager;
        this.shootables = shootables;
        ArmyOfEnemies = new ArrayList();
    }

    //Method which generate random Maze.
    private void generateMaze() {
        //start points of the maze
        int r = 0;
        int c = 0;

        /*Lists of histories in which previous steps of 
        going through the maze are stored*/
        List rows_history = new LinkedList();
        List cols_history = new LinkedList();

        /*Let's add to the history our start point*/
        rows_history.add(r);
        cols_history.add(c);

        Random rand = new Random();
        int rand_value;
        boolean rand_surprise;
        boolean endP = false;

        /*Maze is generated as long as there are values in history
        what means: as long as there are possible steps back
         */
        while (rows_history.size() > 0) {
            /*
            List of visited cells near actual cell.
             */
            List check = new LinkedList();
            M[r][c].visited = 1;
            /*
            Let's check if cells around actual cell were visited,
            if not, add them to the list.
             */
            if (c > 0 && M[r][c - 1].visited == 0) {
                check.add('L');
            }
            if (r > 0 && M[r - 1][c].visited == 0) {
                check.add('U');
            }
            if (c < M[r].length - 1 && M[r][c + 1].visited == 0) {
                check.add('R');
            }
            if (r < M.length - 1 && M[r + 1][c].visited == 0) {
                check.add('D');
            }

            /*
            Check if there are any cells to visit from actual cell
             */
            if (check.size() > 0) {
                /*
                Add actual cell to the history
                 */
                rows_history.add(r);
                cols_history.add(c);
                /*
                Choose random direction from the list of checks
                and "go" to this cell.
                 */
                rand_value = rand.nextInt(check.size());
                if (check.get(rand_value).equals('L')) {
                    M[r][c].left = 1;
                    c = c - 1;
                    M[r][c].right = 1;
                }
                if (check.get(rand_value).equals('U')) {
                    M[r][c].up = 1;
                    r = r - 1;
                    M[r][c].down = 1;
                }
                if (check.get(rand_value).equals('R')) {
                    M[r][c].right = 1;
                    c = c + 1;
                    M[r][c].left = 1;
                }
                if (check.get(rand_value).equals('D')) {
                    M[r][c].down = 1;
                    r = r + 1;
                    M[r][c].up = 1;
                }
                endP = true;

            } /*
            If there are no possible directions to go from actual cell,
            go to the previous cell.
             */ else {

                if (endP) {
                    rand_surprise = rand.nextBoolean();
                    M[r][c].gift = rand_surprise;
                    M[r][c].enemy = !rand_surprise;
                    endP = false;
                }

                r = (Integer) rows_history.get(rows_history.size() - 1);
                c = (Integer) cols_history.get(cols_history.size() - 1);

                rows_history.remove(rows_history.size() - 1);
                cols_history.remove(cols_history.size() - 1);
            }
        }

    }

    /*
    Method which print Maze to the image. 
     */
    public void buildMap() {

        //number of gifts
        int numGift = 0;

        generateMaze();

        /*
        Start point for generating boxes.
         */
        Point p = new Point(1, 1);

        /*
        Create map with its borders dependably of predefined size
         */
        createGround();
        createBorder(M.length * 2 + 5, 3f, 0.5f, M.length * 2, 0, -5);
        createBorder(M.length * 2 + 5, 3f, 0.5f, M.length * 2, 0, M.length * 4 + 5);

        createBorder(0.5f, 3f, M.length * 2 + 5, -5, 0, M.length * 2);
        createBorder(0.5f, 3f, M.length * 2 + 5, M.length * 4 + 5, 0, M.length * 2);
        createObstacle(-2, -2, -2);
        createObstacle(-2, -2, 6);

//        just for testing shooting
//        ArmyOfEnemies.add(new Enemy(shootables, bulletAppState, assetManager, 2, 0, 2));

        /*
//        Loop going through the cells of maze.
        Simple cell was extended to the 4x4 points.
        [][][][]
        [][][][]
        [][][][]
        [][][][]
        Middle points are always "opened". In the loop we check in which 
        direction player can move.
        Values of the possible directions in simple Maze cell are checked.
        In blocked directions boxes are being created.
         */
        for (int r = 0; r < M.length; r++) {
            for (int c = 0; c < M[r].length; c++) {
                /*
                Checking possible directions, 0 means that way is closed.
                Then we create boxes in this direction.
                 */
                if (M[M[0].length - (1 + r)][c].left == 0) {
                    /*
                    For Point = (1,1) we create entrace for the maze
                     */
                    if (r != 0 || c != 0) {
                        createWallBox(p.x - 1, 0, p.y);
                        createWallBox(p.x - 1, 0, p.y + 1);
                    }
                }
                if (M[M[0].length - (1 + r)][c].up == 0) {
                    createWallBox(p.x, 0, p.y + 2);
                    createWallBox(p.x + 1, 0, p.y + 2);
                }
                if (M[M[0].length - (1 + r)][c].right == 0) {
                    /*
                    For last point we create exit from the maze.
                     */
                    if (r != (M.length - 1) || c != (M[r].length - 1)) {
                        createWallBox(p.x + 2, 0, p.y);
                        createWallBox(p.x + 2, 0, p.y + 1);
                    }
                }
                if (M[M[0].length - (1 + r)][c].down == 0) {
                    createWallBox(p.x, 0, p.y - 1);
                    createWallBox(p.x + 1, 0, p.y - 1);
                }

                //creating gifts and enemies, in randomly choose ends of the maze
                if (r > 0 || c > 0) {
                    if (M[M[0].length - (1 + r)][c].gift) {
                        if (flag) {
                            createGift(p.x + 0.5f, 0, p.y + 0.5f);
                            flag = false;
                        } else {
                            ArmyOfEnemies.add(new Enemy(shootables, bulletAppState, assetManager,
                                          p.x + 0.5f, 0, p.y + 0.5f));
                            flag = true;
                        }

//                    if(M[M[0].length-(1+r)][c].gift ){
//                        int random = (int )(Math.random() * 3);
//                        if(random<2){
//                            createGift(p.x+0.5f, 0, p.y+0.5f);    
//                         }
//                       else{
//                            createEnemy(p.x+0.5f, 0, p.y+0.5f);//then enemy
//                         }
//                        //i dont like this idea
                    }

                }

                /*
                Creation boxes in the corners.
                 */
                createWallBox(p.x - 1, 0, p.y - 1);
                createWallBox(p.x + 2, 0, p.y - 1);
                createWallBox(p.x + 2, 0, p.y + 2);
                createWallBox(p.x - 1, 0, p.y + 2);
                /*
                Move point
                 */
                p.translate(4, 0);
            }
            /*
            Move point to the new line
             */
            p.translate(-4 * M.length, 4);
        }
    }

    /*
    Creation of simple wall box. 
    Parameters specifies location of the box
     */
    void createWallBox(float locx, float locy, float locz) {
        MapObject wallBox = new MapObject(0.5f, 3f, 0.5f,
                      locx, locy, locz,
                      bulletAppState, rootNode);

        Material mat = new Material(assetManager,
                      "Common/MatDefs/Light/Lighting.j3md");
        Texture dirt = assetManager.loadTexture(
                      "Textures/Terrain/splat/dirt.jpg");

        wallBox.geom.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        wallBox.addMatText(mat, dirt);
        wallBox.addPhysics();

    }

    /*
    Creation of the ground. Its size depends on size of map.
     */
    private void createGround() {
        MapObject ground = new MapObject(M.length * 2 + 5, 0.5f, M.length * 2 + 5,
                      M.length * 2, -2f, M.length * 2,
                      bulletAppState, rootNode);

        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        Texture grass = assetManager.loadTexture(
                      "Textures/Terrain/splat/grass.jpg");

        ground.geom.setShadowMode(RenderQueue.ShadowMode.Receive);
        ground.addMatText(mat, grass, new Vector2f(10f, 10f));
        ground.addPhysics();

    }

    /*
    Creation of the borders of the map.
    Parameters specifies its size and location.
     */
    private void createBorder(float sizex, float sizey, float sizez,
                  float locx, float locy, float locz) {

        MapObject wallBox = new MapObject(sizex, sizey, sizez,
                      locx, locy, locz,
                      bulletAppState, rootNode);

        Material mat = new Material(assetManager,
                      "Common/MatDefs/Light/Lighting.j3md");
        Texture dirt = assetManager.loadTexture(
                      "Textures/Terrain/splat/dirt.jpg");

        //wallBox.geom.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        wallBox.addMatText(mat, dirt);
        wallBox.addPhysics();

    }

    /*
    do zmiany
     */
    private void createObstacle(float locx, float locy, float locz) {
        Spatial tree = assetManager.loadModel("Models/Tree/Tree.mesh.j3o");
        //Material mat_default = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
        //tree.setMaterial(mat_default);
        tree.setLocalTranslation(locx, locy, locz);
        tree.setLocalScale(4f);
        tree.rotate(0, 15, 0);
        CapsuleCollisionShape shape = new CapsuleCollisionShape(1.5f, 1.5f, 1);

        RigidBodyControl body = new RigidBodyControl(shape, 0);
        tree.addControl(body);
        tree.setShadowMode(RenderQueue.ShadowMode.Cast);
        bulletAppState.getPhysicsSpace().add(body);
        //rootNode.attachChild(tree);
        shootables.attachChild(tree);
    }

    private void createGift(float locx, float locy, float locz) {
        MapObject gift = new MapObject(50, 50, 1,
                      locx, locy, locz,
                      bulletAppState, rootNode);
        Material mat = new Material(assetManager,
                      "Common/MatDefs/Light/Lighting.j3md");
        Texture dirt = assetManager.loadTexture(
                      "Textures/Terrain/Pond/Pond.jpg");

        gift.addMatText(mat, dirt);
        gift.addPhysics(1, 1, 1);

    }

    public void moveGolems(float playerLocX, float playerLocZ) {
        for (Enemy golemEnemy : ArmyOfEnemies) {

            Vector3f golemLoc = golemEnemy.getGolemLocation();

            boolean possibleDirections[] = new boolean[4];
            possibleDirections[0] = false;
            possibleDirections[1] = false;
            possibleDirections[2] = false;
            possibleDirections[3] = false;
            int MazeX = Math.round((golemLoc.x - 1.5f) / 4);
            int MazeY = Math.round((golemLoc.z - 1.5f) / 4);

            if (MazeX >= 0 && MazeY >= 0 && MazeX < M.length && MazeY < M.length) {

//                if(M[M.length-(MazeX+1)][MazeY].left==1){possibleDirections[0]=true;}
//                if(M[M.length-(MazeX+1)][MazeY].right==1){possibleDirections[1]=true;}
//                if(M[M.length-(MazeX+1)][MazeY].down==1){possibleDirections[2]=true;}
//                if(M[M.length-(MazeX+1)][MazeY].up==1){possibleDirections[3]=true;}
                if (M[M.length - (MazeX + 1)][MazeY].left == 1) {
                    possibleDirections[0] = true;
                }
                if (M[M.length - (MazeX + 1)][MazeY].right == 1) {
                    possibleDirections[1] = true;
                }
                if (M[M.length - (MazeX + 1)][MazeY].down == 1) {
                    possibleDirections[2] = true;
                }
                if (M[M.length - (MazeX + 1)][MazeY].up == 1) {
                    possibleDirections[3] = true;
                }

            }

            System.out.print(golemLoc + " ");
            System.out.print(M.length - (MazeX + 1) + " ");
            System.out.print(MazeY + " ");
            System.out.print(Arrays.toString(possibleDirections) + " ");
            golemEnemy.runGolemRun(playerLocX, playerLocZ, possibleDirections);
            System.out.print("\n");
        }
    }

    private List<Enemy> ArmyOfEnemies;

    private Cell[][] M;
    private AssetManager assetManager;
    private Node rootNode;
    private BulletAppState bulletAppState;

    private Node golem;
    private AnimChannel channel;
    private AnimControl control;
    private Boolean flag = true;
    private Node shootables;

}
