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
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
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
        CapsuleCollisionShape shape = new CapsuleCollisionShape(0.6f, 0.6f, 1);
        RigidBodyControl body = new RigidBodyControl(shape, 0);
        golem.addControl(body);
        bulletAppState.getPhysicsSpace().add(body);
        rootNode.attachChild(golem);
        control = golem.getControl(AnimControl.class);

        channel = control.createChannel();
        channel.setAnim("Walk");
        channel.setLoopMode(LoopMode.Loop);
        channel.setSpeed(1f);

    }

    public void runGolemRun(float playerLocX, float playerLocZ, boolean[] directions) {

        System.out.print(Arrays.toString(directions));

        if (/*golem.getLocalTranslation().x>playerLocX &&*/directions[0]) {
            golem.move(-speed, 0, 0);
        } else if (/*golem.getLocalTranslation().x<playerLocX &&*/directions[1]) {
            golem.move(speed, 0, 0);
        } else if (/*golem.getLocalTranslation().z>playerLocX &&*/directions[2]) {
            golem.move(0, 0, -speed);
        } else if (/*golem.getLocalTranslation().z<playerLocX &&*/directions[3]) {
            golem.move(0, 0, speed);
        }

        locx = golem.getLocalTranslation().z;
        locz = golem.getLocalTranslation().x;

        System.out.print(golem.getLocalTranslation());
    }

    public Vector3f getGolemLocation() {
        return new Vector3f(locx, locy, locz);
    }

    private int health;
    private static float speed = 4f;
    private float locx;
    private float locy;
    private float locz;
    private int previousState;

    private AssetManager assetManager;
    private Node rootNode;
    private BulletAppState bulletAppState;

    private Node golem;
    private AnimChannel channel;
    private AnimControl control;
    //private Boolean flag=true;

}
