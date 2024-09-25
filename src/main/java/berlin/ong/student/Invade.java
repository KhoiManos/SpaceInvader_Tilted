package berlin.ong.student;

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
    private Material whiteMaterial;

    private Invade mainInstance;

    private Node pivot = new Node("pivot");

    private List<Bots> botsList = new ArrayList<>();
    private List<Shot> shotList = new ArrayList<>();
    private List<Node> pivotShotList = new ArrayList<>();

    private float frustumSize;
    private float aspect;
    private float timeSinceLastMove = 0;
    private float timeSinceLastShot = 0;

    private int howManySteps = 0;
    private int skin = 0;

    private Vector3f shootVector;


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
        mainInstance.mat2_1.setTexture("ColorMap", assetManager.loadTexture("Textures/playerSpaceInvader.png"));

        mat3 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat3.setTexture("ColorMap", assetManager.loadTexture("Textures/TextText.png"));

        mainInstance.mat4 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mainInstance.mat4.setColor("Color", ColorRGBA.White);

        // Creating ufos
        domainExpansion_ufosVoid();
        // Creating player
        createPlayerUfo();
        // Creating background (either white or black)
        domainExpansionBlack();
        // Creating Text and Title
        createText();
    }

    @Override
    public void simpleUpdate(float tpf) {
        timeSinceLastMove += tpf;
        timeSinceLastShot += tpf;

        // Hindering the ufos from moving too fast
        if (timeSinceLastMove >= 0.6 && howManySteps <= 9) {
            moveBots();
            timeSinceLastMove = 0;
            howManySteps++;

        }

        if (timeSinceLastMove >= 0.6 && howManySteps > 9 && howManySteps <= 20) {
            moveBotsMinusOne();
            howManySteps++;
            timeSinceLastMove = 0;

            if (howManySteps == 20) {
                howManySteps = 0;
            }
        }

        if (timeSinceLastShot >= 3) {
            domainExpansionCreateShot();

            timeSinceLastShot = 0;
        }

        for(Node pivot : pivotShotList){
            pivot.move(0, -0.3f, 0);

            for(Shot shot : shotList){
                pivot.attachChild(shot.getShotGeometry());

            }

        }

    }

    @Override
    public void simpleRender(RenderManager rm) {
        //add render code here (if any)
    }

    public void domainExpansion_ufosVoid() {
        String[] names = {"ufo_one", "ufo_two", "ufo_three", "ufo_four", "ufo_five",
                "ufo_six", "ufo_seven", "ufo_eight", "ufo_nine"};

        int[] vectors = {0, 8, -8, 16, -16, -24, 24, -32, 32};

        int y = 18;

        for (int j = 0; j <= 2; j++) {
            for (int i = 0; i < vectors.length; i++) {
                Bots ufo = new Bots(3, 3, 1, names[i], vectors[i], y, 1, mainInstance);
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
        for (Bots bot : botsList) {
            if (skin == 0) {
                mainInstance.mat1_1.setTexture("ColorMap", assetManager.loadTexture("Textures/Alien1-2.png"));
                skin = 1;
            } else {
                mainInstance.mat1_1.setTexture("ColorMap", assetManager.loadTexture("Textures/Alien1-1.png"));
                skin = 0;
            }
            pivot.attachChild(bot.getUfoGeometry());
        }
    }

    public void createText() {
        Box textbox = new Box(15, 3, 0);
        Geometry textGeoBox = new Geometry("letterbox", textbox);
        textGeoBox.setMaterial(mat3);
        rootNode.attachChild(textGeoBox);
        textGeoBox.move((-frustumSize + 10) * aspect, frustumSize - 4, 0);

    }

    public void createPlayerUfo() {
        Player player = new Player(4, 2, 1, "PLAYER", mainInstance);
        rootNode.attachChild(player.getPlayer());
    }

    public void domainExpansionCreateShot() {
        Shot shot = new Shot(0.5f, 1, 1, mainInstance);

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


    public void moveBotsMinusOne() {
        pivot.move(-1, 0, 0);
        for (Bots bot : botsList) {
            if (skin == 0) {
                mainInstance.mat1_1.setTexture("ColorMap", assetManager.loadTexture("Textures/Alien1-2.png"));
                skin = 1;
            } else {
                mainInstance.mat1_1.setTexture("ColorMap", assetManager.loadTexture("Textures/Alien1-1.png"));
                skin = 0;
            }
            pivot.attachChild(bot.getUfoGeometry());
        }
    }

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