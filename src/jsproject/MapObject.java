/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsproject;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

/**
 *
 * @author Kuba
 */
public class MapObject {
    
    
    /*
    Constructor of the class for creating box-type objects. 
    Stores information about size, 
    location, bulletAppState and Node
    */
    public MapObject(float sizeX, float sizeY, float sizeZ,
                        float locX, float locY, float locZ,
                        BulletAppState bullet, Node rootNode){
        
        b=new Box(Vector3f.ZERO, sizeX, sizeY, sizeZ);
        geom = new Geometry("Box", b);
        geom.setLocalTranslation(locX,locY,locZ);  
        this.bullet=bullet;
        this.rootNode=rootNode;
        
    }
    
    /*
    Add collisions to the object
    */
    public void addPhysics()
    {
        shape=CollisionShapeFactory.createMeshShape(geom);
        body=new RigidBodyControl(shape, 0);
        geom.addControl(body);
        bullet.getPhysicsSpace().add(body);  
        rootNode.attachChild(geom);
        
    }
    /*
    Set material and texture Textures is prescaled.
    */
    public void addMatText(Material mat, Texture text, Vector2f scale)
    {
        text.setWrap(WrapMode.Repeat);
        mat.setTexture("DiffuseMap", text);
        geom.setMaterial(mat); 
        geom.getMesh().scaleTextureCoordinates(scale);
        
    }
    /*
    Set material and texture. Texture is not prescaled.
    */
        public void addMatText(Material mat, Texture text)
    {
        text.setWrap(WrapMode.Repeat);
        mat.setTexture("DiffuseMap", text);
        geom.setMaterial(mat); 
        
        
    }
    
    
    private Box b;
    private Geometry geom;
    private CollisionShape shape;
    private RigidBodyControl body;
    private BulletAppState bullet;
    private Node rootNode;
    
    
}
