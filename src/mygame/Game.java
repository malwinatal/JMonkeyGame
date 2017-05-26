package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FogFilter;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.util.SkyFactory;
import jsproject.Map;

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
    private PointLight lighter;
    private DirectionalLight sun = new DirectionalLight();
    private DirectionalLightShadowRenderer dlsr;
    private final int SHADOWMAP_SIZE=512;
    private Map labirynt;
    private BitmapText ch;
    private Geometry mark;
    private Node shootables;

    //Vectors for fog parameters: distance, density
    private static Vector2f strongFog = new Vector2f(50, 6.4f);
    private static Vector2f noFog = new Vector2f(500, 0.4f);

    //Size of Maze. Size of map depends on this value.
    private static int MazeSize = 10;

    public static void main(String[] args) {
        Game app = new Game();
        app.start();
    }

    @Override
    public void simpleInitApp() {

        initCrossHairs();
        initMark();
        shootables = new Node("Shootables");
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        flyCam.setMoveSpeed(10);
        setUpKeys();
        generateLight();
        createFog();
        labirynt = new Map(MazeSize, MazeSize, assetManager, rootNode, bulletAppState, shootables);
        labirynt.buildMap();
        generatePlayer(-2, 5, 2);
        getRootNode().attachChild(SkyFactory.createSky(getAssetManager(), "Textures/Sky/Bright/BrightSky.dds", SkyFactory.EnvMapType.CubeMap));

//    DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, 1024, 2);
//    dlsf.setLight(sun);
//    dlsf.setEnabled(true);
//    FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
//    fpp.addFilter(dlsf);
//    viewPort.addProcessor(fpp);
//    rootNode.setShadowMode(ShadowMode.CastAndReceive);
        rootNode.attachChild(shootables);

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
            lighter.setPosition(new Vector3f(loc.x, loc.y, loc.z));
        }
                //labirynt.moveGolems(player.getPhysicsLocation().x, player.getPhysicsLocation().z);

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
    }

    private void generateLight() {
        /*
        Creation of two lights, one directional for DebugMode,
        one point light which is following the player.
        */
        
        sun.setDirection(new Vector3f(1, -1, 1));
        lighter = new PointLight(new Vector3f(),100);
        rootNode.addLight(lighter);
       
        
        dlsr = new DirectionalLightShadowRenderer(assetManager, SHADOWMAP_SIZE, 3);
        dlsr.setLight(sun);
        dlsr.setShadowIntensity(0.5f);
        dlsr.setShadowZExtend(5f);
        viewPort.addProcessor(dlsr);
        rootNode.addLight(sun);
    }

    private void turnDebugMode(boolean par) {
        if (par) {
            /*
            During debug mode:
            -turn on light for whole scene
            -turn off point light
            -turn off fog
             */
            rootNode.addLight(sun);
            rootNode.removeLight(lighter);
            dlsr.setShadowIntensity(0);
            cam.setLocation(new Vector3f(MazeSize * 2, MazeSize * 2 + 20, MazeSize * 2));
            cam.lookAt(new Vector3f(MazeSize * 2, 0, MazeSize * 2), new Vector3f(0, 1, 0));
            changeFogParams(noFog);
            guiNode.detachChild(ch);

        } else {
            /*
            For normal mode:
            -turn off directiona light
            -turn on point light
            -turn on fog
             */
            rootNode.addLight(lighter);
            rootNode.removeLight(sun);
            dlsr.setShadowIntensity(0.5f);
            cam.lookAtDirection(camDir, new Vector3f(0, 1, 0));
            changeFogParams(strongFog);
            guiNode.attachChild(ch);
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
        Sphere sphere = new Sphere(15, 15, 0.1f);
        mark = new Geometry("BOOM!", sphere);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", ColorRGBA.Red);
        mark.setMaterial(mark_mat);
    }

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
                    System.out.println("----- Collisions? " + results.size() + "-----");
                    for (int i = 0; i < results.size(); i++) {
                        // For each hit, we know distance, impact point, name of geometry.
                        float dist = results.getCollision(i).getDistance();
                        Vector3f pt = results.getCollision(i).getContactPoint();
                        String hit = results.getCollision(i).getGeometry().getName();
                        System.out.println("* Collision #" + i);
                        System.out.println("  You shot " + hit + " at " + pt + ", " + dist + " wu away.");
                        shootables.detachChild(results.getCollision(i).getGeometry());
                    }
                    // 5. Use the results (we mark the hit object)
                    if (results.size() > 0) {
                        // The closest collision point is what was truly hit:
                        CollisionResult closest = results.getClosestCollision();
                        // Let's interact - we mark the hit with a red dot.
                        mark.setLocalTranslation(closest.getContactPoint());
                        rootNode.attachChild(mark);
                    } else {
                        // No hits? Then remove the red mark.
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
