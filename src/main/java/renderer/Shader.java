package renderer;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;

public class Shader {

    private int shaderProgramID;
    private String vertexShaderSource;
    private String fragmentShaderSource;
    private String filePath;

    public Shader(String filepath) {
        this.filePath = filepath;
        try {
            String source = new String(Files.readAllBytes(Paths.get(this.filePath)));
            String[] splitString = source.split("(#type)( )+([a-zA-Z]+)");

            // Find the first pattern after "#type 'pattern'
            int index = source.indexOf("#type") + 6;
            int eol = source.indexOf("\r\n", index);
            String firstPattern = source.substring(index, eol).trim();

            // Find the second pattern after "#type 'pattern'
            index = source.indexOf("#type", eol) + 6;
            eol = source.indexOf("\r\n", index);
            String secondPattern = source.substring(index, eol).trim();

            if (firstPattern.equals("vertex")) {
                this.vertexShaderSource = splitString[1];
            } else if (firstPattern.equals("fragment")) {
                this.fragmentShaderSource = splitString[1];
            } else {
                throw new IOException("Unexpected token: '" + firstPattern + "'");
            }

            if (secondPattern.equals("vertex")) {
                this.vertexShaderSource = splitString[2];
            } else if (secondPattern.equals("fragment")) {
                this.fragmentShaderSource = splitString[2];
            } else {
                throw new IOException("Unexpected token: '" + secondPattern + "'");
            }

            System.out.println("vertex shader:\n" + this.vertexShaderSource);
            System.out.println("fragment shader:\n" + this.fragmentShaderSource);


        } catch (IOException e) {
            e.printStackTrace();
            assert false : "Error: Couldn't open file for shader: '" + "'";
        }
    }

    public void compile() {

        int vertexID, fragmentID;

        // =========================
        // Compile and link shaders
        // =========================

        // 1. Load and compile the vertex shader
        vertexID = glCreateShader(GL_VERTEX_SHADER);
        // Pass the shader source to the GPU
        glShaderSource(vertexID, this.vertexShaderSource);
        glCompileShader(vertexID);
        // Check for errors in compilation
        int success = glGetShaderi(vertexID, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(vertexID, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '" + this.filePath + "'\n\tVertex shader compilation failed.");
            System.out.println(glGetShaderInfoLog(vertexID, len));
            assert false : "";
        }

        // 2. Load and compile the fragment shader
        fragmentID = glCreateShader(GL_FRAGMENT_SHADER);
        // Pass the shader source to the GPU
        glShaderSource(fragmentID, this.fragmentShaderSource);
        glCompileShader(fragmentID);
        // Check for errors in compilation
        success = glGetShaderi(fragmentID, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(fragmentID, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '" + this.filePath + "'\n\tFragment shader compilation failed.");
            System.out.println(glGetShaderInfoLog(fragmentID, len));
            assert false : "";
        }

        // 3. Link shaders
        this.shaderProgramID = glCreateProgram();
        glAttachShader(this.shaderProgramID, vertexID);
        glAttachShader(this.shaderProgramID, fragmentID);
        glLinkProgram(this.shaderProgramID);
        // Check for linking errors
        success = glGetProgrami(this.shaderProgramID, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            int len = glGetProgrami(this.shaderProgramID, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '" + this.filePath + "'\n\tShader program linking failed.");
            System.out.println(glGetProgramInfoLog(this.shaderProgramID, len));
            assert false : "";
        }
        glDeleteShader(vertexID);
        glDeleteShader(fragmentID);
    }

    public void use() {
        glUseProgram(this.shaderProgramID);
    }

    public void detach() {
        glUseProgram(0);
    }

    public void uploadMatrix4f(String varName, Matrix4f mat4) {
        int varLocation = glGetUniformLocation(this.shaderProgramID, varName);
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(4 * 4);
        mat4.get(matBuffer);
        glUniformMatrix4fv(varLocation, false, matBuffer);
    }


}
