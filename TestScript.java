import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class TestScript {
    public static void main(String[] args){

        ArrayList<ArrayList<String>> environment = new ArrayList<>();

        ArrayList<ArrayList<String>> environmentTmp = new ArrayList<ArrayList<String>>(environment);

        String dirt = "D";
        String water = "~";
        String ground = "O";
        String air = "`";
        String seed = "@";
        String wood = "|";
        String leaves = "#";

        //The last completed frame of the environment
        environment = setEnvironment(environment);

        //The intermediate state between frames of the environment
        environmentTmp = setEnvironment(environmentTmp);

        environmentTmp = deepCopyEnvironment(environmentTmp, environment);

        //determines whether or not there are future changes for the environment
        boolean incomplete = true;


        while (incomplete == true){

            printEnvironment(environment);
            
            incomplete = updateEnvironment(environment, environmentTmp, water, ground, air, dirt, seed, wood, leaves);

            try{
                Thread.sleep(5000);
            }

            catch(InterruptedException e){
                e.printStackTrace();
                System.out.println("Sleep interrupted");
            }

        }
        
    }


    public static ArrayList<ArrayList<String>> setEnvironment(ArrayList<ArrayList<String>> environment){
        try (BufferedReader br = new BufferedReader(new FileReader("file.csv"))) {
            String state;

            while ((state = br.readLine()) != null) {
                String[] rawRow = state.split(",");
                ArrayList<String> row = new ArrayList<>();
                for(String element: rawRow){
                    row.add(element);
                }
                environment.add(row);
            }
            return environment;
        }

        //following exception includes the file not being found
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("an I/O exception occured");
            return environment;
        }
    }

    public static ArrayList<ArrayList<String>> deepCopyEnvironment(ArrayList<ArrayList<String>> environment, ArrayList<ArrayList<String>> source){
        environment.clear();
        for(ArrayList<String> row : source){
            ArrayList<String> newRow = new ArrayList<>(row);
            environment.add(newRow);
        }
        return environment;
    }

    public static void printEnvironment(ArrayList<ArrayList<String>> environment){
        System.out.println();
        for(ArrayList<String> row : environment){
            System.out.print("");
            for (String element : row){
                System.out.print(" "+element);
            }
            System.out.println("");
        }
        System.out.println();
    }

    //Uses environment as reference and makes the corresponding changes to environmentTmp until all changes are made
    //When all changes are made, environment takes on the values of environmentTmp as its new frame.
    public static boolean updateEnvironment(ArrayList<ArrayList<String>> environment, ArrayList<ArrayList<String>> environmentTmp, String water, String ground, String air, String dirt, String seed, String wood, String leaves){
        boolean incomplete = false;

        for(int i=environment.size()-1; i>=0; i--){
            for (int j = 0; j < environment.get(i).size(); j++){
                if(i<environment.size()-1){
                    if(environment.get(i).get(j).equals(ground)){
                        if(environment.get(i+1).get(j).equals(water)){
                            incomplete = fall(environmentTmp, i, j, water, ground, environment);
                        }
                        else if(environment.get(i+1).get(j).equals(air)){
                                incomplete = fall(environmentTmp, i, j, air, ground, environment);
                        }
                    }

                    else if(environment.get(i).get(j).equals(dirt)){
                        if(environment.get(i+1).get(j).equals(water)){
                                incomplete = fall(environmentTmp, i, j, water, dirt, environment);
                        }
                        else if(environment.get(i+1).get(j).equals(air)){
                                incomplete = fall(environmentTmp, i, j, air, dirt, environment);
                        }
                    }

                    else if(environment.get(i).get(j).equals(water)){
                        if(environment.get(i+1).get(j).equals(air)){
                            incomplete = fall(environmentTmp, i, j, air, water, environment);
                        }
                        else if(j>0 && environment.get(i+1).get(j-1).equals(air)){
                            incomplete = spread(environmentTmp, i, j, j-1, water, air, environment);
                        }
                        else if(j<environment.get(i).size()-1 && environment.get(i+1).get(j+1).equals(air)){
                            incomplete = spread(environmentTmp, i, j, j+1, water, air, environment);
                        }
                    }
                    if(environment.get(i).get(j).equals(seed)){
                        if(environment.get(i+1).get(j).equals(water)){
                            incomplete = fall(environmentTmp, i, j, water, seed, environment);
                        }
                        else if(environment.get(i+1).get(j).equals(air)){
                                incomplete = fall(environmentTmp, i, j, air, seed, environment);
                        }
                        else if(j<environment.get(i).size()-1 && environment.get(i-1).get(j).equals(air) && environment.get(i+1).get(j).equals(dirt)){
                            incomplete = grow(environmentTmp, i, j, dirt, air, wood, leaves);
                        }
                    }
                }
            }
        }

        environment = deepCopyEnvironment(environment, environmentTmp);
        
        return incomplete;
    }


    
    public static boolean fall(ArrayList<ArrayList<String>> environment, int i, int j, String lighter, String heavier, ArrayList<ArrayList<String>> environmentRef){
        if(environment.get(i+1).get(j) == environmentRef.get(i+1).get(j)){
            environment.get(i).set(j, lighter);
            environment.get(i+1).set(j, heavier);
        }
        return true;
    }

    public static boolean spread(ArrayList<ArrayList<String>> environment, int i, int j, int k, String heavier, String lighter, ArrayList<ArrayList<String>> environmentRef){
        // System.out.println("right side in func");
        if(environment.get(i).get(j) == environmentRef.get(i).get(j) && environment.get(i+1).get(k) == environmentRef.get(i+1).get(k)){
            // System.out.println("Right side in cond");
            environment.get(i).set(j, lighter);
            // System.out.println(i+"->"+j+","+air);
            environment.get(i+1).set(k, heavier);
            // System.out.println(i+"->"+k+","+water);
        }
        
        return true;
    }

    public static boolean grow(ArrayList<ArrayList<String>> environment, int i, int j, String dirt, String air, String wood, String leaves){
        ArrayList<ArrayList<Boolean>> visited = initEnvironmentBoolean(environment);
        int dirtCount = countDirt(visited, environment, i, j, dirt);
        int airCount = countAirAbove(environment, i, j, air);
        int treeHeight = Math.min(dirtCount, airCount);


        environment.get(i).set(j, wood);
        for(int k=1; k<=treeHeight; ++k){
            environment.get(i-k).set(j, leaves);
        }
        System.out.println(treeHeight);

        return true;
    }

    public static int countDirt(ArrayList<ArrayList<Boolean>> visited, ArrayList<ArrayList<String>> environment, int i, int j, String dirt){

        int row = i+1;
        int col = j;

        List<int[]> stack = new ArrayList<>();
        stack.add(new int[]{row, col});

        int count = 0;

        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        while (!stack.isEmpty()) {
            int[] current = stack.remove(stack.size() - 1);
            int x = current[0];
            int y = current[1];

            if (x < 0 || x >= environment.size() || y < 0 || y >= environment.get(0).size() ||
             visited.get(x).get(y)
              || !environment.get(x).get(y).equals("D")) {
                continue;
            }

            visited.get(x).set(y, true);
            count++;

            for (int[] direction : directions) {
                int newX = x + direction[0];
                int newY = y + direction[1];
                stack.add(new int[]{newX, newY});
            }
        }

        return count;
    }

    public static int countAirAbove(ArrayList<ArrayList<String>> environment, int i, int j, String air){
        int count = 0;
        System.out.println("we get here");
        for(int k = i-1; k>=0; k--){
            if(environment.get(k).get(j).equals(air)){
                ++count;
            }
        }
        return count;
    }

    public static ArrayList<ArrayList<Boolean>> initEnvironmentBoolean(ArrayList<ArrayList<String>> environment) {
        int rows = environment.size();
        int cols = environment.get(0).size();
        ArrayList<ArrayList<Boolean>> visited = new ArrayList<>(rows);
        for (int i = 0; i < rows; i++) {
            ArrayList<Boolean> row = new ArrayList<>(cols);
            for (int j = 0; j < cols; j++) {
                row.add(false);
            }
            visited.add(row);
        }
        return visited;
    }
}