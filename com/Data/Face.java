package com.Data;

/**
 *
 * @author j0ker
 */
public class Face {
    
    public Vector3d vertex1, vertex2, vertex;
    
    public Face(Vector3d vertex) {
        this.vertex = vertex;
    }
    
    public Face(Vector3d vertex, Vector3d vertex1, Vector3d vertex2) {
        this.vertex = vertex;
        this.vertex1 = vertex1;
        this.vertex2 = vertex2;
    }
}