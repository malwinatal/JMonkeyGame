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
import com.jme3.light.PointLight;
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
  private boolean left = false, right = false, up = false, down = false, pause = false, gamebreak=false, returnk = false;
  private Vector3f camDir = new Vector3f();
  private Vector3f camLeft = new Vector3f();
  private FilterPostProcessor fpp;
  private FogFilter fogFilter;
  private PointLight lighter;
  private DirectionalLight sun = new DirectionalLight();

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
    CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(0.3f, 1.5f, 1);
    cam.setFrustumNear(0.5f);
    player = new CharacterControl(capsuleShape, 0.05f);
    player.setJumpSpeed(20);
    player.setFallSpeed(30);
    player.setGravity(30);
    player.setPhysicsLocation(new Vector3f(27, 5, 24));
    flyCam.setMoveSpeed(10);
    //flyCam.setLocation(new Vector3f(1,1,1));
    //flyCam.setLocation();
    
    //Generate the Maze
    Maze labirynt = new Maze(10,10, assetManager, rootNode, bulletAppState);
    try {
        labirynt.generateMaze();
    } catch (IOException ex) {
        Logger.getLogger(Terrain.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    bulletAppState.getPhysicsSpace().add(player);
    
    //for(int i=0;i<20;i++){
    //    CreateTree();
    //}
     //You must add a light to make the model visible
     
     sun.setDirection(new Vector3f(0f, -5f, 0));
     lighter = new PointLight(new Vector3f(27,1,24),15);
     
     rootNode.addLight(lighter);
    
    CreateFog();
  }
  
  protected void CreateTree(){
      //creating a tree
    Random rand = new Random();
    Spatial treeGeo = assetManager.loadModel("Models/Tree/Tree.mesh.j3o");
    treeGeo.scale(rand.nextInt(5)+3); // make tree bigger
    treeGeo.setQueueBucket(Bucket.Transparent); // transparent leaves
    treeGeo.rotate(0,rand.nextInt(360),0);
    rootNode.attachChild(treeGeo);

    Vector3f treeLoc = new Vector3f(0,0,0);
    treeLoc.set(-1*rand.nextInt(1000)+500,0,-1*rand.nextInt(1000)+500);
    treeLoc.setY(terrain.getHeight(new Vector2f( treeLoc.x, treeLoc.z ) ) -100);
    
   // TerrainLodControl treecontrol = new TerrainLodControl(treeGeo, getCamera());

    
    if(treeLoc.y<0){
        treeGeo.setLocalTranslation(treeLoc);
    }
    
    CollisionShape treeShape = CollisionShapeFactory.createMeshShape(treeGeo);
    RigidBodyControl trees = new RigidBodyControl(treeShape, 0);
    //terrain.addControl(control);
    treeGeo.addControl(trees);

   bulletAppState.getPhysicsSpace().add(treeGeo);
  }
  
  protected void CreateFog(){
    fpp = new FilterPostProcessor(assetManager);
    viewPort.addProcessor(fpp);
    //Initialize the FogFilter and
    //add it to the FilterPostProcesor.
    fogFilter = new FogFilter();
    //fogFilter.setFogColor(new ColorRGBA(1, 0, 0, 1));
    fogFilter.setFogColor(new ColorRGBA(0.05f, 0.05f, 0.05f, 0.05f));

    fogFilter.setFogDistance(50);
    fogFilter.setFogDensity(4.4f);
    
    
    
    fpp.addFilter(fogFilter);
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
    } else if (binding.equals("Pause")){
      pause = isPressed;
    }
    else if (binding.equals("Return")){
      returnk = isPressed;
    }
  }

@Override
    public void simpleUpdate(float tpf) {
        
        if(!gamebreak){
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
        
        if (pause) {
            //bulletAppState.getPhysicsSpace().remove(player);
            gamebreak=true;

                rootNode.addLight(sun);
                fogFilter.setFogDistance(500);
                fogFilter.setFogDensity(0.4f);
                cam.setLocation(new Vector3f(0,7,0));
                
        }

        }
        
        else
        {
            if (returnk)
            {
                gamebreak=false;
                fogFilter.setFogDistance(50);
                fogFilter.setFogDensity(4.4f);
                rootNode.removeLight(sun);
                

                
                
            }
        }
        
        //System.out.println(gamebreak);
    }
}