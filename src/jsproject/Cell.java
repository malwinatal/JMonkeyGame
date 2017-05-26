package jsproject;

public class Cell {

    public Cell() {
        left = 0;
        right = 0;
        up = 0;
        down = 0;
        visited = 0;
        enemy = false;
        gift = false;

    }

    public int left;
    public int right;
    public int up;
    public int down;
    public int visited;
    public boolean enemy;
    public boolean gift;

}
