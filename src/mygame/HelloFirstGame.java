package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.bounding.BoundingBox;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import com.jme3.util.SkyFactory.EnvMapType;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.input.ChaseCamera;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.ColorRGBA;
import com.jme3.niftygui.NiftyJmeDisplay;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ButtonClickedEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;


public class HelloFirstGame extends SimpleApplication implements ScreenController{

    public static void main(String[] args) {
        HelloFirstGame app = new HelloFirstGame();
        app.start();
    }
    
    private Kart playerKart;
    private ParticleEmitter smoke;
    private AudioNode playerAudio;
    private float trackTopY;
    private Node trackNode;
    private Spatial car;
    private boolean gameStarted;
    private NiftyJmeDisplay niftyDisplay;
    BulletAppState bullet = new BulletAppState();
    
    @Override
    public void simpleInitApp(){
        //Create GUI
        setDisplayFps(false);
        setDisplayStatView(false);
        inputManager.setCursorVisible(true);
        flyCam.setEnabled(false);
        
        niftyDisplay = new NiftyJmeDisplay(
                assetManager, inputManager, audioRenderer, guiViewPort
        );
        
        guiViewPort.addProcessor(niftyDisplay);
        Nifty nifty = niftyDisplay.getNifty();
        try {
            //nifty.validateXml("Interface/startScreen.xml");
            nifty.fromXml("Interface/startScreen.xml", "start", this);
        } catch (Exception e){
            System.out.println("Error with XML file schema: " + e);
            System.exit(1);
        }
        nifty.subscribeAnnotations(this);
    }

    @NiftyEventSubscriber(id="redKartStart")
    public void onRedKartStart(final String id, final ButtonClickedEvent event) {
        car = assetManager.loadModel("Models/racetrack/car-kart-red.obj");
        guiViewPort.removeProcessor(niftyDisplay);
        gameStarted = true;
        initApp();
    }

    @NiftyEventSubscriber(id="yellowKartStart")
    public void onYellowKartStart(final String id, final ButtonClickedEvent event) {
        car = assetManager.loadModel("Models/racetrack/car-kart-yellow.obj");
        guiViewPort.removeProcessor(niftyDisplay);
        gameStarted = true;
        initApp();
    }
    
    private void initApp(){     
        //Create physics state and attach to app----------------------------------------------------------------
        stateManager.attach(bullet);
        
        //Create sunlight so our images are visible-------------------------------------------------------------
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.1f, -0.7f,-1.0f).normalizeLocal());
        rootNode.addLight(sun);
        
        //Creates Sky texture to create a skybox----------------------------------------------------------------
        Texture skyTex = assetManager.loadTexture("Textures/FullskiesSunset0068.dds");
        Spatial sky = SkyFactory.createSky(assetManager,skyTex,Vector3f.UNIT_XYZ,EnvMapType.CubeMap);
        rootNode.attachChild(sky);

        //Loads racetrack and sets its normals along with physics states----------------------------------------
        Spatial rawTrack = assetManager.loadModel("Models/racetrack/racetrack-racoon.obj");
        Geometry rawCollisionTrack = (Geometry) assetManager.loadModel("Models/racetrack/racetrack-racoon-collision.obj");
        trackNode = new Node("track");
        
        trackNode.attachChild(rawTrack);
        trackNode.attachChild(rawCollisionTrack);
        
        trackNode.depthFirstTraversal(sp -> {
            if (sp instanceof Geometry){
                Geometry g = (Geometry) sp;
                generateNormals(g.getMesh());
                Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                mat.setBoolean("UseMaterialColors", true);
                mat.setColor("Diffuse", ColorRGBA.White);
                mat.setTexture("DiffuseMap", assetManager.loadTexture("Models/racetrack/racetexture.jpg"));
                g.setMaterial(mat);
            }
        });
        
        trackNode.center();
        trackNode.scale(5f);
        
        //Creates and sets physics for racetrack model----------------------------------------------------------
        CollisionShape trackShape = CollisionShapeFactory.createMeshShape(trackNode);
        RigidBodyControl trackPhy = new RigidBodyControl(trackShape, 0f);
        trackPhy.setFriction(5f);

        trackNode.addControl(trackPhy);
        bullet.getPhysicsSpace().add(trackPhy);
        trackNode.detachChild(rawCollisionTrack);
        rootNode.attachChild(trackNode);
        
        BoundingBox bbTrack = (BoundingBox) trackNode.getWorldBound();
        trackTopY = bbTrack.getCenter().y + bbTrack.getYExtent();

        //Create redKart spatial, generate its normals, add physics---------------------------------------------
        playerKart = createKart(car);
        rootNode.attachChild(car);
        
        //Kart effects
        smoke = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
        Material matSmoke = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        matSmoke.setTexture("Texture", assetManager.loadTexture("Effects/smoketrail.png"));
        
        smoke.setMaterial(matSmoke);
        smoke.setImagesX(1);
        smoke.setImagesY(3);
        smoke.setEndColor(new ColorRGBA(1f,1f,1f,1f)); //white
        smoke.setStartColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f)); //gray
        smoke.getParticleInfluencer().setInitialVelocity(new Vector3f(0,0.5f,-2));
        smoke.setFacingVelocity(true);
        smoke.setStartSize(1.5f);
        smoke.setEndSize(0.1f);
        smoke.setGravity(0,0,0);
        smoke.setLowLife(1f);
        smoke.setHighLife(3f);
        smoke.getParticleInfluencer().setVelocityVariation(0.3f);
        smoke.setEnabled(false);
        ((Node)playerKart.getModel()).attachChild(smoke);
        
        //Setting up ambient audio nodes
        Node ambientAudio = new Node("ambientaudio");
        
        AudioNode cheer = new AudioNode(assetManager, "Sounds/crowd-cheer.wav", DataType.Stream);
        cheer.setLooping(true);
        cheer.setPositional(false);
        cheer.setVolume(0.05f);
        ambientAudio.attachChild(cheer);
        cheer.play();
        
        AudioNode racetrack = new AudioNode(assetManager, "Sounds/racecar-background.wav", DataType.Stream);
        racetrack.setLooping(true);
        racetrack.setPositional(false);
        racetrack.setVolume(0.06f);
        ambientAudio.attachChild(racetrack);
        racetrack.play();
        
        rootNode.attachChild(ambientAudio);
        
        //Setting up playerKart audio
        playerAudio = new AudioNode(assetManager, "Sounds/playerkart-engine.wav", DataType.Stream);
        playerAudio.setPositional(false);
        playerAudio.setLooping(true);
        playerAudio.setVolume(0.09f);
        ((Node)playerKart.getModel()).attachChild(playerAudio);

        //Camera settings and helper methods--------------------------------------------------------------------
        
        Vector3f kartPos = playerKart.getVehicleControl().getPhysicsLocation();
        Vector3f behind = new Vector3f(27,  7,  0);

        cam.setLocation(kartPos.add(behind));
        cam.lookAt(kartPos, Vector3f.UNIT_Y);
        
        ChaseCamera chaseCam = new ChaseCamera(cam, playerKart.getModel(), inputManager);
        
        chaseCam.setDefaultDistance(20f);
        chaseCam.setMaxDistance(40f);
        chaseCam.setMinDistance(25f);
        chaseCam.setDefaultVerticalRotation(FastMath.DEG_TO_RAD * 20);
        chaseCam.setDownRotateOnCloseViewOnly(false);
        chaseCam.setHideCursorOnRotate(true);
        chaseCam.setSmoothMotion(true);
        
        inputKeys();
    }
    
    private void inputKeys(){
        inputManager.addMapping("accelerate", new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("reverse", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("steerLeft", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("steerRight", new KeyTrigger(KeyInput.KEY_K));
        
        inputManager.addListener(actionListener, "steerLeft", "steerRight", "accelerate", "reverse");
    }
    
    private boolean forward, reverse, left, right;
    
    final private ActionListener actionListener = new ActionListener(){
        @Override
        public void onAction(String name, boolean isPressed, float tpf){
            switch(name){
                case "accelerate": forward = isPressed; break;
                case "reverse": reverse = isPressed; break;
                case "steerLeft": left = isPressed; break;
                case "steerRight": right = isPressed; break;
            }
        }
    };

    @Override
    public void simpleUpdate(float tpf) {
        if (!gameStarted){
            return;
        }
        
        //Controls for player
        VehicleControl player = playerKart.getVehicleControl();
        if (forward) {
            playerAudio.play();
            smoke.setEnabled(true);
            player.accelerate(1200f);
            player.brake(0f);
        }
        else if (reverse) {
            player.accelerate(-500f);
            player.brake(50f);
        }
        else { player.accelerate(0f); player.brake(0f); smoke.setEnabled(false); playerAudio.pause();}
        
        float steer = 0f;
        if (left) steer = 0.5f;
        if (right) steer = -0.5f;
        player.steer(steer);
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
    
    public static void generateNormals(Mesh mesh) {
        // 1) Grab the raw position buffer
        FloatBuffer posBuffer = (FloatBuffer)
             mesh.getBuffer(Type.Position).getData();
        int vertexCount = posBuffer.limit() / 3;

        // 2) Grab the index buffer (could be ShortBuffer or IntBuffer)
        Buffer rawIdx = mesh.getBuffer(Type.Index).getData();
        IntBuffer idxBuffer;
        if (rawIdx instanceof IntBuffer) {
            idxBuffer = (IntBuffer) rawIdx;
        } else {
            ShortBuffer sb = (ShortBuffer) rawIdx;
            idxBuffer = BufferUtils.createIntBuffer(sb.capacity());
            while (sb.hasRemaining()) {
                idxBuffer.put(sb.get());
            }
            idxBuffer.flip();
        }

        // 3) Prepare a float‐buffer for accumulating normals
        FloatBuffer normBuffer = BufferUtils.createFloatBuffer(vertexCount * 3);
        for (int i = 0; i < vertexCount * 3; i++) normBuffer.put(0f);
        normBuffer.rewind();

        // 4) Walk each triangle, accumulate face normals
        for (int i = 0; i < idxBuffer.limit(); i += 3) {
            int i0 = idxBuffer.get(i),
                i1 = idxBuffer.get(i+1),
                i2 = idxBuffer.get(i+2);

            // read positions
            float x0 = posBuffer.get(i0*3),
                  y0 = posBuffer.get(i0*3+1),
                  z0 = posBuffer.get(i0*3+2);
            float x1 = posBuffer.get(i1*3),
                  y1 = posBuffer.get(i1*3+1),
                  z1 = posBuffer.get(i1*3+2);
            float x2 = posBuffer.get(i2*3),
                  y2 = posBuffer.get(i2*3+1),
                  z2 = posBuffer.get(i2*3+2);

            // cross‐product for face normal
            float ux = x1 - x0, uy = y1 - y0, uz = z1 - z0;
            float vx = x2 - x0, vy = y2 - y0, vz = z2 - z0;
            float nx = uy*vz - uz*vy,
                  ny = uz*vx - ux*vz,
                  nz = ux*vy - uy*vx;

            // accumulate
            for (int vert : new int[]{i0, i1, i2}) {
                int base = vert * 3;
                normBuffer.put(base,   normBuffer.get(base)   + nx);
                normBuffer.put(base+1, normBuffer.get(base+1) + ny);
                normBuffer.put(base+2, normBuffer.get(base+2) + nz);
            }
        }

        // 5) Normalize each normal
        for (int v = 0; v < vertexCount; v++) {
            int b = v * 3;
            float nx = normBuffer.get(b),
                  ny = normBuffer.get(b+1),
                  nz = normBuffer.get(b+2);
            float len = (float)Math.sqrt(nx*nx + ny*ny + nz*nz);
            if (len != 0f) {
                normBuffer.put(b,   nx/len);
                normBuffer.put(b+1, ny/len);
                normBuffer.put(b+2, nz/len);
            }
        }

        // 6) Attach back to mesh
        mesh.setBuffer(Type.Normal, 3, normBuffer);
        mesh.updateBound();
    }
    
    private Kart createKart(Spatial model){
        Kart kart = new Kart(model);
        
        VehicleControl playerKartControl = kart.getVehicleControl();
        
        float wheelRadius = playerKartControl.getWheel(0).getRadius();
        
        CollisionResults results = new CollisionResults();
        Vector3f rayOrigin = new Vector3f(4, trackTopY + 5f, 0);
        Vector3f rayDir = Vector3f.UNIT_Y.negate();
        Ray ray = new Ray(rayOrigin, rayDir);
        
        trackNode.collideWith(ray, results);
        
        float contactY = rayOrigin.y;
        if (results.size() > 0){
            CollisionResult closest = results.getClosestCollision();
            contactY = closest.getContactPoint().getY() + wheelRadius;
        }
        
        BoundingBox bbKart = (BoundingBox) kart.getModel().getWorldBound();
        float halfChassisHeight = bbKart.getYExtent();
        float comOffSet = 0.5f;
        float dropY = contactY - halfChassisHeight + comOffSet;
        
        playerKartControl.setPhysicsLocation(new Vector3f(-18, dropY, -60));
        Quaternion turnRight = new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_Y);
        playerKartControl.setPhysicsRotation(turnRight);
        
        return kart;
    }
        
    private class Kart {
        private final Spatial kartModel;
        private BoxCollisionShape chassisShape;
        private CompoundCollisionShape compound;
        private VehicleControl kart;

        Kart(Spatial model){
            this.kartModel = model;
            generateKartNormals();
            generateKart();
        }
        
        public Spatial getModel(){
            return kartModel;
        }
        
        public VehicleControl getVehicleControl(){
            return kart;
        }

        private void generateKartNormals(){
            kartModel.depthFirstTraversal(spatial -> {
                if (spatial instanceof Geometry) {
                    Geometry geom = (Geometry) spatial;
                    Mesh mesh = geom.getMesh();
                    HelloFirstGame.generateNormals(mesh);
                }
            });
        }

        private void generateKart(){
            kartModel.scale(3f,3f,3f);
            BoundingBox bb = (BoundingBox)kartModel.getWorldBound();
            Vector3f halfExtents = new Vector3f(
                bb.getXExtent(),
                bb.getYExtent(),
                bb.getZExtent()
            );
            
            chassisShape = new BoxCollisionShape(halfExtents);
            compound = new CompoundCollisionShape();
            float comOffSet = halfExtents.y * 0.20f;
            compound.addChildShape(chassisShape, new Vector3f(0,-1f,0));
            kart = new VehicleControl(compound, 250f);
            kartModel.addControl(kart);
            bullet.getPhysicsSpace().add(kart);
            generateKartWheels();
            kart.setLinearDamping(0.5f);
            kart.setAngularDamping(0.5f);
            
            
            for (int i = 0; i < kart.getNumWheels(); i++){
                kart.getWheel(i).setFrictionSlip(5f);
                kart.setSuspensionStiffness(i, 30f);
                kart.setMaxSuspensionForce(i, 10000f);
                kart.setSuspensionDamping(i, 8f);
                kart.setSuspensionCompression(i, 4f);
                kart.setRollInfluence(i, 0f);
            }
        }

        private void generateKartWheels(){
            Vector3f wheelDir = new Vector3f(0,-1,0); //Wheel suspension
            Vector3f wheelAxle = new Vector3f(-1,0,0); //Axle direction
            float susLen = 0.20f; //Suspension Length
            float radius = 0.5f; //Wheel radius
            
            BoundingBox bb = (BoundingBox) kartModel.getWorldBound();
            float halfWidth = bb.getXExtent();
            float halfHeight = bb.getYExtent();
            float halfLength = bb.getZExtent();

            float wheelY = halfHeight;
            float xOff = halfWidth - radius;
            float zOff = halfLength - radius;

            Vector3f connFL = new Vector3f(-xOff, wheelY, +zOff); //Front left location
            Vector3f connFR = new Vector3f(+xOff, wheelY, +zOff); //Front right location
            Vector3f connBL = new Vector3f(-xOff, wheelY, -zOff); //Back left location
            Vector3f connBR = new Vector3f(+xOff, wheelY, -zOff); //Back right location

            kart.addWheel(connFL, wheelDir, wheelAxle, susLen, radius, true);
            kart.addWheel(connFR, wheelDir, wheelAxle, susLen, radius, true);
            kart.addWheel(connBL, wheelDir, wheelAxle, susLen, radius, false);
            kart.addWheel(connBR, wheelDir, wheelAxle, susLen, radius, false);
        }
    }
    
    @Override public void bind(Nifty nifty, Screen screen){System.out.println("[Nifty] bind() called for screen: " + screen.getScreenId());}
    @Override public void onStartScreen(){System.out.println("[Nifty] onStartScreen()");}
    @Override public void onEndScreen(){}
}
