package depsolver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

//This class is used to represent a package in the repository
class Package {
    private String name;
    private String version;
    private Integer size;
    private List<List<String>> depends = new ArrayList<>();
    private List<String> conflicts = new ArrayList<>();

    
  
    public String getName() { return name; }
    public String getVersion() { return version; }
    public Integer getSize() { return size; }
    public List<List<String>> getDepends() { return depends; }
    public List<String> getConflicts() { return conflicts; }
    public void setName(String name) { this.name = name; }
    public void setVersion(String version) { this.version = version; }
    public void setSize(Integer size) { this.size = size; }
    public void setDepends(List<List<String>> depends) { this.depends = depends; }
    public void setConflicts(List<String> conflicts) { this.conflicts = conflicts; }
}

//This class is used to represent currently installed packages (originally from initial.json)
class Install{
    private String name;
    private String version;

    public Install(String name, String version){
        this.name = name;
        this.version = version;
    }

    public String getName() { return name; }
    public String getVersion() { return version; }
}

//This class is used to represent a contraint (package to be un/installed originally from constraints.json)
class Constraint {
    private String name;
    private String version;
    private String state;

    public Constraint(String name, String version, String state){
        this.name = name;
        this.version = version;
        this.state = state;
    }

    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getState() { return state; }
}









class Main {
    //---GLOBAL VARIABLES---------------------------------------------------------------------
    TypeReference<List<Package>> repoType = new TypeReference<List<Package>>() {};
    TypeReference<List<Constraint>> constType = new TypeReference<List<Constraint>>() {};
    TypeReference<List<Install>> initType = new TypeReference<List<Install>>() {};
    TypeReference<List<String>> strListType = new TypeReference<List<String>>() {};

    List<Constraint> consts;
    List<Install> inits;
    List<Package> repo;
    List<String> commands;


    //---CONSTRUCTOR---------------------------------------------------------------------
    public Main(String fileOne, String fileTwo, String fileThree) throws IOException{
        repo = JSON.parseObject(readFile(fileOne), repoType);
        inits = convertInitials(JSON.parseObject(readFile(fileTwo), strListType));
        consts = convertConstraints(JSON.parseObject(readFile(fileThree), strListType));
        commands = new ArrayList<>();
    }

    public List<Install> convertInitials(List<String> initialList){
        List<Install> initsTemp = new ArrayList<Install>();
        for(String init : initialList){
            String[] temp = init.split("=");
            String name = temp[0];
            String version = temp[1];
            initsTemp.add(new Install(name, version));
        }
        return initsTemp;
    }

    public List<Constraint> convertConstraints(List<String> constraintList){
        List<Constraint> constsTemp = new ArrayList<Constraint>();
        for(String constraint : constraintList){
            String state = constraint.substring(0, 1);
            String[] temp = constraint.split("=");
            String name = temp[0].substring(1);
            String version = "";
            if(temp.length>1){
            version = temp[1];}
            constsTemp.add(new Constraint(name, version, state));
        }
        return constsTemp;
    }


    //---AUX METHODS---------------------------------------------------------------------
    private String readFile(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        StringBuilder sb = new StringBuilder();
        br.lines().forEach(line -> sb.append(line));
        br.close();
        return sb.toString();
     }

     private void printTestInfo(){
        System.out.println("");
        System.out.println("---------Repository is as follows-------------");
        for (Package p : repo) {
          System.out.printf("package %s version %s\n", p.getName(), p.getVersion());
          System.out.println("  size:" + p.getSize());
          for (List<String> clause : p.getDepends()) {
            System.out.printf("  dep:");
            for (String q : clause) {
              System.out.printf(" %s", q);
            }
            System.out.printf("\n");
          }
    
          for (String conf : p.getConflicts()){
            System.out.printf("  conf: %s\n", conf);
          }
        }
        System.out.println("");
        System.out.println("");
    
        System.out.println("---------Current State is as follows-------------");
        for(Install i : inits){
            System.out.println("Package: " + i.getName() + " = " + i.getVersion());
        }
        System.out.println("");
        System.out.println("");
    
        System.out.println("---------Constraints are as follows-------------");
        for(Constraint c : consts){
            System.out.println("Constraint: " + c.getName());
            System.out.println("         V: " + c.getVersion());
            System.out.println("         S: " + c.getState());
        }
        System.out.println("");
        System.out.println("");

        checkCurrentStateIsFinal();
      }

      public void checkCurrentStateIsFinal(){
        boolean isFinal = true;
        int constraintsCounter = 0;

        do{
            Constraint constraint = consts.get(constraintsCounter);
            System.out.println("Current constrains is: " + constraint.getState() + " " + constraint.getName());
            boolean isInstalled = false;
            int counter = 0;
            
            do{
                Install installedPackage = inits.get(counter);
                System.out.println("Checking against :" + installedPackage.getName());

                if(installedPackage.getName().equals(constraint.getName())){
                    isInstalled = true;
                }
                counter++;
            }while(!isInstalled && counter < inits.size());

            if(isInstalled){
                if(constraint.getState().equals("-")){
                    isFinal=false;
                }else{constraintsCounter++;}
            }else{
                if(constraint.getState().equals("-")){
                    constraintsCounter++;
                }else{isFinal=false;}
            }
        }while(isFinal && constraintsCounter < consts.size());

        //this will be replaced by a return type in the end
        if(isFinal){
            System.out.println("-----------System is in final state-----------");
        }else{
            System.out.println("-----------System does not meet all the constraints-----------");
        }
      }






    //---MAIN METHOD---------------------------------------------------------------------
    public static void main(String[] args) throws IOException {
          Main m = new Main(args[0], args[1], args[2]);
          m.printTestInfo();
    }
}