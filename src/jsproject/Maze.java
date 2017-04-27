/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsproject;


import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import static com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Root;
import java.awt.Color;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 *
 * @author Kuba
 */
public class Maze {
    
    public Maze(int columns, int rows, AssetManager manager, Node rNode, BulletAppState bulletAppState)
    {
        this.rootNode = rNode;
        this.bulletAppState = bulletAppState;
        /*Initialize whole table of Cells with default values/
        Object of Cell class contains: up, down, left, right which
        are possible directions of discovering the maze, and visited
        which says if cell was visited or not.
        */
        M = new Cell[rows][columns];
        for(int r=0; r<M.length;r++)
        {
            for(int c=0; c<M[r].length;c++)
            {
                M[r][c]=new Cell();
            }
            
        }
        
        this.assetManager=manager;
    }
    
    
    //Method which generate random Maze.
    public void generateMaze() throws IOException 
    {
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
        
        
        /*Maze is generated as long as there are values in history
        what means: as long as there are possible steps back
        */
        
        
        while(rows_history.size()>0)
        {    
            
            /*
            List of visited cells near actual cell.
            */
            List check = new LinkedList();
            
            M[r][c].visited=1;

            /*
            Let's check if cells around actual cell were visited,
            if not, add them to the list.
            */
            if (c > 0 && M[r][c-1].visited == 0)
            {
                check.add('L');
            }
            if (r>0 && M[r-1][c].visited == 0)
            {
                check.add('U');
            }
            if (c<M[r].length-1 && M[r][c+1].visited == 0)
            {
                check.add('R');
            }
            if (r<M.length-1 && M[r+1][c].visited == 0)
            {
                check.add('D');
            }

            /*
            Check if there are any cells to visit from actual cell
            */
            if (check.size() > 0)
            {
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

                if (check.get(rand_value).equals('L'))
                {
                    M[r][c].left=1;
                    c=c-1;
                    M[r][c].right=1;
                }
                if (check.get(rand_value).equals('U'))
                {
                    M[r][c].up=1;
                    r=r-1;
                    M[r][c].down=1;
                }
                if (check.get(rand_value).equals('R'))
                {
                    M[r][c].right=1;
                    c=c+1;
                    M[r][c].left=1;
                }
                if (check.get(rand_value).equals('D'))
                {
                    M[r][c].down=1;
                    r=r+1;
                    M[r][c].up=1;
                }
            }
            /*
            If there are no possible directions to go from actual cell,
            go to the previous cell.
            */
            
            else
            {
                r=(Integer)rows_history.get(rows_history.size()-1);
                c=(Integer)cols_history.get(cols_history.size()-1);
                
                rows_history.remove(rows_history.size()-1);
                cols_history.remove(cols_history.size()-1);
            }
        }
        printMaze();     
    }
    
    /*
    Method which print Maze to the image. 
    */
    private void printMaze() throws IOException
    {
        /*
        Creation of image. 
        Width and height of file is size of M matrix multiplied by four, 
        for extending place for player.
        */
        //BufferedImage image = new BufferedImage(M.length*4,M[0].length*4, BufferedImage.TYPE_INT_ARGB);
   
        /*
        Different colors for paths and walls.
        Heightmap recognize light colors as high terrain, 
        and dark colors as low terrain.
        */
        //int path = Color.DARK_GRAY.getRGB();
        //int walls = Color.LIGHT_GRAY.getRGB();
        
        //System.out.println(Color.DARK_GRAY.getRed());
        //System.out.println(Color.LIGHT_GRAY.getRed());
        
        /*
        Start point for painting the image.
        */
        Point p = new Point(1,1);
        
        Box ground = new Box(Vector3f.ZERO, 30, 0.5f, 30); 
        Geometry geom = new Geometry("Box", ground);
        Material mat = new Material(assetManager,    "Common/MatDefs/Light/Lighting.j3md"); 
        //mat.setColor("Color", ColorRGBA.Red); 
        
        Texture grass = assetManager.loadTexture(
            "Textures/Terrain/splat/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        mat.setTexture("DiffuseMap", grass);
        geom.getMesh().scaleTextureCoordinates(new Vector2f(10f,10f));
    
    CollisionShape sceneShape = CollisionShapeFactory.createMeshShape(geom);
    RigidBodyControl landscape = new RigidBodyControl(sceneShape, 0);
    geom.addControl(landscape);
    
        geom.setMaterial(mat); 
        geom.setLocalTranslation(20,-1.5f,20);         
        rootNode.attachChild(geom);
        bulletAppState.getPhysicsSpace().add(landscape);

        /*
        Loop going through the cells of maze.
        Simple cell was extended to the 4x4 points.
        [][][][]
        [][][][]
        [][][][]
        [][][][]
        Middle points are always "opened". In the loop we check in which 
        direction player can move, and paint them differently.
        Values of the possible directions in simple cell Maze are checked.
        Then possible direction is painted in randomized dark gray color.
        Blocked directions are painted in randomized light gray color.
        */
        for(int r=0; r<M.length;r++)
        {
            for(int c=0; c<M[r].length;c++)
            {
                /*
                Checking possible directions
                */
                if(M[M[0].length-(1+r)][c].left==0)
                {

                    if(r==0&&c==0)
                    {}
                    else
                    {
                        createBox(p.x-1, 0, p.y);
                        createBox(p.x-1, 0, p.y+1);
                        
                    }
                    

                }
                
                if(M[M[0].length-(1+r)][c].up==0)
                {
                    createBox(p.x, 0, p.y+2);
                    createBox(p.x+1, 0, p.y+2);
                }
                
                if(M[M[0].length-(1+r)][c].right==0)
                {
                    if(r==(M.length-1) && c==(M[r].length-1))
                    {
                    }
                    else
                    {
                        createBox(p.x+2, 0, p.y);
                        createBox(p.x+2, 0, p.y+1);
                    }
                    
                }
                
                if(M[M[0].length-(1+r)][c].down==0)
                {
                    createBox(p.x, 0, p.y-1);
                    createBox(p.x+1, 0, p.y-1);
                }

                /*
                Those points are always the same, they are middle points,
                and corners.
                [.][][][.]
                [][.][.][]
                [][.][.][]
                [.][][][.]
                */
                
                
                createBox(p.x-1, 0, p.y-1);
                createBox(p.x+2, 0, p.y-1);
                createBox(p.x+2, 0, p.y+2);
                createBox(p.x-1, 0, p.y+2);
                
                
                /*
                Move point
                */
                p.translate(4,0);
                
             
//                path = Color.DARK_GRAY.getRGB();
//                walls=Color.LIGHT_GRAY.getRGB();
                
            }
            /*
            Move point to the new line
            */
            p.translate(-4*M.length,4);

        }
        
        /*
        Create output image file.
        */
//        BufferedImage output = new BufferedImage(1024,1024, BufferedImage.TYPE_INT_ARGB);
//
//        /*
//        The output image is scaled previous image to the size of terrain.
//        */
//        AffineTransform at = new AffineTransform();
//        at.scale(102.4/4, 102.4/4);
//        AffineTransformOp scaleOp =  new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
//        output = scaleOp.filter(image, output);
//        
//        ImageIO.write(output, "png", new File("assets/Textures/test.png"));

    }
    
    private int getRandomColor(int path)
    {
        
        Color col = new Color(path);
        int red;
        Random generator = new Random();
        red = col.getRed();
        
        if(generator.nextBoolean())
        {
            red = red + generator.nextInt(15);
        }
        else
        {
            red = red - generator.nextInt(15);
        }
        
        if(red>235)
        {
            red=235;
        }
        
        if(red<30)
        {
            red=30;
        }
        
        if (red>100 && red<150)
        {
            if (150-red > red-100)
            {
                red=100;
            }
            else
            {
                red=150;
            }
        }
        //System.out.println(red);
        col = new Color(red,red,red);
        
        return col.getRGB();
    }
    
    void createBox(int x, int y, int z)
    {
        Box b = new Box(Vector3f.ZERO, 0.5f, 3f, 0.5f); 
        Geometry geom = new Geometry("Box", b);
        Material mat = new Material(assetManager,    "Common/MatDefs/Light/Lighting.j3md"); 
        //mat.setColor("Color", ColorRGBA.Red); 
        
    Texture dirt = assetManager.loadTexture(
            "Textures/Terrain/splat/dirt.jpg");
    dirt.setWrap(WrapMode.Repeat);
    mat.setTexture("DiffuseMap", dirt);
    //mat.setFloat("Tex2Scale", 16f);
        
    
    
        geom.setMaterial(mat); 
        geom.setLocalTranslation(x-15,y+1f,z-15);   
        
        
            CollisionShape box = CollisionShapeFactory.createMeshShape(geom);
    RigidBodyControl wall = new RigidBodyControl(box, 0);
    geom.addControl(wall);
    
      bulletAppState.getPhysicsSpace().add(wall);  
        rootNode.attachChild(geom);
    }
    
    
    
    public Cell[][] M;
    private AssetManager assetManager;
    private Node rootNode;
    private BulletAppState bulletAppState;
}
