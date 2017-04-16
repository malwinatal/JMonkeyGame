/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsproject;

import jsproject.Cell;
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
    
    public Maze(int columns, int rows)
    {
        
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
        BufferedImage image = new BufferedImage(M.length*4,M[0].length*4, BufferedImage.TYPE_INT_ARGB);
   
        /*
        Different colors for paths and walls.
        Heightmap recognize light colors as high terrain, 
        and dark colors as low terrain.
        */
        int path = Color.DARK_GRAY.getRGB();
        int walls = Color.LIGHT_GRAY.getRGB();
        
        //System.out.println(Color.DARK_GRAY.getRed());
        //System.out.println(Color.LIGHT_GRAY.getRed());
        
        /*
        Start point for painting the image.
        */
        Point p = new Point(1,1);
        
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
                if(M[M[0].length-(1+r)][c].left==1)
                {
                    path = getRandomColor(path);
                    image.setRGB(p.x-1, p.y, path);
                    image.setRGB(p.x-1, p.y+1, path);
                }
                else
                {
                    walls = getRandomColor(walls);
                    image.setRGB(p.x-1, p.y, walls);
                    image.setRGB(p.x-1, p.y+1, walls);
                }
                
                if(M[M[0].length-(1+r)][c].up==1)
                {
                    path = getRandomColor(path);
                    image.setRGB(p.x, p.y+2, path);
                    image.setRGB(p.x+1, p.y+2, path);
                }
                else
                {
                    walls = getRandomColor(walls);
                    image.setRGB(p.x, p.y+2, walls); 
                    image.setRGB(p.x+1, p.y+2, walls);
                }
                
                if(M[M[0].length-(1+r)][c].right==1)
                {
                    path = getRandomColor(path);
                    image.setRGB(p.x+2, p.y, path);
                    image.setRGB(p.x+2, p.y+1, path);
                }
                else
                {
                    walls = getRandomColor(walls);
                    image.setRGB(p.x+2, p.y, walls);   
                    image.setRGB(p.x+2, p.y+1, walls);
                }
                
                if(M[M[0].length-(1+r)][c].down==1)
                {
                    path = getRandomColor(path);
                    image.setRGB(p.x, p.y-1, path);
                    image.setRGB(p.x+1, p.y-1, path);
                }
                else
                {
                    walls = getRandomColor(walls);
                    image.setRGB(p.x, p.y-1, walls);
                    image.setRGB(p.x+1, p.y-1, walls);
                }

                /*
                Those points are always the same, they are middle points,
                and corners.
                [.][][][.]
                [][.][.][]
                [][.][.][]
                [.][][][.]
                */
                
                path = getRandomColor(path);
                image.setRGB(p.x,p.y,path);
                image.setRGB(p.x+1,p.y,path);
                image.setRGB(p.x,p.y+1,path);
                image.setRGB(p.x+1,p.y+1,path);
                
                walls = getRandomColor(walls);
                image.setRGB(p.x-1, p.y-1, walls);
                image.setRGB(p.x+2, p.y-1, walls);
                image.setRGB(p.x+2, p.y+2, walls);
                image.setRGB(p.x-1, p.y+2, walls);
                
                
                /*
                Move point
                */
                p.translate(4,0);
                
             
                path = Color.DARK_GRAY.getRGB();
                walls=Color.LIGHT_GRAY.getRGB();
                
            }
            /*
            Move point to the new line
            */
            p.translate(-4*M.length,4);

        }
        
        /*
        Create output image file.
        */
        BufferedImage output = new BufferedImage(1024,1024, BufferedImage.TYPE_INT_ARGB);

        /*
        The output image is scaled previous image to the size of terrain.
        */
        AffineTransform at = new AffineTransform();
        at.scale(102.4/4, 102.4/4);
        AffineTransformOp scaleOp =  new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        output = scaleOp.filter(image, output);
        
        ImageIO.write(output, "png", new File("assets/Textures/test.png"));

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
    
    public Cell[][] M;
}
