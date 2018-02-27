package depsolver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


class Package {
    private String name;
    private String version;
    private Integer size;
    private String status;
    private List<List<String>> depends = new ArrayList<>();
    private List<String> conflicts = new ArrayList<>();

    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getVersion() { return version; }
    public Integer getSize() { return size; }
    public List<List<String>> getDepends() { return depends; }
    public List<String> getConflicts() { return conflicts; }

    public void setName(String name) { this.name = name; }
    public void setVersion(String version) { this.version = version; }
    public void setstatus(String status) { this.status = status; }
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
    List<Package> initsPackages;
    List<Package> constsPackages;
    List<Package> repo;
    List<String> commands;


    //---CONSTRUCTOR---------------------------------------------------------------------
    public Main(String fileOne, String fileTwo, String fileThree) throws IOException{
        repo = JSON.parseObject(readFile(fileOne), repoType);
        Package startState = new Package();
        startState.setName("[]");
        startState.setVersion("");
        startState.setSize(0);
        List<String> startStateConflicts = Arrays.asList("[]");
        startState.setConflicts(startStateConflicts);
        repo.add(startState);
        
        inits = convertInitials(JSON.parseObject(readFile(fileTwo), strListType));
        consts = convertConstraints(JSON.parseObject(readFile(fileThree), strListType));
        initsPackages = convertInitialsToPackages(JSON.parseObject(readFile(fileTwo), strListType));
        constsPackages = convertConstraintsToPackages(JSON.parseObject(readFile(fileThree), strListType));
        commands = new ArrayList<>();
    }

    public Package searchRepoForPackage(String pname, String pversion){
        for (Package p : repo) {
            if(p.getName().equals(pname) && reduceVersion(p.getVersion()).equals(reduceVersion(pversion))){
                return p;
            }
        }
        return null;
    }


    public List<Package> searchStateForPackage(List<Package> state, Package p){
        //printCurrentState(state);
        ////System.out.println("Package Passed: " + p.getName() + " version " + p.getVersion());
        for (int i = 0; i < state.size(); i++) {
            ////System.out.println("State Package: " + state.get(i).getName() + " version " + state.get(i).getVersion());
            
            if(state.get(i).getName().equals(p.getName()) && reduceVersion(state.get(i).getVersion()).equals(reduceVersion(p.getVersion()))){
                ////System.out.println("Match on: " + state.get(i).getName() + " version " + state.get(i).getVersion());
                state.remove(i);
                //printCurrentState(state);
                return state;
            }
        }
        //printCurrentState(state);
        state.add(p);
        return state;
    }

    
    public List<Package> convertInitialsToPackages(List<String> initialList){
        List<Package> initsTemp = new ArrayList<Package>();
        

        for (String init : initialList) {
            Package tempP = new Package();
            //System.out.println("init: " + init);
            String[] temp = init.split("=");
            String name = temp[0];
            String version = temp[1];
            
            Package exisingPackage = searchRepoForPackage(name, version);
            if(searchRepoForPackage(name, version) != null){
                tempP.setName(exisingPackage.getName());
                tempP.setVersion(exisingPackage.getVersion());
                tempP.setSize(exisingPackage.getSize());
                tempP.setDepends(exisingPackage.getDepends());
                tempP.setConflicts(exisingPackage.getConflicts());
            }else{
                tempP.setName(name);
                tempP.setVersion(version);
            }
            initsTemp.add(tempP);
        }
        if(initsTemp.isEmpty()){
            initsTemp.add(searchRepoForPackage("[]", ""));
        }
        printCurrentState(initsTemp);
        return initsTemp;
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

    public List<Package> convertConstraintsToPackages(List<String> constraintsList){
        List<Package> constTemp = new ArrayList<Package>();
        

        for (String constr : constraintsList) {
            Package tempP = new Package();
            //System.out.println("constr: " + constr);
            String[] temp = constr.split("=");
            String status = temp[0].substring(0, 1);
            String name = temp[0].substring(1);
            String version = "";
            if(temp.length > 1){
                version = temp[1];
            }
            
            Package exisingPackage = searchRepoForPackage(name, version);
            if(searchRepoForPackage(name, version) != null){
                tempP.setName(exisingPackage.getName());
                tempP.setVersion(exisingPackage.getVersion());
                tempP.setstatus(status);
                tempP.setSize(exisingPackage.getSize());
                tempP.setDepends(exisingPackage.getDepends());
                tempP.setConflicts(exisingPackage.getConflicts());
            }else{
                tempP.setName(name);
                tempP.setVersion(version);
                tempP.setstatus(status);
            }
            constTemp.add(tempP);
        }
        return constTemp;
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
        //System.out.println("");
        //System.out.println("---------Repository is as follows-------------");
        for (Package p : repo) {
          //System.out.printf("package %s version %s\n", p.getName(), p.getVersion());
          //System.out.println("  size:" + p.getSize());
          for (List<String> clause : p.getDepends()) {
            //System.out.printf("  dep:");
            for (String q : clause) {
              //System.out.printf(" %s", q);
            }
            //System.out.printf("\n");
          }
    
          for (String conf : p.getConflicts()){
            //System.out.printf("  conf: %s\n", conf);
          }
        }
        //System.out.println("");
        //System.out.println("");
    
        //System.out.println("---------Current State is as follows-------------");
        for(Install i : inits){
            //System.out.println("Package: " + i.getName() + " = " + i.getVersion());
        }
        //System.out.println("");
        //System.out.println("");
    
        //System.out.println("---------Constraints are as follows-------------");
        for(Constraint c : consts){
            //System.out.println("Constraint: " + c.getName());
            //System.out.println("         V: " + c.getVersion());
            //System.out.println("         S: " + c.getState());
        }
        //System.out.println("");
        //System.out.println("");

        // checkCurrentStateIsFinal();
        // String[] t = {"B",">=","3.1"};
        
        // if(checkCurrentStateSatisfysPackageDependancies(repo, repo.get(0) )){
        //     //System.out.println("True");
        // }else{
        //     //System.out.println("False");
        // }

        ////System.out.println(repo.get(1).getDepends());

        // if(checkConflictAgainstPackage(t, repo.get(1))){
        //     //System.out.println("Conflicts");
        // }else{
        //     //System.out.println("Does NOT Conflict");
        // }

        // if(checkCurrentStatesConflictsForPackage(repo, repo.get(1))){
        //    //System.out.println("Package " + repo.get(1).getName() + " conflicts with a repo package");
        // }else{
        //    //System.out.println("Package " + repo.get(1).getName() + " DOES NOT conflict with the repo");
        // }

        // if(checkPackageConflictAgainstCurrentState(repo, t)){
        //     if(t.length > 1){
        //         //System.out.println("Conflict " + t[0] + " " + t[1] + " " + t[2] + " conflicts with repo");
        //     }else{
        //         //System.out.println("Conflict " + t[0] + " conflicts with repo");
        //     }
        // }else{
        //     if(t.length > 1){
        //         //System.out.println("The current state does NOT conflict with the following " + t[0] + " " + t[1] + " " + t[2]);
        //     }else{
        //         //System.out.println("The current state does NOT conflict with the following " + t[0]);
        //     }
        // }

        //printCurrentState(initsPackages);
        //printNextConfig(nextConfigs(initsPackages));
        LinkedList<List<Package>> test = itterativeDeepening(initsPackages, constsPackages);
        printNextConfig(test);
        stripRouteForCommands(test);
      }

      public void printNextConfig(LinkedList<List<Package>> theList){
        if(theList == null){
            //System.out.println("List is null");
        }else{
            for (List<Package> state : theList) {
                //System.out.print("--> [");

                for(int i = 0; i < state.size(); i++){
                    if(i == state.size()){
                        //System.out.print("{" + state.get(i).getName() + " " + state.get(i).getVersion() + "}");
                    }else{
                        //System.out.print("{" + state.get(i).getName() + " " + state.get(i).getVersion() + "}");
                    }
                }
                //System.out.print("] ");
            }
        }
        //System.out.println(" ");
      }

      public void stripRouteForCommands(LinkedList<List<Package>> route){
        if(route == null){
            //System.out.println("The route is empty");
        }else{
            for(int i = route.size()-1 ; i > 0 ; i--){
                List<Package> stageRight = route.get(i);
                List<Package> stageLeft = route.get(i-1);
                if(stageRight.size()>stageLeft.size()){
                    ////System.out.print("[-" + stageRight.get(stageRight.size()-1).getName() + "=" + stageRight.get(stageRight.size()-1).getVersion() + "]");
                    commands.add("-" + stageRight.get(stageRight.size()-1).getName() + "=" + stageRight.get(stageRight.size()-1).getVersion());
                }else{
                    ////System.out.print("[+" + stageLeft.get(stageLeft.size()-1).getName() + "=" + stageLeft.get(stageLeft.size()-1).getVersion() + "]");
                    commands.add("+" + stageLeft.get(stageLeft.size()-1).getName() + "=" + stageLeft.get(stageLeft.size()-1).getVersion());
                }

            }
        }
        //System.out.println("");
        String jsonString = JSON.toJSONString(commands);
        System.out.println(jsonString);
      }

      public boolean checkCurrentStateMeetsConstraints(List<Package> currentState){
        boolean isFinal = true;
        int constraintsCounter = 0;

        if(!currentState.isEmpty()){
            //System.out.println("Current state is not empty");
            do{
                Package constraint = constsPackages.get(constraintsCounter);
                //System.out.println("Current constrains is: " + constraint.getStatus() + " " + constraint.getName());
                boolean isInstalled = false;
                int counter = 0;
                printCurrentState(currentState);
                do{
                    Package installedPackage = currentState.get(counter);
                    //System.out.println("Checking against: " + installedPackage.getName());

                    if(installedPackage.getName().equals(constraint.getName())){
                        isInstalled = true;
                    }
                    counter++;
                }while(!isInstalled && counter < currentState.size());

                if(isInstalled){
                    if(constraint.getStatus().equals("-")){
                        isFinal=false;
                    }else{constraintsCounter++;}
                }else{
                    if(constraint.getStatus().equals("-")){
                        constraintsCounter++;
                    }else{isFinal=false;}
                }
            }while(isFinal && constraintsCounter < constsPackages.size());
        }else{
            //System.out.println("Current state is empty");
            if(!constsPackages.isEmpty()){
                isFinal = false;
            }
        }

        //this will be replaced by a return type in the end
        if(isFinal){
            //System.out.println("-----------System is in final state-----------");
            return true;
        }else{
            //System.out.println("-----------System does not meet all the constraints-----------");
            return false;
        }
      }

      public boolean checkCurrentStateIsFinal(){
        boolean isFinal = true;
        int constraintsCounter = 0;

        do{
            Constraint constraint = consts.get(constraintsCounter);
            //System.out.println("Current constrains is: " + constraint.getState() + " " + constraint.getName());
            boolean isInstalled = false;
            int counter = 0;
            
            do{
                Install installedPackage = inits.get(counter);
                //System.out.println("Checking against: " + installedPackage.getName());

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
            //System.out.println("-----------System is in final state-----------");
            return true;
        }else{
            //System.out.println("-----------System does not meet all the constraints-----------");
            return false;
        }
      }

      public String reduceVersion(String version){
        if(version.contains(".")){
            ////System.out.println(version.replace(".0","."));
            String temp = version.replace(".0","."); 
            return temp+".";
        }else{
            ////System.out.println(version+".");
            return version+".";
        }
      }


      public String[] understandDependancy(String dependancy){
        if(dependancy.contains("<=")){
            String[] thesplit = dependancy.split("<=");
            String[] understood = {thesplit[0],"<=", thesplit[1]};
            return understood;
        }else if(dependancy.contains(">=")){
            String[] thesplit = dependancy.split(">=");
            String[] understood = {thesplit[0],">=", thesplit[1]};
            return understood;
        }else if(dependancy.contains("<")){
            String[] thesplit = dependancy.split("<");
            String[] understood = {thesplit[0],"<", thesplit[1]};
            return understood;
        }else if(dependancy.contains(">")){
            String[] thesplit = dependancy.split(">");
            String[] understood = {thesplit[0],">", thesplit[1]};
            return understood;
        }else if(dependancy.contains("=")){
            String[] thesplit = dependancy.split("=");
            String[] understood = {thesplit[0],"=", thesplit[1]};
            return understood;
        }else{ //case of just dependancy name
            String[] understood = {dependancy};
            return understood;
        }
      }

    /**
     * Check the current state for the dependancy
     * If the dependancy is met (i.e. the required package is installed) return true
     * If the dependancy is not met return false
     */
      public boolean checkForDependancyInCurrentState(List<Package> currentState, String[] dependancy){
        if(dependancy.length > 1){
            if(dependancy[1].equals("<=")){
                //System.out.println("  Operator was <=");
                for (Package p : currentState) {
                    if(p.getName().equals(dependancy[0])){
                        //System.out.println("  Names matched");
                        String pVersion = reduceVersion(p.getVersion());
                        String dependancyVersion = reduceVersion(dependancy[2]);
                        if((pVersion.compareTo(dependancyVersion)) <= 0 ){
                            //System.out.println("  Dependancy Met");
                            return true;
                        }
                    }
                }
                //System.out.println("  Dependancy Not Met");
                return false;

            }else if(dependancy[1].equals(">=")){
                //System.out.println("  Operator was >=");
                for (Package p : currentState) {
                    if(p.getName().equals(dependancy[0])){
                        //System.out.println("  Names matched");
                        String pVersion = reduceVersion(p.getVersion());
                        String dependancyVersion = reduceVersion(dependancy[2]);
                        if((pVersion.compareTo(dependancyVersion)) >= 0 ){
                            //System.out.println("  Dependancy Met");
                            return true;
                        }
                    }
                }
                //System.out.println("  Dependancy Not Met");
                return false;

            }else if(dependancy[1].equals("<")){
                //System.out.println("  Operator was <");
                for (Package p : currentState) {
                    if(p.getName().equals(dependancy[0])){
                        //System.out.println("  Names matched");
                        String pVersion = reduceVersion(p.getVersion());
                        String dependancyVersion = reduceVersion(dependancy[2]);
                        if((pVersion.compareTo(dependancyVersion)) < 0 ){
                            //System.out.println("  Dependancy Met");
                            return true;
                        }
                    }
                }
                //System.out.println("  Dependancy Not Met");
                return false;

            }else if(dependancy[1].equals(">")){
                //System.out.println("  Operator was >");
                for (Package p : currentState) {
                    if(p.getName().equals(dependancy[0])){
                        //System.out.println("  Names matched");
                        String pVersion = reduceVersion(p.getVersion());
                        String dependancyVersion = reduceVersion(dependancy[2]);
                        if((pVersion.compareTo(dependancyVersion)) > 0 ){
                            //System.out.println("  Dependancy Met");
                            return true;
                        }
                    }
                }
                //System.out.println("  Dependancy Not Met");
                return false;

            }else {     //if(dependancy[1].equals("="))
                //System.out.println("  Operator was =");
                for (Package p : currentState) {
                    if(p.getName().equals(dependancy[0])){
                        //System.out.println("  Names matched");
                        String pVersion = reduceVersion(p.getVersion());
                        String dependancyVersion = reduceVersion(dependancy[2]);
                        if((pVersion.compareTo(dependancyVersion)) == 0 ){
                            //System.out.println("  Dependancy Met");
                            return true;
                        }
                    }
                }
                //System.out.println("  Dependancy Not Met");
                return false;
            }
        }else{ //case of just dependancy name
            //System.out.println("  Operator was none");
            for (Package p : currentState) {
                //System.out.println("  Package is: " + p.getName());
                //System.out.println("  Dependancy is: " + dependancy[0]);

                if(p.getName().equals(dependancy[0])){
                    //System.out.println("  Names matched");
                    //System.out.println("  Dependancy Met");
                    return true;
                }
            }
            //System.out.println("  Dependancy Not Met");
            return false;
        }
    }

    public void printCurrentState(List<Package> currentState){
        //System.out.println("                                        Current state is");
        for (Package p : currentState) {
            //System.out.println("                                        Package: " + p.getName() + " version " + p.getVersion());
        }
    }

    /**
     * Loop through the package toCheck's dependancies checking whether the current state
     * (list of packages installed) satifys the new package's dependancies.
     * 
     * Return false if the package's dependancies are not met
     * Return true if the package's dependancies are met
     */
    public boolean checkCurrentStateSatisfysPackageDependancies(List<Package> currentState, Package toCheck){
        for (List<String> dependancyList : toCheck.getDepends()) {
            boolean indiDep = false;

            for(String dependancy : dependancyList){
                String[] trueDependancy = understandDependancy(dependancy);
                
                //System.out.println("");
                if(trueDependancy.length > 1){
                    //System.out.println("  True dependancy is: " + trueDependancy[0] + " " + trueDependancy[1] + " " + trueDependancy[2]);
                }else{
                    //System.out.println("  True dependancy is: " + trueDependancy[0]);
                }

                //printCurrentState(currentState);
                if(checkForDependancyInCurrentState(currentState, trueDependancy)){
                    indiDep = true;
                }

            }

            if(!indiDep){
                return false;
            }

        }
        if(toCheck.getDepends().size() == 0){
            //System.out.println("  Package " + toCheck.getName() + " version " + toCheck.getVersion() + " has no dependancys");
        }
        return true;
    }

    
    /**
     *  Take a single conflict and check whether the new package (Package toCheck)
     *  satisfys it.
     * 
     *  Return true if package conflicts
     *  Return false if package doesnt conflict
     */
    public boolean checkConflictAgainstPackage(String[] conflict, Package toCheck){
        if(conflict[0].equals(toCheck.getName())){
            if(conflict.length > 1){

                if(conflict[1].equals("<=")){
                    //System.out.println("    Operator was <=");
                    
                    String conflictV = reduceVersion(conflict[2]);
                    String toCheckV = reduceVersion(toCheck.getVersion());
                    if(conflictV.compareTo(toCheckV) >= 0){
                        //System.out.println("    Conflicts");
                        return true;
                    }
                    //System.out.println("    No Conflict");
                    return false;

                }else if(conflict[1].equals(">=")){
                    //System.out.println("    Operator was >=");
                    
                    String conflictV = reduceVersion(conflict[2]);
                    String toCheckV = reduceVersion(toCheck.getVersion());
                    if(conflictV.compareTo(toCheckV) <= 0){
                        //System.out.println("    Conflicts");
                        return true;
                    }
                    //System.out.println("    No Conflict");
                    return false;

                }else if(conflict[1].equals(">")){
                    //System.out.println("    Operator was >");
                    
                    String conflictV = reduceVersion(conflict[2]);
                    String toCheckV = reduceVersion(toCheck.getVersion());
                    if(conflictV.compareTo(toCheckV) < 0){
                        //System.out.println("    Conflicts");
                        return true;
                    }
                    //System.out.println("    No Conflict");
                    return false;

                }else if(conflict[1].equals("<")){
                    //System.out.println("    Operator was <");
                    
                    String conflictV = reduceVersion(conflict[2]);
                    String toCheckV = reduceVersion(toCheck.getVersion());
                    if(conflictV.compareTo(toCheckV) > 0){
                        //System.out.println("    Conflicts");
                        return true;
                    }
                    //System.out.println("    No Conflict");
                    return false;

                }else{ //conflict is "="
                    //System.out.println("    Operator was =");
                    String conflictV = reduceVersion(conflict[2]);
                    String toCheckV = reduceVersion(toCheck.getVersion());
                    if(conflictV.equals(toCheckV)){
                        //System.out.println("    Conflicts");
                        return true;
                    }
                }

            }else{  //conflict is any version of the package
                //System.out.println("    Operator was none");
                //System.out.println("    Conflicts");
                return true;
            }
        }else{  //conflict is not package
            //System.out.println("    Package names did not match");
            //System.out.println("    No Conflict");
            return false;
        }
        return false;
    }

    /** 
     * Loop through the current state (list of packages currently installed) and check
     * whether the new package (Package toCheck) exists as a conflict in the current state
     * 
     * Return true if the package toCheck CONFLICTS with the currentState
     * Return false if the package toCheck DOES NOT CONFLICT with the currentState
    */
    public boolean checkCurrentStatesConflictsForPackage(List<Package> currentState, Package toCheck){
        for (Package p : currentState) {    //for each package in the current state
            //System.out.println("    Checking package: " + p.getName() + " version " + p.getVersion() + " conflicts");
            for (String conflict : p.getConflicts()) {  //check each conflict isnt the package toCheck
                String[] trueConflict = understandDependancy(conflict);
                if(trueConflict.length > 1){
                    //System.out.println("    True conflict is: " + trueConflict[0] + " " + trueConflict[1] + " " + trueConflict[2]);
                }else{
                    //System.out.println("    True conflict is: " + trueConflict[0]);
                }
                if(checkConflictAgainstPackage(trueConflict, toCheck)){
                    return true;
                }
            }
            if(p.getConflicts().size() == 0){
                //System.out.println("    Package " + p.getName() + " version " + p.getVersion() + " has no conflicts");
            }
        }
        return false;
    }

    
    /**
     * Check a single conflict against the current state
     * If the conflict exists in the current state return true
     * If the conflict does NOT exist in the current state return false
     */
    public boolean checkPackageConflictAgainstCurrentState(List<Package> currentState, String[] conflict){
        for(Package cp : currentState){
            if(cp.getName().equals(conflict[0])){
                //package names match
                //System.out.println("      Package names match");

                if(conflict.length > 1){
                    if(conflict[1].equals("<=")){
                        //System.out.println("      Operator was <=");
                    
                        String conflictV = reduceVersion(conflict[2]);
                        String toCheckV = reduceVersion(cp.getVersion());
                        if(conflictV.compareTo(toCheckV) >= 0){
                            //System.out.println("      Conflicts");
                            return true;
                        }
                        //System.out.println("      Packet match doesnt cause a conflict");

                    }else if(conflict[1].equals(">=")){
                        //System.out.println("      Operator was >=");
                    
                        String conflictV = reduceVersion(conflict[2]);
                        String toCheckV = reduceVersion(cp.getVersion());
                        if(conflictV.compareTo(toCheckV) <= 0){
                            //System.out.println("      Conflicts");
                            return true;
                        }
                        //System.out.println("      Packet match doesnt cause a conflict");

                    }else if(conflict[1].equals(">")){
                        //System.out.println("      Operator was >");
                    
                        String conflictV = reduceVersion(conflict[2]);
                        String toCheckV = reduceVersion(cp.getVersion());
                        if(conflictV.compareTo(toCheckV) < 0){
                            //System.out.println("      Conflicts");
                            return true;
                        }
                        //System.out.println("      Packet match doesnt cause a conflict");

                    }else if(conflict[1].equals("<")){
                        //System.out.println("      Operator was <");
                    
                        String conflictV = reduceVersion(conflict[2]);
                        String toCheckV = reduceVersion(cp.getVersion());
                        if(conflictV.compareTo(toCheckV) > 0){
                            //System.out.println("      Conflicts");
                            return true;
                        }
                        //System.out.println("      Packet match doesnt cause a conflict");

                    }else{ //(conflict[1].equals("="))
                        //System.out.println("      Operator was =");
                        String conflictV = reduceVersion(conflict[2]);
                        String toCheckV = reduceVersion(cp.getVersion());
                        if(conflictV.equals(toCheckV)){
                            //System.out.println("      Conflicts");
                            return true;
                        }
                    }
                }else{  //any version of the package
                    //System.out.println("      Operator was none");
                    //System.out.println("      Conflicts");
                    return true;
                }
            }
        }
        //System.out.println("      Conflict does not exist in currentState");
        return false;   //conflict package does not exist in currentState
    }

    
    /**
     * If conflicts exist return true
     * If conflicts dont exist return false
     */
    public boolean checkPackageConflictsExistInCurrentState(List<Package> currentState, Package toCheck){
        for(String conflict : toCheck.getConflicts()){
            String[] trueConflict = understandDependancy(conflict);
            if(trueConflict.length > 1){
                //System.out.println("      True conflict is: " + trueConflict[0] + " " + trueConflict[1] + " " + trueConflict[2]);
            }else{
                //System.out.println("      True conflict is: " + trueConflict[0]);
            }

            if(checkPackageConflictAgainstCurrentState(currentState, trueConflict)){
                return true;
            }
        }
        //System.out.println("      Package " + toCheck.getName() + " version " + toCheck.getVersion() + " doesnt conflict");
        return false;
    }

    


    public LinkedList<List<Package>> nextConfigs(List<Package> currentState){
        LinkedList<List<Package>> nextConfigList = new LinkedList<List<Package>>();
        //List<Package> nextConfig = new ArrayList<Package>();
        
        for (Package p : repo) { //for each package in the repository
            List<Package> nextConfig = new ArrayList(currentState);
            printCurrentState(nextConfig);
            //System.out.println("");
            //System.out.println("1. Checking current state satisfys Package " + p.getName() + " version " + p.getVersion() + " dependancy's");
            if(checkCurrentStateSatisfysPackageDependancies(currentState, p)){ //does the current state satisfy the new package's dependancys
                //System.out.println("");
                //System.out.println("  2. Checking whether the current state accepts " + p.getName() + " version " + p.getVersion() );
                if(!(checkCurrentStatesConflictsForPackage(currentState, p))){ //does the new package conflict with the current state
                    //System.out.println("");
                    //System.out.println("    3. Checking Package " + p.getName() + " version " + p.getVersion() + " has conflicts in the current state");
                    if(!(checkPackageConflictsExistInCurrentState(currentState, p))){ //check the new package's conflict's do not exist in the current state
                        
                        //System.out.println("      4. Package " + p.getName() + " version " + p.getVersion() + " has been added/removed");
                        //System.out.println("");
                        nextConfig = searchStateForPackage(nextConfig, p); // check that the new state isnt already in the list, if it is remove it rather than add it.
                    }else{
                        //System.out.println("    3. Package " + p.getName() + " version " + p.getVersion() + " conflicts with a package in the current state");
                        //System.out.println("");
                        //return null;    
                    }
                }else{
                    //System.out.println("  2. Package " + p.getName() + " version " + p.getVersion() + " conflicts with the current state");
                    //System.out.println("");
                    //return null;    
                }
            }else{
                //System.out.println("1. Current State does not contain the dependancies for " + p.getName() + " version " + p.getVersion());
                //System.out.println("");
                //return null;
            }
            ////System.out.println(nextConfig.size());
            ////System.out.println(currentState.size());
            if(nextConfig.size() != currentState.size()){
                printCurrentState(nextConfig);
                //System.out.println("=========================NEXTCONF DIFF LENGTH==========================");
                nextConfigList.add(nextConfig);
            }
        }
        return nextConfigList;
    }

    public LinkedList<List<Package>> depthFirst(List<Package> currentState, List<Package> constraints, int depth){
        if(depth == 0){
            return null;
        }else if(checkCurrentStateMeetsConstraints(currentState)){
            LinkedList<List<Package>> route = new LinkedList<List<Package>>();
            route.add(currentState);
            return route;
        }else{
            LinkedList<List<Package>> result = nextConfigs(currentState);
            int counter = 1;
            for (List<Package> config : result) {
                //System.out.println("Config " + counter + " of " + result.size());
                for (Package p : config) {
                    //System.out.println("Config contains package: " + p.getName() + " version " + p.getVersion());
                }
                counter++;
            }
            for(List<Package> nextConfig : result){
                LinkedList<List<Package>> route = depthFirst(nextConfig, constraints, depth-1);
                if(route!=null){
                    route.add(currentState);
                    return route;
                }
            }
            return null;
        }
    }

    public LinkedList<List<Package>> itterativeDeepening(List<Package> currentState, List<Package> constraints){
        for(int depth = 1; depth < 15 ; depth++){
            //System.out.println("");
            //System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Current Depth is " + depth);
            LinkedList<List<Package>> finalState = depthFirst(currentState, constraints, depth);
            if(finalState != null){return finalState;}
        }
        return null;
    }


    //---MAIN METHOD---------------------------------------------------------------------
    public static void main(String[] args) throws IOException {
          Main m = new Main(args[0], args[1], args[2]);
          m.printTestInfo();
    }
}