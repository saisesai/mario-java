package jade;

import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import renderer.Shader;
import renderer.Texture;
import util.Time;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL30.*;

public class LevelEditorScene extends Scene {

    private float[] vertexArray = {
            // position         // color              //UV coordinates
            0.0f, 100.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, // Top left
            100.0f, 100.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, // Top right
            100.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, // Bottom right
            0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f // Bottom left
    };

    // IMPORTANT: Must be in counter-clockwise order.
    /*
     * 0(1.3)(2.1)        1(1.2))
     *
     *
     * 3(2.2)             2(1.1)(2.3)
     * */
    private int[] elementArray = {
            2, 1, 0,
            2, 0, 3
    };

    private int vaoID, vboID, eboID;

    private Shader defaultShader;

    public LevelEditorScene() {
        this.defaultShader = new Shader("./assets/shaders/default.glsl");
        this.defaultShader.compile();
    }

    protected Camera camera;
    protected Texture texture;

    @Override
    public void init() {

        this.camera = new Camera(new Vector2f(0.0f, 0.0f));
        this.texture = new Texture("./assets/images/Natsuki.png");

        // ===========================================
        // Generate VAO, VBO, EBO and send them to GPU
        // ===========================================
        this.vaoID = glGenVertexArrays();
        glBindVertexArray(this.vaoID);

        // Create VBO and upload the vertex buffer
        this.vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.vboID);
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(this.vertexArray.length);
        vertexBuffer.put(this.vertexArray).flip();
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // Create EBO and upload the indices
        this.eboID = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.eboID);
        IntBuffer elementBuffer = BufferUtils.createIntBuffer(this.elementArray.length);
        elementBuffer.put(this.elementArray).flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);

        // Add the vertex attribute pointers
        int positionSize = 3;
        int colorSize = 4;
        int uvSize = 2;
        int vertexSizeBytes = (positionSize + colorSize + uvSize) * Float.BYTES;
        glVertexAttribPointer(0, positionSize, GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, positionSize * Float.BYTES);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, uvSize, GL_FLOAT, false, vertexSizeBytes, (positionSize + colorSize) * Float.BYTES);
        glEnableVertexAttribArray(2);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    @Override
    public void update(float dt) {

        this.camera.position.x -= dt * 50.0f;
        this.camera.position.y -= dt * 30.0f;

        // Bind shader program
        this.defaultShader.use();
        this.texture.bind();

        this.defaultShader.uploadMatrix4f("uProjection", this.camera.getProjectionMatrix());
        this.defaultShader.uploadMatrix4f("uView", this.camera.getViewMatrix());
        this.defaultShader.uploadFloat("uTime", Time.getTime());

        this.defaultShader.uploadTexture("TEX_SAMPLER", 0);
        glActiveTexture(GL_TEXTURE0);

        // Bind the VAO that we are using
        glBindVertexArray(this.vaoID);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.eboID);

        glDrawElements(GL_TRIANGLES, this.elementArray.length, GL_UNSIGNED_INT, 0);

        // Unbind everything
        glBindVertexArray(0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        this.defaultShader.detach();
        this.texture.unbind();
    }
}
