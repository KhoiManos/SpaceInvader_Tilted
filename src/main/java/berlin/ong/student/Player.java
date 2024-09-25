package berlin.ong.student;

import com.jme3.app.SimpleApplication;
import com.jme3.material.*;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;


public class Player {
    private int x;
    private int y;
    private int z;
    private String name;
    private Invade instance;
    private Geometry ufoplayer;

    public Player(int x, int y, int z, String name, Invade mainInstance) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;

        createPlayerufo(mainInstance);

    }

    public void createPlayerufo(Invade mainInstance) {
        instance = mainInstance;
        Box player = new Box(x, y, z);
        ufoplayer = new Geometry(name, player);
        this.instance = mainInstance;
        ufoplayer.setMaterial(instance.getMat2());
        ufoplayer.setLocalTranslation(0, -15, 0);

    }

    public Geometry getPlayer() {
        return ufoplayer;
    }
}
