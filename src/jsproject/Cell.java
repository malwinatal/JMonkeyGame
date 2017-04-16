/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsproject;

/**
 *
 * @author Kuba
 */
public class Cell {
    
    public Cell(){
        left = 0;
        right = 0;
        up = 0;
        down = 0;
        visited = 0;
    }
    
    public int left;
    public int right;
    public int up;
    public int down;
    public int visited;
    
}
