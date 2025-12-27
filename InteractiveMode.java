import fileStuffs.IdxSeqFile;
import fileStuffs.ParRecord;
import fileStuffs.RecTableFile;
import fileStuffs.RecordRow;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.Scanner;

public class InteractiveMode {

    private boolean manual;
    private Scanner scanner;

    IdxSeqFile theIdxSeqFile;

//This class will scan input like this:
    //>command
    //arg1, arg2, ...
    //>command
    //arg1, arg2 ...
    //...
//Arguments:
    //newFile : path - creates new IdxSeqFile(path)
    //add : ... - saves specified record to theIdxSeqFile
    //get : key - gets record from the file and prints it to the console
    //printFile - prints out theIdxSeqFile
    //reorg - reorganises theIdxSeqFile
    //counts - get the number of disc operations


    public InteractiveMode(boolean isInManualMode) {
        if (isInManualMode) {
            manual = true;
            scanner = new Scanner(System.in);
        }
        else try{
            manual = false;
            File theFile = new File("input.txt");
            scanner = new Scanner(theFile);

        }catch (FileNotFoundException e){
            System.err.println(e);
        }
    }


    public void mainloop() {
        String command;

        while (true) {
            command = getCmd();
            if (command.equals("stop")) break;
            executeCommand(command);
        }

        theIdxSeqFile.close();
    }

    private String getCmd() {
        if(manual)System.out.print(">");
        return getInput();
    }

    private String getInput() {
        return scanner.nextLine();
    }

    private void executeCommand(String command) {
        if (command.equals("newFile")) newIdxSeqFile();
        else if (command.equals("add")) addRec();
        else if (command.equals("get")) getRec();
        else if (command.equals("printFile")) theIdxSeqFile.printToConsole();
        else if (command.equals("reorg")) theIdxSeqFile.reorganise();
        else if (command.equals("counts")) getCounts();
    }

    private void newIdxSeqFile(){
        String path = scanner.nextLine();
        theIdxSeqFile = new IdxSeqFile(path);
    }

    private void addRec(){
        if(manual)System.out.println("InsertOnlyTheKey (y/n)");
        if(scanner.nextLine().equals("y")){
            ParRecord tmpRecord = new ParRecord(scanner.nextInt(),5,5,0.5f);
            theIdxSeqFile.saveRecord(tmpRecord);
            scanner.nextLine();
        }
    }

    private void getCounts(){
        System.out.println("File pages saved: "+ RecTableFile.saveOperationCount+" File pages read: "+ RecTableFile.readOperationCount);
    }

    private void getRec(){
        RecordRow tmp = RecordRow.randomRecRow();
        int key = scanner.nextInt();
        if(!theIdxSeqFile.getRecordByKey(key,tmp))
            System.out.println("Failed to find a record with key: "+key);
        if(manual)System.out.println(tmp.record);
        scanner.nextLine();

    }
}