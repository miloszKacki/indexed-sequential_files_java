import fileStuffs.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.io.PrintStream;

public class Main {
    public static void main(String[] args) {


        //IdxSeqFile myFile = new IdxSeqFile("/testIdxSeqFile");

        //myFile.saveRecord(new ParRecord(2,3,3,0.5f));
        //myFile.saveRecord(new ParRecord(1,3,3,0.5f));

        //myFile.printToConsole();



        PrintStream stdout = System.out;
        try {
            PrintStream fileStream = new PrintStream("input.txt");
            System.setOut(fileStream);
        }catch (FileNotFoundException e){
            System.err.println(e);
        }

        int numberOfSamples = 5000;
        Random rand = new Random(77777);
        int[] generatedKeys = new int[numberOfSamples];
        for(int i=1;i<numberOfSamples;i++)
            generatedKeys[i] = Math.abs(rand.nextInt(5000)+1);
        System.out.println("newFile\n" + "/test2");

        for(int i=1;i<numberOfSamples;i++){
            System.out.println("add\n"+"y\n"+generatedKeys[i]);
        }
        for(int i=1;i<numberOfSamples;i++){
            System.out.println("get\n"+generatedKeys[i]);
        }

        System.out.println("counts");
        System.out.println("stop");

        System.setOut(stdout);

        //InteractiveMode app = new InteractiveMode(false);

        for(int testAlphab=1;testAlphab<5;testAlphab++)
            for(float testReorg=0.1f;testReorg<1;testReorg += 0.2f) {
                RecTableFile.readOperationCount = 0;
                RecTableFile.saveOperationCount = 0;
                InteractiveMode app = new InteractiveMode(false);
                System.out.println("Alphab: " + testAlphab + " Reorganisation threshold:" + testReorg);
                IdxSeqFile.acceptableOvfProportion = testReorg;
                IdxSeqFile.alphaB = testAlphab;
                app.mainloop();
                System.out.println(" ");
            }
    }
}

