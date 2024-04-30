import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class TestScript {
    public static void main(String[] args){

        ArrayList<ArrayList<String>> environment = new ArrayList<>();

        ArrayList<ArrayList<String>> environmentTmp = new ArrayList<ArrayList<String>>(environment);

        String water = "~";
        String ground = "O";
        String air = "`";

        environment = setEnvironment(environment);

        environmentTmp = setEnvironment(environmentTmp);

        environmentTmp = deepCopyEnvironment(environmentTmp, environment);

        boolean incomplete = true;

        while (incomplete == true){

            System.out.println("environment");
            printEnvironment(environment);

            System.out.println("environmentTmp");
            printEnvironment(environmentTmp);
            
            incomplete = updateEnvironment(environment, water, ground, air);

            try{
                Thread.sleep(750);
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

    public static boolean updateEnvironment(ArrayList<ArrayList<String>> environment, String water, String ground, String air){
        boolean incomplete = false;
        for(int i=environment.size()-1; i>=0; i--){
            for (int j = 0; j < environment.get(i).size(); j++){
                if(i<environment.size()-1){
                    if(environment.get(i).get(j).equals(ground)){
                        if(environment.get(i+1).get(j).equals(water)){
                            incomplete = fall(environment, i, j, water, ground);
                        }
                        else if(environment.get(i+1).get(j).equals(air)){
                            incomplete = fall(environment, i, j, air, ground);
                        }
                    }
                    else if(environment.get(i).get(j).equals(water)){
                        if(environment.get(i+1).get(j).equals(air)){
                            incomplete = fall(environment, i, j, air, water);
                        }
                    }
                }
            }
        }
        return incomplete;
    }

    public static boolean fall(ArrayList<ArrayList<String>> environment, int i, int j, String lighter, String heavier){
        environment.get(i).set(j, lighter);
        environment.get(i+1).set(j, heavier);
        return true;
    }
}