package berlin.ong.student;
import com.jme3.app.SimpleApplication;
import com.jme3.material.*;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

import java.util.ArrayList;
import java.util.List;


public class Bots {
    private int x_axis;
    private int y_axis;
    private int z_axis;
    private String name;
    private int x_vector;
    private int y_vector;
    private int z_vector;
    private Invade mat;
    private Geometry ufo;


    public Bots(int x_axis, int y_axis, int z_axis, String name, int x_vector, int y_vector, int z_vector, Invade mainInstance) {
        this.x_axis = x_axis;
        this.y_axis = y_axis;
        this.z_axis = z_axis;
        this.x_vector = x_vector;
        this.y_vector = y_vector;
        this.z_vector = z_vector;

        this.name = name;
        initializeMainInstance(mainInstance);

    }


    private void initializeMainInstance(Invade mainInstance){
        Box box = new Box(x_axis, y_axis, z_axis);
        ufo = new Geometry(name, box);
        this.mat = mainInstance;
        ufo.setMaterial(mat.getmat1());
        ufo.setLocalTranslation(new Vector3f(x_vector, y_vector, z_vector));
    }

    public Geometry getUfoGeometry(){
        return ufo;
    }




}

