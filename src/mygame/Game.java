package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.input.KeyInput;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.PointLight;
import com.jme3.math.ColorRGBA;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FogFilter;
import com.jme3.util.SkyFactory;
import jsproject.Map;


public class Game extends SimpleApplication 
        implements ActionListener{

//  private TerrainQuad terrain;
//  Material mat_terrain;

  private BulletAppState bulletAppState;
  private CharacterControl player;
  private Vector3f walkDirection = new Vector3f();
  private boolean left = false, right = false, up = false, down = false, 
                  pause = false, gamebreak=false;
  private Vector3f camDir = new Vector3f();
  private Vector3f camLeft = new Vector3f();
  private FilterPostProcessor fpp;
  private FogFilter fogFilter;
  private PointLight lighter;
  private DirectionalLight sun = new DirectionalLight();
  private Map labirynt;
  
  //Vectors for fog parameters: distance, density
  private static Vector2f strongFog = new Vector2f(50, 6.4f);
  private static Vector2f noFog = new Vector2f(500, 0.4f);
  
  //Size of Maze. Size of map depends on this value.
  private static int MazeSize =10;
  

  public static void main(String[] args) {
      Game app = new Game();
      app.start();
  }

  @Override
  public void simpleInitApp() {
    bulletAppState = new BulletAppState();
    stateManager.attach(bulletAppState); 
    flyCam.setMoveSpeed(10);
    setUpKeys();
    generateLight();
    createFog();
    labirynt = new Map(MazeSize,MazeSize, assetManager, rootNode, bulletAppState);
    labirynt.buildMap();
    generatePlayer(-2, 5, 2);
    getRootNode().attachChild(SkyFactory.createSky(getAssetManager(), "Textures/Sky/Bright/BrightSky.dds", SkyFactory.EnvMapType.CubeMap));
  }
  
  private void setUpKeys() {
    inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
    inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
    inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
    inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
    inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
    inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));
    inputManager.addMapping("Return", new KeyTrigger(KeyInput.KEY_R));
    inputManager.addListener(this, "Left");
    inputManager.addListener(this, "Right");
    inputManager.addListener(this, "Up");
    inputManager.addListener(this, "Down");
    inputManager.addListener(this, "Jump");
    inputManager.addListener(this, "Pause");
    inputManager.addListener(this, "Return");
  
  }

  /** These are our custom actions triggered by key presses.
   * We do not walk yet, we just keep track of the direction the user pressed.
     * @param binding */
  @Override
  public void onAction(String binding, boolean isPressed, float tpf) {
      switch (binding) {
          case "Left":
              left = isPressed;
              break;
          case "Right":
              right= isPressed;
              break;
          case "Up":
              up = isPressed;
              break;
          case "Down":
              down = isPressed;
              break;
          case "Jump":
              if (isPressed) { player.jump(); }
              break;
          case "Pause":
              if (isPressed){
                  pause=!pause;
                  turnDebugMode(pause);
              }           
              break;
          default:
              break;
      }
  }

@Override
    public void simpleUpdate(float tpf) {
        
        if(!pause){
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
    }

    private void generatePlayer(float locx, float locy, float locz) {
        //Create player with his physics
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(0.82f, 1.5f, 1);
        cam.setFrustumNear(0.5f);
        player = new CharacterControl(capsuleShape, 0.05f);
        player.setJumpSpeed(20);
        player.setFallSpeed(30);
        player.setGravity(30);
        player.setPhysicsLocation(new Vector3f(locx, locy, locz));
        bulletAppState.getPhysicsSpace().add(player);
    }

    private void generateLight() {
        /*
        Creation of two lights, one directional for DebugMode,
        one point light which is following the player.
        */
        
        sun.setDirection(new Vector3f(0, -5, 0));
        lighter = new PointLight(new Vector3f(),100);
        rootNode.addLight(lighter);
    }
    
    private void turnDebugMode(boolean par){
        if (par){
            /*
            During debug mode:
            -turn on light for whole scene
            -turn off point light
            -turn off fog
            */
            rootNode.addLight(sun);
            rootNode.removeLight(lighter);
            cam.setLocation(new Vector3f(MazeSize*2,MazeSize*2+20,MazeSize*2));
            cam.lookAt(new Vector3f(MazeSize*2,0,MazeSize*2), new Vector3f(0,1,0));
            changeFogParams(noFog);
        }
        else{
            /*
            For normal mode:
            -turn off directiona light
            -turn on point light
            -turn on fog
            */
            rootNode.addLight(lighter);
            rootNode.removeLight(sun);
            cam.lookAtDirection(camDir, new Vector3f(0,1,0));
            changeFogParams(strongFog);
        }
    }
    
    private void createFog(){
    fpp = new FilterPostProcessor(assetManager);
    viewPort.addProcessor(fpp);
    //Initialize the FogFilter and
    //add it to the FilterPostProcesor.
    fogFilter = new FogFilter();
    fogFilter.setFogColor(new ColorRGBA(0.05f, 0.05f, 0.05f, 0.05f));
    changeFogParams(strongFog);
    fpp.addFilter(fogFilter);
}
    
    private void changeFogParams(Vector2f fogParams){
        fogFilter.setFogDistance(fogParams.x);
        fogFilter.setFogDensity(fogParams.y);
    }
    
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
