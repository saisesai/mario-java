package jade;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL30.*;

public class LevelEditorScene extends Scene {

    private String vertexShaderSource = "#version 330 core\n" +
            "\n" +
            "layout (location=0) in vec3 aPos;\n" +
            "layout (location=1) in vec4 aColor;\n" +
            "\n" +
            "out vec4 fColor;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    fColor = aColor;\n" +
            "    gl_Position = vec4(aPos, 1.0);\n" +
            "}\n";

    private String fragmentShaderSource = "#version 330 core\n" +
            "\n" +
            "in vec4 fColor;\n" +
            "\n" +
            "out vec4 color;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    color = fColor;\n" +
            "}\n";

    private int vertexID, fragmentID, shaderProgram;

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

    public LevelEditorScene() {

    }

    @Override
    public void init() {
        // =========================
        // Compile and link shaders
        // =========================

        // 1. Load and compile the vertex shader
        this.vertexID = glCreateShader(GL_VERTEX_SHADER);
        // Pass the shader source to the GPU
        glShaderSource(this.vertexID, this.vertexShaderSource);
        glCompileShader(this.vertexID);
        // Check for errors in compilation
        int success = glGetShaderi(this.vertexID, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(this.vertexID, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: 'defaultShader.glsl'\n\tVertex shader compilation failed.");
            System.out.println(glGetShaderInfoLog(this.vertexID, len));
            assert false : "";
        }

        // 2. Load and compile the fragment shader
        this.fragmentID = glCreateShader(GL_FRAGMENT_SHADER);
        // Pass the shader source to the GPU
        glShaderSource(this.fragmentID, this.fragmentShaderSource);
        glCompileShader(this.fragmentID);
        // Check for errors in compilation
        success = glGetShaderi(this.fragmentID, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(this.fragmentID, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: 'defaultShader.glsl'\n\tFragment shader compilation failed.");
            System.out.println(glGetShaderInfoLog(this.fragmentID, len));
            assert false : "";
        }

        // 3. Link shaders
        this.shaderProgram = glCreateProgram();
        glAttachShader(this.shaderProgram, this.vertexID);
        glAttachShader(this.shaderProgram, this.fragmentID);
        glLinkProgram(this.shaderProgram);
        // Check for linking errors
        success = glGetProgrami(this.shaderProgram, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            int len = glGetProgrami(this.shaderProgram, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: 'defaultShader.glsl'\n\tShader program linking failed.");
            System.out.println(glGetProgramInfoLog(this.shaderProgram, len));
            assert false : "";
        }

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
        glUseProgram(this.shaderProgram);

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
        glUseProgram(0);
    }
}
