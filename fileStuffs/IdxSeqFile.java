package fileStuffs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class IdxSeqFile {
    public static final int pageSize = 100;
    public static int alphaB = 2;
    public static float acceptableOvfProportion = 2.0f;
    private PrimArea primary;
    //private RecTableFile primary;
    private OvfArea overflow;
    private ArrayList<Integer> indexes;
    private int[] idxBuffer;
    private final String path;

    public IdxSeqFile(String path){

        File thisFile = new File(path);
        thisFile.mkdir();

        primary = new PrimArea(path + "/PrimArea.bin");
        //primary = new RecTableFile(path + "/PrimArea.bin");
        overflow = new OvfArea(path + "/OvfArea.bin");
        indexes = new ArrayList<>();
        idxBuffer = new int[idxFilePgeSize];
        Arrays.fill(idxBuffer,nullAtIdxTable);
        this.path = path;
    }
    public boolean getRecordByKey(int key, RecordRow destRecRow){
        int pageIdx = findPageIndexByKey(key);

        if(pageIdx == -1){
            return overflow.getRecordRow(destRecRow,key,OvfArea.guardOffset);
        }

        RecordRow[] page = primary.getPage(pageIdx);
        long tmpOffset = page[0].offset;

        for (RecordRow pageRow : page) {
            if (pageRow.stateByte == RecordRow.EMPTY_STATE) return false;
            if (key == pageRow.record.getKey()) {
                destRecRow.record = pageRow.record;
                return true;
            }
            if (key < pageRow.record.getKey())
                return overflow.getRecordRow(destRecRow,tmpOffset,key);
            tmpOffset = pageRow.offset;
        }
        return overflow.getRecordRow(destRecRow,tmpOffset,key);
    }

    public boolean saveRecord(ParRecord record){
        checkForReorg();

        int pageIdx = findPageIndexByKey(record.getKey());
        RecordRow rowToInsert = new RecordRow(record);

        if(pageIdx == -1) {
            return overflow.PlaceAtTheOffset(rowToInsert,OvfArea.guardOffset);
        }

        RecordRow[] page = primary.getPage(pageIdx);
        long tmpOffset = page[0].offset;

        for(int i=0;i<page.length;i++) {
            if (page[i].stateByte == RecordRow.EMPTY_STATE){
                page[i] = rowToInsert;
                primary.savePage(page,pageIdx);
                if(i==0) appendToIdxs(rowToInsert.record.getKey()); //this happens if its the first record
                return true;
            }
            if(rowToInsert.record.getKey() == page[i].record.getKey()){
                return false;
            }
            if(page[i].record.getKey() > rowToInsert.record.getKey()){
                //No, this won't happen on the firs iteration (if findPageIndexByKey() works)
                if(page[i-1].offset == RecordRow.nullOffset) {
                    page[i - 1].offset = overflow.AppendRecRow(rowToInsert);
                    primary.savePage(page,pageIdx);
                    return true;
                }
                else return overflow.PlaceAtTheOffset(rowToInsert,tmpOffset);
            }

            tmpOffset = page[i].offset;
        }

        //If its the last page, we got to append a page,
        //otherwise, the first rec on the next one has to be bigger - we should add this to overflow
        if (pageIdx == primary.getPageLength()-1){
            RecordRow[] newPage = RecTableFile.createEmptyPage();
            newPage[0] = rowToInsert;
            primary.savePage(newPage,pageIdx+1);
            appendToIdxs(record.getKey());
            return true;
        }
        if(page[page.length-1].offset == RecordRow.nullOffset) {
            page[page.length - 1].offset = overflow.AppendRecRow(rowToInsert);
            primary.savePage(page,pageIdx);
            return true;
        }
        else return overflow.PlaceAtTheOffset(rowToInsert,tmpOffset);
    }

    public void reorganise(){
        PrimArea newPrimary;
        if (primary.getPath().equals(path + "/PrimArea.bin")) newPrimary = new PrimArea(path + "/PrimAreaB.bin");
        else newPrimary = new PrimArea(path+"/PrimArea.bin");

        OvfArea newOverflow;
        if (overflow.getPath().equals(path + "/OvfArea.bin")) newOverflow = new OvfArea(path + "/OvfAreaB.bin");
        else newOverflow = new OvfArea(path + "/OvfArea.bin");

        saveIdxBuffer();
        indexes = new ArrayList<>();

        long idxPrimNew = 0, idxPrimOld=0;
        int idxOnPageNew = 0, idxOnPageOld=0;
        RecordRow[] pageOld = primary.getPage(idxPrimOld),
                pageNew = RecTableFile.createEmptyPage();
        RecordRow tmpRow = RecordRow.randomRecRow();
        overflow.getRecordRow(tmpRow,OvfArea.guardOffset);


        while (true){
            if(tmpRow.offset != RecordRow.nullOffset){
                overflow.getRecordRow(tmpRow,tmpRow.offset);
            }else if (idxOnPageOld < pageSize/RecordRow.size){
                tmpRow = pageOld[idxOnPageOld];
                idxOnPageOld++;
            }else{
                idxOnPageOld = 0;
                idxPrimOld++;
                pageOld = primary.getPage(idxPrimOld);
                tmpRow = pageOld[idxOnPageOld];
                idxOnPageOld++;
            }

            if(idxOnPageNew == alphaB){
                newPrimary.savePage(pageNew,idxPrimNew);
                idxPrimNew++;
                pageNew = RecTableFile.createEmptyPage();
                idxOnPageNew =0;
            }

            //poloz rekord na nowej stronie
            if (tmpRow.stateByte == RecordRow.NORMAL_STATE) {
                if(idxOnPageNew == 0) appendToIdxs(tmpRow.record.getKey());
                tmpRow.copyOnto(pageNew[idxOnPageNew]);
                //because new overflow will be empty
                pageNew[idxOnPageNew].offset = RecordRow.nullOffset;
                idxOnPageNew++;
            }

            //sprawdz, czy plik sie skonczyl
            if((tmpRow.stateByte == RecordRow.EMPTY_STATE ||
                    idxOnPageOld == pageSize/RecordRow.size) &&
                    idxPrimOld == primary.getPageLength()-1
            ){
                //If we break during the creation of the new page, it has to be saved
                if(idxOnPageNew != 0) newPrimary.savePage(pageNew,idxPrimNew);
                break;
            }
        }

        primary.close();
        overflow.close();

        primary = newPrimary;
        overflow = newOverflow;

    }

//    public boolean deleteRecord(int key){
//        int pageIdx = findPageIndexByKey(key);
//
//        if(pageIdx == -1){
//            return overflow.deleteRecord(key,OvfArea.guardOffset);
//        }
//
//        RecordRow[] page = primary.getPage(pageIdx);
//        long tmpOffset = page[0].offset;
//
//        for (int i=0;i < IdxSeqFile.pageSize/RecordRow.size ;i++) {
//            if (page[i].stateByte == RecordRow.EMPTY_STATE) return false;
//            if (key == page[i].record.getKey()) {
//                page[i].stateByte = RecordRow.EMPTY_STATE;
//                primary.savePage(page,pageIdx);
//                if(i == 0) reorganise();
//                return true;
//            }
//            if (key < page[i].record.getKey())
//                return overflow.deleteRecord(key,tmpOffset);
//            tmpOffset = page[i].offset;
//        }
//        return overflow.deleteRecord(key,tmpOffset);
//
//    }
    private static final int nullAtIdxTable = -1;
    private static final int idxFilePgeSize = 4;

    //if key is bigger than the last from keyfile, the last page's index is returned
    //if key is smaller than the first from keyfile, is -1 returned
    private int findPageIndexByKey(int key){

        //if this file is empty, start at the zero page
        if(indexes.isEmpty() && idxBuffer[0] == nullAtIdxTable) return 0;

        int seekIdx = 0, lastPageIdx = -1;

        int[] page = new int[idxFilePgeSize];

        while(seekIdx < indexes.size()){
            copyIdxPage(seekIdx,page);

            for(int i=0;i<idxFilePgeSize;i++){
                if(page[i] == nullAtIdxTable ||page[i] > key) break;
                lastPageIdx++;
            }

            seekIdx += idxFilePgeSize;
        }

        for(int i=0;i<idxFilePgeSize;i++){
            if(idxBuffer[i] == nullAtIdxTable || idxBuffer[i] > key) return lastPageIdx;
            lastPageIdx++;
        }

        return lastPageIdx;
    }


    private void copyIdxPage(int atIdx,int[] dest){
        Arrays.fill(dest, nullAtIdxTable); // this line shouldn't be useful btw

        for(int i=0;i<dest.length;i++){
            if(atIdx+i >= indexes.size()) return;
            dest[i] = indexes.get(atIdx+i);
        }
    }


    private void appendToIdxs(int key){
        for(int i=0;i<idxFilePgeSize;i++){
            if (idxBuffer[i] == nullAtIdxTable){
                idxBuffer[i] = key;
                return;
            }
        }

        for (int each: idxBuffer)
            indexes.add(each);

        idxBuffer[0] = key;
        for(int i=1;i<idxFilePgeSize;i++) idxBuffer[i]=nullAtIdxTable;
    }
    private void saveIdxBuffer(){
        for (int each: idxBuffer)
            indexes.add(each);

        Arrays.fill(idxBuffer, nullAtIdxTable);
    }


    public void printToConsole(){
        System.out.println("========Printing IdxSeq file ========");
        System.out.println("======Primary area======");
        primary.printToConsole();
        System.out.println("======Overflow area======");
        overflow.printToConsole();
        System.out.println("======Index area======");
        for(int each : indexes)System.out.println(each);
        for(int each : idxBuffer)
            if(each != nullAtIdxTable)System.out.println(each);
    }


    private void checkForReorg(){
        if((float) overflow.getLengthP() / primary.getLengthP() > acceptableOvfProportion)
            reorganise();
    }
    public void close(){
        overflow.close();
        primary.close();
    }

}
