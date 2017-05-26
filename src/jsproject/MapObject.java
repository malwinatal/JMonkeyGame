package jsproject;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

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
    
    public MapObject(int zSamples, int radialSamples, float radius,
                        float locX, float locY, float locZ,
                        BulletAppState bullet, Node rootNode){
        
        s=new Sphere(zSamples, radialSamples, radius);
        geom = new Geometry("Sphere", s);
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
    
    public void addPhysics(float radius, float height, int axis)
    {
        shape = new CapsuleCollisionShape(radius, height, axis);
        //shape=CollisionShapeFactory.createMeshShape(geom);
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
        if (text!=null){
            text.setWrap(WrapMode.Repeat);
            mat.setTexture("DiffuseMap", text); 
        }
        
        geom.setMaterial(mat); 
        
        
    }
    
    
    private Box b;
    private Sphere s;
    public Geometry geom;
    private CollisionShape shape;
    private RigidBodyControl body;
    private BulletAppState bullet;
    private Node rootNode;
    
    
}
