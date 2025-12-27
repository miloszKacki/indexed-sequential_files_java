package fileStuffs;

public class PrimArea {
    private RecordRow[] pageBuffer;
    private int pageBuffersIndex;
    private RecTableFile memory;
    private final String path;

    public PrimArea(String path){
        pageBuffer = new RecordRow[IdxSeqFile.pageSize/RecordRow.size];
        pageBuffersIndex = -1; //-1 because we just dont want it to be a Natural number
        memory = new RecTableFile(path);
        this.path = path;
    }

    public RecordRow[] getPage(long pageIdx) {
        if(pageIdx == pageBuffersIndex) return pageBuffer;
        return memory.getPage(pageIdx);
    }
    public long getLengthP(){return memory.getLengthP();}
    public void savePage(RecordRow[] recordRow, long index){
        memory.savePage(recordRow,index);
        if (index == pageBuffersIndex) pageBuffer = recordRow;
    }
    public String getPath() {return path;}
    public long getPageLength(){
        return memory.getLengthP();
    }

    public void printToConsole(){
        memory.printToConsole();
    }
    public void close(){
        memory.close();
    }

}
