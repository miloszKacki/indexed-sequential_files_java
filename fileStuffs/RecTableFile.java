package fileStuffs;

import exceptions.PageSizeExceededException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RecTableFile {

    public static int saveOperationCount = 0;
    public static int readOperationCount = 0;
    private RandomAccessFile RAFile;
    private File theFile;
    private long lenghthP;
    public RecTableFile(String path){
        lenghthP = 0;
        theFile = new File(path);
        try {
            if(theFile.exists()){
                theFile.delete();
                theFile.createNewFile();
            }
            RAFile = new RandomAccessFile(theFile,"rw");

        } catch (FileNotFoundException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }
        //to avoid indexOutOfBoundsAtTheBegining
        this.savePage(RecTableFile.createEmptyPage(),0);

    }

    public void close() {
        try {
            RAFile.close();
        } catch (IOException e) {
            System.err.println(e);
        }

    }

    public void savePage(RecordRow[] recordRows,long index){
        if (recordRows.length*RecordRow.size > IdxSeqFile.pageSize) throw new PageSizeExceededException();
        byte[] bytePage = new byte[IdxSeqFile.pageSize];
        for (int i=0;i<recordRows.length;i++){
            System.arraycopy(
                    recordRows[i].toBytes(),
                    0,
                    bytePage,
                    i*RecordRow.size,
                    RecordRow.size);
        }

        savePage(bytePage,index);
    }

    public RecordRow[] getPage(long index){
        byte[] bytePage = getPageBytes(index),
                byteRow = new byte[RecordRow.size];
        RecordRow[] output = new RecordRow[IdxSeqFile.pageSize/RecordRow.size];

        for(int i=0;i<IdxSeqFile.pageSize/RecordRow.size;i++){
            System.arraycopy(bytePage,i*RecordRow.size,byteRow,0,RecordRow.size);
            output[i] = RecordRow.RecordRowFromBytes(byteRow);
        }

        return output;
    }

    public void savePage(byte[] bytes,long index){
        if (index > lenghthP) throw new IndexOutOfBoundsException();
        if (bytes.length > IdxSeqFile.pageSize) throw new PageSizeExceededException();
        try {
            RAFile.seek(index*IdxSeqFile.pageSize);
            RAFile.write(bytes);
        } catch (IOException e) {
            System.err.println(e);
        }
        if(index == lenghthP) lenghthP++;
        saveOperationCount++;
    }
    public byte[] getPageBytes(long index){
        byte[] output = new byte[IdxSeqFile.pageSize];
        if (index >= lenghthP) throw new IndexOutOfBoundsException();
        try {
            RAFile.seek(index*IdxSeqFile.pageSize);
            RAFile.read(output);
        } catch (IOException e) {
            System.err.println(e);
        }
        readOperationCount++;
        return output;
    }

    public static RecordRow[] createEmptyPage(){
        RecordRow[] output = new RecordRow[IdxSeqFile.pageSize/RecordRow.size];
        for(int i=0;i<output.length;i++){
            output[i] = RecordRow.randomRecRow();
            output[i].stateByte = RecordRow.EMPTY_STATE;
        }
        return output;
    }

    public long getLengthP() {
        return lenghthP;
    }
    public long getPageOffset(long PIndex) {
        return PIndex*IdxSeqFile.pageSize;
    }

    public void printToConsole(){
        System.out.println("====Printing "+theFile.getPath()+"====");
        for (int i=0;i<lenghthP;i++){
            RecordRow[] tmpPage = getPage(i);
            System.out.println("==Page "+(i+1)+"==");
            for (int j=0;j<IdxSeqFile.pageSize/RecordRow.size;j++){
                System.out.println(tmpPage[j]);
            }
        }
    }
}
