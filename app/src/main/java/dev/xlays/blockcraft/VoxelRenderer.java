package dev.xlays.blockcraft;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

public class VoxelRenderer implements GLSurfaceView.Renderer {
    private static final int SIZE=32, HEIGHT=12;
    private final byte[][][] world=new byte[SIZE][HEIGHT][SIZE];
    private final boolean[] moves=new boolean[4];
    private int program, posHandle, colorHandle, mvpHandle;
    private FloatBuffer vertices, colors;
    private int vertexCount;
    private final float[] projection=new float[16], view=new float[16], vp=new float[16], model=new float[16], mvp=new float[16];
    private float px=16,py=7,pz=24,yaw=0,pitch=-12,vertical=0;
    private long last;
    private static final String VS="attribute vec3 aPos; attribute vec3 aColor; uniform mat4 uMVP; varying vec3 vColor; void main(){gl_Position=uMVP*vec4(aPos,1.0);vColor=aColor;}";
    private static final String FS="precision mediump float; varying vec3 vColor; void main(){gl_FragColor=vec4(vColor,1.0);}";
    public VoxelRenderer(){generate();}
    private void generate(){
        Random r=new Random(481516);
        for(int x=0;x<SIZE;x++)for(int z=0;z<SIZE;z++){
            int h=3+(int)(2*Math.sin(x*.28)+2*Math.cos(z*.23)+r.nextDouble()*1.5);
            h=Math.max(1,Math.min(HEIGHT-3,h));
            for(int y=0;y<=h;y++)world[x][y][z]=(byte)(y==h?1:y>h-3?2:3);
            if(r.nextFloat()<.035f && x>2&&z>2&&x<SIZE-3&&z<SIZE-3){
                for(int y=h+1;y<h+4&&y<HEIGHT;y++)world[x][y][z]=4;
                for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)for(int dy=2;dy<=4;dy++){
                    int ax=x+dx,az=z+dz,ay=h+dy;
                    if(ax>=0&&az>=0&&ax<SIZE&&az<SIZE&&ay<HEIGHT&&Math.abs(dx)+Math.abs(dz)+Math.abs(dy-3)<5&&world[ax][ay][az]==0)world[ax][ay][az]=5;
                }
            }
        }
        rebuild();
    }
    private void rebuild(){
        float[] v=new float[SIZE*SIZE*HEIGHT*6*6*3]; float[] c=new float[v.length]; int n=0;
        float[][][] face={{{0,0,0},{0,1,0},{1,1,0},{0,0,0},{1,1,0},{1,0,0}},{{0,0,1},{1,0,1},{1,1,1},{0,0,1},{1,1,1},{0,1,1}},{{0,0,0},{0,0,1},{0,1,1},{0,0,0},{0,1,1},{0,1,0}},{{1,0,0},{1,1,0},{1,1,1},{1,0,0},{1,1,1},{1,0,1}},{{0,1,0},{0,1,1},{1,1,1},{0,1,0},{1,1,1},{1,1,0}},{{0,0,0},{1,0,0},{1,0,1},{0,0,0},{1,0,1},{0,0,1}}};
        float[][] shades={{.72f,.72f,.72f},{.86f,.86f,.86f},{.58f,.58f,.58f},{.68f,.68f,.68f},{1,1,1},{.48f,.48f,.48f}};
        float[][] palette={{.28f,.68f,.20f},{.48f,.30f,.16f},{.48f,.49f,.51f},{.48f,.29f,.12f},{.18f,.52f,.16f},{.20f,.58f,.17f}};
        for(int x=0;x<SIZE;x++)for(int y=0;y<HEIGHT;y++)for(int z=0;z<SIZE;z++){
            int type=world[x][y][z];if(type==0)continue;
            for(int f=0;f<6;f++){
                int nx=x+(f==3?1:f==2?-1:0),ny=y+(f==4?1:f==5?-1:0),nz=z+(f==1?1:f==0?-1:0);
                if(nx>=0&&ny>=0&&nz>=0&&nx<SIZE&&ny<HEIGHT&&nz<SIZE&&world[nx][ny][nz]!=0)continue;
                for(int k=0;k<6;k++){float[] p=face[f][k];v[n]=x+p[0];c[n++]=palette[type-1][0]*shades[f][0];v[n]=y+p[1];c[n++]=palette[type-1][1]*shades[f][1];v[n]=z+p[2];c[n++]=palette[type-1][2]*shades[f][2];}
            }
        }
        vertexCount=n/3;vertices=ByteBuffer.allocateDirect(n*4).order(ByteOrder.nativeOrder()).asFloatBuffer();vertices.put(v,0,n).position(0);
        colors=ByteBuffer.allocateDirect(n*4).order(ByteOrder.nativeOrder()).asFloatBuffer();colors.put(c,0,n).position(0);
    }
    private int shader(int type,String src){int s=GLES20.glCreateShader(type);GLES20.glShaderSource(s,src);GLES20.glCompileShader(s);return s;}
    @Override public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 gl, javax.microedition.khronos.egl.EGLConfig config){
        program=GLES20.glCreateProgram();GLES20.glAttachShader(program,shader(GLES20.GL_VERTEX_SHADER,VS));GLES20.glAttachShader(program,shader(GLES20.GL_FRAGMENT_SHADER,FS));GLES20.glLinkProgram(program);
        posHandle=GLES20.glGetAttribLocation(program,"aPos");colorHandle=GLES20.glGetAttribLocation(program,"aColor");mvpHandle=GLES20.glGetUniformLocation(program,"uMVP");
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);GLES20.glClearColor(.48f,.72f,.94f,1);
    }
    @Override public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 gl,int w,int h){GLES20.glViewport(0,0,w,h);Matrix.perspectiveM(projection,0,70f,(float)w/Math.max(1,h),.1f,100f);}
    @Override public void onDrawFrame(javax.microedition.khronos.opengles.GL10 gl){
        long now=System.nanoTime();float dt=last==0?.016f:Math.min(.05f,(now-last)/1e9f);last=now;
        float a=(float)Math.toRadians(yaw),speed=5*dt;
        if(moves[0]){px-=(float)Math.sin(a)*speed;pz-=(float)Math.cos(a)*speed;}
        if(moves[1]){px+=(float)Math.sin(a)*speed;pz+=(float)Math.cos(a)*speed;}
        if(moves[2]){px-=(float)Math.cos(a)*speed;pz+=(float)Math.sin(a)*speed;}
        if(moves[3]){px+=(float)Math.cos(a)*speed;pz-=(float)Math.sin(a)*speed;}
        vertical-=18*dt;py+=vertical*dt;if(py<5){py=5;vertical=0;}
        px=Math.max(1,Math.min(SIZE-1,px));pz=Math.max(1,Math.min(SIZE-1,pz));
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);
        float lookX=px-(float)Math.sin(a)*(float)Math.cos(Math.toRadians(pitch)),lookY=py+(float)Math.sin(Math.toRadians(pitch)),lookZ=pz-(float)Math.cos(a)*(float)Math.cos(Math.toRadians(pitch));
        Matrix.setLookAtM(view,0,px,py,pz,lookX,lookY,lookZ,0,1,0);Matrix.multiplyMM(vp,0,projection,0,view,0);
        GLES20.glUseProgram(program);vertices.position(0);colors.position(0);
        GLES20.glEnableVertexAttribArray(posHandle);GLES20.glVertexAttribPointer(posHandle,3,GLES20.GL_FLOAT,false,0,vertices);
        GLES20.glEnableVertexAttribArray(colorHandle);GLES20.glVertexAttribPointer(colorHandle,3,GLES20.GL_FLOAT,false,0,colors);
        Matrix.setIdentityM(model,0);Matrix.multiplyMM(mvp,0,vp,0,model,0);GLES20.glUniformMatrix4fv(mvpHandle,1,false,mvp,0);GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,vertexCount);
    }
    public void setMove(String d,boolean b){synchronized(moves){if(d.equals("UP"))moves[0]=b;if(d.equals("DOWN"))moves[1]=b;if(d.equals("LEFT"))moves[2]=b;if(d.equals("RIGHT"))moves[3]=b;}}
    public void look(float x,float y){yaw+=x;pitch=Math.max(-75,Math.min(75,pitch-y));}
    public void jump(){if(py<=5.1f)vertical=7.5f;}
    public void modify(boolean place){
        float a=(float)Math.toRadians(yaw);int x=Math.round(px-(float)Math.sin(a)*2),z=Math.round(pz-(float)Math.cos(a)*2),y=Math.max(0,Math.min(HEIGHT-1,Math.round(py-1)));
        if(x<0||z<0||x>=SIZE||z>=SIZE)return;
        if(place){if(world[x][y][z]==0)world[x][y][z]=1;}else if(world[x][y][z]!=0)world[x][y][z]=0;
        rebuild();
    }
}
