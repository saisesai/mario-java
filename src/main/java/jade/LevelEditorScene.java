package jade;

import org.lwjgl.BufferUtils;
import renderer.Shader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL30.*;

public class LevelEditorScene extends Scene {

    private float[] vertexArray = {
            // position         // color
            -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, // Top left
            0.5f, 0.5f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, // Top right
            0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, // Bottom right
            -0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f // Bottom left
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

    @Override
    public void init() {

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
        int vertexSizeBytes = (positionSize + colorSize) * Float.BYTES;
        glVertexAttribPointer(0, positionSize, GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, positionSize * Float.BYTES);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        //glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0); CANNOT DO THIS!
        glBindVertexArray(0);
    }

    @Override
    public void update(float dt) {
        // Bind shader program
        this.defaultShader.use();

        // Bind the VAO that we are using
        glBindVertexArray(this.vaoID);
        // Enable the vertex attribute pointers
        //glEnableVertexAttribArray(0);
        //glEnableVertexAttribArray(1);

        glDrawElements(GL_TRIANGLES, this.elementArray.length, GL_UNSIGNED_INT, 0);

        // Unbind everything
        //glDisableVertexAttribArray(0);
        //glDisableVertexAttribArray(1);
        glBindVertexArray(0);
        this.defaultShader.detach();
    }
}
