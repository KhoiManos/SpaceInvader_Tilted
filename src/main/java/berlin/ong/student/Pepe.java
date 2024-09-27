package berlin.ong.student;

import com.jme3.math.Vector3f;
import org.jetbrains.annotations.NotNull;


public class Pepe implements InvadeControllerReceiver.Listener {
    private Vector3f movingVector;
    private Player player;

    public Pepe(Player player){
        this.player = player;
    }

    @Override
    public void onRotationChange(@NotNull Vector3f rotation) {
        System.out.println(rotation);

        if (rotation.y > 0.1f) {
            float velocity = rotation.y * -2;
            movingVector = new Vector3f(velocity,0,0);
        }

        if (-0.1f > rotation.y) {
            float velocity = rotation.y * -2;
            movingVector = new Vector3f(velocity,0,0);
        }

        player.getPlayer().move(movingVector);


    }

    @Override
    public void onError() {

    }

}
