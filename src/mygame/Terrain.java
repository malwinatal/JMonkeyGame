package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import java.io.IOException;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FogFilter;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import jsproject.Maze;

/** Sample 10 - How to create fast-rendering terrains from heightmaps,
and how to use texture splatting to make the terrain look good.  */
public class Terrain extends SimpleApplication 
        implements ActionListener{

  private TerrainQuad terrain;
  Material mat_terrain;

  private BulletAppState bulletAppState;
  private RigidBodyControl landscape;
  private CharacterControl player;
  private Vector3f walkDirection = new Vector3f();
  private boolean left = false, right = false, up = false, down = false;
  private Vector3f camDir = new Vector3f();
  private Vector3f camLeft = new Vector3f();
  private FilterPostProcessor fpp;
  private FogFilter fogFilter;

  public static void main(String[] args) {
      Terrain app = new Terrain();
      app.start();
  }

  @Override
  public void simpleInitApp() {
    bulletAppState = new BulletAppState();
    stateManager.attach(bulletAppState); 
    setUpKeys();
    
    //Create player with his physics
    CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(2f, 6f, 1);
    player = new CharacterControl(capsuleShape, 0.05f);
    player.setJumpSpeed(30);
    player.setFallSpeed(30);
    player.setGravity(30);
    player.setPhysicsLocation(new Vector3f(0, -100,0));
    flyCam.setMoveSpeed(50);
    
    //Generate the Maze
    Maze labirynt = new Maze(10,10);
    try {
        labirynt.generateMaze();
    } catch (IOException ex) {
        Logger.getLogger(Terrain.class.getName()).log(Level.SEVERE, null, ex);
    }

    /** 1. Create terrain material and load four textures into it. */
    mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");

    /** 1.1) Add ALPHA map (for red-blue-green coded splat textures) */
    mat_terrain.setTexture("Alpha", assetManager.loadTexture(
            "Textures/Terrain/splat/alphamap.png"));

    /** 1.2) Add GRASS texture into the red layer (Tex1). */
    Texture grass = assetManager.loadTexture(
            "Textures/Terrain/splat/grass.jpg");
    grass.setWrap(WrapMode.Repeat);
    mat_terrain.setTexture("Tex1", grass);
    mat_terrain.setFloat("Tex1Scale", 128f);
    mat_terrain.setTexture("Tex2", grass);
    mat_terrain.setFloat("Tex2Scale", 64f);
    mat_terrain.setTexture("Tex3", grass);
    mat_terrain.setFloat("Tex3Scale", 128f);
    

//    /** 1.3) Add DIRT texture into the green layer (Tex2) */
//    Texture dirt = assetManager.loadTexture(
//            "Textures/Terrain/splat/dirt.jpg");
//    dirt.setWrap(WrapMode.Repeat);
//    mat_terrain.setTexture("Tex2", dirt);
//    mat_terrain.setFloat("Tex2Scale", 16f);

//    /** 1.4) Add ROAD texture into the blue layer (Tex3) */
//    Texture rock = assetManager.loadTexture(
//            "Textures/Terrain/splat/road.jpg");
//    rock.setWrap(WrapMode.Repeat);
//    mat_terrain.setTexture("Tex3", grass);
//    mat_terrain.setFloat("Tex3Scale", 128f);

     /** 2. Create the height map */
    AbstractHeightMap heightmap = null;
    Texture heightMapImage = assetManager.loadTexture(
            "Textures/test.png");
    heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
    heightmap.load();

    /** 3. We have prepared material and heightmap.
     * Now we create the actual terrain:
     * 3.1) Create a TerrainQuad and name it "my terrain".
     * 3.2) A good value for terrain tiles is 64x64 -- so we supply 64+1=65.
     * 3.3) We prepared a heightmap of size 512x512 -- so we supply 512+1=513.
     * 3.4) As LOD step scale we supply Vector3f(1,1,1).
     * 3.5) We supply the prepared heightmap itself.
     */
    int patchSize = 65;
    terrain = new TerrainQuad("my terrain", patchSize, 1025, heightmap.getHeightMap());

    /** 4. We give the terrain its material, position & scale it, and attach it. */
    terrain.setMaterial(mat_terrain);
    terrain.setLocalTranslation(0, -300, 0);
    terrain.setLocalScale(2f, 1f, 2f);
    rootNode.attachChild(terrain);

    /** 5. The LOD (level of detail) depends on were the camera is: */
    TerrainLodControl control = new TerrainLodControl(terrain, getCamera());
    CollisionShape sceneShape = CollisionShapeFactory.createMeshShape(terrain);
    landscape = new RigidBodyControl(sceneShape, 0);
    terrain.addControl(control);
    terrain.addControl(landscape);
    
    bulletAppState.getPhysicsSpace().add(landscape);
    bulletAppState.getPhysicsSpace().add(player);
    
    for(int i=0;i<200;i++){
        CreateTree();
    }
    // You must add a light to make the model visible
    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
    rootNode.addLight(sun);
    
    CreateFog();
  }
  
  protected void CreateTree(){
      //creating a tree
    Random rand = new Random();
    Spatial treeGeo = assetManager.loadModel("Models/Tree/Tree.mesh.j3o");
    treeGeo.scale(rand.nextInt(5)+5); // make tree bigger
    treeGeo.setQueueBucket(Bucket.Transparent); // transparent leaves
    treeGeo.rotate(0,rand.nextInt(360),0);
    rootNode.attachChild(treeGeo);

    Vector3f treeLoc = new Vector3f(0,0,0);

    do{
        treeLoc.set(-1*rand.nextInt(1000)+500,0,-1*rand.nextInt(1000)+500);
        treeLoc.setY(terrain.getHeight(new Vector2f( treeLoc.x, treeLoc.z ) ) -300);
        break;
    }
    while(treeLoc.y<-280);
    
    treeGeo.setLocalTranslation(treeLoc);

   bulletAppState.getPhysicsSpace().add(treeGeo);
  }
  
  protected void CreateFog(){
    fpp = new FilterPostProcessor(assetManager);
    viewPort.addProcessor(fpp);
    //Initialize the FogFilter and
    //add it to the FilterPostProcesor.
    fogFilter = new FogFilter();
    fogFilter.setFogColor(new ColorRGBA(0.8f, 0.7f, 0.6f, 0.9f));
    fogFilter.setFogDistance(110);
    fogFilter.setFogDensity(1.4f);
    fpp.addFilter(fogFilter);
}
  
  
  private void setUpKeys() {
    inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
    inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
    inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
    inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
    inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
    inputManager.addMapping("Camera", new KeyTrigger(KeyInput.KEY_P));
    inputManager.addListener(this, "Left");
    inputManager.addListener(this, "Right");
    inputManager.addListener(this, "Up");
    inputManager.addListener(this, "Down");
    inputManager.addListener(this, "Jump");
    inputManager.addListener(this, "Camera");
  
  }

  /** These are our custom actions triggered by key presses.
   * We do not walk yet, we just keep track of the direction the user pressed. */
  public void onAction(String binding, boolean isPressed, float tpf) {
    if (binding.equals("Left")) {
      left = isPressed;
    } else if (binding.equals("Right")) {
      right= isPressed;
    } else if (binding.equals("Up")) {
      up = isPressed;
    } else if (binding.equals("Down")) {
      down = isPressed;
    } else if (binding.equals("Jump")) {
      if (isPressed) { player.jump(); }
    }
  }

@Override
    public void simpleUpdate(float tpf) {
        camDir.set(cam.getDirection()).multLocal(0.6f);
        camLeft.set(cam.getLeft()).multLocal(0.4f);
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
    }
}
