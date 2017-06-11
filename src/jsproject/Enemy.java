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
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.audio.LowPassFilter;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import static java.lang.Math.abs;
import java.util.Objects;

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
        golem.setShadowMode(RenderQueue.ShadowMode.Cast);

        channel = control.createChannel();
        channel.setAnim("stand");
        channel.setLoopMode(LoopMode.Loop);
        channel.setSpeed(1f);

        moveReminder = new Vector3f(0, 0, 0);
        audioGolem = new AudioNode(assetManager, "Sounds/monster.wav", AudioData.DataType.Buffer);
        audioFarGolem = new AudioNode(assetManager, "Sounds/farmonster.wav", AudioData.DataType.Buffer);
        makeSound(audioGolem);
        makeSound(audioFarGolem);

    }

    public Vector3f getGolemLocation() {
        updateGolemLocation();
        return new Vector3f(locx, locy, locz);
    }

    public void updateGolemLocation() {
        locx = golem.getLocalTranslation().x;
        locz = golem.getLocalTranslation().z;
    }

    public void getGolemCollision(BoundingBox b) {
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

    public void moveGolem(float playerLocX, float playerLocZ) {

        updateGolemLocation();
//        System.out.println("nowy");
//        System.out.println(abs(playerLocX - locx));
//        System.out.println(abs(playerLocZ - locz));
        if (abs(playerLocX - locx) < 5 && abs(playerLocZ - locz) < 5) {
            audioGolem.play();
            audioFarGolem.stop();
            if (playerLocX < locx) {
                golem.move(-speed, 0, 0);
                moveReminder.setX(-speed);
            } else if (playerLocX > locx) {
                golem.move(speed, 0, 0);
                moveReminder.setX(speed);
            } else if (playerLocX == locx) {
                golem.move(0, 0, 0);
                moveReminder.setX(0);
            }

            if (playerLocZ < locz) {
                golem.move(0, 0, -speed);
                moveReminder.setZ(-speed);
            } else if (playerLocZ > locz) {
                golem.move(0, 0, speed);
                moveReminder.setZ(speed);
            } else {
                golem.move(0, 0, 0);
                moveReminder.setX(0);
            }

            golem.lookAt(new Vector3f(playerLocX, 0, playerLocZ), new Vector3f(0, 1, 0));
            if (!"Walk".equals(channel.getAnimationName())) {
                channel.setAnim("Walk");
            }
        } else if (abs(playerLocX - locx) < 7 && abs(playerLocZ - locz) < 7) {
            channel.setAnim("stand");
            audioGolem.stop();
            audioFarGolem.play();
        } else {
            audioGolem.stop();
            audioFarGolem.stop();
            channel.setAnim("stand");
        }

    }

    public void changeAnim() {

    }

    public void stepBack() {
        golem.move(-moveReminder.x, 0, -moveReminder.z);
    }

    private void makeSound(AudioNode audioGolem) {
        audioGolem.setDryFilter(new LowPassFilter(0f, 0f));
        audioGolem.setPositional(true); // Use 3D audio
        audioGolem.setRefDistance(0.5f); // Distance of 50% volume
        audioGolem.setMaxDistance(1000f);
        audioGolem.setDirectional(true);
        audioGolem.setInnerAngle(90);
        audioGolem.setOuterAngle(240);
        audioGolem.setDirection(new Vector3f(audioGolem.getPosition().x, audioGolem.getPosition().y, audioGolem.getPosition().z));
        audioGolem.setVolume(1.5f); // Default volume
        audioGolem.setLooping(true); // play continuously
        audioGolem.setReverbEnabled(true);
        golem.attachChild(audioGolem);
    }

    public void remove() {
        bulletAppState.getPhysicsSpace().remove(body);
        rootNode.detachChild(golem);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof Node) {
            final Node other = (Node) obj;
            return Objects.equals(this.golem, other);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + Objects.hashCode(this.golem);
        return hash;
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
    private AudioNode audioGolem;
    private AudioNode audioFarGolem;

}
