package dev.xlays.blockcraft;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class GameSurface extends GLSurfaceView {
    private final VoxelRenderer renderer;
    public GameSurface(Context c) {
        super(c);
        setEGLContextClientVersion(2);
        renderer = new VoxelRenderer();
        setRenderer(renderer);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }
    public void setMove(String d, boolean down){renderer.setMove(d,down);}
    public void look(float x,float y){renderer.look(x,y);}
    public void modify(boolean place){renderer.modify(place);}
    public void jump(){renderer.jump();}
}
