/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsproject;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingSphere;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import static java.lang.Math.abs;
import java.util.Arrays;

public class Enemy {

    public Enemy(Node rNode, BulletAppState bulletAppState, AssetManager manager,
                  float locx, float locy, float locz) {
        this.rootNode = rNode;
        this.bulletAppState = bulletAppState;
        this.assetManager = manager;
        health = 100;
        previousState = 0;

        this.locx = locx;
        this.locy = locy;
        this.locz = locz;

        golem = (Node) assetManager.loadModel("Models/Oto/Oto.mesh.xml");
        golem.setLocalScale(0.3f);
        golem.setLocalTranslation(locx, locy, locz);
        lastGoodPosition = golem.getLocalTranslation();
        shape = new CapsuleCollisionShape(0.6f, 0.6f, 1);
        body = new RigidBodyControl(shape, 0);
        
        golem.addControl(body);
        
        
        bulletAppState.getPhysicsSpace().add(body);
        body.setKinematic(true);
        rootNode.attachChild(golem);
        control = golem.getControl(AnimControl.class);

        channel = control.createChannel();
        channel.setAnim("stand");
        channel.setLoopMode(LoopMode.Loop);
        channel.setSpeed(1f);
        
        moveReminder = new Vector3f(0,0,0);

    }

   

    public Vector3f getGolemLocation() {
        updateGolemLocation();
        return new Vector3f(locx, locy, locz);
    }
    
    public void updateGolemLocation(){
        locx = golem.getLocalTranslation().x;
        locz = golem.getLocalTranslation().z;
    }
    
    public void getGolemCollision(BoundingBox b){
        CollisionResults results = new CollisionResults();
        golem.collideWith(b, results);
        
        if (results.size() > 0) {
            // how to react when a collision was detected
//            CollisionResult closest  = results.getClosestCollision();
//            System.out.println("What was hit? " + closest.getGeometry().getName() );
//            System.out.println("Where was it hit? " + closest.getContactPoint() );
//            System.out.println("Distance? " + closest.getDistance() );
            stepBack();
            
        }

}

    public void moveGolem(float playerLocX, float playerLocZ){
        
        updateGolemLocation();
        if(abs(playerLocX-locx)<5 || abs(playerLocZ-locz)<5){
            if(playerLocX<locx){golem.move(-speed,0,0);
                                    moveReminder.setX(-speed);
                                   }
            else if (playerLocX>locx){golem.move(speed,0,0);
                                    moveReminder.setX(speed);}
            else if(playerLocX==locx){golem.move(0,0,0);
                                    moveReminder.setX(0);}

            if(playerLocZ<locz){golem.move(0,0,-speed);
                                moveReminder.setZ(-speed);}
            else if (playerLocZ>locz){golem.move(0,0,speed);
                                moveReminder.setZ(speed);}
            else {golem.move(0,0,0);
                                moveReminder.setX(0);}
            
            golem.lookAt(new Vector3f(playerLocX,0, playerLocZ), new Vector3f(0,1,0));
            if(!"Walk".equals(channel.getAnimationName())){
                channel.setAnim("Walk");
            }
        }else{
            channel.setAnim("stand");
        }

        
    }
    
    public void changeAnim(){
        
    }
    
    public void stepBack(){
        golem.move(-moveReminder.x,0,-moveReminder.z);
    }
    
    private int health;
    private static float speed = 0.11f;
    private float locx;
    private float locy;
    private float locz;
    private int previousState;
    private Vector3f moveReminder;
    private Vector3f lastGoodPosition;

    private AssetManager assetManager;
    private Node rootNode;
    private BulletAppState bulletAppState;

    private Node golem;
    private AnimChannel channel;
    private AnimControl control;
    
    private RigidBodyControl body;
    private CapsuleCollisionShape shape;
    //private Boolean flag=true;

}


