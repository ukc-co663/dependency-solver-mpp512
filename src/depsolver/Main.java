package depsolver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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









class Main {
    //---GLOBAL VARIABLES---------------------------------------------------------------------
    TypeReference<List<Package>> repoType = new TypeReference<List<Package>>() {};
    TypeReference<List<String>> strListType = new TypeReference<List<String>>() {};
    List<Package> repo;
    List<String> initial, constraints;


    //---CONSTRUCTOR---------------------------------------------------------------------
    public Main(String fileOne, String fileTwo, String fileThree) throws IOException{
        repo = JSON.parseObject(readFile(fileOne), repoType);
        initial = JSON.parseObject(readFile(fileTwo), strListType);
        constraints = JSON.parseObject(readFile(fileThree), strListType);
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
    
        System.out.println("---------Current State is as follows-------------");
        System.out.println("" + initial.size());
        for(String p : initial){
          System.out.printf("package: %s\n", p);
        }
        System.out.println("");
    
        System.out.println("---------Constraints are as follows-------------");
        System.out.println("" + constraints.size());
        for(String c : constraints){
          System.out.printf("constraint: %s\n", c);
        }
        System.out.println("");
      }








    //---MAIN METHOD---------------------------------------------------------------------
    public static void main(String[] args) throws IOException {
          Main m = new Main(args[0], args[1], args[2]);
          m.printTestInfo();
    }
}