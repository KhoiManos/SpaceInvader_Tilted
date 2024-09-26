package berlin.ong.student;
import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.material.*;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

public class Shot {
    private float x;
    private int y;
    private int z;
    private Invade mat;
    private Geometry shoot;

    public Shot(float x, int y, int z, Invade mainInstance){
        this.x = x;
        this.y = y;
        this.z = z;

        initializeMainInstance(mainInstance);
    }

    private void initializeMainInstance(Invade mainInstance){
        Box shot = new Box(x, y, z);
        shoot = new Geometry("shot", shot);
        this.mat = mainInstance;
        shoot.setMaterial(mat.getMat4());
        shoot.setLocalTranslation(0,0,1);
        shoot.setModelBound(new BoundingBox(new Vector3f(0, 0, 0), x, y, z));
        shoot.updateModelBound();
    }

    public Geometry getShotGeometry(){
        return shoot;
    }
}
