package sample;

import java.util.HashSet;
import java.util.Set;

class Robot{

    public boolean move(){
        int newRow = currRow+currDir[0];
        int newCol = currCol+currDir[1];
        if(newRow>=0 && newRow<space.length && newCol>=0 && newCol<space[newRow].length  && space[newRow][newCol]!=0){
            currRow=newRow;
            currCol=newCol;
            return true;
        }
        else{
            return false;
        }
    }
    public void turnLeft(){
        if(currDir[0]==1 && currDir[1]==0){
            currDir[0]=0;
            currDir[1]=1;
        }
        else if(currDir[0]==0 && currDir[1]==1){
            currDir[0]=-1;
            currDir[1]=0;
        }
        else if(currDir[0]==-1&&currDir[1]==0){
            currDir[0]=0;
            currDir[1]=-1;
        }
        else if(currDir[0]==0&&currDir[1]==-1){
            currDir[0]=1;
            currDir[1]=0;
        }

    }
    public void turnRight(){
        if(currDir[0]==1 && currDir[1]==0){
            currDir[0]=0;
            currDir[1]=-1;
        }
        else if(currDir[0]==0 && currDir[1]==1){
            currDir[0]=1;
            currDir[1]=0;
        }
        else if(currDir[0]==-1 && currDir[1]==0){
            currDir[0]=0;
            currDir[1]=1;
        }
        else if(currDir[0]==0 && currDir[1]==-1){
            currDir[0]=-1;
            currDir[1]=0;
        }
    }
    public void detect(){
        space[currRow][currCol]=2;
        for(int i=0; i<space.length; i++){
            for(int j=0; j<space[i].length; j++){
                System.out.print(space[i][j] + " ");
            }
            System.out.println();
        }

        System.out.println();
    }

    public int[][] space = {
            {1,1,1,1,1,0,1,1},
            {1,1,1,1,1,0,1,1},
            {1,0,1,1,1,1,1,1},
            {0,0,0,1,0,0,0,0},
            {1,1,1,1,1,1,1,1}
    };
    public int currRow=1, currCol=3;
    public int[] currDir = {0,1};


};

class Solver {
    public void detectSpace(Robot robot) {
        // A number can be added to each visited cell
        // use string to identify the class
        Set<String> set = new HashSet<>();
        int cur_dir = 0;   // 0: up, 90: right, 180: down, 270: left
        backtrack(robot, set, 0, 0, 0);
    }

    public void backtrack(Robot robot, Set<String> set, int i,
                          int j, int cur_dir) {
        String tmp = i + "->" + j;
        if(set.contains(tmp)) {
            return;
        }

        robot.detect();
        set.add(tmp);

        for(int n = 0; n < 4; n++) {
            // the robot can to four directions, we use right turn
            if(robot.move()) {
                // can go directly. Find the (x, y) for the next cell based on current direction
                int x = i, y = j;
                switch(cur_dir) {
                    case 0:
                        // go up, reduce i
                        x = i-1;
                        break;
                    case 90:
                        // go right
                        y = j+1;
                        break;
                    case 180:
                        // go down
                        x = i+1;
                        break;
                    case 270:
                        // go left
                        y = j-1;
                        break;
                    default:
                        break;
                }

                backtrack(robot, set, x, y, cur_dir);
                // go back to the starting position
                robot.turnLeft();
                robot.turnLeft();
                robot.move();
                robot.turnRight();
                robot.turnRight();
            }

            // turn to next direction
            robot.turnRight();
            cur_dir += 90;
            cur_dir %= 360;
        }

    }
}

public class DetectAllGrids {
    public static void main(String[] args) {
        Solver mySolver = new Solver();
        Robot myRobot = new Robot();
        mySolver.detectSpace(myRobot);
    }
}
