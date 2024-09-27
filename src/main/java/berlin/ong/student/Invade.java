package berlin.ong.student;

import com.jme3.bounding.BoundingBox;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.FastMath;
import com.jme3.renderer.RenderManager;
import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * This is the Main Class of your Game. It should boot up your game and do initial initialisation
 * Move your Logic into AppStates or Cont * Move your Logic into AppStates or Controls or other java classesrols or other java classes
 */
public class Invade extends SimpleApplication {
    private Material mat1_1;
    private Material mat2_1;
    private Material mat3;
    private Material mat4;
    private Material mat5;
    private Material whiteMaterial;

    private Invade mainInstance;

    private Player player;
    private Bots ufo;

    private final Node pivot = new Node("pivot");

    private final List<Bots> botsList = new ArrayList<>();
    private final List<Shot> shotList = new ArrayList<>();
    private final List<Shot> shotsDePlayer = new ArrayList<>();
    private final List<Node> pivotShotList = new ArrayList<>();
    private final List<Node> pivotDePlayer = new ArrayList();


    private float frustumSize;
    private float aspect;
    private float timeSinceLastMove = 0;
    private float timeSinceLastShot = 0;
    private float timeSinceLost = 0;
    private float timeSinceLastShotDePlayer = 0;
    private float timeSinceLastSkinChange = 0;

    private boolean didBroLoose = false;
    private boolean didBroShoot = false;
    private boolean didBroHit = false;

    private InvadeControllerReceiver client;

    private int howManySteps = 0;
    private int skin = 0; // Animation purposes
    private final int gotShot = 0;
//    private int x = 0;

    private Shot shot;
    private Shot shotDePlayer;
    private Vector3f shootVector;
    private Vector3f shootDePlayer;

    // Collision and hit logic
    private final CollisionResults results = new CollisionResults();
    private final CollisionResults resultsDePlayer = new CollisionResults();

    private Pepe pepe;

    public static void main(String[] args) {
        Invade app = new Invade();
        app.setShowSettings(false);
        AppSettings settings = new AppSettings(true);
        settings.setWidth(1440); // pixels
        settings.setHeight(900); // pixel^2
        settings.setTitle("My Space Invader Game");
        app.setSettings(settings);
        app.setShowSettings(false);

        app.start();
    }

    @Override
    public void simpleInitApp() {
        client = new InvadeControllerReceiver("192.168.178.66", 8080, "/ws");

        cam.setParallelProjection(true); // orthographic mode

        aspect = (float) cam.getWidth() / cam.getHeight();
        frustumSize = 32f; // Size of the frustum

        // frustum dimensions (space/plane you are able to see)
        cam.setFrustum(-0, 1000, -frustumSize * aspect, frustumSize * aspect, frustumSize, -frustumSize);

        cam.setLocation(new Vector3f(0, 0, 10)); // Position the camera above the 2D plane
        cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y); // Look at the center of the scene and horizontally at the x-axis

        flyCam.setEnabled(false);

        // Initializing material for different geometries (because it only works in simpleInitApp())
        mainInstance = this;
        mainInstance.mat1_1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mainInstance.mat1_1.setTexture("ColorMap", assetManager.loadTexture("Textures/Alien1-1.png"));

        whiteMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        whiteMaterial.setColor("Color", ColorRGBA.BlackNoAlpha);

        mainInstance.mat2_1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mainInstance.mat2_1.setTexture("ColorMap", assetManager.loadTexture("Textures/player.png"));

        mat3 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat3.setTexture("ColorMap", assetManager.loadTexture("Textures/FinalAhText.png"));

        mainInstance.mat4 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mainInstance.mat4.setColor("Color", ColorRGBA.White);

        mat5 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat5.setTexture("ColorMap", assetManager.loadTexture("Textures/gamerover.png"));

        // Creating ufos
        domainExpansion_ufosVoid();
        // Creating player
        createPlayerUfo();
        keys();
        // Creating background (either white or black)
        domainExpansionBlack();
        // Creating Text and Title
        createText();
        // Creating first shot for hit logic
        domainExpansionCreateShot();
        domainExpansionPlayerShot();

        int gotShot = shot.getShotGeometry().getModelBound().collideWith(player.getPlayer().getWorldBound(), results);

        client.listen(pepe = new Pepe(player));
    }

    @Override
    public void simpleUpdate(float tpf) {
        timeSinceLastMove += tpf;
        timeSinceLastShot += tpf;
        timeSinceLost += tpf;
        timeSinceLastShotDePlayer += tpf;
        timeSinceLastSkinChange += tpf;

        // Hindering the ufos from moving too fast
        if (timeSinceLastMove >= 0.5 && howManySteps <= 9) {
            moveBots();
            timeSinceLastMove = 0;
            howManySteps++;

        }

        if (timeSinceLastMove >= 0.5 && howManySteps > 9 && howManySteps <= 20) {
            moveBotsMinusOne();
            howManySteps++;
            timeSinceLastMove = 0;

            if (howManySteps == 20) {
                howManySteps = 0;
            }
        }

        if (timeSinceLastShot >= 3 && !didBroLoose) {
            domainExpansionCreateShot();
            timeSinceLastShot = 0;
        }

        for (Node pivot : pivotShotList) {
            pivot.move(0, -0.3f, 0);

            for (Shot shot : shotList) {
                pivot.attachChild(shot.getShotGeometry());
                shot.getShotGeometry().updateModelBound(); // Update bounding box after moving
                // Perform collision check
                results.clear();
                shot.getShotGeometry().getWorldBound().collideWith(player.getHitBox(), results);

                if (results.size() > 0) {
                    createLosingScreen();
                    timeSinceLost = 0;
                    didBroLoose = true;
                }
            }

        }

        for (Node pivot : pivotDePlayer) {
            pivot.move(0, +0.3f, 0);

            for (Shot shot : shotsDePlayer) {
                if (didBroShoot) {
                    shot.getShotGeometry().setLocalTranslation(0, 0, 1);
                    pivot.attachChild(shot.getShotGeometry());
                    shot.getShotGeometry().updateModelBound();
                }

//                if (didBroHit) {
//                    Vector3f shotGoBack = shot.getShotGeometry().getLocalTranslation();
//                    shot.getShotGeometry().setLocalTranslation(shotGoBack.x, shotGoBack.y, shotGoBack.z - 40);
//                    didBroHit = false;
//                }
            }

        }

        for (Bots bots : botsList) {

            Geometry ufoGeometry = bots.getUfoGeometry();
            if (ufoGeometry.getWorldBound().collideWith(shotDePlayer.getShotGeometry().getWorldBound(), resultsDePlayer) > 0) {
                Vector3f currentPosition = ufoGeometry.getLocalTranslation();
                ufoGeometry.setLocalTranslation(currentPosition.x, currentPosition.y, currentPosition.z - 15);
                didBroHit = true;

                resultsDePlayer.clear();
            }

        }

        if (timeSinceLastShotDePlayer >= 1.5 && didBroShoot) {
            domainExpansionPlayerShot();
            timeSinceLastShotDePlayer = 0;
            didBroShoot = false;
        }

        pepe.onRotationChange(new Vector3f(0f,0.2f,0f));


    }

    @Override
    public void simpleRender(RenderManager rm) {

    }

    public void domainExpansion_ufosVoid() {
        String[] names = {"ufo_one", "ufo_two", "ufo_three", "ufo_four", "ufo_five",
                "ufo_six", "ufo_seven", "ufo_eight", "ufo_nine"};

        int[] vectors = {0, 8, -8, 16, -16, -24, 24, -32, 32};

        int y = 18;

        for (int j = 0; j <= 2; j++) {
            for (int i = 0; i < vectors.length; i++) {
                ufo = new Bots(3, 3, 1, names[i], vectors[i], y, 1, mainInstance);
                botsList.add(ufo); // Adding to the list they can all move
                rootNode.attachChild(pivot);
                pivot.setLocalTranslation(-5, 0, 0);

            }

            y = y - 6;

        }
    }

    public void domainExpansionBlack() {
        Box whiteBackground = new Box(1440, 900, -1);
        Geometry background = new Geometry("Back", whiteBackground);
        background.setMaterial(whiteMaterial);
        Node pivo1 = new Node("pivot");
        rootNode.attachChild(pivo1);
        pivo1.attachChild(background);

    }

    public void moveBots() {
        pivot.move(1f, 0, 0);
        if (skin == 0) {
            mainInstance.mat1_1.setTexture("ColorMap", assetManager.loadTexture("Textures/Alien1-1-3.png"));
            skin = 1;
        } else {
            mainInstance.mat1_1.setTexture("ColorMap", assetManager.loadTexture("Textures/Alien1-1.png"));
            skin = 0;
        }
        for (Bots bot : botsList) {
            pivot.attachChild(bot.getUfoGeometry());
            bot.getUfoGeometry().updateModelBound();
        }
    }

    public void moveBotsMinusOne() {
        pivot.move(-1, 0, 0);
        if (skin == 0) {
            mainInstance.mat1_1.setTexture("ColorMap", assetManager.loadTexture("Textures/Alien1-1-3.png"));
            skin = 1;
        } else {
            mainInstance.mat1_1.setTexture("ColorMap", assetManager.loadTexture("Textures/Alien1-1.png"));
            skin = 0;
        }
        for (Bots bot : botsList) {
            pivot.attachChild(bot.getUfoGeometry());
            bot.getUfoGeometry().updateModelBound();
        }
    }

    public void createText() {
        Box textbox = new Box(12, 3, 0);
        Geometry textGeoBox = new Geometry("letterbox", textbox);
        textGeoBox.setMaterial(mat3);
        rootNode.attachChild(textGeoBox);
        textGeoBox.move((-frustumSize + 8) * aspect, frustumSize - 4, 0);

    }

    public void createLosingScreen() {
        Box box = new Box(56, 24, 0);
        Geometry lLose = new Geometry("letterbox", box);
        lLose.setMaterial(mat5);
        lLose.setLocalTranslation(0, 0, 5);
        rootNode.attachChild(lLose);
    }

    public void createPlayerUfo() {
        player = new Player(4, 2, 5, "PLAYER", mainInstance);
        rootNode.attachChild(player.getPlayer());
        player.getPlayer().updateModelBound();
    }

    public void domainExpansionCreateShot() {
        shot = new Shot(0.3f, 1, 5, mainInstance);

        // Picking the vectors/coords for the starting point of the shooting projectile
        int randNum = (int) (Math.random() * botsList.size());
        Vector3f vectorShot = botsList.get(randNum).getUfoGeometry().getLocalTranslation();
        shootVector = vectorShot.add(pivot.getLocalTranslation());

        // Creating pivot for the shooting projectile
        Node pivot1 = new Node("pivot1");
        pivot1.setLocalTranslation(shootVector);
        rootNode.attachChild(pivot1);

        // All the shots created in a list for the method in line 135
        shotList.add(shot);
        pivotShotList.add(pivot1);

    }

    // Nearly the same principle as the shots coming from the ufos
    public void domainExpansionPlayerShot() {
        shotDePlayer = new Shot(0.3f, 1, 5, mainInstance);
        Vector3f vectorShot = player.getPlayer().getLocalTranslation();
        Node pivot2 = new Node("pivot2");
        pivot2.setLocalTranslation(vectorShot);
        pivot2.move(0, 1, 0);
        rootNode.attachChild(pivot2);
        shotsDePlayer.add(shotDePlayer);
        pivotDePlayer.add(pivot2);

    }

    private void keys() {
        inputManager.addMapping("Shoot", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(analogListener, "Shoot");
    }

    final private AnalogListener analogListener = new AnalogListener() {
        @Override
        public void onAnalog(String name, float value, float tpf) {
            if (true) {
                if (name.equals("Shoot")) {
                    didBroShoot = true;
                }
            }
        }
    };

//    public void switchSkin() {
//        if (x == 0) {
//            mainInstance.mat1_1.setTexture("ColorMap", assetManager.loadTexture("Textures/Alien1-1.png"));
//            x = 1;
//        } else if (x == 1) {
//            mainInstance.mat1_1.setTexture("ColorMap", assetManager.loadTexture("Textures/Alien1-1-2.png"));
//            x = 2;
//        } else if (x == 2) {
//            mainInstance.mat1_1.setTexture("ColorMap", assetManager.loadTexture("Textures/Alien1-1-3.png"));
//            x = 3;
//        } else if (x == 3) {
//            mainInstance.mat1_1.setTexture("ColorMap", assetManager.loadTexture("Textures/Alien1-1-4.png"));
//            x = 4;
//        } else if (x == 4) {
//            mainInstance.mat1_1.setTexture("ColorMap", assetManager.loadTexture("Textures/Alien1-1-5.png"));
//            x = 5;
//        } else if (x == 5) {
//            mainInstance.mat1_1.setTexture("ColorMap", assetManager.loadTexture("Textures/Alien1-1-6.png"));
//            x = 0;
//        }
//
//    }


    public Material getmat1() {
        return mainInstance.mat1_1;
    }

    public Material getMat2() {
        return mainInstance.mat2_1;
    }

    public Material getMat4() {
        return mainInstance.mat4;
    }



}
