package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FogFilter;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.PointLightShadowRenderer;
import com.jme3.shadow.SpotLightShadowRenderer;
import com.jme3.util.SkyFactory;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import jsproject.Map;
import jsproject.ParticleWithTimer;

public class Game extends SimpleApplication
              implements ActionListener {

//  private TerrainQuad terrain;
//  Material mat_terrain;
    private BulletAppState bulletAppState;
    private CharacterControl player;
    private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, up = false, down = false,
                  pause = false, gamebreak = false;
    private Vector3f camDir = new Vector3f();
    private Vector3f camLeft = new Vector3f();
    private FilterPostProcessor fpp;
    private FogFilter fogFilter;
    private SpotLight torch;
    private DirectionalLight sun = new DirectionalLight();
    private DirectionalLightShadowRenderer dlsr;
    private final int SHADOWMAP_SIZE = 2048;
    private Map labirynt;
    private BitmapText ch;
    private Geometry mark;
    private Node shootables;
    private Geometry sphereGeometry;
    private Node playerNode;
    private ParticleEmitter debrisEffect;
    private List<ParticleWithTimer> particles;
    private static float lightCounter = 0;
    private String hit;

    //Vectors for fog parameters: distance, density
    private static Vector2f strongFog = new Vector2f(50, 6.4f);
    private static Vector2f noFog = new Vector2f(500, 0.4f);

    //Size of Maze. Size of map depends on this value.
    private static int MazeSize = 10;
    private Spatial plModel;
//    private Node plLight;

    public static void main(String[] args) {
        Game app = new Game();
        app.start();
    }

    @Override
    public void simpleInitApp() {

        initCrossHairs();
        initMark();
//        initEffect();
        shootables = new Node("Shootables");
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        flyCam.setMoveSpeed(10);
        setUpKeys();
        generatePlayer(-2, 5, 2);
        generateLight();
        createFog();
        labirynt = new Map(MazeSize, MazeSize, assetManager, rootNode, bulletAppState, shootables);
        labirynt.buildMap();
        getRootNode().attachChild(SkyFactory.createSky(getAssetManager(), "Textures/Sky/Bright/BrightSky.dds", SkyFactory.EnvMapType.CubeMap));

        rootNode.attachChild(shootables);

        particles = Collections.synchronizedList(new ArrayList<ParticleWithTimer>());

    }

    private void setUpKeys() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping("Return", new KeyTrigger(KeyInput.KEY_R));
        inputManager.addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(this, "Left");
        inputManager.addListener(this, "Right");
        inputManager.addListener(this, "Up");
        inputManager.addListener(this, "Down");
        inputManager.addListener(this, "Jump");
        inputManager.addListener(this, "Pause");
        inputManager.addListener(this, "Return");
        inputManager.addListener(actionListener, "Shoot");

    }

    @Override
    public void onAction(String binding, boolean isPressed, float tpf) {
        switch (binding) {
            case "Left":
                left = isPressed;
                break;
            case "Right":
                right = isPressed;
                break;
            case "Up":
                up = isPressed;
                break;
            case "Down":
                down = isPressed;
                break;
            case "Jump":
                if (isPressed) {
                    player.jump();
                }
                break;
            case "Pause":
                if (isPressed) {
                    pause = !pause;
                    turnDebugMode(pause);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void simpleUpdate(float tpf) {

        if (!pause) {
            camDir.set(cam.getDirection()).multLocal(0.075f);
            camLeft.set(cam.getLeft()).multLocal(0.05f);
            walkDirection.set(0, 0, 0);
            if (left) {
                walkDirection.addLocal(camLeft);
            }
            if (right) {
                walkDirection.addLocal(camLeft.negate());
            }
            if (up) {
                walkDirection.addLocal(camDir);
            }
            if (down) {
                walkDirection.addLocal(camDir.negate());
            }
            player.setWalkDirection(walkDirection);
            cam.setLocation(player.getPhysicsLocation());
            Vector3f loc = player.getPhysicsLocation();
            Vector3f dir = cam.getDirection();
            double angle = Math.atan2(dir.x, dir.z) + Math.PI / 6;
            torch.setPosition(loc.add(new Vector3f(.9f * (float) (Math.sin(angle)), .3f, .9f * (float) (Math.cos(angle)))));
            torch.setDirection(dir);
//            System.out.println("dddd "+angle);
//            sphereGeometry.setLocalTranslation(loc.add(new Vector3f(.9f*(float)(Math.sin(angle)),.3f,.9f*(float)(Math.cos(angle)))));//comment later
//            playerNode.getLocalTranslation().set(loc);
//            playerNode.setLocalRotation(new Quaternion(dir.x, dir.y, dir.z, 0));
            labirynt.moveGolems(player.getPhysicsLocation().x, player.getPhysicsLocation().z);
        }
//        circleLight();

        Iterator<ParticleWithTimer> it = particles.iterator();
        while (it.hasNext()) {
            ParticleWithTimer p = it.next();
            if (p.expired) {
                rootNode.detachChild(p.particle);
                it.remove();
            }
        }
//        System.out.println("sphere loc: " + sphereGeometry.getLocalTranslation());
//        System.out.println("playerNode loc: " + playerNode.getLocalTranslation());

    }

    private void generatePlayer(float locx, float locy, float locz) {
        //Create player with his physics
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(0.82f, 1.5f, 1);
        cam.setFrustumNear(0.5f);
        player = new CharacterControl(capsuleShape, 0.05f);
        player.setJumpSpeed(15);
        player.setFallSpeed(20);
        player.setGravity(40);
        player.setPhysicsLocation(new Vector3f(locx, locy, locz));
        bulletAppState.getPhysicsSpace().add(player);
        generatePlayerModel(locx, locz);

//        playerNode = new Node("Player Node");
//        playerNode.setLocalTranslation(player.getPhysicsLocation());
//        rootNode.attachChild(playerNode);
    }

    private void generatePlayerModel(float locx, float locz) {
        plModel = assetManager.loadModel("Models/Ninja/Ninja.mesh.xml");
        plModel.scale(0.02f);
        plModel.setLocalTranslation(locx, 0, locz);
        //rootNode.attachChild(plModel);
    }

    private void generateLight() {
        /*
        Creation of two lights, one directional for DebugMode,
        one point light which is following the player.
         */

        sun.setDirection(new Vector3f(1, -1, 1));
        sun.setColor(ColorRGBA.White.mult(.3f));
        rootNode.addLight(sun);
        /**
         * A white ambient light source.
         */
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(.3f));
        rootNode.addLight(ambient);

        torch = new SpotLight(new Vector3f(), new Vector3f());
        torch.setSpotRange(3);
        torch.setSpotInnerAngle((float) Math.PI / 8);
        torch.setSpotOuterAngle((float) (Math.PI / 4));

        SpotLightShadowRenderer slsr = new SpotLightShadowRenderer(assetManager, SHADOWMAP_SIZE);
        slsr.setLight(torch);
        viewPort.addProcessor(slsr);

        //checking if working
//       Sphere sphere = new Sphere(8, 8, .1f);
//        sphereGeometry = new Geometry("Sphere", sphere);
//        sphereGeometry.setMaterial(assetManager.loadMaterial("Common/Materials/WhiteColor.j3m"));
//        sphereGeometry.setLocalTranslation(player.getPhysicsLocation().add(new Vector3f(0,3,0)));
//        rootNode.attachChild(sphereGeometry);
        //camera node
//        cameraNode = new CameraNode("Camera Node", cam);
//        cameraNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
//        
//        cameraNode.attachChild(sphereGeometry);
//        rootNode.attachChild(cameraNode);
//        plLight.attachChild(sphereGeometry)
//        dlsr = new DirectionalLightShadowRenderer(assetManager, SHADOWMAP_SIZE, 3);
//        dlsr.setLight(sun);
//        dlsr.setShadowIntensity(0.5f);
//        dlsr.setShadowZExtend(5f);
//        viewPort.addProcessor(dlsr);
    }

//private void circleLight(){
//    float x;
//    float z;
//    x= 5+(float)cos(lightCounter)*20;
//    z= 5+(float)sin(lightCounter)*20;
//    lightCounter=lightCounter+0.1f;
//    
//    
//    sun.setDirection(new Vector3f(x,-1,z));
//}
    private void turnDebugMode(boolean par) {
        if (par) {
            /*
            During debug mode:
            -turn on light for whole scene
            -turn off point light
            -turn off fog
             */
            rootNode.addLight(sun);
//            rootNode.removeLight(lighter);
            rootNode.removeLight(torch);
//            dlsr.setShadowIntensity(0);
            cam.setLocation(new Vector3f(MazeSize * 2, MazeSize * 2 + 20, MazeSize * 2));
            cam.lookAt(new Vector3f(MazeSize * 2, 0, MazeSize * 2), new Vector3f(0, 1, 0));
            changeFogParams(noFog);
            guiNode.detachChild(ch);
            plModel.setLocalTranslation(player.getPhysicsLocation().x, -1.5f, player.getPhysicsLocation().z);
//            plModel.lookAt(player.getPhysicsLocation(), camDir);
            rootNode.attachChild(plModel);

        } else {
            /*
            For normal mode:
            -turn off directiona light
            -turn on point light
            -turn on fog
             */
//            rootNode.addLight(lighter);
            rootNode.addLight(torch);
            rootNode.removeLight(sun);
//            dlsr.setShadowIntensity(0.5f);
            cam.lookAtDirection(camDir, new Vector3f(0, 1, 0));
            changeFogParams(strongFog);
            guiNode.attachChild(ch);
            rootNode.detachChild(plModel);
        }
    }

    private void createFog() {
        fpp = new FilterPostProcessor(assetManager);
        viewPort.addProcessor(fpp);
        //Initialize the FogFilter and
        //add it to the FilterPostProcesor.
        fogFilter = new FogFilter();
        fogFilter.setFogColor(new ColorRGBA(0.05f, 0.05f, 0.05f, 0.05f));
        changeFogParams(strongFog);
        fpp.addFilter(fogFilter);
    }

    private void changeFogParams(Vector2f fogParams) {
        fogFilter.setFogDistance(fogParams.x);
        fogFilter.setFogDensity(fogParams.y);
    }

    protected void initCrossHairs() {
        setDisplayStatView(false);
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+"); // crosshairs
        ch.setLocalTranslation(settings.getWidth() / 2 - ch.getLineWidth() / 2, settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
        guiNode.attachChild(ch);
    }

    protected void initMark() {
        Sphere sphere = new Sphere(20, 20, 0.03f);
        mark = new Geometry("BOOM!", sphere);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", ColorRGBA.Black);
        mark.setMaterial(mark_mat);
    }

//    protected void initEffect() {
//        /**
//         * Explosion effect. Uses Texture from jme3-test-data library!
//         */
//        debrisEffect = new ParticleEmitter("Debris", ParticleMesh.Type.Triangle, 10);
//        Material debrisMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
//        debrisMat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/Debris.png"));
//        debrisEffect.setMaterial(debrisMat);
//        debrisEffect.setImagesX(3);
//        debrisEffect.setImagesY(3);
//        debrisEffect.setRotateSpeed(5);
//        debrisEffect.setSelectRandomImage(true);
//        debrisEffect.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 6, 0));
//        debrisEffect.setStartColor(ColorRGBA.Gray);
//        debrisEffect.setEndColor(ColorRGBA.Cyan);
//        debrisEffect.setGravity(0f, 6f, 0f);
//        debrisEffect.getParticleInfluencer().setVelocityVariation(.90f);
//        debrisEffect.emitParticles(1);
//    }
    private ActionListener actionListener = new ActionListener() {

        @Override
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (!pause) {
                if (name.equals("Shoot") && !keyPressed) {
                    // 1. Reset results list.
                    CollisionResults results = new CollisionResults();
                    // 2. Aim the ray from cam loc to cam direction.
                    Ray ray = new Ray(cam.getLocation(), cam.getDirection());
                    // 3. Collect intersections between Ray and Shootables in results list.
                    // DO NOT check collision with the root node, or else ALL collisions will hit the skybox! Always make a separate node for objects you want to collide with.
                    shootables.collideWith(ray, results);
                    // 4. Print the results

                    //nie potrzebne, ale poki co zostawie
                    System.out.println("----- Collisions? " + results.size() + "-----");
                    for (int i = 0; i < results.size(); i++) {
                        // For each hit, we know distance, impact point, name of geometry.
                        float dist = results.getCollision(i).getDistance();
                        Vector3f pt = results.getCollision(i).getContactPoint();
                        hit = results.getCollision(i).getGeometry().getName();
                        System.out.println("* Collision #" + i);
                        System.out.println("  You shot " + hit + " at " + pt + ", " + dist + " wu away.");
//                        Spatial spatial = shootables.getChild(hit);
//                        if(spatial!=null) shootables.detachChild(spatial);
//                        else System.out.println("null");

                    }
                    // 5. Use the results (we mark the hit object)
                    if (results.size() > 0) {

                        CollisionResult closest = results.getClosestCollision();
                        mark.setLocalTranslation(closest.getContactPoint());
                        shootables.detachChild(shootables.getChild(hit).getParent());
                        ParticleWithTimer particle;
                        particle = new ParticleWithTimer(3000, rootNode, assetManager);
                        particles.add(particle);
                        particle.particle.setLocalTranslation(closest.getContactPoint());
                        rootNode.attachChild(mark);

                    } else {
                        // No hits
                        rootNode.detachChild(mark);

                    }
                }
            }
        }
    };

//  protected void CreateTree(){
//      //creating a tree
//    Random rand = new Random();
//    Spatial treeGeo = assetManager.loadModel("Models/Tree/Tree.mesh.j3o");
//    treeGeo.scale(rand.nextInt(5)+3); // make tree bigger
//    treeGeo.setQueueBucket(Bucket.Transparent); // transparent leaves
//    treeGeo.rotate(0,rand.nextInt(360),0);
//    rootNode.attachChild(treeGeo);
//
//    Vector3f treeLoc = new Vector3f(0,0,0);
//    treeLoc.set(-1*rand.nextInt(1000)+500,0,-1*rand.nextInt(1000)+500);
//    treeLoc.setY(terrain.getHeight(new Vector2f( treeLoc.x, treeLoc.z ) ) -100);
//    
//   // TerrainLodControl treecontrol = new TerrainLodControl(treeGeo, getCamera());
//
//    
//    if(treeLoc.y<0){
//        treeGeo.setLocalTranslation(treeLoc);
//    }
//    
//    CollisionShape treeShape = CollisionShapeFactory.createMeshShape(treeGeo);
//    RigidBodyControl trees = new RigidBodyControl(treeShape, 0);
//    //terrain.addControl(control);
//    treeGeo.addControl(trees);
//
//   bulletAppState.getPhysicsSpace().add(treeGeo);
//  }
}
