package fileStuffs;

import java.io.EOFException;

public class OvfArea {

    public static final int guardOffset = 0;
    private RecTableFile memory;
    private final String path;
    public OvfArea(String path){
        memory = new RecTableFile(path);
        RecordRow[] firstPage = RecTableFile.createEmptyPage();
        firstPage[0].stateByte = RecordRow.GUARD_STATE;
        memory.savePage(firstPage,0);
        this.path = path;
    }

    //returns true if row placed successfully
    public boolean PlaceAtTheOffset(RecordRow newRecRow, long offset){

        if(offset == guardOffset){
            RecordRow[] firstPage = memory.getPage(0);
            if (firstPage[0].offset == RecordRow.nullOffset){
                long tmpOff = AppendRecRow(newRecRow);
                firstPage = memory.getPage(0);
                firstPage[0].offset = tmpOff;
                memory.savePage(firstPage,0);
                return true;
            }
            else return PlaceAtTheOffset(newRecRow,firstPage[0].offset);
        }

        RecordRow tmpRec = RecordRow.randomRecRow();
        getRecordRow(tmpRec,offset);
        while(newRecRow.record.getKey() > tmpRec.record.getKey()){

            if(tmpRec.offset == RecordRow.nullOffset){
                long tmpOffset = AppendRecRow(newRecRow);
                RecordRow[] page = memory.getPage(offset/ IdxSeqFile.pageSize);
                page[(int) ((offset%IdxSeqFile.pageSize) / RecordRow.size)].offset = tmpOffset;
                memory.savePage(page,offset/ IdxSeqFile.pageSize);
                return true;
            }

            offset = tmpRec.offset;
            getRecordRow(tmpRec,offset);
        }

        if(newRecRow.record.getKey() == tmpRec.record.getKey()) return false;

        newRecRow.offset = AppendRecRow(tmpRec);

        RecordRow[] page = memory.getPage(offset/ IdxSeqFile.pageSize);
        page[(int) ((offset%IdxSeqFile.pageSize) / RecordRow.size)] = newRecRow;
        memory.savePage(page,offset/ IdxSeqFile.pageSize);
        return true;
    }

    public long AppendRecRow(RecordRow recordRow){
        //getLastPage
        //check for empty spot
        //If found place it there
        //otherwise append page

        long offset = memory.getPageOffset(memory.getLengthP() - 1);
        RecordRow[] lastPage = memory.getPage(memory.getLengthP()-1);

        for(int i=0;i<lastPage.length;i++){
            if(lastPage[i].stateByte == RecordRow.EMPTY_STATE){
                lastPage[i] = recordRow;
                memory.savePage(lastPage, memory.getLengthP()-1);
                return offset;
            }
            offset += RecordRow.size;
        }

        lastPage = RecTableFile.createEmptyPage();
        lastPage[0] = recordRow;
        memory.savePage(lastPage, memory.getLengthP()); //appending basically
        return offset;
    }

    //returns false if not found
    public boolean getRecordRow(RecordRow dest, long offset,int key){
        RecordRow tmpRow = RecordRow.randomRecRow();
        if(!getRecordRow(tmpRow,offset)) return false;

        while(tmpRow.offset != RecordRow.nullOffset) {
            if (tmpRow.record.getKey() == key) {
                tmpRow.copyOnto(dest);
                return true;
            }
            if (!getRecordRow(tmpRow, tmpRow.offset)) return false;
        }
        if (tmpRow.record.getKey() == key) {
            tmpRow.copyOnto(dest);
            return true;
        }
        return false;
    };
    public boolean getRecordRow(RecordRow dest, long offset){

        RecordRow[] page = memory.getPage(offset/ IdxSeqFile.pageSize);

        if (page[(int) ((offset%IdxSeqFile.pageSize) / RecordRow.size)].stateByte == RecordRow.EMPTY_STATE)
            return false;

        page[(int) ((offset%IdxSeqFile.pageSize) / RecordRow.size)].copyOnto(dest);
        return true;
    }
    public boolean deleteRecord(int key,long offset){

        //if ()

        return false;
    }
    public long getLengthP(){return memory.getLengthP();}

    public String getPath() {return path;}

    public void printToConsole(){
        memory.printToConsole();
    }

    public void close(){
        memory.close();
    }
    public boolean checkIfValidChain(long offset){

        RecordRow tmpRow = RecordRow.randomRecRow();
        if(!getRecordRow(tmpRow,offset)) return false;
        while(tmpRow.offset != RecordRow.nullOffset)
            if(!getRecordRow(tmpRow, tmpRow.offset)) return false;
        return true;
    }


}
